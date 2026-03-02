package org.solovyev.android.calculator.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
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
import androidx.compose.ui.unit.Dp
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
import org.solovyev.android.calculator.ui.animations.FlyingAnimationEvent
import org.solovyev.android.calculator.ui.animations.FlyingAnimationType
import org.solovyev.android.calculator.ui.animations.LocalFlyingAnimationHost
import org.solovyev.android.calculator.ui.tokens.CalculatorButtonBorderTokens
import org.solovyev.android.calculator.ui.tokens.CalculatorGestureTokens

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
    isSimpleMode: Boolean = false,
    gestureAutoActivation: Boolean = false,
    showBottomRightEqualsKey: Boolean = false,
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
            showInverseTrigonometric = !isSimpleMode,
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
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
            )
            ModernButton(
                text = "()",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(up = "(", down = ")", left = "(…)"),
                contentDescription = stringResource(Res.string.cpp_button_parentheses),
                onClick = { actions.onSpecialClick("()") },
                onSwipeUp = { actions.onSpecialClick("(") },
                onSwipeDown = { actions.onSpecialClick(")") },
                onSwipeLeft = { actions.onSpecialClick("(…)") },
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
            )
            ModernButton(
                text = "%",
                buttonType = ButtonType.CONTROL,
                directionTexts = DirectionTexts(up = "f"),
                contentDescription = stringResource(Res.string.cpp_button_percent),
                onClick = { actions.onOperatorClick("%") },
                onSwipeUp = { showScienceSheet = true },
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "/",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(up = "√"),
                contentDescription = stringResource(Res.string.cpp_button_divide),
                onClick = { actions.onOperatorClick("/") },
                onSwipeUp = { actions.onFunctionClick("sqrt") },
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
            )
        }

        // Row 2: 7, 8, 9, *
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
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
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
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
            )
            ModernButton(
                text = "9",
                buttonType = ButtonType.DIGIT,
                contentDescription = stringResource(Res.string.cpp_button_nine),
                onClick = { actions.onNumberClick("9") },
                enabled = isDigitAllowedForBase("9", numeralBase),
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
            )
            ModernButton(
                text = "*",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(up = "^", down = "^2"),
                contentDescription = stringResource(Res.string.cpp_button_multiply),
                onClick = { actions.onOperatorClick("*") },
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
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
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
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
            )
            ModernButton(
                text = "6",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(up = "E"),
                contentDescription = stringResource(Res.string.cpp_button_six),
                onClick = { actions.onNumberClick("6") },
                enabled = isDigitAllowedForBase("6", numeralBase),
                onSwipeUp = { actions.onSpecialClick("E") },
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
            )
            ModernButton(
                text = "-",
                buttonType = ButtonType.OPERATION,
                contentDescription = stringResource(Res.string.cpp_button_minus),
                onClick = { actions.onOperatorClick("-") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: 1, 2, 3, +
        ButtonRow(modifier = Modifier.weight(1f)) {
            ModernButton(
                text = "1",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "sin",
                    down = if (isSimpleMode) null else "asin"
                ),
                contentDescription = stringResource(Res.string.cpp_button_one),
                onClick = { actions.onNumberClick("1") },
                enabled = isDigitAllowedForBase("1", numeralBase),
                onSwipeUp = { actions.onFunctionClick("sin") },
                onSwipeDown = if (isSimpleMode) null else ({ actions.onFunctionClick("asin") }),
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
            )
            ModernButton(
                text = "2",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "cos",
                    down = if (isSimpleMode) null else "acos"
                ),
                contentDescription = stringResource(Res.string.cpp_button_two),
                onClick = { actions.onNumberClick("2") },
                enabled = isDigitAllowedForBase("2", numeralBase),
                onSwipeUp = { actions.onFunctionClick("cos") },
                onSwipeDown = if (isSimpleMode) null else ({ actions.onFunctionClick("acos") }),
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
            )
            ModernButton(
                text = "3",
                buttonType = ButtonType.DIGIT,
                directionTexts = DirectionTexts(
                    up = "tan",
                    down = if (isSimpleMode) null else "atan"
                ),
                contentDescription = stringResource(Res.string.cpp_button_three),
                onClick = { actions.onNumberClick("3") },
                enabled = isDigitAllowedForBase("3", numeralBase),
                onSwipeUp = { actions.onFunctionClick("tan") },
                onSwipeDown = if (isSimpleMode) null else ({ actions.onFunctionClick("atan") }),
                modifier = Modifier.weight(1f)
            )
            ModernButton(
                text = "+",
                buttonType = ButtonType.OPERATION,
                directionTexts = DirectionTexts(up = "°"),
                contentDescription = stringResource(Res.string.cpp_button_plus),
                onClick = { actions.onOperatorClick("+") },
                onSwipeUp = { actions.onSpecialClick("°") },
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
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
                    modifier = Modifier.fillMaxSize(),
                    gestureAutoActivation = gestureAutoActivation
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
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
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
                onLongClick = { actions.onOpenFunctions() },
                onSwipeUp = { showScienceSheet = true },
                isEqualsButton = true,
                modifier = Modifier.weight(1f),
                gestureAutoActivation = gestureAutoActivation
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
 * Refined button with subtle gesture hints, clean animations, and flying text effects
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
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    longPressOptions: List<String> = emptyList(),
    onLongPressOptionSelected: (String) -> Unit = {},
    isEqualsButton: Boolean = false,
    gestureAutoActivation: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    var showLongPressOptions by remember { mutableStateOf(false) }
    var highlightedOption by remember { mutableStateOf(0) }
    var buttonSizePx by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var buttonOriginInRoot by remember { mutableStateOf(Offset.Zero) }
    var gestureCompleted by remember { mutableStateOf(false) }
    var rippleCenter by remember { mutableStateOf(Offset.Zero) }
    var showRipple by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var dragDirection by remember { mutableStateOf<GestureDragDirection?>(null) }
    var lastEligibleDragDirection by remember { mutableStateOf<GestureDragDirection?>(null) }
    var lastEligibleDragProgress by remember { mutableFloatStateOf(0f) }
    var indicatorDirection by remember { mutableStateOf<GestureDragDirection?>(null) }
    var returnHintDirection by remember { mutableStateOf<GestureDragDirection?>(null) }
    var returnHintProgress by remember { mutableFloatStateOf(0f) }
    var returnHintAnimationTrigger by remember { mutableIntStateOf(0) }
    val viewConfig = LocalViewConfiguration.current
    val haptics = LocalHapticFeedback.current
    val hapticsEnabled = LocalCalculatorHapticsEnabled.current
    val layerUpEnabled = LocalCalculatorLayerUpEnabled.current
    val layerDownEnabled = LocalCalculatorLayerDownEnabled.current
    val longPressTimeout = (viewConfig.longPressTimeoutMillis * 0.6f).roundToLong()
    val touchSlop = viewConfig.touchSlop
    val density = LocalDensity.current
    val minDragDistancePx = with(density) { 20.dp.toPx() }
    val optionWidthPx = with(density) { 48.dp.toPx() }
    val autoActivationThresholdPx = with(density) {
        (minDragDistancePx * CalculatorGestureTokens.AutoActivationThresholdRatio)
            .coerceAtLeast(CalculatorGestureTokens.AutoActivationMinDistance.toPx())
    }
    val effectiveDirectionTexts = remember(directionTexts, layerUpEnabled, layerDownEnabled) {
        directionTexts.copy(
            up = if (layerUpEnabled) directionTexts.up else null,
            down = if (layerDownEnabled) directionTexts.down else null
        )
    }
    val effectiveOnSwipeUp = if (layerUpEnabled) onSwipeUp else null
    val effectiveOnSwipeDown = if (layerDownEnabled) onSwipeDown else null

    // Get flying animation host
    val flyingAnimationHost = LocalFlyingAnimationHost.current

    // Function to trigger flying animation
    fun triggerFlyingText(text: String, position: Offset) {
        flyingAnimationHost(FlyingAnimationEvent(
            text = text,
            startPosition = position,
            type = FlyingAnimationType.FUNCTION
        ))
    }

    fun labelFor(direction: GestureDragDirection): String? = when (direction) {
        GestureDragDirection.UP -> effectiveDirectionTexts.up
        GestureDragDirection.DOWN -> effectiveDirectionTexts.down
        GestureDragDirection.LEFT -> effectiveDirectionTexts.left
        GestureDragDirection.RIGHT -> effectiveDirectionTexts.right
    }

    val directionHintSpec = remember { DirectionHintSpec() }
    val directionHintCenterByDirection = remember { mutableStateMapOf<GestureDragDirection, Offset>() }
    val directionLabelInsetPx = with(density) { directionHintSpec.edgePadding.toPx() }

    fun hintTranslation(direction: GestureDragDirection, progress: Float): Offset {
        val p = progress.coerceIn(0f, 1f)
        return when (direction) {
            GestureDragDirection.UP -> Offset(
                x = 0f,
                y = -(buttonSizePx.height * directionHintSpec.verticalTravelFraction * p)
            )
            GestureDragDirection.DOWN -> Offset(
                x = 0f,
                y = buttonSizePx.height * directionHintSpec.verticalTravelFraction * p
            )
            GestureDragDirection.LEFT -> Offset(
                x = -(buttonSizePx.width * directionHintSpec.horizontalTravelFraction * p),
                y = 0f
            )
            GestureDragDirection.RIGHT -> Offset(
                x = buttonSizePx.width * directionHintSpec.horizontalTravelFraction * p,
                y = 0f
            )
        }
    }

    fun captureHintCenter(direction: GestureDragDirection, topLeft: Offset, widthPx: Int, heightPx: Int) {
        directionHintCenterByDirection[direction] = Offset(
            x = topLeft.x + (widthPx / 2f),
            y = topLeft.y + (heightPx / 2f)
        )
    }

    fun flyingStartForDirection(direction: GestureDragDirection, progress: Float): Offset {
        val translation = hintTranslation(direction, progress)
        directionHintCenterByDirection[direction]?.let { center ->
            return center + translation
        }

        val centerX = buttonOriginInRoot.x + (buttonSizePx.width / 2f)
        val centerY = buttonOriginInRoot.y + (buttonSizePx.height / 2f)
        val topY = buttonOriginInRoot.y + directionLabelInsetPx
        val bottomY = buttonOriginInRoot.y + buttonSizePx.height - directionLabelInsetPx
        val leftX = buttonOriginInRoot.x + directionLabelInsetPx
        val rightX = buttonOriginInRoot.x + buttonSizePx.width - directionLabelInsetPx

        val fallback = when (direction) {
            GestureDragDirection.UP -> Offset(centerX, topY)
            GestureDragDirection.DOWN -> Offset(centerX, bottomY)
            GestureDragDirection.LEFT -> Offset(leftX, centerY)
            GestureDragDirection.RIGHT -> Offset(rightX, centerY)
        }

        return fallback + translation
    }

    fun animationProgressFor(direction: GestureDragDirection): Float {
        val live = if (dragDirection == direction) dragProgress else 0f
        if (live > 0f) return live
        if (lastEligibleDragDirection == direction) return lastEligibleDragProgress
        return 0f
    }

    fun startHintRecovery(direction: GestureDragDirection, progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        if (clamped <= 0f) return
        returnHintDirection = direction
        returnHintProgress = clamped
        returnHintAnimationTrigger += 1
    }

    fun invokeSwipe(direction: GestureDragDirection, animationStart: Offset, animationProgress: Float): Boolean {
        val triggered = when (direction) {
            GestureDragDirection.UP -> {
                if (effectiveOnSwipeUp == null) return false
                effectiveOnSwipeUp()
                true
            }
            GestureDragDirection.DOWN -> {
                if (effectiveOnSwipeDown == null) return false
                effectiveOnSwipeDown()
                true
            }
            GestureDragDirection.LEFT -> {
                if (onSwipeLeft == null) return false
                onSwipeLeft()
                true
            }
            GestureDragDirection.RIGHT -> {
                if (onSwipeRight == null) return false
                onSwipeRight()
                true
            }
        }
        if (!triggered) return false
        if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        gestureCompleted = true
        startHintRecovery(direction, animationProgress)
        labelFor(direction)?.let { hint -> triggerFlyingText(hint, animationStart) }
        return true
    }

    fun supportsSwipe(direction: GestureDragDirection): Boolean = when (direction) {
        GestureDragDirection.UP -> effectiveOnSwipeUp != null
        GestureDragDirection.DOWN -> effectiveOnSwipeDown != null
        GestureDragDirection.LEFT -> onSwipeLeft != null
        GestureDragDirection.RIGHT -> onSwipeRight != null
    }

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

    // Gesture feedback should always settle back to idle state.
    LaunchedEffect(gestureCompleted) {
        if (gestureCompleted) {
            delay(130L)
            gestureCompleted = false
        }
    }

    LaunchedEffect(returnHintAnimationTrigger) {
        val direction = returnHintDirection ?: return@LaunchedEffect
        val start = returnHintProgress.coerceIn(0f, 1f)
        if (start <= 0f) return@LaunchedEffect
        animate(
            initialValue = start,
            targetValue = 0f,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
        ) { value, _ ->
            returnHintProgress = value
        }
        if (returnHintDirection == direction) {
            returnHintDirection = null
            returnHintProgress = 0f
        }
    }

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
    val borderColor = rememberButtonBorderColor(
        enabled = enabled,
        pressed = isPressed,
        emphasis = if (buttonType == ButtonType.DIGIT) {
            ButtonBorderEmphasis.DIGIT
        } else {
            ButtonBorderEmphasis.ACCENT
        }
    )

    val effectiveLongPressOptions = longPressOptions
    val onLongPressOptionSelectedEffective = onLongPressOptionSelected

    Box(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .shadow(
                elevation = elevation.dp,
                shape = shape,
                clip = true,
                ambientColor = baseBackgroundColor.copy(alpha = 0.3f),
                spotColor = baseBackgroundColor.copy(alpha = 0.5f)
            )
            .clip(shape)
            .background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.3f))
            .border(CalculatorButtonBorderTokens.Width, borderColor, shape)
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
            .onGloballyPositioned { coordinates ->
                buttonOriginInRoot = coordinates.positionInRoot()
            }
            .semantics(mergeDescendants = true) {
                contentDescription?.let { this.contentDescription = it }
                if (!enabled) stateDescription = "Disabled"
            }
            .pointerInput(
                enabled,
                onClick,
                onLongClick,
                effectiveOnSwipeUp,
                effectiveOnSwipeDown,
                onSwipeLeft,
                onSwipeRight,
                gestureAutoActivation,
                layerUpEnabled,
                layerDownEnabled
            ) {
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
                    var wasAtOrAboveAutoThreshold = false
                    dragProgress = 0f
                    dragDirection = null
                    lastEligibleDragDirection = null
                    lastEligibleDragProgress = 0f
                    returnHintDirection = null
                    returnHintProgress = 0f

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
                                dragProgress = 0f
                                dragDirection = null
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
                                val releaseDirection = detectDragDirection(delta, touchSlop)
                                if (releaseDirection != null &&
                                    supportsSwipe(releaseDirection) &&
                                    releaseDirection.axisDistance(delta) > minDragDistancePx
                                ) {
                                    val releaseProgress = animationProgressFor(releaseDirection)
                                    swipeHandled = invokeSwipe(
                                        releaseDirection,
                                        flyingStartForDirection(releaseDirection, releaseProgress),
                                        releaseProgress
                                    )
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
                            dragProgress = 0f
                            dragDirection = null
                            break
                        }

                        if (change.positionChanged()) {
                            lastPos = change.position
                            if (swipeHandled) {
                                // After auto-trigger, return to idle visuals immediately and wait for finger lift.
                                dragDirection = null
                                dragProgress = 0f
                                change.consume()
                                continue
                            }
                            if (!movedBeyondSlop) {
                                movedBeyondSlop = (lastPos - start).getDistance() > touchSlop
                            }

                            val delta = lastPos - start
                            val effectiveThresholdPx = kotlin.math.max(
                                autoActivationThresholdPx,
                                kotlin.math.min(buttonSizePx.width, buttonSizePx.height) * 0.34f
                            )

                            val detectedDirection = detectDragDirection(delta, touchSlop)
                            val activeDirection = detectedDirection?.takeIf { supportsSwipe(it) }
                            if (activeDirection != null) {
                                val computedProgress = normalizedDragProgress(
                                    axisDistancePx = activeDirection.axisDistance(delta),
                                    thresholdPx = effectiveThresholdPx,
                                    touchSlopPx = touchSlop
                                )
                                dragDirection = activeDirection
                                dragProgress = computedProgress
                                lastEligibleDragDirection = activeDirection
                                lastEligibleDragProgress = computedProgress
                            } else {
                                dragDirection = null
                                dragProgress = 0f
                            }

                            // Auto-activation in modern mode when threshold is reached (no finger lift needed).
                            val reachedAutoThreshold = activeDirection != null && dragProgress >= 1f
                            if (enabled &&
                                activeDirection != null &&
                                reachedAutoThreshold &&
                                !wasAtOrAboveAutoThreshold
                            ) {
                                val autoProgress = animationProgressFor(activeDirection)
                                if (invokeSwipe(
                                        activeDirection,
                                        flyingStartForDirection(activeDirection, autoProgress),
                                        autoProgress
                                    )
                                ) {
                                    swipeHandled = true
                                    isPressed = false
                                    dragProgress = 0f
                                    dragDirection = null
                                }
                            }
                            wasAtOrAboveAutoThreshold = reachedAutoThreshold

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
                    dragProgress = 0f
                    dragDirection = null
                    lastEligibleDragDirection = null
                    lastEligibleDragProgress = 0f
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
                                        fontFamily = CalculatorFontFamily,
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

        val clampedDragProgress = dragProgress.coerceIn(0f, 1f)
        val anyDragActive = dragDirection != null && dragProgress > 0f
        LaunchedEffect(dragDirection) {
            if (dragDirection != null) {
                indicatorDirection = dragDirection
            }
        }
        val indicatorVisibility = if (anyDragActive) 1f else 0f
        val indicatorFillProgress = if (anyDragActive) clampedDragProgress else 0f
        val activeIndicatorDirection = dragDirection ?: indicatorDirection
        val thresholdReached = dragProgress >= 1f
        val indicatorColor = if (thresholdReached) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.primary
        }

        if (activeIndicatorDirection != null && indicatorVisibility > 0.001f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        drawDirectionalProgressOverlay(
                            direction = activeIndicatorDirection,
                            progress = indicatorFillProgress,
                            visibility = indicatorVisibility,
                            color = indicatorColor
                        )
                    }
            )
        }

        effectiveDirectionTexts.up?.let { upText ->
            val active = dragDirection == GestureDragDirection.UP
            val recovering = !active && returnHintDirection == GestureDragDirection.UP && returnHintProgress > 0f
            Text(
                text = upText,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = directionHintSpec.edgePadding)
                    .onGloballyPositioned { coordinates ->
                        captureHintCenter(
                            direction = GestureDragDirection.UP,
                            topLeft = coordinates.positionInRoot(),
                            widthPx = coordinates.size.width,
                            heightPx = coordinates.size.height
                        )
                    }
                    .graphicsLayer {
                        val p = when {
                            active -> clampedDragProgress
                            recovering -> returnHintProgress.coerceIn(0f, 1f)
                            else -> 0f
                        }
                        val translation = hintTranslation(GestureDragDirection.UP, p)
                        translationX = translation.x
                        translationY = translation.y
                        val hintScale = 1f + (0.05f * p)
                        scaleX = hintScale
                        scaleY = hintScale
                        alpha = when {
                            active -> 0.95f
                            anyDragActive -> 0.45f
                            recovering -> 0.55f + (0.25f * (1f - p))
                            else -> 0.9f
                        }
                    },
                style = TextStyle(
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.95f),
                    fontFamily = CalculatorFontFamily,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold
                )
            )
        }

        effectiveDirectionTexts.down?.let { downText ->
            val active = dragDirection == GestureDragDirection.DOWN
            val recovering = !active && returnHintDirection == GestureDragDirection.DOWN && returnHintProgress > 0f
            Text(
                text = downText,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = directionHintSpec.edgePadding)
                    .onGloballyPositioned { coordinates ->
                        captureHintCenter(
                            direction = GestureDragDirection.DOWN,
                            topLeft = coordinates.positionInRoot(),
                            widthPx = coordinates.size.width,
                            heightPx = coordinates.size.height
                        )
                    }
                    .graphicsLayer {
                        val p = when {
                            active -> clampedDragProgress
                            recovering -> returnHintProgress.coerceIn(0f, 1f)
                            else -> 0f
                        }
                        val translation = hintTranslation(GestureDragDirection.DOWN, p)
                        translationX = translation.x
                        translationY = translation.y
                        val hintScale = 1f + (0.05f * p)
                        scaleX = hintScale
                        scaleY = hintScale
                        alpha = when {
                            active -> 0.95f
                            anyDragActive -> 0.45f
                            recovering -> 0.55f + (0.25f * (1f - p))
                            else -> 0.9f
                        }
                    },
                style = TextStyle(
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.95f),
                    fontFamily = CalculatorFontFamily,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold
                )
            )
        }

        effectiveDirectionTexts.left?.let { leftText ->
            val active = dragDirection == GestureDragDirection.LEFT
            val recovering = !active && returnHintDirection == GestureDragDirection.LEFT && returnHintProgress > 0f
            Text(
                text = leftText,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = directionHintSpec.edgePadding)
                    .onGloballyPositioned { coordinates ->
                        captureHintCenter(
                            direction = GestureDragDirection.LEFT,
                            topLeft = coordinates.positionInRoot(),
                            widthPx = coordinates.size.width,
                            heightPx = coordinates.size.height
                        )
                    }
                    .graphicsLayer {
                        val p = when {
                            active -> clampedDragProgress
                            recovering -> returnHintProgress.coerceIn(0f, 1f)
                            else -> 0f
                        }
                        val translation = hintTranslation(GestureDragDirection.LEFT, p)
                        translationX = translation.x
                        translationY = translation.y
                        val hintScale = 1f + (0.05f * p)
                        scaleX = hintScale
                        scaleY = hintScale
                        alpha = when {
                            active -> 0.95f
                            anyDragActive -> 0.45f
                            recovering -> 0.52f + (0.23f * (1f - p))
                            else -> 0.85f
                        }
                    },
                style = TextStyle(
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.95f),
                    fontFamily = CalculatorFontFamily,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold
                )
            )
        }

        effectiveDirectionTexts.right?.let { rightText ->
            val active = dragDirection == GestureDragDirection.RIGHT
            val recovering = !active && returnHintDirection == GestureDragDirection.RIGHT && returnHintProgress > 0f
            Text(
                text = rightText,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = directionHintSpec.edgePadding)
                    .onGloballyPositioned { coordinates ->
                        captureHintCenter(
                            direction = GestureDragDirection.RIGHT,
                            topLeft = coordinates.positionInRoot(),
                            widthPx = coordinates.size.width,
                            heightPx = coordinates.size.height
                        )
                    }
                    .graphicsLayer {
                        val p = when {
                            active -> clampedDragProgress
                            recovering -> returnHintProgress.coerceIn(0f, 1f)
                            else -> 0f
                        }
                        val translation = hintTranslation(GestureDragDirection.RIGHT, p)
                        translationX = translation.x
                        translationY = translation.y
                        val hintScale = 1f + (0.05f * p)
                        scaleX = hintScale
                        scaleY = hintScale
                        alpha = when {
                            active -> 0.95f
                            anyDragActive -> 0.45f
                            recovering -> 0.52f + (0.23f * (1f - p))
                            else -> 0.85f
                        }
                    },
                style = TextStyle(
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.95f),
                    fontFamily = CalculatorFontFamily,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold
                )
            )
        }

        if (activeIndicatorDirection != null && indicatorVisibility > 0.001f) {
            val progress = indicatorFillProgress.coerceIn(0f, 1f)
            when (activeIndicatorDirection) {
                GestureDragDirection.UP, GestureDragDirection.DOWN -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = directionHintSpec.edgePadding)
                            .width(4.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .graphicsLayer {
                                alpha = indicatorVisibility
                                scaleY = 0.92f + (0.08f * indicatorVisibility)
                            }
                            .background(textColor.copy(alpha = 0.18f))
                    ) {
                        Box(
                            modifier = Modifier
                                .align(
                                    if (activeIndicatorDirection == GestureDragDirection.UP) {
                                        Alignment.BottomCenter
                                    } else {
                                        Alignment.TopCenter
                                    }
                                )
                                .fillMaxWidth()
                                .fillMaxHeight(progress)
                                .background(indicatorColor)
                        )
                    }
                }

                GestureDragDirection.LEFT, GestureDragDirection.RIGHT -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = directionHintSpec.edgePadding)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .graphicsLayer {
                                alpha = indicatorVisibility
                                scaleX = 0.92f + (0.08f * indicatorVisibility)
                            }
                            .background(textColor.copy(alpha = 0.18f))
                    ) {
                        Box(
                            modifier = Modifier
                                .align(
                                    if (activeIndicatorDirection == GestureDragDirection.RIGHT) {
                                        Alignment.CenterStart
                                    } else {
                                        Alignment.CenterEnd
                                    }
                                )
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(indicatorColor)
                            )
                    }
                }
            }
        }

        // Main button text
        Text(
            text = text,
            modifier = Modifier.graphicsLayer {
                val p = if (anyDragActive) clampedDragProgress else 0f
                val fade = 1f - (0.35f * p)
                alpha = if (enabled) fade else 0.4f
                val s = 1f - (0.05f * p)
                scaleX = s
                scaleY = s
            },
            style = TextStyle(
                fontSize = 24.sp,
                color = textColor,
                fontFamily = CalculatorFontFamily,
                fontWeight = if (buttonType == ButtonType.OPERATION || buttonType == ButtonType.OPERATION_HIGHLIGHTED) FontWeight.SemiBold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )
    }
}

