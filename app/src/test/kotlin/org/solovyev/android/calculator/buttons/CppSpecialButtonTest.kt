package org.solovyev.android.calculator.buttons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CppSpecialButtonTest {

    @Test
    fun testShouldReturnButtonByGlyph() {
        assertEquals(CppSpecialButton.copy, CppSpecialButton.getByGlyph(CppSpecialButton.copy.glyph))
        assertEquals(CppSpecialButton.paste, CppSpecialButton.getByGlyph(CppSpecialButton.paste.glyph))
    }

    @Test
    fun testShouldReturnNullForButtonWithoutGlyph() {
        assertNull(CppSpecialButton.getByGlyph(CppSpecialButton.brackets_wrap.glyph))
    }
}
