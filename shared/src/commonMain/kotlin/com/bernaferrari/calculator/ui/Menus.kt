package com.bernaferrari.calculator.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp

/** The keys' resting corner radius; menus share it so they read as part of the same set. */
val KeyRadius = 14.dp

val MenuShape = RoundedCornerShape(KeyRadius)

/**
 * For a DropdownMenu's modifier: takes away the 8dp Material puts above the first item and below
 * the last (it has no parameter for it), so the items run edge to edge of the rounded surface.
 */
fun Modifier.flushMenu(): Modifier = layout { measurable, constraints ->
    val trim = 8.dp.roundToPx()
    val grown = if (constraints.hasBoundedHeight) constraints.copy(maxHeight = constraints.maxHeight + trim * 2) else constraints
    val placeable = measurable.measure(grown)
    layout(placeable.width, (placeable.height - trim * 2).coerceAtLeast(0)) { placeable.place(0, -trim) }
}
