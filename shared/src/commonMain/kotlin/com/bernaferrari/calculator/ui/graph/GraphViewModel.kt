package com.bernaferrari.calculator.ui.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.calculator.data.GraphDocument
import com.bernaferrari.calculator.data.GraphStore
import com.bernaferrari.calculator.data.SavedGraph
import kotlin.time.Clock
import com.bernaferrari.calculator.data.StoredParameter
import com.bernaferrari.calculator.domain.input.CalcState
import com.bernaferrari.calculator.domain.input.Calculator
import com.bernaferrari.calculator.domain.input.Editor
import com.bernaferrari.calculator.domain.input.ExpressionEditor
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.math.Curve
import com.bernaferrari.calculator.domain.model.AngleUnit
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

/** One function being graphed: its text and where the cursor is. */
data class GraphFunction(val editor: Editor) {
    val text: String get() = editor.text
    /** Typed as an equation, so the row shows it whole instead of after `y =`. */
    val isEquation: Boolean get() = '=' in text || 'y' in text
}

/** The visible window: its centre in graph units and how many units fit across. */
data class Viewport(val centerX: Double = 0.0, val centerY: Double = 0.0, val span: Double = DEFAULT_SPAN) {
    companion object {
        const val DEFAULT_SPAN = 4 * kotlin.math.PI
    }
}

/** A slider: its value and the range it moves over. */
data class Parameter(val value: Double = 1.0, val min: Double = -10.0, val max: Double = 10.0)

data class GraphState(
    val functions: List<GraphFunction> = listOf(GraphFunction(Editor("sin(x)"))),
    val active: Int = 0,
    val viewport: Viewport = Viewport(),
    /** Every slider ever made, so a letter deleted and typed again comes back where it was. */
    val parameters: Map<Char, Parameter> = emptyMap(),
) {
    val activeFunction: GraphFunction get() = functions[active.coerceIn(0, functions.lastIndex)]
    val canAdd: Boolean get() = functions.size < MAX_FUNCTIONS && functions.none { it.text.isBlank() }

    /** The parameters the functions use right now, each with a slider. */
    val used: List<Char> = functions.flatMap { Curve.parameters(it.text) }.distinct().sorted()

    /** The letter the keypad hands out next, or null once all four are in use. */
    val nextParameter: Char? = PARAMETERS.firstOrNull { it !in used }

    fun parameter(name: Char): Parameter = parameters[name] ?: Parameter()

    /** Each function compiled with the sliders' current values; null while blank or incomplete. */
    val curves: List<Curve?> = run {
        val values = used.associateWith { parameter(it).value }
        functions.map { if (it.text.isBlank()) null else Curve.compile(it.text, AngleUnit.RADIANS, values) }
    }

    /** Mid-equation (`x² + y²` with no `=` yet, or nothing after it): unfinished rather than wrong. */
    fun pending(index: Int): Boolean {
        val text = functions[index].text
        return curves[index] == null && (('y' in text && '=' !in text) || text.split('=').any { it.isBlank() })
    }
}

/** How many unpinned graphs recent keeps; pinned ones don't count. */
const val RECENT_LIMIT = 30

/** One per curve color; past six the plot turns to spaghetti. */
const val MAX_FUNCTIONS = 6
const val PARAMETERS = "abcd"

/**
 * Up to three functions of x or equations in x and y, typed with the calculator's own keys,
 * over a window you pan and pinch, with a slider for every parameter. Graphs work in radians,
 * as graphs everywhere do. Everything is kept between launches.
 */
@OptIn(FlowPreview::class)
class GraphViewModel(private val store: GraphStore) : ViewModel() {
    private val _state = MutableStateFlow(store.load()?.toState() ?: GraphState())
    val state: StateFlow<GraphState> = _state

    private val _recent = MutableStateFlow(store.loadRecent())

    /** Every graph made here, newest first, the current one included once you move on from it. */
    val recent: StateFlow<List<SavedGraph>> = _recent

