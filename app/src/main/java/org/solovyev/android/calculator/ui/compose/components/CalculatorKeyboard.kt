/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

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
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Row 1: Left, Right, %, Erase, Clear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Left arrow
            CalculatorButton(
                text = "◁",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = "<<",
                    down = "\uE001" // copy glyph
                ),
                onClick = { actions.onCursorLeft() },
                onSwipeUp = { actions.onCursorToStart() },
                onSwipeDown = { actions.onCopy() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Right arrow
            CalculatorButton(
                text = "▷",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = ">>",
                    down = "\uE000" // paste glyph
                ),
                onClick = { actions.onCursorRight() },
                onSwipeUp = { actions.onCursorToEnd() },
                onSwipeDown = { actions.onPaste() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Percent
            CalculatorButton(
                text = "%",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("%") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Erase/Backspace
            CalculatorButton(
                text = "\uE004", // backspace glyph
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                onClick = { actions.onDelete() },
                onLongClick = { actions.onClear() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Clear
            CalculatorButton(
                text = "C",
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                onClick = { actions.onClear() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 2: 7, 8, 9, /, Memory
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 7
            CalculatorButton(
                text = "7",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "i",
                    down = "!",
                    left = "0b:"
                ),
                onClick = { actions.onNumberClick("7") },
                onSwipeUp = { actions.onFunctionClick("i") },
                onSwipeDown = { actions.onOperatorClick("!") },
                onSwipeLeft = { actions.onSpecialClick("0b:") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // 8
            CalculatorButton(
                text = "8",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "π",
                    down = "n!",
                    left = "0x:"
                ),
                onClick = { actions.onNumberClick("8") },
                onSwipeUp = { actions.onFunctionClick("π") },
                onSwipeDown = { actions.onFunctionClick("n!") },
                onSwipeLeft = { actions.onSpecialClick("0x:") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // 9
            CalculatorButton(
                text = "9",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "e",
                    down = "|",
                    left = "0o:"
                ),
                onClick = { actions.onNumberClick("9") },
                onSwipeUp = { actions.onFunctionClick("e") },
                onSwipeDown = { actions.onOperatorClick("|") },
                onSwipeLeft = { actions.onSpecialClick("0o:") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Division
            CalculatorButton(
                text = "/",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(
                    up = "√"
                ),
                onClick = { actions.onOperatorClick("/") },
                onSwipeUp = { actions.onFunctionClick("√") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Memory
            CalculatorButton(
                text = "M",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = "M+",
                    down = "M-"
                ),
                fontWeight = FontWeight.Bold,
                onClick = { actions.onMemoryRecall() },
                onSwipeUp = { actions.onMemoryPlus() },
                onSwipeDown = { actions.onMemoryMinus() },
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
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 4
            CalculatorButton(
                text = "4",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "x",
                    down = "y",
                    left = "D"
                ),
                onClick = { actions.onNumberClick("4") },
                onSwipeUp = { actions.onFunctionClick("x") },
                onSwipeDown = { actions.onFunctionClick("y") },
                onSwipeLeft = { actions.onFunctionClick("D") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // 5
            CalculatorButton(
                text = "5",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "t",
                    down = "j",
                    left = "E"
                ),
                onClick = { actions.onNumberClick("5") },
                onSwipeUp = { actions.onFunctionClick("t") },
                onSwipeDown = { actions.onFunctionClick("j") },
                onSwipeLeft = { actions.onFunctionClick("E") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // 6
            CalculatorButton(
                text = "6",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "∞",
                    down = "&",
                    left = "F"
                ),
                onClick = { actions.onNumberClick("6") },
                onSwipeUp = { actions.onFunctionClick("∞") },
                onSwipeDown = { actions.onOperatorClick("&") },
                onSwipeLeft = { actions.onFunctionClick("F") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Multiplication
            CalculatorButton(
                text = "×",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(
                    up = "^",
                    down = "^2"
                ),
                onClick = { actions.onOperatorClick("×") },
                onSwipeUp = { actions.onOperatorClick("^") },
                onSwipeDown = { actions.onOperatorClick("^2") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Variables
            CalculatorButton(
                text = "π",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onOpenVars() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 4: 1, 2, 3, -, Functions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 1
            CalculatorButton(
                text = "1",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "sin",
                    down = "asin",
                    left = "A"
                ),
                onClick = { actions.onNumberClick("1") },
                onSwipeUp = { actions.onFunctionClick("sin") },
                onSwipeDown = { actions.onFunctionClick("asin") },
                onSwipeLeft = { actions.onFunctionClick("A") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // 2
            CalculatorButton(
                text = "2",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "cos",
                    down = "acos",
                    left = "B"
                ),
                onClick = { actions.onNumberClick("2") },
                onSwipeUp = { actions.onFunctionClick("cos") },
                onSwipeDown = { actions.onFunctionClick("acos") },
                onSwipeLeft = { actions.onFunctionClick("B") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // 3
            CalculatorButton(
                text = "3",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "tan",
                    down = "atan",
                    left = "C"
                ),
                onClick = { actions.onNumberClick("3") },
                onSwipeUp = { actions.onFunctionClick("tan") },
                onSwipeDown = { actions.onFunctionClick("atan") },
                onSwipeLeft = { actions.onFunctionClick("C") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Minus
            CalculatorButton(
                text = "−",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(
                    up = "∂"
                ),
                onClick = { actions.onOperatorClick("−") },
                onSwipeUp = { actions.onOperatorClick("∂") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Functions
            CalculatorButton(
                text = "ƒ",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = "+ƒ",
                    down = "+π"
                ),
                fontStyle = FontStyle.Italic,
                onClick = { actions.onOpenFunctions() },
                onSwipeUp = { actions.onSpecialClick("add_function") },
                onSwipeDown = { actions.onSpecialClick("add_var") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 5: (), 0, ., +, History
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Round brackets
            CalculatorButton(
                text = "( )",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "(",
                    down = ")",
                    left = "(…)"
                ),
                onClick = { actions.onSpecialClick("()") },
                onSwipeUp = { actions.onSpecialClick("(") },
                onSwipeDown = { actions.onSpecialClick(")") },
                onSwipeLeft = { actions.onSpecialClick("(…)") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // 0
            CalculatorButton(
                text = "0",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "ln",
                    down = "lg",
                    left = "°"
                ),
                onClick = { actions.onNumberClick("0") },
                onSwipeUp = { actions.onFunctionClick("ln") },
                onSwipeDown = { actions.onFunctionClick("lg") },
                onSwipeLeft = { actions.onOperatorClick("°") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Period/Decimal point
            CalculatorButton(
                text = ".",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = ",",
                    down = "≈"
                ),
                onClick = { actions.onNumberClick(".") },
                onSwipeUp = { actions.onOperatorClick(",") },
                onSwipeDown = { actions.onOperatorClick("≈") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Plus
            CalculatorButton(
                text = "+",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(
                    up = "°"
                ),
                onClick = { actions.onOperatorClick("+") },
                onSwipeUp = { actions.onOperatorClick("°") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // History
            CalculatorButton(
                text = "\uE005", // history glyph
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(
                    up = "↶",
                    down = "↷"
                ),
                onClick = { actions.onOpenHistory() },
                onSwipeUp = { actions.onSpecialClick("undo") },
                onSwipeDown = { actions.onSpecialClick("redo") },
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
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Row 1: Clear, Erase
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CalculatorButton(
                text = "C",
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                onClick = { actions.onClear() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CalculatorButton(
                text = "\uE004",
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                onClick = { actions.onDelete() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 2: 7, 8, 9, /
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
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
        }

        // Row 3: 4, 5, 6, *
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
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
        }

        // Row 4: 1, 2, 3, -
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
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
        }

        // Row 5: 0, ., =, +
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
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
                text = "=",
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                onClick = { actions.onEquals() },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalculatorButton(
                text = "+",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("+") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}
