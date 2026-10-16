package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// Enhanced flying text data with physics properties
data class FlyingText(
    val text: String,
    val startPosition: Offset,
    val endPosition: Offset,
    val isSpecial: Boolean = false,
    val id: Long = System.currentTimeMillis(),
    val color: Color? = null,
    val physics: PhysicsProperties = PhysicsProperties(),
)

// Physics properties for realistic motion
data class PhysicsProperties(
    val mass: Float = 1f,
    val elasticity: Float = 0.8f,
    val airResistance: Float = 0.02f,
    val gravity: Float = 0.1f,
    val initialVelocity: Offset = Offset.Zero,
    val spinRate: Float = 0f,
)

// Particle system for advanced effects
private data class FlyingParticle(
    val position: Offset,
    val velocity: Offset,
    val life: Float,
    val color: Color,
    val size: Float,
    val rotation: Float = 0f,
    val type: FlyingParticleType = FlyingParticleType.CIRCLE,
)

private enum class FlyingParticleType {
    CIRCLE,
    STAR,
    SPARK,
    TRAIL,
}

// Trail point for motion blur effect
private data class TrailPoint(
    val position: Offset,
    val scale: Float,
    val opacity: Float,
    val rotation: Float,
    val timestamp: Long,
)

@Composable
fun FlyingTextOverlay(
    flyingTexts: List<FlyingText>,
    reduceMotion: Boolean,
    onAnimationEnd: (FlyingText) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .zIndex(1000f),
    ) {
        flyingTexts.forEach { flyingText ->
            key(flyingText.id) {
                UltraAnimatedFlyingText(
                    flyingText = flyingText,
                    reduceMotion = reduceMotion,
                    onAnimationEnd = { onAnimationEnd(flyingText) },
                )
            }
        }
    }
}

