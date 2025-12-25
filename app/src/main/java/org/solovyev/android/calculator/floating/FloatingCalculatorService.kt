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

package org.solovyev.android.calculator.floating

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.Check
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Display
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ga.Ga
import javax.inject.Inject

@AndroidEntryPoint
class FloatingCalculatorService : Service(), FloatingViewListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var view: FloatingCalculatorView? = null

    @Inject
    lateinit var bus: Bus

    @Inject
    lateinit var editor: Editor

    @Inject
    lateinit var display: Display

    @Inject
    lateinit var ga: Ga

    @Inject
    lateinit var preferences: SharedPreferences

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Hilt @AndroidEntryPoint handles injection automatically
    }

    override fun onDestroy() {
        view?.let {
            preferences.unregisterOnSharedPreferenceChangeListener(this)
            bus.unregister(this)
            it.hide()
            view = null
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        handleStart(intent)
        return result
    }

    private fun handleStart(intent: Intent?) {
        intent ?: return

        when {
            isShowWindowIntent(intent) -> {
                hideNotification()
                createView()
                ga.onFloatingCalculatorOpened()
            }
            isShowNotificationIntent(intent) -> {
                showNotification()
            }
        }
    }

    private fun createView() {
        if (view != null) {
            return
        }

        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = resources.displayMetrics
        val dd = wm.defaultDisplay

        @Suppress("DEPRECATION")
        val maxWidth = 2 * minOf(dd.width, dd.height) / 3
        val desiredWidth = App.toPixels(dm, 300f)

        val width = minOf(maxWidth, desiredWidth)
        val height = getHeight(width)

        val state = FloatingCalculatorView.State(width, height, -1, -1)
        view = FloatingCalculatorView(this, state, this)
        view?.show()
        view?.updateEditorState(editor.getState())
        view?.updateDisplayState(display.getState())

        bus.register(this)
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun getHeight(width: Int): Int = 4 * width / 3

    private fun isShowWindowIntent(intent: Intent): Boolean =
        intent.action == SHOW_WINDOW_ACTION

    private fun isShowNotificationIntent(intent: Intent): Boolean =
        intent.action == SHOW_NOTIFICATION_ACTION

    private fun hideNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID)
    }

    override fun onViewMinimized() {
        showNotification()
        stopSelf()
    }

    override fun onViewHidden() {
        stopSelf()
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.cpp_floating_calculator_notification_name)
            val description = getString(R.string.cpp_floating_calculator_notification_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                this.description = description
                setShowBadge(false)
                setSound(null, null)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        builder.setSmallIcon(R.drawable.kb_logo)
        builder.setContentTitle(getText(R.string.cpp_app_name))
        builder.setContentText(getString(R.string.open_onscreen_calculator))
        builder.setSilent(true)
        builder.setOngoing(true)

        val intent = createShowWindowIntent(this)
        builder.setContentIntent(
            PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, builder.build())
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Check.isNotNull(view)
        if (Preferences.Gui.theme.isSameKey(key ?: "") || Preferences.Onscreen.theme.isSameKey(key ?: "")) {
            stopSelf()
            show(this)
        }
    }

    @Subscribe
    fun onEditorChanged(e: Editor.ChangedEvent) {
        Check.isNotNull(view)
        view?.updateEditorState(e.newState)
    }

    @Subscribe
    fun onCursorMoved(e: Editor.CursorMovedEvent) {
        Check.isNotNull(view)
        view?.updateEditorState(e.state)
    }

    @Subscribe
    fun onDisplayChanged(e: Display.ChangedEvent) {
        Check.isNotNull(view)
        view?.updateDisplayState(e.newState)
    }

    companion object {
        private const val SHOW_WINDOW_ACTION = "org.solovyev.android.calculator.floating.SHOW_WINDOW"
        private const val SHOW_NOTIFICATION_ACTION = "org.solovyev.android.calculator.floating.SHOW_NOTIFICATION"
        private const val NOTIFICATION_CHANNEL_ID = "floating_window"
        private const val NOTIFICATION_ID = 9031988 // my birthday =)

        fun show(context: Context) {
            context.sendBroadcast(createShowWindowIntent(context))
        }

        private fun createShowWindowIntent(context: Context): Intent {
            val intent = Intent(SHOW_WINDOW_ACTION)
            intent.setClass(context, FloatingCalculatorBroadcastReceiver::class.java)
            return intent
        }
    }
}
