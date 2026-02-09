package org.solovyev.android.calculator.history

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState

/**
 * History implementation using Room for persistence.
 */
class RoomHistory(private val historyDao: HistoryDao) : History {
    
    override fun observeRecent(): Flow<List<HistoryState>> = 
        historyDao.getRecentHistory(20).map { list -> list.map { it.toState() } }

    override fun observeSaved(): Flow<List<HistoryState>> = 
        historyDao.getSavedHistory().map { list -> list.map { it.toState() } }

    override fun getLast(): EditorState {
        // Warning: This is a placeholder. Sync API requires re-architecture or removing usage.
        return EditorState.empty() 
    }
    
    override suspend fun addEntry(state: HistoryState) {
        historyDao.insert(state.toEntry(isSaved = false).copy(id = 0)) // Auto-generate ID
    }

    suspend fun addEntry(expression: String, result: String, selection: Int = 0) {
        val entry = HistoryEntry(
            expression = expression,
            result = result,
            timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            editorSelection = selection,
            isSaved = false
        )
        historyDao.insert(entry)
    }

    override suspend fun updateSaved(state: HistoryState) {
        historyDao.update(state.toEntry(isSaved = true))
    }

    override suspend fun removeSaved(state: HistoryState) {
        historyDao.delete(state.toEntry(isSaved = true))
    }

    override suspend fun clearRecent() = historyDao.clearRecent()

    override suspend fun clearSaved() = historyDao.clearSaved()

    override suspend fun undo() {
        // Simple undo logic: remove last recent entry? 
        // Or if this refers to editor undo, it shouldn't be here.
        // Assuming it means remove last history entry.
        val last = historyDao.getLastEntry()
        if (last != null && !last.isSaved) {
            historyDao.delete(last)
        }
    }
    
    /**
     * Gets the last history entry.
     */
    suspend fun getLastEntry(): HistoryEntry? {
        return historyDao.getLastEntry()
    }
    
    /**
     * Gets current history entry as EditorState for Editor integration.
     */
    suspend fun getCurrentEditorState(): EditorState {
        val lastEntry = historyDao.getLastEntry()
        return lastEntry?.let {
            EditorState.create(it.expression, it.editorSelection)
        } ?: EditorState.empty()
    }
    
    private fun HistoryEntry.toState() = HistoryState(
        id = id,
        editor = EditorState.create(expression, editorSelection),
        display = DisplayState(text = result, valid = true, sequence = 0),
        time = timestamp,
        comment = comment
    )

    private fun HistoryState.toEntry(isSaved: Boolean) = HistoryEntry(
        id = id,
        expression = editor.getTextString(),
        result = display.text,
        timestamp = time,
        editorSelection = editor.selection,
        comment = comment,
        isSaved = isSaved
    )
}

/**
 * RecentHistory wrapper for compatibility with existing Editor usage.
 */
class RecentHistory(private val roomHistory: RoomHistory) {
    suspend fun getCurrent(): HistoryState? {
        val entry = roomHistory.getLastEntry() ?: return null
        return HistoryState(
            editor = EditorState.create(entry.expression, entry.editorSelection),
            display = DisplayState(text = entry.result, valid = true, sequence = 0)
        )
    }
}
