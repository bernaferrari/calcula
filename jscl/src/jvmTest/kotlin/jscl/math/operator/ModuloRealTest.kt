package jscl.math.operator

import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class ModuloRealTest {

    private val engine = JsclMathEngine.getInstance()

    @Test
    fun testModuloWithReals() {
        assertEquals(0.5, engine.evaluate("mod(5.5,1)").toDouble(), 1e-12)
        assertEquals(1.0, engine.evaluate("mod(5,2)").toDouble(), 1e-12)
    }
}
