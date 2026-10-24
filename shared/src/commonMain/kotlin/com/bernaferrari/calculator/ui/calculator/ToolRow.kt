package com.bernaferrari.calculator.ui.calculator

import androidx.compose.material3.AppBarRowScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.AppBarRow
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.outlined.Code
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex

/** Off shows digits; Once flips back after one function, like Shift; Locked stays, like Caps Lock. */
enum class FunctionLayer { Off, Once, Locked }

private val SegmentWidth = 48.dp
private val SegmentStyle @Composable get() = TextStyle(fontFamily = GoogleSansFlex, fontSize = 16.sp, fontWeight = FontWeight.Medium)

/** What a drag on the bar is doing right now, for the pill that stands in for the bar meanwhile. */
data class ResizeFeedback(
    /** Where the keypad's size sits between its smallest (0) and tallest (1). */
    val fraction: Float,
    /** Where the default size sits on that same scale, marked on the track. */
    val defaultFraction: Float,
    /** The size relative to the default, in percent. */
    val percent: Int,
    val mode: Mode,
    /** Letting go now would switch to history or back to the keypad (not just resize). */
    val armed: Boolean = false,
    /** How far toward [armed] the pull has come, 0 to 1; drawn around the pill's edge. */
    val progress: Float = 0f,
) {
    enum class Mode { Resize, ToHistory, ToKeypad }
}

/**
 * The row between display and keypad: the 123/ƒ switch on the left, by the keys it changes;
 * graph and converter on the right, with history and settings in the overflow. Drag the bar up
 * or down to resize the keypad (a pill shows the size meanwhile); keep pulling down for history.
 */
@Composable
fun ToolRow(
    historyOpen: Boolean,
    layer: FunctionLayer,
    onToggleHistory: () -> Unit,
    onLayer: (FunctionLayer) -> Unit,
    onOpenConverter: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenGraph: () -> Unit = {},
    onOpenProgrammer: () -> Unit = {},
    onOpenLibrary: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
    /** Set while the bar is being dragged: the controls step back and a pill shows the size. */
    resize: ResizeFeedback? = null,
    /** False when history already has a pane of its own, so the menu doesn't offer it. */
    showHistoryEntry: Boolean = true,
    /** False when every key already names its functions (Labels), so 123|ƒ would add nothing. */
    showLayerSwitch: Boolean = true,
    /**
     * ‹ › to step the cursor, where 123|ƒ would be: only in Labels mode, and only while there's an
     * expression to move through. Each returns whether the cursor moved.
     */
    cursorArrows: Boolean = false,
    /** Whether the cursor has room to go left / right; an arrow with nowhere to go is dimmed. */
    canStepLeft: Boolean = true,
    canStepRight: Boolean = true,
    onCursorStep: (Int) -> Boolean = { false },
    onCursorJump: (Int) -> Boolean = { false },
    haptics: Haptics? = null,
) {
    val quiet = MaterialTheme.colorScheme.onSurfaceVariant
    val dragging = resize != null
    val step by animateFloatAsState(if (dragging) 1f else 0f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium), label = "step")
    Box(modifier.fillMaxWidth().height(52.dp).padding(horizontal = 8.dp)) {
        Box(
            Modifier.matchParentSize().graphicsLayer {
                alpha = 1f - step
                scaleX = 1f - 0.08f * step
                scaleY = 1f - 0.08f * step
            },
        ) {
        // Functions mean nothing while browsing history, so the switch becomes the way back.
        AnimatedContent(
            historyOpen,
            transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.9f)) },
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp),
            label = "left",
        ) { open ->
            if (open) {
                IconButton(onClick = onToggleHistory) {
                    // The keys come back up from below: a chevron says so better than a dial pad.
                    Icon(Icons.Rounded.ExpandLess, contentDescription = stringResource(Res.string.show_keypad), tint = quiet)
                }
            } else if (showLayerSwitch) {
                LayerSwitch(layer, onLayer)
            } else {
                AnimatedVisibility(cursorArrows, enter = fadeIn() + scaleIn(initialScale = 0.9f), exit = fadeOut() + scaleOut(targetScale = 0.9f)) {
                    Row {
                        CursorArrow(-1, canStepLeft, quiet, haptics, onCursorStep, onCursorJump)
                        CursorArrow(1, canStepRight, quiet, haptics, onCursorStep, onCursorJump)
                    }
                }
            }
        }
        ToolActions(
            tint = quiet,
            onOpenGraph = onOpenGraph,
            onOpenConverter = onOpenConverter,
            onOpenProgrammer = onOpenProgrammer,
            onOpenLibrary = onOpenLibrary,
            onOpenStatistics = onOpenStatistics,
            // The same items whether history is open or not: AppBarRow can't take its list changing
            // under an open menu (picking History used to crash it). In history mode it goes back.
            onOpenHistory = if (showHistoryEntry) onToggleHistory else null,
            onOpenSettings = onOpenSettings,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
        }
        ResizePill(resize, Modifier.align(Alignment.Center))
    }
}

