package org.solovyev.android.calculator.view

import android.app.Application
import android.content.SharedPreferences
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.text.TextProcessor
import org.solovyev.android.calculator.text.TextProcessorEditorResult

class EditorTextProcessor(
    private val application: Application,
    private val preferences: SharedPreferences,
    private val engine: Engine
) : TextProcessor<TextProcessorEditorResult, String>, SharedPreferences.OnSharedPreferenceChangeListener {

    private var textHighlighter: TextHighlighter? = null

    init {
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun process(text: String): TextProcessorEditorResult {
        val highlighter = getTextHighlighter()
        val processedText = highlighter.process(text)
        return TextProcessorEditorResult(processedText.getCharSequence(), processedText.offset)
    }

    private fun getTextHighlighter(): TextHighlighter {
        if (textHighlighter == null) {
            onSharedPreferenceChanged(preferences, Preferences.Gui.theme.key)
        }
        return textHighlighter!!
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String?) {
        if (key != null && Preferences.Gui.theme.isSameKey(key)) {
            val color = getTextColor(preferences)
            textHighlighter = TextHighlighter(color, true, engine)
        }
    }

    private fun getTextColor(preferences: SharedPreferences): Int {
        val theme = Preferences.Gui.getTheme(preferences)
        return theme.getTextColorFor(application).normal
    }
}
