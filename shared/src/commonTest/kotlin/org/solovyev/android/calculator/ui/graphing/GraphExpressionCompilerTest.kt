package org.solovyev.android.calculator.ui.graphing

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GraphExpressionCompilerTest {

    private val compiler = GraphExpressionCompiler()

    @Test
    fun `should compile and evaluate real-valued expression`() {
        val result = compiler.compile("sin(x)")
        assertTrue(result is GraphCompilationResult.Success)

        val y = result.expression.evaluateAt(0.0)
        assertNotNull(y)
        assertTrue(abs(y) < 1e-9)
    }

    @Test
    fun `should preserve old complex unwrap behavior`() {
        val complexWithReal = compiler.compile("1+i")
        assertTrue(complexWithReal is GraphCompilationResult.Success)
        val y1 = complexWithReal.expression.evaluateAt(2.0)
        assertNotNull(y1)
        assertTrue(abs(y1 - 1.0) < 1e-9)

        val pureImaginary = compiler.compile("i")
        assertTrue(pureImaginary is GraphCompilationResult.Success)
        assertNull(pureImaginary.expression.evaluateAt(2.0))
    }

    @Test
    fun `should return error for invalid expression`() {
        val result = compiler.compile("cos(")
        assertTrue(result is GraphCompilationResult.Error)
    }

    @Test
    fun `should split discontinuities into separate segments`() {
        val compiled = compiler.compile("1/x")
        assertTrue(compiled is GraphCompilationResult.Success)

        val sample = GraphCurveSampler(sampleCount = 400).sample(
            expression = compiled.expression,
            state = GraphState(xMin = -10.0, xMax = 10.0, yMin = -10.0, yMax = 10.0)
        )

        assertTrue(sample.finitePointCount > 0)
        assertTrue(sample.segments.size >= 2)
    }
}
