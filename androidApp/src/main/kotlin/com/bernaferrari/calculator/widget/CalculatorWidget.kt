package com.bernaferrari.calculator.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.bernaferrari.calculator.AppGraph
import com.bernaferrari.calculator.MainActivity
import com.bernaferrari.calculator.domain.input.CalcState
import com.bernaferrari.calculator.domain.input.Calculator
import com.bernaferrari.calculator.domain.input.Editor
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.input.Outcome
import com.bernaferrari.calculator.domain.model.CalculationHistory
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.ui.calculator.separators
import kotlinx.coroutines.flow.first

private val TEXT = stringPreferencesKey("text")
private val RESULT = booleanPreferencesKey("result")
private val ERROR = booleanPreferencesKey("error")
private val KEY = ActionParameters.Key<String>("key")

/**
 * The calculator on the home screen: the same keys and the same engine, tap only (flicks need
 * the app). Calculations land on the app's history; tap the display to open the app.
 */
class CalculatorWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = AppGraph.preferences.getUserPreferences().first()
        val formatter = DisplayFormatter(prefs.decimalSeparator.separators())
        provideContent {
            GlanceTheme { Content(formatter, prefs.angleUnit, prefs.decimalPlaces) }
        }
    }

    @Composable
    private fun Content(formatter: DisplayFormatter, angle: com.bernaferrari.calculator.domain.model.AngleUnit, decimals: Int) {
        val state = currentState<Preferences>()
        val text = state[TEXT].orEmpty()
        val resultShown = state[RESULT] ?: false
        val size = LocalSize.current
        val colors = GlanceTheme.colors
        val keyGap = 3.dp
        Column(
            GlanceModifier.fillMaxSize().background(colors.widgetBackground).cornerRadius(28.dp).padding(8.dp),
        ) {
            // The display; tapping it opens the app.
            val shown = if (text.isEmpty()) "0" else formatter.format(text, 0, null).text.text
            val preview = if (resultShown || state[ERROR] == true) null else Calculator.preview(text, angle, decimals)?.let(formatter::number)
            Column(
                GlanceModifier.fillMaxWidth().defaultWeight().padding(horizontal = 12.dp, vertical = 4.dp).clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.End,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    shown,
                    maxLines = 1,
                    style = TextStyle(
                        fontSize = entrySize(shown.length, size.width.value),
                        color = if (text.isEmpty()) colors.onSurfaceVariant else colors.onSurface,
                        textAlign = TextAlign.End,
                    ),
                    modifier = GlanceModifier.fillMaxWidth(),
                )
                Text(
                    if (state[ERROR] == true) "—" else preview?.let { "= $it" } ?: " ",
                    maxLines = 1,
                    style = TextStyle(fontSize = 16.sp, color = colors.onSurfaceVariant, textAlign = TextAlign.End),
                    modifier = GlanceModifier.fillMaxWidth(),
                )
            }
            val keyHeightDp = ((size.height.value * 0.72f) / 5f)
            val digitSize = (keyHeightDp * 0.42f).coerceIn(14f, 28f).sp
            // The keys take most of the height; the display gets the rest.
            Column(GlanceModifier.fillMaxWidth().height((size.height.value * 0.72f).dp)) {
                ROWS.forEach { row ->
                    Row(GlanceModifier.fillMaxWidth().defaultWeight()) {
                        row.forEach { (key, label, tone) ->
                            val (container, content) = when (tone) {
                                Tone.Digit -> colors.surfaceVariant to colors.onSurfaceVariant
                                Tone.Function -> colors.secondaryContainer to colors.onSecondaryContainer
                                Tone.Clear -> colors.secondaryContainer to colors.tertiary
                                Tone.Operator -> colors.primaryContainer to colors.onPrimaryContainer
                                Tone.Equals -> colors.primary to colors.onPrimary
                            }
                            Box(
                                GlanceModifier.defaultWeight().fillMaxHeight().padding(keyGap),
                            ) {
                                Box(
                                    GlanceModifier
                                        .fillMaxSize()
                                        .cornerRadius(16.dp)
                                        .background(container)
                                        .clickable(actionRunCallback<KeyAction>(actionParametersOf(KEY to key.name))),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        if (key == Key.DOT) formatter.decimal.toString() else label,
                                        style = TextStyle(fontSize = digitSize, color = content, fontWeight = if (tone == Tone.Digit) FontWeight.Normal else FontWeight.Medium),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun entrySize(length: Int, widthDp: Float): TextUnit =
        // Roughly 0.6em per character; never smaller than reads comfortably.
        ((widthDp - 40f) / (length.coerceAtLeast(4) * 0.6f)).coerceIn(18f, 44f).sp
}

private enum class Tone { Digit, Function, Clear, Operator, Equals }

private val ROWS = listOf(
    listOf(Triple(Key.CLEAR, "C", Tone.Clear), Triple(Key.PARENS, "( )", Tone.Function), Triple(Key.PERCENT, "%", Tone.Function), Triple(Key.DIVIDE, "÷", Tone.Operator)),
    listOf(Triple(Key.D7, "7", Tone.Digit), Triple(Key.D8, "8", Tone.Digit), Triple(Key.D9, "9", Tone.Digit), Triple(Key.TIMES, "×", Tone.Operator)),
    listOf(Triple(Key.D4, "4", Tone.Digit), Triple(Key.D5, "5", Tone.Digit), Triple(Key.D6, "6", Tone.Digit), Triple(Key.MINUS, "−", Tone.Operator)),
    listOf(Triple(Key.D1, "1", Tone.Digit), Triple(Key.D2, "2", Tone.Digit), Triple(Key.D3, "3", Tone.Digit), Triple(Key.PLUS, "+", Tone.Operator)),
    listOf(Triple(Key.D0, "0", Tone.Digit), Triple(Key.DOT, ".", Tone.Digit), Triple(Key.DELETE, "⌫", Tone.Digit), Triple(Key.EQUALS, "=", Tone.Equals)),
)

/** A key press: the same state machine as the app, kept in the widget's own state. */
class KeyAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val key = parameters[KEY]?.let { runCatching { Key.valueOf(it) }.getOrNull() } ?: return
        val prefs = AppGraph.preferences.getUserPreferences().first()
        var evaluated: Outcome.Evaluated? = null
        updateAppWidgetState(context, glanceId) { state ->
            val current = CalcState(
                editor = Editor(state[TEXT].orEmpty()),
                resultShown = state[RESULT] ?: false,
                angleUnit = prefs.angleUnit,
                decimalPlaces = prefs.decimalPlaces,
            )
            val (next, outcome) = Calculator.press(current, key)
            state[TEXT] = next.editor.text
            state[RESULT] = next.resultShown
            state[ERROR] = next.error != null
            evaluated = outcome as? Outcome.Evaluated
        }
        evaluated?.let { AppGraph.history.insertCalculation(CalculationHistory(expression = it.expression, result = it.result)) }
        CalculatorWidget().update(context, glanceId)
    }
}

class CalculatorWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalculatorWidget()
}
