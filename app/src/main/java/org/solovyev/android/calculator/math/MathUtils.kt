package org.solovyev.android.calculator.math

object MathUtils {

    @JvmStatic
    fun clamp(value: Int, min: Int, max: Int): Int {
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }

    @JvmStatic
    fun clamp(value: Float, min: Float, max: Float): Float {
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }
}
