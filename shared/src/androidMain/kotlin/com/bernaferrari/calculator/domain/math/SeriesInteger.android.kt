package com.bernaferrari.calculator.domain.math

import com.ionspin.kotlin.bignum.integer.BigInteger
import java.math.BigInteger as JavaInteger

internal actual class SeriesInteger private constructor(private val value: JavaInteger) {
    actual fun isZero(): Boolean = value.signum() == 0
    actual operator fun plus(other: SeriesInteger) = SeriesInteger(value.add(other.value))
    actual operator fun minus(other: SeriesInteger) = SeriesInteger(value.subtract(other.value))
    actual operator fun times(other: SeriesInteger) = SeriesInteger(value.multiply(other.value))
    actual operator fun times(other: Int) = SeriesInteger(value.multiply(JavaInteger.valueOf(other.toLong())))
    actual operator fun div(other: SeriesInteger) = SeriesInteger(value.divide(other.value))
    actual operator fun div(other: Int) = SeriesInteger(value.divide(JavaInteger.valueOf(other.toLong())))
    actual fun toDecimal(scale: Int): Decimal = Decimal.fromUnscaled(value.toString(), scale)

    actual companion object {
        actual val zero = SeriesInteger(JavaInteger.ZERO)
        actual fun powerOfTen(scale: Int) = SeriesInteger(JavaInteger.TEN.pow(scale))
        actual fun fromUnscaled(value: BigInteger) = SeriesInteger(JavaInteger(value.toString()))
    }
}
