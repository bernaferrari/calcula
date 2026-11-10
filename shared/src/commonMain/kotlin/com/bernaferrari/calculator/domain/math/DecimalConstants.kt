package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.math.CalcEngine.Value

/** Constant enclosures cached only for the lifetime of one evaluation. */
internal class DecimalConstants(private val checkpoint: () -> Unit) {
    fun check() = checkpoint()

    private enum class Constant { PI, E, LN_TWO, LN_TEN }

    private val refined = mutableMapOf<Constant, Pair<Int, Value>>()

    private fun get(constant: Constant, context: MathContext, generate: () -> Value): Value {
        refined[constant]?.let { (digits, value) -> if (digits >= context.precision) return value }
        val value = generate()
        refined[constant] = context.precision to value
        return value
    }

    fun pi(context: MathContext): Value {
        if (context.precision <= 170) return pi
        return get(Constant.PI, context) { machin(context) }
    }

    private fun machin(precision: MathContext): Value {
        // Machin's formula.
        val a = OddPowerSeries.reciprocal(5, precision, alternating = true, checkpoint)
        val b = OddPowerSeries.reciprocal(239, precision, alternating = true, checkpoint)
        val center = a.v.multiply(Decimal(16)).subtract(b.v.multiply(Decimal(4)))
        val radius = DecimalError.add(a.error?.multiply(Decimal(16)), b.error?.multiply(Decimal(4)))
        return CalcEngine.roundedValue(center, radius, precision)
    }

    fun e(context: MathContext): Value {
        if (context.precision <= 170) return e
        return get(Constant.E, context) {
            // Each integer term is floor(10^scale / n!). The remaining tail is below two units.
            val scale = context.precision + 8
            var term = BigInteger.TEN.pow(scale)
            var sum = term
            var n = 1
            while (!term.isZero()) {
                checkpoint()
                term /= n++
                sum += term
            }
            CalcEngine.roundedValue(sum.toDecimal(scale),
                Decimal(n + 1).scaleByPowerOfTen(-scale), context)
        }
    }

    fun lnTwo(context: MathContext): Value {
        if (context.precision <= 175) return lnTwo
        return get(Constant.LN_TWO, context) {
            logFromReciprocal(3, context)
        }
    }

    fun lnTen(context: MathContext): Value {
        if (context.precision <= 175) return lnTen
        return get(Constant.LN_TEN, context) {
            // 10 = 2³ × 1.25.
            val three = Value(Decimal(3), symbolic = null)
            val quarter = logFromReciprocal(9, context)
            lnTwo(context).multiply(three, context).add(quarter, context)
        }
    }

    // ln((d + 1) / (d - 1)) = 2 atanh(1 / d).
    private fun logFromReciprocal(divisor: Int, context: MathContext): Value =
        OddPowerSeries.reciprocal(divisor, context, alternating = false, checkpoint)
            .multiply(Value(Decimal(2), symbolic = null), context)

    private companion object {
        private val PI = Decimal("3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664709384460955058223172535940812848111745028410270193852112")
        private val E = Decimal("2.71828182845904523536028747135266249775724709369995957496696762772407663035354759457138217852516642742746639193200305992181741359662904357290033429526059563073813232862794349076")
        private val irrationalRadius = Decimal("1E-175")
        val pi = Value(PI, error = irrationalRadius)
        val e = Value(E, error = irrationalRadius)
        // Truncated at 187 decimal places. Each true constant is within one unit of its last place.
        private val constantRadius = Decimal("1E-187")
        val lnTwo = Value(Decimal(
            "0.6931471805599453094172321214581765680755001343602552541206800094933936219696947156058633269964186875420014810205706857336855202357581305570326707516350759619307275708283714351903070386238",
        ), error = constantRadius)
        val lnTen = Value(Decimal(
            "2.3025850929940456840179914546843642076011014886287729760333279009675726096773524802359972050895982983419677840422862486334095254650828067566662873690987816894829072083255546808437998948262",
        ), error = constantRadius)
    }
}
