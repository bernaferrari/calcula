package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

enum class Swipe { Up, Down }

/** Extra width (each side) of the options popup, so a refusing bubble can shake without clipping. */
private val ShakeRoom = 40.dp

/** How far the key's face (label and hints) sits in from its top and bottom edges. */
private val FaceInset = 7.dp

/** A secondary function reachable by flicking a key up or down. */
data class Hint(
    val label: String,
    val description: String = label,
    val enabled: Boolean = true,
    val action: () -> Unit,
)


/**
 * One keypad key: a single label on a softly rounded tile that morphs toward a pill under the finger.
 * Keys with flick shortcuts show two tiny dots (above and below the label); drag up or down and a
 * cookie-shaped bubble pops above the finger showing what will be inserted.
 */
@Composable
fun KeyCell(
    container: Color,
    content: Color,
    description: String,
    haptics: Haptics,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    up: Hint? = null,
    down: Hint? = null,
    showDots: Boolean = true,
    /** Names the flicks in small type in the top and bottom corners, for flicks nobody would guess. */
    labelFlicks: Boolean = false,
    pressHaptic: Boolean = true,
    restRadius: androidx.compose.ui.unit.Dp = com.bernaferrari.calculator.ui.theme.KeyRadius,
    onPressedChange: (Boolean) -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    label: @Composable () -> Unit,
) {
    val reduceMotion by rememberUpdatedState(LocalReduceMotion.current)
    val pressed = remember { Animatable(0f) }
    val drag = remember { Animatable(0f) }
    var selection by remember { mutableStateOf<Swipe?>(null) }
    // Holding a key (without flicking) reveals both of its functions so nothing is a guess.
    var revealed by remember { mutableStateOf(false) }
    var keyWidth by remember { mutableFloatStateOf(0f) }
    var keyHeight by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    val currentUp by rememberUpdatedState(up)
    val currentOnPressedChange by rememberUpdatedState(onPressedChange)
    val currentDown by rememberUpdatedState(down)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            // The key under the finger draws above its neighbours so enlarged hints are never cut off.
            .zIndex(if (pressed.value > 0f || drag.value != 0f) 1f else 0f)
            .semantics {
                role = Role.Button
                contentDescription = description
                onClick { currentOnClick(); true }
                if (onLongClick != null) onLongClick { currentOnLongClick?.invoke(); true }
                val actions = listOfNotNull(
                    up?.takeIf { it.enabled }?.let { CustomAccessibilityAction(it.description) { it.action(); true } },
                    down?.takeIf { it.enabled }?.let { CustomAccessibilityAction(it.description) { it.action(); true } },
                )
                if (actions.isNotEmpty()) customActions = actions
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val first = awaitFirstDown()
                    keyHeight = size.height.toFloat()
                    keyWidth = size.width.toFloat()
                    // A quarter of the key, but never so little that a sloppy tap becomes a swipe.
                    val threshold = maxOf(26.dp.toPx(), size.height * 0.26f)
                    val velocity = VelocityTracker().apply { addPosition(first.uptimeMillis, first.position) }
                    if (pressHaptic) haptics.tap()
                    currentOnPressedChange(true)
                    val grow = scope.launch {
                        pressed.animateTo(1f, if (reduceMotion) snap() else spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium))
                    }
                    var longPressed = false
                    var moved = false
                    val longPress = currentOnLongClick?.let { action ->
                        scope.launch {
                            delay(viewConfiguration.longPressTimeoutMillis)
                            if (!moved) {
                                longPressed = true
                                action()
                            }
                        }
                    }
                    var current: Swipe? = null
                    var last = first.position
                    var cancelled = false
                    val reveal = if (currentUp != null || currentDown != null) {
                        scope.launch {
                            delay(320)
                            if (current == null) {
                                revealed = true
                                haptics.tick()
                            }
                        }
                    } else {
                        null
                    }
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == first.id }
                        if (change == null) {
                            cancelled = true
                            break
                        }
                        velocity.addPosition(change.uptimeMillis, change.position)
                        if (!change.pressed) {
                            change.consume()
                            break
                        }
                        last = change.position
                        val dy = last.y - first.position.y
                        if ((last - first.position).getDistance() > viewConfiguration.touchSlop) {
                            moved = true
                            longPress?.cancel()
                        }
                        // Hysteresis: once a function is picked, it takes a clear move back to let go of it.
                        val keep = threshold * 0.6f
                        val next = when {
                            current == Swipe.Up && dy < -keep -> Swipe.Up
                            current == Swipe.Down && dy > keep -> Swipe.Down
                            dy < -threshold && currentUp != null -> Swipe.Up
                            dy > threshold && currentDown != null -> Swipe.Down
                            else -> null
                        }
                        if (next != current) {
                            current = next
                            selection = next
                            // Heading for a function that can't act yet (1/x with nothing typed) says no
                            // the way a wrong entry does: the error buzz, while its bubble shakes.
                            val hint = if (next == Swipe.Up) currentUp else if (next == Swipe.Down) currentDown else null
                            if (hint != null) { if (hint.enabled) haptics.tick() else haptics.reject() }
                        }
                        scope.launch { drag.snapTo(dy) }
                        change.consume()
                    }
                    // A quick flick counts even if it ended before reaching the threshold.
                    if (current == null && !cancelled && !longPressed) {
                        val vy = velocity.calculateVelocity().y
                        val dy = last.y - first.position.y
                        val minimum = 10.dp.toPx()
                        val fast = 900.dp.toPx()
                        if (vy < -fast && dy < -minimum && currentUp != null) current = Swipe.Up
                        if (vy > fast && dy > minimum && currentDown != null) current = Swipe.Down
                    }
                    longPress?.cancel()
                    reveal?.cancel()
                    grow.cancel()
                    val wasRevealed = revealed
                    revealed = false
                    selection = null
                    currentOnPressedChange(false)
                    scope.launch {
                        if (pressed.value < 0.5f) pressed.snapTo(0.5f)
                        pressed.animateTo(0f, if (reduceMotion) snap() else tween(durationMillis = 260))
                    }
                    scope.launch { drag.animateTo(0f, if (reduceMotion) snap() else spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)) }
                    // Sliding off the key without picking a function cancels, like a real button.
                    val margin = 12.dp.toPx()
                    val inside = last.x in -margin..size.width + margin && last.y in -margin..size.height + margin
                    when {
                        cancelled || longPressed -> Unit
                        // A function that would do nothing here is shown dimmed and picks nothing.
                        current == Swipe.Up -> currentUp?.takeIf { it.enabled }?.action?.invoke()
                        current == Swipe.Down -> currentDown?.takeIf { it.enabled }?.action?.invoke()
                        // Revealed the options but chose neither: that's a cancel, not the digit.
                        wasRevealed -> Unit
                        inside -> currentOnClick()
                    }
                }
            }
            .drawBehind {
                // Expressive shape morph: a softly rounded rectangle at rest, a pill under the finger.
                val p = pressed.value.coerceIn(0f, 1.2f)
                val rest = restRadius.toPx()
                val radius = if (reduceMotion) rest else lerp(rest, min(size.width, size.height) / 2f, p.coerceAtMost(1f))
                val tint = if (selection != null) 0.16f else 0.1f * p.coerceAtMost(1f)
                drawRoundRect(androidx.compose.ui.graphics.lerp(container, content, tint), cornerRadius = CornerRadius(radius, radius))
            },
    ) {
        val pull = with(density) { 10.dp.toPx() }
        // Hints sit a fixed distance from the key's top and bottom edges, so every key in the grid
        // reads the same no matter how tall the rows end up.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .matchParentSize()
                .padding(vertical = FaceInset)
                .graphicsLayer {
                    val s = if (reduceMotion) 1f else 1f - 0.06f * pressed.value.coerceIn(0f, 1f)
                    scaleX = s
                    scaleY = s
                    // The face follows the finger a little, like a rubber key.
                    translationY = if (reduceMotion) 0f else (drag.value * 0.3f).coerceIn(-pull, pull)
                },
        ) {
            val mainAlpha by animateFloatAsState(if (selection != null) 0.3f else 1f, label = "main")
            Box(Modifier.graphicsLayer { alpha = mainAlpha }) { label() }
        }
        // Like the dots, the names belong to the key, not the face: they stay put while the face
        // follows the finger.
        Box(Modifier.matchParentSize().padding(vertical = FaceInset)) {
            if (labelFlicks) {
                val small = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, fontFamily = com.bernaferrari.calculator.ui.theme.GoogleSansFlex)
                // Labels name both directions, including inverse functions, so a flick is discoverable.
                up?.let { Text(it.label, style = small, maxLines = 1, color = content.copy(alpha = if (!it.enabled) 0.25f else if (selection == Swipe.Up) 1f else 0.75f), modifier = Modifier.align(Alignment.TopCenter).offset(y = (-5).dp)) }
                down?.let { hint ->
                    Text(hint.label, style = small, maxLines = 1, color = content.copy(alpha = if (!hint.enabled) 0.25f else if (selection == Swipe.Down) 1f else 0.75f), modifier = Modifier.align(Alignment.BottomCenter).offset(y = 5.dp))
                }
            }
        }
        if (showDots && !labelFlicks) {
            // The dots belong to the key, not the face: they stay put while the face follows the
            // finger, so they never slide past the key's edge. Their gap from the edge grows with
            // the key, 9% of its height, 4 to 8dp: clear of the label on the graph's short keys,
            // not crowded into the corner on tall ones.
            val edge = with(density) { (keyHeight * 0.09f).toDp() }.coerceIn(4.dp, 8.dp)
            Box(Modifier.matchParentSize()) {
                if (up != null) FlickDot(selection == Swipe.Up, up.enabled, content, Modifier.align(Alignment.TopCenter).offset(y = edge))
                if (down != null) FlickDot(selection == Swipe.Down, down.enabled, content, Modifier.align(Alignment.BottomCenter).offset(y = -edge))
            }
        }

        val options = remember { MutableTransitionState(false) }
        options.targetState = revealed || selection != null
        if (options.currentState || options.targetState) {
            FunctionOptions(
                visible = options,
                up = up,
                down = down,
                selection = selection,
                keyWidth = keyWidth,
                keyHeight = keyHeight,
            )
        }
    }
}

