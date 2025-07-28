package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@Composable
fun CelebrationEffect(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    onFinished: () -> Unit = {}
) {
    if (!isActive) return
    
    var particles by remember { mutableStateOf(generateParticles()) }
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(isActive) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 3000,
                easing = LinearEasing
            )
        )
        onFinished()
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawParticle(particle, animationProgress.value)
        }
    }
}

private data class Particle(
    val startPosition: Offset,
    val velocity: Offset,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val type: ParticleType
)

private enum class ParticleType {
    CIRCLE, STAR, SQUARE, TRIANGLE
}

private fun generateParticles(): List<Particle> {
    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFE66D),
        Color(0xFF95E1D3),
        Color(0xFFC7CEEA),
        Color(0xFFFFA07A)
    )
    
    return List(50) {
        Particle(
            startPosition = Offset(
                x = Random.nextFloat() * 1000f,
                y = Random.nextFloat() * 800f
            ),
            velocity = Offset(
                x = (Random.nextFloat() - 0.5f) * 200f,
                y = -Random.nextFloat() * 400f - 200f
            ),
            color = colors.random(),
            size = Random.nextFloat() * 20f + 10f,
            rotationSpeed = Random.nextFloat() * 360f,
            type = ParticleType.values().random()
        )
    }
}

private fun DrawScope.drawParticle(particle: Particle, progress: Float) {
    val gravity = 500f
    val currentPosition = Offset(
        x = particle.startPosition.x + particle.velocity.x * progress,
        y = particle.startPosition.y + particle.velocity.y * progress + 0.5f * gravity * progress * progress
    )
    
    val alpha = 1f - progress
    val rotation = particle.rotationSpeed * progress
    
    when (particle.type) {
        ParticleType.CIRCLE -> {
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size,
                center = currentPosition
            )
        }
        ParticleType.STAR -> {
            // Draw a simple star
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size * 0.8f,
                center = currentPosition
            )
        }
        ParticleType.SQUARE -> {
            drawRect(
                color = particle.color.copy(alpha = alpha),
                topLeft = currentPosition - Offset(particle.size / 2, particle.size / 2),
                size = androidx.compose.ui.geometry.Size(particle.size, particle.size)
            )
        }
        ParticleType.TRIANGLE -> {
            // Simplified triangle
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size * 0.7f,
                center = currentPosition
            )
        }
    }
}

fun shouldCelebrate(result: String): Boolean {
    val specialNumbers = listOf(
        "3.14159", "3.1415", "3.141", "3.14", // Pi
        "2.71828", "2.7182", "2.718", "2.71", // e
        "42", // Answer to everything
        "69", "420", // Meme numbers
        "1337", // Leet
        "666", // Devil's number
        "777", // Lucky number
        "911", // Emergency
        "1234", "12345", // Sequential
        "100", "1000", "10000", // Round numbers
        "0", // Zero
        "1", // Unity
        "infinity", "∞" // Infinity
    )
    
    return specialNumbers.any { result.startsWith(it) }
}