package org.solovyev.android.calculator

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * UI Tests for the History Screen
 * 
 * Tests history display, item reuse, deletion, saved tab, clear history,
 * and empty state following the AAA (Arrange, Act, Assert) pattern.
 */
@OptIn(ExperimentalTestApi::class)
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val recordedActions = mutableListOf<String>()
    private var clickedHistoryItem: org.solovyev.android.calculator.history.HistoryState? = null

    private fun setHistoryScreenContent(
        recent: List<org.solovyev.android.calculator.history.HistoryState> = emptyList(),
        saved: List<org.solovyev.android.calculator.history.HistoryState> = emptyList()
    ) {
        recordedActions.clear()
        clickedHistoryItem = null
        
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    HistoryScreenTestWrapper(
                        recent = recent,
                        saved = saved,
                        onUse = { item ->
                            clickedHistoryItem = item
                            recordedActions.add("use:${item.editor.getTextString()}")
                        },
                        onCopyExpression = { recordedActions.add("copy_expr") },
                        onCopyResult = { recordedActions.add("copy_result") },
                        onSave = { recordedActions.add("save") },
                        onEdit = { recordedActions.add("edit") },
                        onDelete = { item ->
                            recordedActions.add("delete:${item.editor.getTextString()}")
                        },
                        onClearRecent = { recordedActions.add("clear_recent") },
                        onClearSaved = { recordedActions.add("clear_saved") }
                    )
                }
            }
        }
    }

    @Test
    fun test_historyItemDisplayed() {
        // Arrange: Create some history items
        val historyItems = listOf(
            createHistoryState(
                id = 1,
                expression = "2 + 2",
                result = "4",
                time = System.currentTimeMillis()
            ),
            createHistoryState(
                id = 2,
                expression = "10 * 5",
                result = "50",
                time = System.currentTimeMillis() - 1000
            )
        )

        // Act
        setHistoryScreenContent(recent = historyItems)

        // Assert: History items should be displayed
        composeTestRule.onNodeWithText("2 + 2")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("= 4")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("10 * 5")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("= 50")
            .assertIsDisplayed()
    }

    @Test
    fun test_historyItemReuse() {
        // Arrange
        val historyItems = listOf(
            createHistoryState(
                id = 1,
                expression = "5 * 5",
                result = "25",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(recent = historyItems)

        // Act: Click on history item
        composeTestRule.onNodeWithText("5 * 5")
            .performClick()

        // Assert: Item should be reused
        assertNotNull(clickedHistoryItem)
        assertEquals("5 * 5", clickedHistoryItem?.editor?.getTextString())
        assertTrue(recordedActions.contains("use:5 * 5"))
    }

    @Test
    fun test_historyItemDelete() {
        // Arrange
        val historyItems = listOf(
            createHistoryState(
                id = 1,
                expression = "100 / 4",
                result = "25",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(recent = historyItems)

        // Act: Swipe to delete
        composeTestRule.onNodeWithText("100 / 4")
            .performTouchInput { swipeLeft() }

        // Assert: Delete action should be triggered
        // Note: Actual deletion would be handled by the ViewModel
        assertTrue(recordedActions.any { it.startsWith("delete") })
    }

    @Test
    fun test_savedTab() {
        // Arrange
        val savedItems = listOf(
            createHistoryState(
                id = 101,
                expression = "pi * 2",
                result = "6.283185...",
                time = System.currentTimeMillis(),
                comment = "Circumference formula"
            )
        )

        // Act
        setHistoryScreenContent(saved = savedItems)
        
        // Switch to Saved tab
        composeTestRule.onNodeWithText("Saved")
            .performClick()

        // Assert: Saved items should be displayed
        composeTestRule.onNodeWithText("pi * 2")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Circumference formula")
            .assertIsDisplayed()
    }

    @Test
    fun test_clearHistory() {
        // Arrange
        val historyItems = listOf(
            createHistoryState(
                id = 1,
                expression = "1 + 1",
                result = "2",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(recent = historyItems)

        // Act: Click clear button
        composeTestRule.onNodeWithContentDescription("Clear history")
            .performClick()

        // Assert: Clear action should be triggered
        assertTrue(recordedActions.contains("clear_recent"))
    }

    @Test
    fun test_emptyStateRecent() {
        // Arrange: Empty recent history
        setHistoryScreenContent(recent = emptyList())

        // Assert: Empty state should be shown
        composeTestRule.onNodeWithText("No history yet")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Your calculations will appear here")
            .assertIsDisplayed()
    }

    @Test
    fun test_emptyStateSaved() {
        // Arrange: Empty saved history
        setHistoryScreenContent(saved = emptyList())

        // Act: Switch to Saved tab
        composeTestRule.onNodeWithText("Saved")
            .performClick()

        // Assert: Empty state for saved should be shown
        composeTestRule.onNodeWithText("No saved calculations")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Save important calculations for quick access")
            .assertIsDisplayed()
    }

    @Test
    fun test_copyExpression() {
        // Arrange
        val historyItems = listOf(
            createHistoryState(
                id = 1,
                expression = "sin(30)",
                result = "0.5",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(recent = historyItems)

        // Act: Click copy expression button
        composeTestRule.onNodeWithContentDescription("Copy expression")
            .performClick()

        // Assert
        assertTrue(recordedActions.contains("copy_expr"))
    }

    @Test
    fun test_copyResult() {
        // Arrange
        val historyItems = listOf(
            createHistoryState(
                id = 1,
                expression = "sqrt(16)",
                result = "4",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(recent = historyItems)

        // Act: Click more options, then copy result
        composeTestRule.onNodeWithContentDescription("More options")
            .performClick()
        composeTestRule.onNodeWithText("Copy result")
            .performClick()

        // Assert
        assertTrue(recordedActions.contains("copy_result"))
    }

    @Test
    fun test_saveHistoryItem() {
        // Arrange
        val historyItems = listOf(
            createHistoryState(
                id = 1,
                expression = "e^2",
                result = "7.389",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(recent = historyItems)

        // Act: Click more options, then save
        composeTestRule.onNodeWithContentDescription("More options")
            .performClick()
        composeTestRule.onNodeWithText("Save")
            .performClick()

        // Assert
        assertTrue(recordedActions.contains("save"))
    }

    @Test
    fun test_editSavedItem() {
        // Arrange
        val savedItems = listOf(
            createHistoryState(
                id = 101,
                expression = "custom formula",
                result = "42",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(saved = savedItems)

        // Act: Switch to Saved tab and click more options
        composeTestRule.onNodeWithText("Saved").performClick()
        composeTestRule.onNodeWithContentDescription("More options")
            .performClick()
        composeTestRule.onNodeWithText("Edit")
            .performClick()

        // Assert
        assertTrue(recordedActions.contains("edit"))
    }

    @Test
    fun test_deleteSavedItem() {
        // Arrange
        val savedItems = listOf(
            createHistoryState(
                id = 101,
                expression = "temp calculation",
                result = "123",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(saved = savedItems)

        // Act: Switch to Saved tab and delete
        composeTestRule.onNodeWithText("Saved").performClick()
        composeTestRule.onNodeWithContentDescription("More options")
            .performClick()
        composeTestRule.onNodeWithText("Delete")
            .performClick()

        // Assert
        assertTrue(recordedActions.any { it.startsWith("delete") })
    }

    @Test
    fun test_historyTimestamp() {
        // Arrange
        val currentTime = System.currentTimeMillis()
        val historyItems = listOf(
            createHistoryState(
                id = 1,
                expression = "2 + 3",
                result = "5",
                time = currentTime
            )
        )
        setHistoryScreenContent(recent = historyItems)

        // Assert: Timestamp should be displayed (formatted)
        // The exact format depends on the implementation
        composeTestRule.onNodeWithText(containsSubstring = ":")
            .assertIsDisplayed()
    }

    @Test
    fun test_multipleHistoryItems() {
        // Arrange
        val historyItems = (1..10).map { i ->
            createHistoryState(
                id = i.toLong(),
                expression = "$i + $i",
                result = "${i * 2}",
                time = System.currentTimeMillis() - (i * 1000)
            )
        }

        // Act
        setHistoryScreenContent(recent = historyItems)

        // Assert: Multiple items should be displayed
        historyItems.take(5).forEach { item ->
            composeTestRule.onNodeWithText(item.editor.getTextString())
                .assertIsDisplayed()
        }
    }

    @Test
    fun test_historyUseButton() {
        // Arrange
        val historyItems = listOf(
            createHistoryState(
                id = 1,
                expression = "3.14159",
                result = "3.14159",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(recent = historyItems)

        // Act: Click use button (play arrow icon)
        composeTestRule.onNodeWithContentDescription("Use")
            .performClick()

        // Assert
        assertTrue(recordedActions.contains("use:3.14159"))
    }

    @Test
    fun test_clearSavedHistory() {
        // Arrange
        val savedItems = listOf(
            createHistoryState(
                id = 101,
                expression = "saved calc",
                result = "100",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(saved = savedItems)

        // Act: Switch to Saved tab and clear
        composeTestRule.onNodeWithText("Saved").performClick()
        composeTestRule.onNodeWithContentDescription("Clear saved")
            .performClick()

        // Assert
        assertTrue(recordedActions.contains("clear_saved"))
    }

    @Test
    fun test_historyItemLongExpression() {
        // Arrange
        val historyItems = listOf(
            createHistoryState(
                id = 1,
                expression = "(123456789 + 987654321) * 2 / 1000000",
                result = "2222.2222",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(recent = historyItems)

        // Assert: Long expressions should be displayed (possibly truncated)
        composeTestRule.onNodeWithText(containsSubstring = "123456789")
            .assertIsDisplayed()
    }

    @Test
    fun test_historyItemErrorResult() {
        // Arrange
        val historyItems = listOf(
            org.solovyev.android.calculator.history.HistoryState(
                id = 1,
                time = System.currentTimeMillis(),
                editor = org.solovyev.android.calculator.EditorState.create("1/0", 3),
                display = org.solovyev.android.calculator.DisplayState(
                    text = "Division by zero",
                    valid = false,
                    sequence = 1
                ),
                comment = ""
            )
        )
        setHistoryScreenContent(recent = historyItems)

        // Assert: Error results should still be displayed
        composeTestRule.onNodeWithText("1/0")
            .assertIsDisplayed()
    }

    @Test
    fun test_tabSwitching() {
        // Arrange
        val recentItems = listOf(
            createHistoryState(
                id = 1,
                expression = "recent calc",
                result = "10",
                time = System.currentTimeMillis()
            )
        )
        val savedItems = listOf(
            createHistoryState(
                id = 101,
                expression = "saved calc",
                result = "20",
                time = System.currentTimeMillis()
            )
        )
        setHistoryScreenContent(recent = recentItems, saved = savedItems)

        // Act & Assert: Switch between tabs
        composeTestRule.onNodeWithText("Recent")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Saved")
            .performClick()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("saved calc")
            .assertIsDisplayed()
    }

    @Test
    fun test_historyItemWithComment() {
        // Arrange
        val savedItems = listOf(
            createHistoryState(
                id = 101,
                expression = "2 * pi * r",
                result = "formula",
                time = System.currentTimeMillis(),
                comment = "Circumference of a circle"
            )
        )
        setHistoryScreenContent(saved = savedItems)

        // Act: Switch to Saved tab
        composeTestRule.onNodeWithText("Saved").performClick()

        // Assert: Comment should be displayed
        composeTestRule.onNodeWithText("Circumference of a circle")
            .assertIsDisplayed()
    }
}

// Helper function to check text contains substring
private fun androidx.compose.ui.test.SemanticsMatcher.Companion.hasText(
    containsSubstring: String
): androidx.compose.ui.test.SemanticsMatcher {
    return androidx.compose.ui.test.SemanticsMatcher("Text contains '$containsSubstring'") { node ->
        node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)?.any { 
            it.text.contains(containsSubstring) 
        } ?: false
    }
}

private fun androidx.compose.ui.test.ComposeContentTestRule.onNodeWithText(
    containsSubstring: String
): androidx.compose.ui.test.SemanticsNodeInteraction {
    return onNode(androidx.compose.ui.test.SemanticsMatcher.hasText(containsSubstring))
}

/**
 * Helper function to create history state
 */
private fun createHistoryState(
    id: Long,
    expression: String,
    result: String,
    time: Long,
    comment: String = ""
): org.solovyev.android.calculator.history.HistoryState {
    return org.solovyev.android.calculator.history.HistoryState(
        id = id,
        time = time,
        editor = org.solovyev.android.calculator.EditorState.create(expression, expression.length),
        display = org.solovyev.android.calculator.DisplayState(
            text = result,
            valid = true,
            sequence = id
        ),
        comment = comment
    )
}

/**
 * Test wrapper composable for HistoryScreen
 */
@Composable
private fun HistoryScreenTestWrapper(
    recent: List<org.solovyev.android.calculator.history.HistoryState>,
    saved: List<org.solovyev.android.calculator.history.HistoryState>,
    onUse: (org.solovyev.android.calculator.history.HistoryState) -> Unit,
    onCopyExpression: (org.solovyev.android.calculator.history.HistoryState) -> Unit,
    onCopyResult: (org.solovyev.android.calculator.history.HistoryState) -> Unit,
    onSave: (org.solovyev.android.calculator.history.HistoryState) -> Unit,
    onEdit: (org.solovyev.android.calculator.history.HistoryState) -> Unit,
    onDelete: (org.solovyev.android.calculator.history.HistoryState) -> Unit,
    onClearRecent: () -> Unit,
    onClearSaved: () -> Unit
) {
    org.solovyev.android.calculator.ui.history.HistoryScreen(
        recent = recent,
        saved = saved,
        onUse = onUse,
        onCopyExpression = onCopyExpression,
        onCopyResult = onCopyResult,
        onSave = onSave,
        onEdit = onEdit,
        onDelete = onDelete,
        onClearRecent = onClearRecent,
        onClearSaved = onClearSaved,
        onBack = {}
    )
}
