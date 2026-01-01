package org.solovyev.android.calculator.view

import org.solovyev.android.calculator.text.TextProcessorEditorResult

interface Highlighter {
    fun process(text: String): TextProcessorEditorResult
}
