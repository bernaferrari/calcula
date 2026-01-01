package org.solovyev.android.calculator.converter

import org.solovyev.android.calculator.ui.*

internal object ConverterUnits {
    private const val AVOGADRO = 6.02214199E23

    private const val YEAR_SECONDS = 31_556_952.0
    private const val MONTH_SECONDS = YEAR_SECONDS / 12.0

    private const val FOOT_METERS = 0.3048
    private const val INCH_METERS = FOOT_METERS / 12.0
    private const val YARD_METERS = FOOT_METERS * 3.0
    private const val MILE_METERS = 1609.344
    private const val NAUTICAL_MILE_METERS = 1852.0
    private const val ANGSTROM_METERS = 1.0E-10
    private const val ASTRONOMICAL_UNIT_METERS = 1.49597870691E11
    private const val LIGHT_YEAR_METERS = 9.460528405E15
    private const val PARSEC_METERS = 3.085677E16
    private const val POINT_METERS = INCH_METERS * 13837.0 / 1_000_000.0
    private const val PIXEL_METERS = INCH_METERS / 72.0

    private const val POUND_KG = 0.45359237
    private const val OUNCE_KG = POUND_KG / 16.0
    private const val TON_US_KG = POUND_KG * 2000.0
    private const val TON_UK_KG = POUND_KG * 2240.0
    private const val METRIC_TON_KG = 1000.0

    private const val GILBERT_AMPERE = 0.7957747154594768

    val timeUnits: List<Convertible> = listOf(
        LinearUnit(UnitDimension.TIME, Res.string.cpp_units_time_seconds, "s", 1.0),
        LinearUnit(UnitDimension.TIME, Res.string.cpp_units_time_minutes, "min", 60.0),
        LinearUnit(UnitDimension.TIME, Res.string.cpp_units_time_hours, "h", 3600.0),
        LinearUnit(UnitDimension.TIME, Res.string.cpp_units_time_days, "day", 86400.0),
        LinearUnit(UnitDimension.TIME, Res.string.cpp_units_time_weeks, "week", 604800.0),
        LinearUnit(UnitDimension.TIME, Res.string.cpp_units_time_months, "month", MONTH_SECONDS),
        LinearUnit(UnitDimension.TIME, Res.string.cpp_units_time_years, "year", YEAR_SECONDS)
    )

    val amountUnits: List<Convertible> = listOf(
        LinearUnit(UnitDimension.AMOUNT_OF_SUBSTANCE, Res.string.cpp_units_aos_mol, "mol", 1.0),
        LinearUnit(UnitDimension.AMOUNT_OF_SUBSTANCE, Res.string.cpp_units_aos_atoms, "atom", 1.0 / AVOGADRO)
    )

    val currentUnits: List<Convertible> = listOf(
        LinearUnit(UnitDimension.ELECTRIC_CURRENT, Res.string.cpp_units_ec_a, "A", 1.0),
        LinearUnit(UnitDimension.ELECTRIC_CURRENT, Res.string.cpp_units_ec_gi, "Gi", GILBERT_AMPERE)
    )

    val lengthUnits: List<Convertible> = listOf(
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_meters, "m", 1.0),
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_inches, "in", INCH_METERS),
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_feet, "ft", FOOT_METERS),
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_yards, "yd", YARD_METERS),
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_miles, "mi", MILE_METERS),
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_nautical_miles, "nmi", NAUTICAL_MILE_METERS),
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_angstroms, "Å", ANGSTROM_METERS),
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_astronomical_units, "ua", ASTRONOMICAL_UNIT_METERS),
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_light_years, "light years", LIGHT_YEAR_METERS), // Using string for now if ID missing
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_parsecs, "pc", PARSEC_METERS),
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_points, "pt", POINT_METERS),
        LinearUnit(UnitDimension.LENGTH, Res.string.cpp_units_length_pixels, "px", PIXEL_METERS)
    )

    val massUnits: List<Convertible> = listOf(
        LinearUnit(UnitDimension.MASS, Res.string.cpp_units_mass_kg, "kg", 1.0),
        LinearUnit(UnitDimension.MASS, Res.string.cpp_units_mass_lb, "lb", POUND_KG),
        LinearUnit(UnitDimension.MASS, Res.string.cpp_units_mass_oz, "oz", OUNCE_KG),
        LinearUnit(UnitDimension.MASS, Res.string.cpp_units_mass_t, "t", METRIC_TON_KG),
        LinearUnit(UnitDimension.MASS, Res.string.cpp_units_mass_tons_us, "ton_us", TON_US_KG),
        LinearUnit(UnitDimension.MASS, Res.string.cpp_units_mass_tons_uk, "ton_uk", TON_UK_KG)
    )

    val temperatureUnits: List<Convertible> = listOf(
        AffineUnit(UnitDimension.TEMPERATURE, null, "K", 1.0, 0.0),
        AffineUnit(UnitDimension.TEMPERATURE, null, "°C", 1.0, 273.15),
        AffineUnit(UnitDimension.TEMPERATURE, null, "°F", 5.0 / 9.0, 255.3722222222222),
        AffineUnit(UnitDimension.TEMPERATURE, null, "°R", 5.0 / 9.0, 0.0)
    )
}
