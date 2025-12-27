package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ui.compose.theme.CalculatorFontFamily

/**
 * Keyboard mode - simple, engineer, or modern
 */
enum class KeyboardMode {
    SIMPLE,
    ENGINEER,
    MODERN
}

/**
 * Callback interface for keyboard actions
 */
interface KeyboardActions {
    fun onNumberClick(number: String)
    fun onOperatorClick(operator: String)
    fun onFunctionClick(function: String)
    fun onSpecialClick(action: String)
    fun onClear()
    fun onDelete()
    fun onEquals()
    fun onMemoryRecall()
    fun onMemoryPlus()
    fun onMemoryMinus()
    fun onMemoryClear()
    fun onCursorLeft()
    fun onCursorRight()
    fun onCursorToStart()
    fun onCursorToEnd()
    fun onCopy()
    fun onPaste()
    fun onOpenVars()
    fun onOpenFunctions()
    fun onOpenHistory()
}

/**
 * Complete calculator keyboard layout based on the existing XML layouts
 * Supports both simple and engineer modes
 */
@Composable
fun CalculatorKeyboard(
    mode: KeyboardMode = KeyboardMode.ENGINEER,
    actions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    when (mode) {
        KeyboardMode.SIMPLE -> SimpleCalculatorKeyboard(
            actions = actions,
            modifier = modifier
        )
        KeyboardMode.ENGINEER -> EngineerCalculatorKeyboard(
            actions = actions,
            modifier = modifier
        )
        KeyboardMode.MODERN -> ModernCalculatorKeyboard(
            actions = actions,
            modifier = modifier
        )
    }
}

