package org.solovyev.android.calculator.keyboard

import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IdRes
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.view.EditTextCompat
import org.solovyev.android.calculator.view.EditTextLongClickEraser
import org.solovyev.android.views.dragbutton.Drag
import org.solovyev.android.views.dragbutton.DirectionDragButton
import org.solovyev.android.views.dragbutton.DirectionDragListener
import org.solovyev.android.views.dragbutton.DirectionDragView
import org.solovyev.android.views.dragbutton.DragDirection
import org.solovyev.android.views.dragbutton.DragEvent

class FloatingNumberKeyboard(
    user: FloatingKeyboard.User
) : BaseFloatingKeyboard(user) {

    private val buttonHandler = ButtonHandler()
    private val dragListener: DirectionDragListener

    init {
        dragListener = object : DirectionDragListener(user.getContext()) {
            override fun onDrag(view: View, event: DragEvent, direction: DragDirection): Boolean {
                if (!Drag.hasDirectionText(view, direction)) {
                    return false
                }
                insertText((view as DirectionDragView).getText(direction).value)
                return true
            }
        }
    }

    override fun getRowsCount(landscape: Boolean): Int = 4

    override fun getColumnsCount(landscape: Boolean): Int = 4

    override fun makeView(landscape: Boolean) {
        var row = makeRow()
        addButton(row, 0, "7")
        addButton(row, 0, "8")
        addButton(row, 0, "9")
        val backspace = addImageButton(row, R.id.cpp_kb_button_backspace, R.drawable.ic_backspace_white_24dp)
        EditTextLongClickEraser.attachTo(backspace, user.getEditor(), user.isVibrateOnKeypress())

        row = makeRow()
        addButton(row, 0, "4").setText(DragDirection.left, "A")
        addButton(row, 0, "5").setText(DragDirection.left, "B")
        addButton(row, 0, "6").setText(DragDirection.left, "C")
        addButton(row, R.id.cpp_kb_button_clear, "C")

        row = makeRow()
        addButton(row, 0, "1").setText(DragDirection.left, "D")
        addButton(row, 0, "2").setText(DragDirection.left, "E")
        addButton(row, 0, "3").setText(DragDirection.left, "F")
        addButton(row, 0, "E")

        row = makeRow()
        addButton(row, 0, "-")
        addButton(row, 0, "0")
        addButton(row, 0, ".")
        addImageButton(row, R.id.cpp_kb_button_close, R.drawable.ic_done_white_24dp)
    }

    override fun fillButton(button: View, @IdRes id: Int) {
        super.fillButton(button, id)
        button.setOnClickListener(buttonHandler)
    }

    override fun makeButton(@IdRes id: Int, text: String): DirectionDragButton {
        val button = super.makeButton(id, text)
        button.setOnDragListener(dragListener)
        return button
    }

    private fun insertText(text: CharSequence) {
        EditTextCompat.insert(text, getUser().getEditor())
    }

    private inner class ButtonHandler : View.OnClickListener {
        override fun onClick(v: View) {
            val editor = getUser().getEditor()
            when (v.id) {
                R.id.cpp_kb_button_clear -> {
                    editor.setText("")
                    return
                }
                R.id.cpp_kb_button_close -> {
                    getUser().done()
                    return
                }
            }
            if (v is TextView) {
                insertText(v.text)
            }
        }
    }
}
