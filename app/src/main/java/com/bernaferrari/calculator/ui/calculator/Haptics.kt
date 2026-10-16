package com.bernaferrari.calculator.ui.calculator

import android.media.AudioManager
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView

/** Key feedback: haptics always available, plus optional key-click sounds. */
@Stable
class Haptics(private val view: View, private val enabled: () -> Boolean, private val sounds: () -> Boolean) {
    private val audio = view.context.getSystemService(AudioManager::class.java)

    fun tap() = perform(HapticFeedbackConstants.KEYBOARD_TAP, AudioManager.FX_KEYPRESS_STANDARD)

    fun tick() = perform(HapticFeedbackConstants.CLOCK_TICK, null)

    fun heavy() = perform(HapticFeedbackConstants.LONG_PRESS, AudioManager.FX_KEYPRESS_DELETE)

    fun confirm() = perform(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY, AudioManager.FX_KEYPRESS_RETURN)

    fun reject() = perform(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS, AudioManager.FX_KEYPRESS_INVALID)

    private fun perform(type: Int, sound: Int?) {
        if (enabled()) view.performHapticFeedback(type)
        if (sound != null && sounds()) audio?.playSoundEffect(sound, 0.6f)
    }
}

@Composable
fun rememberHaptics(enabled: () -> Boolean, sounds: () -> Boolean = { false }): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view, enabled, sounds) }
}

/** Set when the person asked for less motion: flips, squeezes and roll-ups become simple fades. */
val LocalReduceMotion = staticCompositionLocalOf { false }
