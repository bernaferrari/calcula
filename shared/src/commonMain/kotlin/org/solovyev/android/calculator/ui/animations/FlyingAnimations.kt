package org.solovyev.android.calculator.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Data class representing a flying animation event.
 */
data class FlyingAnimationEvent(
    val text: String,
    val startPosition: Offset,
    val type: FlyingAnimationType
)

/**
 * Types of flying animations for different calculator actions.
 */
enum class FlyingAnimationType {
    FUNCTION,    // When a function is activated via swipe (sin, cos, ln, etc.)
    RESULT,      // When equals is pressed
    CLEAR,       // When clear is pressed
    MEMORY,      // When memory operations are performed
    NUMBER       // When numbers are entered
}

/**
 * CompositionLocal to provide flying animation events.
 * Set this in your root composable to enable flying animations.
 */
val LocalFlyingAnimationHost = staticCompositionLocalOf<(FlyingAnimationEvent) -> Unit> { {} }

/**
 * State holder for managing flying animations.
 * Use this in your screen to collect and display flying animations.
 */
@Composable
fun rememberFlyingAnimationState(): FlyingAnimationState {
    return remember { FlyingAnimationState() }
}

/**
 * State class that manages active flying animations.
 */
class FlyingAnimationState {
    private val _activeAnimations = mutableStateListOf<ActiveFlyingAnimation>()
    val activeAnimations: List<ActiveFlyingAnimation> = _activeAnimations
    private var nextId = 0L

    fun triggerAnimation(event: FlyingAnimationEvent) {
        nextId += 1
        val animation = ActiveFlyingAnimation(
            id = nextId,
            text = event.text,
            startPosition = event.startPosition,
            type = event.type
        )
        _activeAnimations.add(animation)
    }

    fun removeAnimation(id: Long) {
        _activeAnimations.removeAll { it.id == id }
    }
}

/**
 * Represents an active flying animation instance.
 */
data class ActiveFlyingAnimation(
    val id: Long,
    val text: String,
    val startPosition: Offset,
    val type: FlyingAnimationType
)

/**
 * Host composable that renders all flying animations.
 * Place this at the root of your calculator screen, above other content.
 */
@Composable
fun FlyingAnimationHost(
    state: FlyingAnimationState,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        state.activeAnimations.forEach { animation ->
            FlyingTextAnimation(
                text = animation.text,
                startPosition = animation.startPosition,
                type = animation.type,
                reduceMotion = reduceMotion,
                onComplete = { state.removeAnimation(animation.id) }
            )
        }
    }
}

/**
 * The actual flying text animation composable.
 * Redesigned to be subtle, stable, and readable:
 * slight vertical lift + fade.
 */
@Composable
private fun FlyingTextAnimation(
    text: String,
    startPosition: Offset,
    type: FlyingAnimationType,
    reduceMotion: Boolean = false,
    onComplete: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    val (durationMs, flyDistanceDp, peakScale) = when (type) {
        FlyingAnimationType.FUNCTION -> Triple(280, 64f, 1.06f)
        FlyingAnimationType.RESULT -> Triple(320, 72f, 1.08f)
        FlyingAnimationType.CLEAR -> Triple(250, 56f, 1.04f)
        FlyingAnimationType.MEMORY -> Triple(280, 60f, 1.05f)
        FlyingAnimationType.NUMBER -> Triple(220, 44f, 1.03f)
    }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = if (reduceMotion) 140 else durationMs,
                easing = FastOutSlowInEasing
            )
        ) { value, _ ->
            progress = value
        }
        onComplete()
    }

    val eased = FastOutSlowInEasing.transform(progress.coerceIn(0f, 1f))
    val flyDistancePx = with(density) { flyDistanceDp.dp.toPx() }
    val translationY = if (reduceMotion) {
        -(flyDistancePx * 0.35f * progress)
    } else {
        -(flyDistancePx * eased)
    }
    val translationX = 0f
    val scale = if (reduceMotion) 1f else (0.96f + (peakScale - 0.96f) * eased)
    val alpha = (1f - (progress * progress)).coerceIn(0f, 1f)

    val textColor = when (type) {
        FlyingAnimationType.FUNCTION -> MaterialTheme.colorScheme.primary
        FlyingAnimationType.RESULT -> MaterialTheme.colorScheme.tertiary
        FlyingAnimationType.CLEAR -> MaterialTheme.colorScheme.error
        FlyingAnimationType.MEMORY -> MaterialTheme.colorScheme.secondary
        FlyingAnimationType.NUMBER -> MaterialTheme.colorScheme.onSurface
    }
    val fontSize = when (type) {
        FlyingAnimationType.RESULT -> 30.sp
        FlyingAnimationType.FUNCTION -> 14.sp
        else -> 20.sp
    }
    val fontWeight = when (type) {
        FlyingAnimationType.RESULT -> FontWeight.Bold
        FlyingAnimationType.FUNCTION -> FontWeight.SemiBold
        else -> FontWeight.Medium
    }
    val textMeasurer = rememberTextMeasurer()
    val measuredText = remember(text, fontSize, fontWeight) {
        textMeasurer.measure(
            text = text,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        )
    }

    if (alpha <= 0f) return

    Text(
        text = text,
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (startPosition.x - (measuredText.size.width / 2f)).roundToInt(),
                    y = (startPosition.y - (measuredText.size.height / 2f)).roundToInt()
                )
            }
            .graphicsLayer {
                this.alpha = alpha
                this.translationX = translationX
                this.translationY = translationY
                this.scaleX = scale
                this.scaleY = scale
            },
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = textColor
    )
}

/**
 * Helper function to trigger a flying animation from within composables.
 * Uses the LocalFlyingAnimationHost CompositionLocal.
 */
@Composable
fun triggerFlyingAnimation(
    text: String,
    startPosition: Offset,
    type: FlyingAnimationType = FlyingAnimationType.FUNCTION
) {
    val host = LocalFlyingAnimationHost.current
    LaunchedEffect(text, startPosition) {
        host(FlyingAnimationEvent(text, startPosition, type))
    }
}

/**
 * Preview-friendly flying animation trigger that doesn't require a host.
 * Use this for testing or when you don't have a host set up.
 */
@Composable
fun rememberFlyingAnimationTrigger(): (String, Offset, FlyingAnimationType) -> Unit {
    val host = LocalFlyingAnimationHost.current
    return remember { { text, position, type ->
        host(FlyingAnimationEvent(text, position, type))
    } }
}
