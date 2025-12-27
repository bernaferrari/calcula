package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.solovyev.android.calculator.ui.compose.theme.CalculatorFontFamily
import org.solovyev.android.calculator.DisplayState

/**
 * Calculator display component that shows the calculation result.
 *
 * Features:
 * - Auto-resizing text to fit content
 * - Error state with red color styling
 * - Copy button for results (click or long-press on display)
 * - Animated result changes with smooth slide transitions
 * - Horizontal scrolling for long results
 * - Material3 theming with clean, modern design
 * - Haptic feedback on long press
 *
 * @param state The current display state containing text, validity, and result
 * @param onCopy Callback invoked when the copy button is clicked or display is long-pressed
 * @param modifier Modifier to be applied to the display
 * @param minTextSize Minimum text size for auto-resizing
 * @param maxTextSize Maximum text size for auto-resizing
 */
@Composable
fun CalculatorDisplay(
    state: DisplayState,
    modifier: Modifier = Modifier,
    minTextSize: TextUnit = 20.sp,
    maxTextSize: TextUnit = 48.sp
) {
    val textColor = if (state.valid) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    val fontSize = calculateFontSize(
        text = state.text,
        minSize = minTextSize,
        maxSize = maxTextSize
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = state.text,
            style = TextStyle(
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Normal,
                fontFamily = CalculatorFontFamily,
                textAlign = TextAlign.End
            ),
            maxLines = 1
        )
    }
}

/**
 * Calculates an appropriate font size based on text length.
 * Longer text gets smaller font size for better fit.
 */
@Composable
private fun calculateFontSize(
    text: String,
    minSize: TextUnit,
    maxSize: TextUnit
): TextUnit {
    val length = text.length
    val size = when {
        length == 0 -> maxSize
        length < 8 -> maxSize
        length < 12 -> 40.sp
        length < 16 -> 32.sp
        length < 20 -> 28.sp
        else -> minSize
    }

    // Coerce between min and max using value comparison
    return when {
        size.value < minSize.value -> minSize
        size.value > maxSize.value -> maxSize
        else -> size
    }
}
