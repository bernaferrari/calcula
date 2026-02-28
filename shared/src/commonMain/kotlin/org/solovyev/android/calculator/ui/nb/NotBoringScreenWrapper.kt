package org.solovyev.android.calculator.ui.nb

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.TapeEntry
import org.solovyev.android.calculator.ui.*
import jscl.NumeralBase

/**
 * Toggle between classic and Not Boring calculator styles.
 */
enum class UiStyle {
    CLASSIC,
    NOT_BORING
}

@Composable
fun CalculatorScreenWithStyle(
    uiStyle: UiStyle,
    keyboardMode: KeyboardMode = KeyboardMode.ENGINEER,
    displayState: DisplayState,
    editorState: EditorState,
    previewResult: String?,
    unitHint: String?,
    rpnMode: Boolean = false,
    rpnStack: List<String> = emptyList(),
    tapeMode: Boolean = true,
    tapeEntries: List<TapeEntry> = emptyList(),
    liveTapeEntry: TapeEntry? = null,
    memoryActiveRegister: String? = null,
    numeralBase: NumeralBase = NumeralBase.dec,
    bitwiseWordSize: Int = 64,
    bitwiseSigned: Boolean = true,
    bitwiseOverflow: Boolean = false,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFormulas: () -> Unit = {},
    showBottomToolbar: Boolean = false,
    onClearTape: () -> Unit = {},
    hapticsEnabled: Boolean = true,
    reduceMotion: Boolean = false,
    fontScale: Float = 1.0f,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    when (uiStyle) {
        UiStyle.CLASSIC -> {
            org.solovyev.android.calculator.ui.CalculatorScreen(
                displayState = displayState,
                editorState = editorState,
                previewResult = previewResult,
                unitHint = unitHint,
                rpnMode = rpnMode,
                rpnStack = rpnStack,
                tapeMode = tapeMode,
                tapeEntries = tapeEntries,
                liveTapeEntry = liveTapeEntry,
                onEditorTextChange = onEditorTextChange,
                onEditorSelectionChange = onEditorSelectionChange,
                onOpenHistory = onOpenHistory,
                onOpenSettings = onOpenSettings,
                onCopy = keyboardActions::onCopy,
                onEquals = keyboardActions::onEquals,
                onClearTape = onClearTape,
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
                hapticsEnabled = hapticsEnabled
            )
        }
    }
}
