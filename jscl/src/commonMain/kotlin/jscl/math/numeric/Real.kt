package jscl.math.numeric

import jscl.math.NotDivisibleException
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.math.*

class Real internal constructor(private val content: Double) : Numeric() {

    fun add(that: Real): Real {
        return Real(content + that.content)
    }

    override fun add(that: Numeric): Numeric {
        return when (that) {
            is Real -> add(that)
            else -> that.valueOf(this).add(that)
        }
    }

    fun subtract(that: Real): Real {
        return Real(content - that.content)
    }

    override fun subtract(that: Numeric): Numeric {
        return when (that) {
            is Real -> subtract(that)
            else -> that.valueOf(this).subtract(that)
        }
    }

    fun multiply(that: Real): Real {
        return Real(content * that.content)
    }

    override fun multiply(that: Numeric): Numeric {
        return when (that) {
            is Real -> multiply(that)
            else -> that.multiply(this)
        }
    }

    fun divide(that: Real): Real {
        return Real(content / that.content)
    }

    override fun divide(that: Numeric): Numeric {
        return when (that) {
            is Real -> divide(that)
            else -> that.valueOf(this).divide(that)
        }
    }

    override fun negate(): Numeric {
        return Real(-content)
    }

    override fun signum(): Int {
        return signum(content)
    }

    override fun ln(): Numeric {
        return if (signum() >= 0) {
            Real(ln(content))
        } else {
            Complex.valueOf(ln(-content), PI)
        }
    }

    override fun lg(): Numeric {
        return if (signum() >= 0) {
            Real(log10(content))
        } else {
            Complex.valueOf(log10(-content), PI)
        }
    }

    override fun exp(): Numeric {
        return Real(exp(content))
    }

    override fun inverse(): Numeric {
        return Real(1.0 / content)
    }

    override fun pow(exponent: Int): Numeric {
        if (content.isNaN()) {
            return Real(Double.NaN)
        }
        return super.pow(exponent)
    }

    fun pow(that: Real): Numeric {
        return if (signum() < 0) {
            Complex.valueOf(content, 0.0).pow(that)
        } else {
            Real(content.pow(that.content))
        }
    }

    override fun pow(numeric: Numeric): Numeric {
        return when (numeric) {
            is Real -> pow(numeric)
            else -> numeric.valueOf(this).pow(numeric)
        }
    }

    override fun sqrt(): Numeric {
        return if (signum() < 0) {
            Complex.I.multiply(negate().sqrt())
        } else {
            Real(sqrt(content))
        }
    }

    override fun nThRoot(n: Int): Numeric {
        return if (signum() < 0) {
            if (n % 2 == 0) sqrt().nThRoot(n / 2) else negate().nThRoot(n).negate()
        } else {
            super.nThRoot(n)
        }
    }

    override fun conjugate(): Numeric {
        return this
    }

    override fun acos(): Numeric {
        val result = Real(radToDefault(acos(content)))
        return if (result.content.isNaN()) {
            super.acos()
        } else {
            result
        }
    }

    override fun asin(): Numeric {
        val result = Real(radToDefault(asin(content)))
        return if (result.content.isNaN()) {
            super.asin()
        } else {
            result
        }
    }

    override fun atan(): Numeric {
        val result = Real(radToDefault(atanRad()))
        return if (result.content.isNaN()) {
            super.atan()
        } else {
            result
        }
    }

    private fun atanRad(): Double {
        return atan(content)
    }

    override fun acot(): Numeric {
        val result = Real(radToDefault(PI_DIV_BY_2_RAD_DOUBLE - atanRad()))
        return if (result.content.isNaN()) {
            super.acot()
        } else {
            result
        }
    }

    override fun cos(): Numeric {
        val value = cos(defaultToRad(content))
        return Real(Companion.normalizeTrigResult(value))
    }

    override fun sin(): Numeric {
        val value = sin(defaultToRad(content))
        return Real(Companion.normalizeTrigResult(value))
    }

    override fun tan(): Numeric {
        return Real(tan(defaultToRad(content)))
    }

    private fun tan(value: Double): Double {
        var v = value
        if (v > PI || v < -PI) {
            v = v % PI
        }
        if (kotlin.math.abs(v - (PI / 2)) < TRIG_EPS) return Double.POSITIVE_INFINITY
        if (kotlin.math.abs(v + (PI / 2)) < TRIG_EPS) return Double.NEGATIVE_INFINITY
        if (kotlin.math.abs(v) < TRIG_EPS || kotlin.math.abs(kotlin.math.abs(v) - PI) < TRIG_EPS) {
            return 0.0
        }
        return Companion.normalizeTrigResult(kotlin.math.tan(v))
    }

    override fun cot(): Numeric {
        return ONE.divide(tan())
    }

    fun valueOf(value: Real): Real {
        return Real(value.content)
    }

    override fun valueOf(numeric: Numeric): Numeric {
        return when (numeric) {
            is Real -> valueOf(numeric)
            else -> throw ArithmeticException()
        }
    }

    fun compareTo(that: Real): Int {
        return this.content.compareTo(that.content)
    }

    override fun compareTo(other: Numeric): Int {
        return when (other) {
            is Real -> compareTo(other)
            else -> other.valueOf(this).compareTo(other)
        }
    }

    override fun toString(): String {
        return toString(content)
    }

    fun toComplex(): Complex {
        return Complex.valueOf(this.content, 0.0)
    }

    override fun toBigInteger(): BigInteger? {
        return if (content == floor(content)) {
            BigInteger.fromLong(content.toLong())
        } else {
            null
        }
    }

    override fun doubleValue(): Double {
        return content
    }

    companion object {
        val ZERO = Real(0.0)

        val ONE = Real(1.0)

        val TWO = Real(2.0)

        private val PI_DIV_BY_2_RAD = valueOf(PI).divide(TWO)
        private const val PI_DIV_BY_2_RAD_DOUBLE = PI / 2
        private const val TRIG_EPS = 1e-12

        private fun normalizeTrigResult(value: Double): Double {
            return if (kotlin.math.abs(value) < TRIG_EPS) 0.0 else value
        }

        fun signum(value: Double): Int {
            return when {
                value == 0.0 -> 0
                value < 0.0 -> -1
                else -> 1
            }
        }

        fun valueOf(value: Double): Real {
            return when (value) {
                0.0 -> ZERO
                1.0 -> ONE
                2.0 -> TWO
                else -> Real(value)
            }
        }
    }
}
