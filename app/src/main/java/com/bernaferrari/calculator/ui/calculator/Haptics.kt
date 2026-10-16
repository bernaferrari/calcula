package com.bernaferrari.calculator.ui.calculator

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Stable
class Haptics(private val view: View, private val enabled: () -> Boolean) {
    fun tap() = perform(HapticFeedbackConstants.KEYBOARD_TAP)

    fun tick() = perform(HapticFeedbackConstants.CLOCK_TICK)

    fun heavy() = perform(HapticFeedbackConstants.LONG_PRESS)

    fun confirm() = perform(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY)

    fun reject() = perform(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS)

    private fun perform(type: Int) {
        if (enabled()) view.performHapticFeedback(type)
    }
}

@Composable
fun rememberHaptics(enabled: () -> Boolean): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view, enabled) }
}
