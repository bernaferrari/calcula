package com.bernaferrari.calculator.data

import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.domain.model.AppPalette
import com.bernaferrari.calculator.domain.model.CalculationHistory
import com.bernaferrari.calculator.domain.model.DecimalSeparator
import com.bernaferrari.calculator.domain.model.ThemeMode
import com.bernaferrari.calculator.domain.model.UserPreferences
import com.bernaferrari.calculator.domain.repository.CalculatorRepository
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import com.bernaferrari.calculator.platform.KeyValueStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}

/** Settings as one JSON document; unknown or missing fields fall back to defaults. */
class StoredPreferencesRepository(private val store: KeyValueStore) : PreferencesRepository {
    private val state = MutableStateFlow(
        store.get(KEY)?.let { runCatching { json.decodeFromString<UserPreferences>(it) }.getOrNull() } ?: UserPreferences(),
    )

    override fun getUserPreferences(): Flow<UserPreferences> = state

    private fun edit(change: (UserPreferences) -> UserPreferences) {
        state.update(change)
        store.put(KEY, json.encodeToString(state.value))
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) = edit { it.copy(themeMode = themeMode) }
    override suspend fun updatePalette(palette: AppPalette) = edit { it.copy(palette = palette) }
    override suspend fun updateAngleUnit(angleUnit: AngleUnit) = edit { it.copy(angleUnit = angleUnit) }
    override suspend fun updateDecimalPlaces(decimalPlaces: Int) = edit { it.copy(decimalPlaces = decimalPlaces) }
    override suspend fun updateHapticFeedback(enabled: Boolean) = edit { it.copy(hapticFeedback = enabled) }
    override suspend fun updateShowHistory(enabled: Boolean) = edit { it.copy(showHistory = enabled) }
    override suspend fun updateDecimalSeparator(separator: DecimalSeparator) = edit { it.copy(decimalSeparator = separator) }
    override suspend fun updateShowFlickDots(enabled: Boolean) = edit { it.copy(showFlickDots = enabled) }
    override suspend fun updateSoundEnabled(enabled: Boolean) = edit { it.copy(soundEnabled = enabled) }
    override suspend fun updateReduceMotion(enabled: Boolean) = edit { it.copy(reduceMotion = enabled) }
    override suspend fun updateHighContrast(enabled: Boolean) = edit { it.copy(highContrast = enabled) }
    override suspend fun markSwipeHintSeen() = edit { it.copy(swipeHintSeen = true) }

    private companion object {
        const val KEY = "preferences"
    }
}

/** The tape, newest first, capped so it never grows without bound. */
class StoredHistoryRepository(private val store: KeyValueStore) : CalculatorRepository {
    private val entries = MutableStateFlow(
        store.get(KEY)?.let { runCatching { json.decodeFromString<List<CalculationHistory>>(it) }.getOrNull() }.orEmpty(),
    )

    private fun edit(change: (List<CalculationHistory>) -> List<CalculationHistory>) {
        entries.update { change(it).sortedByDescending(CalculationHistory::timestamp).take(LIMIT) }
        store.put(KEY, json.encodeToString(entries.value))
    }

    override fun getAllHistory(): Flow<List<CalculationHistory>> = entries

    override fun getRecentHistory(limit: Int): Flow<List<CalculationHistory>> = entries.map { it.take(limit) }

    override suspend fun insertCalculation(calculation: CalculationHistory) = edit { list ->
        val entry = if (calculation.id != 0L) calculation else calculation.copy(id = (list.maxOfOrNull { it.id } ?: 0L) + 1)
        list.filterNot { it.id == entry.id } + entry
    }

    override suspend fun deleteCalculation(calculation: CalculationHistory) = edit { list -> list.filterNot { it.id == calculation.id } }

    override suspend fun clearAllHistory() = edit { emptyList() }

    override suspend fun deleteOldHistory(cutoffTime: Long) = edit { list -> list.filter { it.timestamp >= cutoffTime } }

    override suspend fun getHistoryCount(): Int = entries.value.size

    private companion object {
        const val KEY = "history"
        const val LIMIT = 500
    }
}
