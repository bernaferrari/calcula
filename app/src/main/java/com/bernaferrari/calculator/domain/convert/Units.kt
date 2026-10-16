package com.bernaferrari.calculator.domain.convert

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

enum class Category { LENGTH, MASS, TEMPERATURE, AREA, VOLUME, SPEED, TIME, DATA, ENERGY, PRESSURE }

/**
 * A unit as a linear map to its category's base unit: base = value × factor + offset.
 * Offsets are only non-zero for temperatures. Factors use exact definitions (1 in = 0.0254 m,
 * 1 lb = 0.45359237 kg, 1 US gal = 231 in³) so round trips stay exact.
 */
data class MeasureUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val category: Category,
    val factor: BigDecimal,
    val offset: BigDecimal = BigDecimal.ZERO,
)

object Units {
    private val MC = MathContext(34, RoundingMode.HALF_EVEN)

    private fun u(id: String, name: String, symbol: String, category: Category, factor: String, offset: String = "0") =
        MeasureUnit(id, name, symbol, category, BigDecimal(factor), BigDecimal(offset))

    private val INCH = BigDecimal("0.0254")
    private val FIVE_NINTHS = BigDecimal(5).divide(BigDecimal(9), MC)
    private val GALLON = INCH.pow(3).multiply(BigDecimal(231)).multiply(BigDecimal(1000)) // litres

