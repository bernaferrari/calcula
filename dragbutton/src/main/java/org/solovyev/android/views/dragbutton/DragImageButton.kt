package org.solovyev.android.views.dragbutton

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageButton

abstract class DragImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr), DragView {

    private val dragDetector = DragGestureDetector(this)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (dragDetector.onTouchEvent(event)) {
            val cancelEvent = DragGestureDetector.makeCancelEvent(event)
            super.onTouchEvent(cancelEvent)
            cancelEvent.recycle()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun setOnDragListener(listener: DragListener?) {
        dragDetector.listener = listener
    }

    override fun setVibrateOnDrag(vibrateOnDrag: Boolean) {
        dragDetector.vibrateOnDrag = vibrateOnDrag
    }
}
