package jscl.math.function

import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * User: serso
 * Date: 2/10/12
 * Time: 9:35 PM
 */
class SgnTest {

    @Test
    fun testSgn() {
        val me = JsclMathEngine.getInstance()

        assertEquals("1", me.evaluate("sgn(10)"))
        assertEquals("1", me.evaluate("sgn(0.5)"))
        assertEquals("0", me.evaluate("sgn(0)"))
        assertEquals("0", me.evaluate("sgn(-0)"))
        assertEquals("-1", me.evaluate("sgn(-1)"))
        assertEquals("-1", me.evaluate("sgn(-10)"))
    }
}