@Composable
private fun UltraAnimatedFlyingText(
    flyingText: FlyingText,
    reduceMotion: Boolean,
    onAnimationEnd: () -> Unit,
) {
    val density = LocalDensity.current

    // Core animation states
    val animationProgress = remember { Animatable(0f) }
    val scale = remember { Animatable(0.3f) }
    val opacity = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val glow = remember { Animatable(0f) }

    // Physics-based motion states
    var position by remember { mutableStateOf(flyingText.startPosition) }
    var velocity by remember { mutableStateOf(flyingText.physics.initialVelocity) }

    // Particle system
    var particles by remember { mutableStateOf<List<FlyingParticle>>(emptyList()) }

    // Motion trail for blur effect
    var trail by remember { mutableStateOf<List<TrailPoint>>(emptyList()) }

    // Distortion field for special effects
    var distortionPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(flyingText, reduceMotion) {
        // Initialize advanced particle system
        if (flyingText.isSpecial && !reduceMotion) {
            particles = createInitialParticles(flyingText)
        }

        // Launch all animations in parallel
        launch {
            // Main progress with variable speed based on distance
            val distance = calculateDistance(flyingText.startPosition, flyingText.endPosition)
            val duration = if (reduceMotion) 220 else (350 + (distance * 0.3f)).toInt().coerceIn(400, 600)

            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec =
                    if (reduceMotion) tween(durationMillis = duration, easing = LinearEasing)
                    else tween(durationMillis = duration, easing = createCustomEasing(flyingText)),
            )
            onAnimationEnd()
        }

        launch {
            // Elastic scale with overshoot
            if (!reduceMotion) {
                scale.animateTo(
                    targetValue = 1.5f,
                    animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
                )
            } else {
                scale.animateTo(1f, tween(150))
            }
        }

        launch {
            // Pulsating glow for special characters
            if (flyingText.isSpecial && !reduceMotion) {
                while (animationProgress.value < 1f) {
                    glow.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(200),
                    )
                    glow.animateTo(
                        targetValue = 0.3f,
                        animationSpec = tween(200),
                    )
                }
            }
        }

        launch {
            // Complex opacity with anticipation
            opacity.animateTo(
                targetValue = 1f,
                animationSpec = tween(50, easing = FastOutSlowInEasing),
            )
            kotlinx.coroutines.delay(300)
            opacity.animateTo(
                targetValue = 0f,
                animationSpec = tween(100, easing = LinearEasing),
            )
        }

        launch {
            // 3D rotation for special characters
            if (flyingText.isSpecial && !reduceMotion) {
                rotation.animateTo(
                    targetValue = 720f, // Two full rotations
                    animationSpec =
                        tween(
                            durationMillis = 500,
                            easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f),
                        ),
                )
            } else if (!reduceMotion) {
                // Subtle wobble for regular text
                while (animationProgress.value < 1f) {
                    rotation.animateTo(
                        targetValue = 5f,
                        animationSpec = tween(100),
                    )
                    rotation.animateTo(
                        targetValue = -5f,
                        animationSpec = tween(100),
                    )
                }
            }
        }

        // Physics simulation
        launch {
            val frameTime = 16L // 60 FPS
            while (animationProgress.value < 1f) {
                updatePhysics(flyingText, animationProgress.value)
                if (!reduceMotion) updateParticles(particles) { particles = it }
                val currentPos = if (reduceMotion) {
                    // Straight linear interpolation
                    Offset(
                        lerp(flyingText.startPosition.x, flyingText.endPosition.x, animationProgress.value),
                        lerp(flyingText.startPosition.y, flyingText.endPosition.y, animationProgress.value)
                    )
                } else {
                    calculateAdvancedPosition(flyingText, animationProgress.value, distortionPhase)
                }
                if (!reduceMotion) updateTrail(currentPos, scale.value, opacity.value, rotation.value, trail) { trail = it }
                if (!reduceMotion) distortionPhase += 0.1f
                kotlinx.coroutines.delay(frameTime)
            }
        }
    }

    // Calculate current position with advanced interpolation
    val progress = animationProgress.value
    val currentPosition = calculateAdvancedPosition(flyingText, progress, distortionPhase)

    // Get theme color outside Canvas
    val defaultColor = MaterialTheme.colorScheme.primary
    val trailColor = flyingText.color ?: defaultColor

    // Main container with trail effect
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (!reduceMotion) {
            // Motion trail layer
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                drawMotionTrail(trail, trailColor)
            }
        }

        // Particle effects layer
        if (!reduceMotion && particles.isNotEmpty()) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize(),
            ) {
                drawParticles(particles, opacity.value, glow.value)
            }
        }

        // Main text with effects
        Box(
            modifier =
                Modifier
                    .offset {
                        IntOffset(
                            x = currentPosition.x.toInt(),
                            y = currentPosition.y.toInt(),
                        )
                    },
        ) {
            RenderFlyingText(
                flyingText = flyingText,
                scale = scale.value,
                opacity = opacity.value,
                rotation = rotation.value,
                glow = glow.value,
                distortionPhase = distortionPhase,
            )
        }
    }
}

