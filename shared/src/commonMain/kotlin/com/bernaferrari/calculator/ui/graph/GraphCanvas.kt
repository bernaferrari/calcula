package com.bernaferrari.calculator.ui.graph

import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.runtime.key
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.domain.math.KeyPoint
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import kotlin.math.abs
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.stringResource

/**
 * The plot. One finger pans, two pinch; hold still for a moment and the finger traces instead,
 * reading out x and every curve's y. The readout stays after you let go (tap to put it away);
 * double-tap to come home.
 */
@Composable
internal fun GraphCanvas(
    state: GraphState,
    colors: List<Color>,
    formatter: DisplayFormatter,
    onViewport: (Viewport) -> Unit,
    onReset: () -> Unit,
    onTraceTick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontFamily = GoogleSansFlex, fontSize = 11.sp, color = scheme.onSurfaceVariant)
    val traceStyle = TextStyle(fontFamily = GoogleSansFlex, fontSize = 14.sp, color = scheme.onSurface)
    val kindStyle = TextStyle(fontFamily = GoogleSansFlex, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    // Where the trace sits, in graph units, so it stays on the same x while you pan and zoom.
    var traceX by remember { mutableStateOf<Double?>(null) }
    var selected by remember { mutableStateOf<ShownPoint?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    // The active curve's roots, turns and y-intercept, and where it meets the others.
    val gapPx = with(LocalDensity.current) { 22.dp.toPx() }
    val points = remember(state.curves, state.active, state.viewport, canvasSize) { keyPoints(state, canvasSize, gapPx) }
    LaunchedEffect(state.functions, state.active) { selected = null }
    val kinds = KeyPoint.Kind.entries.associateWith { stringResource(it.title()) }
    val currentViewport by rememberUpdatedState(state.viewport)
    val currentOnViewport by rememberUpdatedState(onViewport)
    val currentPoints by rememberUpdatedState(points)

    Canvas(
        modifier
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    fun graphX(px: Float): Double {
                        val v = currentViewport
                        return v.centerX + (px - size.width / 2f) * (v.span / size.width)
                    }
                    fun screenX(x: Double): Float {
                        val v = currentViewport
                        return (size.width / 2f + (x - v.centerX) / (v.span / size.width)).toFloat()
                    }
                    // Holding still for the long-press timeout starts a trace; moving first (or a
                    // second finger) means pan and zoom. Waiting with a timeout catches a finger
                    // that rests perfectly still and sends no events at all.
                    var moved = false
                    val early = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.none { it.pressed }) return@withTimeoutOrNull event
                            val drift = event.changes.first().position - down.position
                            if (event.changes.count { it.pressed } > 1 || drift.getDistance() > viewConfiguration.touchSlop) {
                                moved = true
                                return@withTimeoutOrNull event
                            }
                        }
                        @Suppress("UNREACHABLE_CODE") null
                    }
                    val tracing = early == null
                    // The trace clicks onto a key point when the finger passes close to one.
                    var snappedTo: ShownPoint? = null
                    fun traceAt(px: Float) {
                        val snap = currentPoints.filter { it.onActive }.minByOrNull { abs(screenX(it.point.x) - px) }
                            ?.takeIf { abs(screenX(it.point.x) - px) < 12.dp.toPx() }
                        if (snap != null && snap != snappedTo) onTraceTick()
                        snappedTo = snap
                        traceX = snap?.point?.x ?: graphX(px)
                    }
                    if (tracing) {
                        onTraceTick()
                        selected = null
                        traceAt(down.position.x)
                    }
                    if (!tracing && !moved) return@awaitEachGesture // a tap; the tap detector handles it
                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }
                        if (pressed.isEmpty()) {
                            // This gesture was a trace or a pan, not a tap: keep the tap detector out of it.
                            event.changes.forEach { it.consume() }
                            break
                        }
                        if (tracing) {
                            traceAt(pressed.first().position.x)
                            pressed.forEach { it.consume() }
                            continue
                        }
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()
                        val v = currentViewport
                        val unitsPerPx = v.span / size.width
                        val centroid = event.calculateCentroid()
                        // Zoom around the fingers: the point under them stays under them.
                        val span = (v.span / zoom).coerceIn(1e-6, 1e8)
                        val focusX = v.centerX + (centroid.x - size.width / 2f) * unitsPerPx
                        val focusY = v.centerY - (centroid.y - size.height / 2f) * unitsPerPx
                        val ratio = span / v.span
                        currentOnViewport(
                            Viewport(
                                centerX = focusX - (focusX - v.centerX) * ratio - pan.x * unitsPerPx,
                                centerY = focusY - (focusY - v.centerY) * ratio + pan.y * unitsPerPx,
                                span = span,
                            ),
                        )
                        pressed.forEach { if (it.positionChanged()) it.consume() }
                    }
                }
            }
            .pointerInput(Unit) {
                // A tap on a key point names it; anywhere else puts the trace and label away.
                // A double-tap also brings the view home.
                detectTapGestures(
                    onTap = { tap ->
                        val v = currentViewport
                        val unitsPerPx = v.span / size.width
                        fun at(p: KeyPoint) = Offset(
                            (size.width / 2f + (p.x - v.centerX) / unitsPerPx).toFloat(),
                            (size.height / 2f - (p.y - v.centerY) / unitsPerPx).toFloat(),
                        )
                        val hit = currentPoints.minByOrNull { (at(it.point) - tap).getDistance() }
                            ?.takeIf { (at(it.point) - tap).getDistance() < 24.dp.toPx() }
                        traceX = null
                        selected = if (hit == selected) null else hit
                        if (hit != null) onTraceTick()
                    },
                    onDoubleTap = { traceX = null; selected = null; onReset() },
                )
            },
    ) {
        val v = state.viewport
        val unitsPerPx = v.span / size.width
        val xMin = v.centerX - size.width / 2 * unitsPerPx
        val yMax = v.centerY + size.height / 2 * unitsPerPx
        fun sx(x: Double) = ((x - xMin) / unitsPerPx).toFloat()
        fun sy(y: Double) = ((yMax - y) / unitsPerPx).toFloat()

        drawGrid(xMin, yMax, unitsPerPx, ::sx, ::sy, scheme.outlineVariant, scheme.onSurfaceVariant, measurer, labelStyle, formatter)

        drawCurves(state, colors, xMin, yMax, unitsPerPx, ::sy, 3.dp.toPx())

        drawKeyPoints(points, selected, colors, ::sx, ::sy, scheme, measurer, kindStyle, traceStyle, kinds, formatter)

        traceX?.let { x ->
            val tx = sx(x)
            if (tx in 0f..size.width) drawTrace(tx, xMin, unitsPerPx, state, colors, ::sy, scheme, measurer, traceStyle, formatter)
        }
    }
}
