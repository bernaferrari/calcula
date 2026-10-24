package com.bernaferrari.calculator.domain.input

import com.bernaferrari.calculator.domain.model.Notation
import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.domain.math.RoundingMode

import com.bernaferrari.calculator.domain.math.CalcEngine
import com.bernaferrari.calculator.domain.math.CalcError
import com.bernaferrari.calculator.domain.math.CalcException
import com.bernaferrari.calculator.domain.math.NumberFormatter
import com.bernaferrari.calculator.domain.math.Tokenizer
import com.bernaferrari.calculator.domain.math.Fractions
import com.bernaferrari.calculator.domain.model.AUTO_DECIMALS
import com.bernaferrari.calculator.domain.model.AngleUnit

enum class Key {
    D0, D1, D2, D3, D4, D5, D6, D7, D8, D9, DOT,
    PLUS, MINUS, TIMES, DIVIDE, PERCENT,
    PARENS, LPAREN, RPAREN,
    CLEAR, DELETE, EQUALS,
    SQRT, PI, E, POWER, FACTORIAL, SQUARE,
    SIN, COS, TAN, LN, LOG,
    ASIN, ACOS, ATAN, EXP, POW10, CBRT, CUBE, RECIPROCAL, ABS,
    /** The y-th root of what's before it: `8`, ʸ√x, `3` is `8^(1÷3)`. */
    ROOT,
    DOUBLE_ZERO, TRIPLE_ZERO,
    INVERSE,
    /** The graph's variables `x` and `y`, and the `=` of an equation. */
    VARIABLE,
    VARIABLE_Y,
    EQUATION,
    /** The graph's slider parameters. */
    PARAM_A,
    PARAM_B,
    PARAM_C,
    PARAM_D,
    /** RPN's change sign (CHS); algebraic entry types a minus instead. */
    NEGATE,
}

/** A selected stretch of the expression, [start, end) in its raw text; empty when only a spot is picked. */
data class Span(val start: Int, val end: Int) {
    val isEmpty: Boolean get() = start == end
}