@Composable
private fun RenderFlyingText(
    flyingText: FlyingText,
    scale: Float,
    opacity: Float,
    rotation: Float,
    glow: Float,
    distortionPhase: Float,
) {
    val textColor =
        when {
            flyingText.color != null -> flyingText.color
            flyingText.isSpecial -> Color(0xFFFFD700)
            flyingText.text == "log" -> MaterialTheme.colorScheme.tertiary
            flyingText.text in listOf("sin", "cos", "tan") -> MaterialTheme.colorScheme.secondary
            flyingText.text in listOf("ln", "√") -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.primary
        }

    // Multiple render passes for advanced effects
    if (flyingText.isSpecial) {
        // Outer energy field
        Box(
            modifier =
                Modifier
                    .graphicsLayer {
                        scaleX = scale * 1.5f
                        scaleY = scale * 1.5f
                        rotationZ = rotation
                        alpha = opacity * glow * 0.3f
                    }.drawBehind {
                        drawEnergyField(
                            color = textColor,
                            phase = distortionPhase,
                            intensity = glow,
                        )
                    },
        ) {
            Text(
                text = flyingText.text,
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                    ),
                modifier = Modifier.blur(12.dp),
            )
        }

        // Middle plasma layer
        Text(
            text = flyingText.text,
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = opacity * 0.6f),
                ),
            modifier =
                Modifier
                    .blur(6.dp)
                    .graphicsLayer {
                        scaleX = scale * 1.2f
                        scaleY = scale * 1.2f
                        rotationZ = rotation * 0.5f
                        rotationX = sin(distortionPhase) * 10f
                    },
        )
    }

    // Main text with advanced styling
    Text(
        text = flyingText.text,
        style =
            MaterialTheme.typography.headlineMedium.copy(
                fontSize =
                    when {
                        flyingText.isSpecial -> 32.sp
                        flyingText.text.length > 3 -> 26.sp
                        else -> 28.sp
                    },
                fontWeight =
                    when {
                        flyingText.isSpecial -> FontWeight.Black
                        flyingText.text == "log" -> FontWeight.Bold
                        else -> FontWeight.Medium
                    },
                color = textColor.copy(alpha = opacity),
                letterSpacing =
                    when {
                        flyingText.isSpecial -> 1.sp
                        else -> 0.5.sp
                    },
                shadow =
                    Shadow(
                        color = Color.Black.copy(alpha = 0.5f * opacity),
                        offset = Offset(3f, 3f),
                        blurRadius = 6f,
                    ),
            ),
        modifier =
            Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = if (flyingText.isSpecial) rotation * 0.3f else rotation
                    rotationY = if (flyingText.isSpecial) sin(distortionPhase * 2) * 15f else 0f
                    cameraDistance = 12f * with(density) { 1.dp.toPx() }
                }.drawBehind {
                    if (flyingText.isSpecial && glow > 0.5f) {
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            textColor.copy(alpha = 0.3f * opacity),
                                            textColor.copy(alpha = 0.1f * opacity),
                                            Color.Transparent,
                                        ),
                                    radius = 50.dp.toPx(),
                                ),
                        )
                    }
                },
    )
}

// Advanced position calculation with multiple curve types
private fun calculateAdvancedPosition(
    flyingText: FlyingText,
    progress: Float,
    phase: Float,
): Offset {
    val start = flyingText.startPosition
    val end = flyingText.endPosition

    // Calculate base curve parameters
    val distance = calculateDistance(start, end)
    val direction = atan2(end.y - start.y, end.x - start.x)

    // Adaptive curve selection based on distance and direction
    return when {
        flyingText.isSpecial -> {
            // Spiral path for special characters
            calculateSpiralPath(start, end, progress, phase)
        }
        distance > 300f -> {
            // S-curve for long distances
            calculateSCurvePath(start, end, progress)
        }
        abs(start.y - end.y) > abs(start.x - end.x) -> {
            // Vertical arc
            calculateVerticalArcPath(start, end, progress)
        }
        else -> {
            // Enhanced cubic bezier
            calculateEnhancedBezierPath(start, end, progress)
        }
    }
}

private fun calculateSpiralPath(
    start: Offset,
    end: Offset,
    progress: Float,
    phase: Float,
): Offset {
    val centerX = (start.x + end.x) / 2f
    val centerY = (start.y + end.y) / 2f
    val radius = calculateDistance(start, end) / 4f

    val angle = progress * PI * 2 + phase
    val spiralRadius = radius * (1 - progress) * 0.5f

    val spiralX = cos(angle).toFloat() * spiralRadius
    val spiralY = sin(angle).toFloat() * spiralRadius

    return Offset(
        x = lerp(start.x, end.x, progress) + spiralX,
        y = lerp(start.y, end.y, progress) + spiralY,
    )
}

