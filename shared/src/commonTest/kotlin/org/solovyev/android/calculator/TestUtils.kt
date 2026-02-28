package org.solovyev.android.calculator

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.performScrollTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.solovyev.android.calculator.history.HistoryState
import org.solovyev.android.calculator.ui.settings.SettingsActions
import org.solovyev.android.calculator.ui.settings.SettingsUiState
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test tags for Compose UI testing
 */
object CalculatorTestTags {
    const val DISPLAY_TEXT = "display_text"
    const val EDITOR_TEXT = "editor_text"
    const val RESULT_TEXT = "result_text"
    const val PREVIEW_RESULT = "preview_result"
    const val ERROR_TEXT = "error_text"
    const val CLEAR_BUTTON = "clear_button"
    const val DELETE_BUTTON = "delete_button"
    const val EQUALS_BUTTON = "equals_button"
    const val HISTORY_BUTTON = "history_button"
    const val SETTINGS_BUTTON = "settings_button"
    const val FUNCTIONS_BUTTON = "functions_button"
    const val CONVERTER_BUTTON = "converter_button"
    const val KEYBOARD_CONTAINER = "keyboard_container"
    const val HISTORY_LIST = "history_list"
    const val EMPTY_STATE = "empty_state"
    const val TAPE_PANEL = "tape_panel"
    const val RPN_STACK_PANEL = "rpn_stack_panel"
    const val CALCULATION_LATENCY = "calculation_latency"
    const val UNIT_HINT = "unit_hint"
    const val COPY_FEEDBACK = "copy_feedback"
    const val SHARE_FEEDBACK = "share_feedback"
    const val SEARCH_BAR = "search_bar"
    const val SETTINGS_LIST = "settings_list"
    const val TUTORIAL_OVERLAY = "tutorial_overlay"
    const val TUTORIAL_SKIP_BUTTON = "tutorial_skip_button"
    const val TUTORIAL_NEXT_BUTTON = "tutorial_next_button"
    const val GESTURE_HINT = "gesture_hint"
}

/**
 * Extension functions for ComposeTestRule to simplify test writing
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeContentTestRule.waitForIdle(timeoutMillis: Long = 5000) {
    waitUntil(timeoutMillis) { true }
}

fun ComposeContentTestRule.onDigitButton(digit: String): SemanticsNodeInteraction {
    return onNode(hasText(digit) and hasClickAction())
}

fun ComposeContentTestRule.onOperationButton(operation: String): SemanticsNodeInteraction {
    return onNode(hasText(operation) and hasClickAction())
}

fun ComposeContentTestRule.onDisplay(): SemanticsNodeInteraction {
    return onNodeWithTag(CalculatorTestTags.DISPLAY_TEXT)
}

fun ComposeContentTestRule.onEditor(): SemanticsNodeInteraction {
    return onNodeWithTag(CalculatorTestTags.EDITOR_TEXT)
}

fun ComposeContentTestRule.onResult(): SemanticsNodeInteraction {
    return onNodeWithTag(CalculatorTestTags.RESULT_TEXT)
}

fun ComposeContentTestRule.onClearButton(): SemanticsNodeInteraction {
    return onNodeWithContentDescription("Clear") 
        .assertExists("Clear button should exist")
}

fun ComposeContentTestRule.onDeleteButton(): SemanticsNodeInteraction {
    return onNodeWithContentDescription("Delete")
        .assertExists("Delete button should exist")
}

fun ComposeContentTestRule.onEqualsButton(): SemanticsNodeInteraction {
    return onNodeWithTag(CalculatorTestTags.EQUALS_BUTTON)
}

fun ComposeContentTestRule.assertDisplayShows(text: String) {
    onDisplay().assertTextEquals(text)
}

fun ComposeContentTestRule.assertResultShows(text: String) {
    onResult().assertTextEquals(text)
}

fun ComposeContentTestRule.assertEditorShows(text: String) {
    onEditor().assertTextEquals(text)
}

fun ComposeContentTestRule.enterNumber(number: String) {
    number.forEach { digit ->
        onDigitButton(digit.toString()).performClick()
    }
}

fun ComposeContentTestRule.enterExpression(expression: String) {
    expression.forEach { char ->
        when (char) {
            in '0'..'9', '.' -> onDigitButton(char.toString()).performClick()
            '+', '-', '−' -> onOperationButton("+").performClick()
            '*', '×' -> onOperationButton("×").performClick()
            '/', '÷' -> onOperationButton("÷").performClick()
            '(', ')' -> onOperationButton(char.toString()).performClick()
            else -> {
                // Try to find a button with this text
                onNodeWithText(char.toString()).performClick()
            }
        }
    }
}

fun ComposeContentTestRule.performSwipeUpOnButton(buttonText: String) {
    onNode(hasText(buttonText) and hasClickAction())
        .performTouchInput { swipeUp() }
}

fun ComposeContentTestRule.performSwipeDownOnButton(buttonText: String) {
    onNode(hasText(buttonText) and hasClickAction())
        .performTouchInput { swipeDown() }
}

fun ComposeContentTestRule.performSwipeLeftOnButton(buttonText: String) {
    onNode(hasText(buttonText) and hasClickAction())
        .performTouchInput { swipeLeft() }
}

fun ComposeContentTestRule.performSwipeRightOnButton(buttonText: String) {
    onNode(hasText(buttonText) and hasClickAction())
        .performTouchInput { swipeRight() }
}

fun ComposeContentTestRule.performLongPressOnButton(buttonText: String) {
    onNode(hasText(buttonText) and hasClickAction())
        .performTouchInput { longClick() }
}

/**
 * Fake ViewModel for Calculator tests
 */
