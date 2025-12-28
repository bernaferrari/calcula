package jscl.math

import jscl.math.numeric.Real
import org.junit.Test

/**
 * User: serso
 * Date: 12/23/11
 * Time: 5:28 PM
 */
class LiteralTest {
    @Test
    fun testGcd() {
        // Empty test
    }

    @Test
    fun testScm() {
        val e1 = Expression.valueOf("2+sin(2)")
        val e2 = Expression.valueOf("3+cos(2)")
        val l1 = Literal.valueOf(DoubleVariable(NumericWrapper(Real.valueOf(2.0))))
        val l2 = Literal.valueOf(DoubleVariable(NumericWrapper(Real.valueOf(4.0))))

        println(e1)
        println(e2)

        var result = Literal.newInstance()
        println("${-1} -> $result")
        for (i in 0 until e1.size()) {
            result = result.scm(e1.literal(i))
            println("$i -> $result")
        }

        println(e1.literalScm())
        println(e2.literalScm())
    }
}