private fun calculateSCurvePath(
    start: Offset,
    end: Offset,
    progress: Float,
): Offset {
    val midPoint = Offset((start.x + end.x) / 2f, (start.y + end.y) / 2f)
    val offset = 100f

    val control1 = Offset(start.x + offset, start.y - offset)
    val control2 = Offset(end.x - offset, end.y + offset)

    // 4-point bezier for S-curve
    val t = progress
    val mt = 1 - t

    return Offset(
        x =
            mt.pow(3) * start.x +
                3 * mt.pow(2) * t * control1.x +
                3 * mt * t.pow(2) * control2.x +
                t.pow(3) * end.x,
        y =
            mt.pow(3) * start.y +
                3 * mt.pow(2) * t * control1.y +
                3 * mt * t.pow(2) * control2.y +
                t.pow(3) * end.y,
    )
}

private fun calculateVerticalArcPath(
    start: Offset,
    end: Offset,
    progress: Float,
): Offset {
    val height = abs(end.y - start.y)
    val arcHeight = height * 0.3f
    val horizontalDistance = end.x - start.x

    val x = start.x + horizontalDistance * progress
    val baseY = start.y + (end.y - start.y) * progress
    val arcY = sin(progress * PI).toFloat() * arcHeight

    return Offset(x, baseY - arcY)
}

private fun calculateEnhancedBezierPath(
    start: Offset,
    end: Offset,
    progress: Float,
): Offset {
    val distance = calculateDistance(start, end)
    val arcHeight = (distance * 0.4f).coerceIn(60f, 180f)

    val controlPoint =
        Offset(
            x = (start.x + end.x) / 2f + (if (end.x > start.x) 1 else -1) * distance * 0.2f,
            y = min(start.y, end.y) - arcHeight,
        )

    // Enhanced quadratic bezier with easing
    val easedProgress = easeOutQuint(progress)
    val t = easedProgress
    val mt = 1 - t

    return Offset(
        x = mt.pow(2) * start.x + 2 * mt * t * controlPoint.x + t.pow(2) * end.x,
        y = mt.pow(2) * start.y + 2 * mt * t * controlPoint.y + t.pow(2) * end.y,
    )
}

// Particle system functions
private fun createInitialParticles(flyingText: FlyingText): List<FlyingParticle> =
    List(20) { i ->
        val angle = (i * 18f).toRadians()
        val speed = Random.nextFloat() * 3f + 1f
        val particleType =
            when (i % 4) {
                0 -> FlyingParticleType.STAR
                1 -> FlyingParticleType.SPARK
                2 -> FlyingParticleType.TRAIL
                else -> FlyingParticleType.CIRCLE
            }

        FlyingParticle(
            position = flyingText.startPosition,
            velocity =
                Offset(
                    x = cos(angle) * speed,
                    y = sin(angle) * speed,
                ),
            life = 1f,
            color =
                when {
                    flyingText.isSpecial -> Color(0xFFFFD700)
                    else -> Color(0xFF2196F3) // Default blue
                }.copy(alpha = 0.8f),
            size = Random.nextFloat() * 8f + 4f,
            rotation = Random.nextFloat() * 360f,
            type = particleType,
        )
    }

private fun updateParticles(
    particles: List<FlyingParticle>,
    onUpdate: (List<FlyingParticle>) -> Unit,
) {
    onUpdate(
        particles.mapNotNull { particle ->
            if (particle.life <= 0f) return@mapNotNull null

            particle.copy(
                position = particle.position + particle.velocity,
                velocity = particle.velocity * 0.98f, // Air resistance
                life = particle.life - 0.02f,
                size = particle.size * 0.97f,
                rotation = particle.rotation + 5f,
            )
        },
    )
}

private fun updateTrail(
    position: Offset,
    scale: Float,
    opacity: Float,
    rotation: Float,
    trail: List<TrailPoint>,
    onUpdate: (List<TrailPoint>) -> Unit,
) {
    onUpdate(
        buildList {
            add(TrailPoint(position, scale, opacity, rotation, System.currentTimeMillis()))
            // Keep only recent trail points
            addAll(
                trail
                    .filter {
                        System.currentTimeMillis() - it.timestamp < 200
                    }.map { it.copy(opacity = it.opacity * 0.9f) },
            )
        },
    )
}

