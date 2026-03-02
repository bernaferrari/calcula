package org.solovyev.android.calculator.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.solovyev.android.calculator.ui.tokens.CalculatorButtonBorderTokens

internal enum class ButtonBorderEmphasis {
    DIGIT,
    ACCENT
}

@Composable
internal fun rememberButtonBorderColor(
    enabled: Boolean,
    pressed: Boolean,
    emphasis: ButtonBorderEmphasis
): Color {
    val targetAlpha = when {
        !enabled -> CalculatorButtonBorderTokens.AccentAlpha * 0.5f
        pressed -> CalculatorButtonBorderTokens.PressedAlpha
        emphasis == ButtonBorderEmphasis.DIGIT -> CalculatorButtonBorderTokens.DigitAlpha
        else -> CalculatorButtonBorderTokens.AccentAlpha
    }
    val animatedAlpha = animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 130),
        label = "buttonBorderAlpha"
    ).value
    return MaterialTheme.colorScheme.outlineVariant.copy(alpha = animatedAlpha)
}
