package org.solovyev.android.calculator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.solovyev.android.calculator.history.HistoryState
import org.solovyev.android.calculator.history.RecentHistory
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.memory.Memory
import org.solovyev.android.calculator.text.TextProcessorEditorResult
// import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.view.Highlighter
import kotlin.jvm.JvmName
import kotlin.math.max
import kotlin.math.min

class Editor(
    appPreferences: AppPreferences,
    private val engine: Engine,
    private val memory: Memory
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var highlighter: Highlighter? = null

    private var highlighterJob: Job? = null

    private val _stateFlow = MutableStateFlow(EditorState.empty())
    val stateFlow: StateFlow<EditorState> = _stateFlow.asStateFlow()

    private val _changedEvents = MutableSharedFlow<ChangedEvent>()
    val changedEvents: SharedFlow<ChangedEvent> = _changedEvents.asSharedFlow()

    private val _cursorMovedEvents = MutableSharedFlow<CursorMovedEvent>()
    val cursorMovedEvents: SharedFlow<CursorMovedEvent> = _cursorMovedEvents.asSharedFlow()

    private val undoManager = EditorUndoManager()

    @get:JvmName("stateProperty")
    val state: EditorState
        get() = _stateFlow.value

    fun init() {
        scope.launch {
            engine.changedEvents.collect {
                onTextChanged(state, true)
            }
        }
        scope.launch {
            memory.valueReadyEvents.collect { value ->
                insert(value)
            }
        }
    }

    fun getState(): EditorState = state

    private fun onTextChanged(newState: EditorState, force: Boolean = false) {
        asyncHighlightText(newState, force)
    }

    private suspend fun cancelAsyncHighlightText() {
        highlighterJob?.let { job ->
            job.cancelAndJoin()
            highlighterJob = null
        }
    }

    private fun asyncHighlightText(newState: EditorState, force: Boolean) {
        // synchronous operation should continue working regardless of the highlighter
        val oldState = state
        _stateFlow.value = newState

        val processor = highlighter
        val text = newState.getTextString()

        if (text.isEmpty() || processor == null) {
            scope.launch {
                _changedEvents.emit(ChangedEvent(oldState, newState, force))
            }
            return
        }

        scope.launch {
            cancelAsyncHighlightText()

            highlighterJob = launch {
                val processedState = withContext(Dispatchers.Default) {
                    val res: TextProcessorEditorResult = processor.process(newState.getTextString())
                    EditorState.create(res.getCharSequence(), newState.selection + res.offset)
                }

                if (highlighterJob?.isActive == true) {
                    _stateFlow.value = processedState
                    _changedEvents.emit(ChangedEvent(oldState, processedState, force))
                    highlighterJob = null
                }
            }
        }
    }

    private fun onSelectionChanged(newState: EditorState): EditorState {
        _stateFlow.value = newState
        scope.launch {
            _cursorMovedEvents.emit(CursorMovedEvent(newState))
        }
        return state
    }

    fun setState(state: EditorState) {
        undoManager.clear()
        onTextChanged(EditorState.create(state.getTextString(), state.selection))
    }

    private fun newSelectionViewState(newSelection: Int): EditorState {
        if (state.selection == newSelection) {
            return state
        }
        return onSelectionChanged(EditorState.forNewSelection(state, newSelection))
    }

    fun setCursorOnStart(): EditorState {
        return newSelectionViewState(0)
    }

    fun setCursorOnEnd(): EditorState {
        return newSelectionViewState(state.text.length)
    }

    fun moveCursorLeft(): EditorState {
        if (state.selection <= 0) {
            return state
        }
        return newSelectionViewState(state.selection - 1)
    }

    fun moveCursorRight(): EditorState {
        if (state.selection >= state.text.length) {
            return state
        }
        return newSelectionViewState(state.selection + 1)
    }

    fun erase(): Boolean {
        val selection = state.selection
        val text = state.getTextString()
        if (selection <= 0 || text.isEmpty() || selection > text.length) {
            return false
        }
        var removeStart = selection - 1
        if (MathType.getType(text, selection - 1, false, engine).type == MathType.grouping_separator) {
            // we shouldn't remove just separator as it will be re-added after the evaluation is done. Remove the digit
            // before
            removeStart -= 1
        }

        val newText = text.substring(0, removeStart) + text.substring(selection, text.length)
        undoManager.recordBeforeChange(state)
        onTextChanged(EditorState.create(newText, removeStart))
        return newText.isNotEmpty()
    }

    fun clear() {
        setText("")
    }

    fun setText(text: String) {
        if (state.getTextString() == text && state.selection == text.length) return
        undoManager.recordBeforeChange(state)
        onTextChanged(EditorState.create(text, text.length))
    }

    fun setText(text: String, selection: Int) {
        val boundedSelection = clamp(selection, text)
        if (state.getTextString() == text && state.selection == boundedSelection) return
        undoManager.recordBeforeChange(state)
        onTextChanged(EditorState.create(text, boundedSelection))
    }

    fun insert(text: String, selectionOffset: Int = 0) {
        if (text.isEmpty() && selectionOffset == 0) {
            return
        }
        val oldText = state.getTextString()
        val selection = clamp(state.selection, oldText)
        val newTextLength = text.length + oldText.length
        val newSelection = clamp(text.length + selection + selectionOffset, newTextLength)
        val newText = oldText.substring(0, selection) + text + oldText.substring(selection)
        if (oldText == newText && state.selection == newSelection) return
        undoManager.recordBeforeChange(state)
        onTextChanged(EditorState.create(newText, newSelection))
    }

    fun moveSelection(offset: Int): EditorState {
        return setSelection(state.selection + offset)
    }

    fun setSelection(selection: Int): EditorState {
        if (state.selection == selection) {
            return state
        }
        return onSelectionChanged(EditorState.forNewSelection(state, clamp(selection, state.text)))
    }

    fun undo(): Boolean {
        val target = undoManager.undo(state) ?: return false
        onTextChanged(EditorState.create(target.getTextString(), target.selection))
        return true
    }

    fun redo(): Boolean {
        val target = undoManager.redo(state) ?: return false
        onTextChanged(EditorState.create(target.getTextString(), target.selection))
        return true
    }

    fun canUndo(): Boolean = undoManager.canUndo()

    fun canRedo(): Boolean = undoManager.canRedo()

    fun onHistoryLoaded(history: RecentHistory) {
        if (!state.isEmpty()) {
            return
        }
        scope.launch {
            val historyState: HistoryState? = history.getCurrent()
            historyState?.let {
                setState(it.editor)
            }
        }
    }

    data class ChangedEvent(
        val oldState: EditorState,
        val newState: EditorState,
        val force: Boolean
    ) {
        fun shouldEvaluate(): Boolean {
            return force || newState.text != oldState.text
        }
    }

    data class CursorMovedEvent(
        val state: EditorState
    )

    companion object {
        
        fun clamp(selection: Int, text: CharSequence): Int {
            return clamp(selection, text.length)
        }

        
        fun clamp(selection: Int, max: Int): Int {
            return min(max(selection, 0), max)
        }
    }
}
