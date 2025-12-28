package jscl.math.operator

import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class PercentBehaviorTest {

    private val engine = JsclMathEngine.getInstance()

    @Test
    fun testPercentInParentheses() {
        assertEquals("0.6", engine.evaluate("2*(20%+10%)"))
        assertEquals("0.5", engine.evaluate("10%+20%*2"))
    }

    @Test
    fun testPercentWithNegativeValues() {
        assertEquals("-198", engine.evaluate("-200-(-200%)"))
    }

    @Test
    fun testPercentWithMultiplicationContext() {
        assertEquals("65", engine.evaluate("60+50*10%"))
    }
}
