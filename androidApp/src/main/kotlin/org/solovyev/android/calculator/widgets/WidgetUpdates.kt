package org.solovyev.android.calculator.widgets

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import org.solovyev.android.calculator.widget.CalculatorGlanceWidget

internal object WidgetUpdates {
    suspend fun updateAll(context: Context) {
        val widget = CalculatorGlanceWidget()
        val manager = GlanceAppWidgetManager(context)
        val ids = manager.getGlanceIds(CalculatorGlanceWidget::class.java)
        ids.forEach { glanceId ->
            widget.update(context, glanceId)
        }
    }
}