class FakeCalculatorViewModel : CalculatorViewModel(
    calculator = FakeCalculator(),
    editor = FakeEditor(),
    display = FakeDisplay(),
    roomHistory = FakeRoomHistory(),
    memory = FakeMemory(),
    notifier = FakeNotifier(),
    appPreferences = FakeAppPreferences()
) {
    private val _testDisplayState = MutableStateFlow(DisplayState.empty())
    override val displayState: StateFlow<DisplayState> = _testDisplayState

    private val _testEditorState = MutableStateFlow(EditorState.empty())
    override val editorState: StateFlow<EditorState> = _testEditorState

    private val _testPreviewResult = MutableStateFlow<String?>(null)
    override val previewResult: StateFlow<String?> = _testPreviewResult

    private val _testRpnMode = MutableStateFlow(false)
    override val rpnMode: StateFlow<Boolean> = _testRpnMode

    private val _testRpnStack = MutableStateFlow<List<String>>(emptyList())
    override val rpnStack: StateFlow<List<String>> = _testRpnStack

    private val _testTapeMode = MutableStateFlow(false)
    override val tapeMode: StateFlow<Boolean> = _testTapeMode

    private val _testTapeEntries = MutableStateFlow<List<TapeEntry>>(emptyList())
    override val tapeEntries: StateFlow<List<TapeEntry>> = _testTapeEntries

    var lastDigitPressed: String? = null
    var lastOperatorPressed: String? = null
    var clearCalled = false
    var backspaceCalled = false
    var equalsCalled = false

    override fun onDigitPressed(digit: String) {
        lastDigitPressed = digit
        val currentText = _testEditorState.value.text.toString()
        val newText = currentText + digit
        _testEditorState.value = EditorState.create(newText, newText.length)
    }

    override fun onOperatorPressed(operator: String) {
        lastOperatorPressed = operator
        val currentText = _testEditorState.value.text.toString()
        val newText = currentText + " $operator "
        _testEditorState.value = EditorState.create(newText, newText.length)
    }

    override fun onClear() {
        clearCalled = true
        _testEditorState.value = EditorState.empty()
        _testDisplayState.value = DisplayState.empty()
        _testPreviewResult.value = null
    }

    override fun onBackspace(): Boolean {
        backspaceCalled = true
        val currentText = _testEditorState.value.text.toString()
        if (currentText.isNotEmpty()) {
            val newText = currentText.dropLast(1)
            _testEditorState.value = EditorState.create(newText, newText.length)
            return true
        }
        return false
    }

    override fun onEquals() {
        equalsCalled = true
        // Simulate calculation result
        val expression = _testEditorState.value.text.toString()
        val result = calculateFakeResult(expression)
        _testDisplayState.value = DisplayState(
            text = result,
            valid = true,
            sequence = _testEditorState.value.sequence
        )
    }

    fun setDisplayState(state: DisplayState) {
        _testDisplayState.value = state
    }

    fun setEditorState(state: EditorState) {
        _testEditorState.value = state
    }

    fun setPreviewResult(result: String?) {
        _testPreviewResult.value = result
    }

    fun setRpnMode(enabled: Boolean) {
        _testRpnMode.value = enabled
    }

    fun setTapeMode(enabled: Boolean) {
        _testTapeMode.value = enabled
    }

    private fun calculateFakeResult(expression: String): String {
        // Simple mock calculation for testing
        return when {
            expression.contains("2 + 2") -> "4"
            expression.contains("5 - 3") -> "2"
            expression.contains("4 × 5") -> "20"
            expression.contains("10 ÷ 2") -> "5"
            expression.contains("3.14") -> "3.14"
            expression.contains("2 + 3 × 4") -> "14"
            else -> "0"
        }
    }
}

