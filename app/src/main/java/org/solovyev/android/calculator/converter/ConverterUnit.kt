package org.solovyev.android.calculator.converter

import android.content.Context
import androidx.annotation.StringRes
import jscl.JsclMathEngine
import jscl.NumeralBase
import org.solovyev.android.calculator.Named

internal sealed class ConverterUnit(
    val dimension: UnitDimension,
    @StringRes val nameRes: Int,
    val symbol: String
) : Convertible {
    protected abstract fun toBase(value: Double): Double
    protected abstract fun fromBase(value: Double): Double

    override fun convert(to: Convertible, value: String): String {
        val target = to as ConverterUnit
        require(target.dimension == dimension) {
            "Cannot convert between different dimensions: $dimension -> ${target.dimension}"
        }
        val fromValue = Converter.parse(value).toDouble()
        val baseValue = toBase(fromValue)
        return format(target.fromBase(baseValue))
    }

    override fun named(context: Context): Named<Convertible> {
        return Named.create(this as Convertible, nameRes, context)
    }

    override fun toString(): String = symbol

    protected fun format(value: Double): String =
        JsclMathEngine.getInstance().format(value, NumeralBase.dec)
}

internal class LinearUnit(
    dimension: UnitDimension,
    @StringRes nameRes: Int,
    symbol: String,
    private val scaleToBase: Double
) : ConverterUnit(dimension, nameRes, symbol) {
    override fun toBase(value: Double): Double = value * scaleToBase
    override fun fromBase(value: Double): Double = value / scaleToBase
}

internal class AffineUnit(
    dimension: UnitDimension,
    @StringRes nameRes: Int,
    symbol: String,
    private val scaleToBase: Double,
    private val offsetToBase: Double
) : ConverterUnit(dimension, nameRes, symbol) {
    override fun toBase(value: Double): Double = value * scaleToBase + offsetToBase
    override fun fromBase(value: Double): Double = (value - offsetToBase) / scaleToBase
}
