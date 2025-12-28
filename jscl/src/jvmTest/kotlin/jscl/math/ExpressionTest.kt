package jscl.math

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.MathEngine
import jscl.NumeralBase
import jscl.math.function.Constant
import jscl.math.function.ExtendedConstant
import jscl.math.function.IConstant
import jscl.text.ParseException
import midpcalc.Real
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.solovyev.common.NumberFormatter
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

class ExpressionTest {

    @Test
    fun testImag() {
        assertEquals("-i", Expression.valueOf("i^3").numeric().toString())
    }

    @Test
    fun testConstants() {
        assertTrue(Expression.valueOf("3+4").constants.isEmpty())

        var constants = Expression.valueOf("3+4*t").constants
        assertTrue(constants.size == 1)
        assertTrue(constants.contains(Constant("t")))

        var constant: IConstant? = null

        val me = JsclMathEngine.getInstance()
        try {
            val t_0 = ExtendedConstant.Builder(Constant("t_0"), 1.0)
            constant = me.getConstantsRegistry().addOrUpdate(t_0.create())

            constants = Expression.valueOf("3+4*t_0+t_0+t_1").constants
            assertTrue(constants.size == 2)
            assertTrue(constants.contains(Constant("t_0")))
            assertTrue(constants.contains(Constant("t_1")))

            val expression = Expression.valueOf("2*t_0+5*t_1")

            assertEquals("7", expression.substitute(Constant("t_1"), Expression.valueOf(1.0)).numeric().toString())
            assertEquals("12", expression.substitute(Constant("t_1"), Expression.valueOf(2.0)).numeric().toString())
            assertEquals("27", expression.substitute(Constant("t_1"), Expression.valueOf(5.0)).numeric().toString())
        } finally {
            if (constant != null) {
                val jBuilder = ExtendedConstant.Builder(Constant(constant.name), null as String?)
                me.getConstantsRegistry().addOrUpdate(jBuilder.create())
            }
        }
    }

