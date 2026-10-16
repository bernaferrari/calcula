package com.bernaferrari.calculator.ui.graph

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class GraphingViewModel
    @Inject
    constructor() : ViewModel() {
        private val _state = MutableStateFlow(GraphingState())
        val state: StateFlow<GraphingState> = _state.asStateFlow()

        private var nextColorIndex = 0

        fun updateFunction(expression: String) {
            _state.value = _state.value.copy(currentFunction = expression)
        }

        fun addFunction() {
            val currentFunction = _state.value.currentFunction.trim()
            if (currentFunction.isNotEmpty()) {
                val color = functionColors[nextColorIndex % functionColors.size]
                nextColorIndex++

                val newFunction =
                    GraphFunction(
                        expression = currentFunction,
                        color = color,
                        isVisible = true,
                    )

                val updatedFunctions = _state.value.functions + newFunction
                _state.value =
                    _state.value.copy(
                        functions = updatedFunctions,
                        currentFunction = "",
                    )
            }
        }

        fun removeFunction(index: Int) {
            val updatedFunctions = _state.value.functions.toMutableList()
            if (index in updatedFunctions.indices) {
                updatedFunctions.removeAt(index)
                _state.value = _state.value.copy(functions = updatedFunctions)
            }
        }

        fun clearFunctions() {
            _state.value =
                _state.value.copy(
                    functions = emptyList(),
                    currentFunction = "",
                )
            nextColorIndex = 0
        }

        fun updateViewport(viewport: Viewport) {
            _state.value = _state.value.copy(viewport = viewport)
        }

        fun resetZoom() {
            _state.value =
                _state.value.copy(
                    viewport =
                        Viewport(
                            xMin = -10.0,
                            xMax = 10.0,
                            yMin = -10.0,
                            yMax = 10.0,
                        ),
                )
        }

        fun toggleFunctionVisibility(index: Int) {
            val updatedFunctions = _state.value.functions.toMutableList()
            if (index in updatedFunctions.indices) {
                updatedFunctions[index] =
                    updatedFunctions[index].copy(
                        isVisible = !updatedFunctions[index].isVisible,
                    )
                _state.value = _state.value.copy(functions = updatedFunctions)
            }
        }

        fun zoomIn() {
            val viewport = _state.value.viewport
            val centerX = (viewport.xMin + viewport.xMax) / 2
            val centerY = (viewport.yMin + viewport.yMax) / 2
            val rangeX = (viewport.xMax - viewport.xMin) * 0.8
            val rangeY = (viewport.yMax - viewport.yMin) * 0.8

            updateViewport(
                Viewport(
                    xMin = centerX - rangeX / 2,
                    xMax = centerX + rangeX / 2,
                    yMin = centerY - rangeY / 2,
                    yMax = centerY + rangeY / 2,
                ),
            )
        }

        fun zoomOut() {
            val viewport = _state.value.viewport
            val centerX = (viewport.xMin + viewport.xMax) / 2
            val centerY = (viewport.yMin + viewport.yMax) / 2
            val rangeX = (viewport.xMax - viewport.xMin) * 1.25
            val rangeY = (viewport.yMax - viewport.yMin) * 1.25

            updateViewport(
                Viewport(
                    xMin = centerX - rangeX / 2,
                    xMax = centerX + rangeX / 2,
                    yMin = centerY - rangeY / 2,
                    yMax = centerY + rangeY / 2,
                ),
            )
        }
    }

data class GraphingState(
    val currentFunction: String = "",
    val functions: List<GraphFunction> = emptyList(),
    val viewport: Viewport = Viewport(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

private val functionColors =
    listOf(
        0xFF2196F3, // Blue
        0xFFF44336, // Red
        0xFF4CAF50, // Green
        0xFFFF9800, // Orange
        0xFF9C27B0, // Purple
        0xFF607D8B, // Blue Grey
        0xFFE91E63, // Pink
        0xFF795548, // Brown
    )
