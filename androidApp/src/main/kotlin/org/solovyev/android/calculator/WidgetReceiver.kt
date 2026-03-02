package org.solovyev.android.calculator

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
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
import org.solovyev.android.calculator.widgets.WidgetUpdateWorker

class WidgetReceiver : BroadcastReceiver(), KoinComponent {

    private val keyboard: Keyboard by inject()
    private val display: Display by inject()

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

        val handled = when (button) {
            CppButton.app -> {
                launchCalculator(context, CalculatorActivity.newIntent(context))
                true
            }
            CppButton.settings,
            CppButton.settings_widget -> {
                launchCalculator(context, CalculatorActivity.newIntentForSettings(context))
                true
            }
            CppButton.history -> {
                launchCalculator(context, CalculatorActivity.newIntentForHistory(context))
                true
            }
            CppButton.vars -> {
                launchCalculator(context, CalculatorActivity.newIntentForVariables(context))
                true
            }
            CppButton.functions -> {
                launchCalculator(context, CalculatorActivity.newIntentForFunctions(context))
                true
            }
            CppButton.operators,
            CppButton.memory -> {
                launchCalculator(context, CalculatorActivity.newIntent(context))
                true
            }
            CppButton.copy -> copyDisplayToClipboard(context)
            CppButton.paste -> pasteFromClipboard(context)
            else -> keyboard.buttonPressed(button.action)
        }

        if (handled) {
            vibrate(context)
            WidgetUpdateWorker.triggerImmediateUpdate(context)
        }
    }

    private fun launchCalculator(context: Context, intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun copyDisplayToClipboard(context: Context): Boolean {
        val state = display.getState()
        if (!state.valid || state.text.isBlank()) return false

        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return false
        clipboard.setPrimaryClip(ClipData.newPlainText("Calculator++", state.text))
        return true
    }

    private fun pasteFromClipboard(context: Context): Boolean {
        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return false
        val pasted = clipboard.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
            .orEmpty()
            .trim()
        if (pasted.isBlank()) return false

        return keyboard.buttonPressed(pasted)
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
