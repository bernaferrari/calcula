package jscl

import midpcalc.Real
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.solovyev.common.NumberFormatter

/**
 * User: serso
 * Date: 12/15/11
 * Time: 11:25 AM
 */
class JsclMathEngineTest {

    private lateinit var me: JsclMathEngine

    @Before
    fun setUp() {
        me = JsclMathEngine()
    }

    @Test
    fun testFormat() {
        try {
            me.setGroupingSeparator(' ')
            assertEquals("1", me.format(1.0, NumeralBase.bin))
            assertEquals("10", me.format(2.0, NumeralBase.bin))
            assertEquals("11", me.format(3.0, NumeralBase.bin))
            assertEquals("100", me.format(4.0, NumeralBase.bin))
            assertEquals("101", me.format(5.0, NumeralBase.bin))
            assertEquals("110", me.format(6.0, NumeralBase.bin))
            assertEquals("111", me.format(7.0, NumeralBase.bin))
            assertEquals("1000", me.format(8.0, NumeralBase.bin))
            assertEquals("1001", me.format(9.0, NumeralBase.bin))
            assertEquals("1 0001", me.format(17.0, NumeralBase.bin))
            assertEquals("1 0100", me.format(20.0, NumeralBase.bin))
            assertEquals("1 0100", me.format(20.0, NumeralBase.bin))
            assertEquals("1 1111", me.format(31.0, NumeralBase.bin))

            me.setPrecision(10)

            assertEquals("111 1111 0011 0110", me.format(32566.0, NumeralBase.bin))
            assertEquals("100.0100110011", me.format(4.3, NumeralBase.bin))
            assertEquals("1 0001 0101 0011.01010101101", me.format(4435.33423, NumeralBase.bin))
            assertEquals("1100.01010101011", me.format(12.3333, NumeralBase.bin))
            assertEquals("1 0011 1101 1110 0100 0011 0101 0101.00011111", me.format(333333333.1212213321, NumeralBase.bin))

            assertEquals("0.EEEEEEEEEEF", me.format(14.0 / 15.0, NumeralBase.hex))
            assertEquals("7F 36", me.format(32566.0, NumeralBase.hex))
            assertEquals("24", me.format(36.0, NumeralBase.hex))
            assertEquals("8", me.format(8.0, NumeralBase.hex))
            assertEquals("1 3D", me.format(317.0, NumeralBase.hex))
            assertEquals("13 DE 43 55.1F085BEF", me.format(333333333.1212213321, NumeralBase.hex))
            assertEquals("D 25 0F 77 0A.6F7319", me.format(56456345354.43534534523459999, NumeralBase.hex))
            assertEquals("3 E7.4CCCCCCCCCD", me.format(999.3, NumeralBase.hex))

            me.setPrecision(NumberFormatter.MAX_PRECISION)
            assertEquals("6.CCDA6A054226DB6E-19", me.format(0.00000000000000000000009, NumeralBase.hex))
            assertEquals("A.E15D766ED03E2BEE-20", me.format(0.000000000000000000000009, NumeralBase.hex))
        } finally {
            me.setGroupingSeparator(NumberFormatter.NO_GROUPING)
        }

        assertEquals("1", me.format(1.0, NumeralBase.bin))
        assertEquals("10", me.format(2.0, NumeralBase.bin))
        assertEquals("11", me.format(3.0, NumeralBase.bin))
        assertEquals("100", me.format(4.0, NumeralBase.bin))
        assertEquals("101", me.format(5.0, NumeralBase.bin))
        assertEquals("110", me.format(6.0, NumeralBase.bin))
        assertEquals("111", me.format(7.0, NumeralBase.bin))
        assertEquals("1000", me.format(8.0, NumeralBase.bin))
        assertEquals("1001", me.format(9.0, NumeralBase.bin))
        assertEquals("10001", me.format(17.0, NumeralBase.bin))
        assertEquals("10100", me.format(20.0, NumeralBase.bin))
        assertEquals("10100", me.format(20.0, NumeralBase.bin))
        assertEquals("11111", me.format(31.0, NumeralBase.bin))
        assertEquals("111111100110110", me.format(32566.0, NumeralBase.bin))

        assertEquals("7F36", me.format(32566.0, NumeralBase.hex))
        assertEquals("24", me.format(36.0, NumeralBase.hex))
        assertEquals("8", me.format(8.0, NumeralBase.hex))
        assertEquals("13D", me.format(317.0, NumeralBase.hex))
    }

    @Test
    fun testPiComputation() {
        assertEquals("-1", me.evaluate("exp(√(-1)*Π)"))
    }

    @Test
    fun testBinShouldAlwaysUseSpaceAsGroupingSeparator() {
        me.setGroupingSeparator('\'')

        assertEquals("100 0000 0000", me.format(1024.0, NumeralBase.bin))
    }

    @Test
    fun testHexShouldAlwaysUseSpaceAsGroupingSeparator() {
        me.setGroupingSeparator('\'')

        assertEquals("4 00", me.format(1024.0, NumeralBase.hex))
    }

