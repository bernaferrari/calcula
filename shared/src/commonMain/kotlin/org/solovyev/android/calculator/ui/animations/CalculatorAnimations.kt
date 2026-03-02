package org.solovyev.android.calculator.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
// Material icons not available in commonMain - using alternatives
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.*
import kotlin.random.Random


// ============================================
// MATERIAL DESIGN 3 MOTION SYSTEM
// ============================================

/**
 * Material Design 3 standard easing curves.
 * These follow the Material Design motion guidelines for consistent, polished animations.
 */
object MaterialMotion {
    /** Standard easing - for most transitions */
    val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    
    /** Decelerate easing - for elements entering the screen */
    val DecelerateEasing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    
    /** Accelerate easing - for elements exiting the screen */
    val AccelerateEasing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
}

/**
 * Material Design 3 duration guidelines
 */
object MotionDurations {
    const val Short1 = 50
    const val Short2 = 100
    const val Short3 = 150
    const val Short4 = 200
    const val Medium1 = 250
    const val Medium2 = 300
    const val Medium3 = 350
    const val Medium4 = 400
    const val StaggerFast = 25
    const val StaggerMedium = 50
    const val StaggerSlow = 75
}

/**
 * Material Design 3 spring configurations
 */
object MaterialSprings {
    val Gentle = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val Standard = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    val Snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
}

// ============================================
// Animation Specs & Constants (Legacy)
// ============================================

object AnimationSpecs {
    val ButtonPressSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val ButtonReleaseSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    val SuccessPulseSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val ScaleAnimationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val FadeAnimationSpec = tween<Float>(
        durationMillis = 200,
        easing = FastOutSlowInEasing
    )

    val SlideAnimationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val ShakeKeyframes = keyframes<Float> {
        durationMillis = 500
        0f at 0
        -8f at 50
        8f at 100
        -6f at 150
        6f at 200
        -4f at 250
        4f at 300
        -2f at 350
        2f at 400
        0f at 500
    }

    val NumberRollDuration = 600
    val StaggerDelay = 30
    val ConfettiDuration = 2000
}

// ============================================
// Button Animation Modifiers
// ============================================

@Composable
fun Modifier.materialButtonScale(
    isPressed: Boolean,
    reduceMotion: Boolean = false
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = if (reduceMotion) {
            spring(dampingRatio = 1f, stiffness = Float.MAX_VALUE)
        } else {
            if (isPressed) MaterialSprings.Snappy else MaterialSprings.Standard
        },
        label = "buttonScale"
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
fun Modifier.animatedButtonScale(
    isPressed: Boolean,
    reduceMotion: Boolean = false
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = if (reduceMotion) {
            spring(dampingRatio = 1f, stiffness = Float.MAX_VALUE)
        } else {
            if (isPressed) AnimationSpecs.ButtonPressSpring else AnimationSpecs.ButtonReleaseSpring
        },
        label = "buttonScale"
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
fun Modifier.glowEffect(
    enabled: Boolean,
    color: Color,
    intensity: Dp = 8.dp,
    reduceMotion: Boolean = false
): Modifier {
    if (!enabled || reduceMotion) return this

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    return this.drawBehind {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = glowAlpha),
                    color.copy(alpha = 0f)
                ),
                center = center,
                radius = (size.width * 0.8f).coerceAtLeast(0.1f)
            ),
            size = size
        )
    }
}

