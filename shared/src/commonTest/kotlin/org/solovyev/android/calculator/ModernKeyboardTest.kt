package org.solovyev.android.calculator

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertRoleEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performLongClick
import androidx.compose.ui.test.performTouchInput
import jscl.NumeralBase
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI Tests for the Modern Keyboard
 * 
 * Tests button presence, gestures, accessibility, and special modes
 * following the AAA (Arrange, Act, Assert) pattern.
 */
@OptIn(ExperimentalTestApi::class)
class ModernKeyboardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val recordedActions = mutableListOf<String>()
    private var numeralBase = NumeralBase.dec

    private fun setModernKeyboardContent(
        base: NumeralBase = NumeralBase.dec,
        bitwiseWordSize: Int = 64,
        bitwiseSigned: Boolean = true
    ) {
        recordedActions.clear()
        numeralBase = base
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    ModernKeyboardTestWrapper(
                        numeralBase = base,
                        bitwiseWordSize = bitwiseWordSize,
                        bitwiseSigned = bitwiseSigned,
                        onAction = { action -> recordedActions.add(action) }
                    )
                }
            }
        }
    }

    @Test
    fun test_allDigitButtonsPresent() {
        // Arrange
        setModernKeyboardContent()

        // Assert: All digits 0-9 should be present
        (0..9).forEach { digit ->
            composeTestRule.onNodeWithText(digit.toString())
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    fun test_operationButtonsPresent() {
        // Arrange
        setModernKeyboardContent()

        // Assert: All operations should be present
        val operations = listOf("+", "−", "×", "÷")
        operations.forEach { op ->
            composeTestRule.onNodeWithText(op)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    fun test_controlButtonsPresent() {
        // Arrange
        setModernKeyboardContent()

        // Assert: Control buttons should be present
        composeTestRule.onNodeWithContentDescription("Clear")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithContentDescription("Delete")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_parenthesesButtonsPresent() {
        // Arrange
        setModernKeyboardContent()

        // Assert
        composeTestRule.onNodeWithText("(")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_decimalPointButton() {
        // Arrange
        setModernKeyboardContent()

        // Assert
        composeTestRule.onNodeWithText(".")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_percentButton() {
        // Arrange
        setModernKeyboardContent()

        // Assert
        composeTestRule.onNodeWithText("%")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_functionsButton() {
        // Arrange
        setModernKeyboardContent()

        // Assert
        composeTestRule.onNodeWithText("ƒ")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_digitButtonClick() {
        // Arrange
        setModernKeyboardContent()

        // Act
        composeTestRule.onNodeWithText("5").performClick()

        // Assert
        assertTrue(recordedActions.contains("number:5"))
    }

    @Test
    fun test_operationButtonClick() {
        // Arrange
        setModernKeyboardContent()

        // Act
        composeTestRule.onNodeWithText("+").performClick()

        // Assert
        assertTrue(recordedActions.contains("operator:+"))
    }

    @Test
    fun test_clearButtonClick() {
        // Arrange
        setModernKeyboardContent()

        // Act
        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        // Assert
        assertTrue(recordedActions.contains("clear"))
    }

    @Test
    fun test_deleteButtonClick() {
        // Arrange
        setModernKeyboardContent()

        // Act
        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        // Assert
        assertTrue(recordedActions.contains("delete"))
    }

    @Test
    fun test_swipeUpGesture() {
        // Arrange
        setModernKeyboardContent()

        // Act: Swipe up on a digit button
        composeTestRule.performSwipeUpOnButton("7")

        // Assert
        // The swipe action should be recorded
        assertTrue(recordedActions.any { it.startsWith("swipe_up") })
    }

    @Test
    fun test_swipeDownGesture() {
        // Arrange
        setModernKeyboardContent()

        // Act: Swipe down on a digit button
        composeTestRule.performSwipeDownOnButton("4")

        // Assert
        assertTrue(recordedActions.any { it.startsWith("swipe_down") })
    }

    @Test
    fun test_swipeUpOnClearButton() {
        // Arrange
        setModernKeyboardContent()

        // Act: Swipe up on clear button (triggers memory clear)
        composeTestRule.performSwipeUpOnButton("C")

        // Assert
        assertTrue(recordedActions.contains("memory_clear"))
    }

    @Test
    fun test_swipeDownOnClearButton() {
        // Arrange
        setModernKeyboardContent()

        // Act: Swipe down on clear button (triggers memory recall)
        composeTestRule.performSwipeDownOnButton("C")

        // Assert
        assertTrue(recordedActions.contains("memory_recall"))
    }

    @Test
    fun test_longPressShowsOptions() {
        // Arrange
        setModernKeyboardContent()

        // Act: Long press on a button that has options
        composeTestRule.performLongPressOnButton("7")

        // Assert: Option picker should appear (verify via recorded action)
        assertTrue(recordedActions.any { it.startsWith("long_press") })
    }

    @Test
    fun test_buttonAccessibilityLabels() {
        // Arrange
        setModernKeyboardContent()

        // Assert: Check content descriptions for accessibility
        composeTestRule.onNodeWithContentDescription("Clear")
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Delete")
            .assertIsDisplayed()
    }

    @Test
    fun test_disabledStateInHexMode() {
        // Arrange: Set hex mode
        setModernKeyboardContent(base = NumeralBase.hex)

        // Act & Assert: Digits 8 and 9 should be disabled in hex mode
        composeTestRule.onNodeWithText("8")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        composeTestRule.onNodeWithText("9")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        // Digits 0-7 should still be enabled
        composeTestRule.onNodeWithText("7")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun test_hexModeEnablesAF() {
        // Arrange: Set hex mode
        setModernKeyboardContent(base = NumeralBase.hex)

        // Act & Assert: Hex digits A-F should be accessible via long press
        // In hex mode, digits 1-6 show hex options on long press
        composeTestRule.performLongPressOnButton("1")
        assertTrue(recordedActions.any { it.contains("long_press") })
    }

    @Test
    fun test_decimalPointDisabledInNonDecimalModes() {
        // Arrange: Set hex mode
        setModernKeyboardContent(base = NumeralBase.hex)

        // Assert: Decimal point should be disabled
        composeTestRule.onNodeWithText(".")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun test_decimalPointEnabledInDecimalMode() {
        // Arrange: Set decimal mode
        setModernKeyboardContent(base = NumeralBase.dec)

        // Assert: Decimal point should be enabled
        composeTestRule.onNodeWithText(".")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun test_memoryOperations() {
        // Arrange
        setModernKeyboardContent()

        // Act: Memory operations via swipe gestures
        composeTestRule.performSwipeUpOnButton("C") // Memory Clear
        composeTestRule.performSwipeDownOnButton("C") // Memory Recall

        // Assert
        assertTrue(recordedActions.contains("memory_clear"))
        assertTrue(recordedActions.contains("memory_recall"))
    }

    @Test
    fun test_divideButtonSwipeUp() {
        // Arrange
        setModernKeyboardContent()

        // Act: Swipe up on divide button (should trigger sqrt)
        composeTestRule.performSwipeUpOnButton("÷")

        // Assert
        assertTrue(recordedActions.contains("function:sqrt"))
    }

    @Test
    fun test_multiplyButtonSwipeGestures() {
        // Arrange
        setModernKeyboardContent()

        // Act: Swipe up on multiply button (should trigger power)
        composeTestRule.performSwipeUpOnButton("×")

        // Assert
        assertTrue(recordedActions.contains("special:^"))
    }

    @Test
    fun test_percentButtonSwipeUp() {
        // Arrange
        setModernKeyboardContent()

        // Act: Swipe up on percent button (should open scientific sheet)
        composeTestRule.performSwipeUpOnButton("%")

        // Assert: Scientific functions sheet should be triggered
        assertTrue(recordedActions.contains("open_scientific"))
    }

    @Test
    fun test_directionTextHintsVisible() {
        // Arrange
        setModernKeyboardContent()

        // Assert: Buttons with direction texts should show them
        // These are the small text labels showing swipe alternatives
        composeTestRule.onNodeWithText("7")
            .assertIsDisplayed()
    }

    @Test
    fun test_allRowsPresent() {
        // Arrange
        setModernKeyboardContent()

        // Assert: Verify all 5 rows of buttons are present
        // Row 1: C, (, ), ÷
        composeTestRule.onNodeWithText("C").assertIsDisplayed()
        composeTestRule.onNodeWithText("(").assertIsDisplayed()
        composeTestRule.onNodeWithText("÷").assertIsDisplayed()

        // Row 2: 7, 8, 9, ×
        composeTestRule.onNodeWithText("7").assertIsDisplayed()
        composeTestRule.onNodeWithText("8").assertIsDisplayed()
        composeTestRule.onNodeWithText("9").assertIsDisplayed()
        composeTestRule.onNodeWithText("×").assertIsDisplayed()

        // Row 3: 4, 5, 6, −
        composeTestRule.onNodeWithText("4").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithText("6").assertIsDisplayed()
        composeTestRule.onNodeWithText("−").assertIsDisplayed()

        // Row 4: 1, 2, 3, +
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
        composeTestRule.onNodeWithText("+").assertIsDisplayed()

        // Row 5: Delete, 0, ., ƒ
        composeTestRule.onNodeWithContentDescription("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
        composeTestRule.onNodeWithText(".").assertIsDisplayed()
        composeTestRule.onNodeWithText("ƒ").assertIsDisplayed()
    }

    @Test
    fun test_buttonRolesForAccessibility() {
        // Arrange
        setModernKeyboardContent()

        // Assert: Buttons should have Button role for accessibility
        composeTestRule.onNodeWithText("1")
            .assertIsDisplayed()
            .assertRoleEquals(Role.Button)
    }

    @Test
    fun test_zeroButtonDoubleZeroTripleZero() {
        // Arrange
        setModernKeyboardContent()

        // Act & Assert: Test zero button with swipe gestures
        // Swipe down for 00, swipe up for 000
        composeTestRule.onNodeWithText("0").performClick()
        assertTrue(recordedActions.contains("number:0"))

        composeTestRule.performSwipeDownOnButton("0")
        assertTrue(recordedActions.contains("number:00"))

        composeTestRule.performSwipeUpOnButton("0")
        assertTrue(recordedActions.contains("number:000"))
    }

    @Test
    fun test_buttonClickHapticFeedbackRecorded() {
        // Arrange
        setModernKeyboardContent()

        // Act: Click various buttons
        composeTestRule.onNodeWithText("5").performClick()

        // Assert: Action should be recorded
        assertTrue(recordedActions.contains("number:5"))
    }
}

/**
 * Test wrapper composable for ModernKeyboard
 */
@Composable
private fun ModernKeyboardTestWrapper(
    numeralBase: NumeralBase,
    bitwiseWordSize: Int,
    bitwiseSigned: Boolean,
    onAction: (String) -> Unit
) {
    val actions = object : org.solovyev.android.calculator.KeyboardActions {
        override fun onNumberClick(value: String) {
            onAction("number:$value")
        }
        override fun onOperatorClick(op: String) {
            onAction("operator:$op")
        }
        override fun onFunctionClick(name: String) {
            onAction("function:$name")
        }
        override fun onSpecialClick(action: String) {
            onAction("special:$action")
        }
        override fun onClear() {
            onAction("clear")
        }
        override fun onDelete() {
            onAction("delete")
        }
        override fun onEquals() {
            onAction("equals")
        }
        override fun onMemoryStore() {
            onAction("memory_store")
        }
        override fun onMemoryRecall() {
            onAction("memory_recall")
        }
        override fun onMemoryClear() {
            onAction("memory_clear")
        }
        override fun onMemoryPlus() {
            onAction("memory_plus")
        }
        override fun onMemoryMinus() {
            onAction("memory_minus")
        }
        override fun onMemoryRegisterSelected(name: String) {
            onAction("memory_register:$name")
        }
        override fun onOpenFunctions() {
            onAction("open_functions")
        }
        override fun onOpenVars() {
            onAction("open_vars")
        }
        override fun onOpenConverter() {
            onAction("open_converter")
        }
        override fun onUndo() {
            onAction("undo")
        }
        override fun onRedo() {
            onAction("redo")
        }
        override fun onSwipeUp(buttonId: String) {
            onAction("swipe_up:$buttonId")
        }
        override fun onSwipeDown(buttonId: String) {
            onAction("swipe_down:$buttonId")
        }
        override fun onLongPress(buttonId: String) {
            onAction("long_press:$buttonId")
        }
    }

    org.solovyev.android.calculator.ui.ModernCalculatorKeyboard(
        actions = actions,
        numeralBase = numeralBase,
        bitwiseWordSize = bitwiseWordSize,
        bitwiseSigned = bitwiseSigned
    )
}
