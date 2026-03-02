package org.solovyev.android.calculator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.solovyev.android.calculator.ui.CalculatorMenuEntry
import org.solovyev.android.calculator.ui.CalculatorOverflowDropdownMenu
import org.solovyev.android.calculator.ui.CalculatorOverflowIconButton
import kotlin.test.Test
import kotlin.test.assertTrue

class CalculatorOverflowMenuTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun overflowIconButton_hasAccessibleLabel() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    CalculatorOverflowIconButton(onClick = {})
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("More options")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun overflowDropdownMenu_rendersEntriesAndInvokesActions() {
        var settingsClicked = false
        var deleteClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    CalculatorOverflowDropdownMenu(
                        expanded = true,
                        onDismissRequest = {},
                        entries = listOf(
                            CalculatorMenuEntry.Action(
                                label = "Settings",
                                icon = Icons.Default.Settings,
                                onClick = { settingsClicked = true }
                            ),
                            CalculatorMenuEntry.Divider,
                            CalculatorMenuEntry.Action(
                                label = "Delete",
                                icon = Icons.Default.DeleteOutline,
                                destructive = true,
                                showTrailingArrow = false,
                                onClick = { deleteClicked = true }
                            )
                        )
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Settings")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.onNodeWithText("Delete")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.runOnIdle {
            assertTrue(settingsClicked)
            assertTrue(deleteClicked)
        }
    }
}
