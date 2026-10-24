package com.bernaferrari.calculator.domain.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FractionsTest {
    private fun shown(expression: String) = Fractions.worthShowing(CalcEngine.evaluate(expression))?.let(Fractions::format)

    @Test fun simpleFractionsAreFound() {
        assertEquals("3/8", shown("3÷8"))
        assertEquals("1/3", shown("1÷3"))
        assertEquals("2 1/3", shown("7÷3"))
        assertEquals("−5/6", shown("−5÷6"))
        assertEquals("1/7", shown("1÷7"))
        assertEquals("1 1/2", shown("1.5"))
    }

    @Test fun repeatingResultsSurviveArithmetic() {
        assertEquals("5/6", shown("1÷2+1÷3"))
        assertEquals("11/127", shown("11÷127"))
    }

    @Test fun nothingForWholeIrrationalOrMoneyLikeNumbers() {
        assertNull(shown("6÷3"))
        assertNull(shown("√2"))
        assertNull(shown("π"))
        assertNull(shown("19.99×3"))
        // Close to a third, but typed that way: not a third.
        assertNull(shown("0.33"))
        assertNull(shown("sin(45)"))
    }
}
