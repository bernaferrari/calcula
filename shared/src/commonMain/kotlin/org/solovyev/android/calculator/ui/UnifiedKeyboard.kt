package org.solovyev.android.calculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Unified calculator keyboard - clean, minimal design.
 * 
 * Layout:
 * - Row 1: Clear, (, ), ÷
 * - Row 2: 7, 8, 9, ×
 * - Row 3: 4, 5, 6, −
 * - Row 4: 1, 2, 3, +
 * - Row 5: ⌫, 0, ., =
 * 
 * Advanced features available via gestures (no visible hints):
 * - Long press: secondary actions
 * - Swipe: alternative functions
 */
@Composable
fun UnifiedCalculatorKeyboard(
    actions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    val icons = LocalKeyboardIcons.current
    var showScientificSheet by remember { mutableStateOf(false) }

    if (showScientificSheet) {
        ScientificBottomSheet(
            onFunctionClick = { 
                actions.onFunctionClick(it)
                showScientificSheet = false 
            },
            onConstantClick = { 
                actions.onSpecialClick(it)
                showScientificSheet = false
            },
            onDismissRequest = { showScientificSheet = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Clear, (, ), ÷
        KeyboardRow(modifier = Modifier.weight(1f)) {
            UnifiedButton(
                text = "C",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onClear() },
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            UnifiedButton(
                text = "(",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onSpecialClick("(") },
                onSwipeUp = { actions.onOperatorClick("%") },
                onLongClick = { actions.onSpecialClick(")") }
            )
            UnifiedButton(
                text = ")",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onSpecialClick(")") },
                onSwipeUp = { showScientificSheet = true }
            )
            UnifiedButton(
                text = "÷",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("/") }
            )
        }

        // Row 2: 7, 8, 9, ×
        KeyboardRow(modifier = Modifier.weight(1f)) {
            UnifiedButton(
                text = "7",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("7") }
            )
            UnifiedButton(
                text = "8",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("8") }
            )
            UnifiedButton(
                text = "9",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("9") }
            )
            UnifiedButton(
                text = "×",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("×") }
            )
        }

        // Row 3: 4, 5, 6, −
        KeyboardRow(modifier = Modifier.weight(1f)) {
            UnifiedButton(
                text = "4",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("4") }
            )
            UnifiedButton(
                text = "5",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("5") }
            )
            UnifiedButton(
                text = "6",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("6") }
            )
            UnifiedButton(
                text = "−",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("−") }
            )
        }

        // Row 4: 1, 2, 3, +
        KeyboardRow(modifier = Modifier.weight(1f)) {
            UnifiedButton(
                text = "1",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("1") }
            )
            UnifiedButton(
                text = "2",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("2") }
            )
            UnifiedButton(
                text = "3",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("3") }
            )
            UnifiedButton(
                text = "+",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("+") }
            )
        }

        // Row 5: ⌫, 0, ., =
        KeyboardRow(modifier = Modifier.weight(1f)) {
            UnifiedButton(
                text = "",
                buttonType = ButtonType.CONTROL,
                icon = icons.backspace,
                onClick = { actions.onDelete() },
                onLongClick = { actions.onClear() }
            )
            UnifiedButton(
                text = "0",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("0") }
            )
            UnifiedButton(
                text = ".",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick(".") }
            )
            UnifiedButton(
                text = "=",
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                onClick = { actions.onEquals() },
                onLongClick = { actions.onSimplify() }
            )
        }
    }
}

@Composable
private fun KeyboardRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun RowScope.UnifiedButton(
    text: String,
    buttonType: ButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.painter.Painter? = null,
    onLongClick: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    fontWeight: androidx.compose.ui.text.font.FontWeight = androidx.compose.ui.text.font.FontWeight.Normal
) {
    org.solovyev.android.calculator.ui.UnifiedButton(
        text = text,
        buttonType = buttonType,
        onClick = onClick,
        modifier = modifier
            .weight(1f)
            .fillMaxHeight(),
        icon = icon,
        onLongClick = onLongClick,
        onSwipeUp = onSwipeUp,
        onSwipeDown = onSwipeDown,
        fontWeight = fontWeight
    )
}
