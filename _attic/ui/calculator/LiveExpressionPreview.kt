package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LiveExpressionPreview(
    expression: String,
    result: String?,
    isCalculating: Boolean,
    modifier: Modifier = Modifier
) {
    var previousResult by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(result) {
        if (result != null && result != previousResult) {
            previousResult = result
        }
    }
    
    AnimatedVisibility(
        visible = expression.isNotEmpty() && (result != null || isCalculating),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Expression side
                    Text(
                        text = expression,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    // Animated equals sign
                    AnimatedEquals(
                        isAnimating = isCalculating,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    // Result side with animation
                    AnimatedResult(
                        result = result,
                        isCalculating = isCalculating,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedEquals(
    isAnimating: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Text(
        text = "=",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.alpha(if (isAnimating) alpha else 1f)
    )
}

@Composable
private fun AnimatedResult(
    result: String?,
    isCalculating: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (isCalculating) {
            LoadingDots()
        } else {
            result?.let {
                AnimatedContent(
                    targetState = it,
                    transitionSpec = {
                        (slideInVertically { height -> -height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> height } + fadeOut())
                    }
                ) { targetResult ->
                    Text(
                        text = formatPreviewResult(targetResult),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 100),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun SmartParenthesesIndicator(
    openCount: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = openCount > 0,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(openCount) {
                    Text(
                        text = "(",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Text(
                    text = "open",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun formatPreviewResult(value: String): String {
    return try {
        val number = value.toDouble()
        when {
            number >= 1_000_000_000 -> String.format("%.2fB", number / 1_000_000_000)
            number >= 1_000_000 -> String.format("%.2fM", number / 1_000_000)
            number >= 1_000 -> String.format("%.2fK", number / 1_000)
            number % 1 == 0.0 -> number.toInt().toString()
            else -> {
                val formatted = String.format("%.4f", number).trimEnd('0').trimEnd('.')
                if (formatted.length > 10) {
                    String.format("%.2e", number)
                } else {
                    formatted
                }
            }
        }
    } catch (e: Exception) {
        value
    }
}