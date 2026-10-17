package com.bernaferrari.calculator.ui.graph

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.bernaferrari.calculator.ui.calculator.MainKeypad
import com.bernaferrari.calculator.ui.calculator.rememberHaptics
import com.bernaferrari.calculator.ui.calculator.separators
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.ui.settings.SettingsViewModel
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import kotlin.math.abs
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

private val QuickExamples = listOf("x²", "x²+y²=9", "x³−x", "sin(x)÷x", "y²=x", "1÷x", "x²÷9+y²÷4=1", "2^x")

/**
 * Graphs, Calculator++ style: the plot on top with pinch, pan and a hold-to-trace finger, the
 * functions in the middle (tap one to edit it), and the calculator's own keys below, flicks
 * included, with `x` where `%` usually sits (flick it up for `y`) and `=` typing equations.
 */
@Composable
fun GraphScreen(
    onNavigateBack: () -> Unit,
    viewModel: GraphViewModel = viewModel { GraphViewModel(createSavedStateHandle()) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings: SettingsViewModel = viewModel { SettingsViewModel() }
    val prefs by settings.prefs.collectAsStateWithLifecycle()
    val haptics = rememberHaptics(enabled = { prefs.hapticFeedback }, sounds = { prefs.soundEnabled })
    val separators = prefs.decimalSeparator.separators()
    val formatter = remember(separators) { DisplayFormatter(separators) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val colors = curveColors()

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("graph", isScreen = true).fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal))) {
            val landscape = maxWidth > maxHeight && maxHeight < 600.dp
            val header: @Composable () -> Unit = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp).height(56.dp)) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
                    Text(stringResource(Res.string.graph_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = { haptics.tap(); viewModel.onResetView() }) {
                        Icon(Icons.Rounded.CenterFocusStrong, contentDescription = stringResource(Res.string.graph_reset), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            val plot: @Composable (Modifier) -> Unit = { modifier ->
                GraphCanvas(
                    state = state,
                    colors = colors,
                    formatter = formatter,
                    onViewport = viewModel::onViewport,
                    onReset = { haptics.tap(); viewModel.onResetView() },
                    onTraceTick = haptics::tick,
                    modifier = modifier.padding(horizontal = 12.dp).clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow),
                )
            }
            val functions: @Composable () -> Unit = {
                FunctionList(
                    state = state,
                    colors = colors,
                    formatter = formatter,
                    onActivate = { haptics.tap(); viewModel.onActivate(it) },
                    onRemove = { haptics.heavy(); viewModel.onRemove(it) },
                    onAdd = { haptics.tap(); viewModel.onAdd() },
                    onExample = { haptics.tap(); viewModel.onExample(it) },
                )
            }
            val keys: @Composable (Modifier) -> Unit = { modifier ->
                MainKeypad(
                    decimal = formatter.decimal,
                    haptics = haptics,
                    functionLayer = false,
                    onKey = viewModel::onKey,
                    onFunctionKey = viewModel::onKey,
                    onClearAll = { viewModel.onKey(com.bernaferrari.calculator.domain.input.Key.CLEAR) },
                    // Short rows leave no room for hint dots; the flicks still work.
                    showDots = false,
                    bottomInset = bottomInset,
                    variable = true,
                    modifier = modifier,
                )
            }
            if (landscape) {
                Row(Modifier.fillMaxSize()) {
                    Column(Modifier.weight(0.55f).fillMaxHeight()) {
                        header()
                        plot(Modifier.weight(1f).fillMaxWidth().padding(bottom = 12.dp + bottomInset))
                    }
                    Column(Modifier.weight(0.45f).fillMaxHeight().statusBarsPadding()) {
                        functions()
                        keys(Modifier.weight(1f).fillMaxWidth())
                    }
                }
            } else {
                val rowHeight = (maxHeight * 0.36f / 5).coerceIn(52.dp, 72.dp)
                Column(Modifier.fillMaxSize()) {
                    header()
                    plot(Modifier.weight(1f).fillMaxWidth())
                    functions()
                    keys(Modifier.fillMaxWidth().height(rowHeight * 5 + bottomInset))
                }
            }
        }
    }
}

/**
 * One color per function: the theme's accent first, then orange and green, chosen to stay apart
 * from every palette and from each other, lighter on dark backgrounds.
 */
@Composable
private fun curveColors(): List<Color> {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return listOf(
        MaterialTheme.colorScheme.primary,
        if (dark) Color(0xFFFFB06B) else Color(0xFFD9640B),
        if (dark) Color(0xFF7FD99A) else Color(0xFF1E8E3E),
    )
}

