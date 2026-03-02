package org.solovyev.android.calculator.widgets

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.Broadcaster
import org.solovyev.android.calculator.widget.CalculatorGlanceWidget

private val updateActions = setOf(
    Broadcaster.ACTION_INIT,
    Broadcaster.ACTION_EDITOR_STATE_CHANGED,
    Broadcaster.ACTION_DISPLAY_STATE_CHANGED,
    Broadcaster.ACTION_THEME_CHANGED,
    Broadcaster.ACTION_HISTORY_CHANGED,
    Intent.ACTION_CONFIGURATION_CHANGED,
    android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE,
    android.appwidget.AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED
)

internal abstract class BaseCalculatorWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = CalculatorGlanceWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action !in updateActions) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            runCatching {
                WidgetUpdates.updateAll(context)
            }
            pendingResult.finish()
        }
    }
}

internal class CalculatorWidgetReceiver : BaseCalculatorWidgetReceiver()

internal class QuickCalcWidgetReceiver : BaseCalculatorWidgetReceiver()

internal class HistoryWidgetReceiver : BaseCalculatorWidgetReceiver()

internal class ConverterWidgetReceiver : BaseCalculatorWidgetReceiver()

internal class SmartStackWidgetReceiver : BaseCalculatorWidgetReceiver()