    @Test
    fun testExpressions() {
        assertEquals("3", Expression.valueOf("3").numeric().toString())
        assertEquals("0.693147180559945", Expression.valueOf("ln(2)").numeric().toString())
        assertEquals("1", Expression.valueOf("lg(10)").numeric().toString())
        assertEquals("0", Expression.valueOf("eq(0, 1)").numeric().toString())
        assertEquals("1", Expression.valueOf("eq(1, 1)").numeric().toString())

        assertEquals("24", Expression.valueOf("4!").numeric().toString())
        try {
            Expression.valueOf("(-3+2)!").numeric().toString()
            fail()
        } catch (e: ArithmeticException) {
            // Expected
        }
        assertEquals("24", Expression.valueOf("(2+2)!").numeric().toString())
        assertEquals("120", Expression.valueOf("(2+2+1)!").numeric().toString())
        assertEquals("24", Expression.valueOf("(2.0+2.0)!").numeric().toString())
        assertEquals("24", Expression.valueOf("4.0!").numeric().toString())
        assertEquals("48", Expression.valueOf("2*4.0!").numeric().toString())
        assertEquals("40320", Expression.valueOf("(2*4.0)!").numeric().toString())

        val me = JsclMathEngine.getInstance()
        val angleUnits = me.angleUnits
        try {
            me.setAngleUnits(AngleUnit.rad)
            assertEquals("-0.905578362006624", Expression.valueOf("sin(4!)").numeric().toString())
        } finally {
            me.setAngleUnits(angleUnits)
        }
        assertEquals("1", Expression.valueOf("(3.14/3.14)!").numeric().toString())
        assertEquals("1", Expression.valueOf("2/2!").numeric().toString())
        try {
            assertEquals("3.141592653589793!", Expression.valueOf("3.141592653589793!").numeric().toString())
            fail()
        } catch (e: NotIntegerException) {
            // Expected
        }
        assertEquals("0.523598775598299", Expression.valueOf("3.141592653589793/3!").numeric().toString())
        try {
            assertEquals("3.141592653589793/3.141592653589793!", Expression.valueOf("3.141592653589793/3.141592653589793!").numeric().toString())
            fail()
        } catch (e: ArithmeticException) {
            // Expected
        }
        try {
            assertEquals("7.2!", Expression.valueOf("7.2!").numeric().toString())
            fail()
        } catch (e: NotIntegerException) {
            // Expected
        }

        try {
            assertEquals("ln(7.2!)", Expression.valueOf("ln(7.2!)").numeric().toString())
            fail()
        } catch (e: NotIntegerException) {
            // Expected
        }

        assertEquals("ln(7.2!)", Expression.valueOf("ln(7.2!)").simplify().toString())

        assertEquals("36", Expression.valueOf("3!^2").numeric().toString())
        assertEquals("1", Expression.valueOf("(π/π)!").numeric().toString())
        assertEquals("720", Expression.valueOf("(3!)!").numeric().toString())
        assertEquals("36", Expression.valueOf("3!*3!").numeric().toString())

        assertEquals("100", Expression.valueOf("0.1E3").numeric().toString())

        val defaultAngleUnits = me.angleUnits
        try {
            me.setAngleUnits(AngleUnit.rad)
            assertEquals("0.017453292519943", Expression.valueOf("1°").numeric().toString())
            assertEquals("0.034906585039887", Expression.valueOf("2°").numeric().toString())
            assertEquals("0.05235987755983", Expression.valueOf("3°").numeric().toString())
            assertEquals("0.261799387799149", Expression.valueOf("3°*5").numeric().toString())
            assertEquals("0.00274155677808", Expression.valueOf("3°^2").numeric().toString())
            assertEquals("0.010966227112322", Expression.valueOf("3!°^2").numeric().toString())
            assertEquals("0.00091385225936", Expression.valueOf("3°°").numeric().toString())
            assertEquals("0.087266462599716", Expression.valueOf("5°").numeric().toString())
            assertEquals("2.05235987755983", Expression.valueOf("2+3°").numeric().toString())
        } finally {
            me.setAngleUnits(defaultAngleUnits)
        }

        try {
            me.setAngleUnits(AngleUnit.deg)
            assertEquals("1", Expression.valueOf("1°").numeric().toString())
            assertEquals("2", Expression.valueOf("2°").numeric().toString())
            assertEquals("3", Expression.valueOf("3°").numeric().toString())
            assertEquals("15", Expression.valueOf("3°*5").numeric().toString())
            assertEquals("9", Expression.valueOf("3°^2").numeric().toString())
            assertEquals("36", Expression.valueOf("3!°^2").numeric().toString())
            assertEquals("3", Expression.valueOf("3°°").numeric().toString())
            assertEquals("5", Expression.valueOf("5°").numeric().toString())
            assertEquals("5", Expression.valueOf("2+3°").numeric().toString())
        } finally {
            me.setAngleUnits(defaultAngleUnits)
        }

        assertEquals("6", Expression.valueOf("2*∂(3*x,x)").expand().toString())
        assertEquals("3", Expression.valueOf("∂(3*x,x)").expand().toString())
        assertEquals("12", Expression.valueOf("∂(x^3,x,2)").expand().toString())
        assertEquals("3*a", Expression.valueOf("∂(3*x*a,x)").expand().toString())
        assertEquals("0", Expression.valueOf("∂(3*x*a,x,0.011,2)").expand().toString())
        assertEquals("0", Expression.valueOf("2*∂(3*x*a,x,0.011,2)").expand().toString())
        assertEquals("ln(8)+lg(8)*ln(8)", Expression.valueOf("ln(8)*lg(8)+ln(8)").expand().toString())
        assertEquals("3.957364376505986", Expression.valueOf("ln(8)*lg(8)+ln(8)").numeric().toString())

        assertEquals("4!", Expression.valueOf("4.0!").simplify().toString())
        assertEquals("4°", Expression.valueOf("4.0°").simplify().toString())
        assertEquals("30°", Expression.valueOf("30°").simplify().toString())

        assertEquals("1", Expression.valueOf("abs(1)").numeric().toString())
        assertEquals("0", Expression.valueOf("abs(0)").numeric().toString())
        assertEquals("0", Expression.valueOf("abs(-0)").numeric().toString())
        assertEquals("1", Expression.valueOf("abs(-1)").numeric().toString())
        assertEquals("∞", Expression.valueOf("abs(-∞)").numeric().toString())

        assertEquals("1", Expression.valueOf("abs(i)").numeric().toString())
        assertEquals("0", Expression.valueOf("abs(0+0*i)").numeric().toString())
        assertEquals("1", Expression.valueOf("abs(-i)").numeric().toString())
        assertEquals("2.23606797749979", Expression.valueOf("abs(2-i)").numeric().toString())
        assertEquals("2.23606797749979", Expression.valueOf("abs(2+i)").numeric().toString())
        assertEquals("2.82842712474619", Expression.valueOf("abs(2+2*i)").numeric().toString())
        assertEquals("2.82842712474619", Expression.valueOf("abs(2-2*i)").numeric().toString())

        try {
            val k = ExtendedConstant.Builder(Constant("k"), 2.8284271247461903)
            me.getConstantsRegistry().addOrUpdate(k.create())
            assertEquals("k", Expression.valueOf("k").numeric().toString())
            assertEquals("k", Expression.valueOf("k").simplify().toString())
            assertEquals("k", Expression.valueOf("k").simplify().toString())
            assertEquals("k^3", Expression.valueOf("k*k*k").simplify().toString())
            assertEquals("22.62741699796953", Expression.valueOf("k*k*k").numeric().toString())
        } finally {
            val k = ExtendedConstant.Builder(Constant("k"), null as String?)
            me.getConstantsRegistry().addOrUpdate(k.create())
        }

        try {
            val k_1 = ExtendedConstant.Builder(Constant("k_1"), 3.0)
            me.getConstantsRegistry().addOrUpdate(k_1.create())
            assertEquals("k_1", Expression.valueOf("k_1").numeric().toString())
            assertEquals("k_1", Expression.valueOf("k_1[0]").numeric().toString())
            assertEquals("k_1", Expression.valueOf("k_1[2]").numeric().toString())
        } finally {
            val k_1 = ExtendedConstant.Builder(Constant("k_1"), null as String?)
            me.getConstantsRegistry().addOrUpdate(k_1.create())
        }

        var expression = me.simplifyGeneric("cos(t)+∂(cos(t),t)")
        var substituted = expression.substitute(Constant("t"), Expression.valueOf(100.0))
        assertEquals("-1.158455930679138", substituted.numeric().toString())

        expression = me.simplifyGeneric("abs(t)^2+2!")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("102", substituted.numeric().toString())

        expression = me.simplifyGeneric("abs(t)^2+10%")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("110", substituted.numeric().toString())

        expression = me.simplifyGeneric("abs(t)^2-10%")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("90", substituted.numeric().toString())

        expression = me.simplifyGeneric("(abs(t)^2)*10%")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("10", substituted.numeric().toString())

        expression = me.simplifyGeneric("(abs(t)^2)/10%")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("1000", substituted.numeric().toString())

        expression = me.simplifyGeneric("abs(t)^2+t%")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("110", substituted.numeric().toString())

        expression = me.simplifyGeneric("abs(t)^2-t%")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("90", substituted.numeric().toString())

        expression = me.simplifyGeneric("(abs(t)^2)*t%")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("10", substituted.numeric().toString())

        expression = me.simplifyGeneric("(abs(t)^2)/t%")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("1000", substituted.numeric().toString())

        expression = me.simplifyGeneric("Σ(t, t, 0, 10)")
        assertEquals("55", expression.numeric().toString())

        expression = me.simplifyGeneric("Σ(t, t, 0, 10)")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("55", substituted.numeric().toString())

        expression = me.simplifyGeneric("10*Σ(t, t, 0, 10)")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("550", substituted.numeric().toString())

        expression = me.simplifyGeneric("t*Σ(t, t, 0, 10)")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("550", substituted.numeric().toString())

        expression = me.simplifyGeneric("t*Σ(t+100%, t, 0, 10)")
        substituted = expression.substitute(Constant("t"), Expression.valueOf(10.0))
        assertEquals("1100", substituted.numeric().toString())

        assertEquals("i*t", Expression.valueOf("i*t").expand().simplify().toString())
        assertEquals("t", Expression.valueOf("t").simplify().toString())
        assertEquals("t^3", Expression.valueOf("t*t*t").simplify().toString())

        try {
            Expression.valueOf("t").numeric()
            fail()
        } catch (e: ArithmeticException) {
            // Expected
        }

        val t = ExtendedConstant.Builder(Constant("t"), null as String?)
        me.getConstantsRegistry().addOrUpdate(t.create())
        try {
            Expression.valueOf("t").numeric()
            fail()
        } catch (e: ArithmeticException) {
            // Expected
        }

        assertEquals("1/√(1+t)", Expression.valueOf("1/√(1+t)").simplify().toString())

        assertEquals("t", Expression.valueOf("t").simplify().toString())
        assertEquals("t^3", Expression.valueOf("t*t*t").simplify().toString())

        try {
            me.setAngleUnits(AngleUnit.rad)
            assertEquals("0.693147180559945+Π*i", Expression.valueOf("ln(-2)").numeric().toString())
        } finally {
            me.setAngleUnits(AngleUnit.deg)
        }
        assertEquals("-1/(57/2)", Expression.valueOf("1/(-57/2)").simplify().toString())
        assertEquals("sin(30)", Expression.valueOf("sin(30)").expand().toString())
        assertEquals("sin(n)", Expression.valueOf("sin(n)").expand().toString())
        assertEquals("sin(n!)", Expression.valueOf("sin(n!)").expand().toString())
        assertEquals("sin(n°)", Expression.valueOf("sin(n°)").expand().toString())
        assertEquals("sin(30°)", Expression.valueOf("sin(30°)").expand().toString())
        assertEquals("0.5", Expression.valueOf("sin(30°)").expand().numeric().toString())
        assertEquals("sin(2!)", Expression.valueOf("sin(2!)").expand().toString())

        assertEquals("12", Expression.valueOf("3*(3+1)").expand().toString())
        assertEquals("114.5915590261647", Expression.valueOf("deg(2)").numeric().toString())
        try {
            assertEquals("-0.1425465430742778", Expression.valueOf("∏(tan(3))").numeric().toString())
            fail()
        } catch (e: ParseException) {
            // Expected
        }
        try {
            assertEquals("-0.14255", Expression.valueOf("sin(2,2)").expand().numeric().toString())
            fail()
        } catch (e: ParseException) {
            // Expected
        }
        try {
            assertEquals("114.59155902616465", Expression.valueOf("deg(2,2)").numeric().toString())
            fail()
        } catch (e: ParseException) {
            // Expected
        }

        assertEquals("0.5", Expression.valueOf("sin(30°)").numeric().toString())
        assertEquals("π", Expression.valueOf("√(π)^2").simplify().toString())
        assertEquals("π", Expression.valueOf("√(π^2)").simplify().toString())
        assertEquals("π^2", Expression.valueOf("√(π^2*π^2)").simplify().toString())
        assertEquals("π^3", Expression.valueOf("√(π^4*π^2)").simplify().toString())
        assertEquals("e*π^2", Expression.valueOf("√(π^4*e^2)").simplify().toString())

        assertEquals("1", Expression.valueOf("(π/π)!").numeric().toString())

        // in deg mode π=180 and factorial of 180 is calculating
        assertEquals("0", Expression.valueOf("Π/Π!").numeric().toString())

        assertEquals("0", Expression.valueOf("exp((Π*i))+1").numeric().toString())
        assertEquals("20*x^3", Expression.valueOf("∂(5*x^4, x)").expand().simplify().toString())
        assertEquals("25*x", Expression.valueOf("5*x*5").expand().simplify().toString())
        assertEquals("20*x", Expression.valueOf("5*x*4").expand().simplify().toString())

        try {
            me.evaluate("0b:π")
            fail()
        } catch (e: ParseException) {
            // ok
        }

        try {
            me.evaluate("0b:10π")
            fail()
        } catch (e: ParseException) {
            // ok
        }

        try {
            me.setNumeralBase(NumeralBase.hex)

            assertEquals("0.EEEEEEEEEEEEEC88", me.evaluate("0x:E/0x:F"))
            assertEquals("E/F", me.simplify("0x:E/0x:F"))

            assertEquals("0.EEEEEEEEEEEEEC88", me.evaluate("E/F"))
            assertEquals("E/F", me.simplify("E/F"))
        } finally {
            me.setNumeralBase(NumeralBase.dec)
        }

        try {
            me.setAngleUnits(AngleUnit.rad)
            assertEquals("-1.570796326794897+2.993222846126381*i", me.evaluate("asin(-10)"))
            assertEquals("-1.570796326794897+1.316957896924817*i", me.evaluate("asin(-2)"))
            assertEquals("-1.570796326794897", me.evaluate("asin(-1)"))
            assertEquals("0", me.evaluate("asin(0)"))
            assertEquals("1.570796326794897", me.evaluate("asin(1)"))
            assertEquals("1.570796326794897-1.316957896924817*i", me.evaluate("asin(2)"))
            assertEquals("1.570796326794897-2.993222846126381*i", me.evaluate("asin(10)"))

            assertEquals("Π-2.993222846126379*i", me.evaluate("acos(-10)"))
            assertEquals("Π-1.316957896924816*i", me.evaluate("acos(-2)"))
            assertEquals("Π", me.evaluate("acos(-1)"))
            assertEquals("1.570796326794897", me.evaluate("acos(0)"))
            assertEquals("0", me.evaluate("acos(1)"))
            assertEquals("1.316957896924816*i", me.evaluate("acos(2)"))
            assertEquals("2.993222846126379*i", me.evaluate("acos(10)"))

            assertEquals("-1.471127674303735", me.evaluate("atan(-10)"))
            assertEquals("-1.10714871779409", me.evaluate("atan(-2)"))
            assertEquals("-0.785398163397448", me.evaluate("atan(-1)"))
            assertEquals("0", me.evaluate("atan(0)"))
            assertEquals("0.785398163397448", me.evaluate("atan(1)"))
            assertEquals("1.10714871779409", me.evaluate("atan(2)"))
            assertEquals("1.471127674303735", me.evaluate("atan(10)"))

            for (i in -10 until 10) {
                assertEquals(me.evaluate("3.14159265358979323846/2 - atan($i)"), me.evaluate("acot($i)"))
            }

            assertEquals("3.041924001098631", me.evaluate("3.14159265358979323846/2 - atan(-10)"))
            assertEquals("3.041924001098631", me.evaluate("acot(-10)"))
            assertEquals("1.570796326794897", me.evaluate("acot(0)"))
            assertEquals("2.677945044588987", me.evaluate("acot(-2)"))
            assertEquals("2.356194490192345", me.evaluate("acot(-1)"))
            assertEquals("0.785398163397448", me.evaluate("acot(1)"))
            assertEquals("0.463647609000806", me.evaluate("acot(2)"))
            assertEquals("0.099668652491162", me.evaluate("acot(10)"))

            assertEquals("Π", me.evaluate("π"))
            assertEquals("Π", me.evaluate("3.14159265358979323846"))
        } finally {
            me.setAngleUnits(AngleUnit.deg)
        }

        assertEquals("180", me.evaluate("Π"))
        assertEquals("180", me.evaluate("200-10%"))

        assertEquals("∞", me.evaluate("1/0"))
        assertEquals("-∞", me.evaluate("-1/0"))
        assertEquals("-∞", me.evaluate("-1/0"))
        assertEquals("∞", me.evaluate("(1 + 2) / (5 - 3 - 2)"))
        assertEquals("∞", me.evaluate("(1 + 2) / (5.1 - 3.1 - 2.0 )"))
        assertEquals("∞", me.evaluate("1/0"))
    }

