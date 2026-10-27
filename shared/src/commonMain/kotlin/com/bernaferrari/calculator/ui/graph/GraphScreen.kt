package com.bernaferrari.calculator.ui.graph

import com.bernaferrari.calculator.ui.calculator.revealOnHold
import androidx.compose.runtime.mutableIntStateOf
import com.bernaferrari.calculator.ui.calculator.ResizePill
import com.bernaferrari.calculator.ui.calculator.ResizeFeedback
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.Dp
import com.bernaferrari.calculator.domain.model.hints
import com.bernaferrari.calculator.ui.calculator.FunctionLayer
import com.bernaferrari.calculator.ui.calculator.LayerSwitch
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.rememberTextMeasurer
import com.bernaferrari.calculator.platform.encodePng
import com.bernaferrari.calculator.platform.rememberImageSharer
import androidx.compose.runtime.key
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CenterFocusStrong
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bernaferrari.calculator.AppGraph
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.ui.calculator.MainKeypad
import com.bernaferrari.calculator.ui.calculator.rememberHaptics
import com.bernaferrari.calculator.ui.calculator.separators
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.ui.settings.SettingsViewModel
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

/**
 * Graphs, Calcula style: the plot on top with pinch, pan and a hold-to-trace finger, the
 * functions in the middle (tap one to edit it), and the calculator's own keys below, flicks
 * included, with `x` where `%` usually sits (flick it up for `y`) and `=` typing equations.
 */
