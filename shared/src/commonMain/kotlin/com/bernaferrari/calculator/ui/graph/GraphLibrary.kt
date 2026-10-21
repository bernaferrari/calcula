package com.bernaferrari.calculator.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.data.SavedGraph
import com.bernaferrari.calculator.platform.formatDate
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import org.jetbrains.compose.resources.stringResource

/**
 * The graph as a picture to share: the plot over its grid, with every function under it in its
 * color, so the image explains itself wherever it ends up. Drawn off screen at a fixed size.
 */
internal fun renderGraphImage(
    state: GraphState,
    colors: List<Color>,
    scheme: ColorScheme,
    measurer: TextMeasurer,
    formatter: DisplayFormatter,
    font: FontFamily,
): ImageBitmap {
    // 400 "dp" across at 3×: crisp on any screen, small enough to send anywhere.
    val density = Density(3f)
    val shown = state.functions.withIndex().filter { it.value.text.isNotBlank() }
    with(density) {
        val width = 400.dp.toPx()
        val plotHeight = 300.dp.toPx()
        val row = 32.dp.toPx()
        val pad = 16.dp.toPx()
        val legend = if (shown.isEmpty()) 0f else pad + row * shown.size + pad / 2
        val image = ImageBitmap(width.toInt(), (plotHeight + legend).toInt())
        CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(image), Size(width, plotHeight + legend)) {
            drawRect(scheme.surface)
            inset(0f, 0f, 0f, legend) {
                drawRect(scheme.surfaceContainerLow)
                val v = state.viewport
                val unitsPerPx = v.span / size.width
                val xMin = v.centerX - size.width / 2 * unitsPerPx
                val yMax = v.centerY + size.height / 2 * unitsPerPx
                fun sx(x: Double) = ((x - xMin) / unitsPerPx).toFloat()
                fun sy(y: Double) = ((yMax - y) / unitsPerPx).toFloat()
                val labels = TextStyle(fontFamily = font, fontSize = 11.sp, color = scheme.onSurfaceVariant)
                drawGrid(xMin, yMax, unitsPerPx, ::sx, ::sy, scheme.outlineVariant, scheme.onSurfaceVariant, measurer, labels, formatter)
                drawCurves(state, colors, xMin, yMax, unitsPerPx, ::sy, 3.dp.toPx())
            }
            val text = TextStyle(fontFamily = font, fontSize = 18.sp, color = scheme.onSurface)
            shown.forEachIndexed { line, (index, function) ->
                val top = plotHeight + pad + row * line
                drawCircle(colors[index % colors.size], 5.dp.toPx(), Offset(pad + 5.dp.toPx(), top + row / 2))
                val label = measurer.measure(legendText(function, formatter, scheme), text, maxLines = 1, overflow = TextOverflow.Ellipsis, constraints = androidx.compose.ui.unit.Constraints(maxWidth = (width - pad * 3).toInt()))
                drawText(label, topLeft = Offset(pad * 2 + 4.dp.toPx(), top + (row - label.size.height) / 2))
            }
        }
        return image
    }
}

private fun legendText(function: GraphFunction, formatter: DisplayFormatter, scheme: ColorScheme) = buildAnnotatedString {
    if (!function.isEquation) withStyle(SpanStyle(color = scheme.onSurfaceVariant)) { append("y = ") }
    append(formatter.format(function.text, 0, DisplayFormatter.Colors(scheme.primary, scheme.onSurfaceVariant, scheme.outline)).text)
}

/** A small picture of a saved graph, at its own view: recognizable faster than any name. */
@Composable
private fun GraphThumbnail(graph: GraphState, colors: List<Color>, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Canvas(modifier.clip(RoundedCornerShape(12.dp)).background(scheme.surfaceContainerHighest)) {
        val v = graph.viewport
        val unitsPerPx = v.span / size.width
        val xMin = v.centerX - size.width / 2 * unitsPerPx
        val yMax = v.centerY + size.height / 2 * unitsPerPx
        fun sy(y: Double) = ((yMax - y) / unitsPerPx).toFloat()
        val axes = scheme.outline.copy(alpha = 0.5f)
        val x0 = ((0 - xMin) / unitsPerPx).toFloat()
        val y0 = sy(0.0)
        if (x0 in 0f..size.width) drawLine(axes, Offset(x0, 0f), Offset(x0, size.height), 1.dp.toPx())
        if (y0 in 0f..size.height) drawLine(axes, Offset(0f, y0), Offset(size.width, y0), 1.dp.toPx())
        drawCurves(graph, colors, xMin, yMax, unitsPerPx, ::sy, 2.dp.toPx())
    }
}

/**
 * Every graph made here: pinned ones first, then the rest, newest first. Tap one to bring it
 * back (the current graph is kept, so nothing is lost), pin to keep it, swipe to delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GraphLibrarySheet(
    graphs: List<SavedGraph>,
    colors: List<Color>,
    formatter: DisplayFormatter,
    onRestore: (Long) -> Unit,
    onTogglePin: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp, bottom = 8.dp),
        ) {
            Text(stringResource(Res.string.graph_library), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            FilledTonalButton(onClick = onNew) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.graph_new))
            }
        }
        if (graphs.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.graph_library_empty), style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            }
            return@ModalBottomSheet
        }
        val pinned = graphs.filter { it.pinned }
        val recent = graphs.filterNot { it.pinned }
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            if (pinned.isNotEmpty()) {
                item("pinned") { SectionLabel(stringResource(Res.string.graph_pinned)) }
                items(pinned, key = { it.id }) { LibraryRow(it, colors, formatter, onRestore, onTogglePin, onDelete, Modifier.animateItem()) }
            }
            if (recent.isNotEmpty()) {
                item("recent") { SectionLabel(stringResource(Res.string.graph_recent)) }
                items(recent, key = { it.id }) { LibraryRow(it, colors, formatter, onRestore, onTogglePin, onDelete, Modifier.animateItem()) }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 4.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LibraryRow(
    saved: SavedGraph,
    colors: List<Color>,
    formatter: DisplayFormatter,
    onRestore: (Long) -> Unit,
    onTogglePin: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val graph = remember(saved.document) { saved.document.toState() }
    val dismiss = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = dismiss,
        enableDismissFromStartToEnd = false,
        onDismiss = { if (it == SwipeToDismissBoxValue.EndToStart) onDelete(saved.id) },
        backgroundContent = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize().background(scheme.errorContainer).padding(horizontal = 24.dp),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(Res.string.graph_delete), tint = scheme.onErrorContainer)
            }
        },
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.surfaceContainerLow)
                .combinedClickable(onClick = { onRestore(saved.id) })
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            GraphThumbnail(graph, colors, Modifier.size(width = 88.dp, height = 64.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val lines = graph.functions.withIndex().filter { it.value.text.isNotBlank() }
                lines.take(3).forEach { (index, function) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(colors[index % colors.size]))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            legendText(function, formatter, scheme),
                            style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 16.sp),
                            color = scheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                val more = if (lines.size > 3) "+${lines.size - 3} · " else ""
                Text(more + formatDate(saved.savedAt), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            }
            IconButton(onClick = { onTogglePin(saved.id) }) {
                Icon(
                    if (saved.pinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                    contentDescription = stringResource(if (saved.pinned) Res.string.graph_unpin else Res.string.graph_pin),
                    tint = if (saved.pinned) scheme.primary else scheme.onSurfaceVariant,
                )
            }
        }
    }
}
