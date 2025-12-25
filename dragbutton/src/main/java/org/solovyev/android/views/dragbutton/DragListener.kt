package org.solovyev.android.views.dragbutton

import android.view.View
import java.util.EventListener

fun interface DragListener : EventListener {
    fun onDrag(view: View, event: DragEvent): Boolean
}
