package org.solovyev.android.views.dragbutton

import android.graphics.PointF
import android.view.View
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

object Drag {

    @JvmStatic
    fun distance(start: PointF, end: PointF): Float {
        return norm(end.x - start.x, end.y - start.y)
    }

    @JvmStatic
    fun subtract(p1: PointF, p2: PointF): PointF {
        return PointF(p1.x - p2.x, p1.y - p2.y)
    }

    @JvmStatic
    fun hasDirectionText(view: View, direction: DragDirection): Boolean {
        if (view is DirectionDragView) {
            return view.getText(direction).hasValue()
        }
        return false
    }

    @JvmStatic
    fun sum(p1: PointF, p2: PointF): PointF {
        return PointF(p1.x + p2.x, p1.y + p2.y)
    }

    @JvmStatic
    fun norm(point: PointF): Float {
        return norm(point.x, point.y)
    }

    private fun norm(x: Float, y: Float): Float {
        return sqrt(x.pow(2) + y.pow(2))
    }

    @JvmStatic
    fun getAngle(
        start: PointF,
        axisEnd: PointF,
        end: PointF,
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
}
