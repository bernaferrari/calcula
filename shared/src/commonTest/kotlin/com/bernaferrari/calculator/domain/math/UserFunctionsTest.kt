package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.library.CONSTANTS
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserFunctionsTest {
    @AfterTest fun reset() { UserFunctions.all = emptyList() }

    private fun eval(text: String) = CalcEngine.evaluate(text).stripTrailingZeros().toPlainString()

    @Test fun callsExpandIntoTheirBodies() {
        UserFunctions.all = listOf(UserFunction("tip", "x×0.18"), UserFunction("hyp", "√(x²+16)"))
        assertEquals("9", eval("tip(50)"))
        assertEquals("59", eval("50+tip(50)"))
        assertEquals("5", eval("hyp(3)"))
        // Unclosed at the end, like any function while typing.
        assertEquals("9", eval("tip(50"))
        // Implicit multiplication still works around them.
        assertEquals("18", eval("2tip(50)"))
    }

    @Test fun functionsCanUseEachOther() {
        UserFunctions.all = listOf(UserFunction("sq", "x×x"), UserFunction("quad", "sq(sq(x))"))
        assertEquals("81", eval("quad(3)"))
    }

    @Test fun selfReferenceIsAnErrorNotAHang() {
        UserFunctions.all = listOf(UserFunction("loop", "loop(x)+1"))
        assertTrue(CalcEngine.evaluateOrNull("loop(1)") == null)
    }

    @Test fun namesStayClearOfTheLanguage() {
        assertTrue(UserFunctions.isValidName("tip"))
        assertFalse(UserFunctions.isValidName("sin"))
        assertFalse(UserFunctions.isValidName("x"))
        assertFalse(UserFunctions.isValidName("Tip"))
        assertFalse(UserFunctions.isValidName("tip", taken = listOf("tip")))
    }

    @Test fun everyConstantParses() {
        CONSTANTS.forEach { assertTrue(CalcEngine.evaluateOrNull(it.value) != null, it.id) }
    }
}
