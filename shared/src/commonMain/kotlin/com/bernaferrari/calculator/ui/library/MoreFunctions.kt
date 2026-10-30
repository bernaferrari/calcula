package com.bernaferrari.calculator.ui.library

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.acosh
import kotlin.math.asinh
import kotlin.math.atanh
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.tan
import kotlin.math.tanh

/** A function the keypad doesn't have room for, with its curve for the card (in radians). */
private class More(val name: String, val curve: (Double) -> Double)

private val GROUPS: List<Pair<StringResource, List<More>>> = listOf(
    Res.string.library_more_hyperbolic to listOf(
        More("sinh") { sinh(it) }, More("cosh") { cosh(it) }, More("tanh") { tanh(it) },
        More("sinh⁻¹") { asinh(it) }, More("cosh⁻¹") { acosh(it) }, More("tanh⁻¹") { atanh(it) },
    ),
    Res.string.library_more_trigonometry to listOf(
        More("sec") { 1 / cos(it) }, More("csc") { 1 / sin(it) }, More("cot") { 1 / tan(it) },
    ),
    Res.string.library_more_rounding to listOf(
        More("floor") { floor(it) }, More("ceil") { ceil(it) }, More("round") { floor(it + 0.5) },
        More("sgn") { sign(it) }, More("abs") { abs(it) },
    ),
    Res.string.library_more_logarithms to listOf(
        More("log₂") { log2(it) },
    ),
)

/**
 * Functions beyond the keys, grouped, each on a card with a sketch of its curve so the shape is
 * known before it's picked. A tap types `name(` at the cursor, like the keys do.
 */
@Composable
internal fun MoreTab(onPick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 104.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GROUPS.forEach { (title, items) ->
            item(key = title.key, span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    stringResource(title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp),
                )
            }
            items(items, key = { it.name }) { f -> FunctionCard(f) { onPick("${f.name}(") } }
        }
    }
}

@Composable
private fun FunctionCard(f: More, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable(onClick = onClick).semantics { contentDescription = f.name },
    ) {
        Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 10.dp)) {
            Text(f.name, style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 22.sp), color = colors.onSurface)
            val axis = colors.outlineVariant
            val line = colors.primary
            Canvas(Modifier.padding(top = 6.dp).fillMaxWidth().height(40.dp).clipToBounds()) {
                // −4 to 4 across, −2.5 to 2.5 up; jumps and poles break the line instead of joining.
                val (xMin, xMax, yMax) = Triple(-4.0, 4.0, 2.5)
                fun px(x: Double) = ((x - xMin) / (xMax - xMin) * size.width).toFloat()
                fun py(y: Double) = ((1 - (y + yMax) / (2 * yMax)) * size.height).toFloat()
                drawLine(axis, Offset(0f, py(0.0)), Offset(size.width, py(0.0)), strokeWidth = 1.dp.toPx())
                drawLine(axis, Offset(px(0.0), 0f), Offset(px(0.0), size.height), strokeWidth = 1.dp.toPx())
                val path = Path()
                var previous: Double? = null
                val steps = 160
                for (i in 0..steps) {
                    val x = xMin + (xMax - xMin) * i / steps
                    val y = f.curve(x)
                    val usable = y.isFinite() && abs(y) <= yMax * 3
                    if (!usable) { previous = null; continue }
                    val jump = previous?.let { abs(y - it) > yMax * 0.6 } ?: true
                    if (jump) path.moveTo(px(x), py(y)) else path.lineTo(px(x), py(y))
                    previous = y
                }
                drawPath(path, line, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}
