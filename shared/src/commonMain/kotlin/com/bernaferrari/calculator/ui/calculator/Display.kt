package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.unit.em
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.bernaferrari.calculator.domain.model.AngleUnit
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.domain.input.Outcome
import com.bernaferrari.calculator.domain.input.Span
import com.bernaferrari.calculator.domain.math.CalcError
import com.bernaferrari.calculator.domain.model.CalculationHistory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt


/** What the live entry shows; snapshotted so the outgoing entry keeps its text while animating away. */
private data class Entry(
    val text: String,
    val cursor: Int,
    val resultShown: Boolean,
    val preview: String?,
    val error: CalcError?,
    val openParentheses: Int,
    val generation: Int,
    val angleUnit: AngleUnit,
    val selection: Span?,
)

/**
 * The upper half: a tape of past calculations (newest at the bottom, scroll up for more) above
 * the live entry. `=` rolls the entry up into the tape; AC squishes it away.
 */
@Composable
fun CalculatorDisplay(
    state: CalculatorUiState,
    formatter: DisplayFormatter,
    haptics: Haptics,
    events: Flow<Outcome>,
    onCursorMoved: (Int) -> Unit,
    onReplace: (start: Int, end: Int, text: String) -> Unit,
    onSelect: (Span?) -> Unit,
    onToggleAngle: () -> Unit,
    onCopy: (String) -> Unit,
    onUseResult: (CalculationHistory) -> Unit,
    onEditExpression: (CalculationHistory) -> Unit,
    onDeleteEntry: (CalculationHistory) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
    historyOpen: Boolean = false,
    entryMaxFontSize: TextUnit = 72.sp,
) {
    val shake = remember { Animatable(0f) }
    val reduceMotion = LocalReduceMotion.current
    val tapeState = rememberLazyListState()

    LaunchedEffect(events) {
        events.collect { outcome ->
            when (outcome) {
                is Outcome.Evaluated -> haptics.confirm()
                is Outcome.Failed -> {
                    haptics.reject()
                    launch {
                        shake.snapTo(0f)
                        if (!reduceMotion) shake.animateTo(0f, spring(dampingRatio = 0.25f, stiffness = 900f), initialVelocity = -1400f)
                    }
                }
                Outcome.Cleared -> haptics.heavy()
                Outcome.None -> Unit
            }
        }
    }

    // History arrives from the database a moment after `=`; when a new entry lands, bring it into view.
    val newestId = state.history.firstOrNull()?.id
    LaunchedEffect(newestId) {
        if (newestId != null) tapeState.animateScrollToItem(0)
    }

    Box(modifier) {
        Column(Modifier.fillMaxSize()) {
            Tape(
                // With the tape turned off, history still lives one tap away in history mode.
                history = if (state.showHistory || historyOpen) state.history else emptyList(),
                formatter = formatter,
                state = tapeState,
                onUseResult = { haptics.tap(); onUseResult(it) },
                onEditExpression = onEditExpression,
                onDeleteEntry = onDeleteEntry,
                onClearAll = onClearHistory,
                onCopy = onCopy,
                expanded = historyOpen,
                // Right after `=`, the big number below *is* the newest answer; don't say it twice.
                hideNewestResult = state.resultShown && !historyOpen,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            val entry = Entry(
                state.text, state.cursor, state.resultShown, state.preview, state.error, state.openParentheses, state.generation, state.angleUnit, state.selection,
            )
            // History mode is about the past: the live entry steps aside until the keypad returns.
            AnimatedVisibility(
                !historyOpen,
                enter = expandVertically(spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(220, delayMillis = 80)),
                exit = shrinkVertically(spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) + fadeOut(tween(120)),
            ) {
            AnimatedContent(
                targetState = entry,
                contentKey = { it.generation },
                transitionSpec = {
                    if (reduceMotion) {
                        fadeIn(tween(120)) togetherWith fadeOut(tween(120))
                    } else if (targetState.resultShown) {
                        // `=`: the question lifts a little and fades (its copy appears on the tape just after),
                        // while the answer grows in place from the size of the preview line.
                        (scaleIn(spring(dampingRatio = 0.7f, stiffness = 500f), initialScale = 34f / 72f, transformOrigin = TransformOrigin(1f, 0.8f)) +
                            fadeIn(tween(90))) togetherWith
                            (slideOutVertically(tween(180, easing = FastOutSlowInEasing)) { -it / 6 } +
                                scaleOut(tween(180), targetScale = 0.9f, transformOrigin = TransformOrigin(1f, 0f)) +
                                fadeOut(tween(110)))
                    } else {
                        // AC: the expression squishes away and a fresh 0 bounces in.
                        (scaleIn(spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium), initialScale = 0.6f, transformOrigin = TransformOrigin(1f, 0.5f)) +
                            fadeIn(tween(120, delayMillis = 60))) togetherWith
                            (scaleOut(spring(stiffness = Spring.StiffnessMedium), targetScale = 0.5f, transformOrigin = TransformOrigin(1f, 0.5f)) +
                                fadeOut(tween(140)))
                    }.using(SizeTransform(clip = false))
                },
                contentAlignment = Alignment.BottomEnd,
                label = "entry",
                modifier = Modifier.fillMaxWidth(),
            ) { current ->
                LiveEntry(
                    entry = current,
                    formatter = formatter,
                    maxFontSize = entryMaxFontSize,
                    onCursorMoved = onCursorMoved,
                    onReplace = onReplace,
                    onSelect = onSelect,
                    onCursorTick = haptics::tick,
                    onToggleAngle = { haptics.tap(); onToggleAngle() },
                    onCopy = { haptics.heavy(); onCopy(it) },
                    modifier = Modifier.offset { IntOffset(shake.value.roundToInt(), 0) },
                )
            }
            }
        }
    }
}

