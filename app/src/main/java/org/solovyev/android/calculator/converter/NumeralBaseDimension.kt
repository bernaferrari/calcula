package org.solovyev.android.calculator.converter

import android.content.Context
import jscl.NumeralBase
import org.solovyev.android.calculator.Named
import org.solovyev.android.calculator.R

class NumeralBaseDimension private constructor() : ConvertibleDimension {

    private val units: List<Convertible> = NumeralBase.values().map { base ->
        NumeralBaseConvertible(base)
    }

    override fun named(context: Context): Named<ConvertibleDimension> =
        Named.create(this as ConvertibleDimension, R.string.cpp_numeral_system, context)

    override fun getUnits(): List<Convertible> = units

    companion object {
        private val INSTANCE = NumeralBaseDimension()

        @JvmStatic
        fun get(): ConvertibleDimension = INSTANCE
    }
}
