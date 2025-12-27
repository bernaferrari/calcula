package jscl.math.operator

import jscl.math.Expression
import jscl.text.ParseException
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * User: serso
 * Date: 12/26/11
 * Time: 9:56 AM
 */
class RandTest {

    @Test
    fun testRand() {
        /*testRandString("rand()-rand()")
          testRandString("rand()*rand()")
          testRandString("rand()^2")
          testRandString("rand()/rand()")*/
    }

    private fun testRandString(expression: String) {
        assertEquals(expression, Expression.valueOf(expression).toString())
    }
}
