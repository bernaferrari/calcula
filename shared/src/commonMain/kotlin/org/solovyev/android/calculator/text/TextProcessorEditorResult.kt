package org.solovyev.android.calculator.text

interface TextProcessorEditorResult {
    fun getCharSequence(): CharSequence
    val offset: Int
}
