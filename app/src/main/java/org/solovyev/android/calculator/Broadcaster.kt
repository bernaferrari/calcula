package org.solovyev.android.calculator

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.widget.CalculatorWidget
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Broadcaster @Inject constructor(
    private val application: Application,
    preferences: SharedPreferences,
    bus: Bus,
    private val handler: Handler
) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _events = MutableSharedFlow<BroadcastEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<BroadcastEvent> = _events.asSharedFlow()

    init {
        preferences.registerOnSharedPreferenceChangeListener(this)
        bus.register(this)
        handler.postDelayed({
            // we must update the widget when app starts
            sendInitIntent()
        }, 100)
    }

    @Subscribe
    fun onEditorChanged(e: Editor.ChangedEvent) {
        sendBroadcastIntent(ACTION_EDITOR_STATE_CHANGED)
    }

    @Subscribe
    fun onDisplayChanged(e: Display.ChangedEvent) {
        sendBroadcastIntent(ACTION_DISPLAY_STATE_CHANGED)
    }

    @Subscribe
    fun onCursorMoved(e: Editor.CursorMovedEvent) {
        sendBroadcastIntent(ACTION_EDITOR_STATE_CHANGED)
    }

    fun sendInitIntent() {
        sendBroadcastIntent(ACTION_INIT)
    }

    fun sendBroadcastIntent(action: String) {
        val intent = Intent(action).apply {
            setClass(application, CalculatorWidget::class.java)
        }
        application.sendBroadcast(intent)

        scope.launch {
            _events.emit(BroadcastEvent(action))
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key != null && (Preferences.Gui.theme.isSameKey(key) || Preferences.Widget.theme.isSameKey(key))) {
            sendBroadcastIntent(ACTION_THEME_CHANGED)
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
