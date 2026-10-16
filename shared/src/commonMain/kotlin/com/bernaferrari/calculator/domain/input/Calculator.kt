package com.bernaferrari.calculator.domain.input

import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.domain.math.RoundingMode

import com.bernaferrari.calculator.domain.math.CalcEngine
import com.bernaferrari.calculator.domain.math.CalcError
import com.bernaferrari.calculator.domain.math.CalcException
import com.bernaferrari.calculator.domain.math.NumberFormatter
import com.bernaferrari.calculator.domain.math.Tokenizer
import com.bernaferrari.calculator.domain.model.AUTO_DECIMALS
import com.bernaferrari.calculator.domain.model.AngleUnit

enum class Key {
    D0, D1, D2, D3, D4, D5, D6, D7, D8, D9, DOT,
    PLUS, MINUS, TIMES, DIVIDE, PERCENT,
    PARENS, LPAREN, RPAREN,
    CLEAR, DELETE, EQUALS,
    SQRT, PI, E, POWER, FACTORIAL, SQUARE,
    SIN, COS, TAN, LN, LOG,
    ASIN, ACOS, ATAN, EXP, POW10, CBRT, CUBE, RECIPROCAL,
    DOUBLE_ZERO, TRIPLE_ZERO,
    INVERSE,
}

data class CalcState(
    val editor: Editor = Editor(),
    /** The editor holds the result of the last `=`; typing a digit starts over, an operator continues. */
    val resultShown: Boolean = false,
    val inverse: Boolean = false,
    val angleUnit: AngleUnit = AngleUnit.DEGREES,
    val error: CalcError? = null,
    /** The trailing `+3` of the last calculation, so pressing `=` again repeats it. */
    val repeatTail: String? = null,
    /** Decimals to round results to, or [AUTO_DECIMALS]. */
    val decimalPlaces: Int = AUTO_DECIMALS,
)

sealed interface Outcome {
    data object None : Outcome
    data object Cleared : Outcome
    data class Evaluated(val expression: String, val result: String) : Outcome
    data class Failed(val error: CalcError) : Outcome
}

/** The calculator's state machine: a key press in, the next state (and what happened) out. */
object Calculator {
    fun press(state: CalcState, key: Key): Pair<CalcState, Outcome> {
        if (key == Key.EQUALS) return evaluate(state)
        if (key == Key.CLEAR) return state.copy(editor = Editor(), resultShown = false, error = null) to Outcome.Cleared
        if (key == Key.INVERSE) return state.copy(inverse = !state.inverse) to Outcome.None

        val continues = key in CONTINUING_KEYS
        var editor = state.editor
        if (state.resultShown && !continues && key != Key.DELETE) editor = Editor()
        if (state.resultShown && continues && editor.text.startsWith("−") && key != Key.PLUS && key != Key.MINUS &&
            key != Key.TIMES && key != Key.DIVIDE
        ) {
            // (−5)² rather than −5², which would square first.
            editor = Editor("(${editor.text})")
        }

        val edited = edit(editor, key, state.inverse)
        return state.copy(editor = edited, resultShown = false, error = null) to Outcome.None
    }

    /** Puts a number (a previous result) at the cursor, replacing a just-shown result. */
    fun insertNumber(state: CalcState, number: String): CalcState {
        val editor = if (state.resultShown) Editor() else state.editor
        return state.copy(editor = ExpressionEditor.insertNumber(editor, number), resultShown = false, error = null)
    }

    /** The value shown under the expression while typing, or null when it would add nothing. */
    fun preview(text: String, angleUnit: AngleUnit, decimalPlaces: Int = AUTO_DECIMALS): String? {
        if (isPlainNumber(text)) return null
        var candidate = text
        // While an operator is dangling ("5+3×"), preview what's complete so far.
        while (candidate.isNotEmpty() && (candidate.last() in "+−×÷^(" || candidate.last() in "√∛")) {
            candidate = candidate.dropLast(1)
        }
        if (candidate.isEmpty() || isPlainNumber(candidate)) return null
        val value = CalcEngine.evaluateOrNull(candidate, angleUnit) ?: return null
        return formatResult(value, decimalPlaces)
    }

    fun isPlainNumber(text: String): Boolean {
        val body = text.removePrefix("−")
        if (body.isEmpty()) return text.isEmpty()
        return Tokenizer.numberEnd(body, 0) == body.length
    }

    private val CONTINUING_KEYS = setOf(
        Key.PLUS, Key.MINUS, Key.TIMES, Key.DIVIDE, Key.PERCENT, Key.POWER, Key.FACTORIAL, Key.SQUARE, Key.RPAREN,
        Key.CUBE, Key.RECIPROCAL,
    )