    @Test
    fun testAngleUnits() {
        val mathEngine = JsclMathEngine.getInstance()

        val defaultAngleUnits = mathEngine.angleUnits

        for (angleUnits in AngleUnit.values()) {
            try {
                mathEngine.setAngleUnits(angleUnits)
                mathEngine.evaluate("sin(2)")
                mathEngine.evaluate("asin(2)")
            } finally {
                mathEngine.setAngleUnits(defaultAngleUnits)
            }
        }

        try {
            mathEngine.setAngleUnits(AngleUnit.rad)
            assertEquals("Π", mathEngine.evaluate("π"))
            assertEquals("π/2", mathEngine.simplify("π/2"))
            assertEquals(mathEngine.evaluate("0.9092974268256816953960198659117448427022549714478902683789"), mathEngine.evaluate("sin(2)"))
            assertEquals(mathEngine.evaluate("0.1411200080598672221007448028081102798469332642522655841518"), mathEngine.evaluate("sin(3)"))
            assertEquals(mathEngine.evaluate("0"), mathEngine.evaluate("sin(0)"))

            assertEquals(mathEngine.evaluate("1"), mathEngine.evaluate("cos(0)"))
            assertEquals(mathEngine.evaluate("0.8623188722876839341019385139508425355100840085355108292801"), mathEngine.evaluate("cos(100)"))
            assertEquals(mathEngine.evaluate("-0.416146836547142386997568229500762189766000771075544890755"), mathEngine.evaluate("cos(2)"))

            assertEquals(mathEngine.evaluate("-2.185039863261518991643306102313682543432017746227663164562"), mathEngine.evaluate("tan(2)"))
            assertEquals(mathEngine.evaluate("-0.142546543074277805295635410533913493226092284901804647633"), mathEngine.evaluate("tan(3)"))
            assertEquals(mathEngine.evaluate("0.6483608274590872"), mathEngine.evaluate("tan(10)"))

            assertEquals(mathEngine.evaluate("0.6420926159343306"), mathEngine.evaluate("cot(1)"))
            assertEquals(mathEngine.evaluate("-0.457657554360285763750277410432047276428486329231674329641"), mathEngine.evaluate("cot(2)"))
            assertEquals(mathEngine.evaluate("-7.015252551434533469428551379526476578293103352096353838156"), mathEngine.evaluate("cot(3)"))
        } finally {
            mathEngine.setAngleUnits(defaultAngleUnits)
        }

        try {
            mathEngine.setAngleUnits(AngleUnit.deg)
            assertEquals(mathEngine.evaluate("0.9092974268256816953960198659117448427022549714478902683789"), mathEngine.evaluate("sin(deg(2))"))
            assertEquals(mathEngine.evaluate("0.1411200080598672221007448028081102798469332642522655841518"), mathEngine.evaluate("sin(deg(3))"))
            assertEquals(mathEngine.evaluate("0"), mathEngine.evaluate("sin(deg(0))"))

            assertEquals(mathEngine.evaluate("1"), mathEngine.evaluate("cos(deg(0))"))
            assertEquals(mathEngine.evaluate("0.8623188722876839341019385139508425355100840085355108292801"), mathEngine.evaluate("cos(deg(100))"))
            assertEquals(mathEngine.evaluate("-0.416146836547142386997568229500762189766000771075544890755"), mathEngine.evaluate("cos(deg(2))"))

            assertEquals(mathEngine.evaluate("-2.185039863261518991643306102313682543432017746227663164562"), mathEngine.evaluate("tan(deg(2))"))
            assertEquals(mathEngine.evaluate("-0.142546543074277805295635410533913493226092284901804647633"), mathEngine.evaluate("tan(deg(3))"))
            assertEquals(mathEngine.evaluate("0.6483608274590872"), mathEngine.evaluate("tan(deg(10))"))

            assertEquals(mathEngine.evaluate("0.6420926159343306"), mathEngine.evaluate("cot(deg(1))"))
            assertEquals(mathEngine.evaluate("-0.457657554360285763750277410432047276428486329231674329641"), mathEngine.evaluate("cot(deg(2))"))
            assertEquals(mathEngine.evaluate("-7.015252551434533469428551379526476578293103352096353838156"), mathEngine.evaluate("cot(deg(3))"))
        } finally {
            mathEngine.setAngleUnits(defaultAngleUnits)
        }

        try {
            mathEngine.setAngleUnits(AngleUnit.rad)
            assertEquals(mathEngine.evaluate("-0.5235987755982989"), mathEngine.evaluate("asin(-0.5)"))
            assertEquals(mathEngine.evaluate("-0.47349551215005636"), mathEngine.evaluate("asin(-0.456)"))
            assertEquals(mathEngine.evaluate("0.32784124364198347"), mathEngine.evaluate("asin(0.322)"))

            assertEquals(mathEngine.evaluate("1.2429550831529133"), mathEngine.evaluate("acos(0.322)"))
            assertEquals(mathEngine.evaluate("1.5587960387762325"), mathEngine.evaluate("acos(0.012)"))
            assertEquals(mathEngine.evaluate("1.6709637479564563"), mathEngine.evaluate("acos(-0.1)"))

            assertEquals(mathEngine.evaluate("0.3805063771123649"), mathEngine.evaluate("atan(0.4)"))
            assertEquals(mathEngine.evaluate("0.09966865249116204"), mathEngine.evaluate("atan(0.1)"))
            assertEquals(mathEngine.evaluate("-0.5404195002705842"), mathEngine.evaluate("atan(-0.6)"))

            assertEquals(mathEngine.evaluate("1.0603080048781206"), mathEngine.evaluate("acot(0.56)"))
            // todo serso: wolfram alpha returns -0.790423 instead of 2.3511694068615325 (-PI)
            assertEquals(mathEngine.evaluate("2.3511694068615325"), mathEngine.evaluate("acot(-0.99)"))
            // todo serso: wolfram alpha returns -1.373401 instead of 1.7681918866447774 (-PI)
            assertEquals(mathEngine.evaluate("1.7681918866447774"), mathEngine.evaluate("acot(-0.2)"))
        } finally {
            mathEngine.setAngleUnits(defaultAngleUnits)
        }

        try {
            mathEngine.setAngleUnits(AngleUnit.deg)
            assertEquals(mathEngine.evaluate("deg(-0.5235987755982989)"), mathEngine.evaluate("asin(-0.5)"))
            assertEquals(mathEngine.evaluate("-27.129294464583623"), mathEngine.evaluate("asin(-0.456)"))
            assertEquals(mathEngine.evaluate("18.783919611005786"), mathEngine.evaluate("asin(0.322)"))

            assertEquals(mathEngine.evaluate("71.21608038899423"), mathEngine.evaluate("acos(0.322)"))
            assertEquals(mathEngine.evaluate("89.31243414358914"), mathEngine.evaluate("acos(0.012)"))
            assertEquals(mathEngine.evaluate("95.73917047726678"), mathEngine.evaluate("acos(-0.1)"))

            assertEquals(mathEngine.evaluate("deg(0.3805063771123649)"), mathEngine.evaluate("atan(0.4)"))
            assertEquals(mathEngine.evaluate("deg(0.09966865249116204)"), mathEngine.evaluate("atan(0.1)"))
            assertEquals(mathEngine.evaluate("deg(-0.5404195002705842)"), mathEngine.evaluate("atan(-0.6)"))

            assertEquals(mathEngine.evaluate("deg(1.0603080048781206)"), mathEngine.evaluate("acot(0.56)"))
            // todo serso: wolfram alpha returns -0.790423 instead of 2.3511694068615325 (-PI)
            assertEquals(mathEngine.evaluate("134.7120839334429"), mathEngine.evaluate("acot(-0.99)"))
            // todo serso: wolfram alpha returns -1.373401 instead of 1.7681918866447774 (-PI)
            assertEquals(mathEngine.evaluate("deg(1.7681918866447774)"), mathEngine.evaluate("acot(-0.2)"))
        } finally {
            mathEngine.setAngleUnits(defaultAngleUnits)
        }

        try {
            mathEngine.setAngleUnits(AngleUnit.deg)
            assertEquals(mathEngine.evaluate("0.0348994967025009716459951816253329373548245760432968714250"), mathEngine.evaluate("(sin(2))"))
            assertEquals(mathEngine.evaluate("0.0523359562429438327221186296090784187310182539401649204835"), mathEngine.evaluate("(sin(3))"))
            assertEquals(mathEngine.evaluate("0"), mathEngine.evaluate("sin(0)"))

            assertEquals(mathEngine.evaluate("1"), mathEngine.evaluate("cos(0)"))
            assertEquals(mathEngine.evaluate("-0.1736481776669303"), mathEngine.evaluate("(cos(100))"))
            assertEquals(mathEngine.evaluate("0.9993908270190958"), mathEngine.evaluate("(cos(2))"))

            assertEquals(mathEngine.evaluate("0.03492076949174773"), mathEngine.evaluate("(tan(2))"))
            assertEquals(mathEngine.evaluate("0.05240777928304121"), mathEngine.evaluate("(tan(3))"))
            assertEquals(mathEngine.evaluate("0.17632698070846498"), mathEngine.evaluate("(tan(10))"))

            assertEquals(mathEngine.evaluate("57.28996163075943"), mathEngine.evaluate("(cot(1))"))
            assertEquals(mathEngine.evaluate("28.636253282915604"), mathEngine.evaluate("(cot(2))"))
            assertEquals(mathEngine.evaluate("19.081136687728208"), mathEngine.evaluate("(cot(3))"))
        } finally {
            mathEngine.setAngleUnits(defaultAngleUnits)
        }

        try {
            mathEngine.setAngleUnits(AngleUnit.rad)
            testSinEqualsToSinh(mathEngine, 0.0)
            testSinEqualsToSinh(mathEngine, 1.0, "0.841470984807897")
            testSinEqualsToSinh(mathEngine, 3.0, "0.141120008059867")
            testSinEqualsToSinh(mathEngine, 6.0)
            testSinEqualsToSinh(mathEngine, -1.0, "-0.841470984807897")
            testSinEqualsToSinh(mathEngine, -3.3, "0.157745694143248")
            testSinEqualsToSinh(mathEngine, -232.2, "0.274294863736896")
        } finally {
            mathEngine.setAngleUnits(defaultAngleUnits)
        }

        try {
            mathEngine.setAngleUnits(AngleUnit.deg)
            testSinEqualsToSinh(mathEngine, 0.0)
            testSinEqualsToSinh(mathEngine, 1.0, "0.017452406437284")
            testSinEqualsToSinh(mathEngine, 3.0, "0.052335956242944")
            testSinEqualsToSinh(mathEngine, 6.0, "0.104528463267653")
            testSinEqualsToSinh(mathEngine, -1.0, "-0.017452406437284")
            testSinEqualsToSinh(mathEngine, -3.3, "-0.057564026959567")
            testSinEqualsToSinh(mathEngine, -232.2, "0.79015501237569")
            assertEquals("Π/2", mathEngine.simplify("Π/2"))
        } finally {
            mathEngine.setAngleUnits(defaultAngleUnits)
        }

        try {
            mathEngine.setAngleUnits(AngleUnit.rad)
            assertEquals(mathEngine.evaluate("1.5707963267948966-0.8813735870195429*i"), mathEngine.evaluate("acos(i)"))
            assertEquals(mathEngine.evaluate("0.9045568943023814-1.0612750619050357*i"), mathEngine.evaluate("acos(1+i)"))
            assertEquals(mathEngine.evaluate("0.9999999999999999-0.9999999999999998*i"), mathEngine.evaluate("cos(acos(1-i))"))
            assertEquals(mathEngine.evaluate("-0.9045568943023814-1.0612750619050355*i"), mathEngine.evaluate("-acos(1-i)"))
        } finally {
            mathEngine.setAngleUnits(defaultAngleUnits)
        }
    }

