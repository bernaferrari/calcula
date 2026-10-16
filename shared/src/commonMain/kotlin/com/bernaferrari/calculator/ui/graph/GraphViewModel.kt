package com.bernaferrari.calculator.ui.graph

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.bernaferrari.calculator.domain.input.CalcState
import com.bernaferrari.calculator.domain.input.Calculator
import com.bernaferrari.calculator.domain.input.Editor
import com.bernaferrari.calculator.domain.input.ExpressionEditor
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.math.Plot
import com.bernaferrari.calculator.domain.model.AngleUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** One function being graphed: its text, where the cursor is, and its compiled form. */
data class GraphFunction(val editor: Editor) {
    val text: String get() = editor.text
    /** Null while the text is empty or not yet a complete function. */
    val plot: Plot? = if (editor.text.isBlank()) null else Plot.compile(editor.text, AngleUnit.RADIANS)
}

/** The visible window: its centre in graph units and how many units fit across. */
data class Viewport(val centerX: Double = 0.0, val centerY: Double = 0.0, val span: Double = DEFAULT_SPAN) {
    companion object {
        const val DEFAULT_SPAN = 4 * kotlin.math.PI
    }
}

data class GraphState(
    val functions: List<GraphFunction> = listOf(GraphFunction(Editor("sin(x)"))),
    val active: Int = 0,
    val viewport: Viewport = Viewport(),
) {
    val activeFunction: GraphFunction get() = functions[active.coerceIn(0, functions.lastIndex)]
    val canAdd: Boolean get() = functions.size < MAX_FUNCTIONS && functions.none { it.text.isBlank() }
}

const val MAX_FUNCTIONS = 3

/**
 * Up to three functions of x, typed with the calculator's own keys, over a window you pan and
 * pinch. Graphs work in radians, as graphs everywhere do.
 */
class GraphViewModel(private val saved: SavedStateHandle) : ViewModel() {
    private val _state = MutableStateFlow(restore())
    val state: StateFlow<GraphState> = _state

    fun onKey(key: Key) {
        when (key) {
            // On the graph, = means "this one's done, give me another".
            Key.EQUALS -> onAdd()
            Key.CLEAR -> editActive { Editor() }
            else -> editActive { editor ->
                Calculator.press(CalcState(editor = editor, angleUnit = AngleUnit.RADIANS), key).first.editor
            }
        }
    }

    fun onCursorMoved(position: Int) = editActive { ExpressionEditor.moveCursor(it, position) }

    fun onActivate(index: Int) = update { it.copy(active = index.coerceIn(0, it.functions.lastIndex)) }

    fun onAdd() = update {
        if (!it.canAdd) it else it.copy(functions = it.functions + GraphFunction(Editor()), active = it.functions.size)
    }

    fun onRemove(index: Int) = update {
        if (it.functions.size <= 1) {
            it.copy(functions = listOf(GraphFunction(Editor())), active = 0)
        } else {
            val functions = it.functions.filterIndexed { i, _ -> i != index }
            it.copy(functions = functions, active = it.active.coerceAtMost(functions.lastIndex).let { a -> if (index < it.active) a - 1 else a }.coerceAtLeast(0))
        }
    }

    /** Fills the active function, e.g. from a quick example. */
    fun onExample(text: String) = editActive { Editor(text) }

    fun onViewport(viewport: Viewport) = _state.update { it.copy(viewport = viewport) }

    fun onResetView() = onViewport(Viewport())

    private fun editActive(change: (Editor) -> Editor) = update { s ->
        s.copy(functions = s.functions.mapIndexed { i, f -> if (i == s.active) GraphFunction(change(f.editor)) else f })
    }

    private fun update(change: (GraphState) -> GraphState) {
        _state.update(change)
        val s = _state.value
        saved[KEY_FUNCTIONS] = s.functions.joinToString(SEPARATOR) { it.text }
        saved[KEY_ACTIVE] = s.active
    }

    private fun restore(): GraphState {
        val texts = saved.get<String>(KEY_FUNCTIONS)?.split(SEPARATOR) ?: return GraphState()
        val functions = texts.take(MAX_FUNCTIONS).map { GraphFunction(Editor(it)) }.ifEmpty { listOf(GraphFunction(Editor())) }
        return GraphState(functions = functions, active = (saved.get<Int>(KEY_ACTIVE) ?: 0).coerceIn(0, functions.lastIndex))
    }

    private companion object {
        const val KEY_FUNCTIONS = "functions"
        const val KEY_ACTIVE = "active"
        const val SEPARATOR = "\n"
    }
}
