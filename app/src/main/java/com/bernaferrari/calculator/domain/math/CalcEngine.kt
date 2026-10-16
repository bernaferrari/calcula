package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.model.AngleUnit
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs

enum class CalcError { DIVIDE_BY_ZERO, DOMAIN, SYNTAX, OVERFLOW }

class CalcException(val error: CalcError) : Exception(error.name)

/**
 * Evaluates the calculator's expression language.
 *
 * Grammar (lowest to highest precedence):
 *   additive       := multiplicative (('+' | '−') multiplicative)*
 *   multiplicative := unary (('×' | '÷') unary | <implicit ×> power)*
 *   unary          := ('−' | '+') unary | power
 *   power          := postfix ('^' unary)?            right-associative, so 2^3^2 = 2^9
 *   postfix        := primary ('!' | '%' | '²' | '³' | '⁻¹')*
 *   primary        := number | π | e | '(' additive ')' | function '(' additive ')' | ('√' | '∛') postfix
 *
 * Missing closing parentheses at the end are closed automatically. Percent is "smart":
 * `a + b%` adds b percent of a, while `a × b%` multiplies by b/100.
 *
 * Arithmetic, powers with integer exponents, roots and factorials are exact (40 significant
 * digits); transcendental functions go through Double and are rounded when formatted.
 */
object CalcEngine {
    internal val MC = MathContext(40, RoundingMode.HALF_EVEN)

    private val PI = BigDecimal("3.141592653589793238462643383279502884197")
    private val E = BigDecimal("2.718281828459045235360287471352662497757")
    private val HUNDRED = BigDecimal(100)
    private val LIMIT = BigDecimal.ONE.scaleByPowerOfTen(10_000)

    fun evaluate(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): BigDecimal {
        val tokens = Tokenizer.tokenize(expression)
        if (tokens.isEmpty()) throw CalcException(CalcError.SYNTAX)
        val parser = Parser(tokens, angleUnit)
        val result = parser.parseAll()
        if (result.abs() > LIMIT) throw CalcException(CalcError.OVERFLOW)
        return result
    }

    fun evaluateOrNull(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): BigDecimal? =
        try {
            evaluate(expression, angleUnit)
        } catch (e: CalcException) {
            null
        } catch (e: ArithmeticException) {
            null
        }

    private class Value(val v: BigDecimal, val percent: Boolean = false)

    private class Parser(private val tokens: List<Token>, private val angle: AngleUnit) {
        private var pos = 0

        private fun peek(): Token? = tokens.getOrNull(pos)

        private fun next(): Token = tokens.getOrNull(pos++) ?: throw CalcException(CalcError.SYNTAX)

        fun parseAll(): BigDecimal {
            val value = additive().v
            if (pos != tokens.size) throw CalcException(CalcError.SYNTAX)
            return value
        }

        private fun additive(): Value {
            var left = multiplicative()
            while (true) {
                val op = (peek() as? Token.Op)?.symbol
                if (op != '+' && op != '−') break
                pos++
                val right = multiplicative()
                val amount = if (right.percent) left.v.multiply(right.v, MC) else right.v
                left = Value(if (op == '+') left.v.add(amount, MC) else left.v.subtract(amount, MC))
            }
            return left
        }

        private fun multiplicative(): Value {
            var left = unary()
            while (true) {
                val token = peek()
                when {
                    token is Token.Op && token.symbol == '×' -> {
                        pos++
                        left = Value(left.v.multiply(unary().v, MC))
                    }
                    token is Token.Op && token.symbol == '÷' -> {
                        pos++
                        left = Value(divide(left.v, unary().v))
                    }
                    token != null && token.startsOperand() -> {
                        left = Value(left.v.multiply(power().v, MC))
                    }
                    else -> return left
                }
            }
        }

        private fun unary(): Value {
            val token = peek()
            if (token is Token.Op && token.symbol == '−') {
                pos++
                val inner = unary()
                return Value(inner.v.negate(), inner.percent)
            }
            if (token is Token.Op && token.symbol == '+') {
                pos++
                return unary()
            }
            return power()
        }

        private fun power(): Value {
            val base = postfix()
            val token = peek()
            if (token is Token.Op && token.symbol == '^') {
                pos++
                if (peek() == null) throw CalcException(CalcError.SYNTAX)
                return Value(pow(base.v, unary().v))
            }
            return base
        }