// Drawing functions
private fun DrawScope.drawMotionTrail(
    trail: List<TrailPoint>,
    color: Color,
) {
    trail.forEachIndexed { index, point ->
        val alpha = point.opacity * (1f - index.toFloat() / trail.size)
        drawCircle(
            color = color.copy(alpha = alpha * 0.3f),
            radius = 20f * point.scale * (1f - index.toFloat() / trail.size),
            center = point.position,
        )
    }
}

private fun DrawScope.drawParticles(
    particles: List<FlyingParticle>,
    opacity: Float,
    glow: Float,
) {
    particles.forEach { particle ->
        when (particle.type) {
            FlyingParticleType.STAR -> drawStar(particle, opacity)
            FlyingParticleType.SPARK -> drawSpark(particle, opacity, glow)
            FlyingParticleType.TRAIL -> drawTrailParticle(particle, opacity)
            FlyingParticleType.CIRCLE ->
                drawCircle(
                    color = particle.color.copy(alpha = particle.life * opacity),
                    radius = particle.size,
                    center = particle.position,
                )
        }
    }
}

private fun DrawScope.drawStar(
    particle: FlyingParticle,
    opacity: Float,
) {
    val points = 5
    val outerRadius = particle.size
    val innerRadius = particle.size * 0.5f

    translate(particle.position.x, particle.position.y) {
        rotate(particle.rotation) {
            val path = Path()
            for (i in 0 until points * 2) {
                val radius = if (i % 2 == 0) outerRadius else innerRadius
                val angle = (i * 180f / points).toRadians()
                val x = cos(angle) * radius
                val y = sin(angle) * radius
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()

            drawPath(
                path = path,
                color = particle.color.copy(alpha = particle.life * opacity),
            )
        }
    }
}

private fun DrawScope.drawSpark(
    particle: FlyingParticle,
    opacity: Float,
    glow: Float,
) {
    val sparkLength = particle.size * 2
    val sparkWidth = 2f

    translate(particle.position.x, particle.position.y) {
        rotate(particle.rotation) {
            // Main spark
            drawLine(
                color = particle.color.copy(alpha = particle.life * opacity),
                start = Offset(-sparkLength / 2, 0f),
                end = Offset(sparkLength / 2, 0f),
                strokeWidth = sparkWidth,
            )
            drawLine(
                color = particle.color.copy(alpha = particle.life * opacity),
                start = Offset(0f, -sparkLength / 2),
                end = Offset(0f, sparkLength / 2),
                strokeWidth = sparkWidth,
            )

            // Glow effect
            if (glow > 0.5f) {
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    particle.color.copy(alpha = particle.life * opacity * 0.5f),
                                    Color.Transparent,
                                ),
                            radius = sparkLength,
                        ),
                    radius = sparkLength,
                )
            }
        }
    }
}

private fun DrawScope.drawTrailParticle(
    particle: FlyingParticle,
    opacity: Float,
) {
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        particle.color.copy(alpha = particle.life * opacity),
                        particle.color.copy(alpha = particle.life * opacity * 0.3f),
                        Color.Transparent,
                    ),
                radius = particle.size * 2,
            ),
        radius = particle.size * 2,
        center = particle.position,
    )
}

private fun DrawScope.drawEnergyField(
    color: Color,
    phase: Float,
    intensity: Float,
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val maxRadius = max(size.width, size.height) / 2

    // Draw concentric energy rings
    for (i in 0..3) {
        val radius = maxRadius * (0.5f + i * 0.2f)
        val alpha = (1f - i * 0.25f) * intensity * 0.3f
        val offset = sin(phase + i * 0.5f) * 5f

        drawCircle(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            color.copy(alpha = alpha),
                            color.copy(alpha = alpha * 0.5f),
                            Color.Transparent,
                        ),
                    center = Offset(centerX + offset, centerY),
                    radius = radius,
                ),
            radius = radius,
            center = Offset(centerX + offset, centerY),
        )
    }
}

