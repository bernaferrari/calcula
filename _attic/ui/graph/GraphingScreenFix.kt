package com.bernaferrari.calculator.ui.graph

// Extension functions to fix type conversion issues
fun Double.toFloatSafe(): Float = this.toFloat()

fun Int.toFloatSafe(): Float = this.toFloat()

// Fixed viewport calculations
fun calculatePanOffset(
    pan: Float,
    canvasSize: Int,
    viewportMin: Double,
    viewportMax: Double,
): Double {
    val range = viewportMax - viewportMin
    val pixelsPerUnit = canvasSize.toDouble() / range
    return pan.toDouble() / pixelsPerUnit
}
