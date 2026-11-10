package com.bernaferrari.calculator.domain.math

/** Radius arithmetic for the decimal fallback. Null denotes an unmodelled error. */
internal object DecimalError {
    private val outward = MathContext(12, RoundingMode.UP)
    private val half = Decimal("0.5")

    fun add(a: Decimal?, b: Decimal?): Decimal? {
        if (a == null || b == null) return null
        if (a.signum() == 0) return b
        if (b.signum() == 0) return a
        val larger = maxOf(a, b)
        val smaller = minOf(a, b)
        // Adding one outward ULP encloses a much smaller radius without aligning huge scales.
        val unit = roundingUnit(larger, outward)
        return larger.add(maxOf(smaller, unit), outward)
    }

    fun round(value: Decimal, rounded: Decimal, radius: Decimal?): Decimal? =
        if (radius == null || value === rounded) radius else add(radius, value.subtract(rounded).abs())

    fun multiply(a: Decimal, ra: Decimal?, b: Decimal, rb: Decimal?): Decimal? {
        if (ra == null || rb == null) return null
        if (ra.signum() == 0) return if (rb.signum() == 0) Decimal.ZERO else a.abs().multiply(rb, outward)
        if (rb.signum() == 0) return b.abs().multiply(ra, outward)
        return add(add(a.abs().multiply(rb, outward), b.abs().multiply(ra, outward)), ra.multiply(rb, outward))
    }

    fun divide(a: Decimal, ra: Decimal?, b: Decimal, rb: Decimal?, quotient: Decimal): Decimal? {
        if (ra == null || rb == null || b.abs() <= rb) return null
        val residual = a.subtract(quotient.multiply(b)).abs()
        val numerator = add(add(residual, ra), quotient.abs().multiply(rb, outward)) ?: return null
        if (numerator.signum() == 0) return Decimal.ZERO
        val denominator = when {
            rb.signum() == 0 -> b.abs()
            b.abs().multiply(half) >= rb -> b.abs().multiply(half)
            else -> b.abs().subtract(rb)
        }
        return numerator.divide(denominator, outward)
    }

    fun root(input: Decimal, radius: Decimal?, result: Decimal, degree: Int): Decimal? {
        if (radius == null) return null
        if (result.signum() == 0) return Decimal.ZERO.takeIf { radius.signum() == 0 }
        val residual = result.pow(degree).subtract(input).abs()
        val numerator = add(residual, radius) ?: return null
        // |g²−x|/|g| encloses the square-root error. For the cube root, g²/2 is a
        // lower bound on the factored denominator even when the two roots have opposite signs.
        val denominator = if (degree == 2) result.abs() else result.pow(2).multiply(half)
        return numerator.divide(denominator, outward)
    }

    fun roundingUnit(value: Decimal, context: MathContext): Decimal =
        Decimal.ONE.scaleByPowerOfTen(value.precision() - value.scale() - context.precision)
}
