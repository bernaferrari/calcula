package org.solovyev.android.calculator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditorUndoManagerTest {

    @Test
    fun undoReturnsPreviousStateAndEnablesRedo() {
        val manager = EditorUndoManager(maxSize = 10)
        val s1 = state("1")
        val s2 = state("12")
        manager.recordBeforeChange(s1)

        val undone = manager.undo(s2)
        assertEquals("1", undone?.text)
        assertTrue(manager.canRedo())
    }

    @Test
    fun redoReturnsStatePushedByUndo() {
        val manager = EditorUndoManager(maxSize = 10)
        val s1 = state("1")
        val s2 = state("12")
        manager.recordBeforeChange(s1)

        val undone = manager.undo(s2)
        val redone = manager.redo(undone ?: state(""))
        assertEquals("12", redone?.text)
    }

    @Test
    fun newEditClearsRedoStack() {
        val manager = EditorUndoManager(maxSize = 10)
        val s1 = state("1")
        val s2 = state("12")
        val s3 = state("123")

        manager.recordBeforeChange(s1)
        val undone = manager.undo(s2) ?: state("")
        assertTrue(manager.canRedo())

        manager.recordBeforeChange(undone)
        manager.recordBeforeChange(s3)
        assertFalse(manager.canRedo())
    }

    @Test
    fun duplicateConsecutiveRecordsAreIgnored() {
        val manager = EditorUndoManager(maxSize = 10)
        val s1 = state("1")
        val s2 = state("12")

        manager.recordBeforeChange(s1)
        manager.recordBeforeChange(s1)

        val firstUndo = manager.undo(s2)
        assertEquals("1", firstUndo?.text)
        val secondUndo = manager.undo(firstUndo ?: state(""))
        assertNull(secondUndo)
    }

    @Test
    fun maxSizeDropsOldestStates() {
        val manager = EditorUndoManager(maxSize = 2)
        val s1 = state("1")
        val s2 = state("12")
        val s3 = state("123")
        val s4 = state("1234")

        manager.recordBeforeChange(s1)
        manager.recordBeforeChange(s2)
        manager.recordBeforeChange(s3)

        val undo1 = manager.undo(s4) ?: state("")
        assertEquals("123", undo1.text)
        val undo2 = manager.undo(undo1) ?: state("")
        assertEquals("12", undo2.text)
        assertNull(manager.undo(undo2))
    }

    private fun state(text: String, selection: Int = text.length): EditorState =
        EditorState.create(text, selection)
}
