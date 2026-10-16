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

enum class AngleUnit {
    RADIANS,
    DEGREES,
}