@Composable
fun Modifier.successPulse(
    trigger: Boolean,
    reduceMotion: Boolean = false
): Modifier {
    if (reduceMotion) return this

    var pulseActive by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            pulseActive = true
            delay(600)
            pulseActive = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (pulseActive) 1.08f else 1f,
        animationSpec = AnimationSpecs.SuccessPulseSpring,
        label = "successPulse"
    )

    val alpha by animateFloatAsState(
        targetValue = if (pulseActive) 0.8f else 1f,
        animationSpec = tween(300),
        label = "successAlpha"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

/**
 * Material Design 3 breathing animation for gesture discovery.
 * Gentle sine wave that doesn't distract but draws attention.
 */
@Composable
fun Modifier.breathingAnimation(
    enabled: Boolean,
    reduceMotion: Boolean = false
): Modifier {
    if (!enabled || reduceMotion) return this
    
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = MaterialMotion.StandardEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )
    
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Pulsing rings animation for empty states.
 * Elegant, subtle motion that indicates "waiting for content".
 */
@Composable
fun Modifier.pulsingRingsAnimation(
    active: Boolean,
    reduceMotion: Boolean = false
): Modifier {
    if (!active || reduceMotion) return this
    
    val infiniteTransition = rememberInfiniteTransition(label = "rings")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = MaterialMotion.StandardEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )
    
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Swipe-to-delete animation with resistance and snap back.
 * Follows Material Design motion patterns for gesture-based interactions.
 */
@Composable
fun Modifier.swipeToDeleteAnimation(
    offsetX: Float,
    maxOffset: Float = 200f,
    reduceMotion: Boolean = false
): Modifier {
    val resistance = 0.6f
    val adjustedOffset = (offsetX * resistance).coerceIn(-maxOffset, maxOffset)
    
    val scale by animateFloatAsState(
        targetValue = if (kotlin.math.abs(offsetX) > 10) 0.98f else 1f,
        animationSpec = MaterialSprings.Snappy,
        label = "swipeScale"
    )
    
    return this.graphicsLayer {
        translationX = adjustedOffset
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Staggered list item entrance animation.
 * Creates a cascading effect as items appear.
 */
@Composable
fun Modifier.staggeredItemAnimation(
    index: Int,
    visible: Boolean = true,
    reduceMotion: Boolean = false
): Modifier {
    val delay = index * MotionDurations.StaggerMedium
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = MotionDurations.Medium1,
            delayMillis = if (visible) delay else 0,
            easing = MaterialMotion.DecelerateEasing
        ),
        label = "itemAlpha"
    )
    
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
        animationSpec = if (reduceMotion) {
            spring(dampingRatio = 1f, stiffness = Float.MAX_VALUE)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "itemOffset"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "itemScale"
    )
    
    return this.graphicsLayer {
        this.alpha = alpha
        translationY = offsetY
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun Modifier.rippleEffect(
    rippleCenter: Offset,
    rippleColor: Color,
    isActive: Boolean,
    reduceMotion: Boolean = false
): Modifier {
    if (!isActive || reduceMotion) return this

    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleProgress"
    )

    return this.drawBehind {
        val maxRadius = size.maxDimension * 1.5f
        val currentRadius = maxRadius * rippleProgress
        val alpha = (1f - rippleProgress) * 0.3f

        drawCircle(
            color = rippleColor.copy(alpha = alpha),
            radius = currentRadius,
            center = rippleCenter
        )
    }
}

// ============================================
// Display Animations
// ============================================

@Composable
fun NumberRollAnimation(
    value: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = MaterialTheme.colorScheme.primary,
    reduceMotion: Boolean = false
) {
    val density = LocalDensity.current
    val digits = remember(value) { value.toCharArray().toList() }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        digits.forEachIndexed { index, char ->
            val delay = index * AnimationSpecs.StaggerDelay

            val offsetY by animateFloatAsState(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "numberRoll$index"
            )

            val alpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = delay
                ),
                label = "numberAlpha$index"
            )

            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "numberScale$index"
            )

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        if (!reduceMotion) {
                            translationY = offsetY * 20f
                            this.alpha = alpha
                            scaleX = scale
                            scaleY = scale
                        }
                    }
            ) {
                if (char.isDigit()) {
                    AnimatedDigit(
                        digit = char,
                        targetValue = char.digitToInt(),
                        style = style,
                        color = color,
                        reduceMotion = reduceMotion
                    )
                } else {
                    Text(
                        text = char.toString(),
                        style = style,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedDigit(
    digit: Char,
    targetValue: Int,
    style: TextStyle,
    color: Color,
    reduceMotion: Boolean
) {
    if (reduceMotion) {
        Text(
            text = digit.toString(),
            style = style,
            color = color,
            fontWeight = FontWeight.Bold
        )
        return
    }

    val scope = rememberCoroutineScope()
    var displayValue by remember { mutableStateOf(0) }

    LaunchedEffect(targetValue) {
        scope.launch {
            val steps = (0..targetValue).toList()
            steps.forEach { value ->
                displayValue = value
                delay(40)
            }
        }
    }

    Text(
        text = displayValue.toString(),
        style = style,
        color = color,
        fontWeight = FontWeight.Bold
    )
}

/**
 * Elegant error shake animation - gentle and refined, not violent.
 * Uses spring physics for organic, natural feel.
 */
@Composable
fun Modifier.elegantErrorShake(
    trigger: Boolean,
    reduceMotion: Boolean = false
): Modifier {
    if (reduceMotion) return this

    var shakeActive by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            shakeActive = true
            delay(600)
            shakeActive = false
        }
    }

    val offsetX by animateFloatAsState(
        targetValue = if (shakeActive) 1f else 0f,
        animationSpec = if (shakeActive) {
            keyframes {
                durationMillis = 600
                // Gentle, elegant oscillations
                0f at 0 using FastOutSlowInEasing
                -6f at 80 using FastOutSlowInEasing
                5f at 160 using FastOutSlowInEasing
                -3f at 240 using FastOutSlowInEasing
                2f at 320 using FastOutSlowInEasing
                -1f at 400 using FastOutSlowInEasing
                0f at 600 using FastOutSlowInEasing
            }
        } else {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        },
        label = "elegantShake"
    )

    return this.offset(x = offsetX.dp)
}

@Composable
fun Modifier.shakeAnimation(
    trigger: Boolean,
    reduceMotion: Boolean = false
): Modifier {
    if (reduceMotion) return this

    var shakeActive by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            shakeActive = true
            delay(500)
            shakeActive = false
        }
    }

    val offsetX by animateFloatAsState(
        targetValue = if (shakeActive) 1f else 0f,
        animationSpec = if (shakeActive) {
            AnimationSpecs.ShakeKeyframes
        } else {
            tween(100)
        },
        label = "shake"
    )

    return this.offset(x = offsetX.dp)
}