/** Tap for one step, hold to jump to that end. Dimmed, and off, once the cursor is at that end. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CursorArrow(direction: Int, enabled: Boolean, tint: Color, haptics: Haptics?, onStep: (Int) -> Boolean, onJump: (Int) -> Boolean) {
    val description = stringResource(if (direction < 0) Res.string.cursor_left else Res.string.cursor_right)
    val alpha by animateFloatAsState(if (enabled) 1f else 0.38f, label = "arrow")
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .combinedClickable(
                enabled = enabled,
                role = Role.Button,
                onClickLabel = description,
                onClick = { if (onStep(direction)) haptics?.tick() },
                onLongClick = { if (onJump(direction)) haptics?.heavy() },
            )
            .semantics { contentDescription = description },
    ) {
        Icon(
            if (direction < 0) Icons.AutoMirrored.Rounded.KeyboardArrowLeft else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = tint.copy(alpha = tint.alpha * alpha),
            modifier = Modifier.size(28.dp),
        )
    }
}

/**
 * A line traced around the pill's edge, clockwise from the top, as far as [progress] goes: how
 * close the pull is to switching. Once it's armed the pill turns solid instead.
 */
private fun Modifier.pullProgress(progress: Float, color: Color): Modifier = drawWithContent {
    drawContent()
    if (progress <= 0f) return@drawWithContent
    val stroke = 2.5.dp.toPx()
    val inset = stroke / 2
    val r = size.height / 2 - inset
    // The capsule's outline, starting at the top centre.
    val outline = Path().apply {
        moveTo(size.width / 2, inset)
        lineTo(size.width - inset - r, inset)
        arcTo(Rect(size.width - inset - 2 * r, inset, size.width - inset, size.height - inset), -90f, 180f, false)
        lineTo(inset + r, size.height - inset)
        arcTo(Rect(inset, inset, inset + 2 * r, size.height - inset), 90f, 180f, false)
        lineTo(size.width / 2, inset)
    }
    val measure = PathMeasure().apply { setPath(outline, false) }
    val traced = Path()
    measure.getSegment(0f, measure.length * progress.coerceIn(0f, 1f), traced, true)
    drawPath(traced, color, style = Stroke(width = stroke, cap = StrokeCap.Round))
}

/**
 * The tools, Material's way: an [AppBarRow] shows graph and the converter as icons while there's
 * room and folds them into ⋮ when there isn't; the rest (programmer, library, statistics, history,
 * settings) always live in ⋮. Settings grows out of the ⋮ button.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToolActions(
    tint: Color,
    onOpenGraph: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenProgrammer: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenHistory: (() -> Unit)?,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val graph = stringResource(Res.string.graph_title)
    val converter = stringResource(Res.string.menu_unit_converter)
    val programmer = stringResource(Res.string.menu_programmer)
    val library = stringResource(Res.string.menu_library)
    val statistics = stringResource(Res.string.menu_statistics)
    val history = stringResource(Res.string.settings_history)
    val settings = stringResource(Res.string.menu_settings)
    val more = stringResource(Res.string.more_options)
    AppBarRow(
        modifier = modifier,
        // Two icons and ⋮: everything past the first two goes in the menu.
        maxItemCount = 3,
        overflowIndicator = { state ->
            Box(Modifier.containerTransform("settings")) {
                IconButton(onClick = { if (state.isShowing) state.dismiss() else state.show() }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = more, tint = tint)
                }
            }
        },
    ) {
        customItem(
            appbarContent = {
                IconButton(onClick = onOpenGraph, modifier = Modifier.containerTransform("graph")) {
                    Icon(Icons.Outlined.ShowChart, contentDescription = graph, tint = tint)
                }
            },
            menuContent = { state -> MenuEntry(graph, Icons.Outlined.ShowChart) { state.dismiss(); onOpenGraph() } },
        )
        customItem(
            appbarContent = {
                IconButton(onClick = onOpenConverter, modifier = Modifier.containerTransform("converter")) {
                    Icon(Icons.Outlined.Straighten, contentDescription = converter, tint = tint)
                }
            },
            menuContent = { state -> MenuEntry(converter, Icons.Outlined.Straighten) { state.dismiss(); onOpenConverter() } },
        )
        tool(programmer, Icons.Outlined.Code, tint, onOpenProgrammer)
        tool(library, Icons.Outlined.AutoStories, tint, onOpenLibrary)
        tool(statistics, Icons.Outlined.BarChart, tint, onOpenStatistics)
        if (onOpenHistory != null) tool(history, Icons.Outlined.History, tint, onOpenHistory)
        tool(settings, Icons.Outlined.Settings, tint, onOpenSettings)
    }
}

/** An action that is an icon in the row when it fits, and an icon and a name in ⋮ when it doesn't. */
private fun AppBarRowScope.tool(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) = customItem(
    appbarContent = { IconButton(onClick = onClick) { Icon(icon, contentDescription = label, tint = tint) } },
    menuContent = { state -> MenuEntry(label, icon) { state.dismiss(); onClick() } },
)

