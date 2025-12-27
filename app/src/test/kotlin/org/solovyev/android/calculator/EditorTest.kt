package org.solovyev.android.calculator

import android.content.SharedPreferences
import com.squareup.otto.Bus
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class EditorTest {

    private lateinit var editor: Editor

    @Before
    fun setUp() {
        editor = Editor(RuntimeEnvironment.application, mock(SharedPreferences::class.java), Tests.makeEngine())
        editor.bus = mock(Bus::class.java)
        // real text processor causes Robolectric to crash: NullPointerException at
        // org.robolectric.res.ThemeStyleSet$OverlayedStyle.equals
        editor.textProcessor = null
    }

    @Test
    fun testInsert() {
        var viewState = editor.state

        Assert.assertEquals("", viewState.textString)
        Assert.assertEquals(0, viewState.selection)

        viewState = insertAndGet("")

        Assert.assertEquals("", viewState.textString)
        Assert.assertEquals(0, viewState.selection)

        viewState = insertAndGet("test")

        Assert.assertEquals("test", viewState.textString)
        Assert.assertEquals(4, viewState.selection)

        viewState = insertAndGet("test")
        Assert.assertEquals("testtest", viewState.textString)
        Assert.assertEquals(8, viewState.selection)

        viewState = insertAndGet("")
        Assert.assertEquals("testtest", viewState.textString)
        Assert.assertEquals(8, viewState.selection)

        viewState = insertAndGet("1234567890")
        Assert.assertEquals("testtest1234567890", viewState.textString)
        Assert.assertEquals(18, viewState.selection)

        editor.moveCursorLeft()
        viewState = insertAndGet("9")
        Assert.assertEquals("testtest12345678990", viewState.textString)
        Assert.assertEquals(18, viewState.selection)

        editor.setCursorOnStart()
        viewState = insertAndGet("9")
        Assert.assertEquals("9testtest12345678990", viewState.textString)
        Assert.assertEquals(1, viewState.selection)

        editor.erase()
        viewState = insertAndGet("9")
        Assert.assertEquals("9testtest12345678990", viewState.textString)
        Assert.assertEquals(1, viewState.selection)

        viewState = insertAndGet("öäü")
        Assert.assertEquals("9öäütesttest12345678990", viewState.textString)

        editor.setCursorOnEnd()
        viewState = insertAndGet("öäü")
        Assert.assertEquals("9öäütesttest12345678990öäü", viewState.textString)
    }

    private fun insertAndGet(text: String): EditorState {
        editor.insert(text)
        return editor.state
    }

    @Test
    fun testErase() {
        setTextAndGet("")
        editor.erase()

        Assert.assertEquals("", editor.state.textString)

        setTextAndGet("test")
        editor.erase()
        Assert.assertEquals("tes", editor.state.textString)

        editor.erase()
        Assert.assertEquals("te", editor.state.textString)

        editor.erase()
        Assert.assertEquals("t", editor.state.textString)

        editor.erase()
        Assert.assertEquals("", editor.state.textString)

        editor.erase()
        Assert.assertEquals("", editor.state.textString)

        setTextAndGet("1234")
        editor.moveCursorLeft()
        editor.erase()
        Assert.assertEquals("124", editor.state.textString)

        editor.erase()
        Assert.assertEquals("14", editor.state.textString)

        editor.erase()
        Assert.assertEquals("4", editor.state.textString)

        setTextAndGet("1")
        editor.moveCursorLeft()
        editor.erase()
        Assert.assertEquals("1", editor.state.textString)
    }

    @Test
    fun testMoveSelection() {
        setTextAndGet("")

        var viewState = editor.moveSelection(0)
        Assert.assertEquals(0, viewState.selection)

        viewState = editor.moveSelection(2)
        Assert.assertEquals(0, viewState.selection)

        viewState = editor.moveSelection(100)
        Assert.assertEquals(0, viewState.selection)

        viewState = editor.moveSelection(-3)
        Assert.assertEquals(0, viewState.selection)

        viewState = editor.moveSelection(-100)
        Assert.assertEquals(0, viewState.selection)

        setTextAndGet("0123456789")

        viewState = editor.moveSelection(0)
        Assert.assertEquals(10, viewState.selection)

        viewState = editor.moveSelection(1)
        Assert.assertEquals(10, viewState.selection)

        viewState = editor.moveSelection(-2)
        Assert.assertEquals(8, viewState.selection)

        viewState = editor.moveSelection(1)
        Assert.assertEquals(9, viewState.selection)

        viewState = editor.moveSelection(-9)
        Assert.assertEquals(0, viewState.selection)

        viewState = editor.moveSelection(-10)
        Assert.assertEquals(0, viewState.selection)

        viewState = editor.moveSelection(2)
        Assert.assertEquals(2, viewState.selection)

        viewState = editor.moveSelection(2)
        Assert.assertEquals(4, viewState.selection)

        viewState = editor.moveSelection(-6)
        Assert.assertEquals(0, viewState.selection)
    }

    @Test
    fun testSetText() {
        var viewState = setTextAndGet("test")

        Assert.assertEquals("test", viewState.textString)
        Assert.assertEquals(4, viewState.selection)

        viewState = setTextAndGet("testtest")
        Assert.assertEquals("testtest", viewState.textString)
        Assert.assertEquals(8, viewState.selection)

        viewState = setTextAndGet("")
        Assert.assertEquals("", viewState.textString)
        Assert.assertEquals(0, viewState.selection)

        viewState = setTextAndGet("testtest", 0)
        Assert.assertEquals("testtest", viewState.textString)
        Assert.assertEquals(0, viewState.selection)

        viewState = setTextAndGet("testtest", 2)
        Assert.assertEquals("testtest", viewState.textString)
        Assert.assertEquals(2, viewState.selection)

        viewState = setTextAndGet("", 0)
        Assert.assertEquals("", viewState.textString)
        Assert.assertEquals(0, viewState.selection)

        viewState = setTextAndGet("", 3)
        Assert.assertEquals("", viewState.textString)
        Assert.assertEquals(0, viewState.selection)

        viewState = setTextAndGet("", -3)
        Assert.assertEquals("", viewState.textString)
        Assert.assertEquals(0, viewState.selection)

        viewState = setTextAndGet("test")
        Assert.assertEquals("test", viewState.textString)
        Assert.assertEquals(4, viewState.selection)

        viewState = setTextAndGet("", 2)
        Assert.assertEquals("", viewState.textString)
        Assert.assertEquals(0, viewState.selection)
    }

    private fun setTextAndGet(text: String, selection: Int): EditorState {
        editor.setText(text, selection)
        return editor.state
    }

    private fun setTextAndGet(text: String): EditorState {
        editor.setText(text)
        return editor.state
    }
}