/**
 * Fake implementations for dependencies
 */
class FakeCalculator : Calculator {
    override fun evaluate(operation: jscl.JsclOperation, expression: String, sequence: Long) {
        // No-op for tests
    }

    override fun evaluateForPreview(expression: String): String {
        return "preview_result"
    }

    override fun setCalculateOnFly(enabled: Boolean) {
        // No-op for tests
    }
}

class FakeEditor : Editor {
    private val _stateFlow = MutableStateFlow(EditorState.empty())
    override val stateFlow: StateFlow<EditorState> = _stateFlow
    override val state: EditorState get() = _stateFlow.value
    override val changedEvents: kotlinx.coroutines.flow.Flow<EditorChangedEvent> 
        get() = kotlinx.coroutines.flow.emptyFlow()

    override fun insert(text: String, selectionOffset: Int) {
        val current = _stateFlow.value
        val newText = current.text.toString().insert(current.selection, text)
        _stateFlow.value = EditorState.create(newText, current.selection + text.length + selectionOffset)
    }

    override fun setText(text: String, selection: Int) {
        _stateFlow.value = EditorState.create(text, selection)
    }

    override fun clear() {
        _stateFlow.value = EditorState.empty()
    }

    override fun erase(): Boolean {
        val current = _stateFlow.value
        if (current.text.isNotEmpty()) {
            val newText = current.text.toString().dropLast(1)
            _stateFlow.value = EditorState.create(newText, newText.length)
            return true
        }
        return false
    }

    override fun moveCursorLeft() {
        val current = _stateFlow.value
        if (current.selection > 0) {
            _stateFlow.value = EditorState.create(current.text.toString(), current.selection - 1)
        }
    }

    override fun moveCursorRight() {
        val current = _stateFlow.value
        if (current.selection < current.text.length) {
            _stateFlow.value = EditorState.create(current.text.toString(), current.selection + 1)
        }
    }

    override fun setCursorOnStart() {
        val current = _stateFlow.value
        _stateFlow.value = EditorState.create(current.text.toString(), 0)
    }

    override fun setCursorOnEnd() {
        val current = _stateFlow.value
        _stateFlow.value = EditorState.create(current.text.toString(), current.text.length)
    }

    override fun setSelection(selection: Int) {
        val current = _stateFlow.value
        _stateFlow.value = EditorState.create(current.text.toString(), selection)
    }

    override fun undo(): Boolean = false
    override fun redo(): Boolean = false
}

class FakeDisplay : Display {
    private val _stateFlow = MutableStateFlow(DisplayState.empty())
    override val stateFlow: StateFlow<DisplayState> = _stateFlow
    override val changedEvents: kotlinx.coroutines.flow.Flow<DisplayChangedEvent>
        get() = kotlinx.coroutines.flow.emptyFlow()

    override fun init() {
        // No-op for tests
    }

    override fun setState(state: DisplayState) {
        _stateFlow.value = state
    }

    override fun getState(): DisplayState = _stateFlow.value

    override fun getTextToCopy(): String? {
        val state = _stateFlow.value
        return if (state.valid && state.text.isNotEmpty()) state.text else null
    }

    override fun showCopiedMessage() {
        // No-op for tests
    }
}

class FakeRoomHistory : RoomHistory {
    private val _recentFlow = MutableStateFlow<List<HistoryState>>(emptyList())
    override fun observeRecent(): StateFlow<List<HistoryState>> = _recentFlow

    override suspend fun addEntry(expression: String, result: String, selection: Int) {
        // No-op for tests
    }

