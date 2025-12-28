package org.solovyev.android.calculator.history

import android.os.Handler
import android.os.Looper
import dagger.Lazy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.FileSystem as OkioFileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.solovyev.android.calculator.Clipboard
import org.solovyev.android.calculator.Display
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.ErrorReporter
import org.solovyev.android.calculator.Notifier
import org.solovyev.android.calculator.Tests
import org.solovyev.android.calculator.UiPreferences
import org.solovyev.android.calculator.di.AppCoroutineScope
import org.solovyev.android.calculator.di.AppDirectories
import org.solovyev.android.calculator.di.AppDispatchers
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.jscl.JsclOperation.numeric
import org.solovyev.android.calculator.json.Json
import org.solovyev.android.calculator.testutils.MainDispatcherRule
import org.solovyev.android.io.FileSystem
import java.io.File

@RunWith(RobolectricTestRunner::class)
class HistoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var env: Tests.CalculatorEnvironment
    private lateinit var history: History
    private lateinit var appPreferences: AppPreferences
    private lateinit var dispatchers: AppDispatchers
    private lateinit var appScope: AppCoroutineScope
    private lateinit var directories: AppDirectories
    private lateinit var fileSystem: FileSystem
    private lateinit var handler: Handler
    private val errorReporter: ErrorReporter = object : ErrorReporter {
        override fun onException(e: Throwable) {
            throw AssertionError(e)
        }

        override fun onError(message: String) {
            throw AssertionError(message)
        }
    }

    @Before
    fun setUp() {
        env = Tests.createCalculatorEnvironment()
        appPreferences = env.appPreferences
        dispatchers = AppDispatchers()
        appScope = AppCoroutineScope(dispatchers)
        directories = AppDirectories(env.application, dispatchers, appScope)
        handler = Handler(Looper.getMainLooper())
        fileSystem = FileSystem()
        fileSystem.errorReporter = errorReporter
        history = createHistory(fileSystem)
        history.init()
        awaitLoaded(history)
    }

    @After
    fun tearDown() {
        deleteHistoryFile("history-recent.json")
        deleteHistoryFile("history-saved.json")
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

        val states = history.getRecent()
        assertEquals(3, states.size)
        assertEquals("23547", states[0].editor.getTextString())
        // intermediate state
        assertEquals("235", states[1].editor.getTextString())
        assertEquals("123+3", states[2].editor.getTextString())
    }

    @Test
    fun testRecentHistoryShouldTakeIntoAccountGroupingSeparator() = runBlocking {
        appPreferences.settings.setOutputSeparator(' ')
        addState("1")
        addState("12")
        addState("123")
        addState("1 234")
        addState("12 345")

        var states = history.getRecent()
        assertEquals(1, states.size)
        assertEquals("12 345", states[0].editor.getTextString())
        history.clearRecent()

        appPreferences.settings.setOutputSeparator('\'')
        addState("1")
        addState("12")
        addState("123")
        addState("1'234")
        addState("12'345")
        addState("12 345")

        states = history.getRecent()
        assertEquals(2, states.size)
        assertEquals("12 345", states[0].editor.getTextString())
        assertEquals("12'345", states[1].editor.getTextString())
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

        val states = history.getRecent()
        assertEquals(2, states.size)
        assertEquals("34", states[0].editor.getTextString())
        assertEquals("12", states[1].editor.getTextString())
    }

    private fun addState(text: String) {
        history.addRecent(
            HistoryState.builder(EditorState.create(text, 3), DisplayState.empty()).build()
        )
    }

    @Test
    fun testShouldConvertOldHistory() {
        var states = requireNotNull(History.convertOldHistory(oldXml1))
        assertEquals(1, states.size)

        var state = states[0]
        assertEquals(100000000, state.time)
        assertEquals("", state.comment)
        assertEquals("1+1", state.editor.getTextString())
        assertEquals(3, state.editor.selection)
        assertEquals("Error", state.display.text)
        assertEquals(true, state.display.valid)
        assertNull(state.display.result)

        states = requireNotNull(History.convertOldHistory(oldXml2))
        checkOldXml2States(states)
    }

    private fun checkOldXml2States(states: List<HistoryState>) {
        assertNotNull(states)
        assertEquals(4, states.size)

        var state = states[0]
        assertEquals(100000000, state.time)
        assertEquals("boom", state.comment)
        assertEquals("1+11", state.editor.getTextString())
        assertEquals(3, state.editor.selection)
        assertEquals("Error", state.display.text)
        assertEquals(true, state.display.valid)
        assertNull(state.display.result)

        state = states[3]
        assertEquals(1, state.time)
        assertEquals("", state.comment)
        assertEquals("4+5/35sin(41)+dfdsfsdfs", state.editor.getTextString())
        assertEquals(0, state.editor.selection)
        assertEquals("4+5/35sin(41)+dfdsfsdfs", state.display.text)
        assertEquals(true, state.display.valid)
        assertNull(state.display.result)
    }

    @Test
    fun testShouldLoadStates() {
        val resource = requireNotNull(HistoryTest::class.java.getResource("recent-history.json"))
        val file = File(resource.file)
        val states = Json.load(file.absolutePath.toPath(), fileSystem, HistoryState.JSON_CREATOR)
        assertEquals(8, states.size)

        var state = states[0]
        assertEquals(1452770652381L, state.time)
        assertEquals("", state.comment)
        assertEquals("01 234 567 890 123 456 789", state.editor.getTextString())
        assertEquals(26, state.editor.selection)
        assertEquals("1 234 567 890 123 460 000", state.display.text)

        state = states[4]
        assertEquals(1452770626394L, state.time)
        assertEquals("", state.comment)
        assertEquals("985", state.editor.getTextString())
        assertEquals(3, state.editor.selection)
        assertEquals("985", state.display.text)

        state = states[7]
        assertEquals(1452770503823L, state.time)
        assertEquals("", state.comment)
        assertEquals("52", state.editor.getTextString())
        assertEquals(2, state.editor.selection)
        assertEquals("52", state.display.text)
    }

    @Test
    fun testShouldClearSaved() {
        val mockFileSystem = mock(FileSystem::class.java)
        val historyWithMock = createHistory(mockFileSystem)
        historyWithMock.init()
        awaitLoaded(historyWithMock)

        historyWithMock.updateSaved(
            HistoryState.builder(
                EditorState.create("text", 0),
                DisplayState.createValid(numeric, null, "result", 0)
            ).build()
        )
        Robolectric.flushForegroundThreadScheduler()
        assertTrue(historyWithMock.getSaved().isNotEmpty())

        historyWithMock.clearSaved()
        Robolectric.flushForegroundThreadScheduler()

        assertTrue(historyWithMock.getSaved().isEmpty())
        val pathCaptor = argumentCaptor<Path>()
        runBlocking {
            verify(mockFileSystem, timeout(1000)).writeSilently(pathCaptor.capture(), eq("[]"))
        }
        assertEquals("history-saved.json", pathCaptor.firstValue.name)
    }

    @Test
    fun testShouldClearRecent() {
        val mockFileSystem = mock(FileSystem::class.java)
        val historyWithMock = createHistory(mockFileSystem)
        historyWithMock.init()
        awaitLoaded(historyWithMock)

        historyWithMock.addRecent(
            HistoryState.builder(
                EditorState.create("text", 0),
                DisplayState.createValid(numeric, null, "result", 0)
            ).build()
        )
        Robolectric.flushForegroundThreadScheduler()
        assertTrue(historyWithMock.getRecent().isNotEmpty())

        historyWithMock.clearRecent()
        Robolectric.flushForegroundThreadScheduler()

        assertTrue(historyWithMock.getRecent().isEmpty())
        val pathCaptor = argumentCaptor<Path>()
        runBlocking {
            verify(mockFileSystem, timeout(1000)).writeSilently(pathCaptor.capture(), eq("[]"))
        }
        assertEquals("history-recent.json", pathCaptor.firstValue.name)
    }

    @Test
    fun testShouldUpdateSaved() {
        val state = HistoryState.builder(
            EditorState.create("text", 0),
            DisplayState.createValid(numeric, null, "result", 0)
        ).build()
        history.updateSaved(state)
        assertTrue(history.getSaved().size == 1)
        assertEquals(state.time, history.getSaved()[0].time)

        history.updateSaved(HistoryState.builder(state, false).withTime(10).build())
        assertTrue(history.getSaved().size == 1)
        assertEquals(10, history.getSaved()[0].time)
    }

    @Test
    fun testShouldAddStateIfEditorAndDisplayAreInSync() {
        val editorState = EditorState.create("editor", 2)
        env.editor.setState(editorState)

        val displayState = DisplayState.createError(numeric, "test", editorState.sequence)
        history.onDisplayChanged(Display.ChangedEvent(DisplayState.empty(), displayState))

        val states = history.getRecent()
        assertEquals(1, states.size)
        assertEquals(editorState, states[0].editor)
        assertEquals(displayState, states[0].display)
    }

    @Test
    fun testShouldNotAddStateIfEditorAndDisplayAreOutOfSync() {
        val editorState = EditorState.create("editor", 2)
        env.editor.setState(editorState)

        val displayState = DisplayState.createError(numeric, "test", editorState.sequence - 1)
        history.onDisplayChanged(Display.ChangedEvent(DisplayState.empty(), displayState))

        val states = history.getRecent()
        assertEquals(0, states.size)
    }

    private fun createHistory(fileSystem: FileSystem): History {
        val notifier = Notifier().apply {
            application = env.application
            handler = this@HistoryTest.handler
        }
        val uiPreferences = UiPreferences(appPreferences)
        val display = Display(
            env.application,
            daggerLazy(Clipboard(env.application)),
            daggerLazy(notifier),
            daggerLazy(uiPreferences),
            env.calculator
        )
        display.init()
        fileSystem.errorReporter = errorReporter
        return History(
            env.application,
            handler,
            appPreferences,
            env.editor,
            display,
            errorReporter,
            fileSystem,
            directories,
            dispatchers,
            appScope
        )
    }

    private fun awaitLoaded(history: History) {
        runBlocking {
            history.loaded.first { it }
        }
    }

    private fun deleteHistoryFile(name: String) {
        val path = directories.getFile(name)
        if (OkioFileSystem.SYSTEM.exists(path)) {
            OkioFileSystem.SYSTEM.delete(path)
        }
    }

    private fun <T> daggerLazy(value: T): Lazy<T> = object : Lazy<T> {
        override fun get(): T = value
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
