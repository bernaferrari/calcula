package org.solovyev.android.calculator.converter

import jscl.NumeralBase

class NumeralBaseDimension private constructor() : ConvertibleDimension {

    private val units: List<Convertible> = NumeralBase.values().map { base ->
        NumeralBaseConvertible(base)
    }

    override fun getUnits(): List<Convertible> = units

    companion object {
        private val INSTANCE = NumeralBaseDimension()

        fun get(): ConvertibleDimension = INSTANCE
    }
}
