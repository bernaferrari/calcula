package midpcalc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RealTest {
    private fun assertRealEquals(expected: Double, actual: Real, epsilon: Double = 1e-9) {
        assertEquals(expected, actual.toDouble(), epsilon)
    }

    @Test
    fun testConstructorsAndToDouble() {
        assertRealEquals(0.0, Real())
        assertRealEquals(1.0, Real(1))
        assertRealEquals(-42.0, Real(-42))
        assertRealEquals(1234567890123.0, Real(1_234_567_890_123L))
        assertRealEquals(123.456, Real("123.456"), 1e-12)
        assertRealEquals(255.0, Real("FF", 16))
    }

    @Test
    fun testAssignAndCopy() {
        val a = Real(5)
        val b = Real(a)
        assertRealEquals(5.0, b)
        a.add(Real(3))
        assertRealEquals(8.0, a)
        assertRealEquals(5.0, b)
    }

    @Test
    fun testBasicArithmetic() {
        val a = Real(2)
        a.add(Real(3))
        assertRealEquals(5.0, a)

        a.sub(Real(4))
        assertRealEquals(1.0, a)

        a.mul(Real(10))
        assertRealEquals(10.0, a)

        a.div(Real(4))
        assertRealEquals(2.5, a, 1e-12)
    }

    @Test
    fun testPowAndRoots() {
        val a = Real(2)
        a.pow(Real(10))
        assertRealEquals(1024.0, a)

        val b = Real(Real.TWO)
        b.sqrt()
        assertRealEquals(kotlin.math.sqrt(2.0), b)

        val c = Real(27)
        c.cbrt()
        assertRealEquals(3.0, c, 1e-9)
    }

    @Test
    fun testExpLnRoundTrip() {
        val original = Real("1.25")
        val value = Real(original)
        value.exp()
        value.ln()
        assertRealEquals(original.toDouble(), value, 1e-8)
    }

    @Test
    fun testTrigBasics() {
        val halfPi = Real(Real.PI_2)
        halfPi.sin()
        assertRealEquals(1.0, halfPi, 1e-9)

        val pi = Real(Real.PI)
        pi.cos()
        assertRealEquals(-1.0, pi, 1e-9)
    }

    @Test
    fun testRounding() {
        val value = Real("1.9")
        val floor = Real(value)
        floor.floor()
        assertRealEquals(1.0, floor)

        val ceil = Real(value)
        ceil.ceil()
        assertRealEquals(2.0, ceil)

        val round = Real(value)
        round.round()
        assertRealEquals(2.0, round)

        val trunc = Real(value)
        trunc.trunc()
        assertRealEquals(1.0, trunc)
    }

    @Test
    fun testBitsRoundTrip() {
        val values = doubleArrayOf(
            0.0,
            -0.0,
            1.0,
            -1.0,
            1e-300,
            1e300
        )
        for (value in values) {
            val bits = value.toBits()
            val real = Real()
            real.assignDoubleBits(bits)
            assertEquals(bits, real.toDoubleBits())
        }
    }

    @Test
    fun testSpecialValues() {
        val nan = Real()
        nan.assignDoubleBits(Double.NaN.toBits())
        assertTrue(nan.isNan())
        assertFalse(nan.isInfinity())
        assertFalse(nan.isFinite())

        val inf = Real()
        inf.assignDoubleBits(Double.POSITIVE_INFINITY.toBits())
        assertTrue(inf.isInfinity())
        assertFalse(inf.isNan())
        assertFalse(inf.isFinite())

        val negInf = Real()
        negInf.assignDoubleBits(Double.NEGATIVE_INFINITY.toBits())
        assertTrue(negInf.isInfinity())
        assertTrue(negInf.isNegative())
    }
}
