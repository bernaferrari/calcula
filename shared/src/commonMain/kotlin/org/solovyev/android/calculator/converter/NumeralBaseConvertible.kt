package org.solovyev.android.calculator.converter

import jscl.JsclMathEngine
import jscl.NumeralBase
import com.ionspin.kotlin.bignum.integer.BigInteger

class NumeralBaseConvertible(
    val base: NumeralBase
) : Convertible {

    private val mathEngine: JsclMathEngine = JsclMathEngine.getInstance()

    @Throws(NumberFormatException::class)
    override fun convert(to: Convertible, value: String): String {
        val baseTo = (to as NumeralBaseConvertible).base
        val real = Converter.parse(value, base.radix)

        return if (real.isIntegral()) {
            val l = real.toLong()
            if (l != Long.MAX_VALUE && l != -Long.MAX_VALUE) {
                mathEngine.format(BigInteger.fromLong(l), baseTo)
            } else {
                mathEngine.format(real.toDouble(), baseTo)
            }
        } else {
            mathEngine.format(real.toDouble(), baseTo)
        }
    }
}
