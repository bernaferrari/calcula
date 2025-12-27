package jscl.math.function.trigonometric

import jscl.AngleUnit
import jscl.JsclMathEngine
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * User: serso
 * Date: 1/7/12
 * Time: 3:51 PM
 */
class SinTest {

    @Test
    fun testIntegrate() {
        val me = JsclMathEngine.getInstance()

        // todo serso: uncomment after variable modification issue fixed
        /*assertEquals("-cos(x)", me.simplify("∫(sin(x), x)"))
          assertEquals("-cos(x*π)/π", me.simplify("∫(sin(π*x), x)"))

          assertEquals("1.0", me.evaluate("cos(0)"))
          assertEquals("0.8660254037844387", me.evaluate("cos(30)"))
          assertEquals("0.1339745962155613", me.evaluate("∫ab(sin(x), x, 0, 30)"))*/

        try {
            me.setAngleUnits(AngleUnit.rad)
            assertEquals("0.54030230586814", me.evaluate("cos(1)"))
            assertEquals("0.362357754476674", me.evaluate("cos(1.2)"))
            assertEquals("0.177944551391466", me.evaluate("∫ab(sin(x), x, 1, 1.2)"))
        } finally {
            me.setAngleUnits(AngleUnit.deg)
        }

        // assertEquals("7.676178925", me.evaluate("∫ab(sin(x), x, 0, 30°)"))

        try {
            me.setAngleUnits(AngleUnit.rad)
            assertEquals("0.133974596215561", me.evaluate("∫ab(sin(x), x, 0, 30°)"))
        } finally {
            me.setAngleUnits(AngleUnit.deg)
        }
    }
}
