package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.domain.calculator.CalculationSuggestion
import kotlinx.coroutines.delay

@Composable
fun QuickCalculationBubbles(
    suggestions: List<CalculationSuggestion>,
    onSuggestionClick: (CalculationSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = suggestions.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = suggestions,
                key = { it.expression }
            ) { suggestion ->
                QuickCalculationBubble(
                    suggestion = suggestion,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun QuickCalculationBubble(
    suggestion: CalculationSuggestion,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val shimmerTransition = rememberInfiniteTransition()
    val shimmerAlpha by shimmerTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Surface(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .scale(scale)
            .height(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = shimmerAlpha)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = suggestion.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatResult(suggestion.result.toPlainString()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun PercentageQuickActions(
    currentValue: String,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val percentages = listOf("10%", "15%", "20%", "25%", "50%")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        percentages.forEach { percentage ->
            PercentageBubble(
                percentage = percentage,
                onClick = { onActionClick("$currentValue × $percentage") }
            )
        }
    }
}

@Composable
private fun PercentageBubble(
    percentage: String,
    onClick: () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isAnimating) 360f else 0f,
        animationSpec = tween(500),
        finishedListener = { isAnimating = false }
    )
    
    Surface(
        onClick = {
            isAnimating = true
            onClick()
        },
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer { rotationZ = rotation },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 1.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = percentage,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatResult(value: String): String {
    return try {
        val number = value.toDouble()
        when {
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000)
            number >= 1_000 -> String.format("%.1fK", number / 1_000)
            number % 1 == 0.0 -> number.toInt().toString()
            else -> String.format("%.2f", number).trimEnd('0').trimEnd('.')
        }
    } catch (e: Exception) {
        value
    }
}