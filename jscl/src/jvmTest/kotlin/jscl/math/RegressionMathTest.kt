package jscl.math

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.math.function.ConstantsRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RegressionMathTest {

    private val engine = JsclMathEngine.getInstance()
    private var previousAngleUnit: AngleUnit? = null

    @Before
    fun setUp() {
        ConstantsRegistry.getInstance()
        previousAngleUnit = engine.getAngleUnits()
        engine.setAngleUnits(AngleUnit.rad)
    }

    @After
    fun tearDown() {
        previousAngleUnit?.let { engine.setAngleUnits(it) }
    }

    @Test
    fun testTrigConstantsRoundToZero() {
        assertEquals("0", engine.evaluate("sin(π)"))
        assertEquals("0", engine.evaluate("sin(-π)"))
        assertEquals("0", engine.evaluate("cos(π/2)"))
        assertEquals("0", engine.evaluate("cos(-π/2)"))
    }

    @Test
    fun testEulerIdentityAtCardinalAngles() {
        assertEquals("-1", engine.evaluate("e^(i*π)"))
        assertEquals("i", engine.evaluate("e^(i*π/2)"))
    }

    @Test
    fun testOddRootOfNegativeNumber() {
        assertEquals("-2", engine.evaluate("(-8)^(1/3)"))
    }
}
