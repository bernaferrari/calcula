/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Broadcaster.Companion.ACTION_DISPLAY_STATE_CHANGED
import org.solovyev.android.calculator.Broadcaster.Companion.ACTION_EDITOR_STATE_CHANGED
import org.solovyev.android.calculator.Broadcaster.Companion.ACTION_INIT
import org.solovyev.android.calculator.Broadcaster.Companion.ACTION_THEME_CHANGED
import org.solovyev.android.calculator.Display
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Preferences.SimpleTheme
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.WidgetReceiver.Companion.newButtonClickedIntent
import org.solovyev.android.calculator.buttons.CppButton
import javax.inject.Inject

@AndroidEntryPoint
class CalculatorWidget : AppWidgetProvider() {

    @Inject
    lateinit var editor: Editor

    @Inject
    lateinit var display: Display

    @Inject
    lateinit var engine: Engine

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        initCursorString(context)
    }

    private fun getCursorString(context: Context): SpannedString {
        return initCursorString(context)
    }

    private fun initCursorString(context: Context): SpannedString {
        if (cursorString == null) {
            val s = App.colorString("|", readWidgetCursorColor(context))
            // this will override any other style span (f.e. italic)
            s.setSpan(StyleSpan(Typeface.NORMAL), 0, 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            cursorString = SpannedString(s)
        }
        return cursorString!!
    }

    @ColorInt
    private fun readWidgetCursorColor(context: Context): Int {
        return try {
            ContextCompat.getColor(context, R.color.cpp_widget_cursor)
        } catch (e: Resources.NotFoundException) {
            // on Lenovo K910 the color for some reason can't be found
            0x757575
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateWidget(context, appWidgetManager, appWidgetIds, false)
    }

    fun updateWidget(context: Context, partially: Boolean) {
        val manager = AppWidgetManager.getInstance(context)
        val widgetIds = manager.getAppWidgetIds(ComponentName(context, CalculatorWidget::class.java))
        updateWidget(context, manager, widgetIds, partially)
    }

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetIds: IntArray,
        partially: Boolean
    ) {
        val editorState = editor.state
        val displayState = display.getState()

        val resources = context.resources
        val theme = App.getWidgetTheme().resolveThemeFor(App.getTheme())
        for (widgetId in widgetIds) {
            val views = RemoteViews(
                context.packageName,
                getLayout(manager, widgetId, resources, theme)
            )

            if (!partially) {
                for (button in CppButton.values()) {
                    val intent = PendingIntent.getBroadcast(
                        context,
                        button.id,
                        newButtonClickedIntent(context, button),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (intent != null) {
                        val buttonId = if (button == CppButton.settings_widget) {
                            // overriding default settings button behavior
                            CppButton.settings.id
                        } else {
                            button.id
                        }
                        views.setOnClickPendingIntent(buttonId, intent)
                    }
                }
            }

            updateEditorState(context, views, editorState, theme)
            updateDisplayState(context, views, displayState, theme)

            views.setTextViewText(R.id.cpp_button_multiplication, engine.multiplicationSign.value)

            if (partially) {
                manager.partiallyUpdateAppWidget(widgetId, views)
            } else {
                manager.updateAppWidget(widgetId, views)
            }
        }
    }

    private fun getDefaultLayout(theme: SimpleTheme): Int {
        return theme.getWidgetLayout(App.getTheme())
    }

    private fun getLayout(
        manager: AppWidgetManager,
        widgetId: Int,
        resources: Resources,
        theme: SimpleTheme
    ): Int {
        val options = manager.getAppWidgetOptions(widgetId) ?: return getDefaultLayout(theme)

        val category = options.getInt(OPTION_APPWIDGET_HOST_CATEGORY, -1)
        if (category == -1) {
            return getDefaultLayout(theme)
        }

        val keyguard = category == WIDGET_CATEGORY_KEYGUARD
        if (!keyguard) {
            return getDefaultLayout(theme)
        }

        val widgetMinHeight = App.toPixels(
            resources.displayMetrics,
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0).toFloat()
        )
        val lockScreenMinHeight = resources.getDimensionPixelSize(R.dimen.min_expanded_height_lock_screen)
        val expanded = widgetMinHeight >= lockScreenMinHeight
        return if (expanded) {
            R.layout.widget_layout_lockscreen
        } else {
            R.layout.widget_layout_lockscreen_collapsed
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Hilt @AndroidEntryPoint handles injection automatically
        super.onReceive(context, intent)

        val action = intent.action
        if (TextUtils.isEmpty(action)) {
            return
        }
        when (action) {
            ACTION_EDITOR_STATE_CHANGED,
            ACTION_DISPLAY_STATE_CHANGED -> updateWidget(context, true)
            Intent.ACTION_CONFIGURATION_CHANGED,
            ACTION_THEME_CHANGED,
            ACTION_INIT,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED -> updateWidget(context, false)
        }
    }

    private fun updateDisplayState(
        context: Context,
        views: RemoteViews,
        displayState: DisplayState,
        theme: SimpleTheme
    ) {
        val error = !displayState.valid
        if (!error) {
            views.setTextViewText(R.id.calculator_display, displayState.text)
        }
        views.setTextColor(
            R.id.calculator_display,
            ContextCompat.getColor(context, theme.getDisplayTextColor(error))
        )
    }

    private fun updateEditorState(
        context: Context,
        views: RemoteViews,
        state: EditorState,
        theme: SimpleTheme
    ) {
        val unspan = App.getTheme().light != theme.light

        val text = state.text
        val selection = state.selection
        if (selection < 0 || selection > text.length) {
            views.setTextViewText(
                R.id.calculator_editor,
                if (unspan) App.unspan(text) else text
            )
            return
        }

        val result: SpannableStringBuilder
        // inject cursor
        if (unspan) {
            val beforeCursor = text.subSequence(0, selection)
            val afterCursor = text.subSequence(selection, text.length)

            result = SpannableStringBuilder()
            result.append(App.unspan(beforeCursor))
            result.append(getCursorString(context))
            result.append(App.unspan(afterCursor))
        } else {
            result = SpannableStringBuilder(text)
            result.insert(selection, getCursorString(context))
        }
        views.setTextViewText(R.id.calculator_editor, result)
    }

    companion object {
        private const val WIDGET_CATEGORY_KEYGUARD = 2
        private const val OPTION_APPWIDGET_HOST_CATEGORY = "appWidgetCategory"
        private var cursorString: SpannedString? = null
    }
}
