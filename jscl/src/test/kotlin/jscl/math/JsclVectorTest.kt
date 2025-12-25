package jscl.math

import jscl.JsclMathEngine
import jscl.MathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * User: serso
 * Date: 12/26/11
 * Time: 9:52 AM
 */
class JsclVectorTest {

    @Test
    fun testVector() {
        val me: MathEngine = JsclMathEngine.getInstance()
        assertEquals("[1, 0, 0, 1]", me.evaluate("[1, 0, 0, 1]"))
    }
}
