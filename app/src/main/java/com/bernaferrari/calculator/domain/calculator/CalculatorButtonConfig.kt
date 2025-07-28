package com.bernaferrari.calculator.domain.calculator

import com.bernaferrari.calculator.domain.model.ButtonFunction
import com.bernaferrari.calculator.domain.model.ButtonType
import com.bernaferrari.calculator.domain.model.CalculatorButton

object CalculatorButtonConfig {
    val scientificButtons =
        listOf(
            // Row 1: Advanced functions
            listOf(
                CalculatorButton("inv", "inv", null, ButtonType.FUNCTION, ButtonFunction.CLEAR),
                CalculatorButton("rad", "rad", "deg", ButtonType.FUNCTION, ButtonFunction.CLEAR),
                CalculatorButton("fact", "x!", null, ButtonType.FUNCTION, ButtonFunction.FACTORIAL),
                CalculatorButton("paren", "(", ")", ButtonType.PARENTHESIS, ButtonFunction.PARENTHESIS),
                CalculatorButton("percent", "%", null, ButtonType.OPERATOR, ButtonFunction.PERCENT),
                CalculatorButton("ac", "AC", null, ButtonType.SPECIAL, ButtonFunction.CLEAR),
            ),
            // Row 2: Scientific functions (scrollable row)
            listOf(
                CalculatorButton("1", "1", "sin", ButtonType.NUMBER, ButtonFunction.ONE),
                CalculatorButton("2", "2", "cos", ButtonType.NUMBER, ButtonFunction.TWO),
                CalculatorButton("3", "3", "tan", ButtonType.NUMBER, ButtonFunction.THREE),
                CalculatorButton("x", "×", null, ButtonType.OPERATOR, ButtonFunction.MULTIPLY),
                CalculatorButton("div", "÷", null, ButtonType.OPERATOR, ButtonFunction.DIVIDE),
                CalculatorButton("del", "⌫", null, ButtonType.SPECIAL, ButtonFunction.BACKSPACE),
            ),
            // Row 3: More scientific functions (scrollable row)
            listOf(
                CalculatorButton("4", "4", "ln", ButtonType.NUMBER, ButtonFunction.FOUR),
                CalculatorButton("5", "5", "log", ButtonType.NUMBER, ButtonFunction.FIVE),
                CalculatorButton("6", "6", "√", ButtonType.NUMBER, ButtonFunction.SIX),
                CalculatorButton("pow", "x^y", null, ButtonType.FUNCTION, ButtonFunction.POWER),
                CalculatorButton("square", "x²", null, ButtonType.FUNCTION, ButtonFunction.SQUARE),
                CalculatorButton("plus", "+", null, ButtonType.OPERATOR, ButtonFunction.PLUS),
            ),
            // Row 4: Variables and constants (scrollable row)
            listOf(
                CalculatorButton("7", "7", "π", ButtonType.NUMBER, ButtonFunction.SEVEN),
                CalculatorButton("8", "8", "e", ButtonType.NUMBER, ButtonFunction.EIGHT),
                CalculatorButton("9", "9", "x", ButtonType.NUMBER, ButtonFunction.NINE),
                CalculatorButton("recip", "1/x", null, ButtonType.FUNCTION, ButtonFunction.RECIPROCAL),
                CalculatorButton("cube", "x³", null, ButtonType.FUNCTION, ButtonFunction.CUBE),
                CalculatorButton("minus", "−", null, ButtonType.OPERATOR, ButtonFunction.MINUS),
            ),
            // Row 5: Basic numbers and operations
            listOf(
                CalculatorButton("0", "0", null, ButtonType.NUMBER, ButtonFunction.ZERO),
                CalculatorButton("dot", ".", null, ButtonType.SPECIAL, ButtonFunction.DOT),
                CalculatorButton("ans", "Ans", null, ButtonType.SPECIAL, ButtonFunction.ANS),
                CalculatorButton("equals", "=", null, ButtonType.SPECIAL, ButtonFunction.EQUALS),
            ),
        )

    fun getButtonsForRow(rowIndex: Int): List<CalculatorButton> =
        if (rowIndex < scientificButtons.size) {
            scientificButtons[rowIndex]
        } else {
            emptyList()
        }

    fun getScrollableButtons(): List<List<CalculatorButton>> {
        return scientificButtons.drop(1).dropLast(1) // Exclude first and last rows
    }

    fun getSecondaryFunction(buttonId: String): ButtonFunction? =
        when (buttonId) {
            "1" -> ButtonFunction.SIN
            "2" -> ButtonFunction.COS
            "3" -> ButtonFunction.TAN
            "4" -> ButtonFunction.LN
            "5" -> ButtonFunction.LOG
            "6" -> ButtonFunction.SQRT
            "7" -> ButtonFunction.PI
            "8" -> ButtonFunction.E
            "9" -> ButtonFunction.X_VAR
            else -> null
        }

    fun getButtonForFunction(function: ButtonFunction): CalculatorButton? = scientificButtons.flatten().find { it.function == function }
}
