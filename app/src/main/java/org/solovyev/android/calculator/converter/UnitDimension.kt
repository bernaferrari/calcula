package org.solovyev.android.calculator.converter

import android.content.Context
import androidx.annotation.StringRes
import org.solovyev.android.calculator.Named
import org.solovyev.android.calculator.R

enum class UnitDimension(
    @StringRes val nameRes: Int
) : ConvertibleDimension {
    TIME(R.string.cpp_converter_time),
    AMOUNT_OF_SUBSTANCE(R.string.cpp_converter_amount_of_substance),
    ELECTRIC_CURRENT(R.string.cpp_converter_electric_current),
    LENGTH(R.string.cpp_converter_length),
    MASS(R.string.cpp_converter_mass),
    TEMPERATURE(R.string.cpp_converter_temperature);

    override fun named(context: Context): Named<ConvertibleDimension> =
        Named.create(this as ConvertibleDimension, nameRes, context)

    override fun getUnits(): List<Convertible> = units[this] ?: emptyList()

    companion object {
        private val units: Map<UnitDimension, List<Convertible>> = mapOf(
            TIME to ConverterUnits.timeUnits,
            AMOUNT_OF_SUBSTANCE to ConverterUnits.amountUnits,
            ELECTRIC_CURRENT to ConverterUnits.currentUnits,
            LENGTH to ConverterUnits.lengthUnits,
            MASS to ConverterUnits.massUnits,
            TEMPERATURE to ConverterUnits.temperatureUnits
        )
    }
}
