package jscl.math.function

import jscl.JsclMathEngine
import jscl.math.Expression
import jscl.math.JsclInteger
import org.junit.Assert.fail
import org.junit.Test

/**
 * User: serso
 * Date: 6/15/13
 * Time: 10:13 PM
 */
class PowTest {

    @Test
    fun testPow() {
        val me = JsclMathEngine.getInstance()

        Pow(Expression.valueOf("10"), Inverse(JsclInteger.valueOf(10L)).expressionValue()).rootValue()
        try {
            Pow(Expression.valueOf("10"), Inverse(JsclInteger.valueOf(10000000000L)).expressionValue()).rootValue()
            fail()
        } catch (e: NotRootException) {
            // ok
        }
    }
}