    @Test
    fun testEngineeringNotationWithRounding() {
        me.setNotation(Real.NumberFormat.FSE_ENG)
        me.setPrecision(5)

        assertEquals("10E6", me.format(10000000.0))
        assertEquals("99E6", me.format(99000000.0))
        assertEquals("999E6", me.format(999000000.0))
        assertEquals("999E6", me.format(999000001.0))
        assertEquals("999.00001E6", me.format(999000011.0))
        assertEquals("1E6", me.format(1000000.0))
        assertEquals("111.11E3", me.format(111110.0))
        assertEquals("111.1E3", me.format(111100.0))
        assertEquals("111E3", me.format(111000.0))
        assertEquals("110E3", me.format(110000.0))
        assertEquals("100E3", me.format(100000.0))
        assertEquals("10000", me.format(10000.0))
        assertEquals("1000", me.format(1000.0))
        assertEquals("100", me.format(100.0))
        assertEquals("100.1", me.format(100.1))
        assertEquals("100.12", me.format(100.12))
        assertEquals("100.12345", me.format(100.123454))
        assertEquals("100.12346", me.format(100.123455))
        assertEquals("100.12346", me.format(100.123456))
        assertEquals("1", me.format(1.0))
        assertEquals("-42", me.format(-42.0))
        assertEquals("-999", me.format(-999.0))
        assertEquals("-999.99", me.format(-999.99))
        assertEquals("-0.1", me.format(-0.1))

        assertEquals("-0.12", me.format(-0.12))
        assertEquals("-0.123", me.format(-0.123))
        assertEquals("-0.1234", me.format(-0.1234))
        assertEquals("0.1", me.format(0.1))
        assertEquals("0.01", me.format(0.01))
        assertEquals("0.001", me.format(0.001))
        assertEquals("0.001", me.format(0.00100000001))
        assertEquals("0.0011", me.format(0.0011))
        assertEquals("0.001", me.format(0.000999999))
        assertEquals("0.0001", me.format(0.0001))
        assertEquals("1E-6", me.format(0.000001))
        assertEquals("10E-9", me.format(0.00000001))

        assertEquals("-100.001E3", me.format(-100001.0))
        assertEquals("100.001E3", me.format(100001.0))
        assertEquals("111.111E3", me.format(111111.0))
        assertEquals("111.11123E3", me.format(111111.234567))
        assertEquals("111.11123E3", me.format(111111.23456))
        assertEquals("111.11123E3", me.format(111111.2345))
        assertEquals("111.11123E3", me.format(111111.2345))
        assertEquals("111.11123E3", me.format(111111.234))
        assertEquals("111.11123E3", me.format(111111.23))
        assertEquals("111.1112E3", me.format(111111.2))
    }

    @Test
    fun testEngineeringNotationWithoutRounding() {
        me.setNotation(Real.NumberFormat.FSE_ENG)
        me.setPrecision(NumberFormatter.MAX_PRECISION)

        assertEquals("10E6", me.format(10000000.0))
        assertEquals("99E6", me.format(99000000.0))
        assertEquals("999E6", me.format(999000000.0))
        assertEquals("999.000001E6", me.format(999000001.0))
        assertEquals("999.000011E6", me.format(999000011.0))
        assertEquals("1E6", me.format(1000000.0))
        assertEquals("111.11E3", me.format(111110.0))
        assertEquals("111.1E3", me.format(111100.0))
        assertEquals("111E3", me.format(111000.0))
        assertEquals("110E3", me.format(110000.0))
        assertEquals("100E3", me.format(100000.0))
        assertEquals("10000", me.format(10000.0))
        assertEquals("1000", me.format(1000.0))
        assertEquals("100", me.format(100.0))
        assertEquals("100.1", me.format(100.1))
        assertEquals("100.12", me.format(100.12))
        assertEquals("100.123454", me.format(100.123454))
        assertEquals("100.123455", me.format(100.123455))
        assertEquals("100.123456", me.format(100.123456))
        assertEquals("1", me.format(1.0))
        assertEquals("-42", me.format(-42.0))
        assertEquals("-999", me.format(-999.0))
        assertEquals("-999.99", me.format(-999.99))
        assertEquals("-0.1", me.format(-0.1))
        assertEquals("-0.12", me.format(-0.12))
        assertEquals("-0.123", me.format(-0.123))
        assertEquals("-0.1234", me.format(-0.1234))
        assertEquals("0.1", me.format(0.1))
        assertEquals("0.01", me.format(0.01))
        assertEquals("0.001", me.format(0.001))
        assertEquals("0.0011", me.format(0.0011))
        assertEquals("0.000999999", me.format(0.000999999))
        assertEquals("0.0001", me.format(0.0001))

        assertEquals("100.001E3", me.format(100001.0))
        assertEquals("111.111E3", me.format(111111.0))
        assertEquals("111.111234567E3", me.format(111111.234567))
        assertEquals("111.11123456E3", me.format(111111.23456))
        assertEquals("111.1112345E3", me.format(111111.2345))
        assertEquals("111.1112345E3", me.format(111111.2345))
        assertEquals("111.111234E3", me.format(111111.234))
        assertEquals("111.11123E3", me.format(111111.23))
        assertEquals("111.1112E3", me.format(111111.2))
    }
}
