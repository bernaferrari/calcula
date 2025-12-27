package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.solovyev.android.calculator.R

/**
 * Keyboard mode - simple or engineer
 */
enum class KeyboardMode {
    SIMPLE,
    ENGINEER
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
