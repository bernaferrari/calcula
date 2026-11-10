package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.math.CalcEngine.Value
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.math.abs

/** A reduced fraction times one supported factor. A null result hands the operation back to Decimal. */
internal class SymbolicNumber private constructor(
    val coefficient: Fraction,
    private val factor: Factor,
) {
    private sealed interface Factor {
        data object One : Factor
        data object Pi : Factor
        data object Euler : Factor
        data class Root(val radicand: Int) : Factor
    }

    val fraction: Fraction? get() = coefficient.takeIf { factor == Factor.One }
    private val n get() = coefficient.numerator
    private val d get() = coefficient.denominator

    fun negate(): SymbolicNumber = SymbolicNumber(Fraction(n.negate(), d), factor)

    fun add(other: SymbolicNumber): SymbolicNumber? = when {
        n.isZero() -> other
        other.n.isZero() -> this
        factor == other.factor -> create(n * other.d + other.n * d, d * other.d, factor)
        else -> null
    }

    fun multiply(other: SymbolicNumber): SymbolicNumber? {
        if (n.isZero() || other.n.isZero()) return ZERO
        val numerator = n * other.n
        val denominator = d * other.d
        return when {
            factor == Factor.One -> create(numerator, denominator, other.factor)
            other.factor == Factor.One -> create(numerator, denominator, factor)
            factor is Factor.Root && other.factor is Factor.Root ->
                rootProduct(numerator, denominator, factor.radicand, other.factor.radicand)
            else -> null
        }
    }

    fun divide(other: SymbolicNumber): SymbolicNumber? {
        if (other.n.isZero()) throw CalcException(CalcError.DIVIDE_BY_ZERO)
        val numerator = n * other.d
        val denominator = d * other.n
        return when {
            n.isZero() -> ZERO
            factor == other.factor -> create(numerator, denominator)
            other.factor == Factor.One -> create(numerator, denominator, factor)
            other.factor is Factor.Root -> {
                val reciprocalRoot = create(BigInteger.ONE, BigInteger.fromInt(other.factor.radicand), other.factor)
                    ?: return null
                create(numerator, denominator, factor)?.multiply(reciprocalRoot)
            }
            else -> null
        }
    }

    fun pow(exponent: SymbolicNumber): SymbolicNumber? {
        val power = exponent.fraction ?: return null
        val base = when (power.denominator) {
            BigInteger.ONE -> this
            BigInteger.TWO -> squareRoot()
            BigInteger.fromInt(3) -> cubeRoot()
            else -> null
        } ?: return null
        return base.integerPower(power.numerator)
    }

    private fun integerPower(exponent: BigInteger): SymbolicNumber? {
        if (exponent.abs() > BigInteger.fromInt(MAX_BITS)) return null
        val signed = exponent.intValue()
        var left = abs(signed)
        var base = if (signed < 0) ONE.divide(this) ?: return null else this
        var result = ONE
        while (left > 0) {
            if (left and 1 == 1) result = result.multiply(base) ?: return null
            left = left shr 1
            if (left > 0) base = base.multiply(base) ?: return null
        }
        return result
    }

    fun squareRoot(): SymbolicNumber? {
        if (factor != Factor.One || n.signum() < 0) return null
        if (n.isZero()) return ZERO
        val a = n.sqrt()
        val b = d.sqrt()
        if (a * a == n && b * b == d) return create(a, b)
        // sqrt(n/d) = sqrt(n*d)/d. Bound trial division for extracting square factors.
        val radicand = n * d
        if (radicand > BigInteger.fromInt(MAX_RADICAND)) return null
        return rootProduct(BigInteger.ONE, d, radicand.intValue(), 1)
    }

    fun cubeRoot(): SymbolicNumber? {
        if (factor != Factor.One) return null
        val numerator = integerCubeRoot(n)
        val denominator = integerCubeRoot(d)
        if (numerator.pow(3) != n || denominator.pow(3) != d) return null
        return create(numerator, denominator)
    }

    private fun integerCubeRoot(value: BigInteger): BigInteger {
        if (value.isZero()) return BigInteger.ZERO
        val magnitude = value.abs()
        var current = BigInteger.ONE.shl((magnitude.bitLength() + 2) / 3)
        while (true) {
            val next = (current * 2 + magnitude / (current * current)) / 3
            if (next >= current) return if (value.signum() < 0) current.negate() else current
            current = next
        }
    }

    fun inverseTrig(name: String, angle: AngleUnit): SymbolicNumber? {
        if (factor != Factor.One) return null
        val sign = n.signum()
        val one = n.abs() == d
        val half = n.abs() * 2 == d
        val step = when (name) {
            "sin⁻¹" -> when { sign == 0 -> 0; half -> 2 * sign; one -> 6 * sign; else -> return null }
            "cos⁻¹" -> when { sign == 0 -> 6; half -> 6 - 2 * sign; one -> 6 - 6 * sign; else -> return null }
            "tan⁻¹" -> when { sign == 0 -> 0; one -> 3 * sign; else -> return null }
            else -> return null
        }
        return if (angle == AngleUnit.DEGREES) create(BigInteger.fromInt(step * 15), BigInteger.ONE)
        else create(BigInteger.fromInt(step), BigInteger.fromInt(12), Factor.Pi)
    }

    fun periodicCoefficient(angle: AngleUnit): Fraction? {
        val period = when {
            angle == AngleUnit.DEGREES && factor == Factor.One -> 360
            angle == AngleUnit.RADIANS && factor == Factor.Pi -> 2
            else -> return null
        }
        return Fraction(n.rem(d * period), d)
    }

    fun trig(name: String, angle: AngleUnit): SymbolicNumber? {
        val divisor = when {
            angle == AngleUnit.DEGREES && factor == Factor.One -> d * BigInteger.fromInt(180)
            angle == AngleUnit.RADIANS && factor == Factor.Pi -> d
            n.isZero() -> d
            else -> return null
        }
        val (steps, remainder) = (n * BigInteger.fromInt(12)).divideAndRemainder(divisor)
        if (!remainder.isZero()) return null
        val step = ((steps % BigInteger.fromInt(24)).intValue() + 24) % 24
        val sine = sinStep(step)
        return when (name) {
            "sin" -> sine
            "cos" -> sinStep((step + 6) % 24)
            "tan" -> {
                val cosine = sinStep((step + 6) % 24) ?: return null
                if (cosine.n.isZero()) throw CalcException(CalcError.DOMAIN)
                sine?.divide(cosine)
            }
            else -> null
        }
    }

    fun approximation(pi: Value, e: Value, context: MathContext): Value {
        val work = MathContext(context.precision + 8)
        val numerator = Value(Decimal(n), symbolic = null)
        val coefficient = if (d == BigInteger.ONE) numerator
            else numerator.divide(Value(Decimal(d), symbolic = null), work)
        val tag = when (val factor = factor) {
            Factor.One -> {
                val value = if (d == BigInteger.ONE) coefficient else
                    CalcEngine.roundedValue(coefficient.v, coefficient.error, context)
                return value.copy(symbolic = this)
            }
            Factor.Pi -> pi
            Factor.Euler -> e
            is Factor.Root -> {
                val radicand = Decimal(factor.radicand)
                val root = CalcEngine.sqrt(radicand, work)
                Value(root, error = DecimalError.root(radicand, Decimal.ZERO, root, 2), symbolic = null)
            }
        }
        return coefficient.multiply(tag, context).copy(symbolic = this)
    }

    fun exactText(): String {
        val tag = when (val f = factor) {
            Factor.One -> ""
            Factor.Pi -> "π"
            Factor.Euler -> "e"
            is Factor.Root -> "√${f.radicand}"
        }
        val sign = if (n.signum() < 0) "−" else ""
        val magnitude = n.abs()
        val numerator = if (tag.isNotEmpty() && magnitude == BigInteger.ONE) tag else "$magnitude$tag"
        return if (d == BigInteger.ONE) "$sign$numerator" else "$sign$numerator⁄$d"
    }

    fun save(): String {
        val tag = when (val f = factor) {
            Factor.One -> "1"
            Factor.Pi -> "pi"
            Factor.Euler -> "e"
            is Factor.Root -> f.radicand.toString()
        }
        return "$n,$d,$tag"
    }

    override fun equals(other: Any?): Boolean =
        other is SymbolicNumber && coefficient == other.coefficient && factor == other.factor

    override fun hashCode(): Int = 31 * coefficient.hashCode() + factor.hashCode()

    companion object {
        internal const val MAX_BITS = 1_024
        private const val MAX_DECIMAL_DIGITS = 309
        private const val MAX_RADICAND = 1_000_000
        private val ZERO = SymbolicNumber(Fraction(BigInteger.ZERO, BigInteger.ONE), Factor.One)
        val ONE = SymbolicNumber(Fraction(BigInteger.ONE, BigInteger.ONE), Factor.One)
        val PI = SymbolicNumber(ONE.coefficient, Factor.Pi)
        val E = SymbolicNumber(ONE.coefficient, Factor.Euler)

        fun fromDecimal(value: Decimal): SymbolicNumber? {
            if (value.signum() == 0) return ZERO
            if (value.scale() !in -MAX_DECIMAL_DIGITS..MAX_DECIMAL_DIGITS || value.unscaled.abs().bitLength() > MAX_BITS) return null
            return if (value.scale() <= 0) create(value.toBigInteger(), BigInteger.ONE)
            else create(value.unscaled, BigInteger.TEN.pow(value.scale()))
        }

        fun restore(saved: String): SymbolicNumber? {
            val fields = saved.split(',')
            if (fields.size != 3 || fields.any { it.length > MAX_DECIMAL_DIGITS + 1 }) return null
            val (numerator, denominator, tag) = fields
            val factor = when (tag) {
                "1" -> Factor.One
                "pi" -> Factor.Pi
                "e" -> Factor.Euler
                else -> Factor.Root(tag.toIntOrNull()?.takeIf { it in 2..MAX_RADICAND } ?: return null)
            }
            return runCatching { create(BigInteger.parseString(numerator), BigInteger.parseString(denominator), factor) }.getOrNull()
        }

        private fun create(n: BigInteger, d: BigInteger, factor: Factor = Factor.One): SymbolicNumber? {
            if (d.isZero()) return null
            if (n.isZero()) return ZERO
            val gcd = n.abs().gcd(d.abs())
            val numerator = if (d.signum() < 0) n.negate() / gcd else n / gcd
            val denominator = d.abs() / gcd
            if (numerator.abs().bitLength() > MAX_BITS || denominator.bitLength() > MAX_BITS) return null
            return SymbolicNumber(Fraction(numerator, denominator), factor)
        }

        private fun rootProduct(n: BigInteger, d: BigInteger, a: Int, b: Int): SymbolicNumber? {
            var radicand = a.toLong() * b
            var outside = 1
            // Both incoming roots are square-free. Their common factors become a square.
            if (b != 1) {
                var x = a
                var y = b
                while (y != 0) {
                    val rest = x % y
                    x = y
                    y = rest
                }
                outside = x
                radicand /= x.toLong() * x
            } else {
                var factor = 2
                while (factor.toLong() * factor <= radicand) {
                    val square = factor.toLong() * factor
                    while (radicand % square == 0L) {
                        radicand /= square
                        outside *= factor
                    }
                    factor++
                }
            }
            if (radicand > MAX_RADICAND) return null
            val tag = if (radicand == 1L) Factor.One else Factor.Root(radicand.toInt())
            return create(n * BigInteger.fromInt(outside), d, tag)
        }

        private fun sinStep(step: Int): SymbolicNumber? {
            val half = step % 12
            val firstQuadrant = minOf(half, 12 - half)
            val value = when (firstQuadrant) {
                0 -> ZERO
                2 -> create(BigInteger.ONE, BigInteger.TWO)
                3 -> create(BigInteger.ONE, BigInteger.TWO, Factor.Root(2))
                4 -> create(BigInteger.ONE, BigInteger.TWO, Factor.Root(3))
                6 -> ONE
                else -> null
            }
            return if (step >= 12) value?.negate() else value
        }
    }
}