    /** The recent graphs worth showing: everything but the one on screen now. */
    fun visibleRecent(): List<SavedGraph> = visibleRecent(_recent.value, _state.value)

    init {
        // Pans and slider drags change the state every frame; the disk only needs where they stop.
        _state.drop(1).debounce(400).onEach { store.save(it.toDocument()) }.launchIn(viewModelScope)
    }

    public override fun onCleared() {
        store.save(_state.value.toDocument())
        keep(_state.value)
    }

    /** Puts the current graph in recent and starts a blank one. */
    fun onNewGraph() {
        keep(_state.value)
        _state.value = GraphState(functions = listOf(GraphFunction(Editor())))
    }

    /** Brings a recent graph back; the one on screen goes to recent first, so nothing is lost. */
    fun onRestore(id: Long) {
        val saved = _recent.value.firstOrNull { it.id == id } ?: return
        keep(_state.value)
        _state.value = saved.document.toState()
        keep(_state.value)
    }

    fun onTogglePin(id: Long) = editRecent { list -> list.map { if (it.id == id) it.copy(pinned = !it.pinned) else it } }

    fun onDelete(id: Long) = editRecent { list -> list.filterNot { it.id == id } }

    /**
     * Files [graph] in recent: the same functions update their entry (keeping its pin) instead of
     * adding another. Empty graphs aren't kept.
     */
    private fun keep(graph: GraphState) {
        val key = graph.key()
        if (key.isEmpty()) return
        val now = Clock.System.now().toEpochMilliseconds()
        editRecent { list ->
            val existing = list.firstOrNull { it.key() == key }
            val entry = existing?.copy(document = graph.toDocument(), savedAt = now)
                ?: SavedGraph(id = (list.maxOfOrNull { it.id } ?: 0L) + 1, document = graph.toDocument(), savedAt = now)
            listOf(entry) + list.filterNot { it.id == entry.id }
        }
    }

    private fun editRecent(change: (List<SavedGraph>) -> List<SavedGraph>) {
        // Newest first; pinned graphs never roll off, the rest keep the latest [RECENT_LIMIT].
        val changed = change(_recent.value).sortedByDescending { it.savedAt }
        val unpinned = changed.filterNot { it.pinned }.take(RECENT_LIMIT).map { it.id }.toSet()
        _recent.value = changed.filter { it.pinned || it.id in unpinned }
        store.saveRecent(_recent.value)
    }

    fun onKey(key: Key) {
        when (key) {
            // On the graph `=` is part of the language: y = …, x² + y² = 9.
            Key.EQUALS -> onKey(Key.EQUATION)
            Key.CLEAR -> editActive { Editor() }
            else -> editActive { editor ->
                Calculator.press(CalcState(editor = editor, angleUnit = AngleUnit.RADIANS), key).first.editor
            }
        }
    }

    /** Drops a function call or a constant from the library at the cursor. */
    fun onInsertText(text: String) = editActive { editor ->
        Calculator.insertText(CalcState(editor = editor, angleUnit = AngleUnit.RADIANS), text).editor
    }

    /** Types a parameter's letter where the cursor is, e.g. from its slider. */
    fun onInsertParameter(name: Char) = editActive { ExpressionEditor.insertRaw(it, name.toString()) }

    fun onCursorMoved(position: Int) = editActive { ExpressionEditor.moveCursor(it, position) }

    fun onActivate(index: Int) = _state.update { it.copy(active = index.coerceIn(0, it.functions.lastIndex)) }

    fun onAdd() = _state.update {
        if (!it.canAdd) it else it.copy(functions = it.functions + GraphFunction(Editor()), active = it.functions.size)
    }

    fun onRemove(index: Int) {
        // Removing the last function ends this graph: keep it in recent first.
        if (_state.value.functions.size <= 1) keep(_state.value)
        removeAt(index)
    }