// ============================================
// AWARD-WINNING MICRO-INTERACTIONS
// ============================================

/**
 * Hero moment animation for equals button.
 * Radial glow burst + subtle screen flash + result pop.
 */
@Composable
fun Modifier.heroEqualsAnimation(
    trigger: Boolean,
    primaryColor: Color,
    reduceMotion: Boolean = false
): Modifier {
    if (reduceMotion) return this

    var heroActive by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            heroActive = true
            delay(800)
            heroActive = false
        }
    }

    val glowScale by animateFloatAsState(
        targetValue = if (heroActive) 1.5f else 0f,
        animationSpec = if (heroActive) {
            keyframes {
                durationMillis = 600
                0f at 0 using FastOutSlowInEasing
                1.2f at 150 using FastOutSlowInEasing
                1.5f at 300 using FastOutSlowInEasing
                1.3f at 450 using LinearEasing
                0f at 600 using FastOutSlowInEasing
            }
        } else {
            tween(200)
        },
        label = "heroGlowScale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (heroActive) 0.6f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "heroGlowAlpha"
    )

    val flashAlpha by animateFloatAsState(
        targetValue = if (heroActive) 0.15f else 0f,
        animationSpec = keyframes {
            durationMillis = 400
            0f at 0
            0.15f at 100
            0.08f at 200
            0f at 400
        },
        label = "screenFlash"
    )

    return this.drawBehind {
        if (glowScale > 0f && glowAlpha > 0f) {
            // Radial glow burst from center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = glowAlpha),
                        primaryColor.copy(alpha = glowAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = (size.maxDimension * glowScale * 0.5f).coerceAtLeast(0.1f)
                ),
                center = center
            )
        }

        // Subtle screen flash overlay
        if (flashAlpha > 0f) {
            drawRect(
                color = primaryColor.copy(alpha = flashAlpha),
                size = size
            )
        }
    }
}

/**
 * Result "pop" animation - scales up with bounce then settles.
 */
@Composable
fun Modifier.resultPopAnimation(
    trigger: Boolean,
    reduceMotion: Boolean = false
): Modifier {
    if (reduceMotion) return this

    var popActive by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            popActive = true
            delay(500)
            popActive = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (popActive) 1f else 1f,
        animationSpec = if (popActive) {
            spring(
                dampingRatio = 0.6f,
                stiffness = 300f
            )
        } else {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        },
        label = "resultPop"
    )

    val initialScale by remember { mutableStateOf(0.85f) }

    LaunchedEffect(popActive) {
        // Reset for next animation
    }

    return this.graphicsLayer {
        if (popActive) {
            // Start from smaller scale and pop up
            val animatedScale = if (popActive) scale else initialScale
            scaleX = animatedScale
            scaleY = animatedScale
        }
    }
}

/**
 * Animated clear - numbers fade out left-to-right with stagger.
 */
