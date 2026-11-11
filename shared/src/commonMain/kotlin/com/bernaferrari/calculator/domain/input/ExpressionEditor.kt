package com.bernaferrari.calculator.domain.input

import com.bernaferrari.calculator.domain.math.CalcEngine
import com.bernaferrari.calculator.domain.math.CalcError
import com.bernaferrari.calculator.domain.math.CalcException
import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.domain.math.Expression
import com.bernaferrari.calculator.domain.math.SymbolicNumber
import com.bernaferrari.calculator.domain.math.Tokenizer
import com.bernaferrari.calculator.domain.math.UserFunctions
import com.bernaferrari.calculator.domain.model.AngleUnit

/** An expression being typed, with the cursor as an index into [text]. */
data class Editor(
    val text: String = "",
    val cursor: Int = text.length,
    internal val answer: AnswerOperand? = null,
    internal val savedExpression: String? = null,
) {
    val left: Char? get() = text.getOrNull(cursor - 1)
    val isEmpty: Boolean get() = text.isEmpty()

    fun saveAnswer(): String? = answer?.let {
        val value = it.value.v
        val error = it.value.error?.let { radius -> "${radius.unscaled},${radius.scale()}" }.orEmpty()
        val stored = "${it.start}:${it.end}:$error:${value.scale()}:${value.unscaled}"
        val symbol = it.value.symbolic?.save().orEmpty()
        val tree = it.value.tree?.let(Expression::save)
        if (tree != null) "$stored:$symbol:$tree" else if (symbol.isNotEmpty()) "$stored:$symbol" else stored
    }

    fun restoreAnswer(saved: String?): Editor {
        val fields = saved?.split(':', limit = 7) ?: return this
        if (fields.size < 5) return this
        val (savedStart, savedEnd, savedError, savedScale, coefficient) = fields
        val start = savedStart.toIntOrNull() ?: return this
        val end = savedEnd.toIntOrNull() ?: return this
        if (start !in 0 until end || end > text.length) return this
        val error = when (savedError) {
            "", "false" -> null
            "true" -> Decimal.ZERO
            else -> {
                val fields = savedError.split(',')
                if (fields.size != 2) return this
                val scale = fields[1].toIntOrNull() ?: return this
                runCatching { Decimal.fromUnscaled(fields[0], scale) }.getOrNull()?.takeIf { it.signum() >= 0 } ?: return this
            }
        }
        val scale = savedScale.toIntOrNull() ?: return this
        val value = runCatching { Decimal.fromUnscaled(coefficient, scale) }.getOrNull() ?: return this
        val symbolic = fields.getOrNull(5)?.let(SymbolicNumber::restore)
        return copy(answer = AnswerOperand(start, end, CalcEngine.Value(value, error = error, symbolic = symbolic,
            tree = fields.getOrNull(6)?.let(Expression::restore))))
    }

    internal fun evaluate(
        expression: String, angle: AngleUnit, checkCancelled: () -> Unit,
        displaySettled: ((CalcEngine.Value) -> Boolean)? = null,
        timeBudgetMillis: Long = 600,
    ): CalcEngine.Value {
        if (expression == text) {
            savedExpression?.let(Expression::restore)?.let { tree ->
                return CalcEngine.evaluateValue(Tokenizer.BOUND_ANSWER.toString(), angle,
                    CalcEngine.Value(Decimal.ZERO, tree = tree), checkCancelled, displaySettled, timeBudgetMillis)
            }
        }
        val operand = answer
        if (Tokenizer.BOUND_ANSWER in expression) throw CalcException(CalcError.SYNTAX)
        val bound = if (operand == null) expression else
            expression.replaceRange(operand.start, operand.end, Tokenizer.BOUND_ANSWER.toString())
        return CalcEngine.evaluateValue(bound, angle, operand?.value, checkCancelled, displaySettled, timeBudgetMillis)
    }

    internal fun wrapAnswer(): Editor = Editor("($text)", answer = answer?.shift(1))

    companion object {
        /** Keep the unrounded value when an older tape row reaches the recipe storage budget. */
        internal fun withoutTree(saved: String?): String? = saved?.split(':', limit = 7)?.take(6)?.joinToString(":")
    }
}

