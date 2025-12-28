package org.solovyev.android.calculator

import jscl.JsclMathEngine
import jscl.NumeralBase
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.solovyev.android.calculator.testutils.MainDispatcherRule

@RunWith(RobolectricTestRunner::class)
class ToJsclTextProcessorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var preprocessor: ToJsclTextProcessor

    @Before
    fun setUp() {
        preprocessor = ToJsclTextProcessor()
        preprocessor.engine = Tests.makeEngine()
    }

    @Test
    fun testSpecialCases() {
        assertEquals("3^E10", preprocessor.process("3^E10").toString())
    }

    @Test
    fun testProcess() {
        assertEquals("", preprocessor.process("").toString())
        assertEquals("()", preprocessor.process("[]").toString())
        assertEquals("()*()", preprocessor.process("[][]").toString())
        assertEquals("()*(1)", preprocessor.process("[][1]").toString())
        assertEquals("(0)*(1)", preprocessor.process("[0][1]").toString())
        assertEquals("(0)*(1E)", preprocessor.process("[0][1E]").toString())
        assertEquals("(0)*(1E1)", preprocessor.process("[0][1E1]").toString())
        assertEquals("(0)*(1E-1)", preprocessor.process("[0][1E-1]").toString())
        assertEquals("(0)*(1.E-1)", preprocessor.process("[0][1.E-1]").toString())
        assertEquals("(0)*(2*E-1)", preprocessor.process("[0][2*E-1]").toString())
        assertEquals("(0)*ln(1)*(2*E-1)", preprocessor.process("[0]ln(1)[2*E-1]").toString())
        assertEquals("sin(4)*asin(0.5)*√(2)", preprocessor.process("sin(4)asin(0.5)√(2)").toString())
        assertEquals("sin(4)*cos(5)", preprocessor.process("sin(4)cos(5)").toString())
        assertEquals("π*sin(4)*π*cos(√(5))", preprocessor.process("πsin(4)πcos(√(5))").toString())
        assertEquals("π*sin(4)+π*cos(√(5))", preprocessor.process("πsin(4)+πcos(√(5))").toString())
        assertEquals("π*sin(4)+π*cos(√(5+(√(-1))))", preprocessor.process("πsin(4)+πcos(√(5+i))").toString())
        assertEquals("π*sin(4.01)+π*cos(√(5+(√(-1))))", preprocessor.process("πsin(4.01)+πcos(√(5+i))").toString())
        assertEquals("e^π*sin(4.01)+π*cos(√(5+(√(-1))))", preprocessor.process("e^πsin(4.01)+πcos(√(5+i))").toString())
        assertEquals("e^π*sin(4.01)+π*cos(√(5+(√(-1))))E2", preprocessor.process("e^πsin(4.01)+πcos(√(5+i))E2").toString())
        assertEquals("e^π*sin(4.01)+π*cos(√(5+(√(-1))))E-2", preprocessor.process("e^πsin(4.01)+πcos(√(5+i))E-2").toString())
        assertEquals("E2", preprocessor.process("E2").toString())
        assertEquals("E-2", preprocessor.process("E-2").toString())
        assertEquals("E-1/2", preprocessor.process("E-1/2").toString())
        assertEquals("E-1.2", preprocessor.process("E-1.2").toString())
        assertEquals("E+1.2", preprocessor.process("E+1.2").toString())
        assertEquals("E(-1.2)", preprocessor.process("E(-1.2)").toString())
        assertEquals("EE", preprocessor.process("EE").toString())

        try {
            preprocessor.engine.getMathEngine().setNumeralBase(NumeralBase.hex)
            assertEquals("22F*exp(F)", preprocessor.process("22Fexp(F)").toString())
        } finally {
            preprocessor.engine.getMathEngine().setNumeralBase(NumeralBase.dec)
        }
        assertEquals("0x:ABCDEF", preprocessor.process("0x:ABCDEF").toString())
        assertEquals("0x:ABCDEF", preprocessor.process("0x:A BC DEF").toString())
        assertEquals("0x:ABCDEF", preprocessor.process("0x:A BC                           DEF").toString())
        assertEquals("0x:ABCDEF*0*x", preprocessor.process("0x:A BC DEF*0x").toString())
        assertEquals("0x:ABCDEF001*0*x", preprocessor.process("0x:A BC DEF001*0x").toString())
        assertEquals("0x:ABCDEF001*0*c", preprocessor.process("0x:A BC DEF001*0c").toString())
        assertEquals("0x:ABCDEF001*c", preprocessor.process("0x:A BC DEF001*c").toString())
        assertEquals("0b:1101", preprocessor.process("0b:1101").toString())
        assertEquals("0x:1C", preprocessor.process("0x:1C").toString())
        assertEquals("0x:1C", preprocessor.process(" 0x:1C").toString())

        try {
            preprocessor.process("ln()")
            fail()
        } catch (ignored: ParseException) {
        }
        try {
            preprocessor.process("ln()ln()")
            fail()
        } catch (ignored: ParseException) {
        }

        try {
            preprocessor.process("eln()eln()ln()ln()ln()e")
            fail()
        } catch (ignored: ParseException) {
        }

        try {
            preprocessor.process("ln(ln(ln(ln(ln(ln(ln(ln(ln(ln(ln(ln(ln(ln(ln()))))))))))))))")
            fail()
        } catch (ignored: ParseException) {
        }

        try {
            preprocessor.process("cos(cos(cos(cos(acos(acos(acos(acos(acos(acos(acos(acos(cos(cos(cos(cos(cosh(acos(cos(cos(cos(cos(cos(acos(acos(acos(acos(acos(acos(acos(acos(cos(cos(cos(cos(cosh(acos(cos()))))))))))))))))))))))))))))))))))))))")
            fail()
        } catch (ignored: ParseException) {
        }

        assertEquals("0x:1C*0x:1C*sin(0x:1C)-0b:1101+√(0x:1C)+exp(0x:1C)", preprocessor.process("0x:1C*0x:1C * sin(0x:1C) - 0b:1101 + √(0x:1C) + exp ( 0x:1C)").toString())
    }

    @Test
    fun testPostfixFunction() {
    }

    @Test
    fun testNumeralBases() {
        val defaultNumeralBase = JsclMathEngine.getInstance().getNumeralBase()
        try {
            JsclMathEngine.getInstance().setNumeralBase(NumeralBase.bin)
            assertEquals("101", JsclMathEngine.getInstance().evaluate("10+11"))

            JsclMathEngine.getInstance().setNumeralBase(NumeralBase.hex)
            assertEquals("56CE+CAD", preprocessor.process("56CE+CAD").value)
        } finally {
            JsclMathEngine.getInstance().setNumeralBase(defaultNumeralBase)
        }
    }

    @Test
    fun testPercents() {
        assertEquals("100+100%*100", preprocessor.process("100+100%100").toString())
    }
}
