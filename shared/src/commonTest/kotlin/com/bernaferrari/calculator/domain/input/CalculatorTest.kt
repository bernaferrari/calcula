package com.bernaferrari.calculator.domain.input

import com.bernaferrari.calculator.domain.math.CalcError
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test

class CalculatorTest {
    private fun type(vararg keys: Key, from: CalcState = CalcState()): CalcState =
        keys.fold(from) { state, key -> Calculator.press(state, key).first }

    private fun text(vararg keys: Key) = type(*keys).editor.text

    @Test fun digitsAndOperators() {
        assertEquals("12+3", text(Key.D1, Key.D2, Key.PLUS, Key.D3))
        assertEquals("5", text(Key.D0, Key.D5))
        assertEquals("0.5", text(Key.DOT, Key.D5))
        assertEquals("1.5", text(Key.D1, Key.DOT, Key.DOT, Key.D5))
    }

    @Test fun operatorReplacesOperator() {
        assertEquals("5×", text(Key.D5, Key.PLUS, Key.TIMES))
        assertEquals("5×−", text(Key.D5, Key.TIMES, Key.MINUS))
        assertEquals("5+", text(Key.D5, Key.TIMES, Key.MINUS, Key.PLUS))
        assertEquals("", text(Key.TIMES))
        assertEquals("−", text(Key.MINUS))
    }

    @Test fun smartParenthesis() {
        assertEquals("(2+3)", text(Key.PARENS, Key.D2, Key.PLUS, Key.D3, Key.PARENS))
        assertEquals("2(", text(Key.D2, Key.PARENS))
    }

    @Test fun deleteRemovesFunctionNamesWhole() {
        assertEquals("2", text(Key.D2, Key.SIN, Key.DELETE))
        assertEquals("", text(Key.SIN, Key.D3, Key.DELETE, Key.DELETE))
    }

    @Test fun equalsShowsResultAndContinues() {
        val done = type(Key.D1, Key.D2, Key.D0, Key.MINUS, Key.D2, Key.D5, Key.PERCENT, Key.EQUALS)
        assertEquals("90", done.editor.text)
        assertTrue(done.resultShown)
        assertEquals("90+1", type(Key.PLUS, Key.D1, from = done).editor.text)
        assertEquals("7", type(Key.D7, from = done).editor.text)
    }

    @Test fun negativeResultIsParenthesizedForPowers() {
        val done = type(Key.D2, Key.MINUS, Key.D7, Key.EQUALS)
        assertEquals("(−5)²", type(Key.SQUARE, from = done).editor.text)
        assertEquals("−5+", type(Key.PLUS, from = done).editor.text)
    }

    @Test fun failedEqualsKeepsExpression() {
        val failed = Calculator.press(type(Key.D1, Key.DIVIDE, Key.D0), Key.EQUALS)
        assertEquals(Outcome.Failed(CalcError.DIVIDE_BY_ZERO), failed.second)
        assertEquals("1÷0", failed.first.editor.text)
    }

    @Test fun evaluatedExpressionAutoClosesParentheses() {
        val (_, outcome) = Calculator.press(type(Key.SIN, Key.D3, Key.D0), Key.EQUALS)
        assertEquals(Outcome.Evaluated("sin(30)", "0.5"), outcome)
    }

    @Test fun preview() {
        assertNull(Calculator.preview("12", com.bernaferrari.calculator.domain.model.AngleUnit.DEGREES))
        assertEquals("5", Calculator.preview("2+3", com.bernaferrari.calculator.domain.model.AngleUnit.DEGREES))
        assertEquals("5", Calculator.preview("2+3×", com.bernaferrari.calculator.domain.model.AngleUnit.DEGREES))
        assertNull(Calculator.preview("2+", com.bernaferrari.calculator.domain.model.AngleUnit.DEGREES))
    }

    @Test fun insertingPreviousResult() {
        val state = type(Key.D2, Key.TIMES)
        assertEquals("2×(−3)", Calculator.insertNumber(state, "−3").editor.text)
        assertEquals("2×90", Calculator.insertNumber(type(Key.D2), "90").editor.text)
    }

    @Test fun swipeFunctions() {
        assertEquals("sin⁻¹(", text(Key.ASIN))
        assertEquals("2×10^(", text(Key.D2, Key.POW10))
        assertEquals("4⁻¹", text(Key.D4, Key.RECIPROCAL))
        assertEquals("100", text(Key.D1, Key.DOUBLE_ZERO))
        assertEquals("∛", text(Key.CBRT))
    }

    @Test fun nudgeHopsOverFunctionNames() {
        val e = Editor("2sin(30)", 8)
        val left = (1..3).fold(e) { acc, _ -> ExpressionEditor.nudge(acc, -1) }
        assertEquals(5, left.cursor)
        assertEquals(1, ExpressionEditor.nudge(left, -1).cursor)
        assertEquals(5, ExpressionEditor.nudge(Editor("2sin(30)", 1), 1).cursor)
    }

    @Test fun repeatedEqualsRepeatsLastOperation() {
        val first = type(Key.D5, Key.PLUS, Key.D3, Key.EQUALS)
        assertEquals("8", first.editor.text)
        assertEquals("11", type(Key.EQUALS, from = first).editor.text)
        assertEquals("14", type(Key.EQUALS, Key.EQUALS, from = first).editor.text)
        // Nothing sensible to repeat after a function call.
        val trig = type(Key.SIN, Key.D3, Key.D0, Key.EQUALS)
        assertEquals("0.5", type(Key.EQUALS, from = trig).editor.text)
    }

    @Test fun fixedDecimalPlaces() {
        val state = CalcState(decimalPlaces = 2)
        assertEquals("0.33", type(Key.D1, Key.DIVIDE, Key.D3, Key.EQUALS, from = state).editor.text)
        assertEquals("2", type(Key.D4, Key.DIVIDE, Key.D2, Key.EQUALS, from = state).editor.text)
        assertEquals("0.67", Calculator.preview("2÷3", com.bernaferrari.calculator.domain.model.AngleUnit.DEGREES, 2))
    }
}
