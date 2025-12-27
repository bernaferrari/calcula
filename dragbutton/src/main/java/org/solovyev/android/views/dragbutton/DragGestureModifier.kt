package org.solovyev.android.views.dragbutton

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.composed

/**
 * Default minimum distance for a drag gesture to be recognized.
 */
val DefaultMinDragDistance: Dp = 48.dp

/**
 * Modifier that detects directional drag gestures.
 *
 * @param minDistance The minimum distance (in Dp) for a drag to be recognized.
 * @param vibrateOnDrag Whether to provide haptic feedback on successful drag.
 * @param onDrag Callback invoked with the detected [DragDirection]. Return true to consume the event.
 */
fun Modifier.detectDirectionalDrag(
    minDistance: Dp = DefaultMinDragDistance,
    vibrateOnDrag: Boolean = true,
    onDrag: (DragDirection) -> Boolean
): Modifier = composed {
    val hapticFeedback = LocalHapticFeedback.current
    
    pointerInput(minDistance, vibrateOnDrag, onDrag) {
        val minDistancePx = minDistance.toPx()
        
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val start = down.position
            val downTime = System.currentTimeMillis()
            
            val up = waitForUpOrCancellation()
            
            if (up != null) {
                val end = up.position
                val duration = System.currentTimeMillis() - downTime
                
                // Check duration constraints (40ms - 2500ms)
                if (duration in 40..2500) {
                    val distance = Drag.distance(start, end)
                    
                    // Check distance constraint
                    if (distance >= minDistancePx) {
                        val direction = Drag.getDirection(start, end)
                        
                        if (direction != null) {
                            val consumed = onDrag(direction)
                            if (consumed) {
                                up.consume()
                                if (vibrateOnDrag) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
