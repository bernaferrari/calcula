package com.bernaferrari.calculator.domain.math

import com.ionspin.kotlin.bignum.integer.BigInteger

/** Integer backend for fixed-point series. Decimal and symbolic arithmetic use their shared types. */
internal expect class SeriesInteger {
    fun isZero(): Boolean
    operator fun plus(other: SeriesInteger): SeriesInteger
    operator fun minus(other: SeriesInteger): SeriesInteger
    operator fun times(other: SeriesInteger): SeriesInteger
    operator fun times(other: Int): SeriesInteger
    operator fun div(other: SeriesInteger): SeriesInteger
    operator fun div(other: Int): SeriesInteger
    fun toDecimal(scale: Int): Decimal

    companion object {
        val zero: SeriesInteger
        fun powerOfTen(scale: Int): SeriesInteger
        fun fromUnscaled(value: BigInteger): SeriesInteger
    }
}

internal class KotlinSeriesInteger private constructor(private val value: BigInteger) {
    fun isZero(): Boolean = value.isZero()
    operator fun plus(other: KotlinSeriesInteger) = KotlinSeriesInteger(value + other.value)
    operator fun minus(other: KotlinSeriesInteger) = KotlinSeriesInteger(value - other.value)
    operator fun times(other: KotlinSeriesInteger) = KotlinSeriesInteger(value * other.value)
    operator fun times(other: Int) = KotlinSeriesInteger(value * other)
    operator fun div(other: KotlinSeriesInteger) = KotlinSeriesInteger(value / other.value)
    operator fun div(other: Int) = KotlinSeriesInteger(value / other)
    fun toDecimal(scale: Int): Decimal = Decimal.fromUnscaled(value, scale)

    companion object {
        val zero = KotlinSeriesInteger(BigInteger.ZERO)
        fun powerOfTen(scale: Int) = KotlinSeriesInteger(BigInteger.TEN.pow(scale))
        fun fromUnscaled(value: BigInteger) = KotlinSeriesInteger(value)
    }
}