@Composable
private fun FunctionList(
    state: GraphState,
    colors: List<Color>,
    formatter: DisplayFormatter,
    onActivate: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onAdd: () -> Unit,
    onExample: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        state.functions.forEachIndexed { index, function ->
            val active = index == state.active
            val field by animateColorAsState(if (active) scheme.surfaceContainerHigh else Color.Transparent, label = "field")
            val invalid = function.text.isNotBlank() && function.curve == null && !function.pending
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(field)
                    .clickable { onActivate(index) }
                    .padding(start = 14.dp, end = 4.dp),
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(colors[index % colors.size]))
                Spacer(Modifier.width(12.dp))
                Text(
                    buildAnnotatedString {
                        if (!function.isEquation) withStyle(SpanStyle(color = scheme.onSurfaceVariant)) { append("y = ") }
                        if (function.text.isEmpty()) {
                            withStyle(SpanStyle(color = scheme.outline, fontSize = 16.sp)) { append(if (active) stringResource(Res.string.graph_type_hint) else "") }
                        } else {
                            val formatted = formatter.format(function.text, 0, DisplayFormatter.Colors(scheme.primary, scheme.onSurfaceVariant, scheme.outline)).text
                            if (active) {
                                // A caret where the next key lands.
                                val cursor = formatter.format(function.text, 0, null).rawToDisplay[function.editor.cursor.coerceIn(0, function.text.length)]
                                append(formatted.subSequence(0, cursor))
                                withStyle(SpanStyle(color = scheme.primary)) { append("|") }
                                append(formatted.subSequence(cursor, formatted.length))
                            } else {
                                append(formatted)
                            }
                        }
                    },
                    style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 22.sp),
                    color = if (invalid) scheme.error else scheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                if (state.functions.size > 1 || function.text.isNotEmpty()) {
                    IconButton(onClick = { onRemove(index) }) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(Res.string.graph_remove), tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        val active = state.activeFunction
        if (active.text.isEmpty()) {
            // Nothing typed yet: a few shapes worth seeing, one tap each.
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                items(QuickExamples) { example ->
                    AssistChip(onClick = { onExample(example) }, label = { Text(formatter.format(example, 0, null).text.text) })
                }
            }
        } else if (state.canAdd) {
            TextButton(onClick = onAdd) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.graph_add))
            }
        }
    }
}

/**
 * The plot. One finger pans, two pinch; hold still for a moment and the finger traces instead,
 * reading out x and every curve's y. The readout stays after you let go (tap to put it away);
 * double-tap to come home.
 */
@Composable
private fun GraphCanvas(
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
    val points = remember(state.functions, state.active, state.viewport, canvasSize) { keyPoints(state, canvasSize) }
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

        state.functions.forEachIndexed { index, f ->
            val color = colors[index % colors.size]
            val plot = when (val curve = f.curve) {
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
            drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        drawKeyPoints(points, selected, colors, ::sx, ::sy, scheme, measurer, kindStyle, traceStyle, kinds, formatter)

        traceX?.let { x ->
            val tx = sx(x)
            if (tx in 0f..size.width) drawTrace(tx, xMin, unitsPerPx, state, colors, ::sy, scheme, measurer, traceStyle, formatter)
        }
    }
}

/** A key point on screen: which curve it belongs to, and whether it's on the one being edited. */
private data class ShownPoint(val point: KeyPoint, val function: Int, val onActive: Boolean)

private fun keyPoints(state: GraphState, size: IntSize): List<ShownPoint> {
    if (size.width == 0) return emptyList()
    val v = state.viewport
    val half = v.span / 2
    val active = (state.activeFunction.curve as? Curve.Explicit)?.plot ?: return emptyList()
    val points = KeyPoints.of(active, v.centerX - half, v.centerX + half).map { ShownPoint(it, state.active, true) }
    val meets = state.functions.flatMapIndexed { i, f ->
        val other = (f.curve as? Curve.Explicit)?.plot
        if (i == state.active || other == null) emptyList()
        else KeyPoints.intersections(active, other, v.centerX - half, v.centerX + half).map { ShownPoint(it, state.active, true) }
    }
    val yHalf = half * size.height / size.width
    return (points + meets).filter { it.point.y in (v.centerY - yHalf)..(v.centerY + yHalf) }
}

private fun KeyPoint.Kind.title() = when (this) {
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
private fun DrawScope.drawKeyPoints(
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
        withStyle(kindStyle.toSpanStyle().copy(color = scheme.inverseOnSurface.copy(alpha = 0.7f))) { append(kinds.getValue(p.point.kind)) }
        val comma = if (formatter.decimal == ',') "; " else ", "
        val decimals = "(" + formatter.number(readout(p.point.x)) + comma + formatter.number(readout(p.point.y)) + ")"
        val exactX = Exact.recognize(p.point.x)
        val exactY = Exact.recognize(p.point.y)
        if (exactX != null || exactY != null) {
            // π/2 says more than 1.5708; the decimals stay underneath.
            fun exact(v: Double, e: String?) = e ?: formatter.number(readout(v))
            append("\n(" + exact(p.point.x, exactX) + comma + exact(p.point.y, exactY) + ")")
            withStyle(kindStyle.toSpanStyle().copy(color = scheme.inverseOnSurface.copy(alpha = 0.7f))) { append("\n≈ $decimals") }
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
    drawRoundRect(scheme.inverseSurface, Offset(left, top), Size(w, h), androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()))
    drawText(layout, color = scheme.inverseOnSurface, topLeft = Offset(left + pad, top + pad))
}

/**
 * An equation in x and y, drawn where its two sides agree, by marching squares: sample the
 * difference on a fine grid, and wherever it changes sign along a cell's edge the curve passes
 * between the two samples. Cells straddling a pole (tan, 1/…) flip sign without a zero; their
 * centre is larger than any corner, and they are skipped.
 */
private fun DrawScope.drawImplicit(field: Field, xMin: Double, yMax: Double, unitsPerPx: Double, color: Color) {
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

private fun DrawScope.drawGrid(
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

private fun DrawScope.drawTrace(
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
            val y = (f.curve as? Curve.Explicit)?.plot?.at(x) ?: return@forEachIndexed
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
    drawRoundRect(scheme.inverseSurface, Offset(left, top), Size(w, h), androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()))
    drawText(layout, color = scheme.inverseOnSurface, topLeft = Offset(left + pad, top + pad))
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
private fun label(value: Double, decimals: Int): String =
    Decimal.fromDouble(value).setScale(decimals, RoundingMode.HALF_UP).toPlainString().replace('-', '−').let { if (it == "−0") "0" else it }

/** A traced value, rounded to something readable. */
private fun readout(value: Double): String {
    if (!value.isFinite()) return "—"
    return NumberFormatter.format(Decimal.fromDouble(value).round(MathContext(6)), significantDigits = 6)
}
