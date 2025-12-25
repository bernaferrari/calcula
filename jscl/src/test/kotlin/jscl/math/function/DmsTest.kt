package jscl.math.function

import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * User: serso
 * Date: 11/14/11
 * Time: 1:46 PM
 */
class DmsTest {
    @Test
    fun testFunction() {
        val mathEngine = JsclMathEngine.getInstance()

        assertEquals("43.1025", mathEngine.evaluate("dms(43,6,9)"))
        assertEquals("102.765", mathEngine.evaluate("dms(102, 45,  54)"))
    }
}
