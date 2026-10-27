package com.bernaferrari.calculator.ui.graph

import androidx.compose.ui.unit.max
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.bernaferrari.calculator.domain.math.Curve
import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.domain.math.Exact
import com.bernaferrari.calculator.domain.math.Field
import com.bernaferrari.calculator.domain.math.KeyPoint
import com.bernaferrari.calculator.domain.math.KeyPoints
import com.bernaferrari.calculator.domain.math.MathContext
import com.bernaferrari.calculator.domain.math.NumberFormatter
import com.bernaferrari.calculator.domain.math.RoundingMode
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

/**
 * Every function's curve over the window that starts at [xMin] and [yMax], in its color. Shared
 * by the plot, the recent list's thumbnails and the image that gets shared.
 */
internal fun DrawScope.drawCurves(state: GraphState, colors: List<Color>, xMin: Double, yMax: Double, unitsPerPx: Double, sy: (Double) -> Float, strokePx: Float) {
    state.functions.forEachIndexed { index, f ->
        val color = colors[index % colors.size]
        val plot = when (val curve = state.curves[index]) {
            is Curve.Explicit -> curve.plot
            is Curve.Implicit -> {
                drawImplicit(curve.field, xMin, yMax, unitsPerPx, color)
                return@forEachIndexed
            }
            null -> return@forEachIndexed
        }
        val path = Path()
        var drawing = false
        var lastY = 0f
        val step = 2f
        var px = 0f
        while (px <= size.width) {
            val y = plot.at(xMin + px * unitsPerPx)
            val py = sy(y)
            val ok = y.isFinite() && abs(py) < size.height * 4
            // Break the line at holes and at jumps (asymptotes) instead of drawing a wall.
            if (!ok || (drawing && abs(py - lastY) > size.height)) {
                drawing = false
            } else if (!drawing) {
                path.moveTo(px, py)
                drawing = true
            } else {
                path.lineTo(px, py)
            }
            lastY = py
            px += step
        }
        drawPath(path, color, style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** A key point on screen: which curve it belongs to, and whether it's on the one being edited. */
internal data class ShownPoint(val point: KeyPoint, val function: Int, val onActive: Boolean)

internal fun keyPoints(state: GraphState, size: IntSize, gapPx: Float): List<ShownPoint> {
    if (size.width == 0) return emptyList()
    val v = state.viewport
    val half = v.span / 2
    val active = (state.curves.getOrNull(state.active) as? Curve.Explicit)?.plot ?: return emptyList()
    val points = KeyPoints.of(active, v.centerX - half, v.centerX + half).map { ShownPoint(it, state.active, true) }
    val meets = state.curves.flatMapIndexed { i, curve ->
        val other = (curve as? Curve.Explicit)?.plot
        if (i == state.active || other == null) emptyList()
        else KeyPoints.intersections(active, other, v.centerX - half, v.centerX + half).map { ShownPoint(it, state.active, true) }
    }
    val yHalf = half * size.height / size.width
    val visible = (points + meets).filter { it.point.y in (v.centerY - yHalf)..(v.centerY + yHalf) }
    // A fast wiggle has dozens of roots and turns; rings closer than a fingertip become a chain
    // of beads, so keep only those with room around them (intersections first: they matter most).
    val room = v.span / size.width * gapPx
    val kept = mutableListOf<ShownPoint>()
    for (p in visible.sortedBy { if (it.point.kind == KeyPoint.Kind.INTERSECTION) 0 else 1 }) {
        if (kept.none { abs(it.point.x - p.point.x) < room && abs(it.point.y - p.point.y) < room }) kept += p
    }
    return kept
}

internal fun KeyPoint.Kind.title() = when (this) {
    KeyPoint.Kind.ROOT -> Res.string.graph_point_root
    KeyPoint.Kind.MAXIMUM -> Res.string.graph_point_max
    KeyPoint.Kind.MINIMUM -> Res.string.graph_point_min
    KeyPoint.Kind.Y_INTERCEPT -> Res.string.graph_point_y_intercept
    KeyPoint.Kind.INTERSECTION -> Res.string.graph_point_intersection
}

/**
 * Key points as small rings on the curve, like a student marks them in pencil; the one you tapped
 * fills in and says what it is and where.
 */
internal fun DrawScope.drawKeyPoints(
    points: List<ShownPoint>,
    selected: ShownPoint?,
    colors: List<Color>,
    sx: (Double) -> Float,
    sy: (Double) -> Float,
    scheme: androidx.compose.material3.ColorScheme,
    measurer: androidx.compose.ui.text.TextMeasurer,
    kindStyle: TextStyle,
    style: TextStyle,
    kinds: Map<KeyPoint.Kind, String>,
    formatter: DisplayFormatter,
) {
    for (p in points) {
        val center = Offset(sx(p.point.x), sy(p.point.y))
        val color = colors[p.function % colors.size]
        if (p == selected) {
            drawCircle(color.copy(alpha = 0.2f), 12.dp.toPx(), center)
            drawCircle(color, 6.dp.toPx(), center)
        } else {
            drawCircle(scheme.surfaceContainerLow, 5.dp.toPx(), center)
            drawCircle(color, 5.dp.toPx(), center, style = Stroke(2.dp.toPx()))
        }
    }
    val p = selected?.takeIf { it in points } ?: return
    val center = Offset(sx(p.point.x), sy(p.point.y))
    val text = buildAnnotatedString {
        withStyle(kindStyle.toSpanStyle().copy(color = scheme.onSurfaceVariant)) { append(kinds.getValue(p.point.kind)) }
        val comma = if (formatter.decimal == ',') "; " else ", "
        val decimals = "(" + formatter.number(readout(p.point.x)) + comma + formatter.number(readout(p.point.y)) + ")"
        val exactX = Exact.recognize(p.point.x)
        val exactY = Exact.recognize(p.point.y)
        if (exactX != null || exactY != null) {
            // π/2 says more than 1.5708; the decimals stay underneath.
            fun exact(v: Double, e: String?) = e ?: formatter.number(readout(v))
            append("\n(" + exact(p.point.x, exactX) + comma + exact(p.point.y, exactY) + ")")
            withStyle(kindStyle.toSpanStyle().copy(color = scheme.onSurfaceVariant)) { append("\n≈ $decimals") }
        } else {
            append("\n$decimals")
        }
    }
    val layout = measurer.measure(text, style)
    val pad = 10.dp.toPx()
    val w = layout.size.width + pad * 2
    val h = layout.size.height + pad * 2
    val gap = 16.dp.toPx()
    // Above the point, or below it near the top; always inside the plot.
    val top = if (center.y - gap - h > 0f) center.y - gap - h else center.y + gap
    val left = (center.x - w / 2).coerceIn(pad, max(pad, size.width - w - pad))
    // The theme's own raised surface, with a hairline edge to lift it off the plot, in light and dark.
    val corner = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
    drawRoundRect(scheme.surfaceContainerHighest, Offset(left, top), Size(w, h), corner)
    drawRoundRect(scheme.outlineVariant, Offset(left, top), Size(w, h), corner, style = Stroke(1.dp.toPx()))
    drawText(layout, color = scheme.onSurface, topLeft = Offset(left + pad, top + pad))
}

/**
 * An equation in x and y, drawn where its two sides agree, by marching squares: sample the
 * difference on a fine grid, and wherever it changes sign along a cell's edge the curve passes
 * between the two samples. Cells straddling a pole (tan, 1/…) flip sign without a zero; their
 * centre is larger than any corner, and they are skipped.
 */
internal fun DrawScope.drawImplicit(field: Field, xMin: Double, yMax: Double, unitsPerPx: Double, color: Color) {
    val cell = 3.dp.toPx()
    val cols = ceil(size.width / cell).toInt()
    val rows = ceil(size.height / cell).toInt()
    val stride = cols + 1
    val values = DoubleArray(stride * (rows + 1))
    for (r in 0..rows) for (c in 0..cols) {
        values[r * stride + c] = field.at(xMin + c * cell * unitsPerPx, yMax - r * cell * unitsPerPx)
    }
    val path = Path()
    val points = FloatArray(8)
    for (r in 0 until rows) for (c in 0 until cols) {
        val a = values[r * stride + c] // top left
        val b = values[r * stride + c + 1] // top right
        val d = values[(r + 1) * stride + c + 1] // bottom right
        val e = values[(r + 1) * stride + c] // bottom left
        if (!a.isFinite() || !b.isFinite() || !d.isFinite() || !e.isFinite()) continue
        val positive = (a > 0).toInt() + (b > 0).toInt() + (d > 0).toInt() + (e > 0).toInt()
        if (positive == 0 || positive == 4) continue
        val x0 = c * cell
        val y0 = r * cell
        val centre = field.at(xMin + (x0 + cell / 2) * unitsPerPx, yMax - (y0 + cell / 2) * unitsPerPx)
        if (!centre.isFinite() || abs(centre) > maxOf(abs(a), abs(b), abs(d), abs(e))) continue
        var n = 0
        fun cross(v1: Double, v2: Double, px1: Float, py1: Float, px2: Float, py2: Float) {
            if ((v1 > 0) == (v2 > 0)) return
            val t = (v1 / (v1 - v2)).toFloat()
            points[n++] = px1 + (px2 - px1) * t
            points[n++] = py1 + (py2 - py1) * t
        }
        cross(a, b, x0, y0, x0 + cell, y0)
        cross(b, d, x0 + cell, y0, x0 + cell, y0 + cell)
        cross(d, e, x0 + cell, y0 + cell, x0, y0 + cell)
        cross(e, a, x0, y0 + cell, x0, y0)
        // Two crossings make one segment; a saddle makes four, paired around the edges.
        var i = 0
        while (i + 3 < n) {
            path.moveTo(points[i], points[i + 1])
            path.lineTo(points[i + 2], points[i + 3])
            i += 4
        }
    }
    drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun Boolean.toInt() = if (this) 1 else 0

internal fun DrawScope.drawGrid(
    xMin: Double,
    yMax: Double,
    unitsPerPx: Double,
    sx: (Double) -> Float,
    sy: (Double) -> Float,
    grid: Color,
    axis: Color,
    measurer: androidx.compose.ui.text.TextMeasurer,
    labelStyle: TextStyle,
    formatter: DisplayFormatter,
) {
    val xMax = xMin + size.width * unitsPerPx
    val yMin = yMax - size.height * unitsPerPx
    val step = niceStep(80.dp.toPx() * unitsPerPx)
    val minor = grid.copy(alpha = 0.35f)
    val decimals = max(0, -floor(log10(step)).toInt())
    val axisX = sx(0.0).coerceIn(0f, size.width)
    val axisY = sy(0.0).coerceIn(0f, size.height)
    val pad = 4.dp.toPx()

    var x = ceil(xMin / step) * step
    while (x <= xMax) {
        val px = sx(x)
        drawLine(minor, Offset(px, 0f), Offset(px, size.height), 1.dp.toPx())
        if (abs(x) > step / 2) {
            val label = measurer.measure(formatter.number(label(x, decimals)), labelStyle)
            val ly = (axisY + pad).coerceAtMost(size.height - label.size.height - pad)
            val lx = px - label.size.width / 2f
            if (lx > pad && lx + label.size.width < size.width - pad) drawText(label, topLeft = Offset(lx, ly))
        }
        x += step
    }
    var y = ceil(yMin / step) * step
    while (y <= yMax) {
        val py = sy(y)
        drawLine(minor, Offset(0f, py), Offset(size.width, py), 1.dp.toPx())
        if (abs(y) > step / 2) {
            val label = measurer.measure(formatter.number(label(y, decimals)), labelStyle)
            val lx = (axisX + pad).coerceAtMost(size.width - label.size.width - pad)
            val ty = py - label.size.height / 2f
            if (ty > pad && ty + label.size.height < size.height - pad) drawText(label, topLeft = Offset(lx, ty))
        }
        y += step
    }
    // Axes: stronger, and pinned to the edge when the origin is off screen, so you never lose your bearings.
    val axisStroke = 1.5.dp.toPx()
    val axisColor = axis.copy(alpha = 0.7f)
    drawLine(axisColor, Offset(0f, axisY), Offset(size.width, axisY), axisStroke, pathEffect = if (sy(0.0) in 0f..size.height) null else PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
    drawLine(axisColor, Offset(axisX, 0f), Offset(axisX, size.height), axisStroke, pathEffect = if (sx(0.0) in 0f..size.width) null else PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
}

internal fun DrawScope.drawTrace(
    tx: Float,
    xMin: Double,
    unitsPerPx: Double,
    state: GraphState,
    colors: List<Color>,
    sy: (Double) -> Float,
    scheme: androidx.compose.material3.ColorScheme,
    measurer: androidx.compose.ui.text.TextMeasurer,
    style: TextStyle,
    formatter: DisplayFormatter,
) {
    val x = xMin + tx * unitsPerPx
    drawLine(scheme.onSurface.copy(alpha = 0.5f), Offset(tx, 0f), Offset(tx, size.height), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
    val text = buildAnnotatedString {
        append("x = " + formatter.number(readout(x)))
        state.functions.forEachIndexed { index, f ->
            // Equations can cross a vertical line any number of times; the readout follows functions.
            val y = (state.curves[index] as? Curve.Explicit)?.plot?.at(x) ?: return@forEachIndexed
            if (!y.isFinite()) return@forEachIndexed
            val py = sy(y)
            if (py in 0f..size.height) {
                drawCircle(scheme.surfaceContainerLow, 7.dp.toPx(), Offset(tx, py))
                drawCircle(colors[index % colors.size], 5.dp.toPx(), Offset(tx, py))
            }
            append("\n")
            withStyle(SpanStyle(color = colors[index % colors.size])) { append("● ") }
            append("y = ")
            append(formatter.number(readout(y)))
        }
    }
    val layout = measurer.measure(text, style)
    val pad = 10.dp.toPx()
    val w = layout.size.width + pad * 2
    val h = layout.size.height + pad * 2
    // The readout sits beside the finger, flipping sides near the edge so it is never under it.
    val left = if (tx + 24.dp.toPx() + w < size.width) tx + 16.dp.toPx() else tx - 16.dp.toPx() - w
    val top = 12.dp.toPx()
    // The theme's own raised surface, with a hairline edge to lift it off the plot, in light and dark.
    val corner = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
    drawRoundRect(scheme.surfaceContainerHighest, Offset(left, top), Size(w, h), corner)
    drawRoundRect(scheme.outlineVariant, Offset(left, top), Size(w, h), corner, style = Stroke(1.dp.toPx()))
    drawText(layout, color = scheme.onSurface, topLeft = Offset(left + pad, top + pad))
}

/** 1, 2 or 5 times a power of ten: grid steps people can read at a glance. */
private fun niceStep(rough: Double): Double {
    if (!rough.isFinite() || rough <= 0) return 1.0
    val magnitude = 10.0.pow(floor(log10(rough)))
    val n = rough / magnitude
    return magnitude * when {
        n < 1.5 -> 1.0
        n < 3.5 -> 2.0
        n < 7.5 -> 5.0
        else -> 10.0
    }
}

/** A grid label with just enough decimals for its step, in the expression language. */
internal fun label(value: Double, decimals: Int): String =
    Decimal.fromDouble(value).setScale(decimals, RoundingMode.HALF_UP).toPlainString().replace('-', '−').let { if (it == "−0") "0" else it }

/** A traced value, rounded to something readable. */
internal fun readout(value: Double): String {
    if (!value.isFinite()) return "—"
    return NumberFormatter.format(Decimal.fromDouble(value).round(MathContext(6)), significantDigits = 6)
}