/** The displayed number's range and its unrounded value. */
@ConsistentCopyVisibility
data class AnswerOperand internal constructor(internal val start: Int, internal val end: Int, internal val value: CalcEngine.Value) {
    internal fun shift(by: Int): AnswerOperand = copy(start = start + by, end = end + by)
}

/**
 * Pure editing rules for the calculator keyboard. Every function takes an [Editor] and returns the
 * edited one, never throwing: a key that makes no sense in context simply does nothing.
 */
object ExpressionEditor {
    private const val OPERATORS = "+−×÷^"
    private const val POSTFIX_END = "!%²³¹"
    private const val PREFIX_FUNCTIONS = "√∛"

    /** Names that are inserted as `name(` and deleted as a unit, the person's own included. */
    private val BUILT_IN = Tokenizer.FUNCTIONS
    private val FUNCTIONS: List<String>
        get() = (BUILT_IN + UserFunctions.all.map { it.name }).sortedByDescending { it.length }

    /** True if the character can end an operand, so an operator or `)` may follow it. */
    fun endsOperand(c: Char?): Boolean = c != null && (c.isDigit() || c == '.' || c == ')' || c == 'π' || c == 'e' || c == 'x' || c == 'y' || c in "abcd" || c in POSTFIX_END)

    fun insertDigit(e: Editor, digit: Char): Editor {
        val start = numberStart(e)
        val number = e.text.substring(start, e.cursor)
        // "0" followed by a digit replaces the zero instead of producing "07".
        if (number == "0" && e.text.getOrNull(e.cursor)?.isDigit() != true) {
            if (digit == '0') return e
            return replace(e, start, e.cursor, digit.toString())
        }
        return insert(e, digit.toString())
    }

    fun insertDecimal(e: Editor): Editor {
        val start = numberStart(e)
        var end = e.cursor
        while (end < e.text.length && (e.text[end].isDigit() || e.text[end] == '.')) end++
        val number = e.text.substring(start, end)
        val before = e.text.getOrNull(start - 1)
        val exponent = before == 'E' || (before != null && before in "+−-" && e.text.getOrNull(start - 2) == 'E')
        if ('.' in number || exponent) return e
        return insert(e, if (e.left?.isDigit() == true) "." else "0.")
    }

    fun insertOperator(e: Editor, op: Char): Editor {
        val left = e.left
        return when {
            left == null || left == '(' || left in PREFIX_FUNCTIONS -> if (op == '−') insert(e, "−") else e
            left in OPERATORS -> {
                // "5×" then "−" means a negative factor; any other operator replaces the previous one.
                if (op == '−' && left in "×÷^") return insert(e, "−")
                var start = e.cursor
                while (start > 0 && e.text[start - 1] in OPERATORS) start--
                if (start == 0 || e.text[start - 1] == '(') return e
                replace(e, start, e.cursor, op.toString())
            }
            else -> insert(e, op.toString())
        }
    }

    fun insertPostfix(e: Editor, symbol: String): Editor = if (endsOperand(e.left) && e.left != '.') insert(e, symbol) else e

    fun insertFunction(e: Editor, name: String): Editor = insert(e, "$name(")

    fun insertRaw(e: Editor, text: String): Editor = insert(e, text)

    /** Inserts a whole factor at a token boundary, with visible multiplication on either side. */
    fun insertFactor(e: Editor, factor: String): Editor {
        val cursor = snap(e.text, e.cursor)
        var index = 0
        // Keep a numeric literal intact, including its exponent, when tapping inside it.
        while (index < cursor) {
            index = if (e.text[index] in "0123456789.") Tokenizer.numberEnd(e.text, index) else index + 1
        }
        val before = e.text.getOrNull(index - 1)
        val after = e.text.getOrNull(index)
        val prefix = if (endsOperand(before)) "×" else ""
        val suffix = if (after != null && (after in 'a'..'z' || after in "0123456789.(π√∛")) "×" else ""
        return replace(e, index, index, prefix + factor + suffix).let {
            it.copy(cursor = it.cursor - suffix.length)
        }
    }