    private fun testSinEqualsToSinh(mathEngine: MathEngine, x: Double, expected: String? = null) {
        if (expected == null) {
            assertEquals(mathEngine.simplify(mathEngine.evaluate("sinh(i*$x)/i")), mathEngine.evaluate("sin($x)"))
        } else {
            assertEquals(expected, mathEngine.evaluate("sin($x)"))
            assertEquals(expected, mathEngine.evaluate("(exp(i * $x) - cos($x))/i"))
            assertEquals(expected, mathEngine.evaluate("(exp(i * $x) - cos($x))/i"))
        }
    }

    @Test
    fun testName() {
        Expression.valueOf("a*c+b*sin(c)").toString()
    }

    @Test
    fun testIntegrals() {
        assertEquals("50", Expression.valueOf("∫ab(x, x, 0, 10)").expand().numeric().toString())
        assertEquals("1/2*a^2", Expression.valueOf("∫ab(x, x, 0, a)").expand().toString())
        try {
            assertEquals("∫ab(x, x, 0)", Expression.valueOf("∫ab(x, x, 0)").expand().toString())
            fail()
        } catch (e: ParseException) {
            // Expected
        }
        try {
            assertEquals("∫ab(x, x)", Expression.valueOf("∫ab(x, x)").expand().simplify().toString())
            fail()
        } catch (e: ParseException) {
            // Expected
        }
        assertEquals("x^2/2", Expression.valueOf("∫(x, x)").expand().simplify().toString())
        try {
            assertEquals("x^2/2", Expression.valueOf("∫(x, x)").expand().numeric().toString())
            fail()
        } catch (e: ArithmeticException) {
            // Expected
        }

        assertEquals("x^2/2", Expression.valueOf("∫(x, x)").expand().simplify().toString())
        assertEquals("ln(x)", Expression.valueOf("∫(1/x, x)").expand().simplify().toString())
        try {
            JsclMathEngine.getInstance().setAngleUnits(AngleUnit.rad)
            assertEquals("2*ln(2)+ln(cosh(x))", Expression.valueOf("∫(tanh(x), x)").expand().simplify().toString())
            assertEquals("2*ln(2)+ln(sin(x))", Expression.valueOf("∫(cot(x), x)").expand().simplify().toString())
            assertEquals("-2*ln(2)-ln(cos(x))", Expression.valueOf("∫(tan(x), x)").expand().simplify().toString())
        } finally {
            JsclMathEngine.getInstance().setAngleUnits(AngleUnit.deg)
        }
    }

