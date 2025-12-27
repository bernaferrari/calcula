package jscl.math.function.trigonometric

import jscl.JsclMathEngine
import jscl.math.function.Constant
import jscl.math.function.ExtendedConstant
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * User: serso
 * Date: 6/17/13
 * Time: 10:36 PM
 */
class CosTest {

    @Test
    fun testIntegral() {
        val me = JsclMathEngine.getInstance()
        val t = ExtendedConstant.Builder(Constant("t"), 10.0)
        me.getConstantsRegistry().addOrUpdate(t.create())
        assertEquals("-sin(t)", me.simplify("∂(cos(t),t,t,1)"))
        assertEquals("∂(cos(t), t, t, 1°)", me.simplify("∂(cos(t),t,t,1°)"))
        assertEquals("-0.17364817766693", me.evaluate("∂(cos(t),t,t,1)"))
        assertEquals("∂(cos(t), t, t, 1°)", me.evaluate("∂(cos(t),t,t,1°)"))
        assertEquals("-0.17364817766693", me.evaluate("∂(cos(t),t,t,2-1)"))
        assertEquals("-0.17364817766693", me.evaluate("∂(cos(t),t,t,2^5-31)"))
    }
}