@Composable
private fun LiveEntry(
    entry: Entry,
    formatter: DisplayFormatter,
    maxFontSize: TextUnit,
    onCursorMoved: (Int) -> Unit,
    onReplace: (start: Int, end: Int, text: String) -> Unit,
    onSelect: (Span?) -> Unit,
    onCursorTick: () -> Unit,
    onToggleAngle: () -> Unit,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val formatColors = DisplayFormatter.Colors(operator = colors.primary, function = colors.onSurfaceVariant, ghost = colors.outlineVariant)
    val formatted = remember(entry.text, entry.openParentheses, formatColors) {
        formatter.format(entry.text, if (entry.resultShown) 0 else entry.openParentheses, formatColors)
    }
    // Like any Android text field: double-tap selects a number or function, hold selects it all,
    // and the system bar offers copy, cut, paste and select all. The selection lives in the
    // calculator (keys act on it); here it's turned into display offsets. Empty = paste only.
    val raw = { offset: Int -> formatted.displayToRaw[offset.coerceIn(0, formatted.displayToRaw.lastIndex)] }
    val display = { offset: Int -> formatted.rawToDisplay[offset.coerceIn(0, entry.text.length)] }
    val selection = entry.selection?.let { TextRange(display(it.start), display(it.end)) }
    val currentOnSelect by rememberUpdatedState(onSelect)
    val select = { range: TextRange? ->
        currentOnSelect(range?.let { if (entry.text.isEmpty()) Span(0, 0) else Span(raw(it.min), raw(it.max)) })
    }
    var anchor by remember { mutableStateOf<Rect?>(null) }
    val toolbar = LocalTextToolbar.current
    val clipboard = LocalClipboardManager.current
    val currentOnReplace by rememberUpdatedState(onReplace)
    val currentOnCopy by rememberUpdatedState(onCopy)
    LaunchedEffect(entry.selection, entry.text, anchor) {
        val sel = entry.selection
        val rect = anchor
        if (sel == null || rect == null) {
            toolbar.hide()
            return@LaunchedEffect
        }
        val start = sel.start.coerceIn(0, entry.text.length)
        val end = sel.end.coerceIn(start, entry.text.length)
        val picked = entry.text.substring(start, end)
        toolbar.showMenu(
            rect = rect,
            onCopyRequested = if (picked.isEmpty()) null else ({ currentOnCopy(picked); select(null) }),
            onPasteRequested = if (clipboard.hasText()) ({
                val pasted = clipboard.getText()?.text?.let { pastable(it, formatter.decimal) }.orEmpty()
                select(null)
                if (pasted.isNotEmpty()) currentOnReplace(start, end, pasted)
            }) else null,
            onCutRequested = if (picked.isEmpty()) null else ({ currentOnCopy(picked); select(null); currentOnReplace(start, end, "") }),
            onSelectAllRequested = if (entry.text.isEmpty() || (start == 0 && end == entry.text.length)) null
            else ({ currentOnSelect(Span(0, entry.text.length)) }),
        )
    }
    DisposableEffect(Unit) { onDispose { toolbar.hide() } }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp),
    ) {
        if (entry.text.isEmpty()) {
            Text(
                "0",
                style = entryStyle(maxFontSize, GoogleSansFlex),
                color = colors.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { anchor = it.boundsInRoot() }
                    // Nothing to select yet, but holding still offers paste.
                    .pointerInput(Unit) { detectTapGestures(onLongPress = { select(TextRange(0)) }) },
            )
        } else {
            ExpressionText(
                text = formatted.text,
                cursor = if (entry.resultShown) null else formatted.rawToDisplay[entry.cursor.coerceIn(0, entry.text.length)],
                maxFontSize = maxFontSize,
                color = colors.onSurface,
                onTap = { displayOffset ->
                    val raw = formatted.displayToRaw[displayOffset.coerceIn(0, formatted.displayToRaw.lastIndex)]
                    if (entry.selection != null) select(null)
                    if (raw != entry.cursor || entry.resultShown) {
                        onCursorTick()
                        onCursorMoved(raw)
                    }
                },
                selection = selection,
                onSelect = { select(it) },
                onAnchor = { anchor = it },
            )
        }
        val error = entry.error
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
            // Degrees vs radians only matters once there is trigonometry, so that's the only time it shows.
            val usesAngles = TRIG.any { it in entry.text }
            AnimatedVisibility(usesAngles, enter = scaleIn(spring(dampingRatio = 0.5f)) + fadeIn(), exit = scaleOut() + fadeOut()) {
                AngleChip(entry.angleUnit, onToggleAngle)
            }
            when {
                error != null -> Text(
                    stringResource(error.message()),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.error,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
                // Long answers shrink to fit rather than running off the edge.
                entry.preview != null -> FitText(
                    "= " + formatter.number(entry.preview),
                    style = TextStyle(
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.End,
                    ),
                    maxSize = 34.sp,
                    minSize = 16.sp,
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                )
            }
        }
    }
}

