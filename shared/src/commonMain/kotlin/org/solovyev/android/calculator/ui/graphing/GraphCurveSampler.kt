package org.solovyev.android.calculator.ui.graphing

import kotlin.math.abs

internal class GraphCurveSampler(
    private val sampleCount: Int = 640,
    private val discontinuityMultiplier: Double = 3.0
) {

    fun sample(
        expression: CompiledGraphExpression,
        state: GraphState
    ): GraphSampleResult {
        if (state.xMax <= state.xMin) {
            return GraphSampleResult(emptyList(), 0)
        }

        val count = sampleCount.coerceAtLeast(64)
        val step = (state.xMax - state.xMin) / (count - 1)
        val yRange = (state.yMax - state.yMin).let { if (it > 0.0) it else 1.0 }
        val discontinuityThreshold = yRange * discontinuityMultiplier

        val segments = mutableListOf<GraphSegment>()
        val currentSegment = mutableListOf<GraphPoint>()
        var finitePointCount = 0
        var previousY: Double? = null

        for (index in 0 until count) {
            val x = state.xMin + step * index
            val y = expression.evaluateAt(x)
                ?.takeIf { it >= -Float.MAX_VALUE && it <= Float.MAX_VALUE }

            if (y == null) {
                flushSegment(currentSegment, segments)
                previousY = null
                continue
            }

            val shouldSplitForDiscontinuity = previousY != null &&
                abs(y - previousY) > discontinuityThreshold

            if (shouldSplitForDiscontinuity) {
                flushSegment(currentSegment, segments)
            }

            currentSegment += GraphPoint(x = x.toFloat(), y = y.toFloat())
            finitePointCount += 1
            previousY = y
        }

        flushSegment(currentSegment, segments)
        return GraphSampleResult(segments = segments, finitePointCount = finitePointCount)
    }

    private fun flushSegment(
        current: MutableList<GraphPoint>,
        target: MutableList<GraphSegment>
    ) {
        if (current.size >= 2) {
            target += GraphSegment(points = current.toList())
        }
        current.clear()
    }
}

internal data class GraphSampleResult(
    val segments: List<GraphSegment>,
    val finitePointCount: Int
)
