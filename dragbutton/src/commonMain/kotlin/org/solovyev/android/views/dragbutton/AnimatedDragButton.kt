package org.solovyev.android.views.dragbutton

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.TimeSource

/**
 * An animated drag button with premium visual feedback.
 * 
 * Features:
 * - Idle breathing: Subtle pulsing glow when not interacting
 * - 3D tilt: Button tilts toward drag direction
 * - Dynamic shadows: Shadow moves opposite to tilt
 * - Edge glow: Direction-specific glow intensifies near threshold
 * - Direction arrows: Animated arrows appear pointing to active direction
 * - Threshold celebration: Particle burst + strong haptic when activated
 * - Elastic motion: Rubber-band physics for over-dragging
 * - Text animations: Main text shrinks, direction text scales and slides
 */
@Composable
fun AnimatedDragButton(
    text: String,
    onClick: () -> Unit,
    onDrag: (DragDirection) -> Boolean,
    modifier: Modifier = Modifier,
    directionTexts: Map<DragDirection, DirectionTextConfig> = emptyMap(),
    textStyle: TextStyle = LocalTextStyle.current,
    enabled: Boolean = true,
    minDragDistance: Dp = DefaultMinDragDistance,
    vibrateOnDrag: Boolean = true,
    contentColor: Color = LocalContentColor.current,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val minDragDistancePx = with(density) { minDragDistance.toPx() }
    
    // State
    var isPressed by remember { mutableStateOf(false) }
    var rawDragOffset by remember { mutableStateOf(Offset.Zero) }
    var currentDirection by remember { mutableStateOf<DragDirection?>(null) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var hasTriggeredThreshold by remember { mutableStateOf(false) }
    var thresholdPulseCounter by remember { mutableIntStateOf(0) }
    
    // Idle breathing animation
    val breathingAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        breathingAnim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    val breathingGlow = sin(breathingAnim.value * 2 * PI.toFloat()) * 0.5f + 0.5f
    
    // Pulse animation when threshold is crossed
    val pulseScale = remember { Animatable(1f) }
    val celebrationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(thresholdPulseCounter) {
        if (thresholdPulseCounter > 0) {
            // Scale pulse
            pulseScale.snapTo(1.12f)
            pulseScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }
    
    LaunchedEffect(thresholdPulseCounter) {
        if (thresholdPulseCounter > 0) {
            // Celebration particles
            celebrationProgress.snapTo(0f)
            celebrationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(400)
            )
        }
    }
    
    fun updateDragState(offset: Offset) {
        rawDragOffset = offset
        val distance = offset.getDistance()
        val newProgress = (distance / minDragDistancePx).coerceIn(0f, 1.5f)
        dragProgress = newProgress
        
        val newDirection = if (distance > minDragDistancePx * 0.2f) {
            Drag.getDirection(Offset.Zero, offset)
        } else {
            null
        }
        
        val justCrossedThreshold = newProgress >= 1f && !hasTriggeredThreshold && newDirection != null
        if (justCrossedThreshold) {
            hasTriggeredThreshold = true
            thresholdPulseCounter++
            if (vibrateOnDrag) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } else if (newProgress < 0.85f) {
            hasTriggeredThreshold = false
        }
        
        currentDirection = newDirection
    }
    
    // 3D Tilt effect
    val tiltX by animateFloatAsState(
        targetValue = when (currentDirection) {
            DragDirection.up -> 8f * dragProgress.coerceAtMost(1f)
            DragDirection.down -> -8f * dragProgress.coerceAtMost(1f)
            else -> 0f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "tiltX"
    )
    
    val tiltY by animateFloatAsState(
        targetValue = when (currentDirection) {
            DragDirection.left -> -8f * dragProgress.coerceAtMost(1f)
            DragDirection.right -> 8f * dragProgress.coerceAtMost(1f)
            else -> 0f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "tiltY"
    )
    
    // Press scale
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressScale"
    )
    
    // Elastic translation
    val elasticProgress = if (dragProgress > 1f) {
        1f + (dragProgress - 1f) * 0.25f
    } else {
        dragProgress
    }
    
    val translateX by animateFloatAsState(
        targetValue = when (currentDirection) {
            DragDirection.left -> -16f * elasticProgress
            DragDirection.right -> 16f * elasticProgress
            else -> 0f
        },
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "translateX"
    )
    
    val translateY by animateFloatAsState(
        targetValue = when (currentDirection) {
            DragDirection.up -> -16f * elasticProgress
            DragDirection.down -> 16f * elasticProgress
            else -> 0f
        },
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "translateY"
    )
    
    // Main text response
    val mainTextOffsetY by animateDpAsState(
        targetValue = when (currentDirection) {
            DragDirection.up -> 5.dp * dragProgress.coerceAtMost(1f)
            DragDirection.down -> (-5.dp) * dragProgress.coerceAtMost(1f)
            else -> 0.dp
        },
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "mainTextOffsetY"
    )
    
    val mainTextOffsetX by animateDpAsState(
        targetValue = when (currentDirection) {
            DragDirection.left -> 5.dp * dragProgress.coerceAtMost(1f)
            DragDirection.right -> (-5.dp) * dragProgress.coerceAtMost(1f)
            else -> 0.dp
        },
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "mainTextOffsetX"
    )
    
    val mainTextScale by animateFloatAsState(
        targetValue = if (currentDirection != null) {
            lerp(1f, 0.8f, dragProgress.coerceAtMost(1f))
        } else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "mainTextScale"
    )
    
    val mainTextAlpha by animateFloatAsState(
        targetValue = if (hasTriggeredThreshold) 0.5f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "mainTextAlpha"
    )
    
    // Colors
    val accentColor = MaterialTheme.colorScheme.primary
    val activeColor = MaterialTheme.colorScheme.tertiary
    val glowColor = if (hasTriggeredThreshold) activeColor else accentColor
    
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale * pulseScale.value
                scaleY = pressScale * pulseScale.value
                translationX = translateX
                translationY = translateY
                rotationX = tiltX
                rotationY = tiltY
                cameraDistance = 12f * density.density
            }
            .drawBehind {
                // Base background
                drawRect(backgroundColor)
                
                // Idle breathing glow (only when not dragging)
                if (currentDirection == null && !isPressed) {
                    val breathAlpha = breathingGlow * 0.08f
                    drawRoundRect(
                        color = accentColor.copy(alpha = breathAlpha),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
                
                // Edge glow during drag
                if (currentDirection != null && dragProgress > 0.3f) {
                    val glowIntensity = ((dragProgress - 0.3f) / 0.7f).coerceIn(0f, 1f)
                    val glowAlpha = glowIntensity * if (hasTriggeredThreshold) 0.5f else 0.35f
                    val glowSize = size.minDimension * 0.2f
                    
                    when (currentDirection) {
                        DragDirection.up -> {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(glowColor.copy(alpha = glowAlpha), Color.Transparent),
                                    startY = 0f,
                                    endY = glowSize
                                ),
                                size = Size(size.width, glowSize)
                            )
                        }
                        DragDirection.down -> {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, glowColor.copy(alpha = glowAlpha)),
                                    startY = size.height - glowSize,
                                    endY = size.height
                                ),
                                topLeft = Offset(0f, size.height - glowSize),
                                size = Size(size.width, glowSize)
                            )
                        }
                        DragDirection.left -> {
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(glowColor.copy(alpha = glowAlpha), Color.Transparent),
                                    startX = 0f,
                                    endX = glowSize
                                ),
                                size = Size(glowSize, size.height)
                            )
                        }
                        DragDirection.right -> {
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, glowColor.copy(alpha = glowAlpha)),
                                    startX = size.width - glowSize,
                                    endX = size.width
                                ),
                                topLeft = Offset(size.width - glowSize, 0f),
                                size = Size(glowSize, size.height)
                            )
                        }
                        else -> {}
                    }
                }
                
                // Progress arc indicator
                if (dragProgress > 0.1f && currentDirection != null && !hasTriggeredThreshold) {
                    val arcProgress = dragProgress.coerceAtMost(1f)
                    val arcAlpha = arcProgress * 0.6f
                    val strokeWidth = 3f
                    val arcPadding = 6f
                    
                    drawArc(
                        color = accentColor.copy(alpha = arcAlpha),
                        startAngle = -90f,
                        sweepAngle = 360f * arcProgress,
                        useCenter = false,
                        topLeft = Offset(arcPadding, arcPadding),
                        size = Size(size.width - arcPadding * 2, size.height - arcPadding * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                // Celebration particles when threshold triggered
                if (celebrationProgress.value > 0f && celebrationProgress.value < 1f) {
                    val particleCount = 8
                    val maxRadius = size.minDimension * 0.6f
                    val particleProgress = celebrationProgress.value
                    
                    for (i in 0 until particleCount) {
                        val angle = (i.toFloat() / particleCount) * 2 * PI.toFloat()
                        val radius = maxRadius * particleProgress
                        val particleAlpha = (1f - particleProgress) * 0.7f
                        val particleSize = 4f * (1f - particleProgress * 0.5f)
                        
                        val x = size.width / 2 + cos(angle) * radius
                        val y = size.height / 2 + sin(angle) * radius
                        
                        drawCircle(
                            color = activeColor.copy(alpha = particleAlpha),
                            radius = particleSize,
                            center = Offset(x, y)
                        )
                    }
                }
            }
            .drawWithContent {
                drawContent()
                
                // Direction arrow indicator
                if (currentDirection != null && dragProgress > 0.5f) {
                    val arrowAlpha = ((dragProgress - 0.5f) * 2f).coerceAtMost(1f) * 0.6f
                    val arrowSize = 12f
                    val arrowOffset = 8f + dragProgress * 4f
                    val arrowColor = if (hasTriggeredThreshold) activeColor else accentColor
                    
                    val arrowPath = Path()
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    when (currentDirection) {
                        DragDirection.up -> {
                            val tip = Offset(center.x, arrowOffset)
                            arrowPath.moveTo(tip.x, tip.y)
                            arrowPath.lineTo(tip.x - arrowSize / 2, tip.y + arrowSize)
                            arrowPath.lineTo(tip.x + arrowSize / 2, tip.y + arrowSize)
                            arrowPath.close()
                        }
                        DragDirection.down -> {
                            val tip = Offset(center.x, size.height - arrowOffset)
                            arrowPath.moveTo(tip.x, tip.y)
                            arrowPath.lineTo(tip.x - arrowSize / 2, tip.y - arrowSize)
                            arrowPath.lineTo(tip.x + arrowSize / 2, tip.y - arrowSize)
                            arrowPath.close()
                        }
                        DragDirection.left -> {
                            val tip = Offset(arrowOffset, center.y)
                            arrowPath.moveTo(tip.x, tip.y)
                            arrowPath.lineTo(tip.x + arrowSize, tip.y - arrowSize / 2)
                            arrowPath.lineTo(tip.x + arrowSize, tip.y + arrowSize / 2)
                            arrowPath.close()
                        }
                        DragDirection.right -> {
                            val tip = Offset(size.width - arrowOffset, center.y)
                            arrowPath.moveTo(tip.x, tip.y)
                            arrowPath.lineTo(tip.x - arrowSize, tip.y - arrowSize / 2)
                            arrowPath.lineTo(tip.x - arrowSize, tip.y + arrowSize / 2)
                            arrowPath.close()
                        }
                        else -> {}
                    }
                    
                    drawPath(
                        path = arrowPath,
                        color = arrowColor.copy(alpha = arrowAlpha)
                    )
                }
            }
            .pointerInput(enabled, onClick, onDrag) {
                if (!enabled) return@pointerInput
                
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    hasTriggeredThreshold = false
                    val start = down.position
                    val downMark = TimeSource.Monotonic.markNow()
                    
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            
                            if (change.changedToUpIgnoreConsumed()) {
                                val duration = downMark.elapsedNow().inWholeMilliseconds
                                val finalOffset = change.position - start
                                val distance = finalOffset.getDistance()
                                
                                if (distance >= minDragDistancePx && duration in 40..2500) {
                                    val direction = Drag.getDirection(Offset.Zero, finalOffset)
                                    if (direction != null) {
                                        val consumed = onDrag(direction)
                                        if (consumed && vibrateOnDrag) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    }
                                } else if (distance < minDragDistancePx * 0.35f) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onClick()
                                }
                                break
                            }
                            
                            if (change.positionChanged()) {
                                val currentOffset = change.position - start
                                updateDragState(currentOffset)
                                change.consume()
                            }
                        }
                    } finally {
                        isPressed = false
                        rawDragOffset = Offset.Zero
                        dragProgress = 0f
                        currentDirection = null
                        hasTriggeredThreshold = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedDragButtonContent(
            text = text,
            directionTexts = directionTexts,
            textStyle = textStyle,
            contentColor = contentColor,
            currentDirection = currentDirection,
            dragProgress = dragProgress,
            mainTextOffsetX = mainTextOffsetX,
            mainTextOffsetY = mainTextOffsetY,
            mainTextScale = mainTextScale,
            mainTextAlpha = mainTextAlpha,
            hasTriggeredThreshold = hasTriggeredThreshold
        )
    }
}

