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
}
