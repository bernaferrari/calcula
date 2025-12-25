package org.solovyev.android.views.dragbutton

interface DragView {
    fun getId(): Int
    fun setOnDragListener(listener: DragListener?)
    fun setVibrateOnDrag(vibrateOnDrag: Boolean)
    fun setHighContrast(highContrast: Boolean)
}
