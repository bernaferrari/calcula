package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.math.CalcEngine.Value

/** Evaluates atan or atanh after reducing the argument to at most 1/2 in magnitude. */
internal object OddPowerSeries {
    private val half = Decimal("0.5")

    fun evaluate(input: Value, context: MathContext, alternating: Boolean, checkpoint: () -> Unit): Value {
        val magnitude = input.v.abs()
        val inputRadius = input.error ?: throw DomainNeedsPrecision()
        val upper = checkNotNull(DecimalError.add(magnitude, inputRadius))
        if (upper > half) throw DomainNeedsPrecision()

        // Keep significant digits for small inputs without expanding the scale of every product.
        val offset = (magnitude.scale() - magnitude.precision()).coerceAtLeast(0)
        val scale = context.precision + 8
        val unit = BigInteger.TEN.pow(scale)
        val first = magnitude.scaleByPowerOfTen(offset).setScale(scale, RoundingMode.DOWN).unscaled
        val square = magnitude.multiply(magnitude)
        val ratio = if (square.precision() - square.scale() <= -scale) BigInteger.ZERO
            else square.setScale(scale, RoundingMode.DOWN).unscaled
        val series = sum(first, scale + offset, alternating, checkpoint) { power -> power * ratio / unit }
        // On the reduced interval |atan'| <= 1 and |atanh'| <= 4/3.
        val inherited = if (alternating) inputRadius else inputRadius.multiply(Decimal(2))
        val radius = DecimalError.add(series.error, inherited)
        val center = if (input.v.signum() < 0) series.v.negate() else series.v
        return CalcEngine.roundedValue(center, radius, context)
    }

    fun reciprocal(divisor: Int, context: MathContext, alternating: Boolean, checkpoint: () -> Unit): Value {
        val scale = context.precision + 8
        val first = BigInteger.TEN.pow(scale) / divisor
        val square = divisor * divisor
        return sum(first, scale, alternating, checkpoint) { power -> power / square }
    }

    private fun sum(
        first: BigInteger,
        scale: Int,
        alternating: Boolean,
        checkpoint: () -> Unit,
        advance: (BigInteger) -> BigInteger,
    ): Value {
        var power = first
        var total = BigInteger.ZERO
        var count = 0
        while (true) {
            checkpoint()
            val term = power / (2 * count + 1)
            if (term.isZero()) break
            total = if (alternating && count % 2 != 0) total - term else total + term
            count++
            power = advance(power)
        }
        // At most two units are lost per power, with a ratio <= 1/4. Each power
        // is within three units and each term within four. The tail is below six.
        val radius = Decimal(4 * count + 6).scaleByPowerOfTen(-scale)
        return Value(total.toDecimal(scale), error = radius, symbolic = null)
    }
}
