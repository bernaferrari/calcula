package jscl.math

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.math.function.ConstantsRegistry
import jscl.math.numeric.Complex
import jscl.math.numeric.Real
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NumericOracleTest {

    private val engine = JsclMathEngine.getInstance()
    private var previousAngleUnit: AngleUnit? = null

    @Before
    fun setUp() {
        ConstantsRegistry.getInstance()
        previousAngleUnit = engine.getAngleUnits()
        engine.setAngleUnits(AngleUnit.rad)
    }

    @After
    fun tearDown() {
        previousAngleUnit?.let { engine.setAngleUnits(it) }
    }

    @Test
    fun testArithmeticAgainstDoubleOracle() {
        val rnd = Random(0)
        repeat(200) {
            val a = rnd.nextDouble(-1e6, 1e6)
            val b = rnd.nextDouble(-1e6, 1e6)
            assertApprox(a + b, evalReal("($a)+($b)"))
            assertApprox(a - b, evalReal("($a)-($b)"))
            assertApprox(a * b, evalReal("($a)*($b)"))
            assertApprox(a / b, evalReal("($a)/($b)"))
        }
    }

    @Test
    fun testPowersAgainstDoubleOracle() {
        val rnd = Random(1)
        repeat(200) {
            val base = rnd.nextDouble(-100.0, 100.0)
            val exp = rnd.nextInt(-5, 6)
            val expected = base.pow(exp.toDouble())
            assertApprox(expected, evalReal("($base)^$exp"))
        }
    }

    @Test
    fun testSqrtAndLogsAgainstDoubleOracle() {
        val rnd = Random(2)
        repeat(200) {
            val value = rnd.nextDouble(1e-6, 1e6)
            assertApprox(sqrt(value), evalReal("√($value)"))
            assertApprox(ln(value), evalReal("ln($value)"))
            assertApprox(value, evalReal("exp(ln($value))"))
        }
    }

    @Test
    fun testTrigAgainstDoubleOracle() {
        val rnd = Random(3)
        repeat(200) {
            val angle = rnd.nextDouble(-1000.0, 1000.0)
            assertApprox(kotlin.math.sin(angle), evalReal("sin($angle)"))
            assertApprox(kotlin.math.cos(angle), evalReal("cos($angle)"))
            if (abs(kotlin.math.cos(angle)) < 1e-6) return@repeat
            assertApprox(kotlin.math.tan(angle), evalReal("tan($angle)"), eps = 1e-10)
        }
    }

    private fun evalReal(expression: String): Double {
        try {
            val numeric = engine.evaluateGeneric(expression).numeric() as NumericWrapper
            return when (val content = numeric.content()) {
                is Real -> content.doubleValue()
                is Complex -> {
                    val imag = content.imaginaryPart()
                    if (abs(imag) < 1e-12) {
                        content.realPart()
                    } else {
                        throw AssertionError("Expected real result but got complex: $content")
                    }
                }
                else -> throw AssertionError("Unsupported numeric content: ${content::class.simpleName}")
            }
        } catch (e: Exception) {
            throw AssertionError("Failed to evaluate: $expression", e)
        }
    }

    private fun assertApprox(expected: Double, actual: Double, eps: Double = 1e-12) {
        when {
            expected.isNaN() -> assertTrue(actual.isNaN())
            expected.isInfinite() -> assertTrue(actual == expected)
            else -> {
                val diff = abs(expected - actual)
                val scale = maxOf(1.0, abs(expected))
                assertTrue("Expected $expected but was $actual", diff <= eps * scale)
            }
        }
    }
}
