package jscl.math

import jscl.JsclMathEngine
import jscl.util.ExpressionGenerator
import org.junit.Test

/**
 * User: serso
 * Date: 12/14/11
 * Time: 10:40 PM
 */
class RandomExpressionTest {

    @Test
    fun testRandomExpressions() {
        val eg = ExpressionGenerator(20)
        var i = 0
        while (i < MAX) {
            val expression = eg.generate()
            val result = JsclMathEngine.getInstance().evaluate(expression)

            // println("$result-($expression)")

            i++
        }
    }

    companion object {
        const val MAX = 1000
    }
}