@Composable
fun AnimatedClearText(
    text: String,
    isClearing: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val characters = remember(text) { text.toList() }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {
        characters.forEachIndexed { index, char ->
            val delay = index * 30 // Stagger left-to-right

            val alpha by animateFloatAsState(
                targetValue = if (isClearing) 0f else 1f,
                animationSpec = tween(
                    durationMillis = 200,
                    delayMillis = if (isClearing) delay else 0,
                    easing = FastOutSlowInEasing
                ),
                label = "clearAlpha$index"
            )

            val offsetX by animateFloatAsState(
                targetValue = if (isClearing) -30f else 0f,
                animationSpec = tween(
                    durationMillis = 250,
                    delayMillis = if (isClearing) delay else 0,
                    easing = FastOutSlowInEasing
                ),
                label = "clearOffset$index"
            )

            Text(
                text = char.toString(),
                style = style,
                color = color,
                modifier = Modifier.graphicsLayer {
                    this.alpha = alpha
                    translationX = offsetX
                }
            )
        }
    }
}

/**
 * Number input animation - each digit slides up from bottom with scale.
 */
@Composable
fun AnimatedDigitInput(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    reduceMotion: Boolean = false
) {
    val density = LocalDensity.current
    var previousText by remember { mutableStateOf(text) }
    var newChars by remember { mutableStateOf<List<Pair<Char, Int>>>(emptyList()) }

    // Track newly added characters
    LaunchedEffect(text) {
        if (text.length > previousText.length && text.startsWith(previousText)) {
            // Characters were added at the end
            val added = text.substring(previousText.length)
            val startIndex = previousText.length
            newChars = added.mapIndexed { i, c -> c to (startIndex + i) }
        }
        previousText = text
    }

    // Clear animation state after animation completes
    LaunchedEffect(newChars) {
        if (newChars.isNotEmpty()) {
            delay(400)
            newChars = emptyList()
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {
        text.forEachIndexed { index, char ->
            val isNew = newChars.any { it.second == index }

            val offsetY by animateFloatAsState(
                targetValue = 0f,
                animationSpec = if (isNew && !reduceMotion) {
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = 400f,
                        visibilityThreshold = 0.01f
                    )
                } else {
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                },
                label = "digitOffset$index"
            )

            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = if (isNew && !reduceMotion) {
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = 400f
                    )
                } else {
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                },
                label = "digitScale$index"
            )

            // Initial values for new characters
            val initialOffset = with(density) { 10.dp.toPx() }
            val initialScale = 0.9f

            Box(
                modifier = Modifier.graphicsLayer {
                    if (isNew) {
                        translationY = initialOffset * (1f - (offsetY / initialOffset).coerceIn(0f, 1f))
                        scaleX = initialScale + (1f - initialScale) * scale
                        scaleY = initialScale + (1f - initialScale) * scale
                    }
                }
            ) {
                Text(
                    text = char.toString(),
                    style = style,
                    color = color
                )
            }
        }
    }
}

/**
 * Multi-sensory copy feedback with ripple, flying text, and checkmark.
 */
