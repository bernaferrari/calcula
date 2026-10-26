package com.bernaferrari.calculator.domain.math

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.math.max

enum class RoundingMode { HALF_EVEN, HALF_UP, DOWN }

data class MathContext(val precision: Int, val roundingMode: RoundingMode = RoundingMode.HALF_EVEN)

/**
 * An arbitrary-precision decimal for every platform: `unscaled × 10^−scale`, with the same
 * semantics as `java.math.BigDecimal` for the parts the calculator uses. Equality is numeric
 * (2.0 == 2), which is what a calculator means by equal.
 */
class Decimal private constructor(val unscaled: BigInteger, val scale: Int) : Comparable<Decimal> {

    constructor(text: String) : this(parse(text))
    constructor(value: Int) : this(BigInteger.fromInt(value), 0)
    constructor(value: Long) : this(BigInteger.fromLong(value), 0)
    constructor(value: BigInteger) : this(value, 0)
    private constructor(other: Decimal) : this(other.unscaled, other.scale)

    fun signum(): Int = unscaled.signum()

    /** Number of significant digits in the unscaled value (1 for zero). */
    fun precision(): Int = if (unscaled.isZero()) 1 else unscaled.abs().numberOfDecimalDigits().toInt()

    fun scale(): Int = scale

    fun negate(): Decimal = Decimal(unscaled.negate(), scale)

    fun abs(): Decimal = if (signum() < 0) negate() else this

    fun add(other: Decimal): Decimal {
        val s = max(scale, other.scale)
        return Decimal(rescaled(s).add(other.rescaled(s)), s)
    }

    fun add(other: Decimal, mc: MathContext): Decimal = add(other).round(mc)

    fun subtract(other: Decimal): Decimal = add(other.negate())

    fun subtract(other: Decimal, mc: MathContext): Decimal = subtract(other).round(mc)

    fun multiply(other: Decimal): Decimal = Decimal(unscaled.multiply(other.unscaled), scale + other.scale)

    fun multiply(other: Decimal, mc: MathContext): Decimal = multiply(other).round(mc)

    fun divide(other: Decimal, mc: MathContext): Decimal {
        if (other.signum() == 0) throw ArithmeticException("Division by zero")
        if (signum() == 0) return ZERO
        // Shift the dividend so the integer quotient carries the wanted digits plus guards.
        val shift = mc.precision + 3 + other.precision() - precision()
        val dividend = if (shift > 0) unscaled.multiply(pow10(shift)) else unscaled
        val (q, r) = dividend.divideAndRemainder(other.unscaled)
        val shiftUsed = max(shift, 0)
        // A sticky digit makes rounding see a non-zero remainder past the guard digits.
        val tens = q.multiply(BigInteger.TEN)
        val sticky = if (r.isZero()) tens else tens.add(BigInteger.fromInt(signum() * other.signum()))
        return Decimal(sticky, scale - other.scale + shiftUsed + 1).round(mc).stripTrailingZeros()
    }

    /** Exact division when it terminates within [mc], same as above otherwise. */
    fun divide(other: Decimal): Decimal = divide(other, MathContext(64))

    /** Truncated remainder: the sign follows the dividend, like Java. */
    fun remainder(other: Decimal): Decimal = divideAndRemainder(other)[1]

    fun divideAndRemainder(other: Decimal): Array<Decimal> {
        if (other.signum() == 0) throw ArithmeticException("Division by zero")
        val s = max(scale, other.scale)
        val (q, r) = rescaled(s).divideAndRemainder(other.rescaled(s))
        return arrayOf(Decimal(q, 0), Decimal(r, s))
    }

    fun pow(n: Int): Decimal {
        require(n >= 0) { "Negative exponent" }
        return Decimal(unscaled.pow(n), scale * n)
    }

    fun pow(n: Int, mc: MathContext): Decimal {
        if (n == 0) return ONE
        val work = MathContext(mc.precision + 10, mc.roundingMode)
        var result = ONE
        var base = this
        var e = kotlin.math.abs(n)
        while (e > 0) {
            if (e and 1 == 1) result = result.multiply(base, work)
            e = e shr 1
            if (e > 0) base = base.multiply(base, work)
        }
        return if (n < 0) ONE.divide(result, mc) else result.round(mc)
    }

    fun round(mc: MathContext): Decimal {
        val drop = precision() - mc.precision
        if (drop <= 0) return this
        return Decimal(roundDiv(unscaled, pow10(drop), mc.roundingMode), scale - drop).let {
            // Rounding 999 up gives 1000, one digit too many.
            if (it.precision() > mc.precision) it.round(mc) else it
        }
    }

    fun setScale(newScale: Int, mode: RoundingMode): Decimal = when {
        newScale == scale -> this
        newScale > scale -> Decimal(unscaled.multiply(pow10(newScale - scale)), newScale)
        else -> Decimal(roundDiv(unscaled, pow10(scale - newScale), mode), newScale)
    }

