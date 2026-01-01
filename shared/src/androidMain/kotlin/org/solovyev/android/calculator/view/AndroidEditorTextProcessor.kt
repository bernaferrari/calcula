package org.solovyev.android.calculator.view

import android.app.Application
import android.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.AppPreferences
import org.solovyev.android.calculator.text.TextProcessor
import org.solovyev.android.calculator.text.TextProcessorEditorResult

class AndroidEditorTextProcessor(
    private val application: Application,
    private val appPreferences: AppPreferences,
    private val engine: Engine
) : Highlighter {

    private var textHighlighter: Highlighter? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        scope.launch {
            appPreferences.gui.theme.collect { theme ->
                // Legacy Android implementation still uses the old TextHighlighter for now
                // but we can transition to CommonTextHighlighter soon.
            }
        }
    }

    override fun process(text: String): TextProcessorEditorResult {
        return textHighlighter?.process(text) ?: object : TextProcessorEditorResult {
            override fun getCharSequence(): CharSequence = text
            override val offset: Int = 0
        }
    }
}