    @Test
    fun testDerivations() {
        val defaultAngleUnits = JsclMathEngine.getInstance().angleUnits
        try {
            JsclMathEngine.getInstance().setAngleUnits(AngleUnit.rad)
            assertEquals("-0.909297426825682", Expression.valueOf("∂(cos(t),t,2)").numeric().toString())
            assertEquals("∂(cos(t), t, 2, 1)", Expression.valueOf("∂(cos(t),t,2)").simplify().toString())
            assertEquals("-2.234741690198506", Expression.valueOf("∂(t*cos(t),t,2)").numeric().toString())
            assertEquals("-4.469483380397012", Expression.valueOf("2*∂(t*cos(t),t,2)").numeric().toString())
            assertEquals("-sin(2)", Expression.valueOf("∂(cos(t),t,2)").expand().toString())
            assertEquals("-sin(t)", Expression.valueOf("∂(cos(t),t)").expand().toString())
            assertEquals("-sin(t)", Expression.valueOf("∂(cos(t),t,t,1)").expand().simplify().toString())
            assertEquals("∂(cos(t), t, t, 1°)", Expression.valueOf("∂(cos(t),t,t,1°)").expand().simplify().toString())
        } finally {
            JsclMathEngine.getInstance().setAngleUnits(defaultAngleUnits)
        }

        assertEquals("∂(cos(t), t, t, 1°)", Expression.valueOf("∂(cos(t),t,t,1°)").expand().numeric().toString())
    }

