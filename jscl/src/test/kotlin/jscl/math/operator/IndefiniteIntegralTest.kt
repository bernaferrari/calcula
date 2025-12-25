package jscl.math.operator

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.MathEngine
import jscl.math.Expression
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/**
 * User: serso
 * Date: 1/14/12
 * Time: 1:06 PM
 */
class IndefiniteIntegralTest {

    @Test
    fun testIntegral() {
        val me: MathEngine = JsclMathEngine.getInstance()

        try {
            assertEquals("∫(sin(t!), t)", me.evaluate("∫(sin(t!), t)"))
            fail()
        } catch (e: ArithmeticException) {
            // ok
        }

        try {
            me.setAngleUnits(AngleUnit.rad)
            assertEquals("-cos(t)", Expression.valueOf("∫(sin(t), t)").expand().toString())
            assertEquals("∫(sin(t!), t)", Expression.valueOf("∫(sin(t!), t)").expand().toString())
            assertEquals("∫(sin(t!), t)", me.simplify("∫(sin(t!), t)"))
            assertEquals("∫(sin(t°), t)", Expression.valueOf("∫(sin(t°), t)").expand().toString())
            assertEquals("∫(sin(t°), t)", me.simplify("∫(sin(t°), t)"))
        } finally {
            me.setAngleUnits(AngleUnit.deg)
        }
    }
}