@Composable
private fun BoxScope.AnimatedDragButtonContent(
    text: String,
    directionTexts: Map<DragDirection, DirectionTextConfig>,
    textStyle: TextStyle,
    contentColor: Color,
    currentDirection: DragDirection?,
    dragProgress: Float,
    mainTextOffsetX: Dp,
    mainTextOffsetY: Dp,
    mainTextScale: Float,
    mainTextAlpha: Float,
    hasTriggeredThreshold: Boolean
) {
    val density = LocalDensity.current
    val activeColor = MaterialTheme.colorScheme.tertiary
    
    // Main text
    Text(
        text = text,
        style = textStyle,
        color = contentColor.copy(alpha = mainTextAlpha),
        modifier = Modifier
            .graphicsLayer {
                scaleX = mainTextScale
                scaleY = mainTextScale
            }
            .offset {
                IntOffset(
                    x = with(density) { mainTextOffsetX.roundToPx() },
                    y = with(density) { mainTextOffsetY.roundToPx() }
                )
            }
    )
    
    // Direction texts
    for (direction in DragDirection.entries) {
        val config = directionTexts[direction] ?: continue
        if (!config.visible || config.text.isEmpty()) continue
        
        val isActiveDirection = currentDirection == direction
        val clampedProgress = dragProgress.coerceAtMost(1.3f)
        
        val targetAlpha by animateFloatAsState(
            targetValue = when {
                isActiveDirection && hasTriggeredThreshold -> 1f
                isActiveDirection -> lerp(config.alpha, 1f, clampedProgress.coerceAtMost(1f))
                currentDirection != null -> config.alpha * lerp(1f, 0.15f, clampedProgress.coerceAtMost(1f))
                else -> config.alpha
            },
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
            label = "directionAlpha_$direction"
        )
        
        val targetScale by animateFloatAsState(
            targetValue = when {
                isActiveDirection && hasTriggeredThreshold -> 1.6f
                isActiveDirection -> lerp(1f, 1.4f, clampedProgress.coerceAtMost(1f))
                currentDirection != null -> lerp(1f, 0.7f, clampedProgress.coerceAtMost(1f))
                else -> 1f
            },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "directionScale_$direction"
        )
        
        val slideOffset by animateDpAsState(
            targetValue = if (isActiveDirection) {
                val slideAmount = if (hasTriggeredThreshold) 14.dp else 10.dp
                when (direction) {
                    DragDirection.up -> slideAmount * clampedProgress.coerceAtMost(1f)
                    DragDirection.down -> -slideAmount * clampedProgress.coerceAtMost(1f)
                    DragDirection.left -> slideAmount * clampedProgress.coerceAtMost(1f)
                    DragDirection.right -> -slideAmount * clampedProgress.coerceAtMost(1f)
                }
            } else 0.dp,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
            label = "directionSlide_$direction"
        )
        
        val directionTextStyle = textStyle.copy(
            fontSize = textStyle.fontSize * config.scale,
            fontWeight = when {
                isActiveDirection && hasTriggeredThreshold -> FontWeight.ExtraBold
                isActiveDirection && clampedProgress > 0.5f -> FontWeight.Bold
                else -> FontWeight.Normal
            }
        )
        
        val textColor = when {
            isActiveDirection && hasTriggeredThreshold -> activeColor
            else -> contentColor.copy(alpha = targetAlpha)
        }
        
        Text(
            text = config.text,
            style = directionTextStyle,
            color = textColor,
            modifier = Modifier
                .align(direction.toAlignment())
                .padding(config.padding)
                .graphicsLayer {
                    scaleX = targetScale
                    scaleY = targetScale
                }
                .offset {
                    when (direction) {
                        DragDirection.up, DragDirection.down -> IntOffset(
                            x = 0,
                            y = with(density) { slideOffset.roundToPx() }
                        )
                        DragDirection.left, DragDirection.right -> IntOffset(
                            x = with(density) { slideOffset.roundToPx() },
                            y = 0
                        )
                    }
                }
        )
    }
}

private fun DragDirection.toAlignment(): Alignment = when (this) {
    DragDirection.up -> Alignment.TopEnd
    DragDirection.down -> Alignment.BottomEnd
    DragDirection.left -> Alignment.CenterStart
    DragDirection.right -> Alignment.CenterEnd
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
