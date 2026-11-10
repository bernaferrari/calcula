package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.math.CalcEngine.Value

/** Natural logarithm at the requested precision, with a radius that includes the series tail. */
internal object DecimalLogarithm {
    private val half = Decimal("0.5")
    private val two = Decimal(2)
    private val outward = MathContext(12, RoundingMode.UP)

    // Truncated at 187 decimal places. Each true constant is within one unit of its last place.
    private val constantRadius = Decimal("1E-187")
    val lnTwo = Value(Decimal(
        "0.6931471805599453094172321214581765680755001343602552541206800094933936219696947156058633269964186875420014810205706857336855202357581305570326707516350759619307275708283714351903070386238",
    ), error = constantRadius)
    val lnTen = Value(Decimal(
        "2.3025850929940456840179914546843642076011014886287729760333279009675726096773524802359972050895982983419677840422862486334095254650828067566662873690987816894829072083255546808437998948262",
    ), error = constantRadius)

    fun natural(input: Value, context: MathContext, checkpoint: () -> Unit): Value {
        val sign = input.v.signum()
        val radius = input.error
        if (radius != null && radius.signum() > 0 &&
            (if (sign > 0) input.v <= radius else input.v.abs() < radius)
        ) throw DomainNeedsPrecision()
        if (sign <= 0) throw CalcException(CalcError.DOMAIN)
        if (input.symbolic == SymbolicNumber.E) return Value(Decimal.ONE)

        val work = MathContext(context.precision + 12)
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

        var result = reducedLog(reduced, work, checkpoint)
        if (halvings != 0) result = result.add(lnTwo.multiply(number(Decimal(halvings)), work), work)
        if (decades != 0) result = result.add(lnTen.multiply(number(Decimal(decades)), work), work)

        // |ln(x) − ln(center)| <= radius / lower, since 1/x decreases on the positive axis.
        val inherited = inputRadius(input)
        val center = result.v.round(context)
        val error = DecimalError.round(result.v, center, DecimalError.add(result.error, inherited))
        return Value(center, error = error, retryable = input.retryable)
    }

    fun onePlus(increment: Value, context: MathContext, checkpoint: () -> Unit): Value {
        val radius = increment.error
        if (increment.v.abs() > half || radius == null || radius > Decimal("0.25")) {
            val work = MathContext(context.precision + 12)
            return natural(number(Decimal.ONE).add(increment, work), context, checkpoint)
        }
        val work = MathContext(context.precision + 12)
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
    private fun reducedLog(x: Decimal, work: MathContext, checkpoint: () -> Unit): Value =
        nearOne(x.subtract(Decimal.ONE), work, checkpoint)

    private fun nearOne(delta: Decimal, work: MathContext, checkpoint: () -> Unit): Value {
        if (delta.signum() == 0) return number(Decimal.ZERO)
        val denominatorValue = number(two).add(number(delta), work)
        val z = number(delta.abs()).divide(denominatorValue, work)
        val square = z.multiply(z, work)
        val cutoff = z.v.scaleByPowerOfTen(-work.precision)
        var sum = z
        var power = z
        var denominator = 3
        while (true) {
            checkpoint()
            power = power.multiply(square, work)
            val term = power.divide(number(Decimal(denominator)), work)
            val upper = checkNotNull(DecimalError.add(term.v, term.error))
            if (upper < cutoff) {
                // The omitted terms have ratio at most 1/9. Twice the first term bounds their sum.
                sum = sum.copy(error = DecimalError.add(sum.error, upper.multiply(two)))
                break
            }
            sum = sum.add(term, work)
            denominator += 2
        }
        val value = sum.multiply(number(two), work)
        return if (delta.signum() < 0) value.negate() else value
    }

    private fun number(value: Decimal): Value = Value(value, symbolic = null)
}
