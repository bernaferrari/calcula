package org.solovyev.android.calculator.model

import jscl.AngleUnit
import jscl.MathEngine
import jscl.NumeralBase
import jscl.math.Expression
import jscl.math.function.Constant
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.solovyev.android.calculator.BaseCalculatorTest
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.variables.CppVariable

@RunWith(RobolectricTestRunner::class)
class EvaluateTest : BaseCalculatorTest() {

    @Before
    override fun setUp() {
        super.setUp()
        engine.mathEngine.precision = 3
    }

    @Test
    @Throws(Exception::class)
    fun testEvaluate() {
        val cm: MathEngine = engine.mathEngine

        val defaultAngleUnit = cm.angleUnits

        assertEval("cos(t)+10%", "cos(t)+10%", JsclOperation.simplify)

        val expression = cm.simplifyGeneric("cos(t)+10%")
        expression.substitute(Constant("t"), Expression.valueOf(100.0))

        assertEval("it", "it", JsclOperation.simplify)
        assertEval("10%", "10%", JsclOperation.simplify)
        assertEval("0", "eq(0, 1)")
        assertEval("1", "eq(1, 1)")
        assertEval("1", "eq(  1,   1)")
        assertEval("1", "eq(  1,   1)", JsclOperation.simplify)
        assertEval("1", "lg(10)")
        assertEval("4", "2+2")
        try {
            cm.angleUnits = AngleUnit.rad
            assertEval("-0.757", "sin(4)")
            assertEval("0.524", "asin(0.5)")
            assertEval("-0.396", "sin(4)asin(0.5)")
            assertEval("-0.56", "sin(4)asin(0.5)√(2)")
            assertEval("-0.56", "sin(4)asin(0.5)√(2)")
        } finally {
            cm.angleUnits = defaultAngleUnit
        }
        assertEval("7.389", "e^2")
        assertEval("7.389", "exp(1)^2")
        assertEval("7.389", "exp(2)")
        assertEval("2+i", "2*1+√(-1)")
        try {
            cm.angleUnits = AngleUnit.rad
            assertEval("0.921+Πi", "ln(5cosh(38π√(2cos(2))))")
            assertEval("-3.41+3.41i", "(5tan(2i)+2i)/(1-i)")
        } finally {
            cm.angleUnits = defaultAngleUnit
        }
        assertEval("7.389i", "iexp(2)")
        assertEval("2+7.389i", "2+iexp(2)")
        assertEval("2+7.389i", "2+√(-1)exp(2)")
        assertEval("2-2.5i", "2-2.5i")
        assertEval("-2-2.5i", "-2-2.5i")
        assertEval("-2+2.5i", "-2+2.5i")
        assertEval("-2+2.1i", "-2+2.1i")
        assertEval("-0.1-0.2i", "(1-i)/(2+6i)")

        assertEval("24", "4!")
        assertEval("24", "(2+2)!")
        assertEval("120", "(2+2+1)!")
        assertEval("24", "(2.0+2.0)!")
        assertEval("24", "4.0!")
        assertEval("720", "(3!)!")
        assertEval("36", Expression.valueOf("3!^2").numeric().toString())
        assertEval("3", Expression.valueOf("cubic(27)").numeric().toString())
        assertError("i!")

        assertEval("1", cm.evaluate("(π/π)!"))

        assertError("(-1)i!")
        assertEval("24i", "4!i")

        engine.variablesRegistry.addOrUpdate(CppVariable.builder("si", 5.0).build().toJsclConstant())

        try {
            cm.angleUnits = AngleUnit.rad
            assertEval("0.451", "acos(0.8999999999999811)")
            assertEval("-0.959", "sin(5)")
            assertEval("-4.795", "sin(5)si")
            assertEval("-23.973", "sisin(5)si")
            assertEval("-23.973", "si*sin(5)si")
            assertEval("-3.309", "sisin(5si)si")
        } finally {
            cm.angleUnits = defaultAngleUnit
        }

        engine.variablesRegistry.addOrUpdate(CppVariable.builder("s", 1.0).build().toJsclConstant())
        assertEval("5", cm.evaluate("si"))

        engine.variablesRegistry.addOrUpdate(CppVariable.builder("k", 3.5).build().toJsclConstant())
        engine.variablesRegistry.addOrUpdate(CppVariable.builder("k1", 4.0).build().toJsclConstant())
        assertEval("4", "k11")

        engine.variablesRegistry.addOrUpdate(CppVariable.builder("t").build().toJsclConstant())
        assertEval("11t", "t11", JsclOperation.simplify)
        assertEval("11et", "t11e", JsclOperation.simplify)
        assertEval("∞", "∞")
        assertEval("∞", "Infinity")
        assertEval("11∞t", "t11∞", JsclOperation.simplify)
        assertEval("-t+t^3", "t(t-1)(t+1)", JsclOperation.simplify)

        assertEval("100", "0.1E3")
        assertEval("3.957", "ln(8)lg(8)+ln(8)")

        assertEval("0.933", "0x:E/0x:F")

        try {
            cm.numeralBase = NumeralBase.hex
            assertEval("0.EEEF", "0x:E/0x:F")
            assertEval("0.EEEF", cm.simplify("0x:E/0x:F"))
            assertEval("0.EEEF", "E/F")
            assertEval("0.EEEF", cm.simplify("E/F"))
        } finally {
            cm.numeralBase = NumeralBase.dec
        }

        assertEval("0", "((((((0))))))")
        assertEval("0", "((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((0))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))")


        /*	assertEval("0.524", cm.evaluate( "30°").getResult());
        assertEval("0.524", cm.evaluate( "(10+20)°").getResult());
		assertEval("1.047", cm.evaluate( "(10+20)°*2").getResult());
		try {
			assertEval("0.278", cm.evaluate( "30°^2").getResult());
			fail();
		} catch (ParseException e) {
			if ( !e.getMessage().equals("Power operation after postfix function is currently unsupported!") ) {
				fail();
			}
		}*//*

*//*		try {
			cm.setTimeout(5000);
			assertEval("2", cm.evaluate( "2!").getResult());
		} finally {
			cm.setTimeout(3000);
		}*/

        engine.variablesRegistry.addOrUpdate(CppVariable.builder("t").build().toJsclConstant())
        assertEval("2t", "∂(t^2,t)", JsclOperation.simplify)
        assertEval("2t", "∂(t^2,t)")
        engine.variablesRegistry.addOrUpdate(CppVariable.builder("t", 2.0).build().toJsclConstant())
        assertEval("2t", "∂(t^2,t)", JsclOperation.simplify)
        assertEval("4", "∂(t^2,t)")

        assertEval("-x+xln(x)", "∫(ln(x), x)", JsclOperation.simplify)
        assertEval("-(x-xln(x))/(ln(2)+ln(5))", "∫(log(10, x), x)", JsclOperation.simplify)

        assertEval("∫((ln(2)+ln(5))/ln(x), x)", "∫(ln(10)/ln(x), x)", JsclOperation.simplify)
        //assertEval("∫(ln(10)/ln(x), x)", Expression.valueOf("∫(log(x, 10), x)").expand().toString());
        assertEval("∫((ln(2)+ln(5))/ln(x), x)", "∫(log(x, 10), x)")
        assertEval("∫((ln(2)+ln(5))/ln(x), x)", "∫(log(x, 10), x)", JsclOperation.simplify)
    }
}
