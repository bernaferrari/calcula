package com.bernaferrari.calculator.domain.math


object NumberFormatter {
    /**
     * Formats a result in the expression language, so it can be inserted back into an expression.
     * Rounds to [significantDigits] (which hides Double noise like 0.49999999999999994) and switches
     * to `1.5E20` notation outside a readable range.
     */
    fun format(value: Decimal, significantDigits: Int = 15): String {
        val rounded = value.round(MathContext(significantDigits, RoundingMode.HALF_EVEN)).stripTrailingZeros()
        if (rounded.signum() == 0) return "0"
        val exponent = rounded.precision() - rounded.scale() - 1
        val text = if (exponent >= 16 || exponent <= -10) {
            val mantissa = rounded.movePointLeft(exponent).stripTrailingZeros().toPlainString()
            val sign = if (exponent < 0) "−" else ""
            "${mantissa}E$sign${kotlin.math.abs(exponent)}"
        } else {
            rounded.toPlainString()
        }
        return text.replace('-', '−')
    }

    /**
     * `1.234E6` style: one digit before the point, or, for [engineering], one to three with the
     * exponent a multiple of three (`12.5E3`, `470E−9`), the way units go (kilo, nano). [decimals]
     * digits after the point, or up to 15 significant ones when negative.
     */
    fun scientific(value: Decimal, engineering: Boolean, decimals: Int): String {
        if (value.signum() == 0) return "0"
        fun exponentOf(d: Decimal): Int {
            val e = d.precision() - d.scale() - 1
            return if (engineering) e.floorDiv(3) * 3 else e
        }
        var exponent = exponentOf(value)
        fun mantissa(e: Int): Decimal {
            val m = value.scaleByPowerOfTen(-e)
            return if (decimals >= 0) m.setScale(decimals, RoundingMode.HALF_UP)
            else m.round(MathContext(15, RoundingMode.HALF_EVEN)).stripTrailingZeros()
        }
        var m = mantissa(exponent)
        // Rounding can carry over (9.99 → 10.0): then it's the next power.
        val limit = if (engineering) Decimal(1000) else Decimal.TEN
        if (m.abs() >= limit) {
            exponent += if (engineering) 3 else 1
            m = mantissa(exponent)
        }
        val mantissaText = (if (decimals >= 0) m.toPlainString() else m.stripTrailingZeros().toPlainString()).let { if (m.scale() < 0) m.setScale(0, RoundingMode.DOWN).toPlainString() else it }
        val sign = if (exponent < 0) "−" else ""
        return "${mantissaText}E$sign${kotlin.math.abs(exponent)}".replace('-', '−')
    }
}
