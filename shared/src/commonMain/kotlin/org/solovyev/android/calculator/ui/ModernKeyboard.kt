package org.solovyev.android.calculator.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import jscl.NumeralBase
import kotlinx.coroutines.delay
import org.solovyev.android.calculator.memory.MemoryRegisters
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import org.solovyev.android.calculator.ui.LocalCalculatorHapticsEnabled

/**
 * Modern keyboard with swipe gestures as the core feature.
 * Clean, elegant, minimal - the gestures ARE the experience.
 */
@Composable
fun ModernCalculatorKeyboard(
    actions: KeyboardActions,
    numeralBase: NumeralBase = NumeralBase.dec,
    bitwiseWordSize: Int = 64,
    bitwiseSigned: Boolean = true,
    modifier: Modifier = Modifier
) {
    val icons = LocalKeyboardIcons.current
    var showScienceSheet by remember { mutableStateOf(false) }
    val keyGap = 6.dp
    val keyboardPadding = 4.dp

    if (showScienceSheet) {
        ScientificBottomSheet(
            onFunctionClick = {
                actions.onFunctionClick(it)
                showScienceSheet = false
            },
            onConstantClick = {
                actions.onNumberClick(it)
                showScienceSheet = false
            },
            onDismissRequest = { showScienceSheet = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(keyboardPadding),
        verticalArrangement = Arrangement.spacedBy(keyGap)
    ) {
        // Row 1: Clear, (, ), /
        ButtonRow(modifier = Modifier.weight(1f)) {
            ModernButton(
                text = "C",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(up = "MC", down = "MR"),
                contentDescription = stringResource(Res.string.cpp_button_clear),
                onClick = { actions.onClear() },
                onSwipeUp = { actions.onMemoryClear() },
                onSwipeDown = { actions.onMemoryRecall() },
                longPressOptions = listOf("MS", "MR", "M+", "M-", "MC") + MemoryRegisters.QUICK_REGISTERS,
                onLongPressOptionSelected = { option ->
                    when (option) {
                        "MS" -> actions.onMemoryStore()
                        "MR" -> actions.onMemoryRecall()
                        "M+" -> actions.onMemoryPlus()
                        "M-" -> actions.onMemoryMinus()
                        "MC" -> actions.onMemoryClear()
                        else -> actions.onMemoryRegisterSelected(option)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "(",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(up = ")", down = "()"),
                contentDescription = stringResource(Res.string.cpp_button_parentheses),
                onClick = { actions.onSpecialClick("(") },
                onSwipeUp = { actions.onSpecialClick(")") },
                onSwipeDown = { actions.onSpecialClick("()") },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "%",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(up = "ƒ"),
                contentDescription = stringResource(Res.string.cpp_button_percent),
                onClick = { actions.onOperatorClick("%") },
                onSwipeUp = { showScienceSheet = true },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "÷",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(up = "√"),
                contentDescription = stringResource(Res.string.cpp_button_divide),
                onClick = { actions.onOperatorClick("/") },
                onSwipeUp = { actions.onFunctionClick("sqrt") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 7, 8, 9, ×
        ButtonRow(modifier = Modifier.weight(1f)) {
            ModernButton(
                text = "7",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "i", down = "!"),
                contentDescription = stringResource(Res.string.cpp_button_seven),
                onClick = { actions.onNumberClick("7") },
                enabled = isDigitAllowedForBase("7", numeralBase),
                onSwipeUp = { actions.onSpecialClick("i") },
                onSwipeDown = { actions.onSpecialClick("!") },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "8",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "ln", down = "lg"),
                contentDescription = stringResource(Res.string.cpp_button_eight),
                onClick = { actions.onNumberClick("8") },
                enabled = isDigitAllowedForBase("8", numeralBase),
                onSwipeUp = { actions.onFunctionClick("ln") },
                onSwipeDown = { actions.onFunctionClick("log") },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "9",
                buttonType = ButtonType.DIGIT,
                contentDescription = stringResource(Res.string.cpp_button_nine),
                onClick = { actions.onNumberClick("9") },
                enabled = isDigitAllowedForBase("9", numeralBase),
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "×",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(up = "^", down = "^2"),
                contentDescription = stringResource(Res.string.cpp_button_multiply),
                onClick = { actions.onOperatorClick("×") },
                onSwipeUp = { actions.onSpecialClick("^") },
                onSwipeDown = { actions.onSpecialClick("^2") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: 4, 5, 6, -
        ButtonRow(modifier = Modifier.weight(1f)) {
            ModernButton(
                text = "4",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "x", down = "y"),
                contentDescription = stringResource(Res.string.cpp_button_four),
                onClick = { actions.onNumberClick("4") },
                enabled = isDigitAllowedForBase("4", numeralBase),
                onSwipeUp = { actions.onSpecialClick("x") },
                onSwipeDown = { actions.onSpecialClick("y") },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "5",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "t", down = "j"),
                contentDescription = stringResource(Res.string.cpp_button_five),
                onClick = { actions.onNumberClick("5") },
                enabled = isDigitAllowedForBase("5", numeralBase),
                onSwipeUp = { actions.onSpecialClick("t") },
                onSwipeDown = { actions.onSpecialClick("j") },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "6",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "E"),
                contentDescription = stringResource(Res.string.cpp_button_six),
                onClick = { actions.onNumberClick("6") },
                enabled = isDigitAllowedForBase("6", numeralBase),
                onSwipeUp = { actions.onSpecialClick("E") },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "−",
                buttonType = ButtonType.OPERATION,
                contentDescription = stringResource(Res.string.cpp_button_minus),
                onClick = { actions.onOperatorClick("−") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: 1, 2, 3, +
        ButtonRow(modifier = Modifier.weight(1f)) {
            ModernButton(
                text = "1",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "sin", down = "asin"),
                contentDescription = stringResource(Res.string.cpp_button_one),
                onClick = { actions.onNumberClick("1") },
                enabled = isDigitAllowedForBase("1", numeralBase),
                onSwipeUp = { actions.onFunctionClick("sin") },
                onSwipeDown = { actions.onFunctionClick("asin") },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "2",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "cos", down = "acos"),
                contentDescription = stringResource(Res.string.cpp_button_two),
                onClick = { actions.onNumberClick("2") },
                enabled = isDigitAllowedForBase("2", numeralBase),
                onSwipeUp = { actions.onFunctionClick("cos") },
                onSwipeDown = { actions.onFunctionClick("acos") },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "3",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "tan", down = "atan"),
                contentDescription = stringResource(Res.string.cpp_button_three),
                onClick = { actions.onNumberClick("3") },
                enabled = isDigitAllowedForBase("3", numeralBase),
                onSwipeUp = { actions.onFunctionClick("tan") },
                onSwipeDown = { actions.onFunctionClick("atan") },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "+",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(up = "°"),
                contentDescription = stringResource(Res.string.cpp_button_plus),
                onClick = { actions.onOperatorClick("+") },
                onSwipeUp = { actions.onSpecialClick("°") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5: Delete, 0, ., =
        ButtonRow(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ModernButton(
                    text = "",
                    buttonType = ButtonType.CONTROL,
                    contentDescription = stringResource(Res.string.cpp_button_delete),
                    onClick = { actions.onDelete() },
                    onLongClick = { actions.onClear() },
                    modifier = Modifier.fillMaxSize()
                )
                Icon(
                    painter = icons.backspace,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            ModernButton(
                text = "0",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "000", down = "00"),
                contentDescription = stringResource(Res.string.cpp_button_zero),
                onClick = { actions.onNumberClick("0") },
                enabled = isDigitAllowedForBase("0", numeralBase),
                onSwipeUp = { actions.onNumberClick("000") },
                onSwipeDown = { actions.onNumberClick("00") },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = ".",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = ","),
                contentDescription = stringResource(Res.string.cpp_button_decimal),
                onClick = { actions.onNumberClick(".") },
                onSwipeUp = { actions.onNumberClick(",") },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "=",
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                contentDescription = stringResource(Res.string.cpp_button_equals),
                onClick = { actions.onEquals() },
                isEqualsButton = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ButtonRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        content()
    }
}

/**
 * Refined button with subtle gesture hints and clean animations
 */
@Composable
internal fun ModernButton(
    text: String,
    buttonType: ButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    directionTexts: DirectionTexts = DirectionTexts(),
    contentDescription: String? = null,
    onLongClick: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    longPressOptions: List<String> = emptyList(),
    onLongPressOptionSelected: (String) -> Unit = {},
    isEqualsButton: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    var showLongPressOptions by remember { mutableStateOf(false) }
    var highlightedOption by remember { mutableStateOf(0) }
    var buttonSizePx by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var gestureCompleted by remember { mutableStateOf(false) }
    var rippleCenter by remember { mutableStateOf(Offset.Zero) }
    var showRipple by remember { mutableStateOf(false) }
    val viewConfig = LocalViewConfiguration.current
    val haptics = LocalHapticFeedback.current
    val hapticsEnabled = LocalCalculatorHapticsEnabled.current
    val longPressTimeout = (viewConfig.longPressTimeoutMillis * 0.6f).roundToLong()
    val touchSlop = viewConfig.touchSlop
    val density = LocalDensity.current
    val minDragDistancePx = with(density) { 20.dp.toPx() }
    val optionWidthPx = with(density) { 48.dp.toPx() }

    // Enhanced scale animation - 0.95x for better feel
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            gestureCompleted -> 1.02f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 350f),
        label = "buttonScale"
    )

    // Elevation animation - button sinks when pressed
    val elevation by animateFloatAsState(
        targetValue = when {
            isPressed -> 0f
            gestureCompleted -> 4f
            isEqualsButton -> 6f
            else -> 2f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "buttonElevation"
    )

    // Ripple animation
    val rippleProgress by animateFloatAsState(
        targetValue = if (showRipple) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        finishedListener = { if (it == 1f) showRipple = false },
        label = "rippleProgress"
    )

    // Pulse animation for equals button
    val pulseScale by animateFloatAsState(
        targetValue = if (gestureCompleted && isEqualsButton) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
        label = "pulseScale"
    )

    val baseBackgroundColor = when (buttonType) {
        ButtonType.DIGIT -> MaterialTheme.colorScheme.surfaceContainerLowest
        ButtonType.OPERATION -> MaterialTheme.colorScheme.primary
        ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.primary
        ButtonType.CONTROL -> MaterialTheme.colorScheme.tertiaryContainer
        ButtonType.SPECIAL -> MaterialTheme.colorScheme.secondaryContainer
        ButtonType.MEMORY -> MaterialTheme.colorScheme.secondary
    }

    // Darken on press for "sink" effect
    val backgroundColor = if (isPressed) {
        baseBackgroundColor.copy(
            alpha = 1f,
            red = baseBackgroundColor.red * 0.9f,
            green = baseBackgroundColor.green * 0.9f,
            blue = baseBackgroundColor.blue * 0.9f
        )
    } else {
        baseBackgroundColor
    }

    val textColor = when (buttonType) {
        ButtonType.DIGIT -> MaterialTheme.colorScheme.onSurface
        ButtonType.OPERATION -> MaterialTheme.colorScheme.onPrimary
        ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.onPrimary
        ButtonType.CONTROL -> MaterialTheme.colorScheme.onTertiaryContainer
        ButtonType.SPECIAL -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonType.MEMORY -> MaterialTheme.colorScheme.onSecondary
    }

    val effectiveLongPressOptions = longPressOptions
    val onLongPressOptionSelectedEffective = onLongPressOptionSelected

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(if (isEqualsButton) scale * pulseScale else scale)
            .shadow(
                elevation = elevation.dp,
                shape = shape,
                clip = true,
                ambientColor = baseBackgroundColor.copy(alpha = 0.3f),
                spotColor = baseBackgroundColor.copy(alpha = 0.5f)
            )
            .clip(shape)
            .background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.3f))
            .then(
                // Ripple effect on press
                if (showRipple) {
                    Modifier.drawBehind {
                        drawRipple(rippleCenter, rippleProgress, baseBackgroundColor)
                    }
                } else {
                    Modifier
                }
            )
            .onSizeChanged { size ->
                buttonSizePx = androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat())
            }
            .semantics(mergeDescendants = true) {
                contentDescription?.let { this.contentDescription = it }
                if (!enabled) stateDescription = "Disabled"
            }
            .pointerInput(enabled, onClick, onLongClick, onSwipeUp, onSwipeDown) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isPressed = true
                    gestureCompleted = false
                    rippleCenter = down.position
                    showRipple = true
                    val start = down.position
                    val downTime = down.uptimeMillis
                    var lastPos = start
                    var movedBeyondSlop = false
                    var longPressFired = false
                    var swipeHandled = false
                    var longPressSelectionCanceled = false

                    // Enhanced haptic feedback on press
                    if (hapticsEnabled && enabled) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.changedToUpIgnoreConsumed()) {
                            if (!enabled) {
                                isPressed = false
                                break
                            }
                            if (showLongPressOptions && effectiveLongPressOptions.isNotEmpty() && !longPressSelectionCanceled) {
                                val option = effectiveLongPressOptions[highlightedOption.coerceIn(0, effectiveLongPressOptions.lastIndex)]
                                if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                gestureCompleted = true
                                onLongPressOptionSelectedEffective(option)
                                showLongPressOptions = false
                                swipeHandled = true
                            } else if (showLongPressOptions) {
                                showLongPressOptions = false
                            }
                            if (!longPressFired && !swipeHandled) {
                                val delta = lastPos - start
                                val distance = delta.getDistance()
                                if (distance > minDragDistancePx) {
                                    if (kotlin.math.abs(delta.y) > kotlin.math.abs(delta.x)) {
                                        if (delta.y < 0 && onSwipeUp != null) {
                                            if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            gestureCompleted = true
                                            onSwipeUp()
                                            swipeHandled = true
                                        } else if (delta.y > 0 && onSwipeDown != null) {
                                            if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            gestureCompleted = true
                                            onSwipeDown()
                                            swipeHandled = true
                                        }
                                    }
                                }
                                if (!swipeHandled) {
                                    // Completion haptic for equals button
                                    if (hapticsEnabled && isEqualsButton) {
                                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } else if (hapticsEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                    }
                                    gestureCompleted = true
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
                                val dragFromStart = lastPos - start
                                if (kotlin.math.abs(dragFromStart.y) > minDragDistancePx) {
                                    longPressSelectionCanceled = true
                                    showLongPressOptions = false
                                } else {
                                    val relative = lastPos.x - (buttonSizePx.width / 2f)
                                    val baseIndex = (effectiveLongPressOptions.size - 1) / 2f
                                    val rawIndex = (relative / optionWidthPx + baseIndex).roundToInt()
                                    highlightedOption = rawIndex.coerceIn(0, effectiveLongPressOptions.lastIndex)
                                }
                            }
                            change.consume()
                        }

                        if (!longPressFired &&
                            enabled &&
                            effectiveLongPressOptions.isNotEmpty() &&
                            !movedBeyondSlop &&
                            (change.uptimeMillis - downTime) >= longPressTimeout
                        ) {
                            longPressFired = true
                            showLongPressOptions = true
                            highlightedOption = (effectiveLongPressOptions.size - 1) / 2
                            if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else if (onLongClick != null &&
                            enabled &&
                            !longPressFired &&
                            !movedBeyondSlop &&
                            (change.uptimeMillis - downTime) >= longPressTimeout
                        ) {
                            longPressFired = true
                            if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    tonalElevation = 4.dp
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
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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

        // Elegant direction hints with better visibility
        directionTexts.up?.let { upText ->
            Text(
                text = "▲ $upText",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 3.dp)
                    .alpha(0.65f),
                style = TextStyle(
                    fontSize = 9.sp,
                    color = textColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                )
            )
        }
        directionTexts.down?.let { downText ->
            Text(
                text = "$downText ▼",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .alpha(0.65f),
                style = TextStyle(
                    fontSize = 9.sp,
                    color = textColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                )
            )
        }

        // Main button text
        Text(
            text = text,
            style = TextStyle(
                fontSize = if (isEqualsButton) 30.sp else 26.sp,
                color = if (enabled) textColor else textColor.copy(alpha = 0.4f),
                fontWeight = if (buttonType == ButtonType.OPERATION || buttonType == ButtonType.OPERATION_HIGHLIGHTED) FontWeight.SemiBold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )
    }
}

// Ripple draw function
private fun DrawScope.drawRipple(
    center: Offset,
    progress: Float,
    color: Color
) {
    val maxRadius = size.maxDimension * 1.2f
    val currentRadius = maxRadius * progress
    val alpha = (1f - progress) * 0.3f

    drawCircle(
        color = color.copy(alpha = alpha),
        radius = currentRadius,
        center = center
    )
}

// Equals button pulse indicator
@Composable
fun Modifier.equalsPulseIndicator(
    trigger: Boolean,
    color: Color
): Modifier {
    var pulseActive by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            pulseActive = true
            delay(600)
            pulseActive = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (pulseActive) 1.3f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "pulseIndicator"
    )

    val alpha by animateFloatAsState(
        targetValue = if (pulseActive) 0.4f else 0f,
        animationSpec = tween(400),
        label = "pulseAlpha"
    )

    return this.drawBehind {
        if (pulseActive) {
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = size.minDimension * scale,
                center = center
            )
        }
    }
}