@Composable
fun MultiSensoryCopyFeedback(
    visible: Boolean,
    touchPosition: Offset = Offset.Zero,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    var rippleProgress by remember { mutableFloatStateOf(0f) }
    var textOffset by remember { mutableFloatStateOf(0f) }
    var checkmarkProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            // Reset and animate
            rippleProgress = 0f
            textOffset = 0f
            checkmarkProgress = 0f

            // Staggered animation sequence
            launch {
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) { value, _ -> rippleProgress = value }
            }

            launch {
                delay(100)
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = 300f
                    )
                ) { value, _ -> textOffset = value }
            }

            launch {
                delay(200)
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) { value, _ -> checkmarkProgress = value }
            }
        }
    }

    if (!visible && rippleProgress == 0f) return

    Box(modifier = modifier) {
        // Ripple from touch point
        val primaryColor = MaterialTheme.colorScheme.primary
        if (rippleProgress > 0f && !reduceMotion) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxRadius = size.maxDimension * 0.8f
                val currentRadius = maxRadius * rippleProgress
                val alpha = (1f - rippleProgress) * 0.3f

                drawCircle(
                    color = primaryColor.copy(alpha = alpha),
                    radius = currentRadius,
                    center = if (touchPosition == Offset.Zero) center else touchPosition
                )
            }
        }

        // Flying "Copied" text
        if (textOffset > 0f) {
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        if (!reduceMotion) {
                            translationY = -50.dp.toPx() * textOffset
                            alpha = if (textOffset < 0.8f) textOffset else 1f - (textOffset - 0.8f) * 5f
                        }
                    }
            ) {
                Text(
                    text = "Copied",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Animated checkmark (SVG-style path drawing)
        if (checkmarkProgress > 0f) {
            Box(
                modifier = Modifier.align(Alignment.Center)
            ) {
                AnimatedCheckmark(
                    progress = checkmarkProgress,
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

/**
 * SVG-style animated checkmark that draws itself.
 */
@Composable
private fun AnimatedCheckmark(
    progress: Float,
    color: Color,
    strokeWidth: Dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val strokePx = with(density) { strokeWidth.toPx() }

    Canvas(modifier = modifier.size(48.dp)) {
        val path = Path().apply {
            // Checkmark path: short diagonal then long diagonal
            val startX = size.width * 0.2f
            val startY = size.height * 0.5f
            val midX = size.width * 0.4f
            val midY = size.height * 0.7f
            val endX = size.width * 0.8f
            val endY = size.height * 0.3f

            moveTo(startX, startY)
            lineTo(midX, midY)
            lineTo(endX, endY)
        }

        // Calculate path length for trimming
        val pathMeasure = androidx.compose.ui.graphics.PathMeasure().apply {
            setPath(path, false)
        }
        val pathLength = pathMeasure.length

        // Draw the animated path
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokePx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            alpha = progress
        )
    }
}

/**
 * Error state with gentle glow and fade-in.
 */
@Composable
fun ElegantErrorDisplay(
    message: String,
    suggestion: String? = null,
    visible: Boolean = true,
    onSuggestionClick: () -> Unit = {},
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "errorAlpha"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (visible) 0.15f else 0f,
        animationSpec = tween(400),
        label = "errorGlow"
    )

    Box(
        modifier = modifier
            .alpha(alpha)
            .drawBehind {
                if (glowAlpha > 0f) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE53935).copy(alpha = glowAlpha),
                                Color.Transparent
                            ),
                            center = center,
                            radius = (size.maxDimension * 0.6f).coerceAtLeast(0.1f)
                        )
                    )
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )

            if (suggestion != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onSuggestionClick) {
                    Text(
                        text = "Did you mean: $suggestion?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Theme change crossfade with smooth color interpolation.
 */
@Composable
fun SmoothThemeCrossfade(
    targetTheme: Int, // Theme identifier
    content: @Composable () -> Unit
) {
    var currentTheme by remember { mutableIntStateOf(targetTheme) }
    var isTransitioning by remember { mutableStateOf(false) }

    LaunchedEffect(targetTheme) {
        if (targetTheme != currentTheme) {
            isTransitioning = true
            delay(300) // Allow crossfade to complete
            currentTheme = targetTheme
            isTransitioning = false
        }
    }

    Crossfade(
        targetState = currentTheme,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "themeCrossfade"
    ) { _ ->
        content()
    }
}

@Composable
fun Modifier.pulseGlow(
    color: Color,
    trigger: Boolean,
    reduceMotion: Boolean = false
): Modifier {
    if (reduceMotion) return this

    var pulseActive by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            pulseActive = true
            delay(800)
            pulseActive = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (pulseActive) 0.6f else 0f,
        animationSpec = tween(400),
        label = "glowAlpha"
    )

    return this.drawBehind {
        if (pulseActive) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = glowAlpha * glowScale),
                        color.copy(alpha = 0f)
                    ),
                    center = center,
                    radius = (size.maxDimension * glowScale).coerceAtLeast(0.1f)
                )
            )
        }
    }
}

// ============================================
// Character Reveal Animation
// ============================================

