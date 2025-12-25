package org.solovyev.android.calculator.view

import android.view.View
import org.solovyev.android.calculator.Calculator
import org.solovyev.android.calculator.Editor

class EditorLongClickEraser private constructor(
    view: View,
    vibrateOnKeypress: Boolean,
    private val editor: Editor,
    private val calculator: Calculator
) : BaseLongClickEraser(view, vibrateOnKeypress) {

    private var wasCalculatingOnFly = false

    override fun erase(): Boolean = editor.erase()

    override fun onStartErase() {
        wasCalculatingOnFly = calculator.isCalculateOnFly()
        if (wasCalculatingOnFly) {
            calculator.setCalculateOnFly(false)
        }
    }

    override fun onStopErase() {
        if (wasCalculatingOnFly) {
            calculator.setCalculateOnFly(true)
        }
    }

    companion object {
        @JvmStatic
        fun attachTo(
            view: View,
            vibrateOnKeypress: Boolean,
            editor: Editor,
            calculator: Calculator
        ): EditorLongClickEraser {
            return EditorLongClickEraser(view, vibrateOnKeypress, editor, calculator)
        }
    }
}
