package com.bernaferrari.calculator.data

import com.bernaferrari.calculator.domain.math.UserFunctions
import com.bernaferrari.calculator.domain.input.Editor
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
import com.bernaferrari.calculator.platform.StringStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.builtins.serializer
import com.bernaferrari.calculator.domain.math.UserFunction
import com.bernaferrari.calculator.domain.model.Memory
import com.bernaferrari.calculator.domain.model.Usage
import com.bernaferrari.calculator.domain.programmer.ProgEntry

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}

private val lenientJson = Json(json) { coerceInputValues = true }

private fun <T> Json.decodeOrNull(serializer: KSerializer<T>, element: JsonElement): T? =
    try {
        decodeFromJsonElement(serializer, element)
    } catch (_: IllegalArgumentException) {
        null
    }

private fun <T> recoverList(serializer: KSerializer<T>, element: JsonElement): JsonArray? =
    (element as? JsonArray)?.let { rows -> JsonArray(rows.filter { lenientJson.decodeOrNull(serializer, it) != null }) }

private fun <T> recoverFields(
    serializer: KSerializer<T>,
    source: JsonObject,
    recoverField: (String, JsonElement) -> JsonElement? = { _, value -> value },
): JsonObject = buildJsonObject {
    for ((name, raw) in source) {
        val value = recoverField(name, raw) ?: continue
        if (lenientJson.decodeOrNull(serializer, JsonObject(mapOf(name to value))) != null) put(name, value)
    }
}

/** Keep each unreadable original before replacing the working copy with recovered data. */
private fun StringStore.preserveUnreadable(key: String, raw: String) {
    val base = "${key}_recovery"
    var backupKey = base
    var index = 2
    while (true) {
        val previous = get(backupKey)
        if (previous == raw) return
        if (previous == null) {
            put(backupKey, raw)
            return
        }
        backupKey = "${base}_${index++}"
    }
}

/** Settings as one JSON document; a damaged field falls back independently. */
class StoredPreferencesRepository(private val store: StringStore) : PreferencesRepository {
    private val original = store.get(KEY)
    private val source = original?.let { runCatching { json.parseToJsonElement(it) }.getOrNull() }
    private val decoded = source?.let { json.decodeOrNull(UserPreferences.serializer(), it) }
    private val state = MutableStateFlow(
        (decoded ?: recoverPreferences(source as? JsonObject))
            .let { if (it.palette == AppPalette.GRAPHITE) it.copy(palette = AppPalette.INDIGO, colorStyle = ColorStyle.NEUTRAL) else it },
    ).also { UserFunctions.all = it.value.functions }
    private val writes = Mutex()

    override fun getUserPreferences(): Flow<UserPreferences> = state

    private fun recoverPreferences(source: JsonObject?): UserPreferences {
        if (source == null) return UserPreferences()
        val recovered = recoverFields(UserPreferences.serializer(), source) { name, value ->
            when (name) {
                "memories" -> recoverList(Memory.serializer(), value)
                "functions" -> recoverList(UserFunction.serializer(), value)
                "programmerTape" -> recoverList(ProgEntry.serializer(), value)
                "statsData" -> recoverList(String.serializer(), value)
                "usage" -> (value as? JsonObject)?.let { recoverFields(Usage.serializer(), it) }
                else -> value
            }
        }
        return lenientJson.decodeFromJsonElement(recovered)
    }

