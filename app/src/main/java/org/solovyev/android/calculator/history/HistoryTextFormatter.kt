package org.solovyev.android.calculator.history

import org.solovyev.android.calculator.jscl.JsclOperation

object HistoryTextFormatter {
    fun format(state: HistoryState): String {
        return state.editor.getTextString() + identitySign(state.display.operation) + state.display.text
    }

    private fun identitySign(operation: JsclOperation): String {
        return if (operation == JsclOperation.simplify) "≡" else "="
    }
}
