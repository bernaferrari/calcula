package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.math.CalcEngine.Value
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.math.abs

/** A small sum of reduced rational multiples of supported factors. Null hands an operation to Decimal. */
@ConsistentCopyVisibility
internal data class SymbolicNumber private constructor(private val terms: Map<Factor, Fraction>) {
    private sealed interface Factor {
        data object One : Factor
        data class Constant(val pi: Boolean, val power: Int) : Factor
        data class Root(val radicand: Int, val degree: Int) : Factor
        data class LogPrime(val prime: Int) : Factor
    }

    private val factor get() = terms.keys.singleOrNull() ?: if (isZero) Factor.One else null
    val fraction: Fraction? get() = if (isZero) ZERO_FRACTION else terms[Factor.One].takeIf { terms.size == 1 }
    private val coefficient get() = if (isZero) ZERO_FRACTION else terms.values.single()
    private val n get() = coefficient.numerator
    private val d get() = coefficient.denominator
    private val isZero get() = terms.isEmpty()

    // All supported factors are positive. A sum of mixed signs needs an interval to decide its sign.
    fun signum(): Int? {
        val sign = terms.values.firstOrNull()?.numerator?.signum() ?: return 0
        return sign.takeIf { terms.values.all { it.numerator.signum() == sign } }
    }

    fun negate(): SymbolicNumber = SymbolicNumber(terms.mapValues { (_, f) -> Fraction(f.numerator.negate(), f.denominator) })

    fun add(other: SymbolicNumber): SymbolicNumber? {
        if (isZero) return other
        if (other.isZero) return this
        val sum = terms.toMutableMap()
        for ((tag, b) in other.terms) {
            val a = sum[tag]
            val coefficient = if (a == null) b else reduce(
                a.numerator * b.denominator + b.numerator * a.denominator,
                a.denominator * b.denominator,
            ) ?: return null
            if (coefficient.numerator.isZero()) sum.remove(tag) else sum[tag] = coefficient
        }
        return fromTerms(sum)
    }

    fun multiply(other: SymbolicNumber): SymbolicNumber? {
        if (isZero || other.isZero) return ZERO
        var result = ZERO
        for ((aTag, a) in terms) for ((bTag, b) in other.terms) {
            val n = a.numerator * b.numerator
            val d = a.denominator * b.denominator
            val product = when {
                aTag == Factor.One -> create(n, d, bTag)
                bTag == Factor.One -> create(n, d, aTag)
                aTag is Factor.Root && bTag is Factor.Root && aTag.degree == bTag.degree ->
                    rootProduct(n, d, aTag, bTag)
                aTag is Factor.Constant && bTag is Factor.Constant && aTag.pi == bTag.pi ->
                    constant(n, d, aTag.pi, aTag.power + bTag.power)
                else -> null
            } ?: return null
            result = result.add(product) ?: return null
        }
        return result
    }

    fun divide(other: SymbolicNumber): SymbolicNumber? {
        if (other.isZero) throw CalcException(CalcError.DIVIDE_BY_ZERO)
        val tag = other.factor ?: return null
        if (tag == factor) return create(n * other.d, d * other.n)
        val inverse = when (tag) {
            Factor.One -> create(other.d, other.n)
            is Factor.Constant -> constant(other.d, other.n, tag.pi, -tag.power)
            is Factor.Root -> {
                val r = tag.radicand.toLong()
                rootTerm(other.d, other.n * BigInteger.fromLong(r), if (tag.degree == 2) r else r * r, tag.degree)
            }
            is Factor.LogPrime -> null
        } ?: return null
        return multiply(inverse)
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
        if (isZero) return ZERO
        val a = n.sqrt()
        val b = d.sqrt()
        if (a * a == n && b * b == d) return create(a, b)
        // sqrt(n/d) = sqrt(n*d)/d.
        val radicand = n * d
        if (radicand > MAX_RADICAND) return null
        return rootTerm(BigInteger.ONE, d, radicand.longValue(), 2)
    }

