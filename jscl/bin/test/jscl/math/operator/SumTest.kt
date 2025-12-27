package jscl.math.operator

import jscl.JsclMathEngine
import jscl.math.function.Constant
import jscl.math.function.ExtendedConstant
import org.junit.Assert.assertEquals
import org.junit.Test

class SumTest {

    @Test
    fun testExp() {
        val me = JsclMathEngine.getInstance()
        val x = ExtendedConstant.Builder(Constant("x"), 2.0)
        me.getConstantsRegistry().addOrUpdate(x.create())
        val i = ExtendedConstant.Builder(Constant("i"), null as String?)
        me.getConstantsRegistry().addOrUpdate(i.create())
        assertEquals("51.73529646243829", me.evaluate("Σ((1+x/i)^i, i, 1, 10)"))
        assertEquals("686.0048440525586", me.evaluate("Σ((1+x/i)^i, i, 1, 100)"))
    }
}
