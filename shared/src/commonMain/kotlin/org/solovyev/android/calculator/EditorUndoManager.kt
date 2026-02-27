package org.solovyev.android.calculator

internal class EditorUndoManager(
    private val maxSize: Int = 100
) {
    private val undoStack = ArrayDeque<EditorState>()
    private val redoStack = ArrayDeque<EditorState>()

    fun recordBeforeChange(current: EditorState) {
        if (undoStack.lastOrNull()?.same(current) == true) return
        undoStack.addLast(current)
        trim(undoStack)
        redoStack.clear()
    }

    fun undo(current: EditorState): EditorState? {
        while (undoStack.isNotEmpty()) {
            val candidate = undoStack.removeLast()
            if (candidate.same(current)) continue
            pushRedo(current)
            return candidate
        }
        return null
    }

    fun redo(current: EditorState): EditorState? {
        while (redoStack.isNotEmpty()) {
            val candidate = redoStack.removeLast()
            if (candidate.same(current)) continue
            pushUndo(current)
            return candidate
        }
        return null
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()

    fun canRedo(): Boolean = redoStack.isNotEmpty()

    private fun pushUndo(state: EditorState) {
        if (undoStack.lastOrNull()?.same(state) == true) return
        undoStack.addLast(state)
        trim(undoStack)
    }

    private fun pushRedo(state: EditorState) {
        if (redoStack.lastOrNull()?.same(state) == true) return
        redoStack.addLast(state)
        trim(redoStack)
    }

    private fun trim(stack: ArrayDeque<EditorState>) {
        while (stack.size > maxSize) {
            stack.removeFirst()
        }
    }
}
