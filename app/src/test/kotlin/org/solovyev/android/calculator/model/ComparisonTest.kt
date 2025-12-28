package org.solovyev.android.calculator.model

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.solovyev.android.calculator.BaseCalculatorTest

@RunWith(RobolectricTestRunner::class)
class ComparisonTest : BaseCalculatorTest() {

    @Before
    override fun setUp() {
        super.setUp()
        engine.getMathEngine().setPrecision(3)
    }

    @Test
    @Throws(Exception::class)
    fun testComparisonFunction() {
        assertEval("0", "eq(0, 1)")
        assertEval("1", "eq(1, 1)")
        assertEval("1", "eq(1, 1.0)")
        assertEval("0", "eq(1, 1.000000000000001)")
        assertEval("0", "eq(1, 0)")

        assertEval("1", "lt(0, 1)")
        assertEval("0", "lt(1, 1)")
        assertEval("0", "lt(1, 0)")

        assertEval("0", "gt(0, 1)")
        assertEval("0", "gt(1, 1)")
        assertEval("1", "gt(1, 0)")

        assertEval("1", "ne(0, 1)")
        assertEval("0", "ne(1, 1)")
        assertEval("1", "ne(1, 0)")

        assertEval("1", "le(0, 1)")
        assertEval("1", "le(1, 1)")
        assertEval("0", "le(1, 0)")

        assertEval("0", "ge(0, 1)")
        assertEval("1", "ge(1, 1)")
        assertEval("1", "ge(1, 0)")

        assertEval("0", "ap(0, 1)")
        assertEval("1", "ap(1, 1)")
        assertEval("0", "ap(1, 0)")
    }
}
