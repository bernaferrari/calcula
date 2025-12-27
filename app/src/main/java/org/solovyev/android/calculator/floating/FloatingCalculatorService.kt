package org.solovyev.android.calculator.floating

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.solovyev.android.Check
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Display
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ga.Ga
import org.solovyev.android.calculator.di.AppPreferences
import javax.inject.Inject

@AndroidEntryPoint
class FloatingCalculatorService : Service(), FloatingViewListener {

    private var view: FloatingCalculatorView? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observing = false

    @Inject
    lateinit var editor: Editor

    @Inject
    lateinit var display: Display

    @Inject
    lateinit var ga: Ga

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Hilt @AndroidEntryPoint handles injection automatically
        observeStateChanges()
    }

    override fun onDestroy() {
        view?.let {
            it.hide()
            view = null
        }
        scope.cancel()
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
        val screenSize = calculateScreenSize(wm, dm)
        val maxWidth = 2 * minOf(screenSize.first, screenSize.second) / 3
        val desiredWidth = App.toPixels(dm, 300f)

        val width = minOf(maxWidth, desiredWidth)
        val height = getHeight(width)

        val state = FloatingCalculatorView.State(width, height, -1, -1)
        view = FloatingCalculatorView(this, state, this)
        view?.show()
        view?.updateEditorState(editor.state)
        view?.updateDisplayState(display.stateFlow.value)

        observeThemeChanges()
    }

    private fun getHeight(width: Int): Int = 4 * width / 3

    private fun calculateScreenSize(wm: WindowManager, dm: DisplayMetrics): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            dm.widthPixels = bounds.width()
            dm.heightPixels = bounds.height()
            dm.widthPixels to dm.heightPixels
        } else {
            dm.setTo(resources.displayMetrics)
            dm.widthPixels to dm.heightPixels
        }
    }

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

    private fun observeThemeChanges() {
        scope.launch {
            appPreferences.settings.theme.collect {
                restartForThemeChange()
            }
        }
        scope.launch {
            appPreferences.settings.onscreenTheme.collect {
                restartForThemeChange()
            }
        }
    }

    private fun restartForThemeChange() {
        Check.isNotNull(view)
        stopSelf()
        show(this)
    }

    private fun observeStateChanges() {
        if (observing) return
        observing = true
        scope.launch {
            editor.stateFlow.collect { state ->
                view?.updateEditorState(state)
            }
        }
        scope.launch {
            display.stateFlow.collect { state ->
                view?.updateDisplayState(state)
            }
        }
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
