package org.solovyev.android.calculator

import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.IdRes
import org.solovyev.android.calculator.keyboard.BaseFloatingKeyboard
import org.solovyev.android.calculator.keyboard.FloatingKeyboard
import org.solovyev.android.calculator.view.EditTextLongClickEraser
import org.solovyev.android.views.dragbutton.DirectionDragButton
import org.solovyev.android.views.dragbutton.DirectionDragListener
import org.solovyev.android.views.dragbutton.Drag
import org.solovyev.android.views.dragbutton.DragDirection
import org.solovyev.android.views.dragbutton.DragEvent

class FloatingCalculatorKeyboard(
    user: User,
    private val parameterNames: List<String>
) : BaseFloatingKeyboard(user) {

    private val buttonHandler = ButtonHandler()
    private val dragListener = object : DirectionDragListener(user.getContext()) {
        override fun onDrag(view: View, event: DragEvent, direction: DragDirection): Boolean {
            return Drag.hasDirectionText(view, direction) && buttonHandler.onDrag(view, direction)
        }
    }

    override fun getUser(): User {
        return super.getUser() as User
    }

    override fun fillButton(button: View, @IdRes id: Int) {
        super.fillButton(button, id)
        button.setOnClickListener(buttonHandler)
    }

    override fun makeButton(@IdRes id: Int, text: String): DirectionDragButton {
        return super.makeButton(id, text).apply {
            setOnDragListener(dragListener)
        }
    }

    override fun makeView(landscape: Boolean) {
        if (landscape) {
            makeViewLand()
        } else {
            makeViewPort()
        }
    }

    private fun makeViewLand() {
        val parametersCount = parameterNames.size

        var row = makeRow()
        addImageButton(row, R.id.cpp_kb_button_keyboard, R.drawable.ic_keyboard_white_24dp)
        addButton(row, 0, if (parametersCount > 0) parameterNames[0] else "x")
        addButton(row, 0, "7")
        addButton(row, 0, "8")
        addButton(row, 0, "9").apply {
            setText(DragDirection.up, "π")
            setText(DragDirection.down, "e")
        }
        addOperationButton(row, R.id.cpp_kb_button_divide, "/").apply {
            setText(DragDirection.up, "√")
            setText(DragDirection.down, "%")
        }
        addOperationButton(row, R.id.cpp_kb_button_multiply, "×").apply {
            setText(DragDirection.up, "^")
            setText(DragDirection.down, "^2")
        }
        addButton(row, R.id.cpp_kb_button_clear, "C")

        row = makeRow()
        addButton(row, R.id.cpp_kb_button_brackets, "( )").apply {
            setText(DragDirection.up, "(")
            setText(DragDirection.down, ")")
        }
        addButton(row, 0, if (parametersCount > 1) parameterNames[1] else "y")
        addButton(row, 0, "4")
        addButton(row, 0, "5")
        addButton(row, 0, "6")
        addOperationButton(row, R.id.cpp_kb_button_minus, "−")
        addOperationButton(row, R.id.cpp_kb_button_plus, "+")
        val backspace = addImageButton(row, R.id.cpp_kb_button_backspace, R.drawable.ic_backspace_white_24dp)
        EditTextLongClickEraser.attachTo(backspace, user.getEditor(), user.isVibrateOnKeypress())

        row = makeRow()
        addButton(row, R.id.cpp_kb_button_functions_constants, "ƒ/π")
        addImageButton(row, R.id.cpp_kb_button_space, R.drawable.ic_space_bar_white_24dp)
        addButton(row, 0, "1")
        addButton(row, 0, "2")
        addButton(row, 0, "3")
        addButton(row, 0, "0").apply {
            setText(DragDirection.up, "000")
            setText(DragDirection.down, "00")
        }
        addButton(row, 0, ".").apply {
            setText(DragDirection.up, ",")
        }
        addImageButton(row, R.id.cpp_kb_button_close, R.drawable.ic_done_white_24dp)
    }

    private fun makeViewPort() {
        val parametersCount = parameterNames.size

        var row = makeRow()
        addButton(row, R.id.cpp_kb_button_constants, "π…")
        addButton(row, R.id.cpp_kb_button_functions, "ƒ")
        addImageButton(row, R.id.cpp_kb_button_space, R.drawable.ic_space_bar_white_24dp)
        val backspace = addImageButton(row, R.id.cpp_kb_button_backspace, R.drawable.ic_backspace_white_24dp)
        EditTextLongClickEraser.attachTo(backspace, user.getEditor(), user.isVibrateOnKeypress())
        addButton(row, R.id.cpp_kb_button_clear, "C")

        row = makeRow()
        addButton(row, 0, "7")
        addButton(row, 0, "8")
        addButton(row, 0, "9").apply {
            setText(DragDirection.up, "π")
            setText(DragDirection.down, "e")
        }
        addOperationButton(row, R.id.cpp_kb_button_divide, "/").apply {
            setText(DragDirection.up, "√")
            setText(DragDirection.down, "%")
        }
        addButton(row, 0, if (parametersCount > 0) parameterNames[0] else "x")

        row = makeRow()
        addButton(row, 0, "4")
        addButton(row, 0, "5")
        addButton(row, 0, "6")
        addOperationButton(row, R.id.cpp_kb_button_multiply, "×").apply {
            setText(DragDirection.up, "^")
            setText(DragDirection.down, "^2")
        }
        addButton(row, 0, if (parametersCount > 1) parameterNames[1] else "y")

        row = makeRow()
        addButton(row, 0, "1")
        addButton(row, 0, "2")
        addButton(row, 0, "3")
        addOperationButton(row, R.id.cpp_kb_button_minus, "−")
        addImageButton(row, R.id.cpp_kb_button_keyboard, R.drawable.ic_keyboard_white_24dp)

        row = makeRow()
        addButton(row, R.id.cpp_kb_button_brackets, "( )").apply {
            setText(DragDirection.up, "(")
            setText(DragDirection.down, ")")
        }
        addButton(row, 0, "0").apply {
            setText(DragDirection.up, "000")
            setText(DragDirection.down, "00")
        }
        addButton(row, 0, ".").apply {
            setText(DragDirection.up, ",")
        }
        addOperationButton(row, R.id.cpp_kb_button_plus, "+")
        addImageButton(row, R.id.cpp_kb_button_close, R.drawable.ic_done_white_24dp)
    }

    override fun getRowsCount(landscape: Boolean): Int {
        return if (landscape) 3 else 5
    }

    override fun getColumnsCount(landscape: Boolean): Int {
        return if (landscape) 8 else 5
    }

    interface User : FloatingKeyboard.User {
        fun insertOperator(operator: Char)
        fun insertOperator(operator: String)
        fun showFunctions(v: View)
        fun showConstants(v: View)
        fun showFunctionsConstants(v: View)
        fun insertText(text: CharSequence, offset: Int)
    }

    private inner class ButtonHandler : View.OnClickListener {
        private val user: User = getUser()

        override fun onClick(v: View) {
            if (user.isVibrateOnKeypress()) {
                v.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            }

            when (v.id) {
                R.id.cpp_kb_button_divide -> user.insertOperator('/')
                R.id.cpp_kb_button_plus -> user.insertOperator('+')
                R.id.cpp_kb_button_minus -> user.insertOperator('-')
                R.id.cpp_kb_button_multiply -> user.insertOperator("×")
                R.id.cpp_kb_button_functions_constants -> user.showFunctionsConstants(v)
                R.id.cpp_kb_button_functions -> user.showFunctions(v)
                R.id.cpp_kb_button_constants -> user.showConstants(v)
                R.id.cpp_kb_button_space -> user.insertText(" ", 0)
                R.id.cpp_kb_button_keyboard -> user.showIme()
                R.id.cpp_kb_button_clear -> {
                    user.getEditor().setText("")
                    user.getEditor().setSelection(0)
                }
                R.id.cpp_kb_button_brackets -> user.insertText("()", -1)
                R.id.cpp_kb_button_close -> user.done()
                else -> onDefaultClick(v)
            }
            user.getEditor().requestFocus()
        }

        private fun onDefaultClick(v: View) {
            user.insertText((v as Button).text, 0)
        }

        fun onDrag(button: View, direction: DragDirection): Boolean {
            val text = (button as DirectionDragButton).getTextValue(direction)
            if (TextUtils.isEmpty(text)) {
                return false
            }

            when (text) {
                "√" -> user.insertText("√()", -1)
                "," -> user.insertText(", ", 0)
                "^" -> user.insertOperator('^')
                "^2" -> user.insertOperator("^ 2")
                "?", ">", "<", ">=", "<=", ":" -> user.insertOperator(text)
                else -> user.insertText(text, 0)
            }
            return true
        }
    }
}
