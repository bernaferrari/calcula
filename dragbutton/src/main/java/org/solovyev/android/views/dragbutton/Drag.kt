package org.solovyev.android.views.dragbutton

import androidx.compose.ui.geometry.Offset
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Utility object for drag gesture calculations.
 */
object Drag {

    fun distance(start: Offset, end: Offset): Float {
        return norm(end.x - start.x, end.y - start.y)
    }

    fun subtract(p1: Offset, p2: Offset): Offset {
        return Offset(p1.x - p2.x, p1.y - p2.y)
    }

    fun sum(p1: Offset, p2: Offset): Offset {
        return Offset(p1.x + p2.x, p1.y + p2.y)
    }

    fun norm(point: Offset): Float {
        return norm(point.x, point.y)
    }

    private fun norm(x: Float, y: Float): Float {
        return sqrt(x.pow(2) + y.pow(2))
    }

    fun getAngle(
        start: Offset,
        axisEnd: Offset,
        end: Offset,
        right: BooleanArray?
    ): Float {
        val axisVector = subtract(axisEnd, start)
        val vector = subtract(end, start)

        val a2 = distance(vector, axisVector).pow(2)
        val b = norm(vector)
        val b2 = b.pow(2)
        val c = norm(axisVector)
        val c2 = c.pow(2)

        right?.set(0, axisVector.x * vector.y - axisVector.y * vector.x < 0)

        return acos((-a2 + b2 + c2) / (2 * b * c))
    }

    private val AXIS = Offset(0f, 1f)

    /**
     * Determines the drag direction based on the start and end points.
     * Returns null if the gesture doesn't match any direction.
     */
    fun getDirection(start: Offset, end: Offset): DragDirection? {
        val right = BooleanArray(1)
        val angle = Math.toDegrees(getAngle(start, sum(start, AXIS), end, right).toDouble()).toFloat()
        
        for (direction in DragDirection.entries) {
            if (direction == DragDirection.left && right[0]) {
                continue
            }
            if (direction == DragDirection.right && !right[0]) {
                continue
            }
            if (direction.angleFrom <= angle && angle <= direction.angleTo) {
                return direction
            }
        }
        return null
    }
}
