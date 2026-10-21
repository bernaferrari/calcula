package com.bernaferrari.calculator.ui.calculator

import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.coroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import kotlin.math.abs
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.outlined.ShowChart
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.animation.core.animateFloatAsState
import com.bernaferrari.calculator.ui.containerTransform
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.bernaferrari.calculator.platform.systemConfirmsCopy
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.domain.input.Calculator
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.model.AngleUnit

@Composable
fun CalculatorScreen(
    initialExpression: String,
    onOpenSettings: () -> Unit,
    onOpenConverter: (seed: String?) -> Unit,
    onOpenGraph: () -> Unit = {},
    pendingInsert: String? = null,
    onInsertConsumed: () -> Unit = {},
    viewModel: CalculatorViewModel = viewModel { CalculatorViewModel(savedState = createSavedStateHandle()) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(initialExpression) { viewModel.loadExpression(initialExpression) }
    LaunchedEffect(pendingInsert) {
        if (pendingInsert != null) {
            viewModel.insertNumber(pendingInsert)
            onInsertConsumed()
        }
    }
    // The number on screen goes along to the converter, so "how many miles is this?" is one tap.
    val currentNumber = (if (state.resultShown) state.text else state.preview ?: state.text)
        .takeIf { it.isNotEmpty() && 'E' !in it && Calculator.isPlainNumber(it) }

    val formatLocale = state.decimalSeparator.separators()
    val formatter = remember(formatLocale) { DisplayFormatter(formatLocale) }
    val haptics = rememberHaptics(enabled = { state.haptics }, sounds = { state.sounds })
    @Suppress("DEPRECATION") val clipboard = LocalClipboardManager.current
    val copied = stringResource(Res.string.copied)
    val scope = rememberCoroutineScope()

    var layer by rememberSaveable { mutableStateOf(FunctionLayer.Off) }
    var historyOpen by rememberSaveable { mutableStateOf(false) }
    // Back closes history mode before leaving the app.
    @OptIn(ExperimentalComposeUiApi::class)
    BackHandler(enabled = historyOpen) { historyOpen = false }

    val snackbar = remember { SnackbarHostState() }
    val deletedOne = stringResource(Res.string.history_deleted)
    val deletedAll = stringResource(Res.string.history_cleared)
    val undo = stringResource(Res.string.undo)
    LaunchedEffect(Unit) {
        viewModel.removed.collect { entries ->
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(if (entries.size == 1) deletedOne else deletedAll, actionLabel = undo, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) viewModel.onRestore(entries)
        }
    }

    val copy: (String) -> Unit = { raw ->
        val text = if (Calculator.isPlainNumber(raw)) raw.replace('.', formatter.decimal).replace('−', '-') else raw
        clipboard.setText(AnnotatedString(text))
        if (!systemConfirmsCopy) scope.launch { snackbar.currentSnackbarData?.dismiss(); snackbar.showSnackbar(copied) }
    }

    val display: @Composable (Modifier, Boolean) -> Unit = { modifier, compact ->
        CalculatorDisplay(
            state = state,
            formatter = formatter,
            haptics = haptics,
            events = viewModel.events,
            onCursorMoved = viewModel::onCursorMoved,
            onReplace = viewModel::onReplace,
            onSelect = viewModel::onSelect,
            onToggleAngle = viewModel::onToggleAngleUnit,
            onCopy = copy,
            // Reusing a result means going back to calculating.
            onUseResult = { viewModel.onUseResult(it); historyOpen = false },
            onEditExpression = { viewModel.onEditExpression(it); historyOpen = false },
            onDeleteEntry = viewModel::onDeleteEntry,
            onClearHistory = viewModel::onClearHistory,
            historyOpen = historyOpen,
            entryMaxFontSize = if (compact) 52.sp else 72.sp,
            modifier = modifier,
        )
    }

    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    CompositionLocalProvider(LocalReduceMotion provides state.reduceMotion) {
    val currentHistoryOpen by rememberUpdatedState(historyOpen)
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .twoFingerHistory({ currentHistoryOpen }) { open -> haptics.tap(); historyOpen = open },
    ) {
        val landscape = maxWidth > maxHeight && maxHeight < 600.dp
        if (landscape) {
            Row(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)),
            ) {
                Box(Modifier.weight(0.42f).fillMaxHeight().padding(bottom = bottomInset)) {
                    display(Modifier.fillMaxSize().statusBarsPadding().padding(top = 44.dp), true)
                    // Landscape has no tool row; its non-key controls sit quietly at the top.
                    Row(Modifier.statusBarsPadding().fillMaxWidth().padding(4.dp)) {
                        IconButton(onClick = { haptics.tap(); historyOpen = !historyOpen }) {
                            Icon(
                                if (historyOpen) Icons.Outlined.Calculate else Icons.Outlined.History,
                                contentDescription = stringResource(if (historyOpen) Res.string.show_keypad else Res.string.show_history),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = onOpenGraph, modifier = Modifier.containerTransform("graph")) {
                            Icon(Icons.Outlined.ShowChart, contentDescription = stringResource(Res.string.graph_title), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { onOpenConverter(currentNumber) }, modifier = Modifier.containerTransform("converter")) {
                            Icon(Icons.Outlined.Straighten, contentDescription = stringResource(Res.string.menu_unit_converter), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = onOpenSettings, modifier = Modifier.containerTransform("settings")) {
                            Icon(Icons.Outlined.Settings, contentDescription = stringResource(Res.string.menu_settings), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Column(Modifier.weight(0.3f).fillMaxHeight()) {
                    Spacer(Modifier.fillMaxWidth().statusBarsPadding())
                    ScientificGrid(
                        inverse = state.inverse,
                        angleUnit = state.angleUnit,
                        haptics = haptics,
                        onKey = viewModel::onKey,
                        onToggleAngle = viewModel::onToggleAngleUnit,
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = bottomInset),
                    )
                }
                MainKeypad(
                    decimal = formatter.decimal,
                    haptics = haptics,
                    functionLayer = false,
                    onKey = viewModel::onKey,
                    onFunctionKey = viewModel::onSwipeKey,
                    onClearAll = { viewModel.onKey(Key.CLEAR) },
                    showDots = false,
                    inert = state.inert,
                    bottomInset = bottomInset,
                    modifier = Modifier.weight(0.4f).fillMaxHeight().statusBarsPadding(),
                )
            }
        } else {
            // Rows grow with the screen but never get cramped or comically tall; dragging the bar
            // above the keys scales them within those limits, and the tape takes what's left.
            val defaultRow = (maxHeight * 0.47f / 5).coerceIn(60.dp, 92.dp)
            val minRow = 56.dp
            val maxRow = max(defaultRow, min(maxHeight * 0.66f / 5, 120.dp))
            var dragRow by remember { mutableStateOf<Dp?>(null) }
            val rowHeight = dragRow ?: (defaultRow * state.keypadScale).coerceIn(minRow, maxRow)
            // History mode slides the keypad down and away, whole, and gives the screen to the tape.
            val fullKeypad = rowHeight * 5 + bottomInset
            val reveal = remember { Animatable(if (historyOpen) 0f else 1f) }
            var pulling by remember { mutableStateOf(false) }
            LaunchedEffect(historyOpen) {
                if (!pulling) reveal.animateTo(if (historyOpen) 0f else 1f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow))
            }
            val shown = reveal.value
            val resize = Modifier.keypadResize(
                rowHeight = rowHeight,
                limits = minRow..maxRow,
                fullKeypad = fullKeypad,
                historyOpen = historyOpen,
                reveal = reveal,
                onPulling = { pulling = it },
                onRow = { dragRow = it },
                onTick = haptics::tick,
                defaultRow = defaultRow,
                onDone = { row ->
                    val scale = row / defaultRow
                    if (scale == state.keypadScale) dragRow = null else viewModel.onKeypadScale(scale)
                },
                onHistory = { open -> historyOpen = open },
            )
            // The saved size has caught up with the finger: hand the height back to it.
            LaunchedEffect(state.keypadScale) { dragRow = null }
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    display(Modifier.fillMaxSize().statusBarsPadding(), false)
                    SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
                }
                ToolRow(
                    historyOpen = historyOpen,
                    layer = layer,
                    onToggleHistory = { haptics.tap(); historyOpen = !historyOpen },
                    onLayer = { next -> haptics.tap(); layer = next; historyOpen = false },
                    onOpenConverter = { onOpenConverter(currentNumber) },
                    onOpenGraph = onOpenGraph,
                    onOpenSettings = onOpenSettings,
                    resize = if (!pulling) null else {
                        val span = (maxRow - minRow).value.coerceAtLeast(1f)
                        ResizeFeedback(
                            fraction = (rowHeight - minRow).value / span,
                            defaultFraction = (defaultRow - minRow).value / span,
                            percent = (rowHeight / defaultRow * 100).roundToInt(),
                            mode = when {
                                historyOpen -> ResizeFeedback.Mode.ToKeypad
                                shown < 0.85f -> ResizeFeedback.Mode.ToHistory
                                else -> ResizeFeedback.Mode.Resize
                            },
                        )
                    },
                    // Takes over the bottom inset as the keypad leaves, in step with it.
                    modifier = resize.padding(bottom = bottomInset * (1f - shown)),
                )
                MainKeypad(
                    decimal = formatter.decimal,
                    haptics = haptics,
                    functionLayer = layer != FunctionLayer.Off,
                    onKey = viewModel::onKey,
                    onFunctionKey = { key ->
                        viewModel.onSwipeKey(key)
                        // Like Shift: one function, then back to digits (unless locked).
                        if (layer == FunctionLayer.Once) layer = FunctionLayer.Off
                    },
                    onClearAll = { viewModel.onKey(Key.CLEAR) },
                    showDots = state.showFlickDots,
                    inert = state.inert,
                    bottomInset = bottomInset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds()
                        // Laid out at full size, revealed through a shrinking slot: keys never squash.
                        .layout { measurable, constraints ->
                            val full = fullKeypad.roundToPx()
                            val placeable = measurable.measure(constraints.copy(minHeight = full, maxHeight = full))
                            layout(placeable.width, (full * shown).roundToInt()) { placeable.place(0, 0) }
                        }
                        .graphicsLayer { alpha = (shown * 1.6f).coerceIn(0f, 1f) },
                )
            }
        }
    }
    }
}

/**
 * The bar above the keys is a handle: drag it to make the keypad taller or shorter (the tape
 * gets the rest), keep pulling down past the smallest size and the keypad slides away into
 * history mode, and from history drag it back up to bring the keys home. Let go early and it
 * springs back; a tick marks each limit and the point of no return.
 */
@Composable
private fun Modifier.keypadResize(
    rowHeight: Dp,
    limits: ClosedRange<Dp>,
    fullKeypad: Dp,
    historyOpen: Boolean,
    reveal: Animatable<Float, *>,
    onPulling: (Boolean) -> Unit,
    onRow: (Dp?) -> Unit,
    onTick: () -> Unit,
    onDone: (Dp) -> Unit,
    onHistory: (Boolean) -> Unit,
    defaultRow: Dp,
): Modifier {
    // The gesture outlives recompositions; it reads today's sizes, not the first frame's.
    val currentRow by rememberUpdatedState(rowHeight)
    val currentLimits by rememberUpdatedState(limits)
    val currentFull by rememberUpdatedState(fullKeypad)
    val currentHistory by rememberUpdatedState(historyOpen)
    val currentDone by rememberUpdatedState(onDone)
    val currentDefault by rememberUpdatedState(defaultRow)
    return keypadResizeGesture({ currentRow }, { currentLimits }, { currentFull }, { currentHistory }, reveal, onPulling, onRow, onTick, { currentDone(it) }, onHistory, defaultRow = { currentDefault })
}

private fun Modifier.keypadResizeGesture(
    rowHeight: () -> Dp,
    limits: () -> ClosedRange<Dp>,
    fullKeypad: () -> Dp,
    historyOpen: () -> Boolean,
    reveal: Animatable<Float, *>,
    onPulling: (Boolean) -> Unit,
    onRow: (Dp?) -> Unit,
    onTick: () -> Unit,
    onDone: (Dp) -> Unit,
    onHistory: (Boolean) -> Unit,
    /** The size the keypad has by default: dragging past it catches there for a moment, with a tick. */
    defaultRow: () -> Dp,
): Modifier = pointerInput(Unit) {
    var startRow = 0f
    var offset = 0f
    var fromHistory = false
    var past = false
    var atLimit = false
    var atHome = false
    var row = 0.dp
    val threshold = 72.dp.toPx()
    coroutineScope {
        val start: () -> Unit = {
            startRow = rowHeight().toPx()
            offset = 0f
            fromHistory = historyOpen()
            past = false
            atLimit = false
            row = rowHeight()
            onPulling(true)
        }
        val drag: (PointerInputChange, Float) -> Unit = drag@{ change, dy ->
            change.consume()
            offset += dy
            val full = fullKeypad().toPx()
            if (fromHistory) {
                // Up from history: the keys rise with the finger.
                val r = (-offset / full).coerceIn(0f, 1f)
                launch { reveal.snapTo(r) }
                val nowPast = -offset > threshold
                if (nowPast != past) { past = nowPast; onTick() }
                return@drag
            }
            // The keypad's top follows the finger: five rows share the change.
            val range = limits()
            val wanted = startRow - offset / 5f
            val min = range.start.toPx()
            val max = range.endInclusive.toPx()
            // The default size is a soft detent: within a few px of it, the keys rest there.
            val home = defaultRow().toPx()
            val nearHome = abs(wanted - home) < 3.dp.toPx()
            if (nearHome && !atHome) onTick()
            atHome = nearHome
            val clamped = when {
                nearHome -> home
                wanted < min -> min
                // A little give beyond the tallest size, so the limit is felt rather than hit.
                wanted > max -> max + (wanted - max) * 0.15f
                else -> wanted
            }
            row = clamped.toDp()
            onRow(row)
            val limited = wanted <= min || wanted >= max
            if (limited && !atLimit) onTick()
            atLimit = limited
            // Past the smallest size, the pull starts sending the keypad away.
            val over = ((min - wanted) * 5f).coerceAtLeast(0f)
            launch { reveal.snapTo(1f - (over / full).coerceIn(0f, 1f)) }
            val nowPast = over > threshold
            if (nowPast != past) { past = nowPast; onTick() }
        }
        val end: () -> Unit = end@{
            onPulling(false)
            if (fromHistory) {
                if (past) onHistory(false) else launch { reveal.animateTo(0f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) }
                return@end
            }
            if (past) {
                // A pull into history isn't a vote for tiny keys: they come back at the size they had.
                onRow(null)
                onHistory(true)
                return@end
            }
            val range = limits()
            val settled = row.coerceIn(range.start, range.endInclusive)
            onRow(settled)
            onDone(settled)
            launch { reveal.animateTo(1f, spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) }
        }
        val cancel: () -> Unit = {
            onPulling(false)
            launch { reveal.animateTo(if (historyOpen()) 0f else 1f) }
        }
        detectVerticalDragGestures(
            onDragStart = { start() },
            onVerticalDrag = { change, dy -> drag(change, dy) },
            onDragEnd = { end() },
            onDragCancel = { cancel() },
        )
    }
}

/**
 * Two fingers anywhere: swipe down for history, up to bring the keypad back. Watched before the
 * keys and the tape see it, and taken from them once the fingers move, so nothing gets typed.
 */
private fun Modifier.twoFingerHistory(historyOpen: () -> Boolean, onHistory: (Boolean) -> Unit): Modifier = pointerInput(Unit) {
    val threshold = 48.dp.toPx()
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        var travel = 0f
        var twoFingers = false
        var fired = false
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val pressed = event.changes.filter { it.pressed && it.previousPressed }
            if (event.changes.count { it.pressed } >= 2) twoFingers = true
            if (twoFingers && pressed.isNotEmpty()) {
                travel += pressed.sumOf { (it.position.y - it.previousPosition.y).toDouble() }.toFloat() / pressed.size
                if (abs(travel) > viewConfiguration.touchSlop) event.changes.forEach { it.consume() }
                if (!fired && travel > threshold && !historyOpen()) { fired = true; onHistory(true) }
                if (!fired && travel < -threshold && historyOpen()) { fired = true; onHistory(false) }
            }
        } while (event.changes.any { it.pressed })
    }
}
