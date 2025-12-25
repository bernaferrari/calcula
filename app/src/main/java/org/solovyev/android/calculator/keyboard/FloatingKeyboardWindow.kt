package org.solovyev.android.calculator.keyboard

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import org.solovyev.android.Check
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.R

class FloatingKeyboardWindow(
    private val dismissListener: PopupWindow.OnDismissListener?
) {

    private var window: PopupWindow? = null
    private var dialog: Dialog? = null
    private var tablet: Boolean = false

    fun hide() {
        if (!isShown()) {
            return
        }
        Check.isNotNull(window)
        window?.dismiss()
        onDismissed()
    }

    private fun onDismissed() {
        if (!tablet) {
            moveDialog(Gravity.CENTER)
        }
        window = null
        dialog = null
    }

    fun show(keyboard: FloatingKeyboard, dialog: Dialog?) {
        val editor = keyboard.getUser().getEditor()
        if (isShown()) {
            App.hideIme(editor)
            return
        }

        this.dialog = dialog
        val context = editor.context
        this.tablet = App.isTablet(context)

        if (!tablet) {
            moveDialog(Gravity.TOP)
        }

        App.hideIme(editor)
        val view = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val resources = context.resources
        val buttonSize = resources.getDimensionPixelSize(R.dimen.cpp_clickable_area_size)
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val keyboardWidth = keyboard.getColumnsCount(landscape) * buttonSize
        val keyboardHeight = keyboard.getRowsCount(landscape) * buttonSize

        window = PopupWindow(view, keyboardWidth, keyboardHeight).apply {
            isClippingEnabled = false
            setOnDismissListener {
                onDismissed()
                dismissListener?.onDismiss()
            }
        }

        // see http://stackoverflow.com/a/4713487/720489
        editor.post(object : Runnable {
            override fun run() {
                if (window == null) {
                    return
                }
                if (editor.windowToken != null) {
                    App.hideIme(editor)
                    val inputWidth = editor.width
                    val xOff = (inputWidth - keyboardWidth) / 2
                    window?.width = keyboardWidth
                    window?.showAsDropDown(editor, xOff, 0)
                } else {
                    editor.postDelayed(this, 50)
                }
            }
        })

        keyboard.makeView(landscape)
    }

    fun isShown(): Boolean = window != null

    fun <V : View> getContentView(): V {
        @Suppress("UNCHECKED_CAST")
        return window?.contentView as V
    }

    private fun moveDialog(gravity: Int) {
        val dialogWindow = dialog?.window ?: return
        val lp = dialogWindow.attributes
        lp.gravity = gravity
        dialogWindow.attributes = lp
    }
}
