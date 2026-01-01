package org.solovyev.android.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState

/**
 * Main calculator screen combining display, editor, and keyboard.
 *
 * Layout:
 * - Editor at top (shows input)
 * - Row with Equals button and Display below editor
 * - Keyboard at bottom (for input)
 *
 * @param displayState Current display state
 * @param editorState Current editor state
 * @param onEditorTextChange Callback when editor text changes
 * @param onEditorSelectionChange Callback when editor selection changes
 * @param keyboard Keyboard content
 * @param onEquals Callback for equals button
 * @param onSimplify Callback for simplify (drag up on equals)
 * @param onPlot Callback for plot (drag down on equals)
 * @param modifier Modifier to be applied to the screen
 */
@Composable
fun CalculatorScreen(
    displayState: DisplayState,
    editorState: EditorState,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    highlightExpressions: Boolean = true,
    highContrast: Boolean = false,
    hapticsEnabled: Boolean = true,
    keyboard: @Composable (Modifier) -> Unit,
    onEquals: () -> Unit,
    onSimplify: () -> Unit,
    onPlot: () -> Unit,
    overlayContent: @Composable BoxScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val labels = LocalKeyboardStrings.current

    CompositionLocalProvider(
        LocalCalculatorHighContrast provides highContrast,
        LocalCalculatorHapticsEnabled provides hapticsEnabled
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Editor section (input)
                    CalculatorEditor(
                        state = editorState,
                        onTextChange = onEditorTextChange,
                        onSelectionChange = onEditorSelectionChange,
                        highlightExpressions = highlightExpressions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(2f)
                    )

                    // Display row (equals + display)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        CalculatorButton(
                            text = "=",
                            buttonType = ButtonType.CONTROL,
                            directionTexts = DirectionTexts(
                                up = "≡",
                                down = labels.glyphGraph
                            ),
                            onClick = onEquals,
                            onSwipeUp = onSimplify,
                            onSwipeDown = onPlot,
                            backgroundOverride = Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                        CalculatorDisplay(
                            state = displayState,
                            modifier = Modifier
                                .weight(4f)
                                .fillMaxSize()
                                .padding(horizontal = 10.dp)
                        )
                    }

                    keyboard(
                        Modifier
                            .fillMaxWidth()
                            .weight(5f)
                    )

                    // Bottom bar (for modern mode floating toolbar)
                    bottomBar()
                }

                overlayContent()
            }
        }
    }
}
