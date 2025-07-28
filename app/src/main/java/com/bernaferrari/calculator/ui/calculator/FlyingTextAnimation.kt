package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class FlyingText(
    val text: String,
    val startPosition: Offset,
    val endPosition: Offset,
    val isSpecial: Boolean = false,
    val id: Long = System.currentTimeMillis()
)

@Composable
fun FlyingTextOverlay(
    flyingTexts: List<FlyingText>,
    onAnimationEnd: (FlyingText) -> Unit
) {
    flyingTexts.forEach { flyingText ->
        key(flyingText.id) {
            AnimatedFlyingText(
                flyingText = flyingText,
                onAnimationEnd = { onAnimationEnd(flyingText) }
            )
        }
    }
}

@Composable
private fun AnimatedFlyingText(
    flyingText: FlyingText,
    onAnimationEnd: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // Animation values
    val animationProgress = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(flyingText) {
        // Start animations in parallel
        scope.launch {
            // Smooth curved motion
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f) // Custom smooth curve
                )
            )
            onAnimationEnd()
        }
        
        scope.launch {
            // More elegant scale animation
            scale.snapTo(0.5f) // Start smaller
            scale.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(150, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 150,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }
    
    // Calculate position - straight line, no arc for precision
    val progress = animationProgress.value
    val x = lerp(flyingText.startPosition.x, flyingText.endPosition.x, progress)
    val y = lerp(flyingText.startPosition.y, flyingText.endPosition.y, progress)
    
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = with(density) { x.toInt() },
                    y = with(density) { y.toInt() }
                )
            }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                // Fade out completely at the end to prevent flash
                alpha = when {
                    progress < 0.8f -> 1f
                    progress < 0.95f -> 1f - ((progress - 0.8f) / 0.15f)
                    else -> 0f // Completely invisible at the end
                }
            }
    ) {
        if (flyingText.isSpecial) {
            // Special golden effect for π and e
            Box {
                // Golden glow behind text
                Text(
                    text = flyingText.text,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 32.sp, // Slightly larger
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700), // Gold
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier
                        .scale(1.2f)
                        .alpha(0.3f)
                        .graphicsLayer {
                            // Subtle glow effect
                            shadowElevation = 8.dp.toPx()
                        }
                )
                
                // Main text
                Text(
                    text = flyingText.text,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700), // Gold
                        letterSpacing = 0.5.sp
                    )
                )
            }
        } else {
            // Regular text
            Text(
                text = flyingText.text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}