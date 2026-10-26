package com.bernaferrari.calculator.ui.graph

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.bernaferrari.calculator.resources.*

/**
 * One color per function: the theme's accent first, then orange, green, blue, purple and teal, chosen to stay apart
 * from every palette and from each other, lighter on dark backgrounds.
 */
@Composable
internal fun curveColors(): List<Color> {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return listOf(
        MaterialTheme.colorScheme.primary,
        if (dark) Color(0xFFFFB06B) else Color(0xFFD9640B),
        if (dark) Color(0xFF7FD99A) else Color(0xFF1E8E3E),
        if (dark) Color(0xFF8AB4F8) else Color(0xFF1A73E8),
        if (dark) Color(0xFFD7AEFB) else Color(0xFF9334E6),
        if (dark) Color(0xFF78D9EC) else Color(0xFF007B83),
    )
}
