package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.input.*
import com.bernaferrari.calculator.domain.model.AngleUnit
import kotlin.test.*

class EngineBoundaryTest {
    @Test fun smallResultsWithLargeExponentsRemainValid() {
        assertEquals(Decimal.ONE, CalcEngine.evaluate("1^20000"))
        assertEquals(Decimal.ZERO, CalcEngine.evaluate("0^3000000000"))
        assertEquals(Decimal.ONE, CalcEngine.evaluate("(-1)^3000000000"))
        assertEquals(Decimal("1E10000"), CalcEngine.evaluate("10^10000"))
        assertEquals(Decimal.ONE, CalcEngine.evaluate("(-1)^100000"))
        assertEquals(Decimal(-1), CalcEngine.evaluate("(-1)^100001"))
        val result = CalcEngine.evaluate("1.00001^20000").toDouble()
        assertTrue(kotlin.math.abs(result - 1.221401536766) < 1e-10)
        assertTrue(CalcEngine.evaluate("2^15000") > Decimal.ONE)
        assertEquals(CalcError.OVERFLOW, assertFailsWith<CalcException> { CalcEngine.evaluate("10^10001") }.error)
    }

    @Test fun unsupportedDigitsReturnAnErrorThroughPreviewAndEquals() {
        for (text in listOf("٢+١", "１２+３")) {
            assertNull(Calculator.preview(text, AngleUnit.DEGREES))
            val (state, outcome) = Calculator.press(CalcState(editor = Editor(text)), Key.EQUALS)
            assertEquals(CalcError.SYNTAX, state.error)
            assertIs<Outcome.Failed>(outcome)
        }
    }
}
