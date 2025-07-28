package com.bernaferrari.calculator.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CalculationHistory(
    val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
) : Parcelable

@Parcelize
data class CalculatorButton(
    val id: String,
    val primary: String,
    val secondary: String? = null,
    val type: ButtonType,
    val function: ButtonFunction,
) : Parcelable

enum class ButtonType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    SPECIAL,
    PARENTHESIS,
}

enum class ButtonFunction {
    // Numbers
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,

    // Basic operators
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    EQUALS,
    DOT,

    // Scientific functions
    SIN,
    COS,
    TAN,
    ASIN,
    ACOS,
    ATAN,
    LOG,
    LN,
    SQRT,
    POWER,
    FACTORIAL,

    // Special functions
    CLEAR,
    CLEAR_ENTRY,
    BACKSPACE,
    PARENTHESIS,
    PI,
    E,
    ANS,
    
    // Memory functions
    MEMORY_STORE,    // MS - Memory Store
    MEMORY_RECALL,   // MR - Memory Recall  
    MEMORY_CLEAR,    // MC - Memory Clear
    MEMORY_PLUS,     // M+ - Memory Add
    MEMORY_MINUS,    // M- - Memory Subtract

    // Advanced
    RECIPROCAL,
    SQUARE,
    CUBE,
    PERCENT,

    // Variables for graphing
    X_VAR,
    T_VAR,
}

data class CalculatorState(
    val currentExpression: String = "",
    val currentResult: String = "0",
    val history: List<CalculationHistory> = emptyList(),
    val isError: Boolean = false,
    val errorMessage: String = "",
    val isInverseMode: Boolean = false,
    val angleUnit: AngleUnit = AngleUnit.RADIANS,
    val lastAnswer: String = "0",
    val cursorPosition: Int = 0,
    val hasMemoryValue: Boolean = false,
)

enum class AngleUnit {
    RADIANS,
    DEGREES,
}

data class GraphingFunction(
    val expression: String,
    val color: Long,
    val isVisible: Boolean = true,
    val name: String = "",
)

data class GraphState(
    val functions: List<GraphingFunction> = emptyList(),
    val xMin: Double = -10.0,
    val xMax: Double = 10.0,
    val yMin: Double = -10.0,
    val yMax: Double = 10.0,
    val gridEnabled: Boolean = true,
)