    private suspend fun edit(change: (UserPreferences) -> UserPreferences) = writes.withLock {
        val next = change(state.value)
        if (original != null && decoded == null) store.preserveUnreadable(KEY, original)
        store.put(KEY, json.encodeToString(next))
        // The person's functions are part of the language everywhere, so the engine hears at once.
        UserFunctions.all = next.functions
        state.value = next
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) = edit { it.copy(themeMode = themeMode) }
    override suspend fun updatePalette(palette: AppPalette) = edit { it.copy(palette = palette) }
    override suspend fun updateColorStyle(style: ColorStyle) = edit { it.copy(colorStyle = style) }
    override suspend fun updateAngleUnit(angleUnit: AngleUnit) = edit { it.copy(angleUnit = angleUnit) }
    override suspend fun updateDecimalPlaces(decimalPlaces: Int) = edit { it.copy(decimalPlaces = decimalPlaces) }
    override suspend fun updateHapticFeedback(enabled: Boolean) = edit { it.copy(hapticFeedback = enabled) }
    override suspend fun updateShowHistory(enabled: Boolean) = edit { it.copy(showHistory = enabled) }
    override suspend fun updateDecimalSeparator(separator: DecimalSeparator) = edit { it.copy(decimalSeparator = separator) }
    override suspend fun updateFlickHints(hints: FlickHints?) = edit { it.copy(flickHints = hints) }
    override suspend fun updateSoundEnabled(enabled: Boolean) = edit { it.copy(soundEnabled = enabled) }
    override suspend fun updateReduceMotion(enabled: Boolean) = edit { it.copy(reduceMotion = enabled) }
    override suspend fun markSwipeHintSeen() = edit { it.copy(swipeHintSeen = true) }
    override suspend fun updateKeypadScale(scale: Float) = edit { it.copy(keypadScale = scale) }
    override suspend fun updateGraphKeypadScale(scale: Float) = edit { it.copy(graphKeypadScale = scale) }
    override suspend fun update(change: (UserPreferences) -> UserPreferences) = edit(change)

    private companion object {
        const val KEY = "preferences"
    }
}

/** The tape, newest first, capped so it never grows without bound. */
class StoredHistoryRepository(private val store: StringStore) : CalculatorRepository {
    private val original = store.get(KEY)
    private val source = original?.let { runCatching { json.parseToJsonElement(it) as? JsonArray }.getOrNull() }
    private val recovered = source.orEmpty().mapNotNull { json.decodeOrNull(CalculationHistory.serializer(), it) }
    private val damaged = original != null && (source == null || recovered.size != source.size)
    private val entries = MutableStateFlow(recovered)

    private val writes = Mutex()
    private var lastId = maxOf(
        store.get(ID_KEY)?.toLongOrNull() ?: 0L,
        source.orEmpty().maxOfOrNull { ((it as? JsonObject)?.get("id") as? JsonPrimitive)?.longOrNull ?: 0L } ?: 0L,
    )

    private suspend fun edit(change: (List<CalculationHistory>) -> List<CalculationHistory>) = writes.withLock {
        var replayBudget = REPLAY_BUDGET
        val next = change(entries.value).sortedByDescending(CalculationHistory::timestamp).take(LIMIT).map { entry ->
            val size = entry.answerOperand.orEmpty().length + entry.resultOperand.orEmpty().length
            if (size <= replayBudget) {
                replayBudget -= size
                entry
            } else entry.copy(answerOperand = Editor.withoutTree(entry.answerOperand), resultOperand = null)
        }
        if (damaged && original != null) store.preserveUnreadable(KEY, original)
        // Reserve IDs before saving entries. A failed write may skip an ID, but cannot reuse one.
        store.put(ID_KEY, lastId.toString())
        store.put(KEY, json.encodeToString(next))
        entries.value = next
    }

    override fun getAllHistory(): Flow<List<CalculationHistory>> = entries

    override fun getRecentHistory(limit: Int): Flow<List<CalculationHistory>> = entries.map { it.take(limit) }

    override suspend fun insertCalculation(calculation: CalculationHistory) = edit { list ->
        val entry = if (calculation.id != 0L) {
            lastId = maxOf(lastId, calculation.id)
            calculation
        } else {
            check(lastId < Long.MAX_VALUE) { "History ID space exhausted" }
            calculation.copy(id = ++lastId)
        }
        list.filterNot { it.id == entry.id } + entry
    }

    override suspend fun deleteCalculation(calculation: CalculationHistory) = edit { list -> list.filterNot { it.id == calculation.id } }

    override suspend fun clearAllHistory() = edit { emptyList() }

    override suspend fun deleteOldHistory(cutoffTime: Long) = edit { list -> list.filter { it.timestamp >= cutoffTime } }

    override suspend fun getHistoryCount(): Int = entries.value.size

    private companion object {
        const val KEY = "history"
        const val ID_KEY = "history_last_id"
        const val LIMIT = 500
        const val REPLAY_BUDGET = 512_000
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
class StoredGraphStore(private val store: StringStore) : GraphStore {
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
