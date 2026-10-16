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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.R
import com.bernaferrari.calculator.domain.input.Outcome
import com.bernaferrari.calculator.domain.math.CalcError
import com.bernaferrari.calculator.domain.model.CalculationHistory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt

internal const val NumberFeatures = "tnum"

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
                state.text, state.cursor, state.resultShown, state.preview, state.error, state.openParentheses, state.generation, state.angleUnit,
            )
            // History mode is about the past: the live entry steps aside until the keypad returns.
            AnimatedVisibility(!historyOpen, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
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
    val density = LocalDensity.current
        val copyText = entry.preview ?: entry.text

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp),
    ) {
        if (entry.text.isEmpty()) {
            Text(
                "0",
                style = entryStyle(maxFontSize),
                color = colors.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            ExpressionText(
                text = formatted.text,
                cursor = if (entry.resultShown) null else formatted.rawToDisplay[entry.cursor.coerceIn(0, entry.text.length)],
                maxFontSize = maxFontSize,
                color = colors.onSurface,
                onTap = { displayOffset ->
                    val raw = formatted.displayToRaw[displayOffset.coerceIn(0, formatted.displayToRaw.lastIndex)]
                    if (raw != entry.cursor || entry.resultShown) {
                        onCursorTick()
                        onCursorMoved(raw)
                    }
                },
                onLongPress = { onCopy(copyText) },
            )
        }
        val error = entry.error
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
            // Degrees vs radians only matters once there is trigonometry, so that's the only time it shows.
            val usesAngles = TRIG.any { it in entry.text }
            AnimatedVisibility(usesAngles, enter = scaleIn(spring(dampingRatio = 0.5f)) + fadeIn(), exit = scaleOut() + fadeOut()) {
                AngleChip(entry.angleUnit, onToggleAngle)
            }
            Spacer(Modifier.weight(1f))
            when {
                error != null -> Text(
                    stringResource(error.message()),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.error,
                )
                entry.preview != null -> BasicText(
                    "= " + formatter.number(entry.preview),
                    style = TextStyle(
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal,
                        fontFeatureSettings = NumberFeatures,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.End,
                    ),
                    maxLines = 1,
                    softWrap = false,
                    // Long answers shrink to fit rather than running off the edge.
                    autoSize = TextAutoSize.StepBased(minFontSize = 16.sp, maxFontSize = 34.sp, stepSize = 1.sp),
                    modifier = Modifier.weight(1f, fill = false).padding(start = 12.dp),
                )
            }
        }
    }
}

private val TRIG = listOf("sin", "cos", "tan")

@Composable
private fun AngleChip(unit: AngleUnit, onToggle: () -> Unit) {
    val description = stringResource(R.string.angle_toggle_description)
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        AnimatedContent(unit, transitionSpec = { (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut()) }, label = "angle") {
            Text(
                stringResource(if (it == AngleUnit.DEGREES) R.string.angle_degrees else R.string.angle_radians),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

private fun CalcError.message(): Int = when (this) {
    CalcError.DIVIDE_BY_ZERO -> R.string.error_divide_by_zero
    CalcError.DOMAIN -> R.string.error_domain
    CalcError.SYNTAX -> R.string.error_syntax
    CalcError.OVERFLOW -> R.string.error_overflow
}

private fun entryStyle(size: TextUnit) = TextStyle(
    fontFamily = GoogleSansFlex,
    fontSize = size,
    letterSpacing = (-0.02).em,
    lineHeight = size * 1.1f,
    fontWeight = FontWeight.Normal,
    textAlign = TextAlign.End,
    fontFeatureSettings = NumberFeatures,
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
    onLongPress: () -> Unit,
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
        val fontSize = remember(text, width, maxFontSize) {
            var size = maxFontSize.value
            while (size > minFontSize.value) {
                val result = measurer.measure(text, entryStyle(size.sp), maxLines = 1, softWrap = false, constraints = Constraints())
                if (result.size.width <= width) break
                size -= 2f
            }
            size.coerceAtLeast(minFontSize.value).sp
        }
        var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
        val currentOnTap by rememberUpdatedState(onTap)
        val currentOnLongPress by rememberUpdatedState(onLongPress)
        Text(
            text = text,
            style = entryStyle(fontSize),
            color = color,
            onTextLayout = { layout = it },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { position -> layout?.let { currentOnTap(it.getOffsetForPosition(position)) } },
                        onLongPress = { currentOnLongPress() },
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
                    drawContent()
                    val l = layout ?: return@drawWithContent
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
