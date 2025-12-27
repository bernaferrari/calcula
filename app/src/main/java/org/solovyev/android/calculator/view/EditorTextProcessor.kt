package org.solovyev.android.calculator.view

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.text.TextProcessor
import org.solovyev.android.calculator.text.TextProcessorEditorResult

class EditorTextProcessor(
    private val application: Application,
    private val appPreferences: AppPreferences,
    private val engine: Engine
) : TextProcessor<TextProcessorEditorResult, String> {

    private var textHighlighter: TextHighlighter? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        scope.launch {
            appPreferences.settings.theme.collect { theme ->
                textHighlighter = TextHighlighter(
                    theme.getTextColorFor(application).normal,
                    true,
                    engine
                )
            }
        }
    }

    override fun process(from: String): TextProcessorEditorResult {
        val highlighter = getTextHighlighter()
        val processedText = highlighter.process(from)
        return TextProcessorEditorResult(processedText.getCharSequence(), processedText.offset)
    }

    private fun getTextHighlighter(): TextHighlighter {
        if (textHighlighter == null) {
            val theme = appPreferences.settings.getThemeBlocking()
            textHighlighter = TextHighlighter(
                theme.getTextColorFor(application).normal,
                true,
                engine
            )
        }
        return textHighlighter!!
    }
}
