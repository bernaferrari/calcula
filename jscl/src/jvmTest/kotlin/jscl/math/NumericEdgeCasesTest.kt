package jscl.math

import jscl.JsclMathEngine
import jscl.math.function.ConstantsRegistry
import kotlin.math.ln
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NumericEdgeCasesTest {

    private val engine = JsclMathEngine.getInstance()

    @Before
    fun setUp() {
        ConstantsRegistry.getInstance()
    }

    @Test
    fun testDivisionAndInfinityPropagation() {
        assertEquals("NaN", engine.evaluate("0/0"))
        assertEquals("NaN", engine.evaluate("0.0/0.0"))
        assertEquals("NaN", engine.evaluate("∞*0"))
        assertEquals("NaN", engine.evaluate("∞/∞"))
        assertEquals("NaN", engine.evaluate("∞-∞"))
        assertEquals("NaN", engine.evaluate("NaN-NaN"))
        assertEquals("NaN", engine.evaluate("NaN/NaN"))
    }

    @Test
    fun testZeroPowerZero() {
        assertEquals("1", engine.evaluate("0^0"))
    }

    @Test
    fun testAliasesAndComplexLog() {
        assertEquals("3", engine.evaluate("sqrt(9)"))
        assertEquals("0", engine.evaluate("sin(pi)"))
        assertEquals("0", engine.evaluate("sin(PI)"))

        val numeric = engine.evaluateGeneric("ln(-5)").numeric() as NumericWrapper
        val complex = numeric.content() as jscl.math.numeric.Complex
        assertEquals(kotlin.math.ln(5.0), complex.realPart(), 1e-12)
        assertEquals(Math.PI, complex.imaginaryPart(), 1e-12)
    }
}
