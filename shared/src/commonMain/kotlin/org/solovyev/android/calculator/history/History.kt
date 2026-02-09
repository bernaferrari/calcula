package org.solovyev.android.calculator.history

import kotlinx.coroutines.flow.Flow
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState

/**
 * History interface for retrieving past calculations.
 */
interface History {
    // Legacy support (sync) - try to avoid using in new UI if possible
    fun getLast(): EditorState

    // Reactive streams for UI
    fun observeRecent(): Flow<List<HistoryState>>
    fun observeSaved(): Flow<List<HistoryState>>

    // Actions
    suspend fun addEntry(state: HistoryState)
    suspend fun updateSaved(state: HistoryState)
    suspend fun removeSaved(state: HistoryState)
    suspend fun clearRecent()
    suspend fun clearSaved()
    suspend fun undo()
}

/**
 * Represents a history state containing editor and display states.
 * This is a KMP-compatible version without Android Parcelable/JSON dependencies.
 */
data class HistoryState(
    val id: Long = kotlin.time.Clock.System.now().toEpochMilliseconds(),
    val editor: EditorState,
    val display: DisplayState,
    val time: Long = kotlin.time.Clock.System.now().toEpochMilliseconds(),
    val comment: String = ""
) {
    fun same(that: HistoryState): Boolean {
        return this.editor.same(that.editor) && this.display.same(that.display)
    }

    fun isEmpty(): Boolean {
        return display.isEmpty() && editor.isEmpty() && comment.isEmpty()
    }
    
    companion object {
        fun builder(editor: EditorState, display: DisplayState): Builder {
            return Builder(editor, display)
        }
        
        fun builder(state: HistoryState, newState: Boolean): Builder {
            return Builder(state, newState)
        }
    }
    
    class Builder internal constructor(
        private var state: HistoryState
    ) {
        internal constructor(editor: EditorState, display: DisplayState) : this(
            HistoryState(
                editor = editor,
                display = display
            )
        )
        
        internal constructor(state: HistoryState, newState: Boolean) : this(
            HistoryState(
                id = if (newState) kotlin.time.Clock.System.now().toEpochMilliseconds() else state.id,
                editor = state.editor,
                display = state.display,
                time = if (newState) kotlin.time.Clock.System.now().toEpochMilliseconds() else state.time,
                comment = state.comment
            )
        )
        
        fun withTime(time: Long): Builder {
            state = state.copy(time = time)
            return this
        }
        
        fun withComment(comment: String?): Builder {
            state = state.copy(comment = comment ?: "")
            return this
        }
        
        fun build(): HistoryState {
            return state
        }
    }
}