        private fun postfix(): Value {
            var value = primary()
            while (true) {
                val token = peek() as? Token.Postfix ?: return value
                pos++
                value = when (token.symbol) {
                    "%" -> Value(value.v.divide(HUNDRED, MC), percent = true)
                    "!" -> Value(factorial(value.v))
                    "²" -> Value(value.v.multiply(value.v, MC))
                    "³" -> Value(value.v.pow(3, MC))
                    "⁻¹" -> Value(divide(BigDecimal.ONE, value.v))
                    else -> throw CalcException(CalcError.SYNTAX)
                }
            }
        }

        private fun primary(): Value {
            return when (val token = next()) {
                is Token.Num -> Value(token.value)
                is Token.Const -> Value(if (token.name == "π") PI else E)
                is Token.LParen -> Value(group())
                is Token.Func -> {
                    val arg = if (peek() is Token.LParen) {
                        pos++
                        group()
                    } else {
                        power().v
                    }
                    Value(applyFunction(token.name, arg))
                }
                is Token.Root -> {
                    val arg = postfix().v
                    Value(if (token.degree == 2) sqrt(arg) else cbrt(arg))
                }
                else -> throw CalcException(CalcError.SYNTAX)
            }
        }

        /** Parses the inside of a parenthesis; the closing one is optional at the very end. */
        private fun group(): BigDecimal {
            if (peek() == null) throw CalcException(CalcError.SYNTAX)
            val inner = additive().v
            when (peek()) {
                is Token.RParen -> pos++
                null -> Unit
                else -> throw CalcException(CalcError.SYNTAX)
            }
            return inner
        }

        private fun applyFunction(name: String, x: BigDecimal): BigDecimal = when (name) {
            "sin" -> trig(x) { kotlin.math.sin(it) }
            "cos" -> trig(x) { kotlin.math.cos(it) }
            "tan" -> tan(x)
            "sin⁻¹" -> inverseTrig(x) { kotlin.math.asin(it) }
            "cos⁻¹" -> inverseTrig(x) { kotlin.math.acos(it) }
            "tan⁻¹" -> inverseTrig(x) { kotlin.math.atan(it) }
            "sinh" -> fromDouble(kotlin.math.sinh(x.toDouble()))
            "cosh" -> fromDouble(kotlin.math.cosh(x.toDouble()))
            "tanh" -> fromDouble(kotlin.math.tanh(x.toDouble()))
            "ln" -> {
                if (x.signum() <= 0) throw CalcException(CalcError.DOMAIN)
                fromDouble(ln(x))
            }
            "log" -> {
                if (x.signum() <= 0) throw CalcException(CalcError.DOMAIN)
                exactLog10(x) ?: fromDouble(ln(x) / kotlin.math.ln(10.0))
            }
            "abs" -> x.abs()
            else -> throw CalcException(CalcError.SYNTAX)
        }

        private fun toRadians(x: BigDecimal): Double =
            if (angle == AngleUnit.DEGREES) x.remainder(BigDecimal(360)).toDouble() * kotlin.math.PI / 180.0 else x.toDouble()

        /** Degrees that land exactly on an axis (0, 90, 180, 270) get exact answers. */
        private fun quadrant(x: BigDecimal): Int? {
            if (angle != AngleUnit.DEGREES) return null
            val r = x.remainder(BigDecimal(360)).let { if (it.signum() < 0) it.add(BigDecimal(360)) else it }
            val q = r.divideAndRemainder(BigDecimal(90))
            return if (q[1].signum() == 0) q[0].toInt() else null
        }

        private fun trig(x: BigDecimal, f: (Double) -> Double): BigDecimal {
            val result = f(toRadians(x))
            // sin(π) in radians is 1.2e-16 in Double; treat noise around an exact zero as zero.
            if (abs(result) < 1e-15 && abs(x.toDouble()) > 1e-3) return BigDecimal.ZERO
            return fromDouble(result)
        }

        private fun tan(x: BigDecimal): BigDecimal {
            when (quadrant(x)) {
                0, 2 -> return BigDecimal.ZERO
                1, 3 -> throw CalcException(CalcError.DOMAIN)
            }
            val cos = kotlin.math.cos(toRadians(x))
            if (abs(cos) < 1e-15) throw CalcException(CalcError.DOMAIN)
            return trig(x) { kotlin.math.tan(it) }
        }

        private fun inverseTrig(x: BigDecimal, f: (Double) -> Double): BigDecimal {
            val r = f(x.toDouble())
            if (r.isNaN()) throw CalcException(CalcError.DOMAIN)
            return fromDouble(if (angle == AngleUnit.DEGREES) Math.toDegrees(r) else r)
        }
    }

    private fun Token.startsOperand(): Boolean =
        this is Token.Num || this is Token.Const || this is Token.LParen || this is Token.Func || this is Token.Root

