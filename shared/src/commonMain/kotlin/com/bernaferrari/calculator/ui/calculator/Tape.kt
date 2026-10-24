package com.bernaferrari.calculator.ui.calculator

import com.bernaferrari.calculator.ui.MenuShape
import com.bernaferrari.calculator.ui.flushMenu
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.outlined.Share
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.expandHorizontally
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.domain.model.CalculationHistory
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import com.bernaferrari.calculator.platform.formatDate
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** One row of the tape: either a calculation or, in history mode, the day it belongs to. */
private sealed interface TapeRow {
    val key: Any

    data class Entry(val entry: CalculationHistory, val newest: Boolean) : TapeRow {
        override val key: Any get() = entry.id
    }

    data class Day(val label: DayLabel, val start: Long) : TapeRow {
        override val key: Any get() = "day-$start"
    }

}

private sealed interface DayLabel {
    data object Today : DayLabel
    data object Yesterday : DayLabel
    data class Date(val millis: Long) : DayLabel
}

/**
 * Past calculations, newest at the bottom right above the live entry. In history mode the tape
 * takes the whole screen and gains day headers and a clear button. Tap to reuse a result,
 * swipe left to delete, hold for more.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tape(
    history: List<CalculationHistory>,
    formatter: DisplayFormatter,
    state: LazyListState,
    expanded: Boolean,
    hideNewestResult: Boolean,
    onUseResult: (CalculationHistory) -> Unit,
    onEditExpression: (CalculationHistory) -> Unit,
    onDeleteEntry: (CalculationHistory) -> Unit,
    onClearAll: () -> Unit,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
    /** A note on an entry ("rent split"); an empty one removes it. */
    onNote: (CalculationHistory, String) -> Unit = { _, _ -> },
    /** Shares the whole history as text, from history mode. */
    onShare: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val formatColors = DisplayFormatter.Colors(operator = colors.primary.copy(alpha = 0.8f), function = colors.onSurfaceVariant, ghost = colors.outline)
    val rows = remember(history, expanded) { buildRows(history, expanded) }

    Column(modifier) {
        // The title and Clear arrive with history mode, in step with the keypad leaving.
        AnimatedVisibility(
            expanded,
            enter = expandVertically(spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(200, delayMillis = 60)),
            exit = shrinkVertically(spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) + fadeOut(tween(100)),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 8.dp),
            ) {
                Text(
                    stringResource(Res.string.settings_history),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (history.isNotEmpty()) {
                    IconButton(onClick = onShare) { Icon(Icons.Outlined.Share, contentDescription = stringResource(Res.string.history_share), tint = colors.onSurfaceVariant) }
                    TextButton(onClick = onClearAll) { Text(stringResource(Res.string.history_clear)) }
                }
            }
        }
        if (expanded && history.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.history_empty), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            }
        } else {
            TapeList(rows, state, expanded, hideNewestResult, formatter, formatColors, onUseResult, onEditExpression, onDeleteEntry, onCopy, onNote, Modifier.weight(1f).fillMaxWidth())
        }
    }
}