    private fun removeAt(index: Int) = _state.update {
        if (it.functions.size <= 1) {
            it.copy(functions = listOf(GraphFunction(Editor())), active = 0)
        } else {
            val functions = it.functions.filterIndexed { i, _ -> i != index }
            it.copy(functions = functions, active = (if (index < it.active) it.active - 1 else it.active).coerceIn(0, functions.lastIndex))
        }
    }

    /** Fills the active function, e.g. from a quick example. */
    fun onExample(text: String) = editActive { Editor(text) }

    fun onViewport(viewport: Viewport) = _state.update { it.copy(viewport = viewport) }

    fun onResetView() = onViewport(Viewport())

    /**
     * Moves a slider. A finger's values snap to a step that suits the range, so they read cleanly;
     * playback glides between steps.
     */
    fun onParameter(name: Char, value: Double, snapped: Boolean = true) = _state.update { s ->
        val p = s.parameter(name)
        s.copy(parameters = s.parameters + (name to p.copy(value = if (snapped) snap(value, p) else value.coerceIn(p.min, p.max))))
    }

    /** Changes a slider's range; the value stays inside it. */
    fun onRange(name: Char, min: Double, max: Double) = _state.update { s ->
        if (!(min < max) || !min.isFinite() || !max.isFinite()) return@update s
        val p = Parameter(s.parameter(name).value, min, max)
        s.copy(parameters = s.parameters + (name to p.copy(value = snap(p.value, p))))
    }

    private fun editActive(change: (Editor) -> Editor) = _state.update { s ->
        s.copy(functions = s.functions.mapIndexed { i, f -> if (i == s.active) GraphFunction(change(f.editor)) else f })
    }

    companion object {
        /** About a hundred steps across the range, rounded to 1, 2 or 5 times a power of ten. */
        fun stepFor(p: Parameter): Double {
            val rough = max(p.max - p.min, 1e-9) / 100
            val magnitude = 10.0.pow(floor(log10(rough)))
            val n = rough / magnitude
            return magnitude * when {
                n < 1.5 -> 1.0
                n < 3.5 -> 2.0
                n < 7.5 -> 5.0
                else -> 10.0
            }
        }

        fun snap(value: Double, p: Parameter): Double {
            val step = stepFor(p)
            val snapped = (round(value / step) * step).coerceIn(p.min, p.max)
            return if (abs(snapped) < step / 2) 0.0 else snapped
        }
    }
}

/** [recent] without the graph on screen, which would only repeat what's already there. */
fun visibleRecent(recent: List<SavedGraph>, current: GraphState): List<SavedGraph> = recent.filter { it.key() != current.key() }

/** What makes two graphs the same graph: their non-blank functions, in order. */
private fun GraphState.key(): List<String> = functions.map { it.text }.filter { it.isNotBlank() }

private fun SavedGraph.key(): List<String> = document.functions.filter { it.isNotBlank() }

private fun GraphState.toDocument() = GraphDocument(
    functions = functions.map { it.text },
    active = active,
    centerX = viewport.centerX,
    centerY = viewport.centerY,
    span = viewport.span,
    parameters = parameters.map { (k, v) -> k.toString() to StoredParameter(v.value, v.min, v.max) }.toMap(),
)

internal fun GraphDocument.toState(): GraphState {
    val functions = functions.take(MAX_FUNCTIONS).map { GraphFunction(Editor(it)) }.ifEmpty { listOf(GraphFunction(Editor())) }
    return GraphState(
        functions = functions,
        active = active.coerceIn(0, functions.lastIndex),
        viewport = if (span > 0 && span.isFinite()) Viewport(centerX, centerY, span) else Viewport(),
        parameters = parameters.mapNotNull { (k, v) ->
            k.singleOrNull()?.takeIf { it in PARAMETERS }?.let { it to Parameter(v.value, v.min, v.max) }
        }.toMap(),
    )
}
