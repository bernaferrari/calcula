package org.solovyev.android.calculator.ui.nb

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.TapeEntry
import org.solovyev.android.calculator.ui.*
import jscl.NumeralBase

/**
 * Toggle between classic and Not Boring calculator styles.
 * Usage: Set uiStyle = UiStyle.NOT_BORING for the new design.
 */
enum class UiStyle {
    CLASSIC,      // Original unified design
    NOT_BORING    // Andy-inspired, result-focused design
}

/**
 * Calculator screen with style toggle.
 * Wrap your calculator in this to easily switch between designs.
 */
@Composable
fun CalculatorScreenWithStyle(
    uiStyle: UiStyle,
    keyboardMode: KeyboardMode = KeyboardMode.ENGINEER,
    displayState: DisplayState,
    editorState: EditorState,
    previewResult: String?,
    unitHint: String?,
    calculationLatencyMs: Long?,
    rpnMode: Boolean = false,
    rpnStack: List<String> = emptyList(),
    tapeMode: Boolean = true,
    tapeEntries: List<TapeEntry> = emptyList(),
    liveTapeEntry: TapeEntry? = null,
    memoryActiveRegister: String = "A",
    numeralBase: NumeralBase = NumeralBase.dec,
    bitwiseWordSize: Int = 64,
    bitwiseSigned: Boolean = true,
    bitwiseOverflow: Boolean = false,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenVars: () -> Unit,
    onOpenGraph: () -> Unit,
    onOpenSettings: () -> Unit,
    showBottomToolbar: Boolean = false,
    onClearTape: () -> Unit = {},
    highlightExpressions: Boolean,
    highContrast: Boolean,
    hapticsEnabled: Boolean,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    when (uiStyle) {
        UiStyle.CLASSIC -> {
            // Use the original CalculatorScreen
            org.solovyev.android.calculator.ui.CalculatorScreen(
                displayState = displayState,
                editorState = editorState,
                previewResult = previewResult,
                unitHint = unitHint,
                calculationLatencyMs = calculationLatencyMs,
                rpnMode = rpnMode,
                rpnStack = rpnStack,
                tapeMode = tapeMode,
                tapeEntries = tapeEntries,
                liveTapeEntry = liveTapeEntry,
                onEditorTextChange = onEditorTextChange,
                onEditorSelectionChange = onEditorSelectionChange,
                onOpenHistory = onOpenHistory,
                onOpenConverter = onOpenConverter,
                onOpenFunctions = onOpenFunctions,
                onOpenVars = onOpenVars,
                onOpenGraph = onOpenGraph,
                onOpenSettings = onOpenSettings,
                onPrevious = keyboardActions::onCursorLeft,
                onPreviousStart = keyboardActions::onCursorToStart,
                onNext = keyboardActions::onCursorRight,
                onNextEnd = keyboardActions::onCursorToEnd,
                onCopy = keyboardActions::onCopy,
                onPaste = keyboardActions::onPaste,
                onEquals = keyboardActions::onEquals,
                onSimplify = keyboardActions::onSimplify,
                onClearTape = onClearTape,
                showBottomToolbar = showBottomToolbar,
                highlightExpressions = highlightExpressions,
                highContrast = highContrast,
                hapticsEnabled = hapticsEnabled,
                keyboard = { keyboardModifier ->
                    CalculatorKeyboard(
                        mode = keyboardMode,
                        actions = keyboardActions,
                        numeralBase = numeralBase,
                        bitwiseWordSize = bitwiseWordSize,
                        bitwiseSigned = bitwiseSigned,
                        modifier = keyboardModifier
                    )
                },
                modifier = modifier
            )
        }
        UiStyle.NOT_BORING -> {
            NotBoringScreen(
                displayState = displayState,
                editorState = editorState,
                previewResult = previewResult,
                unitHint = unitHint,
                onEditorTextChange = onEditorTextChange,
                onEditorSelectionChange = onEditorSelectionChange,
                onOpenHistory = onOpenHistory,
                keyboardActions = keyboardActions,
                modifier = modifier,
                highContrast = highContrast,
                hapticsEnabled = hapticsEnabled
            )
        }
    }
}
