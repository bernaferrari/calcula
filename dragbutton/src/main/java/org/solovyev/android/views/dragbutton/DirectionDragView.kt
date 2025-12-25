package org.solovyev.android.views.dragbutton

interface DirectionDragView : DragView {
    fun getText(direction: DragDirection): DirectionText
}
