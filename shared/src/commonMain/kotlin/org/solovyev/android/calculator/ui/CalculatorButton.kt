package org.solovyev.android.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.PI
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer


/**
 * Direction texts for swipe actions on calculator buttons
 */
data class DirectionTexts(
    val up: String? = null,
    val down: String? = null,
    val left: String? = null,
    val right: String? = null
)

/**
 * Button type determines the styling
 */
enum class ButtonType {
    DIGIT,
    OPERATION,
    OPERATION_HIGHLIGHTED,
    CONTROL,
    SPECIAL,
    MEMORY
}

/**
 * Calculator button component supporting:
 * - Main text (center)
 * - Direction texts (up, down, left, right) for swipe actions
 * - Different button types (number, operator, function, special)
 * - Click and long-press support
 * - Drag gestures for directional actions
 */
@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.DIGIT,
    directionTexts: DirectionTexts = DirectionTexts(),
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    textSize: TextUnit = 30.sp,
    backgroundOverride: Color? = null,
    directionTextScale: Float = 0.35f,
    directionTextAlpha: Float = 0.7f,
    icon: Painter? = null,
    iconTint: Color? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    // Platform-neutral configuration (assuming standard timeout/slop)
    // In a real app, we might want to expose these via CompositionLocal or platform-specific expectation
    val longPressTimeout = 500L // Standard Android long press timeout
    val touchSlop = with(LocalDensity.current) { 8.dp.toPx() } // Approximate touch slop
    
    val haptics = LocalHapticFeedback.current
    val highContrast = LocalCalculatorHighContrast.current
    val hapticsEnabled = LocalCalculatorHapticsEnabled.current
    val minDragDistancePx = with(LocalDensity.current) { 15.dp.toPx() }
    
    val density = LocalDensity.current
    val directionPaddingUpDown = 4.dp // From dimens.xml
    val directionPaddingLeft = 2.dp   // From dimens.xml
    val directionTextMinSizePx = with(density) { 9.dp.toPx() }
    val directionTextPaddingUpDownPx = with(density) { directionPaddingUpDown.toPx() }
    val directionTextPaddingLeftPx = with(density) { directionPaddingLeft.toPx() }
    val baseTextSizePx = with(density) { textSize.toPx() }
    val directionTextSizePx = max(baseTextSizePx * directionTextScale, directionTextMinSizePx)
    
    // TextMeasurer for drawing direction texts using Compose APIs
    val textMeasurer = rememberTextMeasurer()

    // Expressive shapes based on Material 3 guidelines
    val shape = when (buttonType) {
        ButtonType.DIGIT -> CircleShape // Pill/Circle for digits
        ButtonType.OPERATION, ButtonType.CONTROL -> RoundedCornerShape(16.dp) // Squircle for ops
        ButtonType.OPERATION_HIGHLIGHTED, ButtonType.SPECIAL -> MaterialTheme.shapes.extraLarge // Special shape
        ButtonType.MEMORY -> RoundedCornerShape(16.dp)
    }
    
    // Enhanced spring spec for "bouncy" feel
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f, 
        animationSpec = spring(
            dampingRatio = 0.4f, // Bouncier
            stiffness = 400f // Slightly less stiff than Medium
        ),
        label = "ButtonScale"
    )

    // Color logic
    val backgroundColor = backgroundOverride ?: when (buttonType) {
        ButtonType.DIGIT -> MaterialTheme.colorScheme.surfaceContainerHigh
        ButtonType.OPERATION -> MaterialTheme.colorScheme.secondaryContainer
        ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.tertiaryContainer
        ButtonType.CONTROL -> MaterialTheme.colorScheme.surfaceVariant
        ButtonType.SPECIAL -> MaterialTheme.colorScheme.primaryContainer
        ButtonType.MEMORY -> MaterialTheme.colorScheme.secondaryContainer
    }

    val effectiveTextColor = when (buttonType) {
        ButtonType.DIGIT -> MaterialTheme.colorScheme.onSurface
        ButtonType.OPERATION -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.onTertiaryContainer
        ButtonType.CONTROL -> MaterialTheme.colorScheme.onSurfaceVariant
        ButtonType.SPECIAL -> MaterialTheme.colorScheme.onPrimaryContainer
        ButtonType.MEMORY -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val directionTextStyle = TextStyle(
        fontSize = directionTextSizePx.sp,
        color = effectiveTextColor.copy(alpha = directionTextAlpha),
        textAlign = TextAlign.Center,
        fontFamily = CalculatorFontFamily
    )

    Box(
        modifier = modifier
            .padding(2.dp) // Internal padding for individual button spacing if needed
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (isPressed) 2.dp.toPx() else 0f
                this.shape = shape
                clip = true
            }
            .background(backgroundColor)
            .pointerInput(
                onClick,
                onLongClick,
                onSwipeUp,
                onSwipeDown,
                onSwipeLeft,
                onSwipeRight
            ) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isPressed = true
                    val start = down.position
                    val downTime = down.uptimeMillis
                    var lastPos = start
                    var movedBeyondSlop = false
                    var longPressFired = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.changedToUpIgnoreConsumed()) {
                            val delta = lastPos - start
                            val distance = delta.getDistance()

                            if (distance >= minDragDistancePx && !longPressFired) {
                                val direction = dragDirection(delta)
                                val handled = when (direction) {
                                    DragDirection.Up -> onSwipeUp?.let { it(); true } ?: false
                                    DragDirection.Down -> onSwipeDown?.let { it(); true } ?: false
                                    DragDirection.Left -> onSwipeLeft?.let { it(); true } ?: false
                                    DragDirection.Right -> onSwipeRight?.let { it(); true } ?: false
                                }
                                if (handled) {
                                    HapticHelper.performSwipeFeedback(haptics, hapticsEnabled)
                                    isPressed = false
                                    break
                                }
                                isPressed = false
                                break
                            }

                            if (!longPressFired) {
                                HapticHelper.performButtonFeedback(buttonType, haptics, hapticsEnabled)
                                onClick()
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

                        if (!longPressFired &&
                            !movedBeyondSlop &&
                            (change.uptimeMillis - downTime) >= longPressTimeout
                        ) {
                            longPressFired = true
                            HapticHelper.performLongPressFeedback(haptics, hapticsEnabled)
                            onLongClick()
                        }
                    }
                    if (isPressed) {
                        isPressed = false
                    }
                }
            }
            .drawWithContent {
                drawContent()

                fun drawDirectionText(value: String, direction: DragDirection) {
                    if (value.isEmpty()) return
                    
                    val textLayoutResult = textMeasurer.measure(
                        text = value,
                        style = directionTextStyle
                    )
                    
                    val textWidth = textLayoutResult.size.width
                    val textHeight = textLayoutResult.size.height
                    
                    val (x, y) = when (direction) {
                        DragDirection.Up -> {
                            val x = size.width - directionTextPaddingUpDownPx - textWidth
                            val y = directionTextPaddingUpDownPx
                            x to y
                        }
                        DragDirection.Down -> {
                            val x = size.width - directionTextPaddingUpDownPx - textWidth
                            val y = size.height - directionTextPaddingUpDownPx - textHeight
                            x to y
                        }
                        DragDirection.Left -> {
                            val x = directionTextPaddingLeftPx
                            val y = (size.height - textHeight) / 2
                            x to y
                        }
                        DragDirection.Right -> {
                            val x = size.width - directionTextPaddingUpDownPx - textWidth
                            val y = (size.height - textHeight) / 2
                            x to y
                        }
                    }
                    
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(x, y)
                    )
                }

                directionTexts.up?.let { drawDirectionText(it, DragDirection.Up) }
                directionTexts.down?.let { drawDirectionText(it, DragDirection.Down) }
                directionTexts.left?.let { drawDirectionText(it, DragDirection.Left) }
                directionTexts.right?.let { drawDirectionText(it, DragDirection.Right) }
            },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = text.takeIf { it.isNotEmpty() },
                tint = iconTint ?: effectiveTextColor,
                modifier = Modifier.fillMaxSize(0.55f)
            )
        } else {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = textSize,
                    color = effectiveTextColor,
                    fontStyle = fontStyle ?: FontStyle.Normal,
                    fontWeight = fontWeight ?: FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontFamily = CalculatorFontFamily
                )
            )
        }
    }
}

private enum class DragDirection {
    Up,
    Down,
    Left,
    Right
}

private fun dragDirection(delta: Offset): DragDirection {
    val distance = delta.getDistance()
    if (distance == 0f) {
        return DragDirection.Down
    }
    // Convert to degrees manually since standard KMP Math doesn't have it
    val angle = (acos(delta.y / distance) * 180 / PI).toFloat()
    val right = delta.x > 0f
    return when {
        angle >= 135f -> DragDirection.Up
        angle <= 45f -> DragDirection.Down
        right -> DragDirection.Right
        else -> DragDirection.Left
    }
}

/**
 * Preview helper for calculator button
 */
@Composable
fun CalculatorButtonPreview(
    text: String,
    directionTexts: DirectionTexts = DirectionTexts(),
    buttonType: ButtonType = ButtonType.DIGIT
) {
    CalculatorButton(
        text = text,
        buttonType = buttonType,
        directionTexts = directionTexts,
        modifier = Modifier.padding(4.dp)
    )
}
