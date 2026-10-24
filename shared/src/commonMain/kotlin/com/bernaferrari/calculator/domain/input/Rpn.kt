package com.bernaferrari.calculator.domain.input

import com.bernaferrari.calculator.domain.model.Notation
import com.bernaferrari.calculator.domain.math.CalcEngine
import com.bernaferrari.calculator.domain.math.CalcError
import com.bernaferrari.calculator.domain.math.CalcException
import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.domain.model.AUTO_DECIMALS
import com.bernaferrari.calculator.domain.model.AngleUnit

/**
 * Reverse Polish entry, the HP way: numbers go on a stack, ↵ (the `=` key) pushes, operators take
 * their operands off the top. The stack has no fixed depth; X is the top and is what's shown big.
 */
data class RpnState(
    /** X first, then Y, Z, T… */
    val stack: List<Decimal> = emptyList(),
    /** The number being typed into X, as typed (`12.`), or null when X holds a finished value. */
    val entry: String? = null,
    /** Whether typing a number pushes X up first: off right after ↵ and after clearing X. */
    val lift: Boolean = true,
    val error: CalcError? = null,
    val angleUnit: AngleUnit = AngleUnit.DEGREES,
    val decimalPlaces: Int = AUTO_DECIMALS,
    val notation: Notation = Notation.NORMAL,
)

sealed interface RpnOutcome {
    data object None : RpnOutcome
    data object Cleared : RpnOutcome
    /** An operation ran: [expression] is how it reads in the expression language, for the tape. */
    data class Evaluated(val expression: String, val result: String) : RpnOutcome
    data class Failed(val error: CalcError) : RpnOutcome
}

object Rpn {
    fun press(state: RpnState, key: Key): Pair<RpnState, RpnOutcome> {
        val s = state.copy(error = null)
        return when (key) {
            in DIGITS -> typed(s, DIGITS.getValue(key)) to RpnOutcome.None
            Key.DOUBLE_ZERO -> typed(typed(s, "0"), "0") to RpnOutcome.None
            Key.TRIPLE_ZERO -> typed(typed(typed(s, "0"), "0"), "0") to RpnOutcome.None
            Key.DOT -> typed(s, ".") to RpnOutcome.None
            Key.EQUALS -> {
                val c = commit(s)
                val x = c.stack.firstOrNull() ?: Decimal.ZERO
                c.copy(stack = listOf(x) + c.stack.ifEmpty { listOf(Decimal.ZERO) }, lift = false) to RpnOutcome.None
            }
            Key.CLEAR -> RpnState(angleUnit = s.angleUnit, decimalPlaces = s.decimalPlaces) to RpnOutcome.Cleared
            Key.DELETE -> delete(s) to RpnOutcome.None
            Key.PARENS -> commit(s).let { c -> c.copy(stack = swap(c.stack), lift = true) } to RpnOutcome.None
            Key.LPAREN -> commit(s).let { c -> c.copy(stack = c.stack.drop(1) + c.stack.take(1), lift = true) } to RpnOutcome.None
            Key.RPAREN -> commit(s).let { c -> c.copy(stack = c.stack.takeLast(1) + c.stack.dropLast(1), lift = true) } to RpnOutcome.None
            Key.NEGATE -> negate(s) to RpnOutcome.None
            Key.PI -> push(s, "π")
            Key.E -> push(s, "e")
            in BINARY -> binary(s, BINARY.getValue(key))
            in UNARY -> unary(s, UNARY.getValue(key))
            Key.PERCENT -> {
                // y × x%, keeping y: 200 ↵ 10 % gives 20 with 200 still there to add it to.
                val c = commit(s)
                val y = c.stack.getOrNull(1) ?: return c to RpnOutcome.None
                run(c, "${text(y)}×${text(c.stack.firstOrNull() ?: Decimal.ZERO)}÷100", consumed = 1)
            }
            else -> s to RpnOutcome.None
        }
    }

    /** Puts a number (a result from the tape, memory) on the stack. */
    fun pushNumber(state: RpnState, number: String): RpnState {
        val value = CalcEngine.evaluateOrNull(number) ?: return state
        val c = commit(state)
        val base = if (c.lift) c.stack else c.stack.drop(1)
        return c.copy(stack = listOf(value) + base, lift = true, error = null)
    }

    /** The finished value of X, for memory and the converter. */
    fun x(state: RpnState): Decimal? = commit(state).stack.firstOrNull()

    private fun typed(s: RpnState, c: String): RpnState {
        val entry = s.entry
        if (entry == null) {
            val base = if (s.lift) s.stack else s.stack.drop(1)
            val text = if (c == ".") "0." else c
            return s.copy(stack = listOf(parse(text)) + base, entry = text)
        }
        val next = when {
            c == "." && '.' in entry -> return s
            entry == "0" && c != "." -> c
            entry == "−0" && c != "." -> "−$c"
            else -> entry + c
        }
        if (next.count { it.isDigit() } > 40) return s
        return s.copy(stack = listOf(parse(next)) + s.stack.drop(1), entry = next)
    }

