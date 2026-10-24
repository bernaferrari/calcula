package com.bernaferrari.calculator.domain.input

import com.bernaferrari.calculator.domain.math.CalcEngine
import kotlin.test.Test
import kotlin.test.assertEquals

class SpokenMathTest {
    private fun eval(spoken: String) = CalcEngine.evaluate(SpokenMath.normalize(spoken)).stripTrailingZeros().toPlainString()

    @Test fun wordsBecomeOperators() {
        assertEquals("40", eval("12 times 3 plus 4"))
        assertEquals("12", eval("what is 15% of 80"))
        assertEquals("1024", eval("2 to the power of 10"))
        assertEquals("4", eval("square root of 16"))
        assertEquals("25", eval("5 squared"))
        assertEquals("2.5", eval("10 divided by 4"))
        assertEquals("6", eval("3*4/2"))
        assertEquals("12", eval("3 x 4"))
        assertEquals("400", eval("1,200 ÷ 3"))
        assertEquals("1", eval("3 minus 2 ="))
    }
}
