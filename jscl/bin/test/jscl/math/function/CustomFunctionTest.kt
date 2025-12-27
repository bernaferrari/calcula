package jscl.math.function

import jscl.AngleUnit
import jscl.CustomFunctionCalculationException
import jscl.JsclMathEngine
import jscl.NumeralBase
import jscl.math.Expression
import jscl.text.ParseException
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/**
 * User: serso
 * Date: 11/15/11
 * Time: 5:35 PM
 */
class CustomFunctionTest {

    @Test
    fun testLog() {
        val mathEngine = JsclMathEngine.getInstance()

        assertEquals("∞", Expression.valueOf("1/0").numeric().toString())
        assertEquals("∞", Expression.valueOf("ln(10)/ln(1)").numeric().toString())

        // logarithm
        val jBuilder = CustomFunction.Builder(true, "log", listOf("a", "b"), "ln(b)/ln(a)")
        val function = mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder.create())
        assertEquals("log(a, b)", function.toString())
        assertEquals("ln(b)/ln(a)", (mathEngine.getFunctionsRegistry().get("log") as CustomFunction).getContent())
        assertEquals("∞", Expression.valueOf("log(1, 10)").numeric().toString())
        assertEquals("3.321928094887363", Expression.valueOf("log(2, 10)").numeric().toString())
        assertEquals("1.430676558073393", Expression.valueOf("log(5, 10)").numeric().toString())
        assertEquals("0.960252567789128", Expression.valueOf("log(11, 10)").numeric().toString())
        assertEquals("1/b*1/ln(a)", Expression.valueOf("∂(log(a, b), b)").expand().toString())
        assertEquals("-ln(b)*1/a*(1/ln(a))^2", Expression.valueOf("∂(log(a, b), a)").expand().toString())
    }

    @Test
    fun testDerivative() {
        val mathEngine = JsclMathEngine.getInstance()

        val jBuilder = CustomFunction.Builder("t1", listOf("a"), "sin(a)")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder.create())
        assertEquals("1", Expression.valueOf("t1(90)").numeric().toString())
        assertEquals("cos(t)", Expression.valueOf("∂(t1(t), t)").expand().toString())
        assertEquals("0", Expression.valueOf("∂(t1(t), t2)").expand().toString())
        assertEquals("cos(a)", Expression.valueOf("∂(t1(a), a)").expand().toString())
        assertEquals("1", Expression.valueOf("∂(t1(a), t1(a))").expand().toString())
        val jBuilder1 = CustomFunction.Builder("t2", listOf("a", "b"), "b*sin(a)")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder1.create())
        assertEquals("y*cos(x)", Expression.valueOf("∂(t2(x, y), x)").expand().toString())
        assertEquals("sin(x)", Expression.valueOf("∂(t2(x, y), y)").expand().toString())
    }

    @Test
    fun testAntiDerivative() {
        val mathEngine = JsclMathEngine.getInstance()

        val jBuilder = CustomFunction.Builder("t1", listOf("a"), "sin(a)")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder.create())
        assertEquals("1", Expression.valueOf("t1(90)").numeric().toString())

        try {
            mathEngine.setAngleUnits(AngleUnit.rad)
            assertEquals("-cos(t)", Expression.valueOf("∫(t1(t), t)").expand().toString())
            assertEquals("t2*sin(t)", Expression.valueOf("∫(t1(t), t2)").expand().toString())
            assertEquals("-cos(a)", Expression.valueOf("∫(t1(a), a)").expand().toString())
            assertEquals("1/2*sin(a)^2", Expression.valueOf("∫(t1(a), t1(a))").expand().toString())
            val jBuilder1 = CustomFunction.Builder("t2", listOf("a", "b"), "b*sin(a)")
            mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder1.create())
            assertEquals("-y*cos(x)", Expression.valueOf("∫(t2(x, y), x)").expand().toString())
            assertEquals("1/2*y^2*sin(x)", Expression.valueOf("∫(t2(x, y), y)").expand().toString())
        } finally {
            mathEngine.setAngleUnits(AngleUnit.deg)
        }
    }

    @Test
    fun testFunction() {
        val mathEngine = JsclMathEngine.getInstance()

        val jBuilder = CustomFunction.Builder("testFunction", listOf("a", "b", "c", "d"), "b*cos(a)/c+d")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder.create())
        assertEquals("6.749543120264322", Expression.valueOf("testFunction(2, 3, 4, 6)").numeric().toString())
        assertEquals("7.749543120264322", Expression.valueOf("testFunction(2, 3, 4, 7)").numeric().toString())
        assertEquals("6.749543120264322", Expression.valueOf("testFunction(2*1, 3, 4, 6)").numeric().toString())
        assertEquals("6.749543120264322", Expression.valueOf("testFunction(2*1, 3, 4, 3!)").numeric().toString())
        assertEquals("6.749543120264322", Expression.valueOf("testFunction(2*1, 3, 2^2-1+e^0, 3!)").numeric().toString())
        assertEquals("testFunction(2, 3, 4, 3!)", Expression.valueOf("testFunction(2*1, 3, 2^2-1+e^0, 3!)").simplify().toString())
        assertEquals("3!+3*cos(2)/4", Expression.valueOf("testFunction(2*1, 3, 2^2-1+e^0, 3!)").expand().toString())
        assertEquals("3!+3*(1/2*exp(2*i)+1/2*1/exp(2*i))/4", Expression.valueOf("testFunction(2*1, 3, 2^2-1+e^0, 3!)").elementary().toString())
        assertEquals("sin(t)^2*testFunction(2, 3, 4, 3!)", Expression.valueOf("sin(t)*testFunction(2*1, 3, 2^2-1+e^0, 3!)*sin(t)").simplify().toString())
        assertEquals("testFunction(2, 3, 4, 3!)^2", Expression.valueOf("testFunction(2*1, 3, 2^2-1+e^0, 3!)*testFunction(2, 3, 4, 3!)").simplify().toString())
        try {
            Expression.valueOf("testFunction(2*1, 3, 2^2-1+e^0, 3!)*testFunction(2, 3, 4)")
            fail()
        } catch (e: ParseException) {
            // ok, not enough parameters
        }

        val a = ExtendedConstant.Builder(Constant("a"), 1000.0)
        mathEngine.getConstantsRegistry().addOrUpdate(a.create())
        val jBuilder1 = CustomFunction.Builder("testFunction2", listOf("a", "b", "c", "d"), "b*cos(a)/c+d")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder1.create())
        assertEquals("6.749543120264322", Expression.valueOf("testFunction2(2, 3, 4, 6)").numeric().toString())
        assertEquals("7.749543120264322", Expression.valueOf("testFunction2(2, 3, 4, 7)").numeric().toString())
        assertEquals("6.749543120264322", Expression.valueOf("testFunction2(2*1, 3, 4, 6)").numeric().toString())
        assertEquals("6.749543120264322", Expression.valueOf("testFunction2(2*1, 3, 2^2-1+e^0, 3!)").numeric().toString())

        val jBuilder2 = CustomFunction.Builder("testFunction3", listOf("a", "b", "c", "d"), "testFunction2(a, b, c, d) - testFunction(a, b, c, d)")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder2.create())
        assertEquals("0", Expression.valueOf("testFunction3(2, 3, 4, 6)").numeric().toString())
        assertEquals("0", Expression.valueOf("testFunction3(2, 3, 4, 7)").numeric().toString())
        assertEquals("0", Expression.valueOf("testFunction3(2*1, 3, 4, 6)").numeric().toString())
        assertEquals("0", Expression.valueOf("testFunction3(2*1, 3, 2^2-1+e^0, 3!)").numeric().toString())

        val jBuilder3 = CustomFunction.Builder("testFunction4", listOf("a", "b", "c", "d"), "testFunction2(a, b/2, c/3, d/4) - testFunction(a, b!, c, d)")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder3.create())
        assertEquals("-4.87477156013216", Expression.valueOf("testFunction4(2, 3, 4, 6)").numeric().toString())
        assertEquals("-5.62477156013216", Expression.valueOf("testFunction4(2, 3, 4, 7)").numeric().toString())
        assertEquals("-4.87477156013216", Expression.valueOf("testFunction4(2*1, 3, 4, 6)").numeric().toString())
        assertEquals("-4.87477156013216", Expression.valueOf("testFunction4(2*1, 3, 2^2-1+e^0, 3!)").numeric().toString())

        val jBuilder4 = CustomFunction.Builder("testFunction5", listOf("a", "b"), "testFunction2(a, b/2, 2, 1) - testFunction(a, b!, 4!, 1)")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder4.create())
        assertEquals("0.499695413509548", Expression.valueOf("testFunction5(2, 3)").numeric().toString())
        assertEquals("0.499695413509548", Expression.valueOf("testFunction5(2, 3)").numeric().toString())
        assertEquals("0.499695413509548", Expression.valueOf("testFunction5(2*1, 3)").numeric().toString())
        assertEquals("0", Expression.valueOf("testFunction5(2*1, 2^2-1+e^0)").numeric().toString())

        try {
            Expression.valueOf("testFunction5(2, 3.5)").numeric()
            fail()
        } catch (e: ArithmeticException) {
            // Expected
        }

        val jBuilder5 = CustomFunction.Builder("testFunction6", listOf("a", "b"), "testFunction(a, b!, 4!, Π)")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder5.create())
        assertEquals("180.2498477067548", Expression.valueOf("testFunction6(2, 3)").numeric().toString())

        val e = ExtendedConstant.Builder(Constant("e"), 181.0)
        mathEngine.getConstantsRegistry().addOrUpdate(e.create())
        val jBuilder6 = CustomFunction.Builder("testFunction7", listOf("a", "b"), "testFunction(a, b!, 4!, e)")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder6.create())
        assertEquals("181.2498477067548", Expression.valueOf("testFunction7(2, 3)").numeric().toString())

        val e1 = ExtendedConstant.Builder(Constant("e"), 181.0)
        mathEngine.getConstantsRegistry().addOrUpdate(e1.create())
        val jBuilder7 = CustomFunction.Builder("testFunction8", listOf("a", "b"), "testFunction(sin(a), b!, 4!, e)")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder7.create())
        assertEquals("181.249999953623", Expression.valueOf("testFunction8(2, 3)").numeric().toString())
    }

    @Test
    fun testFunction2() {
        val mathEngine = JsclMathEngine.getInstance()

        val jBuilder = CustomFunction.Builder("f", listOf("x", "y"), "z1/z2*√(x^2+y^2)")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder.create())
        val jBuilder1 = CustomFunction.Builder("f2", listOf("x", "y"), "√(x^2+y^2)")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder1.create())
        val jBuilder2 = CustomFunction.Builder("f3", listOf("x", "y"), "x^2+y^2")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder2.create())

        try {
            assertEquals("1", Expression.valueOf("f(1, 1)").numeric().toString())
            fail()
        } catch (e: ArithmeticException) {
            // ok
        }

        assertEquals("1.414213562373095", Expression.valueOf("f2(1, 1)").numeric().toString())
        assertEquals("5", Expression.valueOf("f2(4, 3)").numeric().toString())

        assertEquals("2*z1", Expression.valueOf("∂(f3(z1, z2), z1)").expand().toString())
        assertEquals("2*z2", Expression.valueOf("∂(f3(z1, z2), z2)").expand().toString())

        // test symbols
        val jBuilder3 = CustomFunction.Builder("f4", listOf("x", "y"), "2 000*x^2+y^2")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder3.create())
        val jBuilder4 = CustomFunction.Builder("f5", listOf("x", "y"), "2'000* x ^2+y^2\r")
        mathEngine.getFunctionsRegistry().addOrUpdate(jBuilder4.create())
    }

    @Test
    fun testNumbersAreReadInBinMode() {
        val me = JsclMathEngine.getInstance()
        me.setNumeralBase(NumeralBase.bin)
        val f = CustomFunction.Builder("test", listOf("x", "y"), "2000*x-0.001*y").create()
        assertEquals(NumeralBase.bin, me.getNumeralBase())
        me.setNumeralBase(NumeralBase.dec)
        assertEquals("2000*x-0.001*y", f.getContent())
    }

    @Test
    fun testInvalidFunctionShouldReturnNumeralBase() {
        val me = JsclMathEngine.getInstance()
        me.setNumeralBase(NumeralBase.bin)
        try {
            CustomFunction.Builder("test", emptyList(), "2000*").create()
            fail()
        } catch (ignored: CustomFunctionCalculationException) {
            // Expected
        }
        assertEquals(NumeralBase.bin, me.getNumeralBase())
        me.setNumeralBase(NumeralBase.dec)
    }
}
