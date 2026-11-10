package com.bernaferrari.calculator.domain.math

import java.math.BigInteger as JavaInteger

actual class BigInteger private constructor(private val value: JavaInteger) : Comparable<BigInteger> {
    actual fun add(other: BigInteger) = BigInteger(value.add(other.value))
    actual fun subtract(other: BigInteger) = BigInteger(value.subtract(other.value))
    actual fun multiply(other: BigInteger) = BigInteger(value.multiply(other.value))
    actual fun divide(other: BigInteger) = BigInteger(value.divide(other.value))
    actual fun divideAndRemainder(other: BigInteger): Pair<BigInteger, BigInteger> {
        val (quotient, remainder) = value.divideAndRemainder(other.value)
        return BigInteger(quotient) to BigInteger(remainder)
    }
    actual operator fun rem(other: BigInteger) = BigInteger(value.remainder(other.value))
    actual fun pow(exponent: Int) = BigInteger(value.pow(exponent))
    actual fun gcd(other: BigInteger) = BigInteger(value.gcd(other.value))
    actual fun abs() = BigInteger(value.abs())
    actual fun negate() = BigInteger(value.negate())
    actual fun shl(bits: Int) = BigInteger(value.shiftLeft(bits))
    actual fun bitLength(): Int = value.abs().bitLength()
    actual fun signum(): Int = value.signum()
    actual fun intValue(): Int {
        if (value.bitLength() > 31) throw ArithmeticException("Integer out of range")
        return value.toInt()
    }
    actual fun longValue(exactRequired: Boolean): Long {
        if (exactRequired && value.bitLength() > 63) throw ArithmeticException("Long out of range")
        return value.toLong()
    }
    override fun compareTo(other: BigInteger): Int = value.compareTo(other.value)
    override fun equals(other: Any?): Boolean = other is BigInteger && value == other.value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value.toString()

    actual companion object {
        actual val ZERO = BigInteger(JavaInteger.ZERO)
        actual val ONE = BigInteger(JavaInteger.ONE)
        actual val TWO = BigInteger(JavaInteger.valueOf(2))
        actual val TEN = BigInteger(JavaInteger.TEN)
        actual fun fromInt(value: Int) = fromLong(value.toLong())
        actual fun fromLong(value: Long) = BigInteger(JavaInteger.valueOf(value))
        actual fun parseString(value: String) = BigInteger(JavaInteger(value))
    }
}
