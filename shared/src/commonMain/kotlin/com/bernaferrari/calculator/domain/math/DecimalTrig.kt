package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.math.CalcEngine.Value
import com.bernaferrari.calculator.domain.model.AngleUnit

/** Decimal trig with explicit range-reduction and series error. */
internal class DecimalTrig(private val context: MathContext, private val checkpoint: () -> Unit) {
    private val work = context.withPrecision(context.precision + 12)
    private val outward = MathContext(12, RoundingMode.UP)
    private val zero = number(0)
    private val one = number(1)
    private val two = number(2)
    private val constants = context.constants ?: DecimalConstants(checkpoint)

    fun circular(name: String, input: Value, angle: AngleUnit): Value {
        val reduction = reduce(input, angle)
        val quarter = reduction.quarter
        fun component(cosine: Boolean): Value {
            val useCosine = cosine != (quarter % 2 == 1)
            val negate = if (cosine) quarter == 1 || quarter == 2 else quarter >= 2
            val series = series(reduction.angle.v, useCosine, alternating = true)
            val withError = series.copy(error = DecimalError.add(series.error, reduction.angle.error))
            return if (negate) withError.negate() else withError
        }
        val result = when (name) {
            "sin" -> component(cosine = false)
            "cos" -> component(cosine = true)
            "tan" -> quotient(component(cosine = false), component(cosine = true))
            else -> throw CalcException(CalcError.SYNTAX)
        }
        return CalcEngine.roundedValue(result.v, result.error, context)
    }

    fun inverse(name: String, input: Value, angle: AngleUnit): Value {
        val result = when (name) {
            "tan⁻¹" -> atan(input)
            "sin⁻¹", "cos⁻¹" -> {
                val magnitude = input.v.abs()
                val radius = input.error
                if (radius != null && radius.signum() > 0 &&
                    magnitude.subtract(Decimal.ONE).abs() <= radius
                ) throw DomainNeedsPrecision()
                if (magnitude > Decimal.ONE) throw CalcException(CalcError.DOMAIN)

                val positive = if (input.v.signum() < 0) input.negate() else input
                val gap = one.add(positive.negate(), work)
                val sum = one.add(positive, work)
                if (name == "cos⁻¹") {
                    // acos(x) = 2 atan(sqrt((1−x)/(1+x))) for x >= 0.
                    val principal = atan(root(quotient(gap, sum))).multiply(two, work)
                    if (input.v.signum() < 0) constants.pi(work).add(principal.negate(), work) else principal
                } else if (magnitude == Decimal.ONE && input.exact) {
                    val halfPi = constants.pi(work).divide(two, work)
                    if (input.v.signum() < 0) halfPi.negate() else halfPi
                } else {
                    atan(quotient(input, root(gap.multiply(sum, work))))
                }
            }
            else -> throw CalcException(CalcError.SYNTAX)
        }
        val converted = if (angle == AngleUnit.RADIANS) result
            else result.multiply(number(180), work).divide(constants.pi(work), work)
        return CalcEngine.roundedValue(converted.v, converted.error, context)
    }

    fun hyperbolic(name: String, input: Value): Value {
        val result = when (name) {
            "sinh", "cosh", "tanh" -> hyperbolicForward(name, input)
            "sinh⁻¹" -> {
                val positive = if (input.v.signum() < 0) input.negate() else input
                val square = positive.multiply(positive, work)
                val denominator = root(one.add(square, work)).add(one, work)
                val increment = positive.add(square.divide(denominator, work), work)
                val principal = DecimalLogarithm.onePlus(increment, work, checkpoint)
                if (input.v.signum() < 0) principal.negate() else principal
            }
            "cosh⁻¹" -> {
                val gap = input.add(one.negate(), work)
                val increment = gap.add(root(gap.multiply(input.add(one, work), work)), work)
                DecimalLogarithm.onePlus(increment, work, checkpoint)
            }
            "tanh⁻¹" -> {
                val positive = DecimalLogarithm.onePlus(input, work, checkpoint)
                val negative = DecimalLogarithm.onePlus(input.negate(), work, checkpoint)
                positive.add(negative.negate(), work).divide(two, work)
            }
            else -> throw CalcException(CalcError.SYNTAX)
        }
        return CalcEngine.roundedValue(result.v, result.error, context)
    }

