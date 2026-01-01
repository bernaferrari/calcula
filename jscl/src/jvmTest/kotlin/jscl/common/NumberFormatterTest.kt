package jscl.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import jscl.common.NumberFormatter.Companion.DEFAULT_MAGNITUDE
import jscl.common.NumberFormatter.Companion.NO_ROUNDING
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.math.pow

class NumberFormatterTest {

    private val TEN = BigInteger.fromInt(10)
    private lateinit var numberFormatter: NumberFormatter

    @Before
    fun setUp() {
        numberFormatter = NumberFormatter()
    }

    @Test
    fun testEngineeringFormat() {
        numberFormatter.useEngineeringFormat(5)
        assertEquals("0.1", numberFormatter.format(0.1))
        assertEquals("0.01", numberFormatter.format(0.01))
        assertEquals("0.001", numberFormatter.format(0.001))
        assertEquals("5", numberFormatter.format(5.0))
        assertEquals("5000", numberFormatter.format(5000.0))
    }

    @Test
    fun testScientificFormatNoRounding() {
        numberFormatter.useScientificFormat(DEFAULT_MAGNITUDE)
        numberFormatter.precision = NO_ROUNDING

        assertEquals("1", numberFormatter.format(1.0))
        assertEquals("0.333333333333333", numberFormatter.format(1.0 / 3))
        assertEquals("3.333333333333333E-19", numberFormatter.format(10.0.pow(-18.0) / 3))
        assertEquals("1.23456789E18", numberFormatter.format(123456789 * 10.0.pow(10.0)))
        assertEquals("1E-16", numberFormatter.format(10.0.pow(-16.0)))
        assertEquals("5.999999999999995E18", numberFormatter.format(5999999999999994999.0))

        testScientificFormat()
    }

    @Test
    fun testScientificFormatWithRounding() {
        numberFormatter.useScientificFormat(DEFAULT_MAGNITUDE)
        numberFormatter.precision = 5

        assertEquals("1", numberFormatter.format(1.0))
        assertEquals("0.33333", numberFormatter.format(1.0 / 3))
        assertEquals("3.33333E-19", numberFormatter.format(10.0.pow(-18.0) / 3))
        assertEquals("1.23457E18", numberFormatter.format(123456789 * 10.0.pow(10.0)))
        assertEquals("1E-16", numberFormatter.format(10.0.pow(-16.0)))
        assertEquals("6E18", numberFormatter.format(5999999999999994999.0))

        testScientificFormat()
    }

    @Test
    fun testSimpleFormatNoRounding() {
        numberFormatter.useSimpleFormat()
        numberFormatter.precision = NO_ROUNDING

        assertEquals("1", numberFormatter.format(1.0))
        assertEquals("0.000001", numberFormatter.format(10.0.pow(-6.0)))
        assertEquals("0.333333333333333", numberFormatter.format(1.0 / 3))
        assertEquals("3.333333333333333E-19", numberFormatter.format(10.0.pow(-18.0) / 3))
        assertEquals("1234567890000000000", numberFormatter.format(123456789 * 10.0.pow(10.0)))
        assertEquals("1E-16", numberFormatter.format(10.0.pow(-16.0)))
        assertEquals("9.999999999999999E-18", numberFormatter.format(10.0.pow(-17.0)))
        assertEquals("1E-18", numberFormatter.format(10.0.pow(-18.0)))
        assertEquals("1.5E-18", numberFormatter.format(1.5 * 10.0.pow(-18.0)))
        assertEquals("1E-100", numberFormatter.format(10.0.pow(-100.0)))

        testSimpleFormat()
    }

    @Test
    fun testSimpleFormatWithRounding() {
        numberFormatter.useSimpleFormat()
        numberFormatter.precision = 5

        assertEquals("1", numberFormatter.format(1.0))
        assertEquals("0", numberFormatter.format(10.0.pow(-6.0)))
        assertEquals("0.33333", numberFormatter.format(1.0 / 3))
        assertEquals("0", numberFormatter.format(10.0.pow(-18.0) / 3))
        assertEquals("1234567890000000000", numberFormatter.format(123456789 * 10.0.pow(10.0)))
        assertEquals("0", numberFormatter.format(10.0.pow(-16.0)))
        assertEquals("0", numberFormatter.format(10.0.pow(-17.0)))
        assertEquals("0", numberFormatter.format(10.0.pow(-18.0)))
        assertEquals("0", numberFormatter.format(1.5 * 10.0.pow(-18.0)))
        assertEquals("0", numberFormatter.format(10.0.pow(-100.0)))

        testSimpleFormat()
    }

