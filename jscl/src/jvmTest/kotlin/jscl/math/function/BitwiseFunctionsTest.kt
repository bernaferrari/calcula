package jscl.math.function

import jscl.math.Expression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Test

class BitwiseFunctionsTest {

    @After
    fun resetBitwiseDefaults() {
        BitwiseRuntimeConfig.update(wordSize = 64, signed = true)
    }

    @Test
    fun `supports binary bitwise functions`() {
        assertInteger("and(6,3)", 2)
        assertInteger("or(6,3)", 7)
        assertInteger("xor(6,3)", 5)
    }

    @Test
    fun `supports unary and shift bitwise functions`() {
        assertInteger("not(0)", -1)
        assertInteger("shl(3,2)", 12)
        assertInteger("shr(12,2)", 3)
    }

    @Test
    fun `respects signed and unsigned modes for configured word size`() {
        BitwiseRuntimeConfig.update(wordSize = 8, signed = true)
        assertInteger("shr(128,1)", -64)

        BitwiseRuntimeConfig.update(wordSize = 8, signed = false)
        assertInteger("not(0)", 255)
        assertInteger("shr(128,1)", 64)
        assertInteger("shl(255,1)", 254)
    }

    private fun assertInteger(expression: String, expected: Int) {
        val numeric = Expression.valueOf(expression).numeric()
        assertTrue(numeric.isInteger)
        assertEquals(expected, numeric.integerValue().intValue())
    }
}