    private fun evaluate(state: CalcState): Pair<CalcState, Outcome> {
        val text = state.editor.text
        if (state.resultShown && state.repeatTail != null) {
            return evaluate(state.copy(editor = Editor(parenthesizeNegative(text) + state.repeatTail), resultShown = false))
        }
        if (text.isEmpty() || state.resultShown || isPlainNumber(text)) return state to Outcome.None
        val closed = text + ")".repeat(ExpressionEditor.openParentheses(text).coerceAtLeast(0))
        return try {
            val result = formatResult(CalcEngine.evaluate(closed, state.angleUnit), state.decimalPlaces)
            state.copy(editor = Editor(result), resultShown = true, error = null, repeatTail = repeatTail(closed)) to
                Outcome.Evaluated(closed, result)
        } catch (e: CalcException) {
            state.copy(error = e.error) to Outcome.Failed(e.error)
        } catch (e: ArithmeticException) {
            state.copy(error = CalcError.OVERFLOW) to Outcome.Failed(CalcError.OVERFLOW)
        }
    }

    /** Rounds to the chosen number of decimals (keeping very large/small values in E notation). */
    fun formatResult(value: Decimal, decimalPlaces: Int): String {
        if (decimalPlaces < 0 || value.signum() == 0) return NumberFormatter.format(value)
        val magnitude = value.precision() - value.scale() - 1
        if (magnitude >= 16 || magnitude <= -10) return NumberFormatter.format(value)
        return NumberFormatter.format(value.setScale(decimalPlaces, RoundingMode.HALF_UP))
    }

    /** `2×(1+2)+3` → `+3`: a final top-level operator and plain number, if there is one. */
    private fun repeatTail(expression: String): String? {
        val match = Regex("([+−×÷^])(\\d+(?:\\.\\d+)?(?:E[−+]?\\d+)?)$").find(expression) ?: return null
        val before = expression.substring(0, match.range.first)
        if (before.isEmpty() || ExpressionEditor.openParentheses(before) != 0) return null
        return match.value
    }

    private fun parenthesizeNegative(text: String) = if (text.startsWith("−")) "($text)" else text

    private fun edit(e: Editor, key: Key, inverse: Boolean): Editor = with(ExpressionEditor) {
        when (key) {
            Key.D0 -> insertDigit(e, '0')
            Key.D1 -> insertDigit(e, '1')
            Key.D2 -> insertDigit(e, '2')
            Key.D3 -> insertDigit(e, '3')
            Key.D4 -> insertDigit(e, '4')
            Key.D5 -> insertDigit(e, '5')
            Key.D6 -> insertDigit(e, '6')
            Key.D7 -> insertDigit(e, '7')
            Key.D8 -> insertDigit(e, '8')
            Key.D9 -> insertDigit(e, '9')
            Key.DOT -> insertDecimal(e)
            Key.PLUS -> insertOperator(e, '+')
            Key.MINUS -> insertOperator(e, '−')
            Key.TIMES -> insertOperator(e, '×')
            Key.DIVIDE -> insertOperator(e, '÷')
            Key.PERCENT -> insertPostfix(e, "%")
            Key.PARENS -> insertParenthesis(e)
            Key.LPAREN -> insertRaw(e, "(")
            Key.RPAREN -> insertClosingParenthesis(e)
            Key.DELETE -> delete(e)
            Key.SQRT -> insertRaw(e, if (inverse) "∛" else "√")
            Key.PI -> insertRaw(e, "π")
            Key.E -> insertRaw(e, "e")
            Key.POWER -> if (inverse) insertPostfix(e, "⁻¹") else insertOperator(e, '^')
            Key.FACTORIAL -> insertPostfix(e, "!")
            Key.SQUARE -> insertPostfix(e, if (inverse) "³" else "²")
            Key.SIN -> insertFunction(e, if (inverse) "sin⁻¹" else "sin")
            Key.COS -> insertFunction(e, if (inverse) "cos⁻¹" else "cos")
            Key.TAN -> insertFunction(e, if (inverse) "tan⁻¹" else "tan")
            Key.LN -> if (inverse) insertRaw(e, "e^(") else insertFunction(e, "ln")
            Key.LOG -> if (inverse) insertRaw(e, if (e.left?.isDigit() == true || e.left == '.') "×10^(" else "10^(") else insertFunction(e, "log")
            Key.ASIN -> insertFunction(e, "sin⁻¹")
            Key.ACOS -> insertFunction(e, "cos⁻¹")
            Key.ATAN -> insertFunction(e, "tan⁻¹")
            Key.EXP -> insertRaw(e, "e^(")
            Key.POW10 -> insertRaw(e, if (e.left?.isDigit() == true || e.left == '.') "×10^(" else "10^(")
            Key.CBRT -> insertRaw(e, "∛")
            Key.CUBE -> insertPostfix(e, "³")
            Key.RECIPROCAL -> insertPostfix(e, "⁻¹")
            Key.DOUBLE_ZERO -> insertDigit(insertDigit(e, '0'), '0')
            Key.TRIPLE_ZERO -> insertDigit(insertDigit(insertDigit(e, '0'), '0'), '0')
            Key.EQUALS, Key.CLEAR, Key.INVERSE -> e
        }
    }
}
