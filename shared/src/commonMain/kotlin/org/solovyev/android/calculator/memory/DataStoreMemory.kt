package org.solovyev.android.calculator.memory

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Memory implementation using DataStore for persistence.
 * Supports multiple named registers and an active register.
 */
class DataStoreMemory(private val dataStore: DataStore<Preferences>) : Memory {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val keyActiveRegister = stringPreferencesKey("memory.activeRegister")

    private val _valueReadyEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val valueReadyEvents: SharedFlow<String> = _valueReadyEvents.asSharedFlow()

    private val _activeRegister = MutableStateFlow(MemoryRegisters.DEFAULT_REGISTER)
    override val activeRegister: StateFlow<String> = _activeRegister.asStateFlow()

    private val _registers = MutableStateFlow(
        listOf(MemoryRegisterState(MemoryRegisters.DEFAULT_REGISTER, null))
    )
    override val registers: StateFlow<List<MemoryRegisterState>> = _registers.asStateFlow()

    init {
        scope.launch {
            dataStore.data.collect { preferences ->
                publishSnapshot(preferences)
            }
        }
    }

    override suspend fun store(value: String) {
        dataStore.edit { preferences ->
            val active = activeRegisterFrom(preferences)
            preferences[keyActiveRegister] = active
            val key = registerKey(active)
            val normalized = value.trim()
            if (normalized.isEmpty()) {
                preferences.remove(key)
            } else {
                preferences[key] = normalized
            }
        }
    }

    override suspend fun add(value: String) {
        val delta = value.trim()
        if (delta.isEmpty()) return
        dataStore.edit { preferences ->
            val active = activeRegisterFrom(preferences)
            preferences[keyActiveRegister] = active
            val key = registerKey(active)
            val current = preferences[key]
            preferences[key] = MemoryRegisters.add(current, delta)
        }
    }

    override suspend fun subtract(value: String) {
        val delta = value.trim()
        if (delta.isEmpty()) return
        dataStore.edit { preferences ->
            val active = activeRegisterFrom(preferences)
            preferences[keyActiveRegister] = active
            val key = registerKey(active)
            val current = preferences[key]
            preferences[key] = MemoryRegisters.subtract(current, delta)
        }
    }

    override fun recall() {
        scope.launch {
            val preferences = dataStore.data.first()
            val active = activeRegisterFrom(preferences)
            val value = preferences[registerKey(active)]
            if (value != null) {
                _valueReadyEvents.emit(value)
            }
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            val active = activeRegisterFrom(preferences)
            preferences[keyActiveRegister] = active
            preferences.remove(registerKey(active))
        }
    }

    override suspend fun setActiveRegister(name: String) {
        val normalized = MemoryRegisters.normalizeName(name)
        dataStore.edit { preferences ->
            preferences[keyActiveRegister] = normalized
        }
    }

    private fun publishSnapshot(preferences: Preferences) {
        val active = activeRegisterFrom(preferences)
        val snapshot = linkedMapOf<String, String?>()
        snapshot[active] = null
        readStoredRegisters(preferences).forEach { (name, value) ->
            snapshot[name] = value
        }
        val sorted = snapshot.entries
            .sortedBy { it.key }
            .map { MemoryRegisterState(it.key, it.value) }
        _activeRegister.value = active
        _registers.value = sorted
    }

    private fun activeRegisterFrom(preferences: Preferences): String {
        val stored = preferences[keyActiveRegister] ?: MemoryRegisters.DEFAULT_REGISTER
        return MemoryRegisters.normalizeName(stored)
    }

    private fun readStoredRegisters(preferences: Preferences): Map<String, String?> {
        val map = linkedMapOf<String, String?>()
        preferences.asMap().forEach { (key, value) ->
            val keyName = key.name
            if (!keyName.startsWith(REGISTER_KEY_PREFIX)) return@forEach
            val encodedName = keyName.removePrefix(REGISTER_KEY_PREFIX)
            val decodedName = decodeRegisterName(encodedName) ?: return@forEach
            val normalizedName = MemoryRegisters.normalizeName(decodedName)
            val stringValue = value as? String ?: return@forEach
            map[normalizedName] = stringValue
        }
        return map
    }

    private fun registerKey(name: String): Preferences.Key<String> {
        val normalized = MemoryRegisters.normalizeName(name)
        return stringPreferencesKey(REGISTER_KEY_PREFIX + encodeRegisterName(normalized))
    }

    private fun encodeRegisterName(name: String): String {
        val bytes = name.encodeToByteArray()
        return buildString(bytes.size * 2) {
            for (byte in bytes) {
                val value = byte.toInt() and 0xff
                append(HEX[value ushr 4])
                append(HEX[value and 0x0f])
            }
        }
    }

    private fun decodeRegisterName(encoded: String): String? {
        if (encoded.isEmpty() || encoded.length % 2 != 0) return null
        val bytes = ByteArray(encoded.length / 2)
        var index = 0
        while (index < encoded.length) {
            val hi = decodeHexNibble(encoded[index]) ?: return null
            val lo = decodeHexNibble(encoded[index + 1]) ?: return null
            bytes[index / 2] = ((hi shl 4) or lo).toByte()
            index += 2
        }
        return bytes.decodeToString()
    }

    private fun decodeHexNibble(ch: Char): Int? = when (ch) {
        in '0'..'9' -> ch - '0'
        in 'a'..'f' -> 10 + (ch - 'a')
        in 'A'..'F' -> 10 + (ch - 'A')
        else -> null
    }

    private companion object {
        private const val REGISTER_KEY_PREFIX = "memory.register."
        private val HEX = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        )
    }
}