    private fun delete(s: RpnState): RpnState {
        val entry = s.entry
        if (entry == null) {
            // CLx: X becomes 0, and the next number takes its place.
            if (s.stack.isEmpty()) return s
            return s.copy(stack = listOf(Decimal.ZERO) + s.stack.drop(1), lift = false)
        }
        val next = entry.dropLast(1).let { if (it == "−") "" else it }
        if (next.isEmpty()) return s.copy(stack = listOf(Decimal.ZERO) + s.stack.drop(1), entry = null, lift = false)
        return s.copy(stack = listOf(parse(next)) + s.stack.drop(1), entry = next)
    }

    private fun negate(s: RpnState): RpnState {
        val entry = s.entry
        if (entry != null) {
            // Changing the sign mid-entry keeps typing going, like CHS on an HP.
            val next = if (entry.startsWith("−")) entry.drop(1) else "−$entry"
            return s.copy(stack = listOf(parse(next)) + s.stack.drop(1), entry = next)
        }
        val x = s.stack.firstOrNull() ?: return s
        return s.copy(stack = listOf(x.negate()) + s.stack.drop(1))
    }

    private fun push(s: RpnState, constant: String): Pair<RpnState, RpnOutcome> {
        val c = commit(s)
        val base = if (c.lift) c.stack else c.stack.drop(1)
        return c.copy(stack = listOf(CalcEngine.evaluate(constant)) + base, lift = true) to RpnOutcome.None
    }

    private fun binary(s: RpnState, template: String): Pair<RpnState, RpnOutcome> {
        val c = commit(s)
        val x = c.stack.getOrElse(0) { Decimal.ZERO }
        val y = c.stack.getOrElse(1) { Decimal.ZERO }
        return run(c, template.replace("Y", text(y)).replace("X", text(x)), consumed = 2)
    }

    private fun unary(s: RpnState, template: String): Pair<RpnState, RpnOutcome> {
        val c = commit(s)
        val x = c.stack.getOrElse(0) { Decimal.ZERO }
        return run(c, template.replace("X", text(x)), consumed = 1)
    }

    /** Evaluates [expression] and replaces the [consumed] top values with its result. */
    private fun run(c: RpnState, expression: String, consumed: Int): Pair<RpnState, RpnOutcome> = try {
        val value = CalcEngine.evaluate(expression, c.angleUnit)
        val result = Calculator.formatResult(value, c.decimalPlaces, c.notation)
        val shown = CalcEngine.evaluate(result)
        c.copy(stack = listOf(shown) + c.stack.drop(consumed), lift = true) to RpnOutcome.Evaluated(expression, result)
    } catch (e: CalcException) {
        c.copy(error = e.error) to RpnOutcome.Failed(e.error)
    } catch (e: ArithmeticException) {
        c.copy(error = CalcError.OVERFLOW) to RpnOutcome.Failed(CalcError.OVERFLOW)
    }

    /** Ends typing: X keeps the value it shows. */
    private fun commit(s: RpnState): RpnState = if (s.entry == null) s else s.copy(entry = null, lift = true)

    private fun swap(stack: List<Decimal>): List<Decimal> =
        if (stack.size < 2) stack else listOf(stack[1], stack[0]) + stack.drop(2)

    private fun parse(text: String): Decimal =
        Decimal(text.replace('−', '-').let { if (it.endsWith(".")) it.dropLast(1) else it }.ifEmpty { "0" }.let { if (it == "-") "0" else it })

    /** A value as an operand in the expression language: plain, negatives in parentheses. */
    private fun text(d: Decimal): String {
        val plain = Calculator.formatResult(d, AUTO_DECIMALS).let { if (d.precision() > 15) d.stripTrailingZeros().toPlainString().replace('-', '−') else it }
        return if (plain.startsWith("−")) "($plain)" else plain
    }

    private val DIGITS = mapOf(
        Key.D0 to "0", Key.D1 to "1", Key.D2 to "2", Key.D3 to "3", Key.D4 to "4",
        Key.D5 to "5", Key.D6 to "6", Key.D7 to "7", Key.D8 to "8", Key.D9 to "9",
    )

    private val BINARY = mapOf(
        Key.PLUS to "Y+X", Key.MINUS to "Y−X", Key.TIMES to "Y×X", Key.DIVIDE to "Y÷X",
        Key.POWER to "Y^X", Key.ROOT to "Y^(1÷X)",
    )

    private val UNARY = mapOf(
        Key.SIN to "sin(X)", Key.COS to "cos(X)", Key.TAN to "tan(X)",
        Key.ASIN to "sin⁻¹(X)", Key.ACOS to "cos⁻¹(X)", Key.ATAN to "tan⁻¹(X)",
        Key.LN to "ln(X)", Key.LOG to "log(X)", Key.EXP to "e^(X)", Key.POW10 to "10^(X)",
        Key.SQRT to "√(X)", Key.CBRT to "∛(X)", Key.SQUARE to "(X)²", Key.CUBE to "(X)³",
        Key.FACTORIAL to "(X)!", Key.RECIPROCAL to "1÷(X)", Key.ABS to "abs(X)",
    )
}
