package org.solovyev.android.calculator.math

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Tests
import org.solovyev.android.calculator.math.MathType.postfix_function

@RunWith(value = RobolectricTestRunner::class)
class MathTypeTest {

    private lateinit var engine: Engine

    @Before
    @Throws(Exception::class)
    fun setUp() {
        engine = Tests.makeEngine()
    }

    @Test
    @Throws(Exception::class)
    fun testGetType() {
        assertEquals(MathType.function, MathType.getType("sin", 0, false, engine).type)
        assertEquals(MathType.text, MathType.getType("sn", 0, false, engine).type)
        assertEquals(MathType.text, MathType.getType("s", 0, false, engine).type)
        assertEquals(MathType.text, MathType.getType("", 0, false, engine).type)

        try {
            assertEquals(MathType.text, MathType.getType("22", -1, false, engine).type)
            fail()
        } catch (e: IllegalArgumentException) {
        }

        try {
            assertEquals(MathType.text, MathType.getType("22", 2, false, engine).type)
            fail()
        } catch (e: IllegalArgumentException) {
        }

        assertEquals("atanh", MathType.getType("atanh", 0, false, engine).match)
    }

    @Test
    @Throws(Exception::class)
    fun testPostfixFunctionsProcessing() {
        assertEquals(postfix_function, MathType.getType("5!", 1, false, engine).type)
        assertEquals(postfix_function, MathType.getType("!", 0, false, engine).type)
    }
}
