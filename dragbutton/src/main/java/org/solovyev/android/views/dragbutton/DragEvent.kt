package org.solovyev.android.views.dragbutton

import android.graphics.PointF
import android.view.MotionEvent

class DragEvent(
    val start: PointF,
    val motionEvent: MotionEvent
) {
    val end: PointF = PointF(motionEvent.x, motionEvent.y)
}
