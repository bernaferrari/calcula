package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LandingBurst(
    position: Offset,
    onFinished: () -> Unit,
    maxRadius: Dp = 64.dp,
    color: Color = Color.White.copy(alpha = 0.6f)
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(position) {
        progress.animateTo(1f, animationSpec = tween(380))
        onFinished()
    }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val xDp = with(density) { (position.x - maxRadius.toPx()).toDp() }
    val yDp = with(density) { (position.y - maxRadius.toPx()).toDp() }

    Box(
        modifier = Modifier
            .offset(x = xDp, y = yDp)
            .size(maxRadius * 2)
    ) {
        Canvas(Modifier.size(maxRadius * 2)) {
            withTransform({}) {
                val r = maxRadius.toPx() * progress.value
                val alpha = (1f - progress.value).coerceIn(0f, 1f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = alpha * 0.35f),
                            color.copy(alpha = alpha * 0.15f),
                            Color.Transparent
                        )
                    ),
                    radius = r,
                    center = Offset.Zero
                )
            }
        }
    }
}

/**
 * Premium visual effects that make the calculator feel award-winning
 */

@Composable
fun BreathingGlow(
    isActive: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(modifier = modifier) {
        if (isActive) {
            // Simple gradient glow effect instead of blur mask
            Canvas(
                modifier = Modifier.matchParentSize()
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 + glowRadius
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = glowAlpha),
                            color.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    center = center,
                    radius = radius
                )
            }
        }
        content()
    }
}

@Composable
fun ParticleSuccess(
    isActive: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var particles by remember { mutableStateOf<List<SuccessParticle>>(emptyList()) }
    
    LaunchedEffect(isActive) {
        if (isActive) {
            particles = (0..20).map {
                SuccessParticle(
                    x = 0f,
                    y = 0f,
                    velocityX = (Math.random() * 200 - 100).toFloat(),
                    velocityY = (Math.random() * -200 - 50).toFloat(),
                    life = 1f,
                    decay = (Math.random() * 0.02 + 0.01).toFloat(),
                    color = when (it % 4) {
                        0 -> Color(0xFFFFD700) // Gold
                        1 -> Color(0xFF00BCD4) // Cyan
                        2 -> Color(0xFF4CAF50) // Green
                        else -> Color(0xFFFF5722) // Orange
                    }
                )
            }
            
            // Animate particles
            val startTime = System.currentTimeMillis()
            while (particles.any { it.life > 0 }) {
                val deltaTime = (System.currentTimeMillis() - startTime) / 1000f
                particles = particles.map { particle ->
                    particle.copy(
                        x = particle.x + particle.velocityX * deltaTime * 0.016f,
                        y = particle.y + particle.velocityY * deltaTime * 0.016f,
                        velocityY = particle.velocityY + 300 * deltaTime * 0.016f, // Gravity
                        life = (particle.life - particle.decay).coerceAtLeast(0f)
                    )
                }
                kotlinx.coroutines.delay(16)
            }
            
            onFinished()
        }
    }
    
    if (isActive && particles.isNotEmpty()) {
        Canvas(modifier = modifier.fillMaxSize()) {
            particles.forEach { particle ->
                if (particle.life > 0) {
                    drawCircle(
                        color = particle.color.copy(alpha = particle.life),
                        radius = 6.dp.toPx() * particle.life,
                        center = Offset(
                            size.width / 2 + particle.x,
                            size.height / 2 + particle.y
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ContextualStatusIndicator(
    hasError: Boolean,
    isCalculating: Boolean,
    hasResult: Boolean,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when {
        hasError -> MaterialTheme.colorScheme.error
        isCalculating -> MaterialTheme.colorScheme.tertiary
        hasResult -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    
    val scale by animateFloatAsState(
        targetValue = if (hasError || isCalculating) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    AnimatedVisibility(
        visible = hasError || isCalculating || hasResult,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(indicatorColor)
                .scale(scale)
        )
    }
}

@Composable
fun SmartFeedbackPulse(
    isActive: Boolean,
    type: FeedbackType = FeedbackType.SUCCESS,
    modifier: Modifier = Modifier
) {
    val color = when (type) {
        FeedbackType.SUCCESS -> Color(0xFF4CAF50)
        FeedbackType.ERROR -> MaterialTheme.colorScheme.error
        FeedbackType.WARNING -> Color(0xFFFF9800)
        FeedbackType.INFO -> MaterialTheme.colorScheme.primary
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.5f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 0.8f else 0f,
        animationSpec = tween(300)
    )
    
    if (alpha > 0.01f) {
        Box(
            modifier = modifier
                .size(48.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
    }
}

data class SuccessParticle(
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val life: Float,
    val decay: Float,
    val color: Color
)

enum class FeedbackType {
    SUCCESS, ERROR, WARNING, INFO
}