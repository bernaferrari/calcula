package jscl.math

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.math.function.ConstantsRegistry
import jscl.math.numeric.Complex
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.cosh
import kotlin.random.Random
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ComplexOracleTest {

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
    fun testEulerIdentityRandomAngles() {
        val rnd = Random(4)
        repeat(200) {
            val theta = rnd.nextDouble(-1000.0, 1000.0)
            val value = evalComplex("e^(i*$theta)")
            assertApprox(cos(theta), value.realPart())
            assertApprox(sin(theta), value.imaginaryPart())
        }
    }

    @Test
    fun testLnOfNegativeReal() {
        val rnd = Random(5)
        repeat(50) {
            val x = rnd.nextDouble(1e-6, 1e6)
            val value = evalComplex("ln(-$x)")
            assertApprox(kotlin.math.ln(x), value.realPart())
            assertApprox(Math.PI, value.imaginaryPart())
        }
    }

    @Test
    fun testExpLnInverseOnPrincipalBranch() {
        val rnd = Random(6)
        repeat(100) {
            val r = rnd.nextDouble(1e-6, 100.0)
            val theta = rnd.nextDouble(-Math.PI + 1e-3, Math.PI - 1e-3)
            val value = evalComplex("exp(ln($r*e^(i*$theta)))")
            assertApprox(r * cos(theta), value.realPart())
            assertApprox(r * sin(theta), value.imaginaryPart())
        }
    }

    @Test
    fun testTrigHyperbolicComplexIdentity() {
        val rnd = Random(7)
        repeat(100) {
            val x = rnd.nextDouble(-10.0, 10.0)
            val sinValue = evalComplex("sin(i*$x)")
            assertApprox(0.0, sinValue.realPart())
            assertApprox(sinh(x), sinValue.imaginaryPart())

            val cosValue = evalComplex("cos(i*$x)")
            assertApprox(cosh(x), cosValue.realPart())
            assertApprox(0.0, cosValue.imaginaryPart())
        }
    }

    private fun evalComplex(expression: String): Complex {
        val numeric = engine.evaluateGeneric(expression).numeric() as NumericWrapper
        val content = numeric.content()
        return content as? Complex
            ?: throw AssertionError("Expected complex result but got: ${content::class.simpleName}")
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
