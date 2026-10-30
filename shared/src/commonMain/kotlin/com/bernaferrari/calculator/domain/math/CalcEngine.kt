package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.model.AngleUnit
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.math.abs
import kotlin.math.pow

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

    private val PI = Decimal("3.141592653589793238462643383279502884197")
    private val E = Decimal("2.718281828459045235360287471352662497757")
    private val HUNDRED = Decimal(100)
    private val LIMIT = Decimal.ONE.scaleByPowerOfTen(10_000)

    fun evaluate(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): Decimal {
        val tokens = Tokenizer.tokenize(expression)
        if (tokens.isEmpty()) throw CalcException(CalcError.SYNTAX)
        val parser = Parser(tokens, angleUnit)
        val result = parser.parseAll()
        if (result.abs() > LIMIT) throw CalcException(CalcError.OVERFLOW)
        return result
    }

    fun evaluateOrNull(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): Decimal? =
        try {
            evaluate(expression, angleUnit)
        } catch (e: CalcException) {
            null
        } catch (e: ArithmeticException) {
            null
        }

    private class Value(val v: Decimal, val percent: Boolean = false)

    private class Parser(private val tokens: List<Token>, private val angle: AngleUnit) {
        private var pos = 0

        private fun peek(): Token? = tokens.getOrNull(pos)

        private fun next(): Token = tokens.getOrNull(pos++) ?: throw CalcException(CalcError.SYNTAX)

        fun parseAll(): Decimal {
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
                    "⁻¹" -> Value(divide(Decimal.ONE, value.v))
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
        private fun group(): Decimal {
            if (peek() == null) throw CalcException(CalcError.SYNTAX)
            val inner = additive().v
            when (peek()) {
                is Token.RParen -> pos++
                null -> Unit
                else -> throw CalcException(CalcError.SYNTAX)
            }
            return inner
        }

        private fun applyFunction(name: String, x: Decimal): Decimal = when (name) {
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
            "sinh⁻¹" -> fromDouble(kotlin.math.asinh(x.toDouble()))
            "cosh⁻¹" -> {
                if (x < Decimal(1)) throw CalcException(CalcError.DOMAIN)
                fromDouble(kotlin.math.acosh(x.toDouble()))
            }
            "tanh⁻¹" -> {
                if (x.abs() >= Decimal(1)) throw CalcException(CalcError.DOMAIN)
                fromDouble(kotlin.math.atanh(x.toDouble()))
            }
            // The reciprocals keep the exact zeros and poles of their sine and cosine.
            "sec" -> reciprocal(applyFunction("cos", x))
            "csc" -> reciprocal(applyFunction("sin", x))
            // cos ÷ sin rather than 1 ÷ tan, so cot(90°) is exactly 0 instead of a pole.
            "cot" -> applyFunction("cos", x).let { c -> applyFunction("sin", x).let { s -> if (s.signum() == 0) throw CalcException(CalcError.DOMAIN) else c.divide(s, MC) } }
            // Rounding stays exact on the decimal itself: floor(−2.5) is −3, round(2.5) is 3.
            "floor" -> whole(x, up = false)
            "ceil" -> whole(x, up = true)
            "round" -> x.setScale(0, RoundingMode.HALF_UP)
            "sgn" -> Decimal(x.signum())
            "log₂" -> {
                if (x.signum() <= 0) throw CalcException(CalcError.DOMAIN)
                fromDouble(ln(x) / kotlin.math.ln(2.0))
            }
            else -> throw CalcException(CalcError.SYNTAX)
        }

        private fun toRadians(x: Decimal): Double =
            if (angle == AngleUnit.DEGREES) x.remainder(Decimal(360)).toDouble() * kotlin.math.PI / 180.0 else x.toDouble()

        /** Degrees that land exactly on an axis (0, 90, 180, 270) get exact answers. */
        private fun quadrant(x: Decimal): Int? {
            if (angle != AngleUnit.DEGREES) return null
            val r = x.remainder(Decimal(360)).let { if (it.signum() < 0) it.add(Decimal(360)) else it }
            val q = r.divideAndRemainder(Decimal(90))
            return if (q[1].signum() == 0) q[0].toInt() else null
        }

        private fun trig(x: Decimal, f: (Double) -> Double): Decimal {
            val result = f(toRadians(x))
            // sin(π) in radians is 1.2e-16 in Double; treat noise around an exact zero as zero.
            if (abs(result) < 1e-15 && abs(x.toDouble()) > 1e-3) return Decimal.ZERO
            return fromDouble(result)
        }

        private fun tan(x: Decimal): Decimal {
            when (quadrant(x)) {
                0, 2 -> return Decimal.ZERO
                1, 3 -> throw CalcException(CalcError.DOMAIN)
            }
            val cos = kotlin.math.cos(toRadians(x))
            if (abs(cos) < 1e-15) throw CalcException(CalcError.DOMAIN)
            return trig(x) { kotlin.math.tan(it) }
        }

        private fun reciprocal(x: Decimal): Decimal {
            if (x.signum() == 0) throw CalcException(CalcError.DOMAIN)
            return Decimal(1).divide(x, MC)
        }

        /** The integer at or below [x] (or at or above, when [up]), without leaving decimals. */
        private fun whole(x: Decimal, up: Boolean): Decimal {
            val truncated = x.setScale(0, RoundingMode.DOWN)
            if (truncated.compareTo(x) == 0) return truncated
            return when {
                !up && x.signum() < 0 -> truncated.subtract(Decimal(1))
                up && x.signum() > 0 -> truncated.add(Decimal(1))
                else -> truncated
            }
        }

        private fun inverseTrig(x: Decimal, f: (Double) -> Double): Decimal {
            val r = f(x.toDouble())
            if (r.isNaN()) throw CalcException(CalcError.DOMAIN)
            return fromDouble(if (angle == AngleUnit.DEGREES) r * 180.0 / kotlin.math.PI else r)
        }
    }

    private fun Token.startsOperand(): Boolean =
        this is Token.Num || this is Token.Const || this is Token.Var || this is Token.LParen || this is Token.Func || this is Token.Root

    private fun fromDouble(d: Double): Decimal {
        if (d.isNaN()) throw CalcException(CalcError.DOMAIN)
        if (d.isInfinite()) throw CalcException(CalcError.OVERFLOW)
        return Decimal.fromDouble(d)
    }

    private fun divide(a: Decimal, b: Decimal): Decimal {
        if (b.signum() == 0) throw CalcException(CalcError.DIVIDE_BY_ZERO)
        return a.divide(b, MC)
    }

    /** ln for any magnitude, avoiding Double overflow on huge Decimals. */
    private fun ln(x: Decimal): Double {
        val digits = x.precision() - x.scale() - 1
        val mantissa = x.movePointLeft(digits).toDouble()
        return kotlin.math.ln(mantissa) + digits * kotlin.math.ln(10.0)
    }

    private fun exactLog10(x: Decimal): Decimal? {
        val stripped = x.stripTrailingZeros()
        return if (stripped.unscaled == BigInteger.ONE) Decimal(-stripped.scale()) else null
    }

    private fun pow(base: Decimal, exponent: Decimal): Decimal {
        if (exponent.signum() == 0) return Decimal.ONE
        if (base.signum() == 0) {
            if (exponent.signum() < 0) throw CalcException(CalcError.DIVIDE_BY_ZERO)
            return Decimal.ZERO
        }
        val integer = exponent.stripTrailingZeros().let { it.scale() <= 0 }
        if (integer && base.abs() == Decimal.ONE) {
            val odd = exponent.remainder(Decimal(2)) != Decimal.ZERO
            return if (base.signum() < 0 && odd) Decimal.ONE.negate() else Decimal.ONE
        }
        if (integer && exponent.abs() <= Decimal(Int.MAX_VALUE)) {
            val n = exponent.toInt()
            // Use log magnitude, not digit count: 1.00001^20000 is only about 1.22.
            // Leave a small guard band for the estimate; evaluate checks the actual result.
            val magnitude = ln(base.abs()) / kotlin.math.ln(10.0) * n
            if (abs(magnitude) > 10_050) throw CalcException(CalcError.OVERFLOW)
            return base.pow(n, MC)
        }
        // An integer outside the bounded power budget must not become a fractional root.
        if (integer) throw CalcException(CalcError.OVERFLOW)
        if (base.signum() < 0) {
            // Odd roots of negatives are real: (−8)^(1/3) = −2.
            val reciprocal = Decimal.ONE.divide(exponent, MC).round(MathContext(12))
            val isOddRoot = reciprocal.stripTrailingZeros().scale() <= 0 &&
                !reciprocal.toBigInteger().rem(BigInteger.TWO).isZero()
            if (isOddRoot) return pow(base.negate(), exponent).negate()
            throw CalcException(CalcError.DOMAIN)
        }
        val log = ln(base) * exponent.toDouble()
        if (log > 23_000) throw CalcException(CalcError.OVERFLOW)
        if (log < -23_000) return Decimal.ZERO
        // Split into 10^k × 10^frac so huge results don't overflow Double.
        val log10 = log / kotlin.math.ln(10.0)
        val k = kotlin.math.floor(log10)
        return fromDouble(10.0.pow(log10 - k)).scaleByPowerOfTen(k.toInt())
    }

    private fun sqrt(x: Decimal): Decimal {
        if (x.signum() < 0) throw CalcException(CalcError.DOMAIN)
        if (x.signum() == 0) return Decimal.ZERO
        val digits = x.precision() - x.scale()
        // Initial guess from Double (rescaled to stay in range), then Newton iterations.
        val half = digits / 2
        var guess = Decimal.fromDouble(kotlin.math.sqrt(x.movePointLeft(half * 2).toDouble())).movePointRight(half)
        val two = Decimal(2)
        repeat(8) { guess = guess.add(x.divide(guess, MC), MC).divide(two, MC) }
        return guess
    }

    private fun cbrt(x: Decimal): Decimal {
        if (x.signum() == 0) return Decimal.ZERO
        val negative = x.signum() < 0
        val a = x.abs()
        val digits = a.precision() - a.scale()
        val third = digits / 3
        var guess = Decimal.fromDouble(kotlin.math.cbrt(a.movePointLeft(third * 3).toDouble())).movePointRight(third)
        val three = Decimal(3)
        repeat(8) {
            guess = guess.multiply(Decimal(2), MC).add(a.divide(guess.multiply(guess, MC), MC), MC).divide(three, MC)
        }
        return if (negative) guess.negate() else guess
    }

    private fun factorial(x: Decimal): Decimal {
        val stripped = x.stripTrailingZeros()
        if (x.signum() < 0 || stripped.scale() > 0) throw CalcException(CalcError.DOMAIN)
        if (x > Decimal(3000)) throw CalcException(CalcError.OVERFLOW)
        var result = BigInteger.ONE
        for (i in 2..x.toInt()) result = result.multiply(BigInteger.fromInt(i))
        return Decimal(result).round(MC)
    }
}
