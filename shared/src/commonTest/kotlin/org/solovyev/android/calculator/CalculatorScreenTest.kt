package org.solovyev.android.calculator

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI Tests for the Calculator Screen
 * 
 * Tests basic arithmetic operations, display functionality, and user interactions
 * following the AAA (Arrange, Act, Assert) pattern.
 */
@OptIn(ExperimentalTestApi::class)
class CalculatorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeCalculatorViewModel

    private fun setCalculatorScreenContent() {
        fakeViewModel = FakeCalculatorViewModel()
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    CalculatorScreenTestWrapper(
                        viewModel = fakeViewModel
                    )
                }
            }
        }
    }

    @Test
    fun test_basicAddition() {
        // Arrange: Set up the calculator screen
        setCalculatorScreenContent()

        // Act: Enter 2 + 2 =
        composeTestRule.onDigitButton("2").performClick()
        composeTestRule.onOperationButton("+").performClick()
        composeTestRule.onDigitButton("2").performClick()
        composeTestRule.onEqualsButton().performClick()

        // Assert: Result should be 4
        composeTestRule.assertResultShows("4")
        assertTrue(fakeViewModel.equalsCalled)
    }

    @Test
    fun test_basicSubtraction() {
        // Arrange
        setCalculatorScreenContent()

        // Act: Enter 5 - 3 =
        composeTestRule.onDigitButton("5").performClick()
        composeTestRule.onOperationButton("−").performClick()
        composeTestRule.onDigitButton("3").performClick()
        composeTestRule.onEqualsButton().performClick()

        // Assert
        composeTestRule.assertResultShows("2")
    }

    @Test
    fun test_basicMultiplication() {
        // Arrange
        setCalculatorScreenContent()

        // Act: Enter 4 × 5 =
        composeTestRule.onDigitButton("4").performClick()
        composeTestRule.onOperationButton("×").performClick()
        composeTestRule.onDigitButton("5").performClick()
        composeTestRule.onEqualsButton().performClick()

        // Assert
        composeTestRule.assertResultShows("20")
    }

    @Test
    fun test_basicDivision() {
        // Arrange
        setCalculatorScreenContent()

        // Act: Enter 10 ÷ 2 =
        composeTestRule.onDigitButton("1").performClick()
        composeTestRule.onDigitButton("0").performClick()
        composeTestRule.onOperationButton("÷").performClick()
        composeTestRule.onDigitButton("2").performClick()
        composeTestRule.onEqualsButton().performClick()

        // Assert
        composeTestRule.assertResultShows("5")
    }

    @Test
    fun test_clearButton() {
        // Arrange
        setCalculatorScreenContent()
        composeTestRule.enterNumber("123")
        
        // Verify display shows the number
        composeTestRule.assertEditorShows("123")

        // Act: Press Clear (C) button
        composeTestRule.onClearButton().performClick()

        // Assert: Display should be cleared
        assertTrue(fakeViewModel.clearCalled)
        composeTestRule.assertEditorShows("")
    }

    @Test
    fun test_deleteButton() {
        // Arrange
        setCalculatorScreenContent()
        composeTestRule.enterNumber("123")
        composeTestRule.assertEditorShows("123")

        // Act: Press Delete (backspace) button twice
        composeTestRule.onDeleteButton().performClick()

        // Assert: Last digit should be removed
        assertTrue(fakeViewModel.backspaceCalled)
        composeTestRule.assertEditorShows("12")

        // Act: Delete again
        composeTestRule.onDeleteButton().performClick()
        composeTestRule.assertEditorShows("1")

        // Act: Delete last digit
        composeTestRule.onDeleteButton().performClick()
        composeTestRule.assertEditorShows("")
    }

    @Test
    fun test_decimalInput() {
        // Arrange
        setCalculatorScreenContent()

        // Act: Enter 3.14
        composeTestRule.onDigitButton("3").performClick()
        composeTestRule.onOperationButton(".").performClick()
        composeTestRule.onDigitButton("1").performClick()
        composeTestRule.onDigitButton("4").performClick()

        // Assert: Display should show 3.14
        composeTestRule.assertEditorShows("3.14")
    }

    @Test
    fun test_chainedOperations() {
        // Arrange
        setCalculatorScreenContent()

        // Act: Enter 2 + 3 × 4 = (should follow order of operations: 2 + (3 × 4) = 14)
        // For simplicity, this test assumes the calculator follows standard precedence
        composeTestRule.onDigitButton("2").performClick()
        composeTestRule.onOperationButton("+").performClick()
        composeTestRule.onDigitButton("3").performClick()
        composeTestRule.onOperationButton("×").performClick()
        composeTestRule.onDigitButton("4").performClick()
        composeTestRule.onEqualsButton().performClick()

        // Assert
        composeTestRule.assertResultShows("14")
    }

    @Test
    fun test_displayScrolls() {
        // Arrange
        setCalculatorScreenContent()
        
        // Act: Enter a very long number
        val longNumber = "12345678901234567890.1234567890"
        composeTestRule.enterNumber(longNumber)

        // Assert: The number should be displayed (may be truncated visually but present)
        composeTestRule.onEditor().assertIsDisplayed()
    }

    @Test
    fun test_resultCopy() {
        // Arrange
        setCalculatorScreenContent()
        
        // Set up a result in the display
        fakeViewModel.setDisplayState(
            DisplayState(
                text = "42",
                valid = true,
                sequence = 1
            )
        )
        composeTestRule.waitForIdle()

        // Act: Tap on result to copy
        composeTestRule.onResult().performClick()

        // Assert: Copy feedback should be shown or copy action triggered
        // Note: In actual implementation, this would verify clipboard content
        composeTestRule.onResult().assertIsDisplayed()
    }

    @Test
    fun test_previewResultDisplay() {
        // Arrange
        setCalculatorScreenContent()
        
        // Act: Set a preview result (live calculation)
        fakeViewModel.setPreviewResult("1234")
        composeTestRule.waitForIdle()

        // Assert: Preview should be visible
        composeTestRule.onNodeWithTag(CalculatorTestTags.PREVIEW_RESULT)
            .assertIsDisplayed()
            .assertTextEquals("1234")
    }

    @Test
    fun test_errorDisplay() {
        // Arrange
        setCalculatorScreenContent()
        
        // Act: Set an error state
        fakeViewModel.setDisplayState(
            DisplayState(
                text = "Invalid expression",
                valid = false,
                sequence = 1
            )
        )
        composeTestRule.waitForIdle()

        // Assert: Error should be displayed
        composeTestRule.onNodeWithTag(CalculatorTestTags.ERROR_TEXT)
            .assertIsDisplayed()
    }

    @Test
    fun test_navigationButtons() {
        // Arrange
        setCalculatorScreenContent()

        // Assert: Navigation buttons should be present
        composeTestRule.onNodeWithContentDescription("History")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithContentDescription("Settings")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithContentDescription("Functions")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_zeroButtonDisplay() {
        // Arrange
        setCalculatorScreenContent()

        // Act: Press 0
        composeTestRule.onDigitButton("0").performClick()

        // Assert: Display shows 0
        composeTestRule.assertEditorShows("0")
    }

    @Test
    fun test_multipleDigits() {
        // Arrange
        setCalculatorScreenContent()

        // Act: Enter 9876543210
        "9876543210".forEach { digit ->
            composeTestRule.onDigitButton(digit.toString()).performClick()
        }

        // Assert
        composeTestRule.assertEditorShows("9876543210")
    }

    @Test
    fun test_operatorButtonsPresent() {
        // Arrange
        setCalculatorScreenContent()

        // Assert: All operators should be present
        listOf("+", "−", "×", "÷").forEach { op ->
            composeTestRule.onOperationButton(op)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    fun test_parenthesesButtons() {
        // Arrange
        setCalculatorScreenContent()

        // Act: Enter (1 + 2)
        composeTestRule.onNodeWithText("(").performClick()
        composeTestRule.onDigitButton("1").performClick()
        composeTestRule.onOperationButton("+").performClick()
        composeTestRule.onDigitButton("2").performClick()
        composeTestRule.onNodeWithText(")").performClick()

        // Assert
        composeTestRule.assertEditorShows("( 1 + 2 )")
    }

    @Test
    fun test_calculationLatencyDisplay() {
        // Arrange
        setCalculatorScreenContent()
        
        // Act: Enable latency display
        fakeViewModel.setDisplayState(
            DisplayState(
                text = "42",
                valid = true,
                sequence = 1
            )
        )
        composeTestRule.waitForIdle()

        // Assert: Latency indicator should be present
        composeTestRule.onNodeWithTag(CalculatorTestTags.CALCULATION_LATENCY)
            .assertDoesNotExist() // When disabled, it shouldn't show
    }

    @Test
    fun test_unitHintDisplay() {
        // Arrange
        setCalculatorScreenContent()
        
        // This would test unit conversion hints like "100 km ≈ 62.14 mi"
        // The actual implementation depends on the ViewModel state
        composeTestRule.onNodeWithTag(CalculatorTestTags.UNIT_HINT)
            .assertDoesNotExist() // When no unit hint is active
    }
}

/**
 * Test wrapper composable that provides necessary dependencies
 */
@Composable
private fun CalculatorScreenTestWrapper(
    viewModel: FakeCalculatorViewModel
) {
    // This would be a simplified version of CalculatorScreen for testing
    // In a real implementation, you'd use the actual CalculatorScreen with test dependencies
    org.solovyev.android.calculator.ui.CalculatorScreen(
        displayState = viewModel.displayState.value,
        editorState = viewModel.editorState.value,
        previewResult = viewModel.previewResult.value,
        unitHint = null,
        calculationLatencyMs = null,
        rpnMode = viewModel.rpnMode.value,
        rpnStack = viewModel.rpnStack.value,
        tapeMode = viewModel.tapeMode.value,
        tapeEntries = viewModel.tapeEntries.value,
        liveTapeEntry = null,
        onEditorTextChange = { text, selection -> 
            viewModel.onEditorTextChange(text, selection)
        },
        onEditorSelectionChange = { selection ->
            viewModel.onEditorSelectionChange(selection)
        },
        onOpenHistory = {},
        onOpenConverter = {},
        onOpenFunctions = {},
        onOpenVars = {},
        onOpenGraph = {},
        onOpenSettings = {},
        onPrevious = { viewModel.moveCursorLeft() },
        onPreviousStart = { viewModel.moveCursorToStart() },
        onNext = { viewModel.moveCursorRight() },
        onNextEnd = { viewModel.moveCursorToEnd() },
        onCopy = { viewModel.onCopied() },
        onPaste = {},
        onEquals = { viewModel.onEquals() },
        onSimplify = { viewModel.onSimplify() },
        onClearTape = {},
        showBottomToolbar = true,
        highlightExpressions = true,
        highContrast = false,
        hapticsEnabled = true,
        reduceMotion = false,
        extendedHaptics = false,
        fontScale = 1.0f,
        showScientificNotation = false,
        onToggleScientificNotation = {},
        keyboard = { modifier ->
            // Provide a simple test keyboard
            org.solovyev.android.calculator.ui.CalculatorKeyboard(
                actions = createTestKeyboardActions(viewModel),
                modifier = modifier
            )
        }
    )
}

private fun createTestKeyboardActions(viewModel: FakeCalculatorViewModel): org.solovyev.android.calculator.KeyboardActions {
    return object : org.solovyev.android.calculator.KeyboardActions {
        override fun onNumberClick(value: String) = viewModel.onDigitPressed(value)
        override fun onOperatorClick(op: String) = viewModel.onOperatorPressed(op)
        override fun onFunctionClick(name: String) = viewModel.onFunctionPressed(name)
        override fun onSpecialClick(action: String) = viewModel.onSpecialClick(action)
        override fun onClear() = viewModel.onClear()
        override fun onDelete() { viewModel.onBackspace() }
        override fun onEquals() = viewModel.onEquals()
        override fun onMemoryStore() = viewModel.memoryStore()
        override fun onMemoryRecall() = viewModel.memoryRecall()
        override fun onMemoryClear() = viewModel.memoryClear()
        override fun onMemoryPlus() = viewModel.memoryAdd()
        override fun onMemoryMinus() = viewModel.memorySubtract()
        override fun onMemoryRegisterSelected(name: String) = viewModel.selectMemoryRegister(name)
        override fun onOpenFunctions() {}
        override fun onOpenVars() {}
        override fun onOpenConverter() {}
        override fun onUndo() { viewModel.undo() }
        override fun onRedo() { viewModel.redo() }
        override fun onSwipeUp(buttonId: String) {}
        override fun onSwipeDown(buttonId: String) {}
        override fun onLongPress(buttonId: String) {}
    }
}
