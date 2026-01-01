package org.solovyev.android.calculator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.TextUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.solovyev.android.calculator.buttons.CppButton

class WidgetReceiver : BroadcastReceiver(), KoinComponent {

    private val keyboard: Keyboard by inject()

    override fun onReceive(context: Context, intent: Intent) {
        handleIntent(context, intent)
    }

    internal fun handleIntent(context: Context, intent: Intent) {
        val action = intent.action
        if (!TextUtils.equals(action, ACTION_BUTTON_PRESSED)) {
            return
        }

        val buttonAction = intent.getStringExtra(ACTION_BUTTON_ACTION_EXTRA)
        val button = if (buttonAction != null) CppButton.getByAction(buttonAction) else null
        if (button == null) return

        if (keyboard.buttonPressed(button.action)) {
            vibrate(context)
        }
    }

    private fun vibrate(context: Context) {
        // Check if vibration is enabled - simplified for now
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        } ?: return
        val effect = VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }

    companion object {
        const val ACTION_BUTTON_ACTION_EXTRA = "buttonAction"
        const val ACTION_BUTTON_PRESSED = "org.solovyev.android.calculator.BUTTON_PRESSED"

        fun newButtonClickedIntent(context: Context, button: CppButton): Intent {
            val intent = Intent(context, WidgetReceiver::class.java)
            intent.action = ACTION_BUTTON_PRESSED
            intent.putExtra(ACTION_BUTTON_ACTION_EXTRA, button.action)
            return intent
        }
    }
}
