package org.solovyev.android.calculator.converter

import android.content.Context
import androidx.annotation.StringRes
import org.solovyev.android.calculator.Named
import org.solovyev.android.calculator.R
import javax.measure.unit.Dimension
import javax.measure.unit.NonSI
import javax.measure.unit.SI
import javax.measure.unit.Unit

enum class UnitDimension(
    val dimension: Dimension,
    @StringRes val nameRes: Int
) : ConvertibleDimension {
    TIME(Dimension.TIME, R.string.cpp_converter_time),
    AMOUNT_OF_SUBSTANCE(Dimension.AMOUNT_OF_SUBSTANCE, R.string.cpp_converter_amount_of_substance),
    ELECTRIC_CURRENT(Dimension.ELECTRIC_CURRENT, R.string.cpp_converter_electric_current),
    LENGTH(Dimension.LENGTH, R.string.cpp_converter_length),
    MASS(Dimension.MASS, R.string.cpp_converter_mass),
    TEMPERATURE(Dimension.TEMPERATURE, R.string.cpp_converter_temperature);

    override fun named(context: Context): Named<ConvertibleDimension> =
        Named.create(this as ConvertibleDimension, nameRes, context)

    override fun getUnits(): List<Convertible> =
        units[dimension] ?: emptyList()

    companion object {
        private val excludedUnits = setOf(
            "year_sidereal", "year_calendar", "day_sidereal",
            "foot_survey_us", "me", "u"
        )

        private val units: Map<Dimension, List<Convertible>> = buildUnitsMap()

        private fun buildUnitsMap(): Map<Dimension, MutableList<Convertible>> {
            val map = mutableMapOf<Dimension, MutableList<Convertible>>()

            // Add SI units
            SI.getInstance().units.forEach { unit ->
                addUnit(unit, map)
            }

            // Add NonSI units
            NonSI.getInstance().units.forEach { unit ->
                addUnit(unit, map)
            }

            return map
        }

        private fun addUnit(unit: Unit<*>, map: MutableMap<Dimension, MutableList<Convertible>>) {
            if (unit.toString() in excludedUnits) {
                return
            }

            val dimension = unit.dimension
            val unitsInDimension = map.getOrPut(dimension) { mutableListOf() }
            unitsInDimension.add(UnitConvertible.create(unit))
        }

        @JvmStatic
        fun of(unit: Unit<*>): UnitDimension? =
            values().find { it.dimension == unit.dimension }
    }
}
