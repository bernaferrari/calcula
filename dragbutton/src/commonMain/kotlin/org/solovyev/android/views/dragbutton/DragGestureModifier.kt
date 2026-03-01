package org.solovyev.android.views.dragbutton

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.time.TimeSource

/**
 * Refined gesture configuration for responsive yet accurate detection.
 */
object GestureConfig {
    // Minimum distance to trigger a swipe - balanced for accuracy
    val MinSwipeDistance: Dp = 28.dp
    
    // Maximum time for a swipe gesture (faster = more responsive)
    const val MaxSwipeDurationMs: Long = 400
    
    // Minimum time to distinguish from accidental taps
    const val MinSwipeDurationMs: Long = 50
    
    // Angle tolerance - must be more vertical than horizontal
    const val VerticalAngleThreshold: Float = 0.5f  // |dy| > 0.5 * |dx|
    
    // Velocity threshold for snappy feel
    const val MinVelocity: Float = 150f  // dp per second
}

/**
 * Enum representing refined swipe directions
 */
enum class SwipeDirection {
    UP, DOWN, LEFT, RIGHT, NONE
}

/**
 * Data class containing gesture result information
 */
data class GestureResult(
    val direction: SwipeDirection,
    val distance: Float,
    val velocity: Float,
    val duration: Long
)

/**
 * Analyzes a gesture from start to end point.
 * Returns null if gesture doesn't meet thresholds.
 */
fun analyzeGesture(
    start: Offset,
    end: Offset,
    duration: Long,
    density: androidx.compose.ui.unit.Density
): GestureResult? {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val distancePx = kotlin.math.hypot(dx, dy)
    val distanceDp = with(density) { distancePx.toDp() }
    
    // Check minimum distance
    if (distanceDp < GestureConfig.MinSwipeDistance) return null
    
    // Check duration constraints
    if (duration !in GestureConfig.MinSwipeDurationMs..GestureConfig.MaxSwipeDurationMs) return null
    
    // Calculate velocity
    val velocity = if (duration > 0) {
        (distanceDp.value * 1000f) / duration
    } else 0f
    
    // Determine direction with angle check
    val direction = when {
        abs(dy) > abs(dx) * (1 / GestureConfig.VerticalAngleThreshold) -> {
            if (dy < 0) SwipeDirection.UP else SwipeDirection.DOWN
        }
        abs(dx) > abs(dy) * (1 / GestureConfig.VerticalAngleThreshold) -> {
            if (dx < 0) SwipeDirection.LEFT else SwipeDirection.RIGHT
        }
        else -> SwipeDirection.NONE
    }
    
    if (direction == SwipeDirection.NONE) return null
    
    return GestureResult(direction, distanceDp.value, velocity, duration)
}

/**
 * Enhanced directional drag gesture detector with refined thresholds.
 * Provides smooth, accurate gesture recognition.
 *
 * @param onSwipe Callback invoked with detected [SwipeDirection]. Return true to consume.
 * @param onGesture Optional callback with detailed gesture result.
 */
fun Modifier.detectRefinedSwipe(
    onSwipe: (SwipeDirection) -> Boolean,
    onGesture: ((GestureResult) -> Unit)? = null
): Modifier = composed {
    val hapticFeedback = LocalHapticFeedback.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    pointerInput(onSwipe, onGesture) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val start = down.position
            val startTime = TimeSource.Monotonic.markNow()
            
            val up = waitForUpOrCancellation()
            
            if (up != null) {
                val end = up.position
                val duration = startTime.elapsedNow().inWholeMilliseconds
                
                val result = analyzeGesture(start, end, duration, density)
                
                if (result != null) {
                    val consumed = onSwipe(result.direction)
                    if (consumed) {
                        up.consume()
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                        onGesture?.invoke(result)
                    }
                }
            }
        }
    }
}

/**
 * Visual feedback modifier that shows gesture preview.
 * Used for gesture discovery - visual communication without words.
 */
fun Modifier.gesturePreview(
    isPressed: Boolean,
    swipeUpAvailable: Boolean,
    swipeDownAvailable: Boolean
): Modifier = composed {
    var showUpHint by remember { mutableStateOf(false) }
    var showDownHint by remember { mutableStateOf(false) }
    
    val upHintAlpha by animateFloatAsState(
        targetValue = if (showUpHint && swipeUpAvailable) 0.5f else 0f,
        animationSpec = tween(150, easing = EaseOutQuart)
    )
    
    val downHintAlpha by animateFloatAsState(
        targetValue = if (showDownHint && swipeDownAvailable) 0.5f else 0f,
        animationSpec = tween(150, easing = EaseOutQuart)
    )
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            showUpHint = true
            showDownHint = true
        } else {
            showUpHint = false
            showDownHint = false
        }
    }
    
    this.graphicsLayer {
        alpha = 1f
    }
}

/**
 * Legacy modifier - kept for compatibility.
 * Uses refined detection thresholds.
 */
fun Modifier.detectDirectionalDrag(
    minDistance: Dp = GestureConfig.MinSwipeDistance,
    vibrateOnDrag: Boolean = true,
    onDrag: (DragDirection) -> Boolean
): Modifier = composed {
    val hapticFeedback = LocalHapticFeedback.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    pointerInput(minDistance, vibrateOnDrag, onDrag) {
        val minDistancePx = minDistance.toPx()
        
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val start = down.position
            val downMark = TimeSource.Monotonic.markNow()
            
            val up = waitForUpOrCancellation()
            
            if (up != null) {
                val end = up.position
                val duration = downMark.elapsedNow().inWholeMilliseconds
                
                // Use refined duration constraints
                if (duration in GestureConfig.MinSwipeDurationMs..GestureConfig.MaxSwipeDurationMs) {
                    val distance = Drag.distance(start, end)
                    
                    if (distance >= minDistancePx) {
                        val direction = Drag.getDirection(start, end)
                        
                        if (direction != null) {
                            val consumed = onDrag(direction)
                            if (consumed) {
                                up.consume()
                                if (vibrateOnDrag) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
