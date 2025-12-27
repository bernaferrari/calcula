package jscl.math.function

import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class RadTest {
    @Test
    fun testRad() {
        val mathEngine = JsclMathEngine()

        assertEquals("0.034906585039887", mathEngine.evaluate("rad(2)"))
        assertEquals("0.034906585039887", mathEngine.evaluate("rad(1+1)"))
        assertEquals("-0.034906585039887", mathEngine.evaluate("rad(-2)"))
        assertEquals("-0.034906585039887", mathEngine.evaluate("rad(-1-1)"))
        assertEquals("π", mathEngine.evaluate("rad(180)"))
        assertEquals((-PI).toString(), mathEngine.evaluate("rad(-180)"))

        // todo serso: think about zeroes
        assertEquals("rad(-180, 0, 0)", mathEngine.simplify("rad(-180)"))
        assertEquals("rad(2, 0, 0)", mathEngine.simplify("rad(1+1)"))

        assertEquals("rad(-180, 0, 0)", mathEngine.elementary("rad(-180)"))
        assertEquals("rad(2, 0, 0)", mathEngine.elementary("rad(1+1)"))

        assertEquals(mathEngine.evaluate("rad(43.1025)"), mathEngine.evaluate("rad(43,6,9)"))
        assertEquals(mathEngine.evaluate("rad(102.765)"), mathEngine.evaluate("rad(102, 45,  54)"))
    }
}