private val TRIG = listOf("sin", "cos", "tan")

@Composable
private fun AngleChip(unit: AngleUnit, onToggle: () -> Unit) {
    val description = stringResource(Res.string.angle_toggle_description)
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        AnimatedContent(unit, transitionSpec = { (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut()) }, label = "angle") {
            Text(
                stringResource(if (it == AngleUnit.DEGREES) Res.string.angle_degrees else Res.string.angle_radians),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

private fun CalcError.message(): StringResource = when (this) {
    CalcError.DIVIDE_BY_ZERO -> Res.string.error_divide_by_zero
    CalcError.DOMAIN -> Res.string.error_domain
    CalcError.SYNTAX -> Res.string.error_syntax
    CalcError.OVERFLOW -> Res.string.error_overflow
}

private fun entryStyle(size: TextUnit, family: FontFamily) = TextStyle(
    fontFamily = family,
    fontSize = size,
    letterSpacing = (-0.02).em,
    lineHeight = size * 1.1f,
    fontWeight = FontWeight.Normal,
    textAlign = TextAlign.End,
)

/**
 * The expression in the largest size that fits on one line, shrinking down to a minimum and
 * wrapping beyond that. Draws its own blinking cursor; tap to move it.
 */
@Composable
private fun ExpressionText(
    text: AnnotatedString,
    cursor: Int?,
    maxFontSize: TextUnit,
    color: Color,
    onTap: (Int) -> Unit,
    selection: TextRange?,
    onSelect: (TextRange) -> Unit,
    /** Where the selection sits on screen, for the copy/paste bar. */
    onAnchor: (Rect) -> Unit,
) {
    val measurer = rememberTextMeasurer()
    val cursorColor = MaterialTheme.colorScheme.primary
    val blink = remember { Animatable(1f) }
    LaunchedEffect(text, cursor) {
        blink.snapTo(1f)
        while (true) {
            delay(550)
            blink.animateTo(0f, tween(150))
            delay(350)
            blink.animateTo(1f, tween(150))
        }
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = constraints.maxWidth
        val minFontSize = (maxFontSize.value * 0.5f).sp
        val family = GoogleSansFlex
        val fontSize = remember(text, width, maxFontSize, family) {
            var size = maxFontSize.value
            while (size > minFontSize.value) {
                val result = measurer.measure(text, entryStyle(size.sp, family), maxLines = 1, softWrap = false, constraints = Constraints())
                if (result.size.width <= width) break
                size -= 2f
            }
            size.coerceAtLeast(minFontSize.value).sp
        }
        var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
        val currentOnTap by rememberUpdatedState(onTap)
        val currentOnSelect by rememberUpdatedState(onSelect)
        var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
        val selectionColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        LaunchedEffect(selection, layout, coordinates) {
            val l = layout ?: return@LaunchedEffect
            val c = coordinates ?: return@LaunchedEffect
            if (selection == null) return@LaunchedEffect
            val bounds = if (selection.collapsed) l.getCursorRect(selection.start.coerceAtMost(l.layoutInput.text.length))
            else l.getPathForRange(selection.min, selection.max).getBounds()
            onAnchor(bounds.translate(c.positionInRoot()))
        }
        Text(
            text = text,
            style = entryStyle(fontSize, GoogleSansFlex),
            color = color,
            onTextLayout = { layout = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { position -> layout?.let { currentOnTap(it.getOffsetForPosition(position)) } },
                        onDoubleTap = { position -> layout?.let { currentOnSelect(it.wordAt(position)) } },
                        onLongPress = { layout?.let { currentOnSelect(TextRange(0, it.layoutInput.text.length)) } },
                    )
                }
                .pointerInput(Unit) {
                    // Drag along the expression and the cursor follows the finger to that exact spot.
                    detectHorizontalDragGestures(
                        onDragStart = { position -> layout?.let { currentOnTap(it.getOffsetForPosition(position)) } },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            layout?.let { currentOnTap(it.getOffsetForPosition(change.position)) }
                        },
                    )
                }
                .drawWithContent {
                    val l = layout
                    if (l != null && selection != null && !selection.collapsed) {
                        drawPath(l.getPathForRange(selection.min, selection.max), selectionColor)
                    }
                    drawContent()
                    if (l == null || selection != null) return@drawWithContent
                    if (cursor == null || cursor > l.layoutInput.text.length) return@drawWithContent
                    val rect = l.getCursorRect(cursor)
                    val w = 2.5.dp.toPx()
                    drawRect(
                        color = cursorColor.copy(alpha = blink.value),
                        topLeft = Offset(rect.left - w / 2, rect.top + rect.height * 0.12f),
                        size = androidx.compose.ui.geometry.Size(w, rect.height * 0.76f),
                    )
                },
        )
    }
}

/** The number or function name under [position], or the single symbol there. */
private fun TextLayoutResult.wordAt(position: Offset): TextRange {
    val text = layoutInput.text.text
    if (text.isEmpty()) return TextRange(0)
    val at = getOffsetForPosition(position).coerceIn(0, text.length - 1)
    fun numeric(c: Char) = c.isDigit() || c == '.' || c == ',' || c == '\u00A0' || c == '\u202F' || c == ' '
    fun named(c: Char) = c.isLetter() || c == '⁻' || c == '¹'
    val kind: ((Char) -> Boolean)? = when {
        numeric(text[at]) -> ::numeric
        named(text[at]) -> ::named
        else -> null
    }
    if (kind == null) return TextRange(at, at + 1)
    var start = at
    while (start > 0 && kind(text[start - 1])) start--
    var end = at + 1
    while (end < text.length && kind(text[end])) end++
    return TextRange(start, end)
}

/** Clipboard text as the calculator writes it: its own symbols, no grouping, `.` for decimals. */
private fun pastable(text: String, decimal: Char): String {
    var s = text.trim().filterNot { it.isWhitespace() || it == '\u00A0' || it == '\u202F' }
    s = if (decimal == ',' && ',' in s) s.replace(".", "").replace(',', '.') else s.replace(",", "")
    return s.map {
        when (it) {
            '*' -> '×'
            '/', ':' -> '÷'
            '-', '–' -> '−'
            else -> it
        }
    }.joinToString("")
}
