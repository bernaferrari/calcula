package org.solovyev.android.calculator.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Unified calculator button with clean, minimal styling.
 * Swipe gestures are supported but hints are NOT shown by default to reduce visual noise.
 */
@Composable
fun UnifiedButton(
    text: String,
    buttonType: ButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: Painter? = null,
    onLongClick: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = 28.sp,
    cornerRadius: Dp = 16.dp,
    fontWeight: FontWeight = FontWeight.Normal
) {
    var isPressed by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val hapticsEnabled = LocalCalculatorHapticsEnabled.current
    val density = LocalDensity.current
    
    val longPressTimeout = 400L
    val touchSlop = with(density) { 8.dp.toPx() }
    val minDragDistancePx = with(density) { 30.dp.toPx() }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ButtonScale"
    )

    // Unified color scheme
    val (backgroundColor, textColor) = when (buttonType) {
        ButtonType.DIGIT -> {
            MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurface
        }
        ButtonType.OPERATION -> {
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }
        ButtonType.OPERATION_HIGHLIGHTED -> {
            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        }
        ButtonType.CONTROL -> {
            MaterialTheme.colorScheme.surfaceContainer to MaterialTheme.colorScheme.onSurfaceVariant
        }
        ButtonType.SPECIAL -> {
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        }
    }

    val shape = RoundedCornerShape(cornerRadius)
    val effectiveTextColor = if (enabled) textColor else textColor.copy(alpha = 0.45f)
    val effectiveBackgroundColor = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(effectiveBackgroundColor)
            .pointerInput(enabled, onClick, onLongClick, onSwipeUp, onSwipeDown, onSwipeLeft, onSwipeRight) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isPressed = true
                    val start = down.position
                    val downTime = down.uptimeMillis
                    var lastPos = start
                    var movedBeyondSlop = false
                    var actionHandled = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.changedToUpIgnoreConsumed()) {
                            if (!enabled) {
                                HapticHelper.performSwipeFeedback(haptics, hapticsEnabled)
                                isPressed = false
                                break
                            }
                            if (!actionHandled) {
                                val delta = lastPos - start
                                val distance = delta.getDistance()

                                // Check for swipe
                                if (distance >= minDragDistancePx) {
                                    val swipeHandled = handleSwipe(
                                        delta = delta,
                                        onSwipeUp = onSwipeUp,
                                        onSwipeDown = onSwipeDown,
                                        onSwipeLeft = onSwipeLeft,
                                        onSwipeRight = onSwipeRight,
                                        haptics = haptics,
                                        hapticsEnabled = hapticsEnabled
                                    )
                                    if (swipeHandled) {
                                        actionHandled = true
                                    }
                                }

                                // If no swipe handled, perform click
                                if (!actionHandled) {
                                    if (hapticsEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onClick()
                                }
                            }
                            isPressed = false
                            break
                        }

                        if (change.positionChanged()) {
                            lastPos = change.position
                            if (!movedBeyondSlop) {
                                movedBeyondSlop = (lastPos - start).getDistance() > touchSlop
                            }
                            change.consume()
                        }

                        // Long press detection
                        if (enabled && !actionHandled && !movedBeyondSlop && onLongClick != null) {
                            if ((change.uptimeMillis - downTime) >= longPressTimeout) {
                                actionHandled = true
                                if (hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onLongClick()
                            }
                        }
                    }
                    if (isPressed) {
                        isPressed = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = text.takeIf { it.isNotEmpty() },
                tint = effectiveTextColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = fontSize,
                    color = effectiveTextColor,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center,
                    fontFamily = CalculatorFontFamily
                )
            )
        }
    }
}

private fun handleSwipe(
    delta: androidx.compose.ui.geometry.Offset,
    onSwipeUp: (() -> Unit)?,
    onSwipeDown: (() -> Unit)?,
    onSwipeLeft: (() -> Unit)?,
    onSwipeRight: (() -> Unit)?,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    hapticsEnabled: Boolean
): Boolean {
    val x = delta.x
    val y = delta.y
    
    return when {
        kotlin.math.abs(y) > kotlin.math.abs(x) -> {
            if (y < 0 && onSwipeUp != null) {
                if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSwipeUp()
                true
            } else if (y > 0 && onSwipeDown != null) {
                if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSwipeDown()
                true
            } else false
        }
        else -> {
            if (x < 0 && onSwipeLeft != null) {
                if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSwipeLeft()
                true
            } else if (x > 0 && onSwipeRight != null) {
                if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSwipeRight()
                true
            } else false
        }
    }
}
