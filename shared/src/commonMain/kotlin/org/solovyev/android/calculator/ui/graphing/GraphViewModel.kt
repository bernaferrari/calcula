package org.solovyev.android.calculator.ui.graphing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.solovyev.android.calculator.BitwiseInfixNormalizer
import org.solovyev.android.calculator.ToJsclTextProcessor

/**
 * ViewModel for the graphing screen.
 * Uses compiled JSCL expressions and samples each curve across the visible window.
 */
class GraphViewModel(
    private val toJsclTextProcessor: ToJsclTextProcessor
) : ViewModel() {

    private val compiler = GraphExpressionCompiler { rawExpression ->
        val normalized = BitwiseInfixNormalizer.normalize(rawExpression)
        toJsclTextProcessor.process(normalized).value
    }

    private val sampler = GraphCurveSampler()

    private val recomputeRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val compiledExpressions = mutableMapOf<Long, CompiledGraphExpression>()

    private val _graphState = MutableStateFlow(GraphState())
    val graphState: StateFlow<GraphState> = _graphState.asStateFlow()

    private val _curves = MutableStateFlow<List<GraphCurve>>(emptyList())
    val curves: StateFlow<List<GraphCurve>> = _curves.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var nextCurveId: Long = 1L
    private var nextColorIndex: Int = 0

    private val palette = listOf(
        0xFF4CAF50.toInt(), // green
        0xFF2196F3.toInt(), // blue
        0xFFF44336.toInt(), // red
        0xFFFF9800.toInt(), // orange
        0xFF9C27B0.toInt(), // purple
        0xFF00BCD4.toInt()  // cyan
    )

    init {
        viewModelScope.launch {
            recomputeRequests.collectLatest {
                recomputeCurves()
            }
        }
    }

    fun setExpression(expr: String) {
        val normalized = expr.trim()
        if (normalized.isEmpty()) return

        val first = _curves.value.firstOrNull()
        if (first == null) {
            addFunction(normalized)
        } else {
            updateFunction(first.id, normalized)
        }
    }

    fun upsertFunction(expression: String, curveId: Long? = null): Long? {
        val normalized = expression.trim()
        if (normalized.isEmpty()) return null
        return if (curveId == null) {
            addFunction(normalized)
        } else {
            if (updateFunction(curveId, normalized)) curveId else null
        }
    }

    fun addFunction(expression: String): Long? {
        val compilation = compiler.compile(expression)
        val compiled = when (compilation) {
            is GraphCompilationResult.Error -> {
                _error.value = compilation.message
                return null
            }
            is GraphCompilationResult.Success -> compilation.expression
        }

        val curve = GraphCurve(
            id = nextCurveId++,
            expression = compiled.rawExpression,
            color = palette[nextColorIndex % palette.size],
            enabled = true
        )
        nextColorIndex += 1

        compiledExpressions[curve.id] = compiled
        _curves.value = _curves.value + curve
        _error.value = null
        triggerRecompute()
        return curve.id
    }

    fun updateFunction(curveId: Long, expression: String): Boolean {
        val compilation = compiler.compile(expression)
        val compiled = when (compilation) {
            is GraphCompilationResult.Error -> {
                _error.value = compilation.message
                return false
            }
            is GraphCompilationResult.Success -> compilation.expression
        }

        var updated = false
        _curves.value = _curves.value.map { curve ->
            if (curve.id == curveId) {
                updated = true
                curve.copy(
                    expression = compiled.rawExpression,
                    error = null
                )
            } else {
                curve
            }
        }
        if (!updated) {
            return false
        }

        compiledExpressions[curveId] = compiled
        _error.value = null
        triggerRecompute()
        return true
    }

    fun removeFunction(curveId: Long) {
        val current = _curves.value
        val updated = current.filterNot { it.id == curveId }
        if (updated.size != current.size) {
            compiledExpressions.remove(curveId)
            _curves.value = updated
            _error.value = null
            triggerRecompute()
        }
    }

    fun clearFunctions() {
        if (_curves.value.isEmpty()) return
        compiledExpressions.clear()
        _curves.value = emptyList()
        _error.value = null
    }

    fun toggleFunction(curveId: Long, enabled: Boolean) {
        _curves.value = _curves.value.map { curve ->
            if (curve.id == curveId) curve.copy(enabled = enabled) else curve
        }
        triggerRecompute()
    }

    fun pan(dx: Float, dy: Float) {
        val state = _graphState.value
        val xRange = state.xMax - state.xMin
        val yRange = state.yMax - state.yMin

        _graphState.value = state.copy(
            xMin = state.xMin - dx * xRange,
            xMax = state.xMax - dx * xRange,
            yMin = state.yMin + dy * yRange,
            yMax = state.yMax + dy * yRange
        )
        triggerRecompute()
    }

    fun zoom(scaleFactor: Float, focusX: Float = 0.5f, focusY: Float = 0.5f) {
        if (!scaleFactor.isFinite() || scaleFactor <= 0f) return

        val scale = scaleFactor.coerceIn(0.2f, 5f).toDouble()
        val state = _graphState.value
        val xRange = state.xMax - state.xMin
        val yRange = state.yMax - state.yMin

        val newXRange = xRange / scale
        val newYRange = yRange / scale

        val safeFocusX = focusX.coerceIn(0f, 1f)
        val safeFocusY = focusY.coerceIn(0f, 1f)

        val xCenter = state.xMin + xRange * safeFocusX
        val yCenter = state.yMin + yRange * (1 - safeFocusY)

        _graphState.value = state.copy(
            xMin = xCenter - newXRange * safeFocusX,
            xMax = xCenter + newXRange * (1 - safeFocusX),
            yMin = yCenter - newYRange * (1 - safeFocusY),
            yMax = yCenter + newYRange * safeFocusY
        )
        triggerRecompute()
    }

    fun resetView() {
        _graphState.value = GraphState()
        triggerRecompute()
    }

    fun setBounds(
        xMin: Double,
        xMax: Double,
        yMin: Double,
        yMax: Double
    ): Boolean {
        if (!xMin.isFinite() || !xMax.isFinite() || !yMin.isFinite() || !yMax.isFinite()) {
            return false
        }
        if (xMin >= xMax || yMin >= yMax) {
            return false
        }
        _graphState.value = GraphState(
            xMin = xMin,
            xMax = xMax,
            yMin = yMin,
            yMax = yMax
        )
        triggerRecompute()
        return true
    }

    private fun triggerRecompute() {
        recomputeRequests.tryEmit(Unit)
    }

    private suspend fun recomputeCurves() {
        val curvesSnapshot = _curves.value
        if (curvesSnapshot.isEmpty()) {
            _error.value = null
            return
        }

        val stateSnapshot = _graphState.value
        val compiledSnapshot = compiledExpressions.toMap()

        val result = withContext(Dispatchers.Default) {
            val cacheUpdates = mutableMapOf<Long, CompiledGraphExpression>()
            val computedCurves = curvesSnapshot.map { curve ->
                computeCurve(
                    curve = curve,
                    state = stateSnapshot,
                    compiledSnapshot = compiledSnapshot,
                    cacheUpdates = cacheUpdates
                )
            }
            RecomputeResult(curves = computedCurves, cacheUpdates = cacheUpdates)
        }

        if (result.cacheUpdates.isNotEmpty()) {
            compiledExpressions.putAll(result.cacheUpdates)
        }

        _curves.value = result.curves
        _error.value = result.curves.firstOrNull { it.error != null }?.error
    }

    private fun computeCurve(
        curve: GraphCurve,
        state: GraphState,
        compiledSnapshot: Map<Long, CompiledGraphExpression>,
        cacheUpdates: MutableMap<Long, CompiledGraphExpression>
    ): GraphCurve {
        if (!curve.enabled) {
            return curve.copy(segments = emptyList(), error = null)
        }

        val compiled = compiledSnapshot[curve.id] ?: run {
            when (val compilation = compiler.compile(curve.expression)) {
                is GraphCompilationResult.Error -> {
                    return curve.copy(segments = emptyList(), error = compilation.message)
                }
                is GraphCompilationResult.Success -> {
                    cacheUpdates[curve.id] = compilation.expression
                    compilation.expression
                }
            }
        }

        val sample = sampler.sample(compiled, state)
        return curve.copy(
            segments = sample.segments,
            error = null
        )
    }

    private data class RecomputeResult(
        val curves: List<GraphCurve>,
        val cacheUpdates: Map<Long, CompiledGraphExpression>
    )
}

/**
 * State representing the visible graph window.
 */
data class GraphState(
    val xMin: Double = -10.0,
    val xMax: Double = 10.0,
    val yMin: Double = -10.0,
    val yMax: Double = 10.0
)

/**
 * A single point on the graph.
 */
data class GraphPoint(
    val x: Float,
    val y: Float
)

data class GraphSegment(
    val points: List<GraphPoint>
)

data class GraphCurve(
    val id: Long,
    val expression: String,
    val color: Int,
    val enabled: Boolean = true,
    val segments: List<GraphSegment> = emptyList(),
    val error: String? = null
)
