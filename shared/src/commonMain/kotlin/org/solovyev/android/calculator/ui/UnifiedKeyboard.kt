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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jscl.NumeralBase

/**
 * Unified calculator keyboard - clean, minimal design.
 * 
 * Layout:
 * - Row 1: Clear, (, ), /
 * - Row 2: 7, 8, 9, *
 * - Row 3: 4, 5, 6, -
 * - Row 4: 1, 2, 3, +
 * - Row 5: backspace, 0, ., =
 * 
 * Advanced features available via gestures:
 * - Long press: secondary actions
 * - Swipe: alternative functions
 */
@Composable
fun UnifiedCalculatorKeyboard(
    actions: KeyboardActions,
    numeralBase: NumeralBase = NumeralBase.dec,
    bitwiseWordSize: Int = 64,
    bitwiseSigned: Boolean = true,
    gestureAutoActivation: Boolean = false,
    showBottomRightEqualsKey: Boolean = false,
    modifier: Modifier = Modifier
) {
    val icons = LocalKeyboardIcons.current
    var showScientificSheet by remember { mutableStateOf(false) }
    val keyboardPadding = 4.dp
    val keyGap = 6.dp

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
            .padding(keyboardPadding),
        verticalArrangement = Arrangement.spacedBy(keyGap)
    ) {
        // Row 1: Clear, (, ), /
        KeyboardRow(modifier = Modifier.weight(1f)) {
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "C",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onClear() }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "(",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onSpecialClick("(") },
                onSwipeUp = { actions.onOperatorClick("%") },
                onLongClick = { actions.onSpecialClick(")") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = ")",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onSpecialClick(")") },
                onSwipeUp = { showScientificSheet = true }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "/",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("/") }
            )
        }

        // Row 2: 7, 8, 9, *
        KeyboardRow(modifier = Modifier.weight(1f)) {
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "7",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "i", down = "!"),
                onClick = { actions.onNumberClick("7") },
                enabled = isDigitAllowedForBase("7", numeralBase),
                onSwipeUp = { actions.onSpecialClick("i") },
                onSwipeDown = { actions.onSpecialClick("!") },
                onSwipeLeft = { actions.onSpecialClick("0b:") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "8",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "ln", down = "lg"),
                onClick = { actions.onNumberClick("8") },
                enabled = isDigitAllowedForBase("8", numeralBase),
                onSwipeUp = { actions.onFunctionClick("ln") },
                onSwipeDown = { actions.onFunctionClick("log") },
                onSwipeLeft = { actions.onSpecialClick("0d:") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "9",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("9") },
                enabled = isDigitAllowedForBase("9", numeralBase),
                onSwipeLeft = { actions.onSpecialClick("0x:") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "*",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(up = "ˆ", down = "x²"),
                onClick = { actions.onOperatorClick("*") },
                onSwipeUp = { actions.onSpecialClick("^") },
                onSwipeDown = { actions.onSpecialClick("^2") }
            )
        }

        // Row 3: 4, 5, 6, -
        KeyboardRow(modifier = Modifier.weight(1f)) {
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "4",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "|x|"),
                onClick = { actions.onNumberClick("4") },
                enabled = isDigitAllowedForBase("4", numeralBase),
                onSwipeUp = { actions.onFunctionClick("abs") },
                onSwipeLeft = { if (numeralBase == NumeralBase.hex) actions.onNumberClick("D") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "5",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "1/x"),
                onClick = { actions.onNumberClick("5") },
                enabled = isDigitAllowedForBase("5", numeralBase),
                onSwipeUp = { actions.onSpecialClick("1/") },
                onSwipeLeft = { if (numeralBase == NumeralBase.hex) actions.onNumberClick("E") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "6",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(down = "τ"),
                onClick = { actions.onNumberClick("6") },
                enabled = isDigitAllowedForBase("6", numeralBase),
                onSwipeDown = { actions.onSpecialClick("2π") },
                onSwipeLeft = { if (numeralBase == NumeralBase.hex) actions.onNumberClick("F") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "-",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("-") }
            )
        }

        // Row 4: 1, 2, 3, +
        KeyboardRow(modifier = Modifier.weight(1f)) {
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "1",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "sin", down = "φ"),
                onClick = { actions.onNumberClick("1") },
                enabled = isDigitAllowedForBase("1", numeralBase),
                onSwipeUp = { actions.onFunctionClick("sin") },
                onSwipeDown = { actions.onSpecialClick("phi") },
                onSwipeLeft = { if (numeralBase == NumeralBase.hex) actions.onNumberClick("A") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "2",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "cos", down = "e"),
                onClick = { actions.onNumberClick("2") },
                enabled = isDigitAllowedForBase("2", numeralBase),
                onSwipeUp = { actions.onFunctionClick("cos") },
                onSwipeDown = { actions.onSpecialClick("e") },
                onSwipeLeft = { if (numeralBase == NumeralBase.hex) actions.onNumberClick("B") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "3",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "tan", down = "π"),
                onClick = { actions.onNumberClick("3") },
                enabled = isDigitAllowedForBase("3", numeralBase),
                onSwipeUp = { actions.onFunctionClick("tan") },
                onSwipeDown = { actions.onSpecialClick("π") },
                onSwipeLeft = { if (numeralBase == NumeralBase.hex) actions.onNumberClick("C") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "+",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("+") }
            )
        }

        // Row 5: backspace, 0, ., =
        KeyboardRow(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                ModernButton(
                    text = "",
                    buttonType = ButtonType.CONTROL,
                    onClick = { actions.onDelete() },
                    onLongClick = { actions.onClear() },
                    modifier = Modifier.fillMaxSize(),
                    gestureAutoActivation = gestureAutoActivation
                )
                Icon(
                    painter = icons.backspace,
                    contentDescription = null,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "0",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("0") },
                enabled = isDigitAllowedForBase("0", numeralBase),
                onSwipeUp = { actions.onNumberClick("0") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = ".",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick(".") },
                enabled = true,
                onSwipeUp = { actions.onSpecialClick(",") }
            )
            UnifiedButton(
                gestureAutoActivation = gestureAutoActivation,
                text = "=",
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                onClick = { actions.onEquals() },
                onLongClick = { actions.onOpenFunctions() },
                onSwipeUp = { showScientificSheet = true }
            )
        }
    }
}

@Composable
private fun KeyboardRow(
    gap: androidx.compose.ui.unit.Dp = 6.dp,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap),
        content = content
    )
}

@Composable
private fun RowScope.UnifiedButton(
    text: String,
    buttonType: ButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    directionTexts: DirectionTexts = DirectionTexts(),
    onLongClick: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    gestureAutoActivation: Boolean = false
) {
    ModernButton(
        text = text,
        buttonType = buttonType,
        onClick = onClick,
        modifier = modifier
            .weight(1f)
            .fillMaxHeight(),
        enabled = enabled,
        directionTexts = directionTexts,
        onLongClick = onLongClick,
        onSwipeUp = onSwipeUp,
        onSwipeDown = onSwipeDown,
        onSwipeLeft = onSwipeLeft,
        onSwipeRight = onSwipeRight,
        gestureAutoActivation = gestureAutoActivation
    )
}
