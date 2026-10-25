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
    val symbol: String,
    val category: Category,
    val factor: BigDecimal,
    val offset: BigDecimal = BigDecimal.ZERO,
)

object Units {
    private val MC = MathContext(34, RoundingMode.HALF_EVEN)

    private fun u(id: String, symbol: String, category: Category, factor: String, offset: String = "0") =
        MeasureUnit(id, symbol, category, BigDecimal(factor), BigDecimal(offset))

    private val INCH = BigDecimal("0.0254")
    private val FIVE_NINTHS = BigDecimal(5).divide(BigDecimal(9), MC)
    private val GALLON = INCH.pow(3).multiply(BigDecimal(231)).multiply(BigDecimal(1000)) // litres

    val all: List<MeasureUnit> = listOf(
        // Length, base metre
        u("mm", "mm", Category.LENGTH, "0.001"),
        u("cm", "cm", Category.LENGTH, "0.01"),
        u("m", "m", Category.LENGTH, "1"),
        u("km", "km", Category.LENGTH, "1000"),
        u("in", "in", Category.LENGTH, "0.0254"),
        u("ft", "ft", Category.LENGTH, "0.3048"),
        u("yd", "yd", Category.LENGTH, "0.9144"),
        u("mi", "mi", Category.LENGTH, "1609.344"),
        u("nmi", "nmi", Category.LENGTH, "1852"),
        // Mass, base kilogram
        u("mg", "mg", Category.MASS, "0.000001"),
        u("g", "g", Category.MASS, "0.001"),
        u("kg", "kg", Category.MASS, "1"),
        u("t", "t", Category.MASS, "1000"),
        u("oz", "oz", Category.MASS, "0.028349523125"),
        u("lb", "lb", Category.MASS, "0.45359237"),
        u("st", "st", Category.MASS, "6.35029318"),
        // Temperature, base kelvin
        u("c", "°C", Category.TEMPERATURE, "1", "273.15"),
        MeasureUnit("f", "°F", Category.TEMPERATURE, FIVE_NINTHS, BigDecimal("459.67").multiply(FIVE_NINTHS, MC)),
        u("k", "K", Category.TEMPERATURE, "1"),
        // Area, base square metre
        u("cm2", "cm²", Category.AREA, "0.0001"),
        u("m2", "m²", Category.AREA, "1"),
        u("ha", "ha", Category.AREA, "10000"),
        u("km2", "km²", Category.AREA, "1000000"),
        u("ft2", "ft²", Category.AREA, "0.09290304"),
        u("ac", "ac", Category.AREA, "4046.8564224"),
        u("mi2", "mi²", Category.AREA, "2589988.110336"),
        // Volume, base litre
        u("ml", "mL", Category.VOLUME, "0.001"),
        u("l", "L", Category.VOLUME, "1"),
        u("m3", "m³", Category.VOLUME, "1000"),
        MeasureUnit("floz", "fl oz", Category.VOLUME, GALLON.divide(BigDecimal(128), MC)),
        MeasureUnit("cup", "cup", Category.VOLUME, GALLON.divide(BigDecimal(16), MC)),
        MeasureUnit("gal", "gal", Category.VOLUME, GALLON),
        // Speed, base metre per second
        u("mps", "m/s", Category.SPEED, "1"),
        MeasureUnit("kmh", "km/h", Category.SPEED, BigDecimal(1000).divide(BigDecimal(3600), MC)),
        MeasureUnit("mph", "mph", Category.SPEED, BigDecimal("1609.344").divide(BigDecimal(3600), MC)),
        MeasureUnit("kn", "kn", Category.SPEED, BigDecimal(1852).divide(BigDecimal(3600), MC)),
        // Time, base second
        u("ms", "ms", Category.TIME, "0.001"),
        u("s", "s", Category.TIME, "1"),
        u("min", "min", Category.TIME, "60"),
        u("h", "h", Category.TIME, "3600"),
        u("d", "d", Category.TIME, "86400"),
        u("wk", "wk", Category.TIME, "604800"),
        u("yr", "yr", Category.TIME, "31557600"),
        // Data, base byte
        u("b", "bit", Category.DATA, "0.125"),
        u("B", "B", Category.DATA, "1"),
        u("KB", "KB", Category.DATA, "1000"),
        u("MB", "MB", Category.DATA, "1000000"),
        u("GB", "GB", Category.DATA, "1000000000"),
        u("TB", "TB", Category.DATA, "1000000000000"),
        u("KiB", "KiB", Category.DATA, "1024"),
        u("MiB", "MiB", Category.DATA, "1048576"),
        u("GiB", "GiB", Category.DATA, "1073741824"),
        // Energy, base joule
        u("J", "J", Category.ENERGY, "1"),
        u("kJ", "kJ", Category.ENERGY, "1000"),
        u("cal", "cal", Category.ENERGY, "4.184"),
        u("kcal", "kcal", Category.ENERGY, "4184"),
        u("Wh", "Wh", Category.ENERGY, "3600"),
        u("kWh", "kWh", Category.ENERGY, "3600000"),
        // Pressure, base pascal
        u("Pa", "Pa", Category.PRESSURE, "1"),
        u("kPa", "kPa", Category.PRESSURE, "1000"),
        u("bar", "bar", Category.PRESSURE, "100000"),
        u("atm", "atm", Category.PRESSURE, "101325"),
        u("psi", "psi", Category.PRESSURE, "6894.757293168361"),
        u("mmHg", "mmHg", Category.PRESSURE, "133.322387415"),
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
