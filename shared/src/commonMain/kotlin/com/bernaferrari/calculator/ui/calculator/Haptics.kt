package com.bernaferrari.calculator.ui.calculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.bernaferrari.calculator.platform.KeySound
import com.bernaferrari.calculator.platform.rememberKeySounds

/** Key feedback: haptics, plus optional key-click sounds. */
@Stable
class Haptics(
    private val haptic: HapticFeedback,
    private val sound: (KeySound) -> Unit,
    private val enabled: () -> Boolean,
    private val sounds: () -> Boolean,
) {
    fun tap() = perform(HapticFeedbackType.KeyboardTap, KeySound.Tap)

    fun tick() = perform(HapticFeedbackType.SegmentFrequentTick, null)

    fun heavy() = perform(HapticFeedbackType.LongPress, KeySound.Delete)

    fun confirm() = perform(HapticFeedbackType.Confirm, KeySound.Equals)

    fun reject() = perform(HapticFeedbackType.Reject, KeySound.Error)

    private fun perform(type: HapticFeedbackType, keySound: KeySound?) {
        if (enabled()) haptic.performHapticFeedback(type)
        if (keySound != null && sounds()) sound(keySound)
    }
}

@Composable
fun rememberHaptics(enabled: () -> Boolean, sounds: () -> Boolean = { false }): Haptics {
    val haptic = LocalHapticFeedback.current
    val sound = rememberKeySounds()
    return remember(haptic, sound) { Haptics(haptic, sound, enabled, sounds) }
}

/** Set when the person asked for less motion: flips, squeezes and roll-ups become simple fades. */
val LocalReduceMotion = staticCompositionLocalOf { false }
