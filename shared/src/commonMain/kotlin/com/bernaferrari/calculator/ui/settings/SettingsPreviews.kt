package com.bernaferrari.calculator.ui.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.bernaferrari.calculator.domain.model.FlickHints
import com.bernaferrari.calculator.ui.motionSpec
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex

/*
 * Small pictures of what a setting does, in the keypad's own colors, so nobody has to leave
 * Settings to find out. They are drawings, not controls: the rows around them carry the semantics.
 */

/** The same right angle, written each way: 90° or π/2. */
@Composable
fun AnglePicture(radians: Boolean) {
    val line = MaterialTheme.colorScheme.outline
    val arc = MaterialTheme.colorScheme.primary
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(Modifier.size(26.dp)) {
            val stroke = 1.75.dp.toPx()
            val corner = Offset(stroke, size.height - stroke)
            drawLine(line, corner, Offset(size.width, corner.y), stroke, StrokeCap.Round)
            drawLine(line, corner, Offset(corner.x, 0f), stroke, StrokeCap.Round)
            val radius = size.width * 0.62f
            drawArc(arc, -90f, 90f, false, Offset(corner.x - radius, corner.y - radius), Size(radius * 2, radius * 2), style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Text(
            if (radians) "π/2" else "90°",
            style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 20.sp, lineHeight = 24.sp),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Two plus three, as it is typed: `2 + 3 =` or `2 ↵ 3 +`. */
@Composable
fun EntryPicture(rpn: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val digit = scheme.surfaceContainerHigh to scheme.onSurface
    val operator = scheme.secondaryContainer to scheme.onSecondaryContainer
    val equals = scheme.primary to scheme.onPrimary
    val keys = if (rpn) listOf("2" to digit, "↵" to equals, "3" to digit, "+" to operator)
    else listOf("2" to digit, "+" to operator, "3" to digit, "=" to equals)
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        keys.forEach { (glyph, colors) ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(26.dp).clip(RoundedCornerShape(8.dp)).background(colors.first),
            ) {
                Text(glyph, style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 14.sp, lineHeight = 14.sp), color = colors.second)
            }
        }
    }
}

/** The 7 key and its two flicks (sin above, sin⁻¹ below), hinted the way [hints] hints them. */
@Composable
fun HintPicture(hints: FlickHints) {
    val content = MaterialTheme.colorScheme.onSurface
    val small = TextStyle(fontFamily = GoogleSansFlex, fontSize = 9.sp, lineHeight = 9.sp, fontWeight = FontWeight.Medium)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Text("7", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 20.sp, lineHeight = 20.sp), color = content)
        val faint = content.copy(alpha = 0.75f)
        if (hints == FlickHints.LABELS || hints == FlickHints.HALF) {
            Text("sin", style = small, color = faint, modifier = Modifier.align(Alignment.TopCenter).offset(y = 5.dp))
        }
        if (hints == FlickHints.LABELS) {
            Text("sin⁻¹", style = small, color = faint, modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-5).dp))
        }
        val dot = Modifier.size(3.5.dp).background(content.copy(alpha = 0.4f), CircleShape)
        if (hints == FlickHints.DOTS) Box(Modifier.align(Alignment.TopCenter).offset(y = 7.dp).then(dot))
        if (hints == FlickHints.DOTS || hints == FlickHints.HALF) Box(Modifier.align(Alignment.BottomCenter).offset(y = (-7).dp).then(dot))
    }
}

/** The frame a row's leading picture sits in: a scrap of the calculator's own background. */
@Composable
private fun Thumbnail(content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(ThumbnailSize).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface),
    ) { content() }
}

private val ThumbnailSize = 44.dp

@Composable
private fun shown(on: Boolean): Float =
    animateFloatAsState(if (on) 1f else 0f, motionSpec(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)), label = "shown").value

/** A tiny display: the answer, and above it the earlier sums that come and go with the switch. */
@Composable
fun TapeThumbnail(on: Boolean) {
    val shown = shown(on)
    val line = TextStyle(fontFamily = GoogleSansFlex, fontSize = 7.5.sp, lineHeight = 9.sp, fontWeight = FontWeight.Medium)
    Thumbnail {
        Box(Modifier.size(ThumbnailSize).padding(horizontal = 6.dp)) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp)
                    // The older lines sink into the answer as they go, the way the tape closes.
                    .graphicsLayer {
                        alpha = shown.coerceIn(0f, 1f)
                        translationY = (1f - shown) * 5.dp.toPx()
                    },
            ) {
                Text("1+2=3", style = line, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), maxLines = 1, softWrap = false)
                Text("3×4=12", style = line, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, softWrap = false)
            }
            // Alone, the answer sits in the middle; the tape pushes it down to make room.
            Text(
                "12",
                style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 15.sp, lineHeight = 15.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterEnd).offset(y = lerp(0f, 9.5f, shown).dp),
            )
        }
    }
}

/** A corner of the keypad, with the slim MC M+ M− MR row that appears above it. */
@Composable
fun MemoryThumbnail(on: Boolean) {
    val memory = MaterialTheme.colorScheme.primary
    val key = MaterialTheme.colorScheme.outlineVariant
    val shown = shown(on)
    Thumbnail {
        Canvas(Modifier.size(ThumbnailSize)) {
            val cell = 6.dp.toPx()
            val gap = 2.dp.toPx()
            val left = (size.width - cell * 4 - gap * 3) / 2
            val alpha = shown.coerceIn(0f, 1f)
            repeat(4) { column ->
                val x = left + column * (cell + gap)
                drawRoundRect(memory.copy(alpha = alpha), Offset(x, 11.5.dp.toPx()), Size(cell, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
                repeat(2) { row ->
                    // The keys make room: they sit centred alone, and move down when the row arrives.
                    val y = lerp(15f, 18.5f, shown).dp.toPx() + row * (cell + gap)
                    drawRoundRect(key, Offset(x, y), Size(cell, cell), CornerRadius(2.dp.toPx()))
                }
            }
        }
    }
}

/** One key with a flick above and below it: the three things Customize keys changes. */
@Composable
fun KeysThumbnail() {
    val accent = MaterialTheme.colorScheme.primary
    Thumbnail {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 6.dp).size(width = 24.dp, height = 32.dp).clip(RoundedCornerShape(7.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Text("7", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 13.sp, lineHeight = 13.sp), color = MaterialTheme.colorScheme.onSurface)
            val dot = Modifier.size(3.dp).background(accent, CircleShape)
            Box(Modifier.align(Alignment.TopCenter).offset(y = 4.dp).then(dot))
            Box(Modifier.align(Alignment.BottomCenter).offset(y = (-4).dp).then(dot))
        }
    }
}
