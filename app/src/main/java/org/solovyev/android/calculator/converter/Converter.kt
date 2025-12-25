package org.solovyev.android.calculator.converter

import android.util.Log
import com.google.common.base.Strings
import jscl.JsclMathEngine
import midpcalc.Real
import org.solovyev.android.calculator.R
import javax.measure.unit.Unit

internal object Converter {

    fun unitName(unit: Unit<*>, dimension: UnitDimension): Int {
        val id = Strings.nullToEmpty(unit.toString())

        return when (dimension) {
            UnitDimension.TIME -> when (id) {
                "s" -> R.string.cpp_units_time_seconds
                "week" -> R.string.cpp_units_time_weeks
                "day" -> R.string.cpp_units_time_days
                "min" -> R.string.cpp_units_time_minutes
                "year" -> R.string.cpp_units_time_years
                "h" -> R.string.cpp_units_time_hours
                "month" -> R.string.cpp_units_time_months
                else -> 0
            }
            UnitDimension.AMOUNT_OF_SUBSTANCE -> when (id) {
                "mol" -> R.string.cpp_units_aos_mol
                "atom" -> R.string.cpp_units_aos_atoms
                else -> 0
            }
            UnitDimension.ELECTRIC_CURRENT -> when (id) {
                "A" -> R.string.cpp_units_ec_a
                "Gi" -> R.string.cpp_units_ec_gi
                else -> 0
            }
            UnitDimension.LENGTH -> when (id) {
                "m" -> R.string.cpp_units_length_meters
                "nmi" -> R.string.cpp_units_length_nautical_miles
                "in" -> R.string.cpp_units_length_inches
                "ua" -> R.string.cpp_units_length_astronomical_units
                "Å" -> R.string.cpp_units_length_angstroms
                "ly" -> R.string.cpp_units_length_light_years
                "mi" -> R.string.cpp_units_length_miles
                "yd" -> R.string.cpp_units_length_yards
                "pixel" -> R.string.cpp_units_length_pixels
                "ft" -> R.string.cpp_units_length_feet
                "pc" -> R.string.cpp_units_length_parsecs
                "pt" -> R.string.cpp_units_length_points
                else -> 0
            }
            UnitDimension.MASS -> when (id) {
                "kg" -> R.string.cpp_units_mass_kg
                "lb" -> R.string.cpp_units_mass_lb
                "oz" -> R.string.cpp_units_mass_oz
                "t" -> R.string.cpp_units_mass_t
                "ton_uk" -> R.string.cpp_units_mass_tons_uk
                "ton_us" -> R.string.cpp_units_mass_tons_us
                else -> 0
            }
            UnitDimension.TEMPERATURE -> 0 // temperature unit ids are international
        }.also { result ->
            if (result == 0) {
                Log.w("Converter", "Unit translation is missing for unit=$id in dimension=$dimension")
            }
        }
    }

    @JvmStatic
    @Throws(NumberFormatException::class)
    fun parse(value: String): Real = parse(value, 10)

    @JvmStatic
    @Throws(NumberFormatException::class)
    fun parse(value: String, base: Int): Real {
        val groupingSeparator = JsclMathEngine.getInstance().getGroupingSeparator().toString()
        val processedValue = if (groupingSeparator.isNotEmpty()) {
            value.replace(groupingSeparator, "")
        } else {
            value
        }

        return Real(processedValue, base).also { real ->
            if (real.isNan) {
                throw NumberFormatException()
            }
        }
    }
}
