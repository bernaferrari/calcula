package org.solovyev.android.calculator

import android.app.Application
import android.content.Intent
import android.os.Handler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.widget.CalculatorGlanceWidgetReceiver
import org.solovyev.android.calculator.di.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Broadcaster @Inject constructor(
    private val application: Application,
    private val appPreferences: AppPreferences,
    private val handler: Handler,
    private val editor: Editor,
    private val display: Display
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _events = MutableSharedFlow<BroadcastEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<BroadcastEvent> = _events.asSharedFlow()

    init {
        handler.postDelayed({
            // we must update the widget when app starts
            sendInitIntent()
        }, 100)
        observeThemeChanges()
        observeEditorChanges()
        observeDisplayChanges()
    }

    private fun observeEditorChanges() {
        scope.launch {
            editor.changedEvents.collect {
                sendBroadcastIntent(ACTION_EDITOR_STATE_CHANGED)
            }
        }
        scope.launch {
            editor.cursorMovedEvents.collect {
                sendBroadcastIntent(ACTION_EDITOR_STATE_CHANGED)
            }
        }
    }

    private fun observeDisplayChanges() {
        scope.launch {
            display.changedEvents.collect {
                sendBroadcastIntent(ACTION_DISPLAY_STATE_CHANGED)
            }
        }
    }

    fun sendInitIntent() {
        sendBroadcastIntent(ACTION_INIT)
    }

    fun sendBroadcastIntent(action: String) {
        val intent = Intent(action).apply {
            setClass(application, CalculatorGlanceWidgetReceiver::class.java)
        }
        application.sendBroadcast(intent)

        scope.launch {
            _events.emit(BroadcastEvent(action))
        }
    }

    private fun observeThemeChanges() {
        scope.launch {
            combine(
                appPreferences.settings.theme,
                appPreferences.settings.widgetTheme
            ) { theme, widgetTheme ->
                theme to widgetTheme
            }.collect {
                sendBroadcastIntent(ACTION_THEME_CHANGED)
            }
        }
    }

    data class BroadcastEvent(val action: String)

    companion object {
        const val ACTION_INIT = "org.solovyev.android.calculator.INIT"
        const val ACTION_EDITOR_STATE_CHANGED = "org.solovyev.android.calculator.EDITOR_STATE_CHANGED"
        const val ACTION_DISPLAY_STATE_CHANGED = "org.solovyev.android.calculator.DISPLAY_STATE_CHANGED"
        const val ACTION_THEME_CHANGED = "org.solovyev.android.calculator.THEME_CHANGED"
    }
}