/**
 * The functions hiding on a key, shown while it is held or flicked: the up function in a pill
 * above the key, the down one below. The one the finger is heading to fills with the accent.
 */
@Composable
private fun FunctionOptions(
    visible: MutableTransitionState<Boolean>,
    up: Hint?,
    down: Hint?,
    selection: Swipe?,
    keyWidth: Float,
    keyHeight: Float,
) {
    val density = LocalDensity.current
    val reduceMotion = LocalReduceMotion.current
    val pill = 52.dp
    val gap = 6.dp
    Popup(alignment = Alignment.Center, properties = PopupProperties(clippingEnabled = false)) {
        AnimatedVisibility(
            visibleState = visible,
            enter = if (reduceMotion) fadeIn(tween(90)) else fadeIn(tween(90)) + scaleIn(spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium), initialScale = 0.7f),
            exit = if (reduceMotion) fadeOut(tween(120)) else fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.9f),
        ) {
            with(density) {
                // The popup is its own window and clips to its size: leave room around the key so a
                // shaking bubble and its shadow never get cut, even on the edge keys.
                Box(Modifier.size(width = keyWidth.toDp().coerceAtLeast(pill) + ShakeRoom * 2, height = keyHeight.toDp() + (pill + gap) * 2)) {
                    if (up != null) OptionPill(up.label, selection == Swipe.Up, up.enabled, Modifier.align(Alignment.TopCenter))
                    if (down != null) OptionPill(down.label, selection == Swipe.Down, down.enabled, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }
}

@Composable
private fun OptionPill(label: String, selected: Boolean, enabled: Boolean, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    // A function with nothing to act on never lights up: it stays a quiet, faded pill, and shakes
    // its head when the finger reaches it.
    val reduceMotion = LocalReduceMotion.current
    val refused = selected && !enabled
    val shake = remember { Animatable(0f) }
    LaunchedEffect(refused, reduceMotion) {
        if (reduceMotion) shake.snapTo(0f)
        else if (refused) shake.animateTo(0f, spring(dampingRatio = 0.2f, stiffness = 900f), initialVelocity = -1600f)
    }
    @Suppress("NAME_SHADOWING") val selected = selected && enabled
    val scale by animateFloatAsState(if (selected && !reduceMotion) 1.12f else 1f, if (reduceMotion) snap() else spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), label = "pill")
    // Waiting options read like tooltips (dark on a light theme, light on a dark one), so they stand
    // out from the pale keys they float over; the one the finger is heading to takes the accent.
    val container by androidx.compose.animation.animateColorAsState(if (selected) colors.primary else colors.onSurface, label = "pillColor")
    val content by androidx.compose.animation.animateColorAsState(if (selected) colors.onPrimary else colors.surface, label = "pillContent")
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = shake.value
                shadowElevation = 6.dp.toPx()
                shape = RoundedCornerShape(26.dp)
                clip = true
            }
            .background(container)
            .graphicsLayer { alpha = if (enabled) 1f else 0.45f }
            .size(height = 52.dp, width = 84.dp),
    ) {
        Text(
            label,
            style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 22.sp, fontWeight = FontWeight.Medium),
            color = if (enabled) content else content.copy(alpha = 0.6f),
            maxLines = 1,
        )
    }
}

/** A hint that the key hides a function in that direction; it swells when that function is picked. */
@Composable
private fun FlickDot(selected: Boolean, enabled: Boolean, color: Color, modifier: Modifier, restAlpha: Float = 0.35f, disabledAlpha: Float = 0.1f) {
    val size by animateFloatAsState(
        if (selected) 7f else 3.5f,
        if (LocalReduceMotion.current) snap() else spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "dot",
    )
    Box(
        modifier
            .size(size.dp)
            // A flick that would do nothing right now (1/x with nothing typed) barely shows its dot.
            .background(color.copy(alpha = if (!enabled) disabledAlpha else if (selected) 1f else restAlpha), androidx.compose.foundation.shape.CircleShape),
    )
}
