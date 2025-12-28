package jscl.math

import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class Ieee754EdgeCasesTest {

    private val engine = JsclMathEngine.getInstance()

    @Test
    fun testEvaluateNonFiniteResults() {
        assertEquals("NaN", engine.evaluate("0/0"))
        assertEquals("NaN", engine.evaluate("0.0/0.0"))
        assertEquals("NaN", engine.evaluate("∞*0"))
        assertEquals("NaN", engine.evaluate("∞/∞"))
        assertEquals("NaN", engine.evaluate("∞-∞"))
        assertEquals("NaN", engine.evaluate("NaN-NaN"))
        assertEquals("NaN", engine.evaluate("NaN/NaN"))

        assertEquals("∞", engine.evaluate("1/0"))
        assertEquals("-∞", engine.evaluate("-1/0"))
        assertEquals("0", engine.evaluate("0/∞"))
        assertEquals("0", engine.evaluate("1/∞"))
        assertEquals("∞", engine.evaluate("∞+1"))
        assertEquals("∞", engine.evaluate("∞-1"))
        assertEquals("NaN", engine.evaluate("∞+(-∞)"))
        assertEquals("∞", engine.evaluate("∞*∞"))
        assertEquals("NaN", engine.evaluate("0*∞"))
        assertEquals("NaN", engine.evaluate("∞*0"))
        assertEquals("NaN", engine.evaluate("∞/∞"))
        assertEquals("∞", engine.evaluate("∞/2"))
        assertEquals("0", engine.evaluate("2/∞"))
        assertEquals("NaN", engine.evaluate("NaN+1"))
        assertEquals("NaN", engine.evaluate("1+NaN"))
    }

    @Test
    fun testSimplifyNonFiniteResults() {
        assertEquals("NaN", engine.simplify("0/0"))
        assertEquals("NaN", engine.simplify("0.0/0.0"))
        assertEquals("NaN", engine.simplify("∞*0"))
        assertEquals("NaN", engine.simplify("∞/∞"))
        assertEquals("NaN", engine.simplify("∞-∞"))
        assertEquals("NaN", engine.simplify("NaN-NaN"))
        assertEquals("NaN", engine.simplify("NaN/NaN"))

        assertEquals("∞", engine.simplify("1/0"))
        assertEquals("-∞", engine.simplify("-1/0"))
        assertEquals("0", engine.simplify("0/∞"))
        assertEquals("0", engine.simplify("1/∞"))
        assertEquals("∞", engine.simplify("∞+1"))
        assertEquals("∞", engine.simplify("∞-1"))
        assertEquals("NaN", engine.simplify("∞+(-∞)"))
        assertEquals("∞", engine.simplify("∞*∞"))
        assertEquals("NaN", engine.simplify("0*∞"))
        assertEquals("NaN", engine.simplify("∞*0"))
        assertEquals("NaN", engine.simplify("∞/∞"))
        assertEquals("∞", engine.simplify("∞/2"))
        assertEquals("0", engine.simplify("2/∞"))
        assertEquals("NaN", engine.simplify("NaN+1"))
        assertEquals("NaN", engine.simplify("1+NaN"))
    }

    @Test
    fun testPowerEdgeCases() {
        assertEquals("1", engine.evaluate("0^0"))
        assertEquals("NaN", engine.evaluate("NaN^0"))
        assertEquals("NaN", engine.evaluate("0^NaN"))
        assertEquals("NaN", engine.evaluate("NaN^2"))
        assertEquals("1", engine.evaluate("∞^0"))
        assertEquals("0", engine.evaluate("0^∞"))
        assertEquals("∞", engine.evaluate("∞^1"))
        assertEquals("0", engine.evaluate("∞^(-1)"))

        assertEquals("1", engine.simplify("0^0"))
        assertEquals("NaN", engine.simplify("NaN^0"))
        assertEquals("NaN", engine.simplify("0^NaN"))
        assertEquals("NaN", engine.simplify("NaN^2"))
        assertEquals("1", engine.simplify("∞^0"))
        assertEquals("0", engine.simplify("0^∞"))
        assertEquals("∞", engine.simplify("∞^1"))
        assertEquals("0", engine.simplify("∞^(-1)"))
    }
}
