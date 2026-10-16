package com.bernaferrari.calculator.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Softly fades scrolling content out over its last [bottom] (e.g. under transparent system bars). */
fun Modifier.fadeBottomEdge(bottom: Dp): Modifier = if (bottom <= 0.dp) this else this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val h = size.height.coerceAtLeast(1f)
        val start = (1f - bottom.toPx() / h).coerceIn(0f, 1f)
        drawRect(
            Brush.verticalGradient(start to Color.Black, 1f to Color.Transparent),
            blendMode = BlendMode.DstIn,
        )
    }
