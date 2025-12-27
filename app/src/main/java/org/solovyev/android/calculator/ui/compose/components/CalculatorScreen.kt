package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.R

/**
 * Main calculator screen combining display, editor, and keyboard.
 *
 * Layout:
 * - Display at top (shows result)
 * - Editor below display (shows input)
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
 * @param topBar Top app bar content
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
                                down = glyphString(R.string.cpp_glyph_graph)
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

/**
 * Preview-friendly calculator screen with sample states
 */
@Composable
fun CalculatorScreenPreview(
    modifier: Modifier = Modifier
) {
    var displayState by remember {
        mutableStateOf(
            DisplayState.createValid(
                operation = org.solovyev.android.calculator.jscl.JsclOperation.numeric,
                result = null,
                stringResult = "42",
                sequence = 1L
            )
        )
    }

    var editorState by remember {
        mutableStateOf(
            EditorState.create(
                text = "6 × 7",
                selection = 5
            )
        )
    }

    CalculatorScreen(
        displayState = displayState,
        editorState = editorState,
        onEditorTextChange = { newText, newSelection ->
            editorState = EditorState.create(newText, newSelection)
        },
        onEditorSelectionChange = { newSelection ->
            editorState = EditorState.forNewSelection(editorState, newSelection)
        },
        highlightExpressions = true,
        keyboard = { modifier ->
            androidx.compose.foundation.layout.Box(modifier = modifier)
        },
        onEquals = {},
        onSimplify = {},
        onPlot = {},
        overlayContent = {},
        modifier = modifier
    )
}
