package org.solovyev.android.calculator.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.Broadcaster

class CalculatorGlanceWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CalculatorGlanceWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Broadcaster.ACTION_INIT,
            Broadcaster.ACTION_EDITOR_STATE_CHANGED,
            Broadcaster.ACTION_DISPLAY_STATE_CHANGED,
            Broadcaster.ACTION_THEME_CHANGED,
            Intent.ACTION_CONFIGURATION_CHANGED,
            android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            android.appwidget.AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED -> {
                updateAllWidgets(context)
            }
        }
    }

    private fun updateAllWidgets(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            val manager = GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(CalculatorGlanceWidget::class.java)
            ids.forEach { glanceId ->
                glanceAppWidget.update(context, glanceId)
            }
            pendingResult.finish()
        }
    }
}