    fun stripTrailingZeros(): Decimal {
        if (unscaled.isZero()) return ZERO
        var u = unscaled
        var s = scale
        while (true) {
            val (q, r) = u.divideAndRemainder(BigInteger.TEN)
            if (!r.isZero()) break
            u = q
            s--
        }
        return Decimal(u, s)
    }

    fun movePointLeft(n: Int): Decimal = Decimal(unscaled, scale + n).let { if (it.scale < 0) it.setScale(0, RoundingMode.DOWN) else it }

    fun movePointRight(n: Int): Decimal = movePointLeft(-n)

    fun scaleByPowerOfTen(n: Int): Decimal = Decimal(unscaled, scale - n)

    fun toBigInteger(): BigInteger = if (scale <= 0) unscaled.multiply(pow10(-scale)) else unscaled.divide(pow10(scale))

    fun toInt(): Int = toBigInteger().longValue(false).toInt()

    fun toDouble(): Double = "${unscaled}E${-scale.toLong()}".toDouble()

    /** True when this is a whole number (2.000 counts). */
    fun isInteger(): Boolean = scale <= 0 || stripTrailingZeros().scale <= 0

    fun toPlainString(): String {
        val digits = unscaled.abs().toString()
        val sign = if (signum() < 0) "-" else ""
        return when {
            scale <= 0 -> sign + digits + "0".repeat(-scale)
            digits.length > scale -> sign + digits.dropLast(scale) + "." + digits.takeLast(scale)
            else -> sign + "0." + "0".repeat(scale - digits.length) + digits
        }
    }

    override fun toString(): String = toPlainString()

    override fun compareTo(other: Decimal): Int {
        val a = signum()
        val b = other.signum()
        if (a != b) return a.compareTo(b)
        if (a == 0) return 0
        // Compare magnitudes by exponent first, so 1E10000 never gets expanded.
        val ea = precision() - scale
        val eb = other.precision() - other.scale
        if (ea != eb) return if (ea > eb) a else -a
        val s = max(scale, other.scale)
        return rescaled(s).compareTo(other.rescaled(s))
    }

    override fun equals(other: Any?): Boolean = other is Decimal && compareTo(other) == 0

    override fun hashCode(): Int = stripTrailingZeros().let { 31 * it.unscaled.hashCode() + it.scale }

    private fun rescaled(s: Int): BigInteger = if (s == scale) unscaled else unscaled.multiply(pow10(s - scale))

    companion object {
        val ZERO = Decimal(BigInteger.ZERO, 0)
        val ONE = Decimal(BigInteger.ONE, 0)
        val TEN = Decimal(BigInteger.TEN, 0)

        private val powers = List(64) { BigInteger.TEN.pow(it) }

        private fun pow10(n: Int): BigInteger =
            if (n in powers.indices) powers[n] else BigInteger.TEN.pow(n)

        /** Integer division of [a] by a positive power of ten [b], rounded with [mode]. */
        private fun roundDiv(a: BigInteger, b: BigInteger, mode: RoundingMode): BigInteger {
            val (q, r) = a.divideAndRemainder(b)
            if (r.isZero() || mode == RoundingMode.DOWN) return q
            val twice = r.abs().multiply(BigInteger.TWO).compareTo(b)
            val up = when {
                twice > 0 -> true
                twice < 0 -> false
                mode == RoundingMode.HALF_UP -> true
                else -> q.rem(BigInteger.TWO).isZero().not()
            }
            if (!up) return q
            return if (a.signum() < 0) q.subtract(BigInteger.ONE) else q.add(BigInteger.ONE)
        }

        /** Exact decimal for a Double, through its shortest round-tripping text. */
        fun fromDouble(d: Double): Decimal {
            require(d.isFinite()) { "Not finite: $d" }
            return Decimal(d.toString())
        }

        private fun parse(raw: String): Decimal {
            val text = raw.trim()
            require(text.isNotEmpty()) { "Empty number" }
            val e = text.indexOfFirst { it == 'e' || it == 'E' }
            val mantissa = if (e >= 0) text.substring(0, e) else text
            val exponent = if (e >= 0) text.substring(e + 1).removePrefix("+").toInt() else 0
            val negative = mantissa.startsWith('-')
            val body = mantissa.removePrefix("-").removePrefix("+")
            val dot = body.indexOf('.')
            val digits = if (dot >= 0) body.removeRange(dot, dot + 1) else body
            require(digits.isNotEmpty() && digits.all { it in '0'..'9' }) { "Not a number: $raw" }
            val fraction = if (dot >= 0) body.length - dot - 1 else 0
            val unscaled = BigInteger.parseString(digits, 10).let { if (negative) it.negate() else it }
            return Decimal(unscaled, fraction - exponent)
        }
    }
}

/** Parses a plain or E-notation number, or returns null. */
fun String.toDecimalOrNull(): Decimal? = runCatching { Decimal(this) }.getOrNull()
