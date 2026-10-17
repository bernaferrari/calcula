package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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

/** A secondary function reachable by flicking a key up or down. */
data class Hint(val label: String, val description: String = label, val action: () -> Unit)


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
    /** Names the up flick in small type where its dot would sit, for a flick nobody would guess. */
    labelUp: Boolean = false,
    pressHaptic: Boolean = true,
    restRadius: androidx.compose.ui.unit.Dp = 14.dp,
    onPressedChange: (Boolean) -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    label: @Composable () -> Unit,
) {
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
                    up?.let { CustomAccessibilityAction(it.description) { it.action(); true } },
                    down?.let { CustomAccessibilityAction(it.description) { it.action(); true } },
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
                        pressed.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium))
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
                            if (next != null) haptics.tick()
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
                        pressed.animateTo(0f, tween(durationMillis = 260))
                    }
                    scope.launch { drag.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)) }
                    // Sliding off the key without picking a function cancels, like a real button.
                    val margin = 12.dp.toPx()
                    val inside = last.x in -margin..size.width + margin && last.y in -margin..size.height + margin
                    when {
                        cancelled || longPressed -> Unit
                        current == Swipe.Up -> currentUp?.action?.invoke()
                        current == Swipe.Down -> currentDown?.action?.invoke()
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
                val radius = lerp(rest, min(size.width, size.height) / 2f, p.coerceAtMost(1f))
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
                .padding(vertical = 7.dp)
                .graphicsLayer {
                    val s = 1f - 0.06f * pressed.value.coerceIn(0f, 1f)
                    scaleX = s
                    scaleY = s
                    // The face follows the finger a little, like a rubber key.
                    translationY = (drag.value * 0.3f).coerceIn(-pull, pull)
                },
        ) {
            val mainAlpha by animateFloatAsState(if (selection != null) 0.3f else 1f, label = "main")
            Box(Modifier.graphicsLayer { alpha = mainAlpha }.padding(top = if (labelUp && up != null) 9.dp else 0.dp)) { label() }
            if (labelUp && up != null) {
                Text(
                    up.label,
                    style = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, fontFamily = com.bernaferrari.calculator.ui.theme.GoogleSansFlex),
                    color = content.copy(alpha = if (selection == Swipe.Up) 1f else 0.55f),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 2.dp),
                )
            }
            if (showDots) {
                if (up != null && !labelUp) FlickDot(selection == Swipe.Up, content, Modifier.align(Alignment.TopCenter).padding(top = 3.dp))
                if (down != null) FlickDot(selection == Swipe.Down, content, Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp))
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
    val pill = 52.dp
    val gap = 6.dp
    Popup(alignment = Alignment.Center, properties = PopupProperties(clippingEnabled = false)) {
        AnimatedVisibility(
            visibleState = visible,
            enter = fadeIn(tween(90)) + scaleIn(spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium), initialScale = 0.7f),
            exit = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.9f),
        ) {
            with(density) {
                Box(Modifier.size(width = keyWidth.toDp().coerceAtLeast(pill), height = keyHeight.toDp() + (pill + gap) * 2)) {
                    if (up != null) OptionPill(up.label, selection == Swipe.Up, Modifier.align(Alignment.TopCenter))
                    if (down != null) OptionPill(down.label, selection == Swipe.Down, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }
}

@Composable
private fun OptionPill(label: String, selected: Boolean, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val scale by animateFloatAsState(if (selected) 1.12f else 1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium), label = "pill")
    val container by androidx.compose.animation.animateColorAsState(if (selected) colors.primary else colors.secondaryContainer, label = "pillColor")
    val content = if (selected) colors.onPrimary else colors.onSecondaryContainer
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = 6.dp.toPx()
                shape = RoundedCornerShape(26.dp)
                clip = true
            }
            .background(container)
            .size(height = 52.dp, width = 84.dp),
    ) {
        Text(
            label,
            style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 22.sp, fontWeight = FontWeight.Medium),
            color = content,
            maxLines = 1,
        )
    }
}

/** A hint that the key hides a function in that direction; it swells when that function is picked. */
@Composable
private fun FlickDot(selected: Boolean, color: Color, modifier: Modifier) {
    val size by animateFloatAsState(
        if (selected) 7f else 3.5f,
        spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "dot",
    )
    Box(
        modifier
            .size(size.dp)
            .background(color.copy(alpha = if (selected) 1f else 0.35f), androidx.compose.foundation.shape.CircleShape),
    )
}
