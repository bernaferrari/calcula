package org.solovyev.android.calculator.model

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.MathEngine
import jscl.NumeralBase
import jscl.math.Expression
import jscl.math.function.CustomFunction
import jscl.text.ParseException
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.solovyev.android.calculator.BaseCalculatorTest
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.variables.CppVariable

@RunWith(value = RobolectricTestRunner::class)
class AndroidEngineTest : BaseCalculatorTest() {

    @Before
    override fun setUp() {
        super.setUp()
        engine.mathEngine.precision = 3
    }

    @Test
    @Throws(Exception::class)
    fun testDegrees() {
        val me: MathEngine = engine.mathEngine
        val defaultAngleUnit = me.angleUnits
        try {
            me.angleUnits = AngleUnit.rad
            me.precision = 3
            assertError("°")
            assertEval("0.017", "1°")
            assertEval("0.349", "20.0°")
            assertEval("0.5", "sin(30°)")
            assertEval("0.524", "asin(sin(30°))")
            assertEval("∂(cos(t), t, t, 1°)", "∂(cos(t),t,t,1°)")

            assertEval("∂(cos(t), t, t, 1°)", "∂(cos(t),t,t,1°)", JsclOperation.simplify)
        } finally {
            me.angleUnits = defaultAngleUnit
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFormatting() {
        val me: MathEngine = engine.mathEngine
        assertEval("12 345", me.simplify("12345"))
    }

    @Test
    @Throws(ParseException::class)
    fun testI() {
        val me: MathEngine = engine.mathEngine

        assertEval("-i", me.evaluate("i^3"))
        for (i in 0 until 1000) {
            val real = (Math.random() - 0.5) * 1000
            val imag = (Math.random() - 0.5) * 1000
            val exp = (Math.random() * 10).toInt()

            val sb = StringBuilder()
            sb.append(real)
            if (imag > 0) {
                sb.append("+")
            }
            sb.append(imag)
            sb.append("^").append(exp)
            try {
                me.evaluate(sb.toString())
            } catch (e: Throwable) {
                org.junit.Assert.fail(sb.toString())
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testEmptyFunction() {
        val me: MathEngine = engine.mathEngine
        try {
            me.evaluate("cos(cos(cos(cos(acos(acos(acos(acos(acos(acos(acos(acos(cos(cos(cos(cos(cosh(acos(cos(cos(cos(cos(cos(acos(acos(acos(acos(acos(acos(acos(acos(cos(cos(cos(cos(cosh(acos(cos()))))))))))))))))))))))))))))))))))))))")
            Assert.fail()
        } catch (ignored: ParseException) {
        }
        assertEval("0.34+1.382i", "ln(ln(ln(ln(ln(ln(ln(ln(ln(ln(ln(ln(ln(ln(ln(100)))))))))))))))")
        try {
            me.evaluate("cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos()))))))))))))))))))))))))))))))))))))")
            Assert.fail()
        } catch (ignored: ParseException) {
        }

        val defaultAngleUnit = me.angleUnits
        try {
            me.angleUnits = AngleUnit.rad
            assertEval("0.739", me.evaluate("cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(cos(1))))))))))))))))))))))))))))))))))))"))
        } finally {
            me.angleUnits = defaultAngleUnit
        }

        engine.variablesRegistry.addOrUpdate(CppVariable.builder("si").withValue(5.0).build().toJsclConstant())
        assertEval("5", me.evaluate("si"))

        assertError("sin")
    }

    @Test
    @Throws(Exception::class)
    fun testRounding() {
        val me: MathEngine = engine.mathEngine

        try {
            me.groupingSeparator = '\''
            me.precision = 2
            assertEval("12'345'678.9", me.evaluate("1.23456789E7"))
            me.precision = 10
            assertEval("12'345'678.9", me.evaluate("1.23456789E7"))
            assertEval("123'456'789", me.evaluate("1.234567890E8"))
            assertEval("1'234'567'890.1", me.evaluate("1.2345678901E9"))
        } finally {
            me.precision = 3
            me.groupingSeparator = JsclMathEngine.GROUPING_SEPARATOR_DEFAULT
        }
    }

    @Test
    @Throws(Exception::class)
    fun testNumeralSystems() {
        val me: MathEngine = engine.mathEngine

        assertEval("11 259 375", "0x:ABCDEF")
        assertEval("30 606 154.462", "0x:ABCDEF*e")
        assertEval("30 606 154.462", "e*0x:ABCDEF")
        assertEval("e", "e*0x:ABCDEF/0x:ABCDEF")
        assertEval("30 606 154.462", "0x:ABCDEF*e*0x:ABCDEF/0x:ABCDEF")
        assertEval("30 606 154.462", "c+0x:ABCDEF*e*0x:ABCDEF/0x:ABCDEF-c+0x:C-0x:C")
        assertEval("1 446 257 064 651.832", "28*28 * sin(28) - 0b:1101 + √(28) + exp(28)")
        assertEval("13", "0b:1101")

        assertError("0b:π")

        val defaultNumeralBase = me.numeralBase
        try {
            me.numeralBase = NumeralBase.bin
            assertEval("101", "10+11")
            assertEval("0.1011", "10/11")

            me.numeralBase = NumeralBase.hex
            assertEval("63 7B", "56CE+CAD")
            assertEval("E", "E")
        } finally {
            me.numeralBase = defaultNumeralBase
        }
    }

    @Test
    @Throws(Exception::class)
    fun testLog() {
        val me: MathEngine = engine.mathEngine

        assertEval("∞", Expression.valueOf("1/0").numeric().toString())
        assertEval("∞", Expression.valueOf("ln(10)/ln(1)").numeric().toString())

        // logarithm
        assertEval("ln(x)/ln(base)", (me.functionsRegistry["log"] as CustomFunction).content, JsclOperation.simplify)
        assertEval("∞", "log(1, 10)")
        assertEval("3.322", "log(2, 10)")
        assertEval("1.431", "log(5, 10)")
        assertEval("0.96", "log(11, 10)")
        assertEval("1/(bln(a))", "∂(log(a, b), b)", JsclOperation.simplify)
        assertEval("-ln(b)/(aln(a)^2)", "∂(log(a, b), a)", JsclOperation.simplify)
    }
}