@Composable
fun CharacterRevealText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    charDelayMillis: Int = 30,
    reduceMotion: Boolean = false
) {
    val visibleChars = remember { mutableStateListOf<Boolean>() }

    LaunchedEffect(text) {
        visibleChars.clear()
        visibleChars.addAll(List(text.length) { false })

        text.indices.forEach { index ->
            delay(charDelayMillis.toLong())
            if (index < visibleChars.size) {
                visibleChars[index] = true
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {
        text.forEachIndexed { index, char ->
            AnimatedVisibility(
                visible = visibleChars.getOrElse(index) { true } || reduceMotion,
                enter = fadeIn(tween(100)) + slideInVertically { it / 2 },
                exit = fadeOut()
            ) {
                Text(
                    text = char.toString(),
                    style = style,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================
// Micro-interactions
// ============================================

@Composable
fun MorphingButton(
    isSuccess: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    successContent: @Composable () -> Unit
) {
    val density = LocalDensity.current

    val sizeProgress by animateFloatAsState(
        targetValue = if (isSuccess) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "morphSize"
    )

    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSuccess) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.primaryContainer
            )
            .combinedClickableWithRipple(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isSuccess,
            transitionSpec = {
                scaleIn(tween(200)) + fadeIn(tween(150)) togetherWith
                scaleOut(tween(150)) + fadeOut(tween(100))
            },
            label = "morphContent"
        ) { success ->
            if (success) {
                successContent()
            } else {
                content()
            }
        }
    }
}

@Composable
fun CopyFeedbackButton(
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    MorphingButton(
        isSuccess = isCopied,
        onClick = {
            onCopy()
            isCopied = true
            scope.launch {
                delay(1500)
                isCopied = false
            }
        },
        modifier = modifier,
        content = {
            // Placeholder for ContentCopy icon
            Text(
                stringResource(Res.string.cpp_copy),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        successContent = {
            // Placeholder for Check icon
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    )
}

// ============================================
// Transition Animations
// ============================================

object CalculatorTransitions {
    // Material Design 3 refined transitions
    val SlideInFromRight: EnterTransition = slideInHorizontally(
        animationSpec = tween(MotionDurations.Medium2, easing = MaterialMotion.DecelerateEasing),
        initialOffsetX = { it }
    ) + fadeIn(animationSpec = tween(MotionDurations.Medium1, easing = MaterialMotion.DecelerateEasing))

    val SlideOutToRight: ExitTransition = slideOutHorizontally(
        animationSpec = tween(MotionDurations.Medium1, easing = MaterialMotion.AccelerateEasing),
        targetOffsetX = { it }
    ) + fadeOut(animationSpec = tween(MotionDurations.Short3, easing = MaterialMotion.AccelerateEasing))

    val SlideInFromBottom: EnterTransition = slideInVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        initialOffsetY = { it }
    ) + fadeIn(animationSpec = tween(MotionDurations.Medium2, easing = MaterialMotion.DecelerateEasing))

    val SlideOutToBottom: ExitTransition = slideOutVertically(
        animationSpec = tween(MotionDurations.Medium1, easing = MaterialMotion.AccelerateEasing),
        targetOffsetY = { it }
    ) + fadeOut(animationSpec = tween(MotionDurations.Short3, easing = MaterialMotion.AccelerateEasing))

    val ScaleIn: EnterTransition = scaleIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        initialScale = 0.85f
    ) + fadeIn(animationSpec = tween(MotionDurations.Medium1, easing = MaterialMotion.DecelerateEasing))

    val ScaleOut: ExitTransition = scaleOut(
        animationSpec = tween(MotionDurations.Short3, easing = MaterialMotion.AccelerateEasing),
        targetScale = 0.9f
    ) + fadeOut(animationSpec = tween(MotionDurations.Short2, easing = MaterialMotion.AccelerateEasing))
    
    val FadeIn: EnterTransition = fadeIn(
        animationSpec = tween(MotionDurations.Medium1, easing = MaterialMotion.DecelerateEasing)
    )
    
    val FadeOut: ExitTransition = fadeOut(
        animationSpec = tween(MotionDurations.Short3, easing = MaterialMotion.AccelerateEasing)
    )
}

@Composable
fun <T> StaggeredListAnimation(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    val visibleItems = remember { mutableStateListOf<Boolean>() }

    LaunchedEffect(items) {
        visibleItems.clear()
        visibleItems.addAll(List(items.size) { false })

        items.indices.forEach { index ->
            delay((index * 50).toLong())
            if (index < visibleItems.size) {
                visibleItems[index] = true
            }
        }
    }

    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            AnimatedVisibility(
                visible = visibleItems.getOrElse(index) { true },
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    initialOffsetY = { it / 2 }
                ) + fadeIn(tween(200)),
                exit = slideOutVertically() + fadeOut()
            ) {
                content(item)
            }
        }
    }
}

// ============================================
// Celebration Animations
// ============================================

@Composable
fun ConfettiBurst(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    reduceMotion: Boolean = false
) {
    if (reduceMotion) return

    var isActive by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger) {
            isActive = true
            delay(AnimationSpecs.ConfettiDuration.toLong())
            isActive = false
        }
    }

    if (!isActive) return

    val particles = remember(particleCount) {
        List(particleCount) { ConfettiParticle.random() }
    }

    val progress = rememberInfiniteTransition(label = "confetti").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(AnimationSpecs.ConfettiDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    // Canvas animation disabled - complex draw operations not fully supported in commonMain
    Box(modifier = modifier) {
        // Placeholder for confetti animation
    }
}

private data class ConfettiParticle(
    val color: Color,
    val size: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val wobble: Float,
    val delay: Float
) {
    companion object {
        fun random(): ConfettiParticle {
            val colors = listOf(
                Color(0xFFE91E63), // Pink
                Color(0xFF9C27B0), // Purple
                Color(0xFF673AB7), // Deep Purple
                Color(0xFF3F51B5), // Indigo
                Color(0xFF2196F3), // Blue
                Color(0xFF03A9F4), // Light Blue
                Color(0xFF00BCD4), // Cyan
                Color(0xFF009688), // Teal
                Color(0xFF4CAF50), // Green
                Color(0xFFFFEB3B), // Yellow
                Color(0xFFFF9800), // Orange
                Color(0xFFFF5722)  // Deep Orange
            )

            return ConfettiParticle(
                color = colors.random(),
                size = Random.nextFloat() * 8f + 4f,
                velocityX = (Random.nextFloat() - 0.5f) * 2f,
                velocityY = Random.nextFloat() * 0.8f + 0.2f,
                rotation = Random.nextFloat() * 720f - 360f,
                wobble = Random.nextFloat() * 2f - 1f,
                delay = Random.nextFloat() * 0.2f
            )
        }
    }
}

@Composable
fun SparkleEffect(
    modifier: Modifier = Modifier,
    trigger: Boolean = true,
    reduceMotion: Boolean = false
) {
    if (reduceMotion) return

    val sparkles = remember { List(6) { index ->
        SparkleData(
            offset = Offset(
                x = Random.nextFloat() * 0.8f + 0.1f,
                y = Random.nextFloat() * 0.8f + 0.1f
            ),
            delay = index * 100,
            scale = Random.nextFloat() * 0.5f + 0.5f
        )
    }}

    val infiniteTransition = rememberInfiniteTransition(label = "sparkles")

    Box(modifier = modifier) {
        sparkles.forEach { sparkle ->
            val progress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, delayMillis = sparkle.delay),
                    repeatMode = RepeatMode.Restart
                ),
                label = "sparkle${sparkle.hashCode()}"
            )

            // Canvas animation disabled - using simple Box instead
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(sin(progress * PI).toFloat())
            )
        }
    }
}

