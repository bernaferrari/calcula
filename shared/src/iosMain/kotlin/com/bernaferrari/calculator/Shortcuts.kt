package com.bernaferrari.calculator

import com.bernaferrari.calculator.domain.input.Calculator
import com.bernaferrari.calculator.domain.input.ExpressionEditor
import com.bernaferrari.calculator.domain.input.SpokenMath
import com.bernaferrari.calculator.domain.math.CalcEngine
import com.bernaferrari.calculator.domain.model.CalculationHistory
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.ui.calculator.separators
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/** What Siri and Shortcuts get back: the calculation as people read it, and the answer alone. */
class ShortcutAnswer(val spoken: String, val result: String)

/**
 * "Calculate 12 times 3" from Siri or Shortcuts: evaluated with the app's own settings, said back
 * the way the app writes it, and kept in history like anything typed. Null when it isn't math.
 */
fun calculateForShortcut(input: String): ShortcutAnswer? = runBlocking {
    val prefs = AppGraph.preferences.getUserPreferences().first()
    val text = SpokenMath.normalize(input)
    val closed = text + ")".repeat(ExpressionEditor.openParentheses(text).coerceAtLeast(0))
    val value = CalcEngine.evaluateOrNull(closed, prefs.angleUnit) ?: return@runBlocking null
    val result = Calculator.formatResult(value, prefs.decimalPlaces, prefs.notation)
    AppGraph.history.insertCalculation(CalculationHistory(expression = closed, result = result))
    val formatter = DisplayFormatter(prefs.decimalSeparator.separators())
    ShortcutAnswer(
        spoken = formatter.format(closed, 0, null).text.text + " = " + formatter.number(result),
        result = formatter.number(result),
    )
}

/** The newest answer in history, as the app shows it, for "last result". */
fun lastResultForShortcut(): ShortcutAnswer? = runBlocking {
    val prefs = AppGraph.preferences.getUserPreferences().first()
    val entry = AppGraph.history.getRecentHistory(1).first().firstOrNull() ?: return@runBlocking null
    val formatter = DisplayFormatter(prefs.decimalSeparator.separators())
    ShortcutAnswer(
        spoken = formatter.format(entry.expression, 0, null).text.text + " = " + formatter.number(entry.result),
        result = formatter.number(entry.result),
    )
}
