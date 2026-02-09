package org.solovyev.android.calculator.memory

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Memory implementation using DataStore for persistence.
 * Stores a single memory value that persists across app restarts.
 */
class DataStoreMemory(private val dataStore: DataStore<Preferences>) : Memory {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val keyMemoryValue = stringPreferencesKey("memory.value")
    
    private val _valueReadyEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val valueReadyEvents: SharedFlow<String> = _valueReadyEvents.asSharedFlow()

    /**
     * Stores a value in memory.
     */
    override suspend fun store(value: String) {
        dataStore.edit { preferences ->
            preferences[keyMemoryValue] = value
        }
    }

    /**
     * Adds a value to the current memory value.
     */
    override suspend fun add(value: String) {
        val current = get() ?: "0"
        try {
            val result = current.toDouble() + value.toDouble()
            store(result.toString())
        } catch (e: NumberFormatException) {
            // If parsing fails, just store the new value
            store(value)
        }
    }

    /**
     * Subtracts a value from the current memory value.
     */
    override suspend fun subtract(value: String) {
        val current = get() ?: "0"
        try {
            val result = current.toDouble() - value.toDouble()
            store(result.toString())
        } catch (e: NumberFormatException) {
            // If parsing fails, just store the new value
            store(value)
        }
    }

    /**
     * Recalls the stored memory value and emits it via [valueReadyEvents].
     */
    override fun recall() {
        scope.launch {
            val value = get()
            if (value != null) {
                _valueReadyEvents.emit(value)
            }
        }
    }

    /**
     * Clears the memory.
     */
    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(keyMemoryValue)
        }
    }

    /**
     * Gets the current memory value.
     */
    suspend fun get(): String? {
        return dataStore.data.map { preferences ->
            preferences[keyMemoryValue]
        }.first()
    }

    /**
     * Checks if memory has a stored value.
     */
    suspend fun hasValue(): Boolean {
        return get() != null
    }
}
