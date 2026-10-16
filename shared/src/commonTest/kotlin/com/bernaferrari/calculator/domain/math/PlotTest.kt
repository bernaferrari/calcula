package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.model.AngleUnit
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlotTest {
    private fun at(expr: String, x: Double, angle: AngleUnit = AngleUnit.RADIANS) = assertNotNull(Plot.compile(expr, angle), expr).at(x)

    private fun near(expected: Double, actual: Double) = assertTrue(abs(expected - actual) < 1e-9, "expected $expected, got $actual")

    @Test fun polynomialsWithImplicitMultiplication() {
        near(9.0, at("2x²+1", 2.0))
        near(9.0, at("x²", 3.0))
        near(27.0, at("x³", 3.0))
        near(-6.0, at("−2x", 3.0))
        near(6.0, at("2(x+1)", 2.0))
    }

    @Test fun functionsAndConstants() {
        near(0.0, at("sin(x)", 0.0))
        near(1.0, at("sin(x)", 90.0, AngleUnit.DEGREES))
        near(kotlin.math.PI * 2, at("2π", 0.0))
        near(2.0, at("√x", 4.0))
        near(-2.0, at("∛x", -8.0))
        near(1.0, at("log(x)", 10.0))
        near(0.25, at("x⁻¹", 4.0))
    }

    @Test fun undefinedPointsAreNaN() {
        assertTrue(at("√x", -1.0).isNaN())
        assertTrue(at("ln(x)", -1.0).isNaN())
        assertTrue(at("1÷x", 0.0).isInfinite())
    }

    @Test fun autoClosesAndRejectsGarbage() {
        near(1.0, at("cos(x", 0.0))
        assertNull(Plot.compile(""))
        assertNull(Plot.compile("2+"))
        assertNull(Plot.compile("×3"))
    }

    @Test fun calculatorStillRejectsX() {
        assertNull(CalcEngine.evaluateOrNull("2x"))
    }
}
