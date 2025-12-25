package jscl.math.operator

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.NumericWrapper
import jscl.text.ParseException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class ModuloTest {

    @Test
    fun testNumeric() {
        assertMod(2, 2)
        assertMod(1, 2)
        assertMod(3.5, 2.0)
        assertMod(3, 2)
        assertMod(1.5, 2.0)
        assertMod(1.5, "1.5", "2")
        assertMod(1.5, "3.5", "2")
    }

    private fun assertMod(expected: Double, numerator: String, denominator: String) {
        val mod = makeModulo(numerator, denominator)
        val numeric = mod.numeric()
        assertEquals(expected, numeric.doubleValue(), 10.0.pow(-8.0))
    }

    private fun assertMod(numerator: Int, denominator: Int) {
        val mod = makeModulo(numerator, denominator)
        val numeric = mod.numeric()
        assertTrue(numeric.isInteger)
        assertEquals(numerator % denominator, numeric.integerValue().intValue())
    }

    private fun assertMod(numerator: Double, denominator: Double) {
        val mod = makeModulo(numerator, denominator)
        val numeric = mod.numeric()
        assertEquals(numerator % denominator, numeric.doubleValue(), 10.0.pow(-8.0))
    }

    private fun makeModulo(n: Int, d: Int): Modulo {
        return Modulo(NumericWrapper.valueOf(n.toLong()), NumericWrapper.valueOf(d.toLong()))
    }

    private fun makeModulo(n: String, d: String): Modulo {
        return Modulo(Expression.valueOf(n), Expression.valueOf(d))
    }

    private fun makeModulo(n: Double, d: Double): Modulo {
        return Modulo(NumericWrapper.valueOf(n), NumericWrapper.valueOf(d))
    }
}
