package org.solovyev.android.calculator.core

import android.app.Application
import android.content.SharedPreferences
import android.text.TextUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.solovyev.android.Check
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.EditorView
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.history.HistoryState
import org.solovyev.android.calculator.history.RecentHistory
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.memory.Memory
import org.solovyev.android.calculator.text.TextProcessorEditorResult
import org.solovyev.android.calculator.view.EditorTextProcessor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class Editor @Inject constructor(
    application: Application,
    preferences: SharedPreferences,
    private val engine: Engine
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var textProcessor: EditorTextProcessor? = EditorTextProcessor(application, preferences, engine)
        internal set

    private var view: EditorView? = null

    private val _state = MutableStateFlow(EditorState.empty())
    val stateFlow: StateFlow<EditorState> = _state.asStateFlow()

    val state: EditorState get() = _state.value

    private val _changedEvents = MutableSharedFlow<ChangedEvent>()
    val changedEvents: SharedFlow<ChangedEvent> = _changedEvents.asSharedFlow()

    private val _cursorMovedEvents = MutableSharedFlow<CursorMovedEvent>()
    val cursorMovedEvents: SharedFlow<CursorMovedEvent> = _cursorMovedEvents.asSharedFlow()

    fun init() {
        // Subscribe to engine changes
        scope.launch {
            engine.changedEvents.collect {
                onTextChanged(state, force = true)
            }
        }
    }

    fun setView(view: EditorView) {
        Check.isMainThread()
        this.view = view
        view.setState(state)
        view.setEditor(this)
    }

    fun clearView(view: EditorView) {
        Check.isMainThread()
        if (this.view == view) {
            this.view?.setEditor(null as org.solovyev.android.calculator.core.Editor?)
            this.view = null
        }
    }

    private fun onTextChanged(newState: EditorState, force: Boolean = false) {
        Check.isMainThread()
        asyncHighlightText(newState, force)
    }

    private fun asyncHighlightText(newState: EditorState, force: Boolean) {
        val oldState = _state.value
        _state.value = newState

        val processor = textProcessor
        val text = newState.getTextString()

        if (text.isNotEmpty() && processor != null) {
            scope.launch {
                val result = withContext(Dispatchers.Default) {
                    processor.process(text)
                }
                val highlightedState = EditorState.create(
                    result.getCharSequence(),
                    newState.selection + result.offset
                )
                _state.value = highlightedState
                view?.setState(highlightedState)
                _changedEvents.emit(ChangedEvent(oldState, highlightedState, force))
            }
        } else {
            view?.setState(newState)
            scope.launch {
                _changedEvents.emit(ChangedEvent(oldState, newState, force))
            }
        }
    }

    private fun onSelectionChanged(newState: EditorState): EditorState {
        Check.isMainThread()
        _state.value = newState
        view?.setState(newState)
        scope.launch {
            _cursorMovedEvents.emit(CursorMovedEvent(newState))
        }
        return _state.value
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
            removeStart -= 1
        }

        val newText = text.substring(0, removeStart) + text.substring(selection)
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
        if (text.isEmpty() && selectionOffset == 0) {
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

    fun onMemoryValueReady(value: String) {
        insert(value)
    }

    fun onHistoryLoaded(history: RecentHistory) {
        if (!state.isEmpty()) {
            return
        }
        val historyState = history.getCurrent() ?: return
        setState(historyState.editor)
    }

    data class ChangedEvent(
        val oldState: EditorState,
        val newState: EditorState,
        val force: Boolean
    ) {
        fun shouldEvaluate(): Boolean = force || !TextUtils.equals(newState.text, oldState.text)
    }

    data class CursorMovedEvent(val state: EditorState)

    companion object {
        @JvmStatic
        fun clamp(selection: Int, text: CharSequence): Int = clamp(selection, text.length)

        @JvmStatic
        fun clamp(selection: Int, max: Int): Int = min(max(selection, 0), max)
    }
}