    @Test
    fun testSum() {
        assertEquals("3", Expression.valueOf("Σ(n,n,1,2)").expand().toString())
        assertEquals("200", Expression.valueOf("Σ(n/n,n,1,200)").expand().toString())
        assertEquals("1/3", Expression.valueOf("Σ((n-1)/(n+1),n,1,2)").expand().toString())
        assertEquals("sin(1)", Expression.valueOf("Σ(sin(n),n,1,1)").expand().toString())
        assertEquals("1/1!", Expression.valueOf("Σ(n/n!,n,1,1)").expand().toString())
        assertEquals("2", Expression.valueOf("Σ(n/n!,n,1,2)").expand().numeric().toString())
        assertEquals("2.718281828459046", Expression.valueOf("Σ(n/n!,n,1,200)").expand().numeric().toString())
        assertEquals("2.718281828459046", Expression.valueOf("Σ(n/(2*n/2)!,n,1,200)").expand().numeric().toString())
        assertEquals(Expression.valueOf("3").numeric().toString(), Expression.valueOf("Σ(n°,n,1,2)").expand().numeric().toString())
        assertEquals("200", Expression.valueOf("Σ(n°/n°,n,1,200)").expand().numeric().toString())
        assertEquals("-sin(1)-sin(2)", Expression.valueOf("Σ(∂(cos(t),t,n),n,1,2)").expand().toString())
        assertEquals("-0.052351903139784", Expression.valueOf("Σ(∂(cos(t),t,n),n,1,2)").expand().numeric().toString())
    }