private enum class GestureDragDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

private data class DirectionHintSpec(
    val edgePadding: Dp = 6.dp,
    val verticalTravelFraction: Float = 0.11f,
    val horizontalTravelFraction: Float = 0.09f
)

private fun normalizedDragProgress(
    axisDistancePx: Float,
    thresholdPx: Float,
    touchSlopPx: Float
): Float {
    val effectiveDistance = (axisDistancePx - touchSlopPx).coerceAtLeast(0f)
    val effectiveThreshold = thresholdPx.coerceAtLeast(1f)
    return (effectiveDistance / effectiveThreshold).coerceIn(0f, 1.2f)
}

private fun detectDragDirection(
    delta: Offset,
    touchSlop: Float
): GestureDragDirection? {
    val absX = kotlin.math.abs(delta.x)
    val absY = kotlin.math.abs(delta.y)
    if (absX <= touchSlop && absY <= touchSlop) return null
    return if (absY >= absX) {
        if (delta.y < 0f) GestureDragDirection.UP else GestureDragDirection.DOWN
    } else {
        if (delta.x < 0f) GestureDragDirection.LEFT else GestureDragDirection.RIGHT
    }
}

private fun GestureDragDirection.axisDistance(delta: Offset): Float = when (this) {
    GestureDragDirection.UP, GestureDragDirection.DOWN -> kotlin.math.abs(delta.y)
    GestureDragDirection.LEFT, GestureDragDirection.RIGHT -> kotlin.math.abs(delta.x)
}

