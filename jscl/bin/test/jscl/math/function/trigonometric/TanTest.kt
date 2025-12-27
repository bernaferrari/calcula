package jscl.math.function.trigonometric

import jscl.AngleUnit
import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * User: serso
 * Date: 1/7/12
 * Time: 4:03 PM
 */
class TanTest {

    @Test
    fun testIntegrate() {
        val me = JsclMathEngine.getInstance()

        // todo serso: uncomment after variable modification issue fixed
        /*assertEquals("-2*ln(2)-ln(cos(x))", me.simplify("∫(tan(x), x)"))
        assertEquals("-(2*ln(2)+ln(cos(x*π)))/π", me.simplify("∫(tan(π*x), x)"))

        assertEquals("-0.015308831465985804", me.evaluate("ln(cos(10))"))
        assertEquals("-0.1438410362258904", me.evaluate("ln(cos(30))"))
        assertEquals("0.12853220475990468", me.evaluate("∫ab(tan(x), x, 10, 30)"))*/

        try {
            me.setAngleUnits(AngleUnit.rad)
            assertEquals("-2*ln(2)-ln(cos(x))", me.simplify("∫(tan(x), x)"))
            assertEquals("-(2*ln(2)+ln(cos(x*π)))/π", me.simplify("∫(tan(π*x), x)"))
            assertEquals("-0.015308831465986", me.evaluate("ln(cos(10*π/180))"))
            assertEquals("-0.14384103622589", me.evaluate("ln(cos(30*π/180))"))
            assertEquals("0.128532204759905", me.evaluate("∫ab(tan(x), x, 10*π/180, 30*π/180)"))
        } finally {
            me.setAngleUnits(AngleUnit.deg)
        }
    }

    @Test
    fun testBoundaryConditions() {
        val me = JsclMathEngine.getInstance()
        assertEquals("-∞", me.evaluate("tan(-450)"))
        assertEquals("0", me.evaluate("tan(-360)"))
        assertEquals("-∞", me.evaluate("tan(-270)"))
        assertEquals("0", me.evaluate("tan(-180)"))
        assertEquals("-∞", me.evaluate("tan(-90)"))
        assertEquals("0", me.evaluate("tan(0)"))
        assertEquals("∞", me.evaluate("tan(180/2)"))
        assertEquals("∞", me.evaluate("tan(45 + 45)"))
        assertEquals("∞", me.evaluate("tan(30*3)"))
        assertEquals("∞", me.evaluate("tan(90)"))
        assertEquals("0", me.evaluate("tan(180)"))
        assertEquals("∞", me.evaluate("tan(270)"))
        assertEquals("0", me.evaluate("tan(360)"))
        assertEquals("∞", me.evaluate("tan(450)"))
    }
}
