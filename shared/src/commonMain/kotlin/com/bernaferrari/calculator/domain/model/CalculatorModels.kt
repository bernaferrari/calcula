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
)

enum class AngleUnit {
    RADIANS,
    DEGREES,
}
