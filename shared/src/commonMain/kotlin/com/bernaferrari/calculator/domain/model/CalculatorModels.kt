package com.bernaferrari.calculator.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class CalculationHistory(
    val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    /** A few words the person added ("rent split"), shown above the calculation. */
    val note: String? = null,
    /** The unrounded operand when the expression continued a previous answer. */
    val answerOperand: String? = null,
    val approximate: Boolean = false,
    /** Captures the evaluated expression for re-opening an unchanged tape entry. */
    val resultOperand: String? = null,
)

enum class AngleUnit {
    RADIANS,
    DEGREES,
}
