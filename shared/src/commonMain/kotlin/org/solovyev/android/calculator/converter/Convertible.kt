package org.solovyev.android.calculator.converter

interface Convertible {
    @Throws(NumberFormatException::class)
    fun convert(to: Convertible, value: String): String
}

interface ConvertibleDimension {
    fun getUnits(): List<Convertible>
}
