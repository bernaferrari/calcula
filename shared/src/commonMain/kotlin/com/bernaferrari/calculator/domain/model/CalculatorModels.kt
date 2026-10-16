package com.bernaferrari.calculator.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class CalculationHistory(
    val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
)

enum class AngleUnit {
    RADIANS,
    DEGREES,
}
