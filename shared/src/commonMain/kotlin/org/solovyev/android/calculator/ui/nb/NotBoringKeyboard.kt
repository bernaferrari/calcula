package org.solovyev.android.calculator.ui.nb

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.solovyev.android.calculator.ui.KeyboardActions
import org.solovyev.android.calculator.ui.LocalCalculatorHapticsEnabled
import org.solovyev.android.calculator.ui.LocalKeyboardIcons
import org.solovyev.android.calculator.ui.CalculatorFontFamily

/**
 * Not Boring Keyboard - Clean, minimal, confident.
 * 
 * Philosophy:
 * - Buttons are just buttons - uniform, quiet
 * - Only "=" gets accent color - it's the action
 * - Scientific functions revealed by swipe UP anywhere
 * - Generous spacing, feels airy
 * - Satisfying press animations
 */
@Composable
fun NotBoringKeyboard(
    actions: KeyboardActions,
    onSwipeUp: () -> Unit, // Reveal scientific
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val hapticsEnabled = LocalCalculatorHapticsEnabled.current
    val density = LocalDensity.current
    
    val touchSlop = with(density) { 40.dp.toPx() }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val startY = down.position.y
                    
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        
                        if (change == null || change.changedToUpIgnoreConsumed()) {
                            break
                        }
                        
                        val dragY = startY - change.position.y
                        
                        if (dragY > touchSlop) {
                            if (hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onSwipeUp()
                            change.consume()
                            break
                        }
                    }
                }
            },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Clear, (, ), ÷
        KeyboardRow(weight = 1f) {
            NbButton("C", ButtonStyle.Control) { actions.onClear() }
            NbButton("(", ButtonStyle.Control) { actions.onSpecialClick("(") }
            NbButton(")", ButtonStyle.Control) { actions.onSpecialClick(")") }
            NbButton("÷", ButtonStyle.Operation) { actions.onOperatorClick("/") }
        }
        
        // Row 2: 7, 8, 9, ×
        KeyboardRow(weight = 1f) {
            NbButton("7") { actions.onNumberClick("7") }
            NbButton("8") { actions.onNumberClick("8") }
            NbButton("9") { actions.onNumberClick("9") }
            NbButton("×", ButtonStyle.Operation) { actions.onOperatorClick("×") }
        }
        
        // Row 3: 4, 5, 6, −
        KeyboardRow(weight = 1f) {
            NbButton("4") { actions.onNumberClick("4") }
            NbButton("5") { actions.onNumberClick("5") }
            NbButton("6") { actions.onNumberClick("6") }
            NbButton("−", ButtonStyle.Operation) { actions.onOperatorClick("−") }
        }
        
        // Row 4: 1, 2, 3, +
        KeyboardRow(weight = 1f) {
            NbButton("1") { actions.onNumberClick("1") }
            NbButton("2") { actions.onNumberClick("2") }
            NbButton("3") { actions.onNumberClick("3") }
            NbButton("+", ButtonStyle.Operation) { actions.onOperatorClick("+") }
        }
        
        // Row 5: ⌫, 0, ., =
        KeyboardRow(weight = 1f) {
            NbButton("", ButtonStyle.Control, icon = LocalKeyboardIcons.current.backspace) { 
                actions.onDelete() 
            }
            NbButton("0") { actions.onNumberClick("0") }
            NbButton(".") { actions.onNumberClick(".") }
            NbButton("=", ButtonStyle.Equals) { 
                if (hapticsEnabled) {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                }
                actions.onEquals() 
            }
        }
    }
}

@Composable
private fun ColumnScope.KeyboardRow(
    weight: Float = 1f,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(weight),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

private enum class ButtonStyle {
    Digit,       // Neutral
    Operation,   // Subtle accent
    Control,     // Muted
    Equals       // Full accent - THE action
}

@Composable
private fun RowScope.NbButton(
    text: String,
    style: ButtonStyle = ButtonStyle.Digit,
    icon: Painter? = null,
    onClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val hapticsEnabled = LocalCalculatorHapticsEnabled.current
    
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    // Colors based on style
    val (bgColor, textColor) = when (style) {
        ButtonStyle.Digit -> {
            MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
        }
        ButtonStyle.Operation -> {
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }
        ButtonStyle.Control -> {
            MaterialTheme.colorScheme.surfaceContainerLow to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        }
        ButtonStyle.Equals -> {
            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        }
    }
    
    val fontSize = when (style) {
        ButtonStyle.Equals -> 36.sp
        ButtonStyle.Control -> 24.sp
        else -> 28.sp
    }
    
    val fontWeight = when (style) {
        ButtonStyle.Equals -> FontWeight.Bold
        ButtonStyle.Control -> FontWeight.Medium
        else -> FontWeight.Normal
    }
    
    val shape = RoundedCornerShape(20.dp)
    
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // We handle our own scale animation
            ) {
                if (hapticsEnabled && style != ButtonStyle.Equals) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                onClick()
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isPressed = true
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.all { !it.pressed }) {
                                break
                            }
                        }
                    } finally {
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
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = CalculatorFontFamily,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = textColor
                )
            )
        }
    }
}
