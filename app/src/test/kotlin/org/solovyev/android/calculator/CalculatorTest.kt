package org.solovyev.android.calculator

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CalculatorTest : BaseCalculatorTest() {

    @Test
    fun testAnsVariable() {
        assertEval("2", "2")
        assertEval("2", "2")
        assertEval("2", "ans")
        assertEval("4", "ans^2")
        assertEval("16", "ans^2")
        assertEval("0", "0")
        assertEval("0", "ans")
        assertEval("3", "3")
        assertEval("9", "ans*ans")
        assertError("ans*an")
        assertEval("81", "ans*ans")
    }

}
