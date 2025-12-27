package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ui.compose.theme.CalculatorFontFamily
import kotlin.math.acos
import kotlin.math.max

val LocalCalculatorHighContrast = compositionLocalOf { false }
val LocalCalculatorHapticsEnabled = compositionLocalOf { true }

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
    SPECIAL
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
    textSize: androidx.compose.ui.unit.TextUnit = 30.sp,
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
    val viewConfig = LocalViewConfiguration.current
    val haptics = LocalHapticFeedback.current
    val highContrast = LocalCalculatorHighContrast.current
    val hapticsEnabled = LocalCalculatorHapticsEnabled.current
    val minDragDistancePx = with(LocalDensity.current) { 15.dp.toPx() }
    val longPressTimeout = viewConfig.longPressTimeoutMillis
    val touchSlop = viewConfig.touchSlop
    val density = LocalDensity.current
    val context = LocalContext.current
    val directionPaddingUpDown = dimensionResource(R.dimen.cpp_keyboard_button_direction_text_padding_updown)
    val directionPaddingLeft = dimensionResource(R.dimen.cpp_keyboard_button_direction_text_padding_left)
    val directionTextMinSizePx = with(density) { 9.dp.toPx() }
    val directionTextPaddingUpDownPx = with(density) { directionPaddingUpDown.toPx() }
    val directionTextPaddingLeftPx = with(density) { directionPaddingLeft.toPx() }
    val baseTextSizePx = with(density) { textSize.toPx() }
    val directionTextSizePx = max(baseTextSizePx * directionTextScale, directionTextMinSizePx)
    val fixedHeightTextSizePx = max(baseTextSizePx * 0.4f, directionTextMinSizePx)
    val directionTypeface = remember {
        ResourcesCompat.getFont(context, R.font.roboto_regular) ?: android.graphics.Typeface.DEFAULT
    }

    val backgroundColor = backgroundOverride ?: when {
        isPressed -> when (buttonType) {
            ButtonType.DIGIT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            ButtonType.OPERATION -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
            ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
            ButtonType.CONTROL -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ButtonType.SPECIAL -> MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        }
        else -> when (buttonType) {
            ButtonType.DIGIT -> MaterialTheme.colorScheme.surface
            ButtonType.OPERATION -> MaterialTheme.colorScheme.secondaryContainer
            ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.tertiaryContainer
            ButtonType.CONTROL -> MaterialTheme.colorScheme.surfaceVariant
            ButtonType.SPECIAL -> MaterialTheme.colorScheme.surface
        }
    }

    val textColor = when (buttonType) {
        ButtonType.DIGIT -> MaterialTheme.colorScheme.onSurface
        ButtonType.OPERATION -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.onTertiaryContainer
        ButtonType.CONTROL -> MaterialTheme.colorScheme.onSurface
        ButtonType.SPECIAL -> MaterialTheme.colorScheme.onSurface
    }

    val effectiveTextColor = if (highContrast) {
        MaterialTheme.colorScheme.onSurface
    } else {
        textColor
    }
    val directionTextColor = effectiveTextColor.copy(alpha = if (highContrast) 1f else directionTextAlpha)

    Box(
        modifier = modifier
            .fillMaxSize()
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
                            val duration = change.uptimeMillis - downTime
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
                                    if (hapticsEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    isPressed = false
                                    break
                                }
                                isPressed = false
                                break
                            }

                            if (!longPressFired) {
                                if (hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
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
                            if (hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
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

                val nativeCanvas = drawContext.canvas.nativeCanvas
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = directionTextColor.toArgb()
                    typeface = directionTypeface
                }
                paint.textSize = directionTextSizePx

                val fixedHeight = android.graphics.Rect().let { rect ->
                    val oldSize = paint.textSize
                    paint.textSize = fixedHeightTextSizePx
                    paint.getTextBounds("|", 0, 1, rect)
                    paint.textSize = oldSize
                    rect.height().toFloat()
                }

                fun drawDirectionText(value: String, direction: DragDirection) {
                    if (value.isEmpty()) return
                    val bounds = android.graphics.Rect()
                    paint.getTextBounds(value, 0, value.length, bounds)
                    val textWidth = bounds.width().toFloat()
                    val textLeft = bounds.left.toFloat()
                    when (direction) {
                        DragDirection.Up -> {
                            val x = size.width - directionTextPaddingUpDownPx - textWidth - textLeft
                            val y = directionTextPaddingUpDownPx + fixedHeight
                            nativeCanvas.drawText(value, x, y, paint)
                        }
                        DragDirection.Down -> {
                            val x = size.width - directionTextPaddingUpDownPx - textWidth - textLeft
                            val y = size.height - directionTextPaddingUpDownPx - paint.descent()
                            nativeCanvas.drawText(value, x, y, paint)
                        }
                        DragDirection.Left -> {
                            val x = directionTextPaddingLeftPx
                            val y = size.height / 2 + fixedHeight / 2
                            nativeCanvas.drawText(value, x, y, paint)
                        }
                        DragDirection.Right -> {
                            val x = size.width - directionTextPaddingUpDownPx - textWidth
                            val y = size.height / 2 + fixedHeight / 2
                            nativeCanvas.drawText(value, x, y, paint)
                        }
                    }
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
    val angle = Math.toDegrees(acos(delta.y / distance).toDouble()).toFloat()
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