    fun cubeRoot(): SymbolicNumber? {
        if (factor != Factor.One) return null
        val numerator = integerCubeRoot(n)
        val denominator = integerCubeRoot(d)
        if (numerator.pow(3) == n && denominator.pow(3) == d) return create(numerator, denominator)
        // cbrt(n/d) = cbrt(n*d²)/d.
        val radicand = n.abs() * d * d
        if (radicand > MAX_RADICAND) return null
        return rootTerm(BigInteger.fromInt(n.signum()), d, radicand.longValue(), 3)
    }

    fun naturalLog(): SymbolicNumber? {
        val tag = factor
        if (tag is Factor.Constant && !tag.pi && n == d) return fromDecimal(Decimal(tag.power))
        if (tag != Factor.One || n.signum() <= 0 || n > MAX_RADICAND || d > MAX_RADICAND) return null

        val factors = mutableMapOf<Factor, Fraction>()
        fun factorize(value: Int, sign: Int) {
            var rest = value
            var prime = 2
            fun record(prime: Int, multiplicity: Int) {
                factors[Factor.LogPrime(prime)] = Fraction(BigInteger.fromInt(sign * multiplicity), BigInteger.ONE)
            }
            while (prime * prime <= rest) {
                var multiplicity = 0
                while (rest % prime == 0) {
                    rest /= prime
                    multiplicity++
                }
                if (multiplicity > 0) record(prime, multiplicity)
                prime = if (prime == 2) 3 else prime + 2
            }
            if (rest > 1) record(rest, 1)
        }
        // The rational is reduced, so its numerator and denominator share no prime.
        factorize(n.intValue(), 1)
        factorize(d.intValue(), -1)
        return fromTerms(factors)
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
        else create(BigInteger.fromInt(step), BigInteger.fromInt(12), PI_FACTOR)
    }

    fun periodicCoefficient(angle: AngleUnit): Fraction? {
        val period = when {
            angle == AngleUnit.DEGREES && factor == Factor.One -> 360
            angle == AngleUnit.RADIANS && factor == PI_FACTOR -> 2
            else -> return null
        }
        return Fraction(n.rem(d * period), d)
    }