    val all: List<MeasureUnit> = listOf(
        // Length, base metre
        u("mm", "Millimetre", "mm", Category.LENGTH, "0.001"),
        u("cm", "Centimetre", "cm", Category.LENGTH, "0.01"),
        u("m", "Metre", "m", Category.LENGTH, "1"),
        u("km", "Kilometre", "km", Category.LENGTH, "1000"),
        u("in", "Inch", "in", Category.LENGTH, "0.0254"),
        u("ft", "Foot", "ft", Category.LENGTH, "0.3048"),
        u("yd", "Yard", "yd", Category.LENGTH, "0.9144"),
        u("mi", "Mile", "mi", Category.LENGTH, "1609.344"),
        u("nmi", "Nautical mile", "nmi", Category.LENGTH, "1852"),
        // Mass, base kilogram
        u("mg", "Milligram", "mg", Category.MASS, "0.000001"),
        u("g", "Gram", "g", Category.MASS, "0.001"),
        u("kg", "Kilogram", "kg", Category.MASS, "1"),
        u("t", "Tonne", "t", Category.MASS, "1000"),
        u("oz", "Ounce", "oz", Category.MASS, "0.028349523125"),
        u("lb", "Pound", "lb", Category.MASS, "0.45359237"),
        u("st", "Stone", "st", Category.MASS, "6.35029318"),
        // Temperature, base kelvin
        u("c", "Celsius", "°C", Category.TEMPERATURE, "1", "273.15"),
        MeasureUnit("f", "Fahrenheit", "°F", Category.TEMPERATURE, FIVE_NINTHS, BigDecimal("459.67").multiply(FIVE_NINTHS, MC)),
        u("k", "Kelvin", "K", Category.TEMPERATURE, "1"),
        // Area, base square metre
        u("cm2", "Square centimetre", "cm²", Category.AREA, "0.0001"),
        u("m2", "Square metre", "m²", Category.AREA, "1"),
        u("ha", "Hectare", "ha", Category.AREA, "10000"),
        u("km2", "Square kilometre", "km²", Category.AREA, "1000000"),
        u("ft2", "Square foot", "ft²", Category.AREA, "0.09290304"),
        u("ac", "Acre", "ac", Category.AREA, "4046.8564224"),
        u("mi2", "Square mile", "mi²", Category.AREA, "2589988.110336"),
        // Volume, base litre
        u("ml", "Millilitre", "mL", Category.VOLUME, "0.001"),
        u("l", "Litre", "L", Category.VOLUME, "1"),
        u("m3", "Cubic metre", "m³", Category.VOLUME, "1000"),
        MeasureUnit("floz", "US fluid ounce", "fl oz", Category.VOLUME, GALLON.divide(BigDecimal(128), MC)),
        MeasureUnit("cup", "US cup", "cup", Category.VOLUME, GALLON.divide(BigDecimal(16), MC)),
        MeasureUnit("gal", "US gallon", "gal", Category.VOLUME, GALLON),
        // Speed, base metre per second
        u("mps", "Metres per second", "m/s", Category.SPEED, "1"),
        MeasureUnit("kmh", "Kilometres per hour", "km/h", Category.SPEED, BigDecimal(1000).divide(BigDecimal(3600), MC)),
        MeasureUnit("mph", "Miles per hour", "mph", Category.SPEED, BigDecimal("1609.344").divide(BigDecimal(3600), MC)),
        MeasureUnit("kn", "Knots", "kn", Category.SPEED, BigDecimal(1852).divide(BigDecimal(3600), MC)),
        // Time, base second
        u("ms", "Millisecond", "ms", Category.TIME, "0.001"),
        u("s", "Second", "s", Category.TIME, "1"),
        u("min", "Minute", "min", Category.TIME, "60"),
        u("h", "Hour", "h", Category.TIME, "3600"),
        u("d", "Day", "d", Category.TIME, "86400"),
        u("wk", "Week", "wk", Category.TIME, "604800"),
        u("yr", "Year", "yr", Category.TIME, "31557600"),
        // Data, base byte
        u("b", "Bit", "bit", Category.DATA, "0.125"),
        u("B", "Byte", "B", Category.DATA, "1"),
        u("KB", "Kilobyte", "KB", Category.DATA, "1000"),
        u("MB", "Megabyte", "MB", Category.DATA, "1000000"),
        u("GB", "Gigabyte", "GB", Category.DATA, "1000000000"),
        u("TB", "Terabyte", "TB", Category.DATA, "1000000000000"),
        u("KiB", "Kibibyte", "KiB", Category.DATA, "1024"),
        u("MiB", "Mebibyte", "MiB", Category.DATA, "1048576"),
        u("GiB", "Gibibyte", "GiB", Category.DATA, "1073741824"),
        // Energy, base joule
        u("J", "Joule", "J", Category.ENERGY, "1"),
        u("kJ", "Kilojoule", "kJ", Category.ENERGY, "1000"),
        u("cal", "Calorie", "cal", Category.ENERGY, "4.184"),
        u("kcal", "Kilocalorie", "kcal", Category.ENERGY, "4184"),
        u("Wh", "Watt-hour", "Wh", Category.ENERGY, "3600"),
        u("kWh", "Kilowatt-hour", "kWh", Category.ENERGY, "3600000"),
        // Pressure, base pascal
        u("Pa", "Pascal", "Pa", Category.PRESSURE, "1"),
        u("kPa", "Kilopascal", "kPa", Category.PRESSURE, "1000"),
        u("bar", "Bar", "bar", Category.PRESSURE, "100000"),
        u("atm", "Atmosphere", "atm", Category.PRESSURE, "101325"),
        u("psi", "Pound per square inch", "psi", Category.PRESSURE, "6894.757293168361"),
        u("mmHg", "Millimetre of mercury", "mmHg", Category.PRESSURE, "133.322387415"),
    )

    fun of(category: Category): List<MeasureUnit> = all.filter { it.category == category }

    fun byId(id: String): MeasureUnit? = all.firstOrNull { it.id == id }

    /** Sensible starting pair for each category (the first is what people usually type in). */
    fun defaults(category: Category): Pair<MeasureUnit, MeasureUnit> {
        val ids = when (category) {
            Category.LENGTH -> "km" to "mi"
            Category.MASS -> "kg" to "lb"
            Category.TEMPERATURE -> "c" to "f"
            Category.AREA -> "m2" to "ft2"
            Category.VOLUME -> "l" to "gal"
            Category.SPEED -> "kmh" to "mph"
            Category.TIME -> "h" to "min"
            Category.DATA -> "GB" to "MB"
            Category.ENERGY -> "kcal" to "kJ"
            Category.PRESSURE -> "bar" to "psi"
        }
        return byId(ids.first)!! to byId(ids.second)!!
    }

    fun convert(value: BigDecimal, from: MeasureUnit, to: MeasureUnit): BigDecimal {
        require(from.category == to.category) { "Can't convert ${from.category} to ${to.category}" }
        if (from == to) return value
        val base = value.multiply(from.factor, MC).add(from.offset, MC)
        return base.subtract(to.offset, MC).divide(to.factor, MC)
    }
}
