package jscl.math

import jscl.JsclMathEngine
import jscl.math.function.ConstantsRegistry
import jscl.math.function.Constant
import jscl.math.numeric.Numeric
import kotlin.random.Random
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SimplifyConsistencyTest {

    private val engine = JsclMathEngine.getInstance()

    @Before
    fun setUp() {
        ConstantsRegistry.getInstance()
    }

    @Test
    fun testSimplifyMatchesNumericEvaluation() {
        val expressions = listOf(
            "x+x",
            "x-x",
            "x^2-2*x+1",
            "(x+1)^2",
            "sin(x)^2+cos(x)^2",
            "ln(e^x)",
            "e^(ln(x))",
            "x/(1/x)",
            "x*0+5",
            "(x^3)^(1/3)"
        )
        val rnd = Random(6)
        repeat(100) {
            val xValue = rnd.nextDouble(0.1, 10.0)
            val x = Constant("x")
            val replacement = NumericWrapper.valueOf(xValue)
            for (expr in expressions) {
                val base = Expression.valueOf(expr)
                val simplified = base.expand().simplify()
                val numericBase = base.substitute(x, replacement).numeric() as NumericWrapper
                val numericSimplified = simplified.substitute(x, replacement).numeric() as NumericWrapper
                assertNumericApprox(
                    numericBase.content(),
                    numericSimplified.content(),
                    "Mismatch for $expr at x=$xValue"
                )
            }
        }
    }

    private fun assertNumericApprox(expected: Numeric, actual: Numeric, message: String) {
        when {
            expected is jscl.math.numeric.Real && actual is jscl.math.numeric.Real -> {
                val diff = kotlin.math.abs(expected.doubleValue() - actual.doubleValue())
                val scale = maxOf(1.0, kotlin.math.abs(expected.doubleValue()))
                assertTrue("$message (real)", diff <= 1e-12 * scale)
            }
            expected is jscl.math.numeric.Complex && actual is jscl.math.numeric.Complex -> {
                val realDiff = kotlin.math.abs(expected.realPart() - actual.realPart())
                val imagDiff = kotlin.math.abs(expected.imaginaryPart() - actual.imaginaryPart())
                assertTrue("$message (complex)", realDiff <= 1e-12 && imagDiff <= 1e-12)
            }
            expected is jscl.math.numeric.Real && actual is jscl.math.numeric.Complex -> {
                val realDiff = kotlin.math.abs(expected.doubleValue() - actual.realPart())
                val imagDiff = kotlin.math.abs(actual.imaginaryPart())
                assertTrue("$message (real->complex)", realDiff <= 1e-12 && imagDiff <= 1e-12)
            }
            expected is jscl.math.numeric.Complex && actual is jscl.math.numeric.Real -> {
                val realDiff = kotlin.math.abs(expected.realPart() - actual.doubleValue())
                val imagDiff = kotlin.math.abs(expected.imaginaryPart())
                assertTrue("$message (complex->real)", realDiff <= 1e-12 && imagDiff <= 1e-12)
            }
            else -> assertTrue("$message (type mismatch)", false)
        }
    }
}
