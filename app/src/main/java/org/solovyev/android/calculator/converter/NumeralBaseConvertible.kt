package org.solovyev.android.calculator.converter

import android.content.Context
import jscl.JsclMathEngine
import jscl.NumeralBase
import org.solovyev.android.calculator.Named
import com.ionspin.kotlin.bignum.integer.BigInteger

class NumeralBaseConvertible(
    private val base: NumeralBase
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

    override fun named(context: Context): Named<Convertible> =
        Named.create(this as Convertible, base.name)
}
