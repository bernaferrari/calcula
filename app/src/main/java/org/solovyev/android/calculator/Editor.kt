package org.solovyev.android.calculator

import android.app.Application
import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.solovyev.android.Check
import org.solovyev.android.calculator.history.HistoryState
import org.solovyev.android.calculator.history.RecentHistory
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.memory.Memory
import org.solovyev.android.calculator.text.TextProcessorEditorResult
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.view.EditorTextProcessor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class Editor @Inject constructor(
    application: Application,
    appPreferences: AppPreferences,
    private val engine: Engine,
    private val memory: Memory
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @VisibleForTesting
    var textProcessor: EditorTextProcessor? = EditorTextProcessor(application, appPreferences, engine)

    private var highlighterJob: Job? = null

    private val _stateFlow = MutableStateFlow(EditorState.empty())
    val stateFlow: StateFlow<EditorState> = _stateFlow.asStateFlow()

    private val _changedEvents = MutableSharedFlow<ChangedEvent>()
    val changedEvents: SharedFlow<ChangedEvent> = _changedEvents.asSharedFlow()

    private val _cursorMovedEvents = MutableSharedFlow<CursorMovedEvent>()
    val cursorMovedEvents: SharedFlow<CursorMovedEvent> = _cursorMovedEvents.asSharedFlow()

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
        Check.isMainThread()
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

        val processor = textProcessor
        val text = newState.getTextString()

        if (TextUtils.isEmpty(text) || processor == null) {
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
        Check.isMainThread()
        _stateFlow.value = newState
        scope.launch {
            _cursorMovedEvents.emit(CursorMovedEvent(newState))
        }
        return state
    }

    fun setState(state: EditorState) {
        Check.isMainThread()
        onTextChanged(state)
    }

    private fun newSelectionViewState(newSelection: Int): EditorState {
        Check.isMainThread()
        if (state.selection == newSelection) {
            return state
        }
        return onSelectionChanged(EditorState.forNewSelection(state, newSelection))
    }

    fun setCursorOnStart(): EditorState {
        Check.isMainThread()
        return newSelectionViewState(0)
    }

    fun setCursorOnEnd(): EditorState {
        Check.isMainThread()
        return newSelectionViewState(state.text.length)
    }

    fun moveCursorLeft(): EditorState {
        Check.isMainThread()
        if (state.selection <= 0) {
            return state
        }
        return newSelectionViewState(state.selection - 1)
    }

    fun moveCursorRight(): EditorState {
        Check.isMainThread()
        if (state.selection >= state.text.length) {
            return state
        }
        return newSelectionViewState(state.selection + 1)
    }

    fun erase(): Boolean {
        Check.isMainThread()
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
        onTextChanged(EditorState.create(newText, removeStart))
        return newText.isNotEmpty()
    }

    fun clear() {
        Check.isMainThread()
        setText("")
    }

    fun setText(text: String) {
        Check.isMainThread()
        onTextChanged(EditorState.create(text, text.length))
    }

    fun setText(text: String, selection: Int) {
        Check.isMainThread()
        onTextChanged(EditorState.create(text, clamp(selection, text)))
    }

    fun insert(text: String, selectionOffset: Int = 0) {
        Check.isMainThread()
        if (TextUtils.isEmpty(text) && selectionOffset == 0) {
            return
        }
        val oldText = state.getTextString()
        val selection = clamp(state.selection, oldText)
        val newTextLength = text.length + oldText.length
        val newSelection = clamp(text.length + selection + selectionOffset, newTextLength)
        val newText = oldText.substring(0, selection) + text + oldText.substring(selection)
        onTextChanged(EditorState.create(newText, newSelection))
    }

    fun moveSelection(offset: Int): EditorState {
        Check.isMainThread()
        return setSelection(state.selection + offset)
    }

    fun setSelection(selection: Int): EditorState {
        Check.isMainThread()
        if (state.selection == selection) {
            return state
        }
        return onSelectionChanged(EditorState.forNewSelection(state, clamp(selection, state.text)))
    }

    fun onHistoryLoaded(history: RecentHistory) {
        if (!state.isEmpty()) {
            return
        }
        val historyState: HistoryState? = history.getCurrent()
        historyState?.let {
            setState(it.editor)
        }
    }

    data class ChangedEvent(
        val oldState: EditorState,
        val newState: EditorState,
        val force: Boolean
    ) {
        fun shouldEvaluate(): Boolean {
            return force || !TextUtils.equals(newState.text, oldState.text)
        }
    }

    data class CursorMovedEvent(
        val state: EditorState
    )

    companion object {
        @JvmStatic
        fun clamp(selection: Int, text: CharSequence): Int {
            return clamp(selection, text.length)
        }

        @JvmStatic
        fun clamp(selection: Int, max: Int): Int {
            return min(max(selection, 0), max)
        }
    }
}
