package org.solovyev.android.calculator.converter

import jscl.JsclMathEngine
import midpcalc.Real

internal object Converter {

    @JvmStatic
    @Throws(NumberFormatException::class)
    fun parse(value: String): Real = parse(value, 10)

    @JvmStatic
    @Throws(NumberFormatException::class)
    fun parse(value: String, base: Int): Real {
        val groupingSeparator = JsclMathEngine.getInstance().getGroupingSeparator().toString()
        val sanitized = value
            .trim()
            .replace("−", "-")
            .replace(",", ".")
        val processedValue = if (groupingSeparator.isNotEmpty()) {
            sanitized.replace(groupingSeparator, "")
        } else {
            sanitized
        }

        return Real(processedValue, base).also { real ->
            if (real.isNan) {
                throw NumberFormatException()
            }
        }
    }
}
