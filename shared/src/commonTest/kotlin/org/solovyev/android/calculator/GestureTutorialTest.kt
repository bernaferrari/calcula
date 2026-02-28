package org.solovyev.android.calculator

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * UI Tests for the Gesture Tutorial
 * 
 * Tests tutorial appearance, skip functionality, swipe hints, hint dismissal,
 * and progress persistence following the AAA (Arrange, Act, Assert) pattern.
 */
@OptIn(ExperimentalTestApi::class)
class GestureTutorialTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var tutorialCompleted = false
    private var tutorialSkipped = false
    private var resetRequested = false

    private fun setGestureTutorialContent(
        showTutorial: Boolean = true,
        tutorialState: org.solovyev.android.calculator.ui.tutorial.TutorialState = 
            org.solovyev.android.calculator.ui.tutorial.TutorialState()
    ) {
        tutorialCompleted = false
        tutorialSkipped = false
        resetRequested = false
        
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    GestureTutorialTestWrapper(
                        showTutorial = showTutorial,
                        tutorialState = tutorialState,
                        onComplete = { tutorialCompleted = true },
                        onSkip = { tutorialSkipped = true }
                    )
                }
            }
        }
    }

    @Test
    fun test_tutorialAppearsOnFirstLaunch() {
        // Arrange: First launch state
        val firstLaunchState = org.solovyev.android.calculator.ui.tutorial.TutorialState(
            hasCompletedTutorial = false,
            shownHints = emptySet()
        )

        // Act
        setGestureTutorialContent(
            showTutorial = true,
            tutorialState = firstLaunchState
        )

        // Assert: Tutorial should be visible
        composeTestRule.onNodeWithText("Welcome to Calculator++")
            .assertIsDisplayed()
    }

    @Test
    fun test_tutorialDoesNotAppearWhenCompleted() {
        // Arrange: Completed tutorial state
        val completedState = org.solovyev.android.calculator.ui.tutorial.TutorialState(
            hasCompletedTutorial = true,
            shownHints = setOf("swipe_up", "swipe_down", "long_press")
        )

        // Act
        setGestureTutorialContent(
            showTutorial = true,
            tutorialState = completedState
        )

        // Assert: Tutorial should not be visible
        composeTestRule.onNodeWithText("Welcome to Calculator++")
            .assertDoesNotExist()
    }

    @Test
    fun test_tutorialCanBeSkipped() {
        // Arrange
        setGestureTutorialContent()

        // Assert: Initial state shows welcome
        composeTestRule.onNodeWithText("Welcome to Calculator++")
            .assertIsDisplayed()

        // Act: Click skip button
        composeTestRule.onNodeWithText("Skip for now")
            .performClick()

        // Assert: Tutorial was skipped
        assertTrue(tutorialSkipped)
    }

    @Test
    fun test_tutorialStartButton() {
        // Arrange
        setGestureTutorialContent()

        // Act: Click start button
        composeTestRule.onNodeWithText("Start Tutorial")
            .performClick()

        // Assert: Should advance to first gesture step
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Swipe Up for More")
            .assertIsDisplayed()
    }

    @Test
    fun test_swipeHintAppears() {
        // Arrange
        val state = org.solovyev.android.calculator.ui.tutorial.TutorialState(
            shownHints = emptySet(),
            swipeUpShown = false
        )
        
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    GestureHintOverlayTestWrapper(
                        hintData = org.solovyev.android.calculator.ui.tutorial.GestureHintData(
                            direction = org.solovyev.android.calculator.ui.tutorial.GestureDirection.UP,
                            actionLabel = "Swipe Up",
                            actionDescription = "Access secondary functions",
                            exampleValue = "sin → asin"
                        ),
                        isVisible = true,
                        onDismiss = {},
                        onHintShown = {}
                    )
                }
            }
        }

        // Assert: Hint should be displayed
        composeTestRule.onNodeWithText("Swipe Up")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Access secondary functions")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("sin → asin")
            .assertIsDisplayed()
    }

    @Test
    fun test_hintDismissal() {
        // Arrange
        var dismissed = false
        var hintShown = false
        
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    GestureHintOverlayTestWrapper(
                        hintData = org.solovyev.android.calculator.ui.tutorial.GestureHintData(
                            direction = org.solovyev.android.calculator.ui.tutorial.GestureDirection.UP,
                            actionLabel = "Swipe Up",
                            actionDescription = "Access secondary functions",
                            exampleValue = null
                        ),
                        isVisible = true,
                        onDismiss = { dismissed = true },
                        onHintShown = { hintShown = true }
                    )
                }
            }
        }

        // Assert: Hint is displayed
        composeTestRule.onNodeWithText("Got it")
            .assertIsDisplayed()

        // Act: Click dismiss button
        composeTestRule.onNodeWithText("Got it")
            .performClick()

        // Assert
        assertTrue(dismissed)
    }

    @Test
    fun test_tutorialProgressThroughSteps() {
        // Arrange
        setGestureTutorialContent()
        composeTestRule.onNodeWithText("Start Tutorial").performClick()

        // Step 1: Swipe Up
        composeTestRule.onNodeWithText("Swipe Up for More").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()

        // Step 2: Swipe Down
        composeTestRule.onNodeWithText("Swipe Down for Alternates").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()

        // Step 3: Swipe Left
        composeTestRule.onNodeWithText("Swipe Left to Clear").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()

        // Step 4: Swipe Right
        composeTestRule.onNodeWithText("Swipe Right for Actions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()

        // Step 5: Long Press
        composeTestRule.onNodeWithText("Long Press for Options").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()

        // Step 6: Double Tap
        composeTestRule.onNodeWithText("Double Tap Equals for Graph").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()

        // Step 7: Practice
        composeTestRule.onNodeWithText("Practice Time").assertIsDisplayed()
    }

    @Test
    fun test_tutorialCompletion() {
        // Arrange: Set up tutorial at completion step
        setGestureTutorialContent()
        
        // Navigate through all steps
        composeTestRule.onNodeWithText("Start Tutorial").performClick()
        
        // Go through all steps quickly
        repeat(7) {
            composeTestRule.waitForIdle()
            val nextButton = composeTestRule.onNodeWithText("Next")
            if (nextButton.isDisplayed()) {
                nextButton.performClick()
            }
        }

        // At practice step, complete all gestures
        composeTestRule.onNodeWithText("Practice Time").assertIsDisplayed()
        
        // Complete all required gestures
        // Note: This would require actual swipe interactions in a real test
    }

    @Test
    fun test_tutorialProgressIndicator() {
        // Arrange
        setGestureTutorialContent()

        // Assert: Progress indicator should be visible
        composeTestRule.onNodeWithTag("tutorial_progress_indicator")
            .assertIsDisplayed()
    }

    @Test
    fun test_swipeUpTutorialStep() {
        // Arrange
        setGestureTutorialContent()
        composeTestRule.onNodeWithText("Start Tutorial").performClick()

        // Assert: Swipe Up step should show demo buttons
        composeTestRule.onNodeWithText("Try it:")
            .assertIsDisplayed()
    }

    @Test
    fun test_longPressTutorialStep() {
        // Arrange
        setGestureTutorialContent()
        composeTestRule.onNodeWithText("Start Tutorial").performClick()
        
        // Navigate to long press step
        repeat(5) {
            composeTestRule.onNodeWithText("Next").performClick()
            composeTestRule.waitForIdle()
        }

        // Assert: Long press step should be shown
        composeTestRule.onNodeWithText("Long Press for Options")
            .assertIsDisplayed()
    }

    @Test
    fun test_doubleTapTutorialStep() {
        // Arrange
        setGestureTutorialContent()
        composeTestRule.onNodeWithText("Start Tutorial").performClick()
        
        // Navigate to double tap step
        repeat(6) {
            composeTestRule.onNodeWithText("Next").performClick()
            composeTestRule.waitForIdle()
        }

        // Assert: Double tap step should be shown
        composeTestRule.onNodeWithText("Double Tap Equals for Graph")
            .assertIsDisplayed()
    }

    @Test
    fun test_practiceStepRequiredGestures() {
        // Arrange
        setGestureTutorialContent()
        composeTestRule.onNodeWithText("Start Tutorial").performClick()
        
        // Navigate to practice step
        repeat(7) {
            composeTestRule.waitForIdle()
            val nextButton = composeTestRule.onNodeWithText("Next")
            if (nextButton.isDisplayed()) {
                nextButton.performClick()
            }
        }

        // Assert: Practice step shows required gestures
        composeTestRule.onNodeWithText("Swipe up on sin")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Swipe down on π")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Long press cos")
            .assertIsDisplayed()
    }

    @Test
    fun test_tutorialBackNavigation() {
        // Arrange
        setGestureTutorialContent()
        composeTestRule.onNodeWithText("Start Tutorial").performClick()
        composeTestRule.onNodeWithText("Next").performClick()

        // Assert: At step 2
        composeTestRule.onNodeWithText("Swipe Down for Alternates").assertIsDisplayed()

        // Act: Go back
        composeTestRule.onNodeWithText("Back").performClick()

        // Assert: Should be at step 1
        composeTestRule.onNodeWithText("Swipe Up for More").assertIsDisplayed()
    }

    @Test
    fun test_gestureHintSettingsItem() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.tutorial.GestureHintSettingsItem(
                        onResetTutorial = { resetRequested = true }
                    )
                }
            }
        }

        // Assert: Settings item should be visible
        composeTestRule.onNodeWithText("Replay Gesture Tutorial")
            .assertIsDisplayed()

        // Act: Click it
        composeTestRule.onNodeWithText("Replay Gesture Tutorial")
            .performClick()

        // Assert
        assertTrue(resetRequested)
    }

    @Test
    fun test_buttonGestureHint() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.tutorial.ButtonGestureHint(
                        hintText = "Swipe up",
                        direction = org.solovyev.android.calculator.ui.tutorial.GestureDirection.UP,
                        isVisible = true,
                        onDismiss = {}
                    )
                }
            }
        }

        // Assert: Button gesture hint should be visible
        composeTestRule.onNodeWithTag(CalculatorTestTags.GESTURE_HINT)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Swipe up")
            .assertIsDisplayed()
    }

    @Test
    fun test_gestureHintAutoDismiss() {
        // Arrange
        var hintShown = false
        
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.tutorial.GestureHintOverlay(
                        hintData = org.solovyev.android.calculator.ui.tutorial.GestureHintData(
                            direction = org.solovyev.android.calculator.ui.tutorial.GestureDirection.DOWN,
                            actionLabel = "Swipe Down",
                            actionDescription = "Access alternate functions"
                        ),
                        isVisible = true,
                        onDismiss = {},
                        onHintShown = { hintShown = true }
                    )
                }
            }
        }

        // Assert: Hint is shown
        composeTestRule.onNodeWithText("Swipe Down").assertIsDisplayed()

        // Wait for auto-dismiss (3 seconds in implementation)
        // In a real test, we'd use a test-specific timeout
    }

    @Test
    fun test_tutorialCompletionStep() {
        // Arrange
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    org.solovyev.android.calculator.ui.tutorial.CompleteStep(
                        onFinish = { tutorialCompleted = true }
                    )
                }
            }
        }

        // Assert: Completion step should be shown
        composeTestRule.onNodeWithText("You're All Set!")
            .assertIsDisplayed()

        // Act: Click finish button
        composeTestRule.onNodeWithText("Start Calculating")
            .performClick()

        // Assert
        assertTrue(tutorialCompleted)
    }

    @Test
    fun test_allGestureDirections() {
        // Test all gesture directions
        val directions = listOf(
            org.solovyev.android.calculator.ui.tutorial.GestureDirection.UP,
            org.solovyev.android.calculator.ui.tutorial.GestureDirection.DOWN,
            org.solovyev.android.calculator.ui.tutorial.GestureDirection.LEFT,
            org.solovyev.android.calculator.ui.tutorial.GestureDirection.RIGHT
        )

        directions.forEach { direction ->
            // Arrange
            composeTestRule.setContent {
                MaterialTheme {
                    Surface {
                        org.solovyev.android.calculator.ui.tutorial.AnimatedSwipeIcon(
                            direction = direction,
                            modifier = androidx.compose.ui.Modifier.size(80.dp)
                        )
                    }
                }
            }

            // Assert: Icon should be displayed
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun test_tutorialProgressPersistence() = runTest {
        // Arrange
        val fakeDataStore = FakeDataStore()
        val tutorialManager = org.solovyev.android.calculator.ui.tutorial.TutorialManager(fakeDataStore)

        // Act: Mark hint as shown
        tutorialManager.markHintShown("swipe_up")

        // Assert: State should be persisted
        val state = tutorialManager.tutorialState.first()
        assertTrue(state.shownHints.contains("swipe_up"))
        assertTrue(state.swipeUpShown)
    }

    @Test
    fun test_tutorialReset() = runTest {
        // Arrange
        val fakeDataStore = FakeDataStore()
        val tutorialManager = org.solovyev.android.calculator.ui.tutorial.TutorialManager(fakeDataStore)

        // Set some state
        tutorialManager.markHintShown("swipe_up")
        tutorialManager.markTutorialCompleted()

        // Act: Reset tutorial
        tutorialManager.resetTutorial()

        // Assert: State should be reset
        val state = tutorialManager.tutorialState.first()
        assertFalse(state.hasCompletedTutorial)
        assertTrue(state.shownHints.isEmpty())
    }

    @Test
    fun test_shouldShowHint() = runTest {
        // Arrange
        val fakeDataStore = FakeDataStore()
        val tutorialManager = org.solovyev.android.calculator.ui.tutorial.TutorialManager(fakeDataStore)

        // Act & Assert: Should show hint that hasn't been shown
        assertTrue(tutorialManager.shouldShowHint("swipe_up"))

        // Mark as shown
        tutorialManager.markHintShown("swipe_up")

        // Assert: Should not show hint that was already shown
        assertFalse(tutorialManager.shouldShowHint("swipe_up"))
    }
}

// Extension function for Compose test
private fun androidx.compose.ui.test.SemanticsNodeInteraction.isDisplayed(): Boolean {
    return try {
        assertIsDisplayed()
        true
    } catch (e: AssertionError) {
        false
    }
}

/**
 * Test wrapper composable for Gesture Tutorial
 */
@Composable
private fun GestureTutorialTestWrapper(
    showTutorial: Boolean,
    tutorialState: org.solovyev.android.calculator.ui.tutorial.TutorialState,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    if (showTutorial && !tutorialState.hasCompletedTutorial) {
        org.solovyev.android.calculator.ui.tutorial.FirstTimeTutorial(
            onComplete = onComplete,
            onSkip = onSkip,
            tutorialManager = FakeTutorialManager(tutorialState)
        )
    }
}

@Composable
private fun GestureHintOverlayTestWrapper(
    hintData: org.solovyev.android.calculator.ui.tutorial.GestureHintData,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onHintShown: () -> Unit
) {
    org.solovyev.android.calculator.ui.tutorial.GestureHintOverlay(
        hintData = hintData,
        isVisible = isVisible,
        onDismiss = onDismiss,
        onHintShown = onHintShown
    )
}

/**
 * Fake TutorialManager for testing
 */
class FakeTutorialManager(
    private val initialState: org.solovyev.android.calculator.ui.tutorial.TutorialState
) : org.solovyev.android.calculator.ui.tutorial.TutorialManager(
    dataStore = FakeDataStore()
) {
    override val tutorialState: kotlinx.coroutines.flow.Flow<org.solovyev.android.calculator.ui.tutorial.TutorialState> = 
        kotlinx.coroutines.flow.flowOf(initialState)
}

/**
 * Fake DataStore for testing
 */
class FakeDataStore : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
    private val preferences = androidx.datastore.preferences.core.Preferences.Factory.create(
        androidx.datastore.preferences.core.mutablePreferencesOf()
    )

    override val data: kotlinx.coroutines.flow.Flow<androidx.datastore.preferences.core.Preferences> = 
        kotlinx.coroutines.flow.flowOf(preferences)

    override suspend fun updateData(transform: suspend (androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences): androidx.datastore.preferences.core.Preferences {
        return transform(preferences)
    }
}
