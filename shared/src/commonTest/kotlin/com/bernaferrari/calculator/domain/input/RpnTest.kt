package com.bernaferrari.calculator.domain.input

import com.bernaferrari.calculator.domain.math.CalcError
import com.bernaferrari.calculator.domain.math.Decimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RpnTest {
    private fun type(vararg keys: Key, from: RpnState = RpnState()): RpnState =
        keys.fold(from) { state, key -> Rpn.press(state, key).first }

    private fun RpnState.shown() = stack.map { it.stripTrailingZeros().toPlainString() }

    @Test fun enterThenOperate() {
        // 3 ↵ 4 + → 7
        assertEquals(listOf("7"), type(Key.D3, Key.EQUALS, Key.D4, Key.PLUS).shown())
        // (2 + 3) × 4 → 2 ↵ 3 + 4 ×
        assertEquals(listOf("20"), type(Key.D2, Key.EQUALS, Key.D3, Key.PLUS, Key.D4, Key.TIMES).shown())
        // Operand order: 10 ↵ 4 − is 6, and 2 ↵ 10 ^ is 1024.
        assertEquals(listOf("6"), type(Key.D1, Key.D0, Key.EQUALS, Key.D4, Key.MINUS).shown())
        assertEquals(listOf("1024"), type(Key.D2, Key.EQUALS, Key.D1, Key.D0, Key.POWER).shown())
    }

    @Test fun enterDuplicatesAndResultsLift() {
        // 5 ↵ × squares it.
        assertEquals(listOf("25"), type(Key.D5, Key.EQUALS, Key.TIMES).shown())
        // A number typed after a result pushes the result up.
        assertEquals(listOf("2", "7"), type(Key.D3, Key.EQUALS, Key.D4, Key.PLUS, Key.D2).shown())
    }

    @Test fun functionsActOnX() {
        assertEquals(listOf("3"), type(Key.D9, Key.SQRT).shown())
        assertEquals(listOf("0.5", "1"), type(Key.D1, Key.EQUALS, Key.D3, Key.D0, Key.SIN).shown())
        assertEquals(listOf("20", "200"), type(Key.D2, Key.D0, Key.D0, Key.EQUALS, Key.D1, Key.D0, Key.PERCENT).shown())
    }

    @Test fun swapRollAndSign() {
        assertEquals(listOf("1", "2"), type(Key.D1, Key.EQUALS, Key.D2, Key.PARENS).shown())
        assertEquals(listOf("-5"), type(Key.D5, Key.NEGATE).shown())
        // Mid-entry the sign flips and typing goes on.
        val s = type(Key.D5, Key.NEGATE, Key.D2)
        assertEquals("−52", s.entry)
    }

    @Test fun deleteEditsThenClearsX() {
        assertEquals("1", type(Key.D1, Key.D2, Key.DELETE).entry)
        val cleared = type(Key.D7, Key.EQUALS, Key.D8, Key.PLUS, Key.DELETE)
        assertEquals(listOf("0"), cleared.shown())
        // After CLx, a new number replaces the 0 instead of pushing it.
        assertEquals(listOf("4"), type(Key.D4, from = cleared).shown())
    }

    @Test fun errorsKeepTheStack() {
        val (state, outcome) = Rpn.press(type(Key.D1, Key.EQUALS, Key.D0), Key.DIVIDE)
        assertEquals(RpnOutcome.Failed(CalcError.DIVIDE_BY_ZERO), outcome)
        assertEquals(listOf("0", "1"), state.shown())
        assertNull(type(Key.D1, from = state).error)
    }

    @Test fun operationsReachTheTape() {
        val (_, outcome) = Rpn.press(type(Key.D3, Key.EQUALS, Key.D4), Key.TIMES)
        assertEquals(RpnOutcome.Evaluated("3×4", "12"), outcome)
        assertEquals(Decimal(12), Rpn.x(type(Key.D3, Key.EQUALS, Key.D4, Key.TIMES)))
    }
}
