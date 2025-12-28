package jscl.math.function

import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.pow

/**
 * User: serso
 * Date: 11/12/11
 * Time: 4:17 PM
 */
class DegTest {

    @Test
    fun testDeg() {
        val mathEngine = JsclMathEngine.getInstance()

        assertEquals("2", mathEngine.evaluate("deg(0.03490658503988659)"))
        assertEquals("-2", mathEngine.evaluate("deg(-0.03490658503988659)"))
        assertEquals("180", mathEngine.evaluate("deg($PI)"))

        for (i in 0 until 1000) {
            val value = Math.random() * 100000
            assertEquals(value, mathEngine.evaluate("rad(deg($value))").toDouble())
            assertEquals(value, mathEngine.evaluate("deg(rad($value))").toDouble())
        }
    }

    private fun assertEquals(expected: Double, actual: Double) {
        assertEquals(expected, actual, 10.0.pow(-8.0))
    }
}
