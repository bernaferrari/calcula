package org.solovyev.android.calculator.view

import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.EditText

class EditTextLongClickEraser private constructor(
    view: View,
    private val editView: EditText,
    vibrateOnKeypress: Boolean
) : BaseLongClickEraser(view, vibrateOnKeypress), View.OnClickListener {

    init {
        view.setOnClickListener(this)
    }

    override fun onStopErase() {}

    override fun onStartErase() {}

    override fun erase(): Boolean {
        val start = editView.selectionStart
        val end = editView.selectionEnd
        if (start < 0 || end < 0) {
            return false
        }
        val text = editView.text
        when {
            start != end -> text.delete(minOf(start, end), maxOf(start, end))
            start > 0 -> text.delete(start - 1, start)
        }
        return text.isNotEmpty()
    }

    override fun onClick(v: View) {
        erase()
        if (vibrateOnKeypress) {
            v.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        }
    }

    companion object {
        @JvmStatic
        fun attachTo(view: View, editView: EditText, vibrateOnKeypress: Boolean) {
            EditTextLongClickEraser(view, editView, vibrateOnKeypress)
        }
    }
}