    override suspend fun clearRecent() {
        _recentFlow.value = emptyList()
    }

    override fun getLastEntry(): HistoryState? = null

    fun setRecentHistory(history: List<HistoryState>) {
        _recentFlow.value = history
    }
}

class FakeMemory : org.solovyev.android.calculator.memory.Memory {
    private val _activeRegister = MutableStateFlow("M")
    override val activeRegister: StateFlow<String> = _activeRegister

    private val _registers = MutableStateFlow<List<org.solovyev.android.calculator.memory.MemoryRegisterState>>(emptyList())
    override val registers: StateFlow<List<org.solovyev.android.calculator.memory.MemoryRegisterState>> = _registers

    override suspend fun store(value: String) {
        // No-op for tests
    }

    override fun recall() {
        // No-op for tests
    }

    override suspend fun clear() {
        // No-op for tests
    }

    override suspend fun add(value: String) {
        // No-op for tests
    }

    override suspend fun subtract(value: String) {
        // No-op for tests
    }

    override suspend fun setActiveRegister(name: String) {
        _activeRegister.value = name
    }
}

class FakeNotifier : Notifier {
    override val events: kotlinx.coroutines.flow.Flow<CalculatorMessage>
        get() = kotlinx.coroutines.flow.emptyFlow()

    override fun showMessage(message: CalculatorMessage) {
        // No-op for tests
    }

    override fun showMessage(message: String) {
        // No-op for tests
    }

    override fun showMessage(messageResId: Int) {
        // No-op for tests
    }
}

class FakeAppPreferences : AppPreferences {
    override val gui: org.solovyev.android.calculator.GuiPreferences
        get() = FakeGuiPreferences()
    override val settings: org.solovyev.android.calculator.SettingsPreferences
        get() = FakeSettingsPreferences()
}

class FakeGuiPreferences : org.solovyev.android.calculator.GuiPreferences {
    override val latexMode: StateFlow<Boolean> = MutableStateFlow(false)
    override val theme: StateFlow<Int> = MutableStateFlow(0)
    override val mode: StateFlow<Int> = MutableStateFlow(0)
    override val showCalculationLatency: StateFlow<Boolean> = MutableStateFlow(false)
    override val isSmallKeyboardMode: StateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun setLatexMode(enabled: Boolean) {}
    override suspend fun setTheme(themeId: Int) {}
    override suspend fun setMode(modeId: Int) {}
    override suspend fun setShowCalculationLatency(show: Boolean) {}
    override suspend fun setSmallKeyboardMode(small: Boolean) {}
}

class FakeSettingsPreferences : org.solovyev.android.calculator.SettingsPreferences {
    override val calculateOnFly: StateFlow<Boolean> = MutableStateFlow(true)
    override val tapeMode: StateFlow<Boolean> = MutableStateFlow(false)
    override val rpnMode: StateFlow<Boolean> = MutableStateFlow(false)
    override val numeralBase: StateFlow<Int> = MutableStateFlow(0)
    override val bitwiseWordSize: StateFlow<Int> = MutableStateFlow(64)
    override val bitwiseSigned: StateFlow<Boolean> = MutableStateFlow(true)

    override suspend fun setCalculateOnFly(enabled: Boolean) {}
    override suspend fun setTapeMode(enabled: Boolean) {}
    override suspend fun setRpnMode(enabled: Boolean) {}
    override suspend fun setAngleUnit(unitOrdinal: Int) {}
    override suspend fun setNumeralBase(baseOrdinal: Int) {}
    override suspend fun setBitwiseWordSize(size: Int) {}
    override suspend fun setBitwiseSigned(signed: Boolean) {}
}

private fun String.insert(index: Int, text: String): String {
    return this.substring(0, index) + text + this.substring(index)
}

/**
 * Test actions for Settings
 */
class FakeSettingsActions : SettingsActions {
    val recordedActions = mutableListOf<String>()

    override fun setMode(mode: org.solovyev.android.calculator.ui.settings.CalculatorMode) {
        recordedActions.add("setMode($mode)")
    }

    override fun setAngleUnit(unit: org.solovyev.android.calculator.ui.settings.AngleUnit) {
        recordedActions.add("setAngleUnit($unit)")
    }

