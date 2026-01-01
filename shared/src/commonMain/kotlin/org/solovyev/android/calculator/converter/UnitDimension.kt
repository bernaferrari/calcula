package org.solovyev.android.calculator.converter

import org.solovyev.android.calculator.ui.*
import org.jetbrains.compose.resources.StringResource

enum class UnitDimension(
    val nameRes: StringResource
) : ConvertibleDimension {
    TIME(Res.string.cpp_converter_time),
    AMOUNT_OF_SUBSTANCE(Res.string.cpp_converter_amount_of_substance),
    ELECTRIC_CURRENT(Res.string.cpp_converter_electric_current),
    LENGTH(Res.string.cpp_converter_length),
    MASS(Res.string.cpp_converter_mass),
    TEMPERATURE(Res.string.cpp_converter_temperature);

    override fun getUnits(): List<Convertible> = units[this] ?: emptyList()

    companion object {
        private val units: Map<UnitDimension, List<Convertible>> by lazy {
            mapOf(
                TIME to ConverterUnits.timeUnits,
                AMOUNT_OF_SUBSTANCE to ConverterUnits.amountUnits,
                ELECTRIC_CURRENT to ConverterUnits.currentUnits,
                LENGTH to ConverterUnits.lengthUnits,
                MASS to ConverterUnits.massUnits,
                TEMPERATURE to ConverterUnits.temperatureUnits
            )
        }
    }
}
