package org.solovyev.android.calculator.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
// Material icons not available in commonMain - using alternatives
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
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
import kotlin.math.*
import kotlin.random.Random


// ============================================
// Animation Specs & Constants
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
                radius = size.width * 0.8f
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
                    radius = size.maxDimension * glowScale
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
            Text("Copy", color = MaterialTheme.colorScheme.onPrimaryContainer)
        },
        successContent = {
            // Placeholder for Check icon
            Text("✓", color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    )
}

// ============================================
// Transition Animations
// ============================================

object CalculatorTransitions {
    val SlideInFromRight: EnterTransition = slideInHorizontally(
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        initialOffsetX = { it }
    ) + fadeIn(animationSpec = tween(250))

    val SlideOutToRight: ExitTransition = slideOutHorizontally(
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        targetOffsetX = { it }
    ) + fadeOut(animationSpec = tween(200))

    val SlideInFromBottom: EnterTransition = slideInVertically(
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        initialOffsetY = { it }
    ) + fadeIn(animationSpec = tween(250))

    val SlideOutToBottom: ExitTransition = slideOutVertically(
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        targetOffsetY = { it }
    ) + fadeOut(animationSpec = tween(200))

    val ScaleIn = scaleIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        initialScale = 0.8f
    ) + fadeIn(animationSpec = tween(200))

    val ScaleOut = scaleOut(
        animationSpec = tween(150),
        targetScale = 0.8f
    ) + fadeOut(animationSpec = tween(150))
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
// Loading States
// ============================================

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
                Text("✓", color = MaterialTheme.colorScheme.onSecondary)
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