    override fun setNumeralBase(base: org.solovyev.android.calculator.ui.settings.NumeralBase) {
        recordedActions.add("setNumeralBase($base)")
    }

    override fun setOutputNotation(notation: org.solovyev.android.calculator.ui.settings.OutputNotation) {
        recordedActions.add("setOutputNotation($notation)")
    }

    override fun setOutputPrecision(precision: Int) {
        recordedActions.add("setOutputPrecision($precision)")
    }

    override fun setOutputSeparator(separator: Char) {
        recordedActions.add("setOutputSeparator($separator)")
    }

    override fun setAppearanceMode(mode: org.solovyev.android.calculator.ui.settings.AppearanceMode) {
        recordedActions.add("setAppearanceMode($mode)")
    }

    override fun setTheme(theme: org.solovyev.android.calculator.ui.settings.AppTheme) {
        recordedActions.add("setTheme($theme)")
    }

    override fun setDynamicColor(enabled: Boolean) {
        recordedActions.add("setDynamicColor($enabled)")
    }

    override fun setThemeSeedColor(color: Int) {
        recordedActions.add("setThemeSeedColor($color)")
    }

    override fun setIsAmoledTheme(enabled: Boolean) {
        recordedActions.add("setIsAmoledTheme($enabled)")
    }

    override fun setLanguage(code: String) {
        recordedActions.add("setLanguage($code)")
    }

    override fun setVibrateOnKeypress(enabled: Boolean) {
        recordedActions.add("setVibrateOnKeypress($enabled)")
    }

    override fun setHighContrast(enabled: Boolean) {
        recordedActions.add("setHighContrast($enabled)")
    }

    override fun setHighlightExpressions(enabled: Boolean) {
        recordedActions.add("setHighlightExpressions($enabled)")
    }

    override fun setRotateScreen(enabled: Boolean) {
        recordedActions.add("setRotateScreen($enabled)")
    }

    override fun setKeepScreenOn(enabled: Boolean) {
        recordedActions.add("setKeepScreenOn($enabled)")
    }

    override fun setWidgetTheme(theme: org.solovyev.android.calculator.ui.settings.SimpleTheme) {
        recordedActions.add("setWidgetTheme($theme)")
    }

    override fun setCalculateOnFly(enabled: Boolean) {
        recordedActions.add("setCalculateOnFly($enabled)")
    }

    override fun setRpnMode(enabled: Boolean) {
        recordedActions.add("setRpnMode($enabled)")
    }

    override fun setTapeMode(enabled: Boolean) {
        recordedActions.add("setTapeMode($enabled)")
    }

    override fun setShowReleaseNotes(enabled: Boolean) {
        recordedActions.add("setShowReleaseNotes($enabled)")
    }

    override fun setShowCalculationLatency(enabled: Boolean) {
        recordedActions.add("setShowCalculationLatency($enabled)")
    }

    override fun setUseBackAsPrevious(enabled: Boolean) {
        recordedActions.add("setUseBackAsPrevious($enabled)")
    }

    override fun setPlotImag(enabled: Boolean) {
        recordedActions.add("setPlotImag($enabled)")
    }

    override fun setLatexMode(enabled: Boolean) {
        recordedActions.add("setLatexMode($enabled)")
    }

    override fun setReduceMotion(enabled: Boolean) {
        recordedActions.add("setReduceMotion($enabled)")
    }

    override fun setExtendedHaptics(enabled: Boolean) {
        recordedActions.add("setExtendedHaptics($enabled)")
    }

    override fun setFontScale(scale: Float) {
        recordedActions.add("setFontScale($scale)")
    }

    override fun setHintsEnabled(enabled: Boolean) {
        recordedActions.add("setHintsEnabled($enabled)")
    }

    override fun setHintsDisabledPermanently(disabled: Boolean) {
        recordedActions.add("setHintsDisabledPermanently($disabled)")
    }
}

/**
 * Assert helpers for common test patterns
 */
fun assertActionRecorded(actions: List<String>, expected: String) {
    assertTrue(
        actions.any { it.contains(expected) },
        "Expected action containing '$expected' but found: $actions"
    )
}

fun assertNoActionRecorded(actions: List<String>, unexpected: String) {
    assertTrue(
        actions.none { it.contains(unexpected) },
        "Did not expect action containing '$unexpected' but found: $actions"
    )
}