private fun DrawScope.drawDirectionalProgressOverlay(
    direction: GestureDragDirection,
    progress: Float,
    visibility: Float,
    color: Color
) {
    val p = progress.coerceIn(0f, 1f)
    val v = visibility.coerceIn(0f, 1f)
    if (p <= 0f || v <= 0f) return

    val strongAlpha = (0.06f + (0.18f * p)) * v
    val softAlpha = strongAlpha * 0.12f
    val overlayHeight = size.height * p
    val overlayWidth = size.width * p

    when (direction) {
        GestureDragDirection.UP -> {
            val top = size.height - overlayHeight
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = softAlpha),
                        color.copy(alpha = strongAlpha)
                    )
                ),
                topLeft = Offset(0f, top),
                size = Size(size.width, overlayHeight)
            )
        }
        GestureDragDirection.DOWN -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = strongAlpha),
                        color.copy(alpha = softAlpha)
                    )
                ),
                topLeft = Offset.Zero,
                size = Size(size.width, overlayHeight)
            )
        }
        GestureDragDirection.LEFT -> {
            val left = size.width - overlayWidth
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        color.copy(alpha = softAlpha),
                        color.copy(alpha = strongAlpha)
                    )
                ),
                topLeft = Offset(left, 0f),
                size = Size(overlayWidth, size.height)
            )
        }
        GestureDragDirection.RIGHT -> {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        color.copy(alpha = strongAlpha),
                        color.copy(alpha = softAlpha)
                    )
                ),
                topLeft = Offset.Zero,
                size = Size(overlayWidth, size.height)
            )
        }
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