private data class SparkleData(
    val offset: Offset,
    val delay: Int,
    val scale: Float
)

// ============================================
// Spring-Based Drag Panel (History Panel)
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha))
    )
}

@Composable
fun CalculatingIndicator(
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Calculating",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AnimatedDots(reduceMotion = reduceMotion)
    }
}

@Composable
private fun AnimatedDots(
    reduceMotion: Boolean = false
) {
    val dots = listOf(0, 1, 2)

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        dots.forEachIndexed { index, _ ->
            val delay = index * 150

            val offsetY by animateFloatAsState(
                targetValue = if (reduceMotion) 0f else -4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )

            Box(
                modifier = Modifier
                    .size(4.dp)
                    .offset(y = if (reduceMotion) 0.dp else offsetY.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun CircularProgressIndicatorWithPulse(
    progress: Float,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val pulseScale by rememberInfiniteTransition(label = "progressPulse").animateFloat(
        initialValue = 1f,
        targetValue = if (reduceMotion) 1f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = if (!reduceMotion) Modifier.scale(pulseScale) else Modifier,
            strokeWidth = 4.dp,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )

        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ============================================
// Scroll Animations
// ============================================

@Composable
fun Modifier.parallaxScroll(
    scrollProgress: Float,
    parallaxFactor: Float = 0.3f,
    reduceMotion: Boolean = false
): Modifier {
    if (reduceMotion) return this

    return this.graphicsLayer {
        translationY = scrollProgress * parallaxFactor * 100f
    }
}

@Composable
fun Modifier.fadeEdges(
    isTop: Boolean = true,
    isBottom: Boolean = true,
    fadeHeight: Dp = 20.dp
): Modifier {
    return this.drawWithContent {
        drawContent()

        val fadeHeightPx = fadeHeight.toPx()

        if (isTop) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.1f)
                    ),
                    startY = 0f,
                    endY = fadeHeightPx
                ),
                blendMode = BlendMode.DstIn
            )
        }

        if (isBottom) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    startY = size.height - fadeHeightPx,
                    endY = size.height
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }
}

// ============================================
// Utility Composables
// ============================================