@Composable
private fun EngineerCalculatorKeyboard(
    actions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    val glyphLeft = glyphString(R.string.cpp_glyph_left)
    val glyphRight = glyphString(R.string.cpp_glyph_right)
    val glyphCopy = glyphString(R.string.cpp_glyph_copy)
    val glyphPaste = glyphString(R.string.cpp_glyph_paste)
    val glyphFastBack = glyphString(R.string.cpp_glyph_fast_back)
    val glyphFastForward = glyphString(R.string.cpp_glyph_fast_forward)
    val glyphHistory = glyphString(R.string.cpp_glyph_history)
    val glyphUndo = glyphString(R.string.cpp_glyph_undo)
    val glyphRedo = glyphString(R.string.cpp_glyph_redo)
    val glyphBackspace = glyphString(R.string.cpp_glyph_backspace)
    val variablesLabel = stringResource(R.string.cpp_kb_variables)
    val functionsLabel = stringResource(R.string.cpp_kb_functions)
    val memoryPlus = stringResource(R.string.cpp_kb_memory_plus)
    val memoryMinus = stringResource(R.string.cpp_kb_memory_minus)
    val memoryClear = stringResource(R.string.cpp_kb_memory_clear)
    val clearLabel = stringResource(R.string.cpp_kb_clear)

    fun swipeAction(text: String?): (() -> Unit)? =
        text?.takeIf { it.isNotEmpty() }?.let { { actions.onSpecialClick(it) } }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Row 1: Left, Right, %, Erase, Clear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
            CalculatorButton(
                text = glyphLeft,
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = glyphFastBack,
                    down = glyphCopy
                ),
                onClick = { actions.onCursorLeft() },
                onSwipeUp = swipeAction(glyphFastBack),
                onSwipeDown = swipeAction(glyphCopy),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = glyphRight,
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = glyphFastForward,
                    down = glyphPaste
                ),
                onClick = { actions.onCursorRight() },
                onSwipeUp = swipeAction(glyphFastForward),
                onSwipeDown = swipeAction(glyphPaste),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "%",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("%") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = glyphBackspace,
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                onClick = { actions.onDelete() },
                onLongClick = { actions.onClear() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = clearLabel,
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = memoryClear
                ),
                fontWeight = FontWeight.Bold,
                onClick = { actions.onClear() },
                onSwipeUp = swipeAction(memoryClear),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 2: 7, 8, 9, /, Memory
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
            CalculatorButton(
                text = "7",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "i",
                    down = "!",
                    left = "0b:"
                ),
                onClick = { actions.onNumberClick("7") },
                onSwipeUp = swipeAction("i"),
                onSwipeDown = swipeAction("!"),
                onSwipeLeft = swipeAction("0b:"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "8",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "ln",
                    down = "lg",
                    left = "0d:"
                ),
                onClick = { actions.onNumberClick("8") },
                onSwipeUp = swipeAction("ln"),
                onSwipeDown = swipeAction("lg"),
                onSwipeLeft = swipeAction("0d:"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "9",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    left = "0x:"
                ),
                onClick = { actions.onNumberClick("9") },
                onSwipeLeft = swipeAction("0x:"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "/",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(
                    up = "√"
                ),
                onClick = { actions.onOperatorClick("/") },
                onSwipeUp = swipeAction("√"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = stringResource(R.string.cpp_kb_memory_recall),
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = memoryPlus,
                    down = memoryMinus
                ),
                fontWeight = FontWeight.Bold,
                onClick = { actions.onMemoryRecall() },
                onSwipeUp = swipeAction(memoryPlus),
                onSwipeDown = swipeAction(memoryMinus),
                onLongClick = { actions.onMemoryClear() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 3: 4, 5, 6, *, Vars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
            CalculatorButton(
                text = "4",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "x",
                    down = "y",
                    left = "D"
                ),
                onClick = { actions.onNumberClick("4") },
                onSwipeUp = swipeAction("x"),
                onSwipeDown = swipeAction("y"),
                onSwipeLeft = swipeAction("D"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "5",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "t",
                    down = "j",
                    left = "E"
                ),
                onClick = { actions.onNumberClick("5") },
                onSwipeUp = swipeAction("t"),
                onSwipeDown = swipeAction("j"),
                onSwipeLeft = swipeAction("E"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "6",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "E",
                    left = "F"
                ),
                onClick = { actions.onNumberClick("6") },
                onSwipeUp = swipeAction("E"),
                onSwipeLeft = swipeAction("F"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "×",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(
                    up = "^",
                    down = "^2"
                ),
                onClick = { actions.onOperatorClick("×") },
                onSwipeUp = swipeAction("^"),
                onSwipeDown = swipeAction("^2"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = variablesLabel,
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = "π",
                    down = "e"
                ),
                onClick = { actions.onOpenVars() },
                onSwipeUp = swipeAction("π"),
                onSwipeDown = swipeAction("e"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 4: 1, 2, 3, -, Functions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
            CalculatorButton(
                text = "1",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "sin",
                    down = "asin",
                    left = "A"
                ),
                onClick = { actions.onNumberClick("1") },
                onSwipeUp = swipeAction("sin"),
                onSwipeDown = swipeAction("asin"),
                onSwipeLeft = swipeAction("A"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "2",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "cos",
                    down = "acos",
                    left = "B"
                ),
                onClick = { actions.onNumberClick("2") },
                onSwipeUp = swipeAction("cos"),
                onSwipeDown = swipeAction("acos"),
                onSwipeLeft = swipeAction("B"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "3",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "tan",
                    down = "atan",
                    left = "C"
                ),
                onClick = { actions.onNumberClick("3") },
                onSwipeUp = swipeAction("tan"),
                onSwipeDown = swipeAction("atan"),
                onSwipeLeft = swipeAction("C"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "−",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("−") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = functionsLabel,
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = "+ƒ",
                    down = "+π"
                ),
                fontStyle = FontStyle.Italic,
                onClick = { actions.onOpenFunctions() },
                onSwipeUp = swipeAction("+ƒ"),
                onSwipeDown = swipeAction("+π"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 5: (), 0, ., +, History
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
            CalculatorButton(
                text = "( )",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "(",
                    down = ")",
                    left = "(…)"
                ),
                onClick = { actions.onSpecialClick("()") },
                onSwipeUp = swipeAction("("),
                onSwipeDown = swipeAction(")"),
                onSwipeLeft = swipeAction("(…)"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "0",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "000",
                    down = "00"
                ),
                onClick = { actions.onNumberClick("0") },
                onSwipeUp = swipeAction("000"),
                onSwipeDown = swipeAction("00"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = ".",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = ","
                ),
                onClick = { actions.onNumberClick(".") },
                onSwipeUp = swipeAction(","),
                directionTextScale = 0.6f,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "+",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(
                    up = "°"
                ),
                onClick = { actions.onOperatorClick("+") },
                onSwipeUp = swipeAction("°"),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = glyphHistory,
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = glyphUndo,
                    down = glyphRedo
                ),
                onClick = { actions.onOpenHistory() },
                onSwipeUp = swipeAction(glyphUndo),
                onSwipeDown = swipeAction(glyphRedo),
                directionTextScale = 0.5f,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * Simple keyboard with basic operations only
 */
@Composable
fun SimpleCalculatorKeyboard(
    actions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        val clearLabel = stringResource(R.string.cpp_kb_clear)
        val variablesLabel = stringResource(R.string.cpp_kb_variables)
        val functionsLabel = stringResource(R.string.cpp_kb_functions)
        val operatorsLabel = stringResource(R.string.cpp_kb_operators)
        val settingsLabel = stringResource(R.string.cpp_settings)
        val historyLabel = stringResource(R.string.c_history)

        // Row 1: %, Left, Right, Settings, Erase, Clear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
            CalculatorButton(
                text = "%",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("%") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "",
                buttonType = ButtonType.CONTROL,
                icon = painterResource(R.drawable.ic_keyboard_arrow_left_white_48dp),
                onClick = { actions.onCursorLeft() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "",
                buttonType = ButtonType.CONTROL,
                icon = painterResource(R.drawable.ic_keyboard_arrow_right_white_48dp),
                onClick = { actions.onCursorRight() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = settingsLabel,
                buttonType = ButtonType.CONTROL,
                icon = painterResource(R.drawable.ic_settings_white_48dp),
                onClick = { actions.onSpecialClick("settings") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "",
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                icon = painterResource(R.drawable.ic_backspace_white_48dp),
                onClick = { actions.onDelete() },
                onLongClick = { actions.onClear() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = clearLabel,
                buttonType = ButtonType.CONTROL,
                fontWeight = FontWeight.Bold,
                onClick = { actions.onClear() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 2: Functions, 7, 8, 9, /, Copy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
            CalculatorButton(
                text = functionsLabel,
                buttonType = ButtonType.CONTROL,
                fontStyle = FontStyle.Italic,
                onClick = { actions.onOpenFunctions() },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "7",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("7") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "8",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("8") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "9",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("9") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "/",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("/") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "",
                buttonType = ButtonType.CONTROL,
                icon = painterResource(R.drawable.ic_content_copy_white_48dp),
                onClick = { actions.onCopy() },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 3: Vars, 4, 5, 6, ×, Paste
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
            CalculatorButton(
                text = variablesLabel,
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onOpenVars() },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "4",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("4") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "5",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("5") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "6",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("6") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "×",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("×") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "",
                buttonType = ButtonType.CONTROL,
                icon = painterResource(R.drawable.ic_content_paste_white_48dp),
                onClick = { actions.onPaste() },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 4: Operators, 1, 2, 3, -, History
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
            CalculatorButton(
                text = operatorsLabel,
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onSpecialClick("operators") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "1",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("1") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "2",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("2") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "3",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("3") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "−",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("−") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "app",
                buttonType = ButtonType.CONTROL,
                icon = painterResource(R.drawable.ic_launch_white_48dp),
                onClick = { actions.onSpecialClick("open_app") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 5: ^, ( ), 0, ., +, History
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
            CalculatorButton(
                text = "^",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("^") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "( )",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onSpecialClick("()") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "0",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("0") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = ".",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick(".") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "+",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("+") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = historyLabel,
                buttonType = ButtonType.CONTROL,
                icon = painterResource(R.drawable.ic_history_white_48dp),
                onClick = { actions.onOpenHistory() },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

/**
 * Modern keyboard with simplified layout and rounded buttons.
 * Standard calculator layout with operators on the right column.
 * Advanced functions (π, <, >, f, etc.) are accessible via floating toolbar.
 */
@Composable
fun ModernCalculatorKeyboard(
    actions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    val glyphBackspace = glyphString(R.string.cpp_glyph_backspace)
    val buttonSpacing = 6.dp
    val buttonCornerRadius = 16.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        // Row 1: C, ±, %, ÷ (√ up)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            ModernButton(
                text = "C",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onClear() },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "±",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("-(") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "%",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("%") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "÷",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("/") },
                directionTexts = DirectionTexts(up = "√"),
                onSwipeUp = { actions.onOperatorClick("√") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 2: 7 (sin), 8 (cos), 9 (tan), ×
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            ModernButton(
                text = "7",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("7") },
                directionTexts = DirectionTexts(up = "sin"),
                onSwipeUp = { actions.onOperatorClick("sin") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "8",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("8") },
                directionTexts = DirectionTexts(up = "cos"),
                onSwipeUp = { actions.onOperatorClick("cos") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "9",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("9") },
                directionTexts = DirectionTexts(up = "tan"),
                onSwipeUp = { actions.onOperatorClick("tan") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "×",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("×") },
                directionTexts = DirectionTexts(up = "^", down = "x²"),
                onSwipeUp = { actions.onOperatorClick("^") },
                onSwipeDown = { actions.onOperatorClick("^2") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 3: 4 (log), 5 (!), 6 (°), −
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            ModernButton(
                text = "4",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("4") },
                directionTexts = DirectionTexts(up = "log"),
                onSwipeUp = { actions.onOperatorClick("log") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "5",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("5") },
                directionTexts = DirectionTexts(up = "!"),
                onSwipeUp = { actions.onOperatorClick("!") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "6",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("6") },
                directionTexts = DirectionTexts(up = "°"),
                onSwipeUp = { actions.onOperatorClick("°") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "−",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("−") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 4: 1 (ln), 2 (e), 3 (π), +
        // Mnemonic: ln(e)=1, e≈2.718, π≈3.14
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            ModernButton(
                text = "1",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("1") },
                directionTexts = DirectionTexts(up = "ln"),
                onSwipeUp = { actions.onOperatorClick("ln") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "2",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("2") },
                directionTexts = DirectionTexts(up = "e"),
                onSwipeUp = { actions.onOperatorClick("e") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "3",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("3") },
                directionTexts = DirectionTexts(up = "π"),
                onSwipeUp = { actions.onOperatorClick("π") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "+",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("+") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 5: (), 0 (log), ., ⌫
        // Mnemonic: log(1)=0
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            ModernButton(
                text = "( )",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onSpecialClick("()") },
                directionTexts = DirectionTexts(up = "(", down = ")"),
                onSwipeUp = { actions.onOperatorClick("(") },
                onSwipeDown = { actions.onOperatorClick(")") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = "0",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("0") },
                directionTexts = DirectionTexts(up = "000"),
                onSwipeUp = { actions.onNumberClick("000") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = ".",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick(".") },
                directionTexts = DirectionTexts(up = ","),
                onSwipeUp = { actions.onNumberClick(",") },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ModernButton(
                text = glyphBackspace,
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                onClick = { actions.onDelete() },
                onLongClick = { actions.onClear() },
                cornerRadius = buttonCornerRadius,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * Modern button with rounded corners for the modern keyboard layout.
 * Supports swipe gestures with direction text indicators.
 */
@Composable
private fun ModernButton(
    text: String,
    buttonType: ButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    directionTexts: DirectionTexts = DirectionTexts(),
    onLongClick: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val viewConfig = LocalViewConfiguration.current
    val haptics = LocalHapticFeedback.current
    val highContrast = LocalCalculatorHighContrast.current
    val hapticsEnabled = LocalCalculatorHapticsEnabled.current
    val longPressTimeout = viewConfig.longPressTimeoutMillis
    val touchSlop = viewConfig.touchSlop
    val density = LocalDensity.current
    val minDragDistancePx = with(density) { 20.dp.toPx() }

    val backgroundColor = when {
        isPressed -> when (buttonType) {
            ButtonType.DIGIT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            ButtonType.OPERATION -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
            ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
            ButtonType.CONTROL -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ButtonType.SPECIAL -> MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        }
        else -> when (buttonType) {
            ButtonType.DIGIT -> MaterialTheme.colorScheme.surfaceContainerHigh
            ButtonType.OPERATION -> MaterialTheme.colorScheme.secondaryContainer
            ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.tertiaryContainer
            ButtonType.CONTROL -> MaterialTheme.colorScheme.surfaceContainerHighest
            ButtonType.SPECIAL -> MaterialTheme.colorScheme.surface
        }
    }

    val textColor = when (buttonType) {
        ButtonType.DIGIT -> MaterialTheme.colorScheme.onSurface
        ButtonType.OPERATION -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.onTertiaryContainer
        ButtonType.CONTROL -> MaterialTheme.colorScheme.onSurface
        ButtonType.SPECIAL -> MaterialTheme.colorScheme.onSurface
    }

    val effectiveTextColor = if (highContrast) {
        MaterialTheme.colorScheme.onSurface
    } else {
        textColor
    }

    val directionTextColor = effectiveTextColor.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .pointerInput(onClick, onLongClick, onSwipeUp, onSwipeDown) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isPressed = true
                    val start = down.position
                    val downTime = down.uptimeMillis
                    var lastPos = start
                    var movedBeyondSlop = false
                    var longPressFired = false
                    var swipeHandled = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.changedToUpIgnoreConsumed()) {
                            if (!longPressFired && !swipeHandled) {
                                // Check for swipe
                                val delta = lastPos - start
                                val distance = delta.getDistance()
                                if (distance > minDragDistancePx) {
                                    // Determine swipe direction (vertical only for modern buttons)
                                    if (kotlin.math.abs(delta.y) > kotlin.math.abs(delta.x)) {
                                        if (delta.y < 0 && onSwipeUp != null) {
                                            if (hapticsEnabled) {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                            onSwipeUp()
                                            swipeHandled = true
                                        } else if (delta.y > 0 && onSwipeDown != null) {
                                            if (hapticsEnabled) {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                            onSwipeDown()
                                            swipeHandled = true
                                        }
                                    }
                                }
                                if (!swipeHandled) {
                                    if (hapticsEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onClick()
                                }
                            }
                            isPressed = false
                            break
                        }

                        if (change.positionChanged()) {
                            lastPos = change.position
                            if (!movedBeyondSlop) {
                                movedBeyondSlop = (lastPos - start).getDistance() > touchSlop
                            }
                            change.consume()
                        }

                        if (onLongClick != null &&
                            !longPressFired &&
                            !movedBeyondSlop &&
                            (change.uptimeMillis - downTime) >= longPressTimeout
                        ) {
                            longPressFired = true
                            if (hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onLongClick()
                        }
                    }
                    if (isPressed) {
                        isPressed = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Direction text indicators
        directionTexts.up?.let { upText ->
            Text(
                text = upText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = directionTextColor,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontFamily = CalculatorFontFamily
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )
        }
        directionTexts.down?.let { downText ->
            Text(
                text = downText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = directionTextColor,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontFamily = CalculatorFontFamily
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            )
        }

        // Main text
        Text(
            text = text,
            style = TextStyle(
                fontSize = 32.sp,
                color = effectiveTextColor,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                fontFamily = CalculatorFontFamily
            )
        )
    }
}
