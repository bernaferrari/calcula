package org.solovyev.android.calculator.variables

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IdRes
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.keyboard.BaseFloatingKeyboard
import org.solovyev.android.calculator.keyboard.FloatingKeyboard
import org.solovyev.android.calculator.view.EditTextLongClickEraser
import java.util.*

class GreekFloatingKeyboard(user: FloatingKeyboard.User) : BaseFloatingKeyboard(user), View.OnClickListener {

    override fun makeView(landscape: Boolean) {
        val columns = getColumnsCount(landscape)
        val rows = getRowsCount(landscape)
        var rowView: LinearLayout? = null
        var letter = 0

        for (i in 0 until columns * rows) {
            val column = i % columns
            val row = i / columns

            if (column == 0) {
                rowView = makeRow()
            }

            if (column == columns - 1) {
                if (!landscape) {
                    makeLastColumnPort(rowView!!, row)
                } else {
                    makeLastColumnLand(rowView!!, row)
                }
            } else if (letter < ALPHABET.length) {
                val button = addButton(rowView!!, View.NO_ID, ALPHABET[letter].toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    button.isAllCaps = false
                }
                letter++
            } else {
                addButton(rowView!!, View.NO_ID, "")
            }
        }
    }

    private fun makeLastColumnLand(rowView: LinearLayout, row: Int) {
        when (row) {
            0 -> {
                val backspace = addImageButton(rowView, R.id.cpp_kb_button_backspace, R.drawable.ic_backspace_white_24dp)
                EditTextLongClickEraser.attachTo(backspace, user.getEditor(), user.isVibrateOnKeypress())
            }
            1 -> addButton(rowView, R.id.cpp_kb_button_change_case, "↑")
            2 -> addImageButton(rowView, R.id.cpp_kb_button_keyboard, R.drawable.ic_keyboard_white_24dp)
            3 -> addImageButton(rowView, R.id.cpp_kb_button_close, R.drawable.ic_done_white_24dp)
            else -> addButton(rowView, View.NO_ID, "")
        }
    }

    private fun makeLastColumnPort(rowView: LinearLayout, row: Int) {
        when (row) {
            0 -> addButton(rowView, R.id.cpp_kb_button_clear, "C")
            1 -> {
                val backspace = addImageButton(rowView, R.id.cpp_kb_button_backspace, R.drawable.ic_backspace_white_24dp)
                EditTextLongClickEraser.attachTo(backspace, user.getEditor(), user.isVibrateOnKeypress())
            }
            2 -> addButton(rowView, R.id.cpp_kb_button_change_case, "↑")
            3 -> addImageButton(rowView, R.id.cpp_kb_button_keyboard, R.drawable.ic_keyboard_white_24dp)
            4 -> addImageButton(rowView, R.id.cpp_kb_button_close, R.drawable.ic_done_white_24dp)
            else -> addButton(rowView, View.NO_ID, "")
        }
    }

    override fun fillButton(button: View, @IdRes id: Int) {
        super.fillButton(button, id)
        button.setOnClickListener(this)
    }

    override fun getRowsCount(landscape: Boolean): Int = if (landscape) 4 else 5

    override fun getColumnsCount(landscape: Boolean): Int = if (landscape) 7 else 6

    override fun onClick(v: View) {
        if (user.isVibrateOnKeypress()) {
            v.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        }

        when (v.id) {
            R.id.cpp_kb_button_close -> user.done()
            R.id.cpp_kb_button_change_case -> changeCase(v as Button)
            R.id.cpp_kb_button_keyboard -> user.showIme()
            R.id.cpp_kb_button_clear -> {
                user.getEditor().setText("")
                user.getEditor().setSelection(0)
            }
            else -> user.getEditor().append((v as TextView).text)
        }
        user.getEditor().requestFocus()
    }

    private fun changeCase(button: Button) {
        val upperCase = button.text == "↑"
        App.processViewsOfType(user.getKeyboard(), Button::class.java) { key ->
            val letter = key.text.toString()
            if (!ALPHABET.contains(letter.lowercase(Locale.US))) {
                return@processViewsOfType
            }
            key.text = if (upperCase) {
                letter.uppercase(Locale.US)
            } else {
                letter.lowercase(Locale.US)
            }
        }
        button.text = if (upperCase) "↓" else "↑"
    }

    companion object {
        const val ALPHABET = "αβγδεζηθικλμνξοπρστυφχψω"
    }
}
