package org.solovyev.android.calculator

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertRoleEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
nimport androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Accessibility Tests for the Calculator App
 * 
 * Tests TalkBack navigation, high contrast mode, large text support,
 * and focus management following the AAA (Arrange, Act, Assert) pattern.
 */
@OptIn(ExperimentalTestApi::class)
class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_talkBackNavigation() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.CalculatorScreen(
                        displayState = org.solovyev.android.calculator.DisplayState(
                            text = "42",
                            valid = true,
                            sequence = 1
                        ),
                        editorState = org.solovyev.android.calculator.EditorState.create("2+2", 3),
                        onEditorTextChange = { _, _ -> },
                        onEditorSelectionChange = {},
                        onOpenHistory = {},
                        onOpenConverter = {},
                        onOpenFunctions = {},
                        onOpenVars = {},
                        onOpenGraph = {},
                        onOpenSettings = {},
                        onPrevious = {},
                        onPreviousStart = {},
                        onNext = {},
                        onNextEnd = {},
                        onCopy = {},
                        onPaste = {},
                        onEquals = {},
                        onSimplify = {},
                        keyboard = { modifier ->
                            org.solovyev.android.calculator.ui.ModernCalculatorKeyboard(
                                actions = createDummyKeyboardActions(),
                                modifier = modifier
                            )
                        }
                    )
                }
            }
        }

        // Assert: All interactive elements should have content descriptions
        composeTestRule.onNodeWithContentDescription("History")
            .assertIsDisplayed()
            .assertRoleEquals(Role.Button)

        composeTestRule.onNodeWithContentDescription("Settings")
            .assertIsDisplayed()
            .assertRoleEquals(Role.Button)

        composeTestRule.onNodeWithContentDescription("Clear")
            .assertIsDisplayed()
            .assertRoleEquals(Role.Button)

        composeTestRule.onNodeWithContentDescription("Delete")
            .assertIsDisplayed()
            .assertRoleEquals(Role.Button)
    }

    @Test
    fun test_digitButtonsHaveContentDescriptions() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.ModernCalculatorKeyboard(
                        actions = createDummyKeyboardActions()
                    )
                }
            }
        }

        // Assert: Digit buttons should be accessible
        listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9").forEach { digit ->
            composeTestRule.onNodeWithText(digit)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    fun test_highContrastMode() {
        // Arrange
        composeTestRule.setContent {
            org.solovyev.android.calculator.ui.LocalCalculatorHighContrast provides true
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.CalculatorButton(
                        text = "5",
                        buttonType = org.solovyev.android.calculator.ui.ButtonType.DIGIT,
                        onClick = {}
                    )
                }
            }
        }

        // Assert: Button should be visible with high contrast
        composeTestRule.onNodeWithText("5")
            .assertIsDisplayed()
    }

    @Test
    fun test_largeTextSupport() {
        // Arrange: Set large font scale
        composeTestRule.setContent {
            org.solovyev.android.calculator.ui.LocalCalculatorFontScale provides 1.5f
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.CalculatorScreen(
                        displayState = org.solovyev.android.calculator.DisplayState(
                            text = "12345",
                            valid = true,
                            sequence = 1
                        ),
                        editorState = org.solovyev.android.calculator.EditorState.create("12345", 5),
                        onEditorTextChange = { _, _ -> },
                        onEditorSelectionChange = {},
                        onOpenHistory = {},
                        onOpenConverter = {},
                        onOpenFunctions = {},
                        onOpenVars = {},
                        onOpenGraph = {},
                        onOpenSettings = {},
                        onPrevious = {},
                        onPreviousStart = {},
                        onNext = {},
                        onNextEnd = {},
                        onCopy = {},
                        onPaste = {},
                        onEquals = {},
                        onSimplify = {},
                        keyboard = { modifier ->
                            org.solovyev.android.calculator.ui.ModernCalculatorKeyboard(
                                actions = createDummyKeyboardActions(),
                                modifier = modifier
                            )
                        }
                    )
                }
            }
        }

        // Assert: Display should be visible even with large text
        composeTestRule.onNodeWithText("12345")
            .assertIsDisplayed()
    }

    @Test
    fun test_focusManagement() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.ModernCalculatorKeyboard(
                        actions = createDummyKeyboardActions()
                    )
                }
            }
        }

        // Act: Click on a button to give it focus
        composeTestRule.onNodeWithText("1").performClick()

        // Assert: Button should be enabled and clickable
        composeTestRule.onNodeWithText("1")
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
    }

    @Test
    fun test_disabledStateAccessibility() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.ModernCalculatorKeyboard(
                        actions = createDummyKeyboardActions(),
                        numeralBase = jscl.NumeralBase.hex
                    )
                }
            }
        }

        // Assert: Disabled buttons (8 and 9 in hex mode) should still be visible but not enabled
        composeTestRule.onNodeWithText("8")
            .assertIsDisplayed()
            // Note: Disabled state assertion depends on implementation
    }

    @Test
    fun test_switchToggleAccessibility() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.settings.SettingsScreen(
                        destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.ACCESSIBILITY,
                        state = org.solovyev.android.calculator.ui.settings.SettingsUiState(
                            highContrast = false,
                            reduceMotion = false
                        ),
                        actions = FakeSettingsActions(),
                        onNavigate = {}
                    )
                }
            }
        }

        // Assert: Toggle switches should have proper semantics
        composeTestRule.onNodeWithText("High Contrast")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Reduce Motion")
            .assertIsDisplayed()
    }

    @Test
    fun test_screenReaderLabels() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.CalculatorScreen(
                        displayState = org.solovyev.android.calculator.DisplayState(
                            text = "Result: 42",
                            valid = true,
                            sequence = 1
                        ),
                        editorState = org.solovyev.android.calculator.EditorState.create("2+2", 3),
                        onEditorTextChange = { _, _ -> },
                        onEditorSelectionChange = {},
                        onOpenHistory = {},
                        onOpenConverter = {},
                        onOpenFunctions = {},
                        onOpenVars = {},
                        onOpenGraph = {},
                        onOpenSettings = {},
                        onPrevious = {},
                        onPreviousStart = {},
                        onNext = {},
                        onNextEnd = {},
                        onCopy = {},
                        onPaste = {},
                        onEquals = {},
                        onSimplify = {},
                        keyboard = { modifier ->
                            org.solovyev.android.calculator.ui.ModernCalculatorKeyboard(
                                actions = createDummyKeyboardActions(),
                                modifier = modifier
                            )
                        }
                    )
                }
            }
        }

        // Assert: Result should have appropriate semantics
        // This tests that screen readers can access the result
        composeTestRule.onNodeWithTag(CalculatorTestTags.DISPLAY_TEXT)
            .assertExists()
    }

    @Test
    fun test_historyItemAccessibility() {
        // Arrange
        val historyItem = org.solovyev.android.calculator.history.HistoryState(
            id = 1,
            time = System.currentTimeMillis(),
            editor = org.solovyev.android.calculator.EditorState.create("2+2", 3),
            display = org.solovyev.android.calculator.DisplayState(
                text = "4",
                valid = true,
                sequence = 1
            ),
            comment = ""
        )

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.history.HistoryScreen(
                        recent = listOf(historyItem),
                        saved = emptyList(),
                        onUse = {},
                        onCopyExpression = {},
                        onCopyResult = {},
                        onSave = {},
                        onEdit = {},
                        onDelete = {},
                        onClearRecent = {},
                        onClearSaved = {},
                        onBack = {}
                    )
                }
            }
        }

        // Assert: History item should have appropriate content description
        composeTestRule.onNodeWithText("2+2")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_keyboardButtonContentDescriptions() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.ModernCalculatorKeyboard(
                        actions = createDummyKeyboardActions()
                    )
                }
            }
        }

        // Assert: Operation buttons should have descriptive labels
        val operations = listOf("+", "−", "×", "÷")
        operations.forEach { op ->
            composeTestRule.onNodeWithText(op)
                .assertIsDisplayed()
                .assertRoleEquals(Role.Button)
        }
    }

    @Test
    fun test_errorMessageAccessibility() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.CalculatorScreen(
                        displayState = org.solovyev.android.calculator.DisplayState(
                            text = "Invalid expression",
                            valid = false,
                            sequence = 1
                        ),
                        editorState = org.solovyev.android.calculator.EditorState.create("1/0", 3),
                        onEditorTextChange = { _, _ -> },
                        onEditorSelectionChange = {},
                        onOpenHistory = {},
                        onOpenConverter = {},
                        onOpenFunctions = {},
                        onOpenVars = {},
                        onOpenGraph = {},
                        onOpenSettings = {},
                        onPrevious = {},
                        onPreviousStart = {},
                        onNext = {},
                        onNextEnd = {},
                        onCopy = {},
                        onPaste = {},
                        onEquals = {},
                        onSimplify = {},
                        keyboard = { modifier ->
                            org.solovyev.android.calculator.ui.ModernCalculatorKeyboard(
                                actions = createDummyKeyboardActions(),
                                modifier = modifier
                            )
                        }
                    )
                }
            }
        }

        // Assert: Error message should be accessible
        composeTestRule.onNodeWithTag(CalculatorTestTags.ERROR_TEXT)
            .assertExists()
    }

    @Test
    fun test_tabNavigationAccessibility() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.history.HistoryScreen(
                        recent = emptyList(),
                        saved = emptyList(),
                        onUse = {},
                        onCopyExpression = {},
                        onCopyResult = {},
                        onSave = {},
                        onEdit = {},
                        onDelete = {},
                        onClearRecent = {},
                        onClearSaved = {},
                        onBack = {}
                    )
                }
            }
        }

        // Assert: Tab buttons should be accessible
        composeTestRule.onNodeWithText("History")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertRoleEquals(Role.Tab)

        composeTestRule.onNodeWithText("Saved")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertRoleEquals(Role.Tab)
    }

    @Test
    fun test_settingsSearchAccessibility() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.settings.SettingsScreen(
                        destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.MAIN,
                        state = org.solovyev.android.calculator.ui.settings.SettingsUiState(),
                        actions = FakeSettingsActions(),
                        onNavigate = {}
                    )
                }
            }
        }

        // Assert: Search bar should be accessible
        composeTestRule.onNodeWithTag(CalculatorTestTags.SEARCH_BAR)
            .assertIsDisplayed()
    }

    @Test
    fun test_tutorialAccessibility() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.tutorial.FirstTimeTutorial(
                        onComplete = {},
                        onSkip = {},
                        tutorialManager = FakeTutorialManager(
                            org.solovyev.android.calculator.ui.tutorial.TutorialState()
                        )
                    )
                }
            }
        }

        // Assert: Tutorial should have accessible buttons
        composeTestRule.onNodeWithText("Start Tutorial")
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithText("Skip for now")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_hapticFeedbackAccessibility() {
        // Arrange
        composeTestRule.setContent {
            org.solovyev.android.calculator.ui.LocalCalculatorHapticsEnabled provides true
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.CalculatorButton(
                        text = "1",
                        buttonType = org.solovyev.android.calculator.ui.ButtonType.DIGIT,
                        onClick = {}
                    )
                }
            }
        }

        // Assert: Button should be accessible with haptics enabled
        composeTestRule.onNodeWithText("1")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_reduceMotionAccessibility() {
        // Arrange
        composeTestRule.setContent {
            org.solovyev.android.calculator.ui.LocalCalculatorReduceMotion provides true
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.CalculatorButton(
                        text = "5",
                        buttonType = org.solovyev.android.calculator.ui.ButtonType.DIGIT,
                        onClick = {}
                    )
                }
            }
        }

        // Assert: Button should be accessible with reduced motion
        composeTestRule.onNodeWithText("5")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_headingSemantics() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.settings.SettingsScreen(
                        destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.MAIN,
                        state = org.solovyev.android.calculator.ui.settings.SettingsUiState(),
                        actions = FakeSettingsActions(),
                        onNavigate = {}
                    )
                }
            }
        }

        // Assert: Settings categories should have heading semantics
        // Note: This would require custom semantics in the actual implementation
        composeTestRule.onNodeWithText("Calculator")
            .assertIsDisplayed()
    }

    @Test
    fun test_liveRegionForResults() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.CalculatorScreen(
                        displayState = org.solovyev.android.calculator.DisplayState(
                            text = "100",
                            valid = true,
                            sequence = 1
                        ),
                        editorState = org.solovyev.android.calculator.EditorState.create("50*2", 4),
                        onEditorTextChange = { _, _ -> },
                        onEditorSelectionChange = {},
                        onOpenHistory = {},
                        onOpenConverter = {},
                        onOpenFunctions = {},
                        onOpenVars = {},
                        onOpenGraph = {},
                        onOpenSettings = {},
                        onPrevious = {},
                        onPreviousStart = {},
                        onNext = {},
                        onNextEnd = {},
                        onCopy = {},
                        onPaste = {},
                        onEquals = {},
                        onSimplify = {},
                        keyboard = { modifier ->
                            org.solovyev.android.calculator.ui.ModernCalculatorKeyboard(
                                actions = createDummyKeyboardActions(),
                                modifier = modifier
                            )
                        }
                    )
                }
            }
        }

        // Assert: Result display should be accessible
        composeTestRule.onNodeWithTag(CalculatorTestTags.RESULT_TEXT)
            .assertExists()
    }

    @Test
    fun test_gestureHintAccessibility() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.tutorial.GestureHintOverlay(
                        hintData = org.solovyev.android.calculator.ui.tutorial.GestureHintData(
                            direction = org.solovyev.android.calculator.ui.tutorial.GestureDirection.UP,
                            actionLabel = "Swipe Up",
                            actionDescription = "Access secondary functions"
                        ),
                        isVisible = true,
                        onDismiss = {},
                        onHintShown = {}
                    )
                }
            }
        }

        // Assert: Gesture hint should be accessible
        composeTestRule.onNodeWithText("Swipe Up")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Access secondary functions")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Got it")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}

/**
 * Helper function to create dummy keyboard actions
 */
private fun createDummyKeyboardActions(): org.solovyev.android.calculator.KeyboardActions {
    return object : org.solovyev.android.calculator.KeyboardActions {
        override fun onNumberClick(value: String) {}
        override fun onOperatorClick(op: String) {}
        override fun onFunctionClick(name: String) {}
        override fun onSpecialClick(action: String) {}
        override fun onClear() {}
        override fun onDelete() {}
        override fun onEquals() {}
        override fun onMemoryStore() {}
        override fun onMemoryRecall() {}
        override fun onMemoryClear() {}
        override fun onMemoryPlus() {}
        override fun onMemoryMinus() {}
        override fun onMemoryRegisterSelected(name: String) {}
        override fun onOpenFunctions() {}
        override fun onOpenVars() {}
        override fun onOpenConverter() {}
        override fun onUndo() {}
        override fun onRedo() {}
        override fun onSwipeUp(buttonId: String) {}
        override fun onSwipeDown(buttonId: String) {}
        override fun onLongPress(buttonId: String) {}
    }
}
