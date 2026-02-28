package org.solovyev.android.calculator.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Refined haptic feedback helper for premium tactile feel with accessibility support.
 *
 * Different button types get different vibration patterns:
 * - DIGIT: Light, crisp tick
 * - OPERATION: Medium click
 * - OPERATION_HIGHLIGHTED: Heavier confirmation
 * - CONTROL: Light tick
 * - SPECIAL: Light tick
 *
 * Extended haptics mode provides additional feedback for improved accessibility.
 */
object HapticHelper {

    /**
     * Perform haptic feedback appropriate for the given button type.
     * Only performs feedback if haptics are enabled.
     *
     * @param buttonType The type of button pressed
     * @param haptics The haptic feedback controller
     * @param enabled Whether haptics are enabled
     * @param extended Whether extended haptics (stronger feedback) should be used for accessibility
     */
    fun performButtonFeedback(
        buttonType: ButtonType,
        haptics: HapticFeedback,
        enabled: Boolean,
        extended: Boolean = false
    ) {
        if (!enabled) return

        val feedbackType = when {
            extended -> HapticFeedbackType.LongPress // Stronger feedback for extended mode
            buttonType == ButtonType.OPERATION_HIGHLIGHTED -> HapticFeedbackType.LongPress
            else -> HapticFeedbackType.TextHandleMove
        }

        haptics.performHapticFeedback(feedbackType)

        // In extended mode, also perform additional feedback for operation buttons
        if (extended && (buttonType == ButtonType.OPERATION || buttonType == ButtonType.OPERATION_HIGHLIGHTED)) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /**
     * Perform haptic feedback for swipe/drag actions.
     *
     * @param extended Whether extended haptics should be used
     */
    fun performSwipeFeedback(haptics: HapticFeedback, enabled: Boolean, extended: Boolean = false) {
        if (!enabled) return
        val feedbackType = if (extended) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
        haptics.performHapticFeedback(feedbackType)
    }

    /**
     * Perform haptic feedback for long press actions.
     *
     * @param extended Whether extended haptics should be used
     */
    fun performLongPressFeedback(haptics: HapticFeedback, enabled: Boolean, extended: Boolean = false) {
        if (!enabled) return
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        if (extended) {
            // Double pulse for extended mode
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /**
     * Perform haptic feedback for destructive actions (clear, delete).
     *
     * @param extended Whether extended haptics should be used
     */
    fun performDestructiveFeedback(haptics: HapticFeedback, enabled: Boolean, extended: Boolean = false) {
        if (!enabled) return
        // Use LongPress for heavier feedback on destructive actions
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        if (extended) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /**
     * Perform haptic feedback for result/equals (confirmation).
     *
     * @param extended Whether extended haptics should be used
     */
    fun performResultFeedback(haptics: HapticFeedback, enabled: Boolean, extended: Boolean = false) {
        if (!enabled) return
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        if (extended) {
            // Success pattern: two quick pulses
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /**
     * Perform haptic feedback for error states.
     * Available only in extended mode.
     */
    fun performErrorFeedback(haptics: HapticFeedback, enabled: Boolean, extended: Boolean = false) {
        if (!enabled || !extended) return
        // Error pattern: three quick pulses
        repeat(3) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /**
     * Perform haptic feedback for focus changes (for switch access scanning).
     * Available only in extended mode.
     */
    fun performFocusFeedback(haptics: HapticFeedback, enabled: Boolean, extended: Boolean = false) {
        if (!enabled || !extended) return
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
}

/**
 * Composable helper that gets the current extended haptics setting.
 */
@Composable
fun rememberExtendedHapticsEnabled(): Boolean {
    return LocalCalculatorExtendedHaptics.current
}
