package jscl.math.function

import jscl.JsclMathEngine
import jscl.MathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * User: serso
 * Date: 1/9/12
 * Time: 6:49 PM
 */
class LnTest {

    @Test
    fun testConjugate() {
        val me: MathEngine = JsclMathEngine.getInstance()

        assertEquals("ln(5-i)", me.simplify("conjugate(ln(5+√(-1)))"))
        assertEquals("lg(5-i)", me.simplify("conjugate(lg(5+√(-1)))"))
    }

    @Test
    fun testAntiDerivative() {
        val me: MathEngine = JsclMathEngine.getInstance()

        assertEquals("-x+x*ln(x)", me.simplify("∫(ln(x), x)"))
        assertEquals("-(x-x*ln(x))/ln(10)", me.simplify("∫(lg(x), x)"))
    }

    @Test
    fun testDerivative() {
        val me: MathEngine = JsclMathEngine.getInstance()

        assertEquals("1/x", me.simplify("∂(ln(x), x)"))
        assertEquals("1/(x*ln(10))", me.simplify("∂(lg(x), x)"))
    }
}