    fun trig(name: String, angle: AngleUnit): SymbolicNumber? {
        val divisor = when {
            angle == AngleUnit.DEGREES && factor == Factor.One -> d * BigInteger.fromInt(180)
            angle == AngleUnit.RADIANS && factor == PI_FACTOR -> d
            isZero -> d
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
                if (cosine.isZero) throw CalcException(CalcError.DOMAIN)
                sine?.divide(cosine)
            }
            else -> null
        }
    }

    fun approximation(context: MathContext): Value {
        if (fraction?.denominator == BigInteger.ONE) return Value(Decimal(n), symbolic = this)
        val work = context.withPrecision(context.precision + 8)
        val constants = context.constants ?: DecimalConstants {}
        var result = Value(Decimal.ZERO, symbolic = null)
        for ((factor, fraction) in terms) {
            constants.check()
            val numerator = Value(Decimal(fraction.numerator), symbolic = null)
            val coefficient = if (fraction.denominator == BigInteger.ONE) numerator else
                numerator.divide(Value(Decimal(fraction.denominator), symbolic = null), work)
            val term = when (factor) {
                Factor.One -> coefficient
                is Factor.Constant -> {
                    val constant = if (factor.pi) constants.pi(work) else constants.e(work)
                    val power = if (factor.power == 1) constant else
                        CalcEngine.integerPower(constant, Decimal(factor.power), work, constants::check)
                    coefficient.multiply(power, work)
                }
                is Factor.Root -> {
                    val radicand = Decimal(factor.radicand)
                    val root = if (factor.degree == 2) CalcEngine.sqrt(radicand, work) else CalcEngine.cbrt(radicand, work)
                    coefficient.multiply(Value(root,
                        error = DecimalError.root(radicand, Decimal.ZERO, root, factor.degree), symbolic = null), work)
                }
                is Factor.LogPrime -> {
                    val argument = Value(Decimal(factor.prime), symbolic = null)
                    coefficient.multiply(DecimalLogarithm.natural(argument, work, constants::check), work)
                }
            }
            result = result.add(term, work)
        }
        return CalcEngine.roundedValue(result.v, result.error, context).copy(symbolic = this)
    }

    fun exactText(): String {
        if (isZero) return "0"
        return orderedTerms().mapIndexed { index, (factor, f) ->
            val tag = when (factor) {
                Factor.One -> ""
                is Factor.Constant -> {
                    val base = if (factor.pi) "π" else "e"
                    if (factor.power == 1) base else "$base^${factor.power}".replace('-', '−')
                }
                is Factor.Root -> "${if (factor.degree == 2) "√" else "∛"}${factor.radicand}"
                is Factor.LogPrime -> "ln(${factor.prime})"
            }
            val sign = if (f.numerator.signum() < 0) "−" else if (index == 0) "" else "+"
            val magnitude = f.numerator.abs()
            val numerator = if (tag.isNotEmpty() && magnitude == BigInteger.ONE) tag else "$magnitude$tag"
            if (f.denominator == BigInteger.ONE) "$sign$numerator" else "$sign$numerator⁄${f.denominator}"
        }.joinToString("")
    }

    fun save(): String = if (isZero) "0,1,1" else orderedTerms().joinToString(";") { (factor, f) ->
        "${f.numerator},${f.denominator},${tag(factor)}"
    }

    private fun orderedTerms() = terms.entries.sortedBy { tag(it.key) }

    companion object {
        internal const val MAX_BITS = 1_024
        private const val MAX_DECIMAL_DIGITS = 309
        private const val MAX_RADICAND = 1_000_000
        private const val MAX_TERMS = 8
        private val PI_FACTOR = Factor.Constant(pi = true, power = 1)
        private val E_FACTOR = Factor.Constant(pi = false, power = 1)
        private val ZERO_FRACTION = Fraction(BigInteger.ZERO, BigInteger.ONE)
        private val ONE_FRACTION = Fraction(BigInteger.ONE, BigInteger.ONE)
        private val ZERO = SymbolicNumber(emptyMap())
        val ONE = SymbolicNumber(mapOf(Factor.One to ONE_FRACTION))
        val PI = SymbolicNumber(mapOf(PI_FACTOR to ONE_FRACTION))
        val E = SymbolicNumber(mapOf(E_FACTOR to ONE_FRACTION))

        fun fromDecimal(value: Decimal): SymbolicNumber? {
            if (value.signum() == 0) return ZERO
            if (value.scale() !in -MAX_DECIMAL_DIGITS..MAX_DECIMAL_DIGITS || value.unscaled.abs().bitLength() > MAX_BITS) return null
            return if (value.scale() <= 0) create(value.toBigInteger(), BigInteger.ONE)
            else create(value.unscaled, BigInteger.TEN.pow(value.scale()))
        }

        fun restore(saved: String): SymbolicNumber? {
            val encoded = saved.split(';', limit = MAX_TERMS + 1)
            if (encoded.size > MAX_TERMS) return null
            var result = ZERO
            for (term in encoded) {
                val fields = term.split(',', limit = 4)
                if (fields.size != 3 || fields.any { it.length > MAX_DECIMAL_DIGITS + 1 }) return null
                val (numerator, denominator, encodedTag) = fields
                val factor = when (encodedTag) {
                    "1" -> Factor.One
                    "pi" -> PI_FACTOR
                    "e" -> E_FACTOR
                    else -> when {
                        encodedTag.startsWith("pi^") || encodedTag.startsWith("e^") -> {
                            val power = encodedTag.substringAfter('^').toIntOrNull()
                                ?.takeIf { it in -MAX_BITS..MAX_BITS && it != 0 } ?: return null
                            Factor.Constant(encodedTag.startsWith("pi^"), power)
                        }
                        encodedTag.startsWith("ln") -> Factor.LogPrime(parseRadicand(encodedTag.drop(2)) ?: return null)
                        encodedTag.startsWith("r3_") -> Factor.Root(parseRadicand(encodedTag.drop(3)) ?: return null, 3)
                        else -> Factor.Root(parseRadicand(encodedTag) ?: return null, 2)
                    }
                }
                val number = runCatching {
                    val n = BigInteger.parseString(numerator)
                    val d = BigInteger.parseString(denominator)
                    if (factor is Factor.Root) rootTerm(n, d, factor.radicand.toLong(), factor.degree)
                    else create(n, d, factor)
                }.getOrNull() ?: return null
                result = result.add(number) ?: return null
            }
            return result
        }

        private fun parseRadicand(text: String): Int? = text.toIntOrNull()?.takeIf { it in 2..MAX_RADICAND }

        private fun tag(factor: Factor): String = when (factor) {
            Factor.One -> "1"
            is Factor.Constant -> {
                val base = if (factor.pi) "pi" else "e"
                if (factor.power == 1) base else "$base^${factor.power}"
            }
            is Factor.Root -> if (factor.degree == 2) factor.radicand.toString() else "r3_${factor.radicand}"
            is Factor.LogPrime -> "ln${factor.prime}"
        }

        private fun fromTerms(terms: Map<Factor, Fraction>): SymbolicNumber? =
            if (terms.size <= MAX_TERMS) SymbolicNumber(terms) else null

        private fun create(n: BigInteger, d: BigInteger, factor: Factor = Factor.One): SymbolicNumber? {
            val coefficient = reduce(n, d) ?: return null
            return if (coefficient.numerator.isZero()) ZERO else SymbolicNumber(mapOf(factor to coefficient))
        }

        private fun reduce(n: BigInteger, d: BigInteger): Fraction? {
            if (d.isZero()) return null
            if (n.isZero()) return ZERO_FRACTION
            if (d == BigInteger.ONE) return Fraction(n, d).takeIf { n.abs().bitLength() <= MAX_BITS }
            val gcd = n.abs().gcd(d.abs())
            val numerator = if (d.signum() < 0) n.negate() / gcd else n / gcd
            val denominator = d.abs() / gcd
            if (numerator.abs().bitLength() > MAX_BITS || denominator.bitLength() > MAX_BITS) return null
            return Fraction(numerator, denominator)
        }

        private fun constant(n: BigInteger, d: BigInteger, pi: Boolean, power: Int): SymbolicNumber? =
            if (abs(power) > MAX_BITS) null else create(n, d, if (power == 0) Factor.One else Factor.Constant(pi, power))

        private fun rootProduct(n: BigInteger, d: BigInteger, a: Factor.Root, b: Factor.Root): SymbolicNumber? {
            val radicand = a.radicand.toLong() * b.radicand
            if (a.degree == 3) return rootTerm(n, d, radicand, 3)
            // The inputs are square-free. Only their common factors can leave the root.
            var x = a.radicand
            var y = b.radicand
            while (y != 0) {
                val remainder = x % y
                x = y
                y = remainder
            }
            val inside = radicand / (x.toLong() * x)
            if (inside > MAX_RADICAND) return null
            return create(n * BigInteger.fromInt(x), d, if (inside == 1L) Factor.One else Factor.Root(inside.toInt(), 2))
        }

        private fun rootTerm(n: BigInteger, d: BigInteger, radicand: Long, degree: Int): SymbolicNumber? {
            var inside = radicand
            var outside = 1L
            var factor = 2L
            while (true) {
                val power = if (degree == 2) factor * factor else factor * factor * factor
                if (power > inside) break
                while (inside % power == 0L) {
                    inside /= power
                    outside *= factor
                }
                factor++
            }
            if (inside > MAX_RADICAND) return null
            val tag = if (inside == 1L) Factor.One else Factor.Root(inside.toInt(), degree)
            return create(n * BigInteger.fromLong(outside), d, tag)
        }

        private fun sinStep(step: Int): SymbolicNumber? {
            val half = step % 12
            val firstQuadrant = minOf(half, 12 - half)
            val value = when (firstQuadrant) {
                0 -> ZERO
                2 -> create(BigInteger.ONE, BigInteger.TWO)
                3 -> create(BigInteger.ONE, BigInteger.TWO, Factor.Root(2, 2))
                4 -> create(BigInteger.ONE, BigInteger.TWO, Factor.Root(3, 2))
                6 -> ONE
                else -> null
            }
            return if (step >= 12) value?.negate() else value
        }
    }
}