// Helper functions
private fun calculateDistance(
    start: Offset,
    end: Offset,
): Float = sqrt((end.x - start.x).pow(2) + (end.y - start.y).pow(2))

private fun lerp(
    start: Float,
    stop: Float,
    fraction: Float,
): Float = start + fraction * (stop - start)

private fun Float.toRadians() = this * PI.toFloat() / 180f

private fun easeOutQuint(x: Float): Float = 1 - (1 - x).pow(5)

private fun createCustomEasing(flyingText: FlyingText): Easing =
    if (flyingText.isSpecial) {
        // Bouncy easing for special characters
        CubicBezierEasing(0.68f, -0.55f, 0.265f, 1.55f)
    } else {
        // Smooth easing for regular text
        CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
    }

private fun updatePhysics(
    flyingText: FlyingText,
    progress: Float,
) {
    // Physics simulation would go here
    // Currently using animation-based motion
}

// Enhanced display field tracker
data class DisplayFieldInfo(
    val position: Offset = Offset.Zero,
    val size: androidx.compose.ui.unit.IntSize = androidx.compose.ui.unit.IntSize.Zero,
    val textStyle: TextStyle? = null,
    val textLayoutResult: androidx.compose.ui.text.TextLayoutResult? = null,
    val expression: String = "",
    val cursorVisible: Boolean = true,
)

@Composable
fun rememberDisplayFieldTracker(): MutableState<DisplayFieldInfo> = remember { mutableStateOf(DisplayFieldInfo()) }

// Ultra-precise position calculation with subpixel accuracy
fun DisplayFieldInfo.getAccurateInsertionPosition(
    cursorPosition: Int,
    expression: String,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: androidx.compose.ui.unit.Density,
): Offset {
    // Use cached layout result if available and expression matches
    if (textLayoutResult != null && this.expression == expression) {
        return try {
            val cursorRect = textLayoutResult.getCursorRect(cursorPosition.coerceIn(0, expression.length))
            Offset(
                x = position.x + cursorRect.left,
                y = position.y + cursorRect.center.y,
            )
        } catch (e: Exception) {
            // Fallback to measurement
            getFallbackPosition(cursorPosition, expression, textMeasurer, density)
        }
    }

    return getFallbackPosition(cursorPosition, expression, textMeasurer, density)
}

private fun DisplayFieldInfo.getFallbackPosition(
    cursorPosition: Int,
    expression: String,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: androidx.compose.ui.unit.Density,
): Offset {
    val actualTextStyle =
        textStyle ?: TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
        )

    // Handle empty expression
    if (expression.isEmpty()) {
        return Offset(
            x = position.x + size.width - 40f,
            y = position.y + size.height / 2f,
        )
    }

    // Measure full text and text up to cursor
    val fullMeasure = textMeasurer.measure(expression, actualTextStyle)
    val beforeCursorText = expression.substring(0, cursorPosition.coerceIn(0, expression.length))
    val beforeCursorMeasure =
        if (beforeCursorText.isNotEmpty()) {
            textMeasurer.measure(beforeCursorText, actualTextStyle)
        } else {
            null
        }

    // Calculate for right-aligned, possibly multi-line text
    val rightEdge = position.x + size.width - 40f
    val textStartX = rightEdge - fullMeasure.size.width

    // Compose TextMeasurer returns single layout; emulate caret using measured segment
    val cursorX = if (beforeCursorMeasure != null) textStartX + beforeCursorMeasure.size.width else textStartX

    // Vertical position: try to keep it on the baseline of the line where caret would be.
    val lineHeightPx = fullMeasure.size.height.toFloat()
    val visualCenterY = position.y + size.height / 2f

    return Offset(
        x = cursorX.coerceIn(position.x + 20f, position.x + size.width - 20f),
        y = visualCenterY,
    )
}
