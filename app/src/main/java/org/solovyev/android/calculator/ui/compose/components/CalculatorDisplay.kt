package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
 * - On error: shows last valid result with reduced opacity (ghosted)
 * - Copy button for results (click or long-press on display)
 * - Material3 theming with clean, modern design
 *
 * @param state The current display state containing text, validity, and result
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
    // Track the last valid result to show with reduced opacity on errors
    var lastValidText by remember { mutableStateOf("") }
    
    // Update lastValidText when we get a valid result with non-empty text
    if (state.valid && state.text.isNotEmpty()) {
        lastValidText = state.text
    }

    // Determine what to display and how
    val displayText: String
    val textAlpha: Float
    val textColor = MaterialTheme.colorScheme.onSurface
    
    if (state.valid) {
        // Valid result - show normally
        displayText = state.text
        textAlpha = 1f
    } else {
        // Error state - show last valid result with reduced opacity (ghosted)
        // Don't show the error message, just ghost the previous value
        displayText = lastValidText
        textAlpha = 0.4f
    }

    val fontSize = calculateFontSize(
        text = displayText,
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
            text = displayText,
            style = TextStyle(
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Normal,
                fontFamily = CalculatorFontFamily,
                textAlign = TextAlign.End
            ),
            maxLines = 1,
            modifier = Modifier.alpha(textAlpha)
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
