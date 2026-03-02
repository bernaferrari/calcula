package org.solovyev.android.calculator.ui

import org.solovyev.android.calculator.DisplayState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DisplayResultResolverTest {

    @Test
    fun `should cache valid result and reuse it for invalid states`() {
        val valid = resolveDisplayResult(
            state = DisplayState(text = "42", valid = true, sequence = 1L),
            editorText = "6*7",
            lastValidText = ""
        )
        assertEquals("42", valid.text)
        assertEquals("42", valid.updatedLastValidText)
        assertFalse(valid.isCachedValue)

        val invalid = resolveDisplayResult(
            state = DisplayState(text = "Syntax error", valid = false, sequence = 2L),
            editorText = "cos(",
            lastValidText = valid.updatedLastValidText
        )
        assertEquals("42", invalid.text)
        assertEquals("42", invalid.updatedLastValidText)
        assertTrue(invalid.isCachedValue)
    }

    @Test
    fun `should clear cache when editor and display are both empty`() {
        val cleared = resolveDisplayResult(
            state = DisplayState(text = "", valid = true, sequence = 3L),
            editorText = "",
            lastValidText = "123"
        )
        assertEquals("", cleared.text)
        assertEquals("", cleared.updatedLastValidText)
        assertFalse(cleared.isCachedValue)
    }

    @Test
    fun `should keep cache and show it while typing incomplete expression`() {
        val resolution = resolveDisplayResult(
            state = DisplayState(text = "", valid = true, sequence = 4L),
            editorText = "sin(",
            lastValidText = "0.5"
        )
        assertEquals("0.5", resolution.text)
        assertEquals("0.5", resolution.updatedLastValidText)
        assertTrue(resolution.isCachedValue)
    }

    @Test
    fun `should show state text when no cached result exists`() {
        val resolution = resolveDisplayResult(
            state = DisplayState(text = "Syntax error", valid = false, sequence = 5L),
            editorText = "cos(",
            lastValidText = ""
        )
        assertEquals("Syntax error", resolution.text)
        assertEquals("", resolution.updatedLastValidText)
        assertFalse(resolution.isCachedValue)
    }
}
