package jscl.math.function

import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class PowEdgeCasesTest {

    private val engine = JsclMathEngine.getInstance()

    @Test
    fun testNegativeBaseFractionalExponent() {
        assertEquals("-2", engine.evaluate("(-8)^(1/3)"))
        assertEquals("9", engine.evaluate("(-27)^(2/3)"))
        assertEquals("-3", engine.evaluate("(-27)^(1/3)"))
    }
}
