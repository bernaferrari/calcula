package com.bernaferrari.calculator.domain.math

import com.ionspin.kotlin.bignum.integer.BigInteger as IonsPinInteger

/** Integer coefficients for decimal arithmetic, exact fractions, and fixed-point series. */
expect class BigInteger : Comparable<BigInteger> {
    fun add(other: BigInteger): BigInteger
    fun subtract(other: BigInteger): BigInteger
    fun multiply(other: BigInteger): BigInteger
    fun divide(other: BigInteger): BigInteger
    fun divideAndRemainder(other: BigInteger): Pair<BigInteger, BigInteger>
    operator fun rem(other: BigInteger): BigInteger
    fun pow(exponent: Int): BigInteger
    fun gcd(other: BigInteger): BigInteger
    fun abs(): BigInteger
    fun negate(): BigInteger
    fun shl(bits: Int): BigInteger
    fun bitLength(): Int
    fun signum(): Int
    fun intValue(): Int
    fun longValue(exactRequired: Boolean): Long

    companion object {
        val ZERO: BigInteger
        val ONE: BigInteger
        val TWO: BigInteger
        val TEN: BigInteger
        fun fromInt(value: Int): BigInteger
        fun fromLong(value: Long): BigInteger
        fun parseString(value: String): BigInteger
    }
}

internal fun BigInteger.isZero(): Boolean = signum() == 0
internal operator fun BigInteger.plus(other: BigInteger): BigInteger = add(other)
internal operator fun BigInteger.minus(other: BigInteger): BigInteger = subtract(other)
internal operator fun BigInteger.times(other: BigInteger): BigInteger = multiply(other)
internal operator fun BigInteger.times(other: Int): BigInteger = multiply(BigInteger.fromInt(other))
internal operator fun BigInteger.div(other: BigInteger): BigInteger = divide(other)
internal operator fun BigInteger.div(other: Int): BigInteger = divide(BigInteger.fromInt(other))
internal fun BigInteger.toDecimal(scale: Int): Decimal = Decimal.fromUnscaled(this, scale)

internal fun BigInteger.sqrt(): BigInteger {
    require(signum() >= 0)
    if (isZero()) return this
    var root = BigInteger.ONE.shl((bitLength() + 1) / 2)
    while (true) {
        val next = (root + this / root) / 2
        if (next >= root) return root
        root = next
    }
}

class KotlinBigInteger private constructor(private val value: IonsPinInteger) : Comparable<KotlinBigInteger> {
    fun add(other: KotlinBigInteger) = KotlinBigInteger(value.add(other.value))
    fun subtract(other: KotlinBigInteger) = KotlinBigInteger(value.subtract(other.value))
    fun multiply(other: KotlinBigInteger) = KotlinBigInteger(value.multiply(other.value))
    fun divide(other: KotlinBigInteger) = KotlinBigInteger(value.divide(other.value))
    fun divideAndRemainder(other: KotlinBigInteger): Pair<KotlinBigInteger, KotlinBigInteger> {
        val (quotient, remainder) = value.divideAndRemainder(other.value)
        return KotlinBigInteger(quotient) to KotlinBigInteger(remainder)
    }
    operator fun rem(other: KotlinBigInteger) = KotlinBigInteger(value.rem(other.value))
    fun pow(exponent: Int) = KotlinBigInteger(value.pow(exponent))
    fun gcd(other: KotlinBigInteger) = KotlinBigInteger(value.gcd(other.value))
    fun abs() = KotlinBigInteger(value.abs())
    fun negate() = KotlinBigInteger(value.negate())
    fun shl(bits: Int) = KotlinBigInteger(value.shl(bits))
    fun bitLength(): Int = value.bitLength()
    fun signum(): Int = value.signum()
    fun intValue(): Int = value.intValue()
    fun longValue(exactRequired: Boolean): Long = value.longValue(exactRequired)
    override fun compareTo(other: KotlinBigInteger): Int = value.compareTo(other.value)
    override fun equals(other: Any?): Boolean = other is KotlinBigInteger && value == other.value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value.toString()

    companion object {
        val ZERO = KotlinBigInteger(IonsPinInteger.ZERO)
        val ONE = KotlinBigInteger(IonsPinInteger.ONE)
        val TWO = KotlinBigInteger(IonsPinInteger.TWO)
        val TEN = KotlinBigInteger(IonsPinInteger.TEN)
        fun fromInt(value: Int) = KotlinBigInteger(IonsPinInteger.fromInt(value))
        fun fromLong(value: Long) = KotlinBigInteger(IonsPinInteger.fromLong(value))
        fun parseString(value: String) = KotlinBigInteger(IonsPinInteger.parseString(value))
    }
}
