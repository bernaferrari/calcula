package com.bernaferrari.calculator.ui.calculator

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Text at the largest single-line size that fits its width: a measured
 * first guess, corrected from the real layout if the shaped line still overflows. A plain text
 * node underneath, so it keeps its baseline for `alignByBaseline`. Long values can optionally
 * wrap once the minimum readable size is reached.
 */
@Composable
fun FitText(text: String, style: TextStyle, maxSize: TextUnit, minSize: TextUnit, modifier: Modifier = Modifier, wrapAtMinimum: Boolean = false) {
    val measurer = rememberTextMeasurer()
    var available by remember { mutableIntStateOf(-1) }
    val guess = remember(text, style, available, maxSize, minSize) {
        var s = maxSize.value
        if (available >= 0) {
            while (s > minSize.value) {
                val measured = measurer.measure(text, style.copy(fontSize = s.sp), maxLines = 1, softWrap = false, constraints = Constraints())
                if (measured.size.width <= available) break
                s -= 1f
            }
        }
        s.coerceAtLeast(minSize.value)
    }
    var size by remember(guess) { mutableFloatStateOf(guess) }
    var settled by remember(guess) { mutableStateOf(false) }
    BasicText(
        text,
        style = style.copy(fontSize = size.sp),
        maxLines = if (wrapAtMinimum && size <= minSize.value) Int.MAX_VALUE else 1,
        softWrap = wrapAtMinimum && size <= minSize.value,
        onTextLayout = { layout ->
            if (layout.didOverflowWidth && size > minSize.value) size -= 1f else settled = available >= 0
        },
        modifier = modifier
            .layout { measurable, constraints ->
                if (constraints.maxWidth != available) available = constraints.maxWidth
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            // Hidden until it fits, so a too-big first frame never flashes.
            .drawWithContent { if (settled) drawContent() },
    )
}