    @Test
    fun testNumeralBases() {
        val me = JsclMathEngine.getInstance()
        try {
            assertEquals("10", me.evaluate("0b:01010"))
            assertEquals("10", me.evaluate("0b:1010"))
            assertEquals("520", me.evaluate("0o:1010"))
            assertEquals("1010", me.evaluate("1010"))
            assertEquals("1010.1", me.evaluate("1010.1"))
        } finally {
            // No cleanup needed
        }

        try {
            me.setNumeralBase(NumeralBase.hex)
            assertEquals("22F", me.evaluate("22F*exp(F)/exp(F)"))
            assertEquals("E", me.evaluate("E"))
        } finally {
            me.setNumeralBase(NumeralBase.dec)
        }
    }

    @Test
    fun testFormat() {
        val me = JsclMathEngine.getInstance()
        try {
            me.setGroupingSeparator(' ')
            assertEquals("123 456.7891011", Expression.valueOf("123456.7891011").numeric().toString())
            assertEquals("123 456.7891011", Expression.valueOf("123456.7891011").simplify().toString())
            assertEquals("123 456.7891011123", Expression.valueOf("123456.7891011123123123123123").simplify().toString())
            assertEquals("0.000001222", Expression.valueOf("1222/(10^9)").numeric().toString())
            assertEquals("12 345", JsclInteger.valueOf(12345L).toString())

            me.setNotation(Real.NumberFormat.FSE_SCI)
            assertEquals("0", Expression.valueOf("0.0").simplify().toString())
            assertEquals("1", Expression.valueOf("1.0").simplify().toString())
            assertEquals("100", Expression.valueOf("100.0").simplify().toString())

            me.setNotation(Real.NumberFormat.FSE_NONE)
            me.setPrecision(5)
            assertEquals("0", Expression.valueOf("1222/(10^9)").numeric().toString())

            me.setNotation(Real.NumberFormat.FSE_SCI)
            me.setPrecision(5)
            assertEquals("1.222E-6", Expression.valueOf("1222/(10^9)").numeric().toString())

            me.setPrecision(10)
            assertEquals("1.222E-6", Expression.valueOf("1222/(10^9)").numeric().toString())

            me.setPrecision(NumberFormatter.MAX_PRECISION)
            assertEquals("1.222E-6", Expression.valueOf("1222/(10^9)").numeric().toString())

            me.setNotation(Real.NumberFormat.FSE_NONE)
            assertEquals("0.333333333333333", Expression.valueOf("1/3").numeric().toString())

            me.setNotation(Real.NumberFormat.FSE_SCI)
            assertEquals("0.333333333333333", Expression.valueOf("1/3").numeric().toString())

            me.setPrecision(10)
            assertEquals("0.3333333333", Expression.valueOf("1/3").numeric().toString())

            me.setNotation(Real.NumberFormat.FSE_NONE)
            me.setPrecision(10)
            assertEquals("0.3333333333", Expression.valueOf("1/3").numeric().toString())
        } finally {
            me.setGroupingSeparator(NumberFormatter.NO_GROUPING)
            me.setNotation(Real.NumberFormat.FSE_NONE)
            me.setPrecision(NumberFormatter.MAX_PRECISION)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Result: " + getWolframAlphaResult("APP_ID", "-24.37581129610191-((2699.798527427213-4032.781981216783)*√(4657.120529143301)/6202.47137988087-ln(4435.662292261872)*sin(5134.044125137488)-sin(5150.617980207194)+sin(1416.6029070906816))"))
        }

        fun getWolframAlphaResult(appId: String, expression: String): String? {
            var result: String? = null

            val wolframAlphaUrl: URL
            try {
                wolframAlphaUrl = URL("http://api.wolframalpha.com/v2/query?input=$expression&appid=$appId&format=plaintext&podtitle=Decimal+approximation")

                val connection = wolframAlphaUrl.openConnection()
                var `in`: BufferedReader? = null
                try {
                    `in` = BufferedReader(InputStreamReader(connection.getInputStream()))

                    var line: String?
                    while (`in`.readLine().also { line = it } != null) {
                        println(line)
                        if (line!!.contains("<plaintext>")) {
                            result = line!!.replace("<plaintext>", "").replace("</plaintext>", "").replace("...", "").trim()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    `in`?.close()
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return result
        }
    }
}
