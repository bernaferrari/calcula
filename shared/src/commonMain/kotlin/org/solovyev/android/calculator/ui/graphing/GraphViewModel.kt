package org.solovyev.android.calculator.ui.graphing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.solovyev.android.calculator.Engine
import jscl.math.Expression
import kotlin.math.max
import kotlin.math.min

/**
 * ViewModel for the graphing screen.
 * Evaluates mathematical expressions as functions of x.
 */
class GraphViewModel(
    private val engine: Engine
) : ViewModel() {
    
    // Graph state
    private val _graphState = MutableStateFlow(GraphState())
    val graphState: StateFlow<GraphState> = _graphState.asStateFlow()
    
    // Expression input
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()
    
    // Computed points for the graph
    private val _points = MutableStateFlow<List<GraphPoint>>(emptyList())
    val points: StateFlow<List<GraphPoint>> = _points.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun setExpression(expr: String) {
        _expression.value = expr
        computePoints()
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
        computePoints()
    }
    
    fun zoom(scaleFactor: Float, focusX: Float = 0.5f, focusY: Float = 0.5f) {
        val state = _graphState.value
        val xRange = state.xMax - state.xMin
        val yRange = state.yMax - state.yMin
        
        val newXRange = xRange / scaleFactor
        val newYRange = yRange / scaleFactor
        
        val xCenter = state.xMin + xRange * focusX
        val yCenter = state.yMin + yRange * (1 - focusY)
        
        _graphState.value = state.copy(
            xMin = xCenter - newXRange * focusX,
            xMax = xCenter + newXRange * (1 - focusX),
            yMin = yCenter - newYRange * (1 - focusY),
            yMax = yCenter + newYRange * focusY
        )
        computePoints()
    }
    
    fun resetView() {
        _graphState.value = GraphState()
        computePoints()
    }
    
    private fun computePoints() {
        val expr = _expression.value
        if (expr.isBlank()) {
            _points.value = emptyList()
            _error.value = null
            return
        }
        
        viewModelScope.launch {
            try {
                val computed = withContext(Dispatchers.Default) {
                    val state = _graphState.value
                    val numPoints = 200
                    val step = (state.xMax - state.xMin) / numPoints
                    
                    val mathEngine = engine.getMathEngine()
                    val result = mutableListOf<GraphPoint>()
                    
                    var x = state.xMin
                    while (x <= state.xMax) {
                        try {
                            // Substitute x value into expression
                            val evalExpr = expr.replace("x", "($x)")
                            val y = mathEngine.evaluate(evalExpr).toDoubleOrNull()
                            if (y != null && y.isFinite()) {
                                result.add(GraphPoint(x.toFloat(), y.toFloat()))
                            }
                        } catch (e: Exception) {
                            // Skip invalid points (discontinuities, etc.)
                        }
                        x += step
                    }
                    result
                }
                _points.value = computed
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Invalid expression"
                _points.value = emptyList()
            }
        }
    }
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