@Composable
private fun MenuEntry(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(label) }, leadingIcon = { Icon(icon, contentDescription = null) }, onClick = onClick)
}

/**
 * Stands in for the bar while it's dragged: an arrow, the size in percent and a small track with
 * the default marked. Pulled past the smallest size it says where the pull is going instead.
 */
@Composable
internal fun ResizePill(feedback: ResizeFeedback?, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    // Keeps showing the last reading while the pill animates away.
    var last by remember { mutableStateOf(feedback) }
    if (feedback != null) last = feedback
    AnimatedVisibility(
        feedback != null,
        enter = fadeIn(tween(90)) + scaleIn(spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium), initialScale = 0.6f),
        exit = fadeOut(tween(140)) + scaleOut(tween(160), targetScale = 0.85f),
        modifier = modifier,
    ) {
        val f = last ?: return@AnimatedVisibility
        val fill by animateFloatAsState(f.fraction, spring(stiffness = Spring.StiffnessHigh), label = "fill")
        // Solid once letting go will act, like pull-to-refresh: the pill says what release does.
        val solid = f.armed && f.mode != ResizeFeedback.Mode.Resize
        val pillColor by animateColorAsState(if (solid) colors.primary else colors.primaryContainer, label = "pill")
        val ink by animateColorAsState(if (solid) colors.onPrimary else colors.onPrimaryContainer, label = "pillInk")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(40.dp)
                .clip(CircleShape)
                .background(pillColor)
                .pullProgress(if (f.mode == ResizeFeedback.Mode.Resize || solid) 0f else f.progress, colors.primary)
                .animateContentSize(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium))
                .padding(horizontal = 16.dp),
        ) {
            AnimatedContent(
                f.mode,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.6f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.6f)) },
                label = "pillMode",
            ) { mode ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (mode) {
                            ResizeFeedback.Mode.Resize -> Icons.Rounded.UnfoldMore
                            ResizeFeedback.Mode.ToHistory -> Icons.Outlined.History
                            ResizeFeedback.Mode.ToKeypad -> Icons.Rounded.ExpandLess
                        },
                        contentDescription = null,
                        tint = ink,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    when (mode) {
                        ResizeFeedback.Mode.Resize -> {
                            Text(
                                "${f.percent}%",
                                style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 15.sp, fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum"),
                                color = ink,
                                modifier = Modifier.widthIn(min = 44.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            // The track: how far between smallest and tallest, with a notch at the default.
                            Box(Modifier.size(width = 72.dp, height = 6.dp).clip(CircleShape).background(colors.onPrimaryContainer.copy(alpha = 0.18f))) {
                                Box(Modifier.fillMaxHeight().fillMaxWidth(fill.coerceIn(0f, 1f)).clip(CircleShape).background(colors.primary))
                                Box(
                                    Modifier
                                        .offset(x = 72.dp * f.defaultFraction.coerceIn(0f, 1f) - 1.dp)
                                        .size(width = 2.dp, height = 6.dp)
                                        .background(colors.onPrimaryContainer.copy(alpha = 0.7f)),
                                )
                            }
                        }
                        ResizeFeedback.Mode.ToHistory -> Text(
                            stringResource(if (f.armed) Res.string.resize_release_history else Res.string.resize_pull_history),
                            style = MaterialTheme.typography.labelLarge,
                            color = ink,
                        )
                        ResizeFeedback.Mode.ToKeypad -> Text(
                            stringResource(if (f.armed) Res.string.resize_release_keypad else Res.string.resize_pull_keypad),
                            style = MaterialTheme.typography.labelLarge,
                            color = ink,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun LayerSwitch(layer: FunctionLayer, onLayer: (FunctionLayer) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val on = layer != FunctionLayer.Off
    val locked = layer == FunctionLayer.Locked
    val segment = with(LocalDensity.current) { SegmentWidth.toPx() }
    // The indicator is also a thumb: slide it between 123 and ƒ, like an iOS segmented control.
    val indicator = remember { Animatable(if (on) segment else 0f) }
    var dragging by remember { mutableStateOf(false) }
    val settle = spring<Float>(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow)
    LaunchedEffect(on) { if (!dragging) indicator.animateTo(if (on) segment else 0f, settle) }
    val scope = rememberCoroutineScope()
    val currentLayer by rememberUpdatedState(layer)
    val indicatorColor by animateColorAsState(
        when {
            locked -> colors.primary
            on -> colors.primaryContainer
            else -> colors.surfaceContainerHighest
        },
        label = "indicatorColor",
    )
    val description = stringResource(Res.string.function_layer_description)
    Box(
        Modifier
            .height(40.dp)
            .width(SegmentWidth * 2)
            .clip(CircleShape)
            .background(colors.surfaceContainer)
            .pointerInput(segment) {
                detectHorizontalDragGestures(
                    onDragStart = { dragging = true },
                    onHorizontalDrag = { change, dx ->
                        change.consume()
                        scope.launch { indicator.snapTo((indicator.value + dx).coerceIn(0f, segment)) }
                    },
                    onDragEnd = {
                        dragging = false
                        val toFunctions = indicator.value > segment / 2
                        scope.launch { indicator.animateTo(if (toFunctions) segment else 0f, settle) }
                        val next = when {
                            !toFunctions -> FunctionLayer.Off
                            currentLayer == FunctionLayer.Off -> FunctionLayer.Once
                            else -> currentLayer
                        }
                        if (next != currentLayer) onLayer(next)
                    },
                    onDragCancel = {
                        dragging = false
                        scope.launch { indicator.animateTo(if (currentLayer != FunctionLayer.Off) segment else 0f, settle) }
                    },
                )
            },
    ) {
        Box(
            Modifier
                .offset { IntOffset(indicator.value.roundToInt(), 0) }
                .width(SegmentWidth)
                .fillMaxHeight()
                .padding(3.dp)
                .background(indicatorColor, CircleShape),
        )
        Row {
            Segment(
                label = "123",
                selected = !on,
                color = if (!on) colors.onSurface else colors.onSurfaceVariant,
                onClick = { onLayer(FunctionLayer.Off) },
                onLongClick = null,
            )
            Segment(
                label = "ƒ",
                selected = on,
                color = when {
                    locked -> colors.onPrimary
                    on -> colors.onPrimaryContainer
                    else -> colors.onSurfaceVariant
                },
                // Tap once for one function, again to lock; hold to lock straight away.
                onClick = {
                    onLayer(
                        when (layer) {
                            FunctionLayer.Off -> FunctionLayer.Once
                            FunctionLayer.Once -> FunctionLayer.Locked
                            FunctionLayer.Locked -> FunctionLayer.Off
                        },
                    )
                },
                onLongClick = { onLayer(FunctionLayer.Locked) },
                modifier = Modifier.semantics { contentDescription = description },
                underline = locked,
            )
        }
    }
}

@Composable
private fun Segment(
    label: String,
    selected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    underline: Boolean = false,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(SegmentWidth)
            .fillMaxHeight()
            .clip(CircleShape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick, role = Role.Tab)
            .semantics { this.selected = selected },
    ) {
        Text(label, style = SegmentStyle, color = color)
        if (underline) {
            // Locked, like Caps Lock: a small bar under the ƒ.
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .size(width = 12.dp, height = 2.dp)
                    .background(color, CircleShape),
            )
        }
    }
}
