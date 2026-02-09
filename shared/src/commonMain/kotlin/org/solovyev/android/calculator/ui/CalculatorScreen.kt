@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package org.solovyev.android.calculator.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalculatorScreen(
    displayState: DisplayState,
    editorState: EditorState,
    previewResult: String? = null,
    unitHint: String? = null,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenGraph: () -> Unit,
    onOpenSettings: () -> Unit,
    highlightExpressions: Boolean = true,
    highContrast: Boolean = false,
    hapticsEnabled: Boolean = true,
    keyboard: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalCalculatorHighContrast provides highContrast,
        LocalCalculatorHapticsEnabled provides hapticsEnabled
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Minimal top bar with just history and settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onOpenHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Display area
                DisplayCard(
                    state = displayState,
                    editorState = editorState,
                    previewResult = previewResult,
                    unitHint = unitHint,
                    highlightExpressions = highlightExpressions,
                    onEditorTextChange = onEditorTextChange,
                    onEditorSelectionChange = onEditorSelectionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                )

                // Keyboard area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    keyboard(Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun DisplayCard(
    state: DisplayState,
    editorState: EditorState,
    previewResult: String?,
    unitHint: String?,
    highlightExpressions: Boolean,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Clean, reasonable font sizes
    val inputFontSize = 32.sp
    val resultFontSize = 48.sp

    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.End
    ) {
        CalculatorEditor(
            state = editorState,
            onTextChange = onEditorTextChange,
            onSelectionChange = onEditorSelectionChange,
            highlightExpressions = highlightExpressions,
            minTextSize = inputFontSize,
            maxTextSize = inputFontSize,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(
            visible = state.text.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Text(
                text = state.text,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = resultFontSize,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }

        AnimatedVisibility(
            visible = previewResult != null && state.text.isEmpty(),
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 }
        ) {
            Text(
                text = "= ${previewResult ?: ""}",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.End
            )
        }

        AnimatedVisibility(
            visible = unitHint != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = unitHint ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.End
            )
        }
    }
}
