package jscl.math

import jscl.AngleUnit
import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class ComplexTrigEdgeCasesTest {

    private val engine = JsclMathEngine.getInstance()

    @Test
    fun testEulerFormulaInRadians() {
        val previous = engine.angleUnits
        try {
            engine.setAngleUnits(AngleUnit.rad)
            assertEquals("-1", engine.evaluate("exp(i*π)"))
            assertEquals("i", engine.evaluate("exp(i*π/2)"))
        } finally {
            engine.setAngleUnits(previous)
        }
    }

    @Test
    fun testTrigAtSpecialPoints() {
        val previous = engine.angleUnits
        try {
            engine.setAngleUnits(AngleUnit.rad)
            assertEquals("0", engine.evaluate("sin(π)"))
            assertEquals("0", engine.evaluate("sin(-π)"))
            assertEquals("0", engine.evaluate("cos(π/2)"))
            assertEquals("0", engine.evaluate("cos(-π/2)"))
            assertEquals("∞", engine.evaluate("tan(π/2)"))
            assertEquals("-∞", engine.evaluate("tan(-π/2)"))
        } finally {
            engine.setAngleUnits(previous)
        }
    }
}
