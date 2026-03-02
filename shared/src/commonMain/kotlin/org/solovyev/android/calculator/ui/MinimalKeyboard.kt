package org.solovyev.android.calculator.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Deprecated("Simple mode has been removed. Use ModernCalculatorKeyboard or UnifiedCalculatorKeyboard instead.", ReplaceWith("ModernCalculatorKeyboard"))
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MinimalCalculatorKeyboard(
    actions: KeyboardActions,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalWindowInfo.current
    val isTablet = windowInfo.containerSize.width > 600

    val horizontalSpacing = if (isTablet) 16.dp else 10.dp
    val verticalSpacing = if (isTablet) 14.dp else 10.dp
    val buttonFontSize = if (isTablet) 36.sp else 28.sp
    val buttonCornerRadius = if (isTablet) 20.dp else 16.dp

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
        ) {
            CalculatorButton(
                text = "C",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onClear() },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "+/-",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onSpecialClick("±") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "%",
                buttonType = ButtonType.CONTROL,
                onClick = { actions.onOperatorClick("%") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "/",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("/") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
        ) {
            CalculatorButton(
                text = "7",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("7") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "8",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("8") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "9",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("9") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "*",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("*") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
        ) {
            CalculatorButton(
                text = "4",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("4") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "5",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("5") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "6",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("6") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "-",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("-") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
        ) {
            CalculatorButton(
                text = "1",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("1") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "2",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("2") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "3",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("3") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "+",
                buttonType = ButtonType.OPERATION,
                onClick = { actions.onOperatorClick("+") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
        ) {
            CalculatorButton(
                text = "0",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick("0") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(2f)
            )
            CalculatorButton(
                text = ".",
                buttonType = ButtonType.DIGIT,
                onClick = { actions.onNumberClick(".") },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "=",
                buttonType = ButtonType.OPERATION_HIGHLIGHTED,
                onClick = { actions.onEquals() },
                onLongClick = { actions.onSimplify() },
                fontSize = buttonFontSize,
                cornerRadius = buttonCornerRadius,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalculatorButton(
    text: String,
    buttonType: ButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    fontSize: TextUnit = 28.sp,
    cornerRadius: Dp = 16.dp
) {
    var isPressed by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val hapticsEnabled = LocalCalculatorHapticsEnabled.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ButtonScale"
    )

    // Use calculator-specific color scheme
    val calcColors = org.solovyev.android.calculator.ui.theme.calculatorColors()

    val backgroundColor = when (buttonType) {
        ButtonType.DIGIT -> calcColors.digitButtonBackground
        ButtonType.OPERATION -> calcColors.operatorButtonBackground
        ButtonType.CONTROL -> calcColors.controlButtonBackground
        ButtonType.OPERATION_HIGHLIGHTED -> calcColors.equalsButtonBackground
        ButtonType.SPECIAL -> calcColors.scientificButtonBackground
        ButtonType.MEMORY -> calcColors.memoryButtonBackground
    }

    val textColor = when (buttonType) {
        ButtonType.DIGIT -> calcColors.digitButtonText
        ButtonType.OPERATION -> calcColors.operatorButtonText
        ButtonType.CONTROL -> calcColors.controlButtonText
        ButtonType.OPERATION_HIGHLIGHTED -> calcColors.equalsButtonText
        ButtonType.SPECIAL -> calcColors.scientificButtonText
        ButtonType.MEMORY -> calcColors.memoryButtonText
    }

    Box(
        modifier = modifier
            .aspectRatio(if (text == "0") 2f else 1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 2.dp else 4.dp,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .pointerInput(onClick, onLongClick) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        if (hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = {
                        if (hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onLongClick?.invoke()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                color = textColor,
                fontWeight = if (buttonType == ButtonType.CONTROL) FontWeight.Medium else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        )
    }
}
