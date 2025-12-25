/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.history

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import com.squareup.otto.Bus
import dagger.Lazy
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.solovyev.android.calculator.Display
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.ErrorReporter
import org.solovyev.android.calculator.Tests.sameThreadExecutor
import org.solovyev.android.calculator.jscl.JsclOperation.numeric
import org.solovyev.android.calculator.json.Json
import org.solovyev.android.io.FileSystem
import java.io.File

@RunWith(RobolectricTestRunner::class)
class HistoryTest {

    private lateinit var history: History

    @Before
    fun setUp() {
        history = History()
        history.backgroundThread = sameThreadExecutor()
        history.filesDir = Lazy { File(".") }
        history.application = RuntimeEnvironment.application
        history.bus = mock(Bus::class.java)
        history.errorReporter = mock(ErrorReporter::class.java)
        history.fileSystem = mock(FileSystem::class.java)
        history.handler = Handler(Looper.getMainLooper())
        history.preferences = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)
        `when`(history.preferences.edit()).thenReturn(editor)
        `when`(editor.remove(anyString())).thenReturn(editor)
        history.editor = mock(Editor::class.java)
        history.setLoaded(true)
    }

    @After
    fun tearDown() {
        history.savedHistoryFile.delete()
        history.recentHistoryFile.delete()
    }

    @Test
    fun testGetStates() {
        addState("1")
        addState("12")
        addState("123")
        addState("123+")
        addState("123+3")
        addState("")
        addState("2")
        addState("23")
        addState("235")
        addState("2355")
        addState("235")
        addState("2354")
        addState("23547")

        val states = history.recent
        assertEquals(3, states.size)
        assertEquals("23547", states[0].editor.textString)
        // intermediate state
        assertEquals("235", states[1].editor.textString)
        assertEquals("123+3", states[2].editor.textString)
    }

    @Test
    fun testRecentHistoryShouldTakeIntoAccountGroupingSeparator() {
        `when`(history.preferences.contains(eq(Engine.Preferences.Output.separator.key))).thenReturn(true)
        `when`(history.preferences.getString(eq(Engine.Preferences.Output.separator.key), anyString())).thenReturn(" ")
        addState("1")
        addState("12")
        addState("123")
        addState("1 234")
        addState("12 345")

        var states = history.recent
        assertEquals(3, states.size)
        assertEquals("12 345", states[0].editor.textString)
        assertEquals("1 234", states[1].editor.textString)
        assertEquals("123", states[2].editor.textString)
        history.clearRecent()

        `when`(history.preferences.getString(eq(Engine.Preferences.Output.separator.key), anyString())).thenReturn("'")
        addState("1")
        addState("12")
        addState("123")
        addState("1'234")
        addState("12'345")
        addState("12 345")

        states = history.recent
        assertEquals(4, states.size)
        assertEquals("12 345", states[0].editor.textString)
        assertEquals("12'345", states[1].editor.textString)
    }

    @Test
    fun testRecentHistoryShouldNotContainEmptyStates() {
        addState("")
        addState("1")
        addState("12")
        addState("")
        addState("")
        addState("34")
        addState("")

        val states = history.recent
        assertEquals(2, states.size)
        assertEquals("34", states[0].editor.textString)
        assertEquals("12", states[1].editor.textString)
    }

    private fun addState(text: String) {
        history.addRecent(HistoryState.builder(EditorState.create(text, 3), DisplayState.empty()).build())
    }

    @Test
    fun testShouldConvertOldHistory() {
        var states = History.convertOldHistory(oldXml1)
        assertNotNull(states)
        assertEquals(1, states.size)

        var state = states[0]
        assertEquals(100000000, state.time)
        assertEquals("", state.comment)
        assertEquals("1+1", state.editor.textString)
        assertEquals(3, state.editor.selection)
        assertEquals("Error", state.display.text)
        assertEquals(true, state.display.valid)
        assertNull(state.display.result)

        states = History.convertOldHistory(oldXml2)
        checkOldXml2States(states)
    }

    private fun checkOldXml2States(states: List<HistoryState>) {
        assertNotNull(states)
        assertEquals(4, states.size)

        var state = states[0]
        assertEquals(100000000, state.time)
        assertEquals("boom", state.comment)
        assertEquals("1+11", state.editor.textString)
        assertEquals(3, state.editor.selection)
        assertEquals("Error", state.display.text)
        assertEquals(true, state.display.valid)
        assertNull(state.display.result)

        state = states[3]
        assertEquals(1, state.time)
        assertEquals("", state.comment)
        assertEquals("4+5/35sin(41)+dfdsfsdfs", state.editor.textString)
        assertEquals(0, state.editor.selection)
        assertEquals("4+5/35sin(41)+dfdsfsdfs", state.display.text)
        assertEquals(true, state.display.valid)
        assertNull(state.display.result)
    }

    @Test
    fun testShouldMigrateOldHistory() {
        history.fileSystem = FileSystem()
        `when`(history.preferences.getString(eq(History.OLD_HISTORY_PREFS_KEY), any())).thenReturn(oldXml2)
        history.init(sameThreadExecutor())
        Robolectric.flushForegroundThreadScheduler()
        checkOldXml2States(history.saved)
    }

    @Test
    fun testShouldWriteNewHistoryFile() {
        history.fileSystem = mock(FileSystem::class.java)
        `when`(history.preferences.getString(eq(History.OLD_HISTORY_PREFS_KEY), any()))
            .thenReturn(oldXml1)
        history.init(sameThreadExecutor())
        Robolectric.flushForegroundThreadScheduler()
        verify(history.fileSystem).write(eq(history.savedHistoryFile), eq(
            "[{\"e\":{\"t\":\"1+1\",\"s\":3},\"d\":{\"t\":\"Error\",\"v\":true},\"t\":100000000}]"))
    }

    @Test
    fun testShouldAddStateIfEditorAndDisplayAreInSync() {
        val editorState = EditorState.create("editor", 2)
        `when`(history.editor.state).thenReturn(editorState)

        val displayState = DisplayState.createError(numeric, "test", editorState.sequence)
        history.onDisplayChanged(Display.ChangedEvent(DisplayState.empty(), displayState))

        val states = history.recent
        assertEquals(1, states.size)
        assertSame(editorState, states[0].editor)
        assertSame(displayState, states[0].display)
    }

    @Test
    fun testShouldNotAddStateIfEditorAndDisplayAreOutOfSync() {
        val editorState = EditorState.create("editor", 2)
        `when`(history.editor.state).thenReturn(editorState)

        val displayState = DisplayState.createError(numeric, "test", editorState.sequence - 1)
        history.onDisplayChanged(Display.ChangedEvent(DisplayState.empty(), displayState))

        val states = history.recent
        assertEquals(0, states.size)
    }

    @Test
    fun testShouldReportOnMigrateException() {
        `when`(history.preferences.getString(eq(History.OLD_HISTORY_PREFS_KEY), any())).thenReturn(
            "boom")
        history.init(sameThreadExecutor())

        verify(history.errorReporter).onException(any(Throwable::class.java))
    }

    @Test
    fun testShouldNotRemoveOldHistoryOnError() {
        `when`(history.preferences.getString(eq(History.OLD_HISTORY_PREFS_KEY), any())).thenReturn("boom")
        history.init(sameThreadExecutor())

        verify(history.preferences, never()).edit()
        verify(history.errorReporter).onException(any(Throwable::class.java))
    }

    @Test
    fun testShouldLoadStates() {
        val states = Json.load(File(HistoryTest::class.java.getResource("recent-history.json").file),
            FileSystem(), HistoryState.JSON_CREATOR)
        assertEquals(8, states.size)

        var state = states[0]
        assertEquals(1452770652381L, state.time)
        assertEquals("", state.comment)
        assertEquals("01 234 567 890 123 456 789", state.editor.textString)
        assertEquals(26, state.editor.selection)
        assertEquals("1 234 567 890 123 460 000", state.display.text)

        state = states[4]
        assertEquals(1452770626394L, state.time)
        assertEquals("", state.comment)
        assertEquals("985", state.editor.textString)
        assertEquals(3, state.editor.selection)
        assertEquals("985", state.display.text)

        state = states[7]
        assertEquals(1452770503823L, state.time)
        assertEquals("", state.comment)
        assertEquals("52", state.editor.textString)
        assertEquals(2, state.editor.selection)
        assertEquals("52", state.display.text)
    }

    @Test
    fun testShouldClearSaved() {
        history.updateSaved(HistoryState.builder(EditorState.create("text", 0),
            DisplayState.createValid(numeric, null, "result", 0)).build())
        Robolectric.flushForegroundThreadScheduler()
        assertTrue(history.saved.isNotEmpty())

        // renew counter
        history.fileSystem = mock(FileSystem::class.java)
        history.clearSaved()
        Robolectric.flushForegroundThreadScheduler()

        assertTrue(history.saved.isEmpty())
        verify(history.fileSystem).writeSilently(eq(history.savedHistoryFile), eq("[]"))
    }

    @Test
    fun testShouldClearRecent() {
        history.addRecent(HistoryState.builder(EditorState.create("text", 0),
            DisplayState.createValid(numeric, null, "result", 0)).build())
        Robolectric.flushForegroundThreadScheduler()
        assertTrue(history.recent.isNotEmpty())

        // renew counter
        history.fileSystem = mock(FileSystem::class.java)
        history.clearRecent()
        Robolectric.flushForegroundThreadScheduler()

        assertTrue(history.recent.isEmpty())
        verify(history.fileSystem).writeSilently(eq(history.recentHistoryFile), eq("[]"))
    }

    @Test
    fun testShouldUpdateSaved() {
        val state = HistoryState.builder(EditorState.create("text", 0),
            DisplayState.createValid(numeric, null, "result", 0)).build()
        history.updateSaved(state)
        assertTrue(history.saved.size == 1)
        assertEquals(state.time, history.saved[0].time)

        history.updateSaved(HistoryState.builder(state, false).withTime(10).build())
        assertTrue(history.saved.size == 1)
        assertEquals(10, history.saved[0].time)
    }

    companion object {
        private const val oldXml1 = """<history>
   <historyItems class="java.util.ArrayList">
      <calculatorHistoryState>
         <time>100000000</time>
         <editorState>
            <cursorPosition>3</cursorPosition>
            <text>1+1</text>
         </editorState>
         <displayState>
            <editorState>
               <cursorPosition>0</cursorPosition>
               <text>Error</text>
            </editorState>
            <jsclOperation>simplify</jsclOperation>
         </displayState>
      </calculatorHistoryState>
   </historyItems>
</history>"""

        private const val oldXml2 = """<history>
   <historyItems class="java.util.ArrayList">
      <calculatorHistoryState>
         <time>100000000</time>
         <comment>boom</comment>
         <editorState>
            <cursorPosition>3</cursorPosition>
            <text>1+11</text>
         </editorState>
         <displayState>
            <editorState>
               <cursorPosition>0</cursorPosition>
               <text>Error</text>
            </editorState>
            <jsclOperation>simplify</jsclOperation>
         </displayState>
      </calculatorHistoryState>
      <calculatorHistoryState>
         <time>100000000</time>
         <editorState>
            <cursorPosition>2</cursorPosition>
            <text>5/6</text>
         </editorState>
         <displayState>
            <editorState>
               <cursorPosition>3</cursorPosition>
               <text>5/6</text>
            </editorState>
            <jsclOperation>numeric</jsclOperation>
         </displayState>
      </calculatorHistoryState>
      <calculatorHistoryState>
         <time>100000000</time>
         <editorState>
            <cursorPosition>1</cursorPosition>
            <text></text>
         </editorState>
         <displayState>
            <editorState>
               <cursorPosition>0</cursorPosition>
               <text>Error</text>
            </editorState>
            <jsclOperation>elementary</jsclOperation>
         </displayState>
      </calculatorHistoryState>
      <calculatorHistoryState>
         <time>1</time>
         <editorState>
            <cursorPosition>0</cursorPosition>
            <text>4+5/35sin(41)+dfdsfsdfs</text>
         </editorState>
         <displayState>
            <editorState>
               <cursorPosition>1</cursorPosition>
               <text>4+5/35sin(41)+dfdsfsdfs</text>
            </editorState>
            <jsclOperation>numeric</jsclOperation>
         </displayState>
      </calculatorHistoryState>
   </historyItems>
</history>"""
    }
}