    private fun hyperbolicForward(name: String, input: Value): Value {
        val magnitude = input.v.abs()
        val radius = input.error
        if (magnitude <= Decimal.ONE && radius != null && radius <= Decimal("0.5")) {
            fun component(cosine: Boolean): Value {
                val value = series(input.v, cosine, alternating = false)
                // On [−1.5, 1.5], the magnitudes of both derivatives are below 3.
                return value.copy(error = DecimalError.add(value.error, radius.multiply(Decimal(3))))
            }
            return when (name) {
                "sinh" -> component(cosine = false)
                "cosh" -> component(cosine = true)
                else -> quotient(component(cosine = false), component(cosine = true))
            }
        }

        if (name == "tanh") {
            val positive = if (input.v.signum() < 0) input.negate() else input
            val decay = CalcEngine.exponential(positive.multiply(number(-2), work), work, checkpoint)
            val value = quotient(one.add(decay.negate(), work), one.add(decay, work))
            return if (input.v.signum() < 0) value.negate() else value
        }
        val up = CalcEngine.exponential(input, work, checkpoint)
        val down = CalcEngine.exponential(input.negate(), work, checkpoint)
        return up.add(if (name == "sinh") down.negate() else down, work).divide(two, work)
    }

    private data class Reduction(val quarter: Int, val angle: Value)

    private fun reduce(input: Value, angle: AngleUnit): Reduction {
        val symbolicRemainder = input.symbolic?.periodicCoefficient(angle)?.let {
            number(Decimal(it.numerator)).divide(number(Decimal(it.denominator)), work)
        }
        if (angle == AngleUnit.DEGREES) {
            val degrees = symbolicRemainder ?: Value(degreeRemainder(input.v), error = input.error, symbolic = null)
            val quarter = degrees.v.divide(Decimal(90), work).setScale(0, RoundingMode.HALF_UP)
            val remainder = degrees.add(number(quarter.multiply(Decimal(-90))), work)
            val radians = remainder.multiply(constants.pi(work), work).divide(number(180), work)
            return Reduction(quarterIndex(quarter), radians)
        }

        val radians = symbolicRemainder?.multiply(constants.pi(work), work) ?: input
        val integerDigits = (radians.v.precision().toLong() - radians.v.scale()).coerceAtLeast(0)
        val precision = work.precision + integerDigits
        if (precision > MAX_REDUCTION_PRECISION) throw CalcException(CalcError.PRECISION_LIMIT)
        val reduction = MathContext(precision.toInt())
        val halfPi = constants.pi(reduction).divide(two, reduction)
        val quarter = radians.v.divide(halfPi.v, reduction).setScale(0, RoundingMode.HALF_UP)
        val offset = halfPi.multiply(number(quarter), reduction)
        val remainder = radians.add(offset.negate(), reduction)
        // The integer can be off by a turn only if the available π precision is insufficient.
        // Any selected integer is valid when its remainder lies inside the series range.
        if (remainder.v.abs() > Decimal.ONE) throw DomainNeedsPrecision()
        return Reduction(quarterIndex(quarter), remainder)
    }

    private fun degreeRemainder(value: Decimal): Decimal {
        if (value.abs() < Decimal(360)) return value
        if (value.scale() >= 0) return value.remainder(Decimal(360))
        val unit = when (value.scale()) { -1 -> 10; -2 -> 100; else -> 280 }
        val coefficient = value.unscaled.rem(BigInteger.fromInt(360)).intValue()
        return Decimal(coefficient * unit % 360)
    }

    private fun quarterIndex(quarter: Decimal): Int {
        val mod = quarter.remainder(Decimal(4)).toInt()
        return (mod + 4) % 4
    }

