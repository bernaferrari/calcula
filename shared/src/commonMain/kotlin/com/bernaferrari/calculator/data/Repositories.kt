package com.bernaferrari.calculator.data

import com.bernaferrari.calculator.domain.model.ColorStyle
import com.bernaferrari.calculator.domain.model.FlickHints
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    // A value this version no longer knows (a retired style, say) falls back to its default
    // instead of failing the whole document.
    coerceInputValues = true
    encodeDefaults = false
}

/** Saved settings, or the defaults if the document can't be read at all. */
internal fun decodePreferences(text: String): UserPreferences =
    runCatching { json.decodeFromString<UserPreferences>(text) }.getOrElse { UserPreferences() }

/** Settings as one JSON document; unknown or missing fields fall back to defaults. */
class StoredPreferencesRepository(private val store: KeyValueStore) : PreferencesRepository {
    private val state = MutableStateFlow(
        store.get(KEY)?.let(::decodePreferences) ?: UserPreferences(),
    )

    override fun getUserPreferences(): Flow<UserPreferences> = state

    private fun edit(change: (UserPreferences) -> UserPreferences) {
        state.update(change)
        store.put(KEY, json.encodeToString(state.value))
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) = edit { it.copy(themeMode = themeMode) }
    override suspend fun updatePalette(palette: AppPalette) = edit { it.copy(palette = palette) }
    override suspend fun updateColorStyle(style: ColorStyle) = edit { it.copy(colorStyle = style) }
    override suspend fun updateAngleUnit(angleUnit: AngleUnit) = edit { it.copy(angleUnit = angleUnit) }
    override suspend fun updateDecimalPlaces(decimalPlaces: Int) = edit { it.copy(decimalPlaces = decimalPlaces) }
    override suspend fun updateHapticFeedback(enabled: Boolean) = edit { it.copy(hapticFeedback = enabled) }
    override suspend fun updateShowHistory(enabled: Boolean) = edit { it.copy(showHistory = enabled) }
    override suspend fun updateDecimalSeparator(separator: DecimalSeparator) = edit { it.copy(decimalSeparator = separator) }
    override suspend fun updateFlickHints(hints: FlickHints) = edit { it.copy(flickHints = hints) }
    override suspend fun updateSoundEnabled(enabled: Boolean) = edit { it.copy(soundEnabled = enabled) }
    override suspend fun updateReduceMotion(enabled: Boolean) = edit { it.copy(reduceMotion = enabled) }
    override suspend fun markSwipeHintSeen() = edit { it.copy(swipeHintSeen = true) }
    override suspend fun updateKeypadScale(scale: Float) = edit { it.copy(keypadScale = scale) }

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

/** The graph as saved between launches. */
@Serializable
data class GraphDocument(
    val functions: List<String> = emptyList(),
    val active: Int = 0,
    val centerX: Double = 0.0,
    val centerY: Double = 0.0,
    val span: Double = 0.0,
    val parameters: Map<String, StoredParameter> = emptyMap(),
)

@Serializable
data class StoredParameter(val value: Double = 1.0, val min: Double = -10.0, val max: Double = 10.0)

/** A graph kept in the recent list: what it was, when it was last looked at, and whether it's pinned. */
@Serializable
data class SavedGraph(
    val id: Long,
    val document: GraphDocument,
    val savedAt: Long,
    val pinned: Boolean = false,
)

interface GraphStore {
    fun load(): GraphDocument?
    fun save(document: GraphDocument)
    fun loadRecent(): List<SavedGraph> = emptyList()
    fun saveRecent(graphs: List<SavedGraph>) {}
}

/** The graph's functions, view and sliders, kept beside the settings. */
class StoredGraphStore(private val store: KeyValueStore) : GraphStore {
    override fun load(): GraphDocument? = store.get(KEY)?.let { runCatching { json.decodeFromString<GraphDocument>(it) }.getOrNull() }
    override fun save(document: GraphDocument) = store.put(KEY, json.encodeToString(document))
    override fun loadRecent(): List<SavedGraph> =
        store.get(RECENT)?.let { runCatching { json.decodeFromString<List<SavedGraph>>(it) }.getOrNull() }.orEmpty()
    override fun saveRecent(graphs: List<SavedGraph>) = store.put(RECENT, json.encodeToString(graphs))

    private companion object {
        const val KEY = "graph"
        const val RECENT = "graph_recent"
    }
}
