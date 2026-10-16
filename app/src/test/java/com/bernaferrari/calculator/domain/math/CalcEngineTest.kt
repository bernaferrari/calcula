package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.model.AngleUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class CalcEngineTest {
    private fun eval(expr: String, angle: AngleUnit = AngleUnit.DEGREES) =
        NumberFormatter.format(CalcEngine.evaluate(expr, angle))

    private fun assertError(expr: String, error: CalcError) {
        try {
            CalcEngine.evaluate(expr)
            fail("Expected $error for $expr")
        } catch (e: CalcException) {
            assertEquals(expr, error, e.error)
        }
    }

    @Test fun arithmetic() {
        assertEquals("3", eval("1+2"))
        assertEquals("0.3", eval("0.1+0.2"))
        assertEquals("7", eval("1+2×3"))
        assertEquals("9", eval("(1+2)×3"))
        assertEquals("2.5", eval("5÷2"))
        assertEquals("1", eval("1÷3×3"))
        assertEquals("0.3333333333333333".take(17), eval("1÷3"))
        assertEquals("−1", eval("2−3"))
        assertEquals("6", eval("2*3"))
        assertEquals("−6", eval("2×−3"))
    }

    @Test fun precedenceOfPowerAndUnaryMinus() {
        assertEquals("−4", eval("−2^2"))
        assertEquals("4", eval("(−2)^2"))
        assertEquals("512", eval("2^3^2"))
        assertEquals("0.5", eval("2^−1"))
        assertEquals("1.4142135623731", eval("2^0.5").take(15))
        assertEquals("−2", eval("(−8)^(1÷3)"))
    }

    @Test fun implicitMultiplication() {
        assertEquals("6", eval("2(3)"))
        assertEquals("6.28318530717959", eval("2π"))
        assertEquals("12", eval("(3)(4)"))
        assertEquals("1", eval("2sin(30)"))
        assertEquals("6", eval("3√4"))
    }

    @Test fun autoClosesParentheses() {
        assertEquals("5", eval("(2+3"))
        assertEquals("0.5", eval("sin(30"))
        assertEquals("20", eval("2×(3+(7"))
    }

    @Test fun smartPercent() {
        assertEquals("90", eval("120−25%"))
        assertEquals("110", eval("100+10%"))
        assertEquals("5", eval("50×10%"))
        assertEquals("500", eval("50÷10%"))
        assertEquals("0.25", eval("25%"))
    }

    @Test fun postfix() {
        assertEquals("120", eval("5!"))
        assertEquals("25", eval("5²"))
        assertEquals("27", eval("3³"))
        assertEquals("0.25", eval("4⁻¹"))
        assertEquals("3", eval("√9"))
        assertEquals("−3", eval("∛(−27)"))
        assertEquals("1.4142135623731", eval("√2").take(15))
    }

    @Test fun trigInDegrees() {
        assertEquals("0.5", eval("sin(30)"))
        assertEquals("0.5", eval("(sin(30)^2+cos(30)^2)÷2"))
        assertEquals("0", eval("cos(90)"))
        assertEquals("0", eval("sin(180)"))
        assertEquals("1", eval("tan(45)"))
        assertEquals("30", eval("sin⁻¹(0.5)"))
        assertError("tan(90)", CalcError.DOMAIN)
    }

    @Test fun trigInRadians() {
        assertEquals("0", eval("sin(π)", AngleUnit.RADIANS))
        assertEquals("−1", eval("cos(π)", AngleUnit.RADIANS))
        assertEquals("1.5707963267949", eval("sin⁻¹(1)", AngleUnit.RADIANS))
    }

    @Test fun logs() {
        assertEquals("3", eval("log(1000)"))
        assertEquals("−2", eval("log(0.01)"))
        assertEquals("1", eval("ln(e)"))
        assertError("ln(0)", CalcError.DOMAIN)
        assertError("log(−1)", CalcError.DOMAIN)
    }

    @Test fun errors() {
        assertError("1÷0", CalcError.DIVIDE_BY_ZERO)
        assertError("5+", CalcError.SYNTAX)
        assertError("×5", CalcError.SYNTAX)
        assertError("1)", CalcError.SYNTAX)
        assertError("√(−1)", CalcError.DOMAIN)
        assertError("(−1)!", CalcError.DOMAIN)
        assertError("2.5!", CalcError.DOMAIN)
        assertError("10^99999^9", CalcError.OVERFLOW)
    }

    @Test fun formatting() {
        assertEquals("1E20", eval("10^20"))
        assertEquals("1.5E−12", eval("1.5÷10^12"))
        assertEquals("123456789012345", eval("123456789012345"))
        assertEquals("2.43290200817664E18", eval("20!"))
        assertEquals("1E21", eval("1E20×10"))
        assertEquals("0.000001", eval("10^−6"))
    }
}
