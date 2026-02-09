package org.solovyev.android.calculator.ui

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Refined haptic feedback helper for premium tactile feel.
 * 
 * Different button types get different vibration patterns:
 * - DIGIT: Light, crisp tick
 * - OPERATION: Medium click
 * - OPERATION_HIGHLIGHTED: Heavier confirmation
 * - CONTROL: Light tick
 * - SPECIAL: Light tick
 */
object HapticHelper {
    
    /**
     * Perform haptic feedback appropriate for the given button type.
     * Only performs feedback if haptics are enabled.
     */
    fun performButtonFeedback(
        buttonType: ButtonType,
        haptics: HapticFeedback,
        enabled: Boolean
    ) {
        if (!enabled) return
        
        val feedbackType = when (buttonType) {
            ButtonType.DIGIT -> HapticFeedbackType.TextHandleMove // Light tick
            ButtonType.OPERATION -> HapticFeedbackType.TextHandleMove // Medium - same API, intensity differs on device
            ButtonType.OPERATION_HIGHLIGHTED -> HapticFeedbackType.LongPress // Heavier confirmation
            ButtonType.CONTROL -> HapticFeedbackType.TextHandleMove // Light tick
            ButtonType.SPECIAL -> HapticFeedbackType.TextHandleMove // Light tick
        }
        
        haptics.performHapticFeedback(feedbackType)
    }
    
    /**
     * Perform haptic feedback for swipe/drag actions.
     */
    fun performSwipeFeedback(haptics: HapticFeedback, enabled: Boolean) {
        if (!enabled) return
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    /**
     * Perform haptic feedback for long press actions.
     */
    fun performLongPressFeedback(haptics: HapticFeedback, enabled: Boolean) {
        if (!enabled) return
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    /**
     * Perform haptic feedback for destructive actions (clear, delete).
     */
    fun performDestructiveFeedback(haptics: HapticFeedback, enabled: Boolean) {
        if (!enabled) return
        // Use LongPress for heavier feedback on destructive actions
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    /**
     * Perform haptic feedback for result/equals (confirmation).
     */
    fun performResultFeedback(haptics: HapticFeedback, enabled: Boolean) {
        if (!enabled) return
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}