    private fun fromDouble(d: Double): BigDecimal {
        if (d.isNaN()) throw CalcException(CalcError.DOMAIN)
        if (d.isInfinite()) throw CalcException(CalcError.OVERFLOW)
        return BigDecimal(d.toString())
    }

    private fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (b.signum() == 0) throw CalcException(CalcError.DIVIDE_BY_ZERO)
        return a.divide(b, MC)
    }

    /** ln for any magnitude, avoiding Double overflow on huge BigDecimals. */
    private fun ln(x: BigDecimal): Double {
        val digits = x.precision() - x.scale() - 1
        val mantissa = x.movePointLeft(digits).toDouble()
        return kotlin.math.ln(mantissa) + digits * kotlin.math.ln(10.0)
    }

    private fun exactLog10(x: BigDecimal): BigDecimal? {
        val stripped = x.stripTrailingZeros()
        return if (stripped.unscaledValue() == BigInteger.ONE) BigDecimal(-stripped.scale()) else null
    }

    private fun pow(base: BigDecimal, exponent: BigDecimal): BigDecimal {
        val integer = exponent.stripTrailingZeros().let { it.scale() <= 0 }
        if (integer && exponent.abs() <= BigDecimal(99_999)) {
            val n = exponent.toInt()
            if (base.signum() == 0) {
                if (n < 0) throw CalcException(CalcError.DIVIDE_BY_ZERO)
                return if (n == 0) BigDecimal.ONE else BigDecimal.ZERO
            }
            // Bail out before BigDecimal.pow spends ages building a number we would reject anyway.
            val magnitude = (base.precision() - base.scale()).toDouble() * n
            if (abs(magnitude) > 10_050) throw CalcException(CalcError.OVERFLOW)
            return base.pow(n, MC)
        }
        if (base.signum() < 0) {
            // Odd roots of negatives are real: (−8)^(1/3) = −2.
            val reciprocal = BigDecimal.ONE.divide(exponent, MC).round(MathContext(12))
            val isOddRoot = reciprocal.stripTrailingZeros().scale() <= 0 &&
                reciprocal.toBigInteger().testBit(0)
            if (isOddRoot) return pow(base.negate(), exponent).negate()
            throw CalcException(CalcError.DOMAIN)
        }
        if (base.signum() == 0) {
            if (exponent.signum() < 0) throw CalcException(CalcError.DIVIDE_BY_ZERO)
            return BigDecimal.ZERO
        }
        val log = ln(base) * exponent.toDouble()
        if (log > 23_000) throw CalcException(CalcError.OVERFLOW)
        if (log < -23_000) return BigDecimal.ZERO
        // Split into 10^k × 10^frac so huge results don't overflow Double.
        val log10 = log / kotlin.math.ln(10.0)
        val k = kotlin.math.floor(log10)
        return fromDouble(Math.pow(10.0, log10 - k)).scaleByPowerOfTen(k.toInt())
    }

    private fun sqrt(x: BigDecimal): BigDecimal {
        if (x.signum() < 0) throw CalcException(CalcError.DOMAIN)
        if (x.signum() == 0) return BigDecimal.ZERO
        val digits = x.precision() - x.scale()
        // Initial guess from Double (rescaled to stay in range), then Newton iterations.
        val half = digits / 2
        var guess = BigDecimal(kotlin.math.sqrt(x.movePointLeft(half * 2).toDouble())).movePointRight(half)
        val two = BigDecimal(2)
        repeat(8) { guess = guess.add(x.divide(guess, MC), MC).divide(two, MC) }
        return guess
    }

    private fun cbrt(x: BigDecimal): BigDecimal {
        if (x.signum() == 0) return BigDecimal.ZERO
        val negative = x.signum() < 0
        val a = x.abs()
        val digits = a.precision() - a.scale()
        val third = digits / 3
        var guess = BigDecimal(Math.cbrt(a.movePointLeft(third * 3).toDouble())).movePointRight(third)
        val three = BigDecimal(3)
        repeat(8) {
            guess = guess.multiply(BigDecimal(2), MC).add(a.divide(guess.multiply(guess, MC), MC), MC).divide(three, MC)
        }
        return if (negative) guess.negate() else guess
    }

    private fun factorial(x: BigDecimal): BigDecimal {
        val stripped = x.stripTrailingZeros()
        if (x.signum() < 0 || stripped.scale() > 0) throw CalcException(CalcError.DOMAIN)
        if (x > BigDecimal(3000)) throw CalcException(CalcError.OVERFLOW)
        var result = BigInteger.ONE
        for (i in 2..x.toInt()) result = result.multiply(BigInteger.valueOf(i.toLong()))
        return BigDecimal(result).round(MC)
    }
}
