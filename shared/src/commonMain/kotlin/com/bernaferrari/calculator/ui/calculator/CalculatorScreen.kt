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
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
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
                    bottomInset = bottomInset,
                    modifier = Modifier.weight(0.4f).fillMaxHeight().statusBarsPadding(),
                )
            }
        } else {
            // Rows grow with the screen but never get cramped or comically tall.
            val rowHeight = (maxHeight * 0.47f / 5).coerceIn(60.dp, 92.dp)
            // History mode slides the keypad down and away, whole, and gives the screen to the tape.
            val fullKeypad = rowHeight * 5 + bottomInset
            val shown by animateFloatAsState(
                if (historyOpen) 0f else 1f,
                spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow),
                label = "keypad",
            )
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
                    // Takes over the bottom inset as the keypad leaves, in step with it.
                    modifier = Modifier.padding(bottom = bottomInset * (1f - shown)),
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
