package jscl.math

import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class TrigonometricFormattingTest {

    private val engine = JsclMathEngine.getInstance()

    @Test
    fun testSmallRoundingToZero() {
        val previousUnits = engine.angleUnits
        try {
            engine.setAngleUnits(jscl.AngleUnit.rad)
            assertEquals("0", engine.evaluate("sin(π)"))
            assertEquals("0", engine.evaluate("sin(-π)"))
            assertEquals("0", engine.evaluate("cos(π/2)"))
            assertEquals("0", engine.evaluate("cos(-π/2)"))
        } finally {
            engine.setAngleUnits(previousUnits)
        }
    }
}
