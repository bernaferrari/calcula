package org.solovyev.android.calculator.plot

import jscl.math.function.CustomFunction
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpressionFunctionTest {

    @Test
    fun testComplexResultsAreUndefined() {
        val function = CustomFunction.Builder("f", listOf("x"), "ln(x)").create()
        val plotFunction = ExpressionFunction(function)

        val negative = plotFunction.evaluate(-5f)
        assertTrue(negative.isNaN())

        val positive = plotFunction.evaluate(5f)
        assertTrue(positive.isFinite())
    }
}