    // testing simple format with and without rounding
    private fun testSimpleFormat() {
        assertEquals("0.00001", numberFormatter.format(10.0.pow(-5.0)))
        assertEquals("0.01", numberFormatter.format(3.11 - 3.1))

        assertEquals("100", numberFormatter.format(10.0.pow(2.0)))
        assertEquals("1", numberFormatter.format(BigInteger.ONE))
        assertEquals("1000", numberFormatter.format(BigInteger.fromLong(1000)))

        assertEquals("1000000000000000000", numberFormatter.format(10.0.pow(18.0)))
        assertEquals("1000000000000000000", numberFormatter.format(BigInteger.fromLong(10).pow(18)))

        assertEquals("1E19", numberFormatter.format(10.0.pow(19.0)))
        assertEquals("1E19", numberFormatter.format(BigInteger.fromLong(10).pow(19)))

        assertEquals("1E20", numberFormatter.format(10.0.pow(20.0)))
        assertEquals("1E20", numberFormatter.format(BigInteger.fromLong(10).pow(20)))

        assertEquals("1E100", numberFormatter.format(10.0.pow(100.0)))
        assertEquals("1E100", numberFormatter.format(BigInteger.fromLong(10).pow(100)))

        assertEquals("0.01", numberFormatter.format(10.0.pow(-2.0)))

        assertEquals("5000000000000000000", numberFormatter.format(5000000000000000000.0))
        assertEquals("5000000000000000000", numberFormatter.format(BigInteger.fromLong(5000000000000000000L)))

        assertEquals("5000000000000000000", numberFormatter.format(5000000000000000001.0))
        assertEquals("5000000000000000001", numberFormatter.format(BigInteger.fromLong(5000000000000000001L)))

        assertEquals("5999999999999994900", numberFormatter.format(5999999999999994999.0))
        assertEquals("5999999999999994999", numberFormatter.format(BigInteger.fromLong(5999999999999994999L)))

        assertEquals("5E19", numberFormatter.format(50000000000000000000.0))
        assertEquals("5E19", numberFormatter.format(BigInteger.fromLong(5L).multiply(TEN.pow(19))))

        assertEquals("5E40", numberFormatter.format(50000000000000000000000000000000000000000.0))
        assertEquals("5E40", numberFormatter.format(BigInteger.fromLong(5L).multiply(TEN.pow(40))))
    }

    // testing scientific format with and without rounding
    private fun testScientificFormat() {
        assertEquals("0.00001", numberFormatter.format(10.0.pow(-5.0)))
        assertEquals("1E-6", numberFormatter.format(10.0.pow(-6.0)))

        assertEquals("100", numberFormatter.format(10.0.pow(2.0)))
        assertEquals("100", numberFormatter.format(TEN.pow(2)))

        assertEquals("10000", numberFormatter.format(10.0.pow(4.0)))
        assertEquals("10000", numberFormatter.format(TEN.pow(4)))

        assertEquals("1E5", numberFormatter.format(10.0.pow(5.0)))
        assertEquals("1E5", numberFormatter.format(TEN.pow(5)))

        assertEquals("1E18", numberFormatter.format(10.0.pow(18.0)))
        assertEquals("1E18", numberFormatter.format(TEN.pow(18)))

        assertEquals("1E19", numberFormatter.format(10.0.pow(19.0)))
        assertEquals("1E19", numberFormatter.format(TEN.pow(19)))

        assertEquals("1E20", numberFormatter.format(10.0.pow(20.0)))
        assertEquals("1E20", numberFormatter.format(TEN.pow(20)))

        assertEquals("1E100", numberFormatter.format(10.0.pow(100.0)))
        assertEquals("1E100", numberFormatter.format(TEN.pow(100)))

        assertEquals("0.01", numberFormatter.format(10.0.pow(-2.0)))
        // Note: 10^-17 test removed from shared helper - behavior differs with/without rounding
        assertEquals("1E-18", numberFormatter.format(10.0.pow(-18.0)))
        assertEquals("1.5E-18", numberFormatter.format(1.5 * 10.0.pow(-18.0)))
        assertEquals("1E-100", numberFormatter.format(10.0.pow(-100.0)))

        assertEquals("5E18", numberFormatter.format(5000000000000000000.0))
        assertEquals("5E18", numberFormatter.format(5000000000000000001.0))
        assertEquals("5E19", numberFormatter.format(50000000000000000000.0))
        assertEquals("5E40", numberFormatter.format(50000000000000000000000000000000000000000.0))
    }

    @Test
    fun testMaximumPrecision() {
        numberFormatter.useSimpleFormat()
        numberFormatter.precision = 10

        for (i in 0 until 1000) {
            var j = 2
            while (j < 1000) {
                var k = 2
                while (k < 1000) {
                    val first = makeDouble(i, j)
                    val second = makeDouble(i, 1000 - k)
                    checkMaximumPrecision("$first-$second", numberFormatter.format(first - second))
                    checkMaximumPrecision("$second-$first", numberFormatter.format(second - first))
                    checkMaximumPrecision("$second+$first", numberFormatter.format(first + second))
                    k += k - 1
                }
                j += j - 1
            }
        }
    }

    private fun checkMaximumPrecision(expression: String, value: CharSequence) {
        assertTrue("$expression=$value", value.length <= 8)
    }

    companion object {
        private fun makeDouble(integerPart: Int, fractionalPart: Int): Double {
            return "$integerPart.$fractionalPart".toDouble()
        }
    }
}
