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
import jscl.NumeralBase
import org.solovyev.android.calculator.memory.MemoryRegisters

/**
 * Modern keyboard with simplified layout and rounded buttons.
 * Extracted from CalculatorKeyboard.kt for better maintainability.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)

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
            .padding(keyboardPadding),
        verticalArrangement = Arrangement.spacedBy(keyGap)
    ) {
        // Row 1: Clear, (, ), /
        ButtonGroup(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keyGap)
        ) {
            ModernButton(
                text = "C",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(up = "MC", down = "MR"),
                onClick = { actions.onClear() },
                onSwipeUp = { actions.onMemoryClear() },
                onSwipeDown = { actions.onMemoryRecall() },
                longPressOptions = listOf(
                    "MS", "MR", "M+", "M-", "MC"
                ) + MemoryRegisters.QUICK_REGISTERS,
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
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "(",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(up = ")", down = "()"),
                onClick = { actions.onSpecialClick("(") },
                onSwipeUp = { actions.onSpecialClick(")") },
                onSwipeDown = { actions.onSpecialClick("()") },
                longPressOptions = listOf("(", ")", "()"),
                onLongPressOptionSelected = { option ->
                    when (option) {
                        "()" -> actions.onSpecialClick("()")
                        else -> actions.onSpecialClick(option)
                    }
                },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "%",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(up = "ƒ"),
                onClick = { actions.onOperatorClick("%") },
                onSwipeUp = { showScienceSheet = true },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "÷",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(up = "√"),
                onClick = { actions.onOperatorClick("/") },
                onSwipeUp = { actions.onFunctionClick("sqrt") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 2: 7, 8, 9, ×
        ButtonGroup(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keyGap)
        ) {
            ModernButton(
                text = "7",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "i", down = "!"),
                onClick = { actions.onNumberClick("7") },
                enabled = isDigitAllowedForBase("7", numeralBase),
                onSwipeUp = { actions.onSpecialClick("i") },
                onSwipeDown = { actions.onSpecialClick("!") },
                longPressOptions = listOf("i", "!", "0b:"),
                onLongPressOptionSelected = { insertLegacyOption(actions, numeralBase, it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "8",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "ln", down = "lg"),
                onClick = { actions.onNumberClick("8") },
                enabled = isDigitAllowedForBase("8", numeralBase),
                onSwipeUp = { actions.onFunctionClick("ln") },
                onSwipeDown = { actions.onFunctionClick("log") },
                longPressOptions = listOf("ln", "lg", "0d:"),
                onLongPressOptionSelected = { insertLegacyOption(actions, numeralBase, it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "9",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("9") },
                enabled = isDigitAllowedForBase("9", numeralBase),
                longPressOptions = listOf("0x:"),
                onLongPressOptionSelected = { insertLegacyOption(actions, numeralBase, it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "×",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(up = "^", down = "^2"),
                onClick = { actions.onOperatorClick("×") },
                onSwipeUp = { actions.onSpecialClick("^") },
                onSwipeDown = { actions.onSpecialClick("^2") },
                longPressOptions = listOf("^", "^2"),
                onLongPressOptionSelected = { insertLegacyOption(actions, numeralBase, it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 3: 4, 5, 6, -
        ButtonGroup(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keyGap)
        ) {
            ModernButton(
                text = "4",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "x", down = "y"),
                onClick = { actions.onNumberClick("4") },
                enabled = isDigitAllowedForBase("4", numeralBase),
                onSwipeUp = { actions.onSpecialClick("x") },
                onSwipeDown = { actions.onSpecialClick("y") },
                longPressOptions = listOf("x", "y"),
                onLongPressOptionSelected = { insertLegacyOption(actions, numeralBase, it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "5",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "t", down = "j"),
                onClick = { actions.onNumberClick("5") },
                enabled = isDigitAllowedForBase("5", numeralBase),
                onSwipeUp = { actions.onSpecialClick("t") },
                onSwipeDown = { actions.onSpecialClick("j") },
                longPressOptions = listOf("t", "j"),
                onLongPressOptionSelected = { insertLegacyOption(actions, numeralBase, it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "6",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "E"),
                onClick = { actions.onNumberClick("6") },
                enabled = isDigitAllowedForBase("6", numeralBase),
                onSwipeUp = { actions.onSpecialClick("E") },
                longPressOptions = buildList {
                    add("E")
                    if (numeralBase == NumeralBase.hex) add("F")
                },
                onLongPressOptionSelected = { insertLegacyOption(actions, numeralBase, it) },
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
            horizontalArrangement = Arrangement.spacedBy(keyGap)
        ) {
            ModernButton(
                text = "1",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "sin", down = "asin"),
                onClick = { actions.onNumberClick("1") },
                enabled = isDigitAllowedForBase("1", numeralBase),
                onSwipeUp = { actions.onFunctionClick("sin") },
                onSwipeDown = { actions.onFunctionClick("asin") },
                longPressOptions = buildList {
                    add("sin")
                    add("asin")
                    if (numeralBase == NumeralBase.hex) add("A")
                },
                onLongPressOptionSelected = { insertLegacyOption(actions, numeralBase, it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "2",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "cos", down = "acos"),
                onClick = { actions.onNumberClick("2") },
                enabled = isDigitAllowedForBase("2", numeralBase),
                onSwipeUp = { actions.onFunctionClick("cos") },
                onSwipeDown = { actions.onFunctionClick("acos") },
                longPressOptions = buildList {
                    add("cos")
                    add("acos")
                    if (numeralBase == NumeralBase.hex) add("B")
                },
                onLongPressOptionSelected = { insertLegacyOption(actions, numeralBase, it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "3",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "tan", down = "atan"),
                onClick = { actions.onNumberClick("3") },
                enabled = isDigitAllowedForBase("3", numeralBase),
                onSwipeUp = { actions.onFunctionClick("tan") },
                onSwipeDown = { actions.onFunctionClick("atan") },
                longPressOptions = buildList {
                    add("tan")
                    add("atan")
                    if (numeralBase == NumeralBase.hex) add("C")
                },
                onLongPressOptionSelected = { insertLegacyOption(actions, numeralBase, it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "+",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(up = "°"),
                onClick = { actions.onOperatorClick("+") },
                onSwipeUp = { actions.onSpecialClick("°") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 5: Delete, 0, ., =
        ButtonGroup(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(keyGap)
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
                directionTexts = DirectionTexts(up = "000", down = "00"),
                onClick = { actions.onNumberClick("0") },
                enabled = isDigitAllowedForBase("0", numeralBase),
                onSwipeUp = { actions.onNumberClick("000") },
                onSwipeDown = { actions.onNumberClick("00") },
                longPressOptions = listOf("000", "00"),
                onLongPressOptionSelected = { insertLegacyOption(actions, numeralBase, it) },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = ".",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = ","),
                onClick = { actions.onNumberClick(".") },
                enabled = true,
                onSwipeUp = { actions.onNumberClick(",") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            ModernButton(
                text = "ƒ",
                buttonType = ButtonType.SPECIAL,
                onClick = { actions.onOpenFunctions() },
                onSwipeUp = { showScienceSheet = true },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

private fun insertLegacyOption(actions: KeyboardActions, numeralBase: NumeralBase, option: String) {
    when (option) {
        "sin", "asin", "cos", "acos", "tan", "atan", "ln" -> actions.onFunctionClick(option)
        "lg" -> actions.onFunctionClick("log")
        "A", "B", "C", "D", "E", "F" -> {
            if (numeralBase == NumeralBase.hex) {
                actions.onNumberClick(option)
            }
        }
        else -> actions.onSpecialClick(option)
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
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
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

    val baseBackgroundColor = when (buttonType) {
        ButtonType.DIGIT -> MaterialTheme.colorScheme.surfaceContainerLow
        ButtonType.OPERATION -> MaterialTheme.colorScheme.secondaryContainer
        ButtonType.OPERATION_HIGHLIGHTED -> MaterialTheme.colorScheme.primary
        ButtonType.CONTROL -> MaterialTheme.colorScheme.surfaceContainer
        ButtonType.SPECIAL -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val backgroundColor = if (isPressed) {
        when (buttonType) {
            ButtonType.OPERATION_HIGHLIGHTED -> baseBackgroundColor.copy(alpha = 0.85f)
            else -> baseBackgroundColor.copy(alpha = 0.72f)
        }
    } else {
        baseBackgroundColor
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
    }.copy(alpha = if (enabled) 1f else 0.45f)

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
            .disabledAlpha(enabled)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape) // Use the passed shape (pill)
            .background(backgroundColor)
            .onSizeChanged { size ->
                buttonSizePx = androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat())
            }
            .pointerInput(enabled, onClick, onLongClick, onSwipeUp, onSwipeDown) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isPressed = true
                    val start = down.position
                    val downTime = down.uptimeMillis
                    var lastPos = start
                    var movedBeyondSlop = false
                    var longPressFired = false
                    var swipeHandled = false
                    var longPressSelectionCanceled = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.changedToUpIgnoreConsumed()) {
                            if (!enabled) {
                                HapticHelper.performSwipeFeedback(haptics, hapticsEnabled)
                                showLongPressOptions = false
                                isPressed = false
                                break
                            }
                            if (showLongPressOptions && effectiveLongPressOptions.isNotEmpty() && !longPressSelectionCanceled) {
                                val option = effectiveLongPressOptions[highlightedOption.coerceIn(0, effectiveLongPressOptions.lastIndex)]
                                HapticHelper.performLongPressFeedback(haptics, hapticsEnabled)
                                onLongPressOptionSelectedEffective(option)
                                showLongPressOptions = false
                                swipeHandled = true
                            } else if (showLongPressOptions) {
                                // User scrolled/dragged away while picker was open: dismiss without applying.
                                showLongPressOptions = false
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
                            HapticHelper.performLongPressFeedback(haptics, hapticsEnabled)
                        } else if (onLongClick != null &&
                            enabled &&
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
