package com.bernaferrari.calculator.ui.graph

import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.bernaferrari.calculator.resources.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal val GraphBarHeight = 48.dp

/**
 * Dragging the graph's bar sizes its keypad: the top of the keys follows the finger (five rows
 * share the change), with a little give past the limits, a tick at each limit and a soft catch at
 * the default size. Unlike the calculator there's no history to pull into, so that's all it does.
 */
@Composable
internal fun Modifier.keypadResize(
    rowHeight: () -> Dp,
    limits: () -> ClosedRange<Dp>,
    defaultRow: () -> Dp,
    onRow: (Dp?) -> Unit,
    onDragging: (Boolean) -> Unit,
    onTick: () -> Unit,
    onDone: (Dp) -> Unit,
): Modifier {
    val row by rememberUpdatedState(rowHeight)
    val range by rememberUpdatedState(limits)
    val home by rememberUpdatedState(defaultRow)
    val done by rememberUpdatedState(onDone)
    return pointerInput(Unit) {
        var start = 0f
        var offset = 0f
        var current = 0.dp
        var atLimit = false
        var atHome = false
        detectVerticalDragGestures(
            onDragStart = {
                start = row().toPx()
                offset = 0f
                current = row()
                atLimit = false
                atHome = false
                onDragging(true)
            },
            onVerticalDrag = { change, dy ->
                change.consume()
                offset += dy
                val r = range()
                val min = r.start.toPx()
                val max = r.endInclusive.toPx()
                val wanted = start - offset / 5f
                val target = home().toPx()
                val nearHome = abs(wanted - target) < 3.dp.toPx()
                if (nearHome && !atHome) onTick()
                atHome = nearHome
                val clamped = when {
                    nearHome -> target
                    wanted < min -> min - (min - wanted) * 0.15f
                    wanted > max -> max + (wanted - max) * 0.15f
                    else -> wanted
                }
                current = clamped.toDp()
                onRow(current)
                val limited = wanted <= min || wanted >= max
                if (limited && !atLimit) onTick()
                atLimit = limited
            },
            onDragEnd = {
                onDragging(false)
                val r = range()
                val settled = current.coerceIn(r.start, r.endInclusive)
                onRow(settled)
                done(settled)
            },
            onDragCancel = {
                onDragging(false)
                onRow(null)
            },
        )
    }
}
