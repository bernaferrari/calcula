package org.solovyev.android.calculator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.text.TextUtils
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.buttons.CppButton
import org.solovyev.android.calculator.history.History
import javax.inject.Inject

@AndroidEntryPoint
class WidgetReceiver : BroadcastReceiver() {

    @Inject
    lateinit var keyboard: Keyboard

    @Inject
    lateinit var history: History

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (!TextUtils.equals(action, ACTION_BUTTON_PRESSED)) {
            return
        }

        // Hilt @AndroidEntryPoint handles injection automatically

        val buttonId = intent.getIntExtra(ACTION_BUTTON_ID_EXTRA, 0)
        val button = CppButton.getById(buttonId) ?: return

        if (history.loaded.value) {
            if (!keyboard.buttonPressed(button.action)) {
                // prevent vibrate
                return
            }
        } else {
            // if app has been killed we need first to restore the state and only after doing this
            // to apply actions. Otherwise, we will apply actions on the empty editor
            history.runWhenLoaded(MyRunnable(keyboard, button.action))
        }

        vibrate(context)
    }

    private fun vibrate(context: Context) {
        if (!keyboard.vibrateOnKeypress.value) {
            return
        }
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        vibrator.vibrate(10)
    }

    private class MyRunnable(
        private val keyboard: Keyboard,
        private val action: String
    ) : Runnable {
        override fun run() {
            keyboard.buttonPressed(action)
        }
    }

    companion object {
        const val ACTION_BUTTON_ID_EXTRA = "buttonId"
        const val ACTION_BUTTON_PRESSED = "org.solovyev.android.calculator.BUTTON_PRESSED"

        fun newButtonClickedIntent(context: Context, button: CppButton): Intent {
            val intent = Intent(context, WidgetReceiver::class.java)
            intent.action = ACTION_BUTTON_PRESSED
            intent.putExtra(ACTION_BUTTON_ID_EXTRA, button.id)
            return intent
        }
    }
}
