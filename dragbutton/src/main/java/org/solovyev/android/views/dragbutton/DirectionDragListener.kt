package org.solovyev.android.views.dragbutton

import android.content.Context
import android.graphics.PointF
import android.util.Log
import android.view.View
import org.solovyev.android.views.dragbutton.Drag.distance
import org.solovyev.android.views.dragbutton.Drag.getAngle
import org.solovyev.android.views.dragbutton.Drag.sum
import kotlin.math.atan2

abstract class DirectionDragListener(context: Context) : DragListener {

    private val minDistancePxs: Float = context.resources.getDimensionPixelSize(R.dimen.drag_min_distance).toFloat()
    private val right = BooleanArray(1)

    override fun onDrag(view: View, e: DragEvent): Boolean {
        val duration = e.motionEvent.eventTime - e.motionEvent.downTime
        if (duration < 40 || duration > 2500) {
            Log.v(TAG, "Drag stopped: too fast movement, ${duration}ms")
            return false
        }

        val distance = distance(e.start, e.end)
        if (distance < minDistancePxs) {
            Log.v(TAG, "Drag stopped: too short distance, ${distance}pxs")
            return false
        }

        val angle = Math.toDegrees(getAngle(e.start, sum(e.start, AXIS), e.end, right).toDouble())
        val direction = getDirection(angle.toFloat(), right[0]) ?: run {
            Log.v(TAG, "Drag stopped: unknown direction")
            return false
        }

        return onDrag(view, e, direction)
    }

    protected abstract fun onDrag(view: View, event: DragEvent, direction: DragDirection): Boolean

    companion object {
        private const val TAG = "DirectionDragListener"
        private val AXIS = PointF(0f, 1f)

        private fun getDirection(angle: Float, right: Boolean): DragDirection? {
            for (direction in DragDirection.values()) {
                if (direction == DragDirection.left && right) {
                    continue
                }
                if (direction == DragDirection.right && !right) {
                    continue
                }
                if (direction.angleFrom <= angle && angle <= direction.angleTo) {
                    return direction
                }
            }
            return null
        }
    }
}