    /** Inserts a number coming from a previous result, parenthesizing negatives where needed. */
    fun insertNumber(e: Editor, number: String): Editor {
        val needsParens = number.startsWith("−") && (e.left != null && e.left != '(')
        val needsTimes = e.left?.isDigit() == true || e.left == '.'
        return insert(e, (if (needsTimes) "×" else "") + if (needsParens) "($number)" else number)
    }

    fun insertParenthesis(e: Editor): Editor {
        val open = openParentheses(e.text.substring(0, e.cursor))
        return if (open > 0 && endsOperand(e.left)) insert(e, ")") else insert(e, "(")
    }

    fun insertClosingParenthesis(e: Editor): Editor {
        val open = openParentheses(e.text.substring(0, e.cursor))
        return if (open > 0 && endsOperand(e.left)) insert(e, ")") else e
    }

    fun delete(e: Editor): Editor {
        if (e.cursor == 0) return e
        val before = e.text.substring(0, e.cursor)
        val length = when {
            before.endsWith("⁻¹") -> 2
            before.endsWith("(") -> FUNCTIONS.firstOrNull { before.endsWith("$it(") }?.let { it.length + 1 } ?: 1
            else -> 1
        }
        return replace(e, e.cursor - length, e.cursor, "")
    }

    /** Moves the cursor one step left (−1) or right (+1), hopping over function names whole. */
    fun nudge(e: Editor, direction: Int): Editor {
        val before = e.text.substring(0, e.cursor)
        val after = e.text.substring(e.cursor)
        val step = if (direction < 0) {
            when {
                before.isEmpty() -> 0
                before.endsWith("⁻¹") -> -2
                before.endsWith("(") -> -(FUNCTIONS.firstOrNull { before.endsWith("$it(") }?.let { it.length + 1 } ?: 1)
                else -> -1
            }
        } else {
            when {
                after.isEmpty() -> 0
                after.startsWith("⁻¹") -> 2
                else -> FUNCTIONS.firstOrNull { after.startsWith("$it(") }?.let { it.length + 1 } ?: 1
            }
        }
        return e.copy(cursor = e.cursor + step)
    }

    fun moveCursor(e: Editor, position: Int): Editor = e.copy(cursor = snap(e.text, position.coerceIn(0, e.text.length)))

    fun openParentheses(text: String): Int =
        // Each side of a graph's equation balances its own parentheses.
        text.substringAfterLast('=').let { side -> side.count { it == '(' } - side.count { it == ')' } }

    /** Keeps the cursor out of the middle of a function name like `si|n(`. */
    private fun snap(text: String, position: Int): Int {
        for (name in FUNCTIONS) {
            var index = text.indexOf("$name(")
            while (index >= 0) {
                val end = index + name.length + 1
                if (position in (index + 1) until end) return end
                index = text.indexOf("$name(", index + 1)
            }
        }
        if (position > 0 && text.getOrNull(position - 1) == '⁻' && text.getOrNull(position) == '¹') return position + 1
        return position
    }

    private fun numberStart(e: Editor): Int {
        var i = e.cursor
        while (i > 0 && (e.text[i - 1].isDigit() || e.text[i - 1] == '.')) i--
        return i
    }

    /** Replaces [start, end) with [s], as cut and paste do; the cursor lands after it. */
    fun replaceRange(e: Editor, start: Int, end: Int, s: String): Editor {
        val from = start.coerceIn(0, e.text.length)
        return replace(e, from, end.coerceIn(from, e.text.length), s)
    }

    private fun insert(e: Editor, s: String): Editor = replace(e, e.cursor, e.cursor, s)

    private fun replace(e: Editor, start: Int, end: Int, s: String): Editor {
        val text = e.text.replaceRange(start, end, s)
        val answer = e.answer?.let {
            when {
                end <= it.start -> it.shift(s.length - (end - start))
                start >= it.end -> it
                else -> null
            }
        }?.takeIf {
            // Drop the binding when an adjacent edit joins the displayed number.
            val before = text.getOrNull(it.start - 1)
            val numberStart = it.start + if (text[it.start] == '−') 1 else 0
            (before == null || (!before.isDigit() && before != '.')) &&
                Tokenizer.numberEnd(text, numberStart) == it.end
        }
        return Editor(text, start + s.length, answer)
    }
}
