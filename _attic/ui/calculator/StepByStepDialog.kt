package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

data class CalculationStep(
    val expression: String,
    val result: String,
    val explanation: String,
    val highlightStart: Int = -1,
    val highlightEnd: Int = -1
)

@Composable
fun StepByStepDialog(
    expression: String,
    result: String,
    onDismiss: () -> Unit
) {
    val steps = remember(expression) {
        generateSteps(expression, result)
    }
    
    var currentStep by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    
    // Auto-play animation
    LaunchedEffect(isPlaying, currentStep) {
        if (isPlaying && currentStep < steps.size - 1) {
            delay(2000) // 2 seconds per step
            currentStep++
        } else if (currentStep >= steps.size - 1) {
            isPlaying = false
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Step-by-Step Solution",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    steps.forEachIndexed { index, step ->
                        AnimatedVisibility(
                            visible = index <= currentStep,
                            enter = slideInVertically() + fadeIn(),
                            exit = ExitTransition.None
                        ) {
                            StepCard(
                                step = step,
                                stepNumber = index + 1,
                                isActive = index == currentStep,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                
                // Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button
                    TextButton(
                        onClick = { 
                            if (currentStep > 0) {
                                currentStep--
                                isPlaying = false
                            }
                        },
                        enabled = currentStep > 0
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }
                    
                    // Play/Pause button
                    FilledTonalButton(
                        onClick = { isPlaying = !isPlaying }
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPlaying) "Pause" else "Play")
                    }
                    
                    // Next button
                    TextButton(
                        onClick = { 
                            if (currentStep < steps.size - 1) {
                                currentStep++
                                isPlaying = false
                            }
                        },
                        enabled = currentStep < steps.size - 1
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    step: CalculationStep,
    stepNumber: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.6f,
        animationSpec = tween(300)
    )
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        border = if (isActive) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .graphicsLayer { alpha = animatedAlpha }
        ) {
            // Step number and explanation
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = stepNumber.toString(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = step.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Expression with highlighting
            val annotatedExpression = buildAnnotatedString {
                if (step.highlightStart >= 0 && step.highlightEnd >= 0) {
                    append(step.expression.substring(0, step.highlightStart))
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            background = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        append(step.expression.substring(step.highlightStart, step.highlightEnd))
                    }
                    append(step.expression.substring(step.highlightEnd))
                } else {
                    append(step.expression)
                }
            }
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = annotatedExpression,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (step.result.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "= ${step.result}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun generateSteps(expression: String, finalResult: String): List<CalculationStep> {
    val steps = mutableListOf<CalculationStep>()
    
    // This is a simplified example - in a real implementation,
    // you would parse the expression tree and evaluate step by step
    
    // Example for "2 + 3 × 4 - 1"
    if (expression.contains("×") || expression.contains("÷")) {
        // Step 1: Identify order of operations
        steps.add(
            CalculationStep(
                expression = expression,
                result = "",
                explanation = "Following order of operations (PEMDAS/BODMAS), we first calculate multiplication and division from left to right.",
                highlightStart = expression.indexOfAny(listOf("×", "÷")),
                highlightEnd = expression.indexOfAny(listOf("×", "÷")) + 1
            )
        )
    }
    
    // Example multiplication step
    val multiplyMatch = Regex("(\\d+)\\s*×\\s*(\\d+)").find(expression)
    if (multiplyMatch != null) {
        val (num1, num2) = multiplyMatch.destructured
        val product = num1.toInt() * num2.toInt()
        steps.add(
            CalculationStep(
                expression = expression,
                result = expression.replace(multiplyMatch.value, product.toString()),
                explanation = "Multiply $num1 × $num2 = $product",
                highlightStart = multiplyMatch.range.first,
                highlightEnd = multiplyMatch.range.last + 1
            )
        )
    }
    
    // Final step
    steps.add(
        CalculationStep(
            expression = expression,
            result = finalResult,
            explanation = "Final result after performing all operations",
            highlightStart = -1,
            highlightEnd = -1
        )
    )
    
    return steps
}