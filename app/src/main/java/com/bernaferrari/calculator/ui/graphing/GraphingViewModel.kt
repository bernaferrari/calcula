package com.bernaferrari.calculator.ui.graphing

import androidx.lifecycle.ViewModel
import com.bernaferrari.calculator.domain.model.GraphingFunction
import com.bernaferrari.calculator.domain.model.GraphState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class GraphingState(
    val functions: List<GraphingFunction> = emptyList(),
    val xMin: Double = -10.0,
    val xMax: Double = 10.0,
    val yMin: Double = -10.0,
    val yMax: Double = 10.0,
    val gridEnabled: Boolean = true
)

@HiltViewModel
class GraphingViewModel @Inject constructor() : ViewModel() {
    
    private val _state = MutableStateFlow(GraphingState())
    val state = _state.asStateFlow()
    
    private val predefinedColors = listOf(
        0xFF2196F3, 0xFFF44336, 0xFF4CAF50, 0xFFFF9800,
        0xFF9C27B0, 0xFF607D8B, 0xFFE91E63, 0xFF795548
    )
    
    fun addFunction() {
        val currentFunctions = _state.value.functions
        val colorIndex = currentFunctions.size % predefinedColors.size
        val newFunction = GraphingFunction(
            expression = "sin(x)",
            color = predefinedColors[colorIndex],
            isVisible = true,
            name = "f${currentFunctions.size + 1}"
        )
        
        _state.value = _state.value.copy(
            functions = currentFunctions + newFunction
        )
    }
    
    fun updateFunction(function: GraphingFunction, newExpression: String) {
        val updatedFunctions = _state.value.functions.map { f ->
            if (f == function) {
                f.copy(expression = newExpression)
            } else {
                f
            }
        }
        
        _state.value = _state.value.copy(functions = updatedFunctions)
    }
    
    fun toggleFunctionVisibility(function: GraphingFunction) {
        val updatedFunctions = _state.value.functions.map { f ->
            if (f == function) {
                f.copy(isVisible = !f.isVisible)
            } else {
                f
            }
        }
        
        _state.value = _state.value.copy(functions = updatedFunctions)
    }
    
    fun deleteFunction(function: GraphingFunction) {
        val updatedFunctions = _state.value.functions.filter { it != function }
        _state.value = _state.value.copy(functions = updatedFunctions)
    }
    
    fun changeFunctionColor(function: GraphingFunction, color: Long) {
        val updatedFunctions = _state.value.functions.map { f ->
            if (f == function) {
                f.copy(color = color)
            } else {
                f
            }
        }
        
        _state.value = _state.value.copy(functions = updatedFunctions)
    }
    
    fun resetView() {
        _state.value = _state.value.copy(
            xMin = -10.0,
            xMax = 10.0,
            yMin = -10.0,
            yMax = 10.0
        )
    }
    
    fun zoomIn() {
        val currentState = _state.value
        val xRange = currentState.xMax - currentState.xMin
        val yRange = currentState.yMax - currentState.yMin
        
        _state.value = currentState.copy(
            xMin = currentState.xMin + xRange * 0.1,
            xMax = currentState.xMax - xRange * 0.1,
            yMin = currentState.yMin + yRange * 0.1,
            yMax = currentState.yMax - yRange * 0.1
        )
    }
    
    fun zoomOut() {
        val currentState = _state.value
        val xRange = currentState.xMax - currentState.xMin
        val yRange = currentState.yMax - currentState.yMin
        
        _state.value = currentState.copy(
            xMin = currentState.xMin - xRange * 0.1,
            xMax = currentState.xMax + xRange * 0.1,
            yMin = currentState.yMin - yRange * 0.1,
            yMax = currentState.yMax + yRange * 0.1
        )
    }
    
    fun toggleGrid() {
        _state.value = _state.value.copy(
            gridEnabled = !_state.value.gridEnabled
        )
    }
}