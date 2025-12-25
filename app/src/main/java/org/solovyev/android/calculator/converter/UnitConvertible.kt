package org.solovyev.android.calculator.converter

import android.content.Context
import jscl.JsclMathEngine
import jscl.NumeralBase
import org.solovyev.android.calculator.Named
import javax.measure.unit.Unit

internal class UnitConvertible private constructor(
    private val unit: Unit<*>
) : Convertible {

    private fun format(value: Double): String =
        JsclMathEngine.getInstance().format(value, NumeralBase.dec)

    @Throws(NumberFormatException::class)
    override fun convert(to: Convertible, value: String): String {
        val from = Converter.parse(value).toDouble()
        val converted = unit.getConverterTo((to as UnitConvertible).unit).convert(from)
        return format(converted)
    }

    override fun named(context: Context): Named<Convertible> {
        val dimension = UnitDimension.of(unit)
        val nameRes = dimension?.let { Converter.unitName(unit, it) } ?: 0
        return Named.create(this as Convertible, nameRes, context)
    }

    override fun toString(): String = unit.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as UnitConvertible
        return unit == that.unit
    }

    override fun hashCode(): Int = unit.hashCode()

    companion object {
        @JvmStatic
        fun create(unit: Unit<*>): UnitConvertible = UnitConvertible(unit)
    }
}
