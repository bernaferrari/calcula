@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
package org.solovyev.android.calculator.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Modern keyboard with simplified layout and rounded buttons.
 * Extracted from CalculatorKeyboard.kt for better maintainability.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)

@Composable
fun ModernCalculatorKeyboard(
    actions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    val icons = LocalKeyboardIcons.current
    var showScienceSheet by remember { mutableStateOf(false) }
    
    if (showScienceSheet) {
        ScientificBottomSheet(
            onFunctionClick = { 
                actions.onFunctionClick(it)
                // Optional: keep open or close? Let's keep it open for multiple ops, or close? 
                // Google closes. Let's close for now.
                showScienceSheet = false 
            },
            onConstantClick = { 
                actions.onNumberClick(it) // Constants are like numbers
                showScienceSheet = false
            },
            onDismissRequest = { showScienceSheet = false }
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp), // Comfortable outer padding
        verticalArrangement = Arrangement.spacedBy(16.dp) // Generous vertical spacing
    ) {
        // Row 1: Clear, (, ), /
        ButtonGroup(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernButton(
                text = "C",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onClear() },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "(",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(up = "%"),
                onClick = { actions.onSpecialClick("(") },
                onSwipeUp = { actions.onOperatorClick("%") },
                longPressOptions = listOf("(", ")", "()", "%"),
                onLongPressOptionSelected = { option ->
                    when (option) {
                        "()" -> actions.onSpecialClick("()")
                        "%" -> actions.onOperatorClick("%")
                        else -> actions.onSpecialClick(option)
                    }
                },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = ")",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(up = "ƒ"),
                onClick = { actions.onSpecialClick(")") },
                onSwipeUp = { showScienceSheet = true },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "÷",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("/") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 2: 7, 8, 9, ×
        ButtonGroup(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernButton(
                text = "7",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("7") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "8",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("8") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "9",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("9") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "×",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("×") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 3: 4, 5, 6, -
        ButtonGroup(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernButton(
                text = "4",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "tan"),
                onClick = { actions.onNumberClick("4") },
                onSwipeUp = { actions.onFunctionClick("tan") },
                longPressOptions = listOf("tan", "atan"),
                onLongPressOptionSelected = { actions.onFunctionClick(it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "5",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("5") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "6",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("6") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "−",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("−") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 4: 1, 2, 3, +
        ButtonGroup(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernButton(
                text = "1",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("1") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "2",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "sin"),
                onClick = { actions.onNumberClick("2") },
                onSwipeUp = { actions.onFunctionClick("sin") },
                longPressOptions = listOf("sin", "asin"),
                onLongPressOptionSelected = { actions.onFunctionClick(it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "3",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "cos"),
                onClick = { actions.onNumberClick("3") },
                onSwipeUp = { actions.onFunctionClick("cos") },
                longPressOptions = listOf("cos", "acos"),
                onLongPressOptionSelected = { actions.onFunctionClick(it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "+",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("+") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 5: Delete, 0, ., Equals
        ButtonGroup(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Replaced long label with icon for clarity in modern mode
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                ModernButton(
                    text = "",
                    buttonType = ButtonType.CONTROL,
                    onClick = { actions.onDelete() },
                    onLongClick = { actions.onClear() },
                    modifier = Modifier.fillMaxSize()
                )
                Icon(
                    painter = icons.backspace,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            ModernButton(
                text = "0",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("0") },
                longPressOptions = listOf("k", "m", "b"),
                onLongPressOptionSelected = { suffix ->
                    actions.onSpecialClick(suffix)
                },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = ".",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick(".") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "=",
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                onClick = { actions.onEquals() },
                onLongClick = { actions.onSimplify() },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

/**
 * Modern button with rounded corners, spring animations, and expressive feedback.
 */
@Composable
internal fun ModernButton(
    text: String,
    buttonType: ButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge, // Default to full pill/circle
    directionTexts: DirectionTexts = DirectionTexts(),
    onLongClick: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    longPressOptions: List<String> = emptyList(),
    onLongPressOptionSelected: (String) -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    var showLongPressOptions by remember { mutableStateOf(false) }
    var highlightedOption by remember { mutableStateOf(0) }
    var buttonSizePx by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val viewConfig = LocalViewConfiguration.current
    val haptics = LocalHapticFeedback.current
    val hapticsEnabled = LocalCalculatorHapticsEnabled.current
    val longPressTimeout = (viewConfig.longPressTimeoutMillis * 0.6f).roundToLong()
    val touchSlop = viewConfig.touchSlop
    val density = LocalDensity.current
    val minDragDistancePx = with(density) { 20.dp.toPx() }
    val optionWidthPx = with(density) { 48.dp.toPx() }

    // Expressive spring animation for button press - deeper press for more tactile feel
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ModernButtonScale"
    )

    // Modern color logic: Lighter, more tonal for digits.
    val backgroundColor = when {
        isPressed -> when (buttonType) {
            ButtonType.DIGIT -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
            ButtonType.OPERATION -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
            ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Primary for equals
            ButtonType.CONTROL -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
            ButtonType.SPECIAL -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        }
        else -> when (buttonType) {
            // Digits are now clean/ghost-like, just on the surface (or very light container)
            ButtonType.DIGIT -> Color.Transparent // Ghost style for digits
            ButtonType.OPERATION -> MaterialTheme.colorScheme.secondaryContainer
            ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.primary
            ButtonType.CONTROL -> MaterialTheme.colorScheme.surfaceContainer
            ButtonType.SPECIAL -> MaterialTheme.colorScheme.surfaceContainerLow
        }
    }

    val textColor = when (buttonType) {
        ButtonType.DIGIT -> MaterialTheme.colorScheme.onSurface
        ButtonType.OPERATION -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.onPrimary
        ButtonType.CONTROL -> MaterialTheme.colorScheme.onSurfaceVariant
        ButtonType.SPECIAL -> MaterialTheme.colorScheme.onSurface
    }

    val highContrast = LocalCalculatorHighContrast.current
    val effectiveTextColor = if (highContrast) {
        MaterialTheme.colorScheme.onSurface
    } else {
        textColor
    }

    val directionTextColor = effectiveTextColor.copy(alpha = 0.5f)

    val effectiveLongPressOptions = if (longPressOptions.isEmpty() && onSwipeUp != null && directionTexts.up != null) {
        listOf(directionTexts.up)
    } else {
        longPressOptions
    }
    val onLongPressOptionSelectedEffective: (String) -> Unit = if (longPressOptions.isEmpty() && onSwipeUp != null && directionTexts.up != null) {
        { onSwipeUp() }
    } else {
        onLongPressOptionSelected
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape) // Use the passed shape (pill)
            .background(backgroundColor)
            .onSizeChanged { size ->
                buttonSizePx = androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat())
            }
            .pointerInput(onClick, onLongClick, onSwipeUp, onSwipeDown) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isPressed = true
                    val start = down.position
                    val downTime = down.uptimeMillis
                    var lastPos = start
                    var movedBeyondSlop = false
                    var longPressFired = false
                    var swipeHandled = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.changedToUpIgnoreConsumed()) {
                            if (showLongPressOptions && effectiveLongPressOptions.isNotEmpty()) {
                                val option = effectiveLongPressOptions[highlightedOption.coerceIn(0, effectiveLongPressOptions.lastIndex)]
                                HapticHelper.performLongPressFeedback(haptics, hapticsEnabled)
                                onLongPressOptionSelectedEffective(option)
                                showLongPressOptions = false
                                swipeHandled = true
                            }
                            if (!longPressFired && !swipeHandled) {
                                // Check for swipe
                                val delta = lastPos - start
                                val distance = delta.getDistance()
                                if (distance > minDragDistancePx) {
                                    // Determine swipe direction (vertical only for modern buttons)
                                    if (kotlin.math.abs(delta.y) > kotlin.math.abs(delta.x)) {
                                        if (delta.y < 0 && onSwipeUp != null) {
                                            HapticHelper.performSwipeFeedback(haptics, hapticsEnabled)
                                            onSwipeUp()
                                            swipeHandled = true
                                        } else if (delta.y > 0 && onSwipeDown != null) {
                                            HapticHelper.performSwipeFeedback(haptics, hapticsEnabled)
                                            onSwipeDown()
                                            swipeHandled = true
                                        }
                                    }
                                }
                                if (!swipeHandled) {
                                    HapticHelper.performButtonFeedback(buttonType, haptics, hapticsEnabled)
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
                            if (showLongPressOptions && effectiveLongPressOptions.isNotEmpty()) {
                                val relative = lastPos.x - (buttonSizePx.width / 2f)
                                val baseIndex = (effectiveLongPressOptions.size - 1) / 2f
                                val rawIndex = (relative / optionWidthPx + baseIndex).roundToInt()
                                highlightedOption = rawIndex.coerceIn(0, effectiveLongPressOptions.lastIndex)
                            }
                            change.consume()
                        }

                        if (!longPressFired &&
                            effectiveLongPressOptions.isNotEmpty() &&
                            !movedBeyondSlop &&
                            (change.uptimeMillis - downTime) >= longPressTimeout
                        ) {
                            longPressFired = true
                            showLongPressOptions = true
                            highlightedOption = (effectiveLongPressOptions.size - 1) / 2
                            HapticHelper.performLongPressFeedback(haptics, hapticsEnabled)
                        } else if (onLongClick != null &&
                            !longPressFired &&
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
            },
        contentAlignment = Alignment.Center
    ) {
        if (showLongPressOptions && effectiveLongPressOptions.isNotEmpty() && buttonSizePx.height > 0f) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(
                    x = 0,
                    y = -(buttonSizePx.height + with(density) { 12.dp.toPx() }).roundToInt()
                )
            ) {
                Surface(
                    modifier = Modifier.wrapContentSize(),
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        effectiveLongPressOptions.forEachIndexed { index, option ->
                            val isSelected = index == highlightedOption
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Direction text indicators
        directionTexts.up?.let { upText ->
            Text(
                text = upText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = directionTextColor,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontFamily = CalculatorFontFamily
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp) // Adjusted padding for pill shape
            )
        }
        directionTexts.down?.let { downText ->
            Text(
                text = downText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = directionTextColor,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontFamily = CalculatorFontFamily
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }

        // Main text
        Text(
            text = text,
            style = TextStyle(
                fontSize = 28.sp, // Slightly smaller for better proportion in pills
                color = effectiveTextColor,
                fontWeight = FontWeight.Medium, // Slightly bolder for clarity
                textAlign = TextAlign.Center,
                fontFamily = CalculatorFontFamily
            )
        )
    }
}