@Composable
fun GraphScreen(
    onNavigateBack: () -> Unit,
    viewModel: GraphViewModel = viewModel { GraphViewModel(AppGraph.graph) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Each ask to come home (the button, a double-tap); the plot glides there rather than jumping.
    var homeRequests by remember { mutableIntStateOf(0) }
    val settings: SettingsViewModel = viewModel { SettingsViewModel() }
    val prefs by settings.prefs.collectAsStateWithLifecycle()
    val haptics = rememberHaptics(enabled = { prefs.hapticFeedback }, sounds = { prefs.soundEnabled })
    val separators = prefs.decimalSeparator.separators()
    val formatter = remember(separators) { DisplayFormatter(separators) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val colors = curveColors()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    var libraryOpen by rememberSaveable { mutableStateOf(false) }
    val share = rememberImageSharer()
    val exportMeasurer = rememberTextMeasurer()
    val exportFont = GoogleSansFlex
    val scheme = MaterialTheme.colorScheme
    var layer by rememberSaveable { mutableStateOf(FunctionLayer.Off) }

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("graph", isScreen = true).fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal))) {
            // Side by side on phones held sideways, and on any wide window (tablets, unfolded foldables).
            val landscape = maxWidth > maxHeight && (maxHeight < 600.dp || maxWidth >= 840.dp)
            val header: @Composable () -> Unit = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp).height(56.dp)) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
                    Text(stringResource(Res.string.graph_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = { haptics.tap(); libraryOpen = true }) {
                        Icon(Icons.Outlined.History, contentDescription = stringResource(Res.string.graph_open_library), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = {
                        haptics.tap()
                        share(renderGraphImage(state, colors, scheme, exportMeasurer, formatter, exportFont).encodePng(), "graph.png")
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = stringResource(Res.string.graph_share), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { haptics.tap(); homeRequests++ }) {
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
                    onReset = { haptics.tap(); homeRequests++ },
                    homeRequests = homeRequests,
                    liveViewport = { viewModel.state.value.viewport },
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
                    onCursor = { index, position -> haptics.tick(); viewModel.onActivate(index); viewModel.onCursorMoved(position) },
                    onRemove = { haptics.heavy(); viewModel.onRemove(it) },
                    onExample = { haptics.tap(); viewModel.onExample(it) },
                )
                ParameterSliders(
                    state = state,
                    formatter = formatter,
                    onInsert = { haptics.tap(); viewModel.onInsertParameter(it) },
                    onChange = viewModel::onParameter,
                    onTick = haptics::tick,
                )
            }
            val keys: @Composable (Modifier) -> Unit = { modifier ->
                MainKeypad(
                    decimal = formatter.decimal,
                    haptics = haptics,
                    functionLayer = layer != FunctionLayer.Off,
                    onKey = viewModel::onKey,
                    onFunctionKey = { key ->
                        viewModel.onKey(key)
                        // Like Shift: one function, then back to digits (unless locked).
                        if (layer == FunctionLayer.Once) layer = FunctionLayer.Off
                    },
                    onClearAll = { viewModel.onKey(com.bernaferrari.calculator.domain.input.Key.CLEAR) },
                    // The same keypad as the calculator, dots and all (they step aside on very short rows).
                    hints = prefs.hints,
                    bottomInset = bottomInset,
                    variable = true,
                    nextParameter = state.nextParameter,
                    modifier = modifier,
                )
            }
            // The calculator's own 123|ƒ switch, so every function is a tap away here too, and the
            // way to add another function, always in reach above the keys.
            // Like the calculator's bar, it's also a handle: drag it to size the keys (portrait only).
            // While dragging, its controls step back for the same pill that shows the size.
            val bar: @Composable (Modifier, ResizeFeedback?) -> Unit = { handle, resize ->
                val step by animateFloatAsState(if (resize != null) 1f else 0f, if (prefs.reduceMotion) snap() else spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium), label = "step")
                Box(handle.fillMaxWidth().height(GraphBarHeight)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.matchParentSize().padding(horizontal = 12.dp).graphicsLayer {
                            alpha = 1f - step
                            scaleX = 1f - 0.08f * step
                            scaleY = 1f - 0.08f * step
                        },
                    ) {
                        LayerSwitch(layer) { haptics.tap(); layer = it }
                        Spacer(Modifier.weight(1f))
                        if (state.canAdd) {
                            TextButton(onClick = { haptics.tap(); viewModel.onAdd() }) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(Res.string.graph_add))
                            }
                        }
                    }
                    ResizePill(resize, Modifier.align(Alignment.Center))
                }
            }
            if (landscape) {
                Row(Modifier.fillMaxSize()) {
                    Column(Modifier.weight(0.55f).fillMaxHeight()) {
                        header()
                        plot(Modifier.weight(1f).fillMaxWidth().padding(bottom = 12.dp + bottomInset))
                    }
                    Column(Modifier.weight(0.45f).fillMaxHeight().statusBarsPadding()) {
                        functions()
                        bar(Modifier, null)
                        keys(Modifier.weight(1f).fillMaxWidth())
                    }
                }
            } else {
                val defaultRow = (maxHeight * 0.34f / 5).coerceIn(50.dp, 68.dp)
                val minRow = 50.dp
                val maxRow = max(defaultRow, min(maxHeight * 0.5f / 5, 88.dp))
                var dragRow by remember { mutableStateOf<Dp?>(null) }
                var dragging by remember { mutableStateOf(false) }
                // Holding the bar shows the size pill before any drag, so people find it.
                var holding by remember { mutableStateOf(false) }
                val rowHeight = dragRow ?: (defaultRow * prefs.graphKeypadScale).coerceIn(minRow, maxRow)
                // The saved size has caught up with the finger: hand the height back to it.
                LaunchedEffect(prefs.graphKeypadScale) { dragRow = null }
                val handle = Modifier.keypadResize(
                    rowHeight = { rowHeight },
                    limits = { minRow..maxRow },
                    defaultRow = { defaultRow },
                    onRow = { dragRow = it },
                    onDragging = { dragging = it },
                    onTick = haptics::tick,
                    onDone = { row ->
                        val scale = row / defaultRow
                        // Unchanged (say, back on the default), nothing will come back to clear it.
                        if (scale == prefs.graphKeypadScale) dragRow = null else settings.setGraphKeypadScale(scale)
                    },
                ).revealOnHold(haptics::tick) { holding = it }
                val resize = if (!dragging && !holding) null else {
                    val span = (maxRow - minRow).value.coerceAtLeast(1f)
                    ResizeFeedback(
                        fraction = (rowHeight - minRow).value / span,
                        defaultFraction = (defaultRow - minRow).value / span,
                        percent = (rowHeight / defaultRow * 100).roundToInt(),
                        mode = ResizeFeedback.Mode.Resize,
                    )
                }
                val keypad = rowHeight * 5 + bottomInset
                val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 56.dp
                // The plot is the point: it always keeps a good share of the screen. The functions and
                // sliders get what's left and scroll beyond that, instead of squeezing the plot to a strip.
                val minPlot = maxOf(200.dp, maxHeight * 0.3f)
                val panel = (maxHeight - top - keypad - GraphBarHeight - minPlot).coerceAtLeast(96.dp)
                Column(Modifier.fillMaxSize()) {
                    header()
                    plot(Modifier.weight(1f).fillMaxWidth())
                    val scroll = rememberScrollState()
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = panel)
                            // When more is below (examples, sliders), the edge fades to say so.
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .drawWithContent {
                                drawContent()
                                if (scroll.canScrollForward) {
                                    val fade = 28.dp.toPx()
                                    drawRect(
                                        Brush.verticalGradient(listOf(Color.Black, Color.Transparent), startY = size.height - fade, endY = size.height),
                                        topLeft = Offset(0f, size.height - fade),
                                        blendMode = BlendMode.DstIn,
                                    )
                                }
                            }
                            .verticalScroll(scroll),
                    ) {
                        functions()
                    }
                    bar(handle, resize)
                    keys(Modifier.fillMaxWidth().height(keypad))
                }
            }
        }
        if (libraryOpen) {
            GraphLibrarySheet(
                graphs = visibleRecent(recent, state),
                colors = colors,
                formatter = formatter,
                onRestore = { haptics.tap(); viewModel.onRestore(it); libraryOpen = false },
                onTogglePin = { haptics.tick(); viewModel.onTogglePin(it) },
                onDelete = { haptics.heavy(); viewModel.onDelete(it) },
                onNew = { haptics.tap(); viewModel.onNewGraph(); libraryOpen = false },
                onDismiss = { libraryOpen = false },
            )
        }
    }
}