    private fun atan(input: Value): Value {
        val magnitude = input.v.abs()
        if (magnitude.signum() == 0) return zero.copy(error = input.error)
        val reciprocal = magnitude > Decimal.ONE
        var reduced = if (reciprocal) one.divide(number(magnitude), work) else number(magnitude)
        var doubling = 1
        while (reduced.v > Decimal("0.25")) {
            checkpoint()
            // atan(x) = 2 atan(x / (1 + sqrt(1 + x²))).
            val denominator = one.add(root(one.add(reduced.multiply(reduced, work), work)), work)
            reduced = reduced.divide(denominator, work)
            doubling *= 2
        }
        val series = OddPowerSeries.evaluate(reduced, work, alternating = true, checkpoint).multiply(number(doubling), work)
        val principal = if (reciprocal) constants.pi(work).divide(two, work).add(series.negate(), work) else series
        val inputError = input.error?.let { radius ->
            if (radius.signum() == 0) Decimal.ZERO
            else if (radius <= magnitude.multiply(Decimal("0.5"))) {
                val lower = magnitude.multiply(Decimal("0.5"))
                val denominator = Decimal.ONE.add(lower.multiply(lower), MathContext(12, RoundingMode.DOWN))
                radius.divide(denominator, outward)
            } else radius // |atan′| <= 1 globally.
        }
        val result = principal.copy(error = DecimalError.add(principal.error, inputError))
        return if (input.v.signum() < 0) result.negate() else result
    }

    private fun series(center: Decimal, cosine: Boolean, alternating: Boolean): Value {
        if (center.signum() == 0) return if (cosine) one else zero
        if (center.abs() >= Decimal("0.01")) return fixedPointSeries(center, cosine, alternating)
        val x = number(center.abs())
        val square = x.multiply(x, work)
        var term = if (cosine) one else x
        val cutoff = term.v.scaleByPowerOfTen(-work.precision)
        var sum = term
        var n = if (cosine) 0 else 1
        var subtract = alternating
        while (true) {
            checkpoint()
            term = term.multiply(square, work).divide(number((n + 1) * (n + 2)), work)
            val upper = upper(term)
            if (upper < cutoff) break
            sum = sum.add(if (subtract) term.negate() else term, work)
            if (alternating) subtract = !subtract
            n += 2
        }
        val result = withTail(sum, upper(term))
        return if (!cosine && center.signum() < 0) result.negate() else result
    }

    private fun fixedPointSeries(center: Decimal, cosine: Boolean, alternating: Boolean): Value {
        val scale = work.precision + 8
        val unit = BigInteger.TEN.pow(scale)
        val x = center.abs().setScale(scale, RoundingMode.DOWN).unscaled
        val square = x * x / unit
        var term = if (cosine) unit else x
        var sum = term
        var degree = if (cosine) 0 else 1
        var subtract = alternating
        var count = 1
        while (true) {
            checkpoint()
            term = term * square / unit / ((degree + 1) * (degree + 2))
            if (term.isZero()) break
            sum = if (subtract) sum - term else sum + term
            if (alternating) subtract = !subtract
            degree += 2
            count++
        }
        // For |x| <= 1, the term ratio is at most 1/2. Rounding x changes
        // these functions by < 2 units. Each recurrence loses < 1.5 units,
        // so each term is within 3 units, and the omitted tail is < 6.
        val magnitude = sum.toDecimal(scale)
        val signed = if (!cosine && center.signum() < 0) magnitude.negate() else magnitude
        return Value(signed,
            error = Decimal(3 * count + 8).scaleByPowerOfTen(-scale), symbolic = null)
    }

    private fun root(input: Value): Value {
        if (input.error?.let { it.signum() > 0 && input.v.abs() < it } == true) throw DomainNeedsPrecision()
        val center = CalcEngine.sqrt(input.v, work)
        return Value(center, error = DecimalError.root(input.v, input.error, center, 2), symbolic = null)
    }

    private fun quotient(numerator: Value, denominator: Value): Value {
        val radius = denominator.error
        if (radius != null && radius.signum() > 0 && denominator.v.abs() <= radius) throw DomainNeedsPrecision()
        if (denominator.v.signum() == 0) throw CalcException(CalcError.DOMAIN)
        return numerator.divide(denominator, work)
    }

    private fun upper(term: Value): Decimal = checkNotNull(DecimalError.add(term.v.abs(), term.error))

    // Every reduced series used here has a term ratio below 1/2.
    private fun withTail(sum: Value, upper: Decimal): Value =
        sum.copy(error = DecimalError.add(sum.error, upper.multiply(Decimal(2))))

    private fun number(value: Int): Value = number(Decimal(value))
    private fun number(value: Decimal): Value = Value(value, symbolic = null)

    private companion object {
        const val MAX_REDUCTION_PRECISION = 8_192
    }
}