@Composable
fun AnimatedHeight(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeOut(),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun SlideUpAppear(
    delayMillis: Int = 0,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300, delayMillis = delayMillis),
        label = "slideUpAlpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = if (reduceMotion) {
            tween(100, delayMillis = delayMillis)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
                visibilityThreshold = 0.01f
            )
        },
        label = "slideUpOffset"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                translationY = if (reduceMotion) 0f else offsetY * 50f
            }
    ) {
        content()
    }
}

// ============================================
// Clickable with Ripple (simplified version)
// ============================================

fun Modifier.combinedClickableWithRipple(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null
): Modifier = this.clickable(onClick = onClick) // Simplified - ignores long/double click for now

// ============================================
// Demo / Usage Examples
// ============================================

/**
 * Example demonstrating how to use celebration animations together.
 * This can be used for achievements, tutorial completion, or calculation streaks.
 */
@Composable
fun CelebrationDemo(
    trigger: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalCalculatorReduceMotion.current

    Box(modifier = modifier.fillMaxSize()) {
        // Confetti burst in background
        ConfettiBurst(
            trigger = trigger,
            modifier = Modifier.fillMaxSize(),
            particleCount = if (reduceMotion) 0 else 60,
            reduceMotion = reduceMotion
        )

        // Sparkle effect overlay
        SparkleEffect(
            modifier = Modifier.fillMaxSize(),
            trigger = trigger,
            reduceMotion = reduceMotion
        )

        // Achievement toast
        AchievementToast(
            title = "Calculation Streak!",
            description = "You've solved 10 problems correctly in a row",
            icon = {
                // Placeholder for Check icon
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            },
            visible = trigger,
            onDismiss = onComplete,
            modifier = Modifier.align(Alignment.TopCenter),
            reduceMotion = reduceMotion
        )
    }
}

/**
 * Demo of loading states for history or calculation screens.
 */
@Composable
fun LoadingStatesDemo(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalCalculatorReduceMotion.current

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Skeleton loader for history items
        if (isLoading) {
            repeat(3) { index ->
                SlideUpAppear(
                    delayMillis = index * 100,
                    reduceMotion = reduceMotion
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonLoader(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SkeletonLoader(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            SkeletonLoader(
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        } else {
            CalculatingIndicator(reduceMotion = reduceMotion)
        }
    }
}

// ============================================
// Achievement Toast
// ============================================

@Composable
fun AchievementToast(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(visible) {
        if (visible) {
            delay(3000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = if (reduceMotion) {
            fadeIn()
        } else {
            slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                initialOffsetY = { -it }
            ) + fadeIn(tween(200))
        },
        exit = if (reduceMotion) {
            fadeOut()
        } else {
            slideOutVertically(
                animationSpec = tween(200),
                targetOffsetY = { -it }
            ) + fadeOut(tween(150))
        },
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.secondary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ============================================
// Material Design 3 Utility Composables
// ============================================

/**
 * Elegant copy feedback toast with scale + fade animation.
 * Follows Material Design 3 snackbar patterns.
 */
@Composable
fun CopyFeedbackToast(
    visible: Boolean,
    message: String = "Copied to clipboard",
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    AnimatedVisibility(
        visible = visible,
        enter = if (reduceMotion) {
            fadeIn(tween(MotionDurations.Short3))
        } else {
            scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialScale = 0.85f
            ) + fadeIn(tween(MotionDurations.Medium1, easing = MaterialMotion.DecelerateEasing)) +
            slideInVertically { it / 2 }
        },
        exit = if (reduceMotion) {
            fadeOut(tween(MotionDurations.Short2))
        } else {
            scaleOut(
                animationSpec = tween(MotionDurations.Short3, easing = MaterialMotion.AccelerateEasing),
                targetScale = 0.9f
            ) + fadeOut(tween(MotionDurations.Short2, easing = MaterialMotion.AccelerateEasing))
        },
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.inverseSurface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.inverseOnSurface
                )
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Long-press popup animation - scales from bottom center.
 * Used for button long-press option menus.
 */
@Composable
fun LongPressPopupAnimation(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialScale = 0.5f,
            transformOrigin = TransformOrigin(0.5f, 1f)
        ) + fadeIn(tween(MotionDurations.Short3)),
        exit = scaleOut(
            animationSpec = tween(MotionDurations.Short2),
            targetScale = 0.9f
        ) + fadeOut(tween(MotionDurations.Short2))
    ) {
        content()
    }
}
