package org.solovyev.android.calculator.jscl

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.math.Expression
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(value = RobolectricTestRunner::class)
class FromJsclNumericTextProcessorTest {

    @Test
    @Throws(Exception::class)
    fun testCreateResultForComplexNumber() {
        val cm = FromJsclNumericTextProcessor()

        val me = JsclMathEngine.getInstance()
        me.groupingSeparator = ' '
        val defaultAngleUnits = me.angleUnits

        assertEquals("1.22133+23 123i", cm.process(Expression.valueOf("1.22133232+23123*i").numeric()))
        assertEquals("1.22133+1.2i", cm.process(Expression.valueOf("1.22133232+1.2*i").numeric()))
        assertEquals("1.22133+0i", cm.process(Expression.valueOf("1.22133232+0.000000001*i").numeric()))
        try {
            me.angleUnits = AngleUnit.rad
            assertEquals("1-0i", cm.process(Expression.valueOf("-(e^(i*π))").numeric()))
        } finally {
            me.angleUnits = defaultAngleUnits
        }
        assertEquals("1.22i", cm.process(Expression.valueOf("1.22*i").numeric()))
        assertEquals("i", cm.process(Expression.valueOf("i").numeric()))
        val numeric = Expression.valueOf("e^(Π*i)+1").numeric()
        assertEquals("0i", cm.process(numeric))
    }
}