data class CalcState(
    val editor: Editor = Editor(),
    /** Text picked in the display (double-tap, hold): the next key acts on it, like in any text field. */
    val selection: Span? = null,
    /** The editor holds the result of the last `=`; typing a digit starts over, an operator continues. */
    val resultShown: Boolean = false,
    val inverse: Boolean = false,
    val angleUnit: AngleUnit = AngleUnit.DEGREES,
    val error: CalcError? = null,
    /** The trailing `+3` of the last calculation, so pressing `=` again repeats it. */
    val repeatTail: String? = null,
    /** Decimals to round results to, or [AUTO_DECIMALS]. */
    val decimalPlaces: Int = AUTO_DECIMALS,
    /** The last result as a simple fraction (`³⁄₈`), when it is one; shown while [resultShown]. */
    val fraction: String? = null,
    val notation: Notation = Notation.NORMAL,
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
        state.selection?.let { return pressOnSelection(state.copy(selection = null), key, it) }
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

    /**
     * A key with text selected does what it would in a text field, made calculator-shaped: `( )`
     * and functions wrap it, powers and postfixes apply to it as a whole, ⌫ deletes it, and
     * anything else types over it.
     */
    private fun pressOnSelection(state: CalcState, key: Key, selection: Span): Pair<CalcState, Outcome> {
        val text = state.editor.text
        val start = selection.start.coerceIn(0, text.length)
        val end = selection.end.coerceIn(start, text.length)
        val inner = text.substring(start, end)
        if (inner.isEmpty() || key in SELECTION_IGNORED) return press(state, key)
        fun put(replacement: String) =
            state.copy(editor = ExpressionEditor.replaceRange(state.editor, start, end, replacement), resultShown = false, error = null) to Outcome.None
        return when (key) {
            Key.PARENS, Key.LPAREN, Key.RPAREN -> put("($inner)")
            Key.DELETE -> put("")
            in WRAP_PREFIX -> {
                val name = edit(Editor(), key, state.inverse).text
                put((if (name.endsWith("(")) name else "$name(") + inner + ")")
            }
            in WRAP_POSTFIX -> {
                val suffix = edit(Editor("1"), key, state.inverse).text.removePrefix("1")
                put((if (isPlainNumber(inner) || isWrapped(inner)) inner else "($inner)") + suffix)
            }
            else -> press(state.copy(editor = ExpressionEditor.replaceRange(state.editor, start, end, ""), resultShown = false), key)
        }
    }

    /** True for `(…)` where the first parenthesis closes at the very end. */
    private fun isWrapped(text: String): Boolean {
        if (!text.startsWith("(") || !text.endsWith(")")) return false
        var depth = 0
        text.forEachIndexed { i, c ->
            if (c == '(') depth++
            if (c == ')') depth--
            if (depth == 0 && i < text.lastIndex) return false
        }
        return true
    }

    private val SELECTION_IGNORED = setOf(Key.EQUALS, Key.CLEAR, Key.INVERSE)
    private val WRAP_PREFIX = setOf(
        Key.SIN, Key.COS, Key.TAN, Key.ASIN, Key.ACOS, Key.ATAN, Key.LN, Key.LOG, Key.ABS,
        Key.SQRT, Key.CBRT, Key.EXP, Key.POW10,
    )
    private val WRAP_POSTFIX = setOf(Key.SQUARE, Key.CUBE, Key.FACTORIAL, Key.RECIPROCAL, Key.PERCENT, Key.POWER, Key.ROOT)

    /**
     * Keys that would leave the expression as it is right now, like `1/x` with nothing to invert.
     * Asking the editor itself keeps this exact: it is the same rule that ignores the key.
     */
    fun inertKeys(state: CalcState): Set<Key> =
        Key.entries.filterTo(mutableSetOf()) { key ->
            key !in NEVER_INERT && press(state, key).first.editor == state.editor
        }

    private val NEVER_INERT = setOf(Key.EQUALS, Key.CLEAR, Key.INVERSE)

    /** Puts a number (a previous result) at the cursor, replacing a just-shown result. */
    fun insertNumber(state: CalcState, number: String): CalcState {
        val editor = if (state.resultShown) Editor() else state.editor
        return state.copy(editor = ExpressionEditor.insertNumber(editor, number), resultShown = false, error = null)
    }

    /** Puts text (a constant's value, a function's `name(`) at the cursor, replacing a just-shown result. */
    fun insertText(state: CalcState, text: String): CalcState {
        val number = isPlainNumber(text)
        if (number) return insertNumber(state, text)
        val editor = if (state.resultShown) Editor() else state.editor
        return state.copy(editor = ExpressionEditor.insertRaw(editor, text), resultShown = false, error = null, selection = null)
    }

    /** The value shown under the expression while typing, or null when it would add nothing. */
    fun preview(text: String, angleUnit: AngleUnit, decimalPlaces: Int = AUTO_DECIMALS, notation: Notation = Notation.NORMAL): String? {
        if (isPlainNumber(text)) return null
        var candidate = text
        // While an operator is dangling ("5+3×"), preview what's complete so far.
        while (candidate.isNotEmpty() && (candidate.last() in "+−×÷^(" || candidate.last() in "√∛")) {
            candidate = candidate.dropLast(1)
        }
        if (candidate.isEmpty() || isPlainNumber(candidate)) return null
        val value = CalcEngine.evaluateOrNull(candidate, angleUnit) ?: return null
        return formatResult(value, decimalPlaces, notation)
    }

    fun isPlainNumber(text: String): Boolean {
        val body = text.removePrefix("−")
        if (body.isEmpty()) return text.isEmpty()
        return Tokenizer.numberEnd(body, 0) == body.length
    }

    private val CONTINUING_KEYS = setOf(
        Key.PLUS, Key.MINUS, Key.TIMES, Key.DIVIDE, Key.PERCENT, Key.POWER, Key.FACTORIAL, Key.SQUARE, Key.RPAREN,
        Key.CUBE, Key.RECIPROCAL, Key.ROOT,
    )

    private fun evaluate(state: CalcState): Pair<CalcState, Outcome> {
        val text = state.editor.text
        if (state.resultShown && state.repeatTail != null) {
            return evaluate(state.copy(editor = Editor(parenthesizeNegative(text) + state.repeatTail), resultShown = false))
        }
        if (text.isEmpty() || state.resultShown || isPlainNumber(text)) return state to Outcome.None
        val closed = text + ")".repeat(ExpressionEditor.openParentheses(text).coerceAtLeast(0))
        return try {
            val value = CalcEngine.evaluate(closed, state.angleUnit)
            val result = formatResult(value, state.decimalPlaces, state.notation)
            val fraction = Fractions.worthShowing(value)?.let(Fractions::format)
            state.copy(editor = Editor(result), resultShown = true, error = null, repeatTail = repeatTail(closed), fraction = fraction) to
                Outcome.Evaluated(closed, result)
        } catch (e: CalcException) {
            state.copy(error = e.error) to Outcome.Failed(e.error)
        } catch (e: ArithmeticException) {
            state.copy(error = CalcError.OVERFLOW) to Outcome.Failed(CalcError.OVERFLOW)
        }
    }

    /** Rounds to the chosen number of decimals (keeping very large/small values in E notation). */
    fun formatResult(value: Decimal, decimalPlaces: Int, notation: Notation = Notation.NORMAL): String {
        if (notation != Notation.NORMAL) return NumberFormatter.scientific(value, notation == Notation.ENGINEERING, decimalPlaces)
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
            Key.VARIABLE -> insertRaw(e, "x")
            Key.VARIABLE_Y -> insertRaw(e, "y")
            Key.PARAM_A -> insertRaw(e, "a")
            Key.PARAM_B -> insertRaw(e, "b")
            Key.PARAM_C -> insertRaw(e, "c")
            Key.PARAM_D -> insertRaw(e, "d")
            Key.EQUATION -> if ('=' in e.text) e else insertRaw(e, "=")
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
            Key.ABS -> insertFunction(e, "abs")
            // Written as the power it is; the open parenthesis closes itself at `=`.
            Key.ROOT -> if (endsOperand(e.left) && e.left != '.') insertRaw(e, "^(1÷") else e
            Key.DOUBLE_ZERO -> insertDigit(insertDigit(e, '0'), '0')
            Key.TRIPLE_ZERO -> insertDigit(insertDigit(insertDigit(e, '0'), '0'), '0')
            Key.EQUALS, Key.CLEAR, Key.INVERSE, Key.NEGATE -> e
        }
    }
}
