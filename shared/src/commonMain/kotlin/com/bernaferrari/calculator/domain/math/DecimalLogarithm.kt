package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.math.CalcEngine.Value

/** Natural logarithm at the requested precision, with a radius that includes the series tail. */
internal object DecimalLogarithm {
    private val half = Decimal("0.5")
    private val two = Decimal(2)
    private val outward = MathContext(12, RoundingMode.UP)

    fun natural(input: Value, context: MathContext, checkpoint: () -> Unit): Value {
        val sign = input.v.signum()
        val radius = input.error
        if (radius != null && radius.signum() > 0 &&
            (if (sign > 0) input.v <= radius else input.v.abs() < radius)
        ) throw DomainNeedsPrecision()
        if (sign <= 0) throw CalcException(CalcError.DOMAIN)
        if (input.symbolic == SymbolicNumber.E) return Value(Decimal.ONE)

        val work = context.withPrecision(context.precision + 12)
        var reduced = input.v
        var decades = 0
        var halvings = 0
        // Near one, avoid subtracting two large logarithms to get a small result.
        if (reduced < half || reduced > two) {
            decades = reduced.precision() - reduced.scale() - 1
            reduced = reduced.scaleByPowerOfTen(-decades)
            while (reduced > two) {
                reduced = reduced.multiply(half)
                halvings++
            }
        }

        val constants = context.constants ?: DecimalConstants(checkpoint)
        var result = when (reduced) {
            two -> constants.lnTwo(work)
            half -> constants.lnTwo(work).negate()
            else -> nearOne(reduced.subtract(Decimal.ONE), work, checkpoint)
        }
        if (halvings != 0) result = result.add(constants.lnTwo(work).multiply(number(Decimal(halvings)), work), work)
        if (decades != 0) result = result.add(constants.lnTen(work).multiply(number(Decimal(decades)), work), work)

        // |ln(x) − ln(center)| <= radius / lower, since 1/x decreases on the positive axis.
        val inherited = inputRadius(input)
        val center = result.v.round(context)
        val error = DecimalError.round(result.v, center, DecimalError.add(result.error, inherited))
        return Value(center, error = error, retryable = input.retryable)
    }

    fun onePlus(increment: Value, context: MathContext, checkpoint: () -> Unit): Value {
        val radius = increment.error
        if (increment.v.abs() > half || radius == null || radius > Decimal("0.25")) {
            val work = context.withPrecision(context.precision + 12)
            return natural(number(Decimal.ONE).add(increment, work), context, checkpoint)
        }
        val work = context.withPrecision(context.precision + 12)
        val result = nearOne(increment.v, work, checkpoint)
        // 1 + increment is at least 1/4 throughout this interval.
        val bound = DecimalError.add(result.error, radius.multiply(Decimal(4)))
        return CalcEngine.roundedValue(result.v, bound, context)
    }

    private fun inputRadius(input: Value): Decimal? {
        val radius = input.error ?: return null
        if (radius.signum() == 0) return Decimal.ZERO
        if (radius >= input.v) return null
        // Avoid aligning thousands of decimal places when the radius is very small.
        val halfCenter = input.v.multiply(half)
        val lower = if (radius <= halfCenter) halfCenter else input.v.subtract(radius)
        return radius.divide(lower, outward)
    }

    /** ln(x) = 2 atanh((x−1)/(x+1)). The reduced input is between 1/2 and 2. */
    private fun nearOne(delta: Decimal, work: MathContext, checkpoint: () -> Unit): Value {
        if (delta.signum() == 0) return number(Decimal.ZERO)
        val denominator = number(two).add(number(delta), work)
        val z = number(delta).divide(denominator, work)
        return OddPowerSeries.evaluate(z, work, alternating = false, checkpoint).multiply(number(two), work)
    }

    private fun number(value: Decimal): Value = Value(value, symbolic = null)
}
