package org.solovyev.android.calculator.ui.nb

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.ui.*

/**
 * Not Boring Calculator Screen
 * 
 * A reimagined calculator UI inspired by Not Boring Apps philosophy:
 * - Result is the absolute hero - massive, bold, confident
 * - No chrome - swipe gestures reveal history/scientific
 * - Clean, minimal keyboard - only = gets accent color
 * - Satisfying micro-interactions that feel delightful, not distracting
 * 
 * Gestures:
 * - Pull DOWN on display: Show history
 * - Swipe UP on keyboard: Show scientific functions
 */
@Composable
fun NotBoringScreen(
    displayState: DisplayState,
    editorState: EditorState,
    previewResult: String?,
    unitHint: String?,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    onOpenHistory: () -> Unit,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier,
    highContrast: Boolean = false,
    hapticsEnabled: Boolean = true
) {
    var showScientific by remember { mutableStateOf(false) }
    
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp)
                ) {
                    // Display - The Hero
                    NotBoringDisplay(
                        state = displayState,
                        editorState = editorState,
                        previewResult = previewResult,
                        unitHint = unitHint,
                        onEditorTextChange = onEditorTextChange,
                        onEditorSelectionChange = onEditorSelectionChange,
                        onSwipeDown = onOpenHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    
                    // Keyboard - Clean and minimal
                    NotBoringKeyboard(
                        actions = keyboardActions,
                        onSwipeUp = { showScientific = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 320.dp)
                    )
                }
            }
        }
        
        // Scientific sheet - slides up from bottom
        if (showScientific) {
            ScientificBottomSheet(
                onFunctionClick = { function ->
                    keyboardActions.onFunctionClick(function)
                    showScientific = false
                },
                onConstantClick = { constant ->
                    keyboardActions.onNumberClick(constant)
                    showScientific = false
                },
                onDismissRequest = { showScientific = false }
            )
        }
    }
}
