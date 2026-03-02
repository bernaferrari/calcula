package org.solovyev.android.calculator.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.first
import org.koin.core.context.GlobalContext
import org.solovyev.android.calculator.AppPreferences
import org.solovyev.android.calculator.Display
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.WidgetReceiver
import org.solovyev.android.calculator.buttons.CppButton

class CalculatorGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: android.content.Context, id: androidx.glance.GlanceId) {
        val snapshot = readSnapshot()
        provideContent {
            WidgetContent(snapshot)
        }
    }

    private suspend fun readSnapshot(): WidgetSnapshot {
        val koin = GlobalContext.getOrNull()
        val displayState = runCatching { koin?.get<Display>()?.getState() }
            .getOrNull()
            ?: DisplayState.empty()
        val editorState = runCatching { koin?.get<Editor>()?.state }
            .getOrNull()
            ?: EditorState.empty()
        val multiplicationSign = runCatching { koin?.get<Engine>()?.multiplicationSign?.value }
            .getOrNull()
            ?: "×"
        val widgetTheme = runCatching { koin?.get<AppPreferences>()?.widget?.theme?.first() }
            .getOrNull()
            .orEmpty()
        val isLightTheme = widgetTheme == "material_light"
        return WidgetSnapshot(
            displayState = displayState,
            editorState = editorState,
            multiplicationSign = multiplicationSign,
            isLightTheme = isLightTheme
        )
    }

    @Composable
    private fun WidgetContent(snapshot: WidgetSnapshot) {
        val context = LocalContext.current

        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(widgetBackground(snapshot.isLightTheme))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WidgetDisplay(snapshot.displayState, snapshot.isLightTheme)
                Spacer(modifier = GlanceModifier.height(6.dp))
                WidgetEditor(snapshot.editorState, snapshot.isLightTheme)
                Spacer(modifier = GlanceModifier.height(10.dp))
                WidgetKeyboard(
                    multiplicationSign = snapshot.multiplicationSign,
                    onButtonAction = { button ->
                        actionSendBroadcast(WidgetReceiver.newButtonClickedIntent(context, button))
                    }
                )
            }
        }
    }

    @Composable
    private fun WidgetDisplay(state: DisplayState, isLightTheme: Boolean) {
        val text = state.text
        Text(
            text = text,
            maxLines = 1,
            style = TextStyle(
                color = displayTextColor(isLightTheme, !state.valid),
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 4.dp)
        )
    }

    @Composable
    private fun WidgetEditor(state: EditorState, isLightTheme: Boolean) {
        Text(
            text = formatEditorText(state),
            maxLines = 2,
            style = TextStyle(
                color = displayTextColor(isLightTheme, false)
            ),
            modifier = GlanceModifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 4.dp)
        )
    }

    @Composable
    private fun WidgetKeyboard(
        multiplicationSign: String?,
        onButtonAction: (CppButton) -> Action
    ) {
        val rows = listOf(
            listOf(
                WidgetButtonSpec(CppButton.app, "APP"),
                WidgetButtonSpec(CppButton.settings_widget, "SET"),
                WidgetButtonSpec(CppButton.memory, "M"),
                WidgetButtonSpec(CppButton.history, "H"),
                WidgetButtonSpec(CppButton.vars, "VAR")
            ),
            listOf(
                WidgetButtonSpec(CppButton.functions, "f(x)"),
                WidgetButtonSpec(CppButton.operators, "OP"),
                WidgetButtonSpec(CppButton.clear, "C"),
                WidgetButtonSpec(CppButton.erase, "⌫"),
                WidgetButtonSpec(CppButton.percent, "%")
            ),
            listOf(
                WidgetButtonSpec(CppButton.seven, "7"),
                WidgetButtonSpec(CppButton.eight, "8"),
                WidgetButtonSpec(CppButton.nine, "9"),
                WidgetButtonSpec(CppButton.division, "÷"),
                WidgetButtonSpec(CppButton.power, "^")
            ),
            listOf(
                WidgetButtonSpec(CppButton.four, "4"),
                WidgetButtonSpec(CppButton.five, "5"),
                WidgetButtonSpec(CppButton.six, "6"),
                WidgetButtonSpec(CppButton.multiplication, multiplicationSign ?: "×"),
                WidgetButtonSpec(CppButton.subtraction, "−")
            ),
            listOf(
                WidgetButtonSpec(CppButton.one, "1"),
                WidgetButtonSpec(CppButton.two, "2"),
                WidgetButtonSpec(CppButton.three, "3"),
                WidgetButtonSpec(CppButton.plus, "+"),
                WidgetButtonSpec(CppButton.brackets, "()")
            ),
            listOf(
                WidgetButtonSpec(CppButton.zero, "0"),
                WidgetButtonSpec(CppButton.period, "."),
                WidgetButtonSpec(CppButton.copy, "COPY"),
                WidgetButtonSpec(CppButton.paste, "PASTE"),
                WidgetButtonSpec(CppButton.equals, "=")
            )
        )

        Column(modifier = GlanceModifier.fillMaxWidth()) {
            rows.forEach { row ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    row.forEach { spec ->
                        WidgetKey(
                            spec = spec,
                            action = onButtonAction(spec.button)
                        )
                    }
                }
                Spacer(modifier = GlanceModifier.height(4.dp))
            }
        }
    }

    @Composable
    private fun WidgetKey(spec: WidgetButtonSpec, action: Action) {
        val background = ColorProvider(Color(0xFF2C2F33))
        val textColor = ColorProvider(Color(0xFFFFFFFF))
        androidx.glance.layout.Box(
            modifier = GlanceModifier
                .padding(2.dp)
                .background(background)
                .clickable(action)
                .width(56.dp)
                .height(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = spec.label,
                style = TextStyle(color = textColor, fontWeight = FontWeight.Medium)
            )
        }
    }

    private fun widgetBackground(isLightTheme: Boolean): ColorProvider {
        return if (isLightTheme) {
            ColorProvider(Color(0xFFF5F5F5))
        } else {
            ColorProvider(Color(0xFF121212))
        }
    }

    private fun displayTextColor(isLightTheme: Boolean, error: Boolean): ColorProvider {
        return if (error) {
            if (isLightTheme) ColorProvider(Color(0xFFB00020)) else ColorProvider(Color(0xFFFFB4A9))
        } else {
            if (isLightTheme) ColorProvider(Color(0xFF1C1B1F)) else ColorProvider(Color(0xFFE6E1E5))
        }
    }

    private fun formatEditorText(state: EditorState): String {
        val text = state.getTextString()
        val selection = state.selection.coerceIn(0, text.length)
        return buildString {
            append(text.substring(0, selection))
            append('|')
            append(text.substring(selection))
        }
    }

    private data class WidgetButtonSpec(
        val button: CppButton,
        val label: String
    )

    private data class WidgetSnapshot(
        val displayState: DisplayState,
        val editorState: EditorState,
        val multiplicationSign: String,
        val isLightTheme: Boolean
    )
}
