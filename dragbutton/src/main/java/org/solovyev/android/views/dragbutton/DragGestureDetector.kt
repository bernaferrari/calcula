package org.solovyev.android.views.dragbutton

import android.graphics.PointF
import android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
import android.view.HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
import android.view.HapticFeedbackConstants.KEYBOARD_TAP
import android.view.MotionEvent
import android.view.View

class DragGestureDetector(private val view: View) {

    var listener: DragListener? = null
    private var start: PointF? = null
    var vibrateOnDrag = true

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startTracking(event)
                return false
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                return stopTracking(event)
            }
        }
        return false
    }

    private fun stopTracking(event: MotionEvent): Boolean {
        val currentStart = start
        val currentListener = listener

        if (currentStart == null || currentListener == null) {
            start = null
            return false
        }

        if (!currentListener.onDrag(view, DragEvent(currentStart, event))) {
            start = null
            return false
        }

        start = null
        if (vibrateOnDrag) {
            view.performHapticFeedback(
                KEYBOARD_TAP,
                FLAG_IGNORE_GLOBAL_SETTING or FLAG_IGNORE_VIEW_SETTING
            )
        }
        return true
    }

    private fun startTracking(event: MotionEvent) {
        start = PointF(event.x, event.y)
    }

    companion object {
        @JvmStatic
        fun makeCancelEvent(original: MotionEvent): MotionEvent {
            val event = MotionEvent.obtain(original)
            event.action = MotionEvent.ACTION_CANCEL
            return event
        }
    }
}