@Composable
private fun TapeList(
    rows: List<TapeRow>,
    state: LazyListState,
    expanded: Boolean,
    hideNewestResult: Boolean,
    formatter: DisplayFormatter,
    formatColors: DisplayFormatter.Colors,
    onUseResult: (CalculationHistory) -> Unit,
    onEditExpression: (CalculationHistory) -> Unit,
    onDeleteEntry: (CalculationHistory) -> Unit,
    onCopy: (String) -> Unit,
    onNote: (CalculationHistory, String) -> Unit,
    modifier: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    LazyColumn(
        state = state,
        reverseLayout = true,
        contentPadding = PaddingValues(top = if (expanded) 8.dp else 24.dp),
        modifier = modifier
            // Fade the oldest entries out at the top instead of cutting them off.
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        // Just a short soft edge so entries don't clip hard under the status bar.
                        20.dp.toPx() / size.height.coerceAtLeast(1f) to Color.Black,
                    ),
                    blendMode = BlendMode.DstIn,
                )
            },
    ) {
        items(rows, key = { it.key }, contentType = { it::class }) { row ->
            when (row) {
                is TapeRow.Day -> Text(
                    row.label.text(),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary,
                    modifier = Modifier.animateItem().fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 4.dp),
                )
                is TapeRow.Entry -> TapeEntry(
                    entry = row.entry,
                    showResult = !(row.newest && hideNewestResult),
                    formatter = formatter,
                    formatColors = formatColors,
                    onUseResult = onUseResult,
                    onEditExpression = onEditExpression,
                    onDeleteEntry = onDeleteEntry,
                    onCopy = onCopy,
                    onNote = onNote,
                    modifier = Modifier.animateItem(fadeInSpec = tween(220, delayMillis = 140)),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TapeEntry(
    entry: CalculationHistory,
    showResult: Boolean,
    formatter: DisplayFormatter,
    formatColors: DisplayFormatter.Colors,
    onUseResult: (CalculationHistory) -> Unit,
    onEditExpression: (CalculationHistory) -> Unit,
    onDeleteEntry: (CalculationHistory) -> Unit,
    onCopy: (String) -> Unit,
    onNote: (CalculationHistory, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    var menu by remember { mutableStateOf(false) }
    var noting by remember { mutableStateOf(false) }
    val dismiss = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = dismiss,
        enableDismissFromStartToEnd = false,
        onDismiss = { if (it == SwipeToDismissBoxValue.EndToStart) onDeleteEntry(entry) },
        backgroundContent = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize().background(colors.errorContainer).padding(horizontal = 24.dp),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = colors.onErrorContainer)
            }
        },
        modifier = modifier,
    ) {
        Column(Modifier.background(colors.surface)) {
            // The person's note sits just above its calculation, in the accent, like a label on a receipt.
            entry.note?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 8.dp),
                )
            }
        Box {
            // One line per calculation, "12+7×3 = 33": the question quiet, the answer full strength.
            // A long one wraps, keeping "= 33" together on the last line.
            FlowRow(
                horizontalArrangement = Arrangement.End,
                itemVerticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = { onUseResult(entry) }, onLongClick = { menu = true })
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Text(
                    formatter.format(entry.expression, 0, formatColors).text,
                    style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 22.sp, textAlign = TextAlign.End),
                    color = colors.onSurfaceVariant,
                )
                // The "=" always closes the question, so "1+1 =" reads as asked-and-answered even while
                // the answer is still the big number below; the answer joins the line later.
                Text("  =", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 22.sp), color = colors.outline, maxLines = 1)
                AnimatedVisibility(
                    visible = showResult,
                    enter = expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow), expandFrom = Alignment.Start) + fadeIn(tween(220, delayMillis = 60)),
                    exit = shrinkHorizontally(tween(120)) + fadeOut(tween(80)),
                ) {
                    Text(
                        buildAnnotatedString {
                            append("  ")
                            append(formatter.number(entry.result))
                        },
                        style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 22.sp, fontWeight = FontWeight.Medium),
                        color = colors.onSurface,
                        maxLines = 1,
                    )
                }
            }
            Box(Modifier.align(Alignment.BottomEnd)) {
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, shape = MenuShape, modifier = Modifier.flushMenu()) {
                    DropdownMenuItem(text = { Text(stringResource(Res.string.tape_copy_result)) }, onClick = { menu = false; onCopy(entry.result) })
                    DropdownMenuItem(text = { Text(stringResource(Res.string.tape_edit)) }, onClick = { menu = false; onEditExpression(entry) })
                    DropdownMenuItem(
                        text = { Text(stringResource(if (entry.note == null) Res.string.tape_note_add else Res.string.tape_note_edit)) },
                        onClick = { menu = false; noting = true },
                    )
                    DropdownMenuItem(text = { Text(stringResource(Res.string.tape_delete)) }, onClick = { menu = false; onDeleteEntry(entry) })
                }
            }
        }
        }
    }
    if (noting) {
        var note by remember { mutableStateOf(entry.note.orEmpty()) }
        AlertDialog(
            onDismissRequest = { noting = false },
            title = { Text(stringResource(if (entry.note == null) Res.string.tape_note_add else Res.string.tape_note_edit)) },
            text = {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(40) },
                    singleLine = true,
                    placeholder = { Text(stringResource(Res.string.tape_note_hint)) },
                )
            },
            confirmButton = { TextButton(onClick = { onNote(entry, note); noting = false }) { Text(stringResource(Res.string.function_save)) } },
            dismissButton = { TextButton(onClick = { noting = false }) { Text(stringResource(Res.string.function_cancel)) } },
        )
    }
}

@Composable
private fun DayLabel.text(): String = when (this) {
    DayLabel.Today -> stringResource(Res.string.history_today)
    DayLabel.Yesterday -> stringResource(Res.string.history_yesterday)
    is DayLabel.Date -> remember(millis) { formatDate(millis) }
}

/** The list is drawn bottom-up, so each day's header comes after its entries. */
private fun buildRows(history: List<CalculationHistory>, expanded: Boolean): List<TapeRow> {
    if (!expanded) return history.mapIndexed { i, entry -> TapeRow.Entry(entry, newest = i == 0) }
    val today = epochDay(Clock.System.now().toEpochMilliseconds())
    val rows = mutableListOf<TapeRow>()
    history.groupBy { epochDay(it.timestamp) }.forEach { (day, entries) ->
        entries.forEach { rows += TapeRow.Entry(it, newest = it === history.first()) }
        val label = when (day) {
            today -> DayLabel.Today
            today - 1 -> DayLabel.Yesterday
            else -> DayLabel.Date(entries.first().timestamp)
        }
        rows += TapeRow.Day(label, day)
    }
    return rows
}

/** The local calendar day of [millis], as days since 1970, so neighbouring days differ by one. */
private fun epochDay(millis: Long): Long =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays()
