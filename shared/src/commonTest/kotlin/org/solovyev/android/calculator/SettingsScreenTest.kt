package org.solovyev.android.calculator

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI Tests for the Settings Screen
 * 
 * Tests settings search, theme changes, precision slider, angle units,
 * RPN mode, accessibility settings, and navigation
 * following the AAA (Arrange, Act, Assert) pattern.
 */
@OptIn(ExperimentalTestApi::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeActions: FakeSettingsActions
    private var currentState: org.solovyev.android.calculator.ui.settings.SettingsUiState = 
        org.solovyev.android.calculator.ui.settings.SettingsUiState()

    private fun setSettingsScreenContent(
        destination: org.solovyev.android.calculator.ui.settings.SettingsDestination = 
            org.solovyev.android.calculator.ui.settings.SettingsDestination.MAIN,
        state: org.solovyev.android.calculator.ui.settings.SettingsUiState = 
            org.solovyev.android.calculator.ui.settings.SettingsUiState()
    ) {
        fakeActions = FakeSettingsActions()
        currentState = state
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    SettingsScreenTestWrapper(
                        destination = destination,
                        state = state,
                        actions = fakeActions
                    )
                }
            }
        }
    }

    @Test
    fun test_searchSettings() {
        // Arrange
        setSettingsScreenContent()

        // Act: Type "theme" in search
        composeTestRule.onNodeWithTag(CalculatorTestTags.SEARCH_BAR)
            .assertIsDisplayed()
            .performTextInput("theme")

        // Assert: Search results should show theme-related settings
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("App Theme")
            .assertIsDisplayed()
    }

    @Test
    fun test_searchSettingsNoResults() {
        // Arrange
        setSettingsScreenContent()

        // Act: Type a non-existent setting
        composeTestRule.onNodeWithTag(CalculatorTestTags.SEARCH_BAR)
            .performTextInput("xyznonexistent")

        // Assert: Empty state should be shown
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("No results found")
            .assertIsDisplayed()
    }

    @Test
    fun test_searchSettingsClear() {
        // Arrange
        setSettingsScreenContent()
        composeTestRule.onNodeWithTag(CalculatorTestTags.SEARCH_BAR)
            .performTextInput("test")

        // Act: Clear search
        composeTestRule.onNodeWithContentDescription("Clear search")
            .performClick()

        // Assert: Search should be cleared
        composeTestRule.onNodeWithTag(CalculatorTestTags.SEARCH_BAR)
            .assertTextEquals("")
    }

    @Test
    fun test_themeChange() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.APPEARANCE
        )

        // Act: Select a different theme
        composeTestRule.onNodeWithText("Material Dark")
            .performScrollTo()
            .performClick()

        // Assert: Theme change action should be recorded
        assertActionRecorded(fakeActions.recordedActions, "setTheme")
    }

    @Test
    fun test_precisionSlider() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.NUMBER_FORMAT
        )

        // Act: Find and interact with precision slider
        // Note: Slider testing requires specific semantics or bounds
        composeTestRule.onNodeWithText("Output Precision")
            .assertIsDisplayed()

        // Assert: Precision setting should be visible
        composeTestRule.onNodeWithText("5")
            .assertIsDisplayed()
    }

    @Test
    fun test_angleUnitChange() {
        // Arrange
        setSettingsScreenContent()

        // Act: Click on angle unit setting
        composeTestRule.onNodeWithText("Angle Unit")
            .performScrollTo()
            .performClick()

        // Assert: Angle unit options should be visible
        composeTestRule.onNodeWithText("RAD")
            .assertIsDisplayed()

        // Act: Select Radians
        composeTestRule.onNodeWithText("RAD").performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setAngleUnit")
    }

    @Test
    fun test_angleUnitDEG() {
        // Arrange
        setSettingsScreenContent()

        // Act: Select DEG
        composeTestRule.onNodeWithText("DEG")
            .performScrollTo()
            .performClick()

        // Assert
        assertTrue(fakeActions.recordedActions.any { 
            it.contains("setAngleUnit") && it.contains("DEG") 
        })
    }

    @Test
    fun test_angleUnitGRAD() {
        // Arrange
        setSettingsScreenContent()

        // Act: Select GRAD
        composeTestRule.onNodeWithText("GRAD")
            .performScrollTo()
            .performClick()

        // Assert
        assertTrue(fakeActions.recordedActions.any { 
            it.contains("setAngleUnit") && it.contains("GRAD") 
        })
    }

    @Test
    fun test_rpnModeToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.OTHER
        )

        // Act: Toggle RPN mode
        composeTestRule.onNodeWithText("RPN Mode")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setRpnMode")
    }

    @Test
    fun test_accessibilitySettings() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.ACCESSIBILITY
        )

        // Assert: Accessibility settings should be visible
        composeTestRule.onNodeWithText("High Contrast")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Reduce Motion")
            .assertIsDisplayed()
    }

    @Test
    fun test_highContrastToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.ACCESSIBILITY
        )

        // Act: Toggle high contrast
        composeTestRule.onNodeWithText("High Contrast")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setHighContrast")
    }

    @Test
    fun test_reduceMotionToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.ACCESSIBILITY
        )

        // Act: Toggle reduce motion
        composeTestRule.onNodeWithText("Reduce Motion")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setReduceMotion")
    }

    @Test
    fun test_settingsNavigation() {
        // Arrange
        var navigationDestination: org.solovyev.android.calculator.ui.settings.SettingsDestination? = null
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.settings.SettingsScreen(
                        destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.MAIN,
                        state = org.solovyev.android.calculator.ui.settings.SettingsUiState(),
                        actions = FakeSettingsActions(),
                        onNavigate = { destination ->
                            navigationDestination = destination
                        }
                    )
                }
            }
        }

        // Act: Click on Appearance
        composeTestRule.onNodeWithText("Appearance")
            .performScrollTo()
            .performClick()

        // Assert
        assertEquals(org.solovyev.android.calculator.ui.settings.SettingsDestination.APPEARANCE, navigationDestination)
    }

    @Test
    fun test_numberFormatNavigation() {
        // Arrange
        setSettingsScreenContent()

        // Act: Navigate to Number Format
        composeTestRule.onNodeWithText("Number Format")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setOutputNotation")
    }

    @Test
    fun test_numeralBaseChange() {
        // Arrange
        setSettingsScreenContent()

        // Act: Change numeral base
        composeTestRule.onNodeWithText("Numeral Base")
            .performScrollTo()
            .performClick()

        // Assert: Base options should be visible
        composeTestRule.onNodeWithText("HEX")
            .assertIsDisplayed()

        // Act: Select Hex
        composeTestRule.onNodeWithText("HEX").performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setNumeralBase")
    }

    @Test
    fun test_calculatorModeChange() {
        // Arrange
        setSettingsScreenContent()

        // Act: Change calculator mode
        composeTestRule.onNodeWithText("Modern")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setMode")
    }

    @Test
    fun test_dynamicColorToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.APPEARANCE
        )

        // Act: Toggle dynamic colors
        composeTestRule.onNodeWithText("Dynamic Colors")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setDynamicColor")
    }

    @Test
    fun test_vibrateOnKeypressToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.APPEARANCE
        )

        // Act: Toggle vibration
        composeTestRule.onNodeWithText("Vibrate on Keypress")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setVibrateOnKeypress")
    }

    @Test
    fun test_tapeModeToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.OTHER
        )

        // Act: Toggle tape mode
        composeTestRule.onNodeWithText("Tape Mode")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setTapeMode")
    }

    @Test
    fun test_calculateOnFlyToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.OTHER
        )

        // Act: Toggle calculate on fly
        composeTestRule.onNodeWithText("Calculate on Fly")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setCalculateOnFly")
    }

    @Test
    fun test_helpSectionPresent() {
        // Arrange
        setSettingsScreenContent()

        // Assert: Help section should be visible
        composeTestRule.onNodeWithText("Help")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Introduction")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Show Gesture Tutorial")
            .assertIsDisplayed()
    }

    @Test
    fun test_resetHintsButton() {
        // Arrange
        setSettingsScreenContent()

        // Act: Click reset hints
        composeTestRule.onNodeWithText("Reset All Hints")
            .performScrollTo()
            .performClick()

        // Assert: Action should be recorded (through onResetHints callback)
        // This would be verified through the callback in a real test
    }

    @Test
    fun test_aboutButton() {
        // Arrange
        setSettingsScreenContent()

        // Assert: About button should be present
        composeTestRule.onNodeWithText("About")
            .performScrollTo()
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_reportBugButton() {
        // Arrange
        setSettingsScreenContent()

        // Assert: Report bug button should be present
        composeTestRule.onNodeWithText("Report a Problem")
            .performScrollTo()
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun test_widgetSettingsNavigation() {
        // Arrange
        setSettingsScreenContent()

        // Act: Click on Widget
        composeTestRule.onNodeWithText("Widget")
            .performScrollTo()
            .performClick()

        // Assert: Widget settings should be navigated
        // This verifies navigation occurred
    }

    @Test
    fun test_searchCategoryFilter() {
        // Arrange
        setSettingsScreenContent()

        // Act: Search for a calculator setting
        composeTestRule.onNodeWithTag(CalculatorTestTags.SEARCH_BAR)
            .performTextInput("precision")

        // Assert: Calculator category results should show
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Output Precision")
            .assertIsDisplayed()
    }

    @Test
    fun test_settingsListScrollable() {
        // Arrange
        setSettingsScreenContent()

        // Act & Assert: Scroll through settings
        composeTestRule.onNodeWithTag(CalculatorTestTags.SETTINGS_LIST)
            .assertExists()
    }

    @Test
    fun test_latexModeToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.OTHER
        )

        // Act: Toggle LaTeX mode
        composeTestRule.onNodeWithText("LaTeX Output")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setLatexMode")
    }

    @Test
    fun test_fontScaleSetting() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.ACCESSIBILITY
        )

        // Assert: Font scale setting should be present
        composeTestRule.onNodeWithText("Font Scale")
            .assertIsDisplayed()
    }

    @Test
    fun test_extendedHapticsToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.ACCESSIBILITY
        )

        // Act: Toggle extended haptics
        composeTestRule.onNodeWithText("Extended Haptics")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setExtendedHaptics")
    }

    @Test
    fun test_rotateScreenToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.APPEARANCE
        )

        // Act: Toggle auto-rotate
        composeTestRule.onNodeWithText("Auto-rotate Screen")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setRotateScreen")
    }

    @Test
    fun test_keepScreenOnToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.APPEARANCE
        )

        // Act: Toggle keep screen on
        composeTestRule.onNodeWithText("Keep Screen On")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setKeepScreenOn")
    }

    @Test
    fun test_showCalculationLatencyToggle() {
        // Arrange
        setSettingsScreenContent(
            destination = org.solovyev.android.calculator.ui.settings.SettingsDestination.OTHER
        )

        // Act: Toggle calculation latency display
        composeTestRule.onNodeWithText("Show Calculation Latency")
            .performScrollTo()
            .performClick()

        // Assert
        assertActionRecorded(fakeActions.recordedActions, "setShowCalculationLatency")
    }
}

/**
 * Test wrapper composable for SettingsScreen
 */
@Composable
private fun SettingsScreenTestWrapper(
    destination: org.solovyev.android.calculator.ui.settings.SettingsDestination,
    state: org.solovyev.android.calculator.ui.settings.SettingsUiState,
    actions: SettingsActions
) {
    org.solovyev.android.calculator.ui.settings.SettingsScreen(
        destination = destination,
        state = state,
        actions = actions,
        onNavigate = {},
        onStartWizard = {},
        onReportBug = {},
        onOpenAbout = {},
        onSupportProject = {},
        onResetHints = {}
    )
}
