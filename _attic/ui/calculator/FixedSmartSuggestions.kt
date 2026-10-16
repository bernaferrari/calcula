package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Fixed smart suggestions bar that never causes UI jumping
 * Always maintains consistent height and smoothly transitions content
 */
@Composable
fun FixedSmartSuggestionsBar(
    expression: String,
    openParenthesesCount: Int,
    onSuggestionClick: (SmartSuggestion) -> Unit,
    onParenthesesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions by remember(expression) {
        derivedStateOf { IntelligentInputAssistant.getSuggestions(expression) }
    }
    
    // Always render the container with fixed height - no jumping!
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp), // Fixed height prevents jumping
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Smart parentheses indicator (always visible space)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = openParenthesesCount,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
                    }
                ) { count ->
                    if (count > 0) {
                        SmartParenthesesChip(
                            count = count,
                            onClick = onParenthesesClick
                        )
                    } else {
                        // Empty space to maintain layout stability
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }
            
            // Right side: Smart suggestions (scrollable, fixed height area)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                AnimatedContent(
                    targetState = suggestions.isNotEmpty(),
                    transitionSpec = {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
                    }
                ) { hasSuggestions ->
                    if (hasSuggestions) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            contentPadding = PaddingValues(start = 16.dp)
                        ) {
                            items(suggestions) { suggestion ->
                                CompactSuggestionChip(
                                    suggestion = suggestion,
                                    onClick = { onSuggestionClick(suggestion) }
                                )
                            }
                        }
                    } else {
                        // Show helpful hint when no suggestions
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (expression.isEmpty()) "Start typing for suggestions" else "Ready to calculate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartParenthesesChip(
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.DataObject,
                contentDescription = "Close parentheses",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(16.dp)
            )
            
            // Show visual parentheses
            repeat(minOf(count, 3)) { // Max 3 visual indicators
                Text(
                    text = "(",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            if (count > 3) {
                Text(
                    text = "+${count - 3}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CompactSuggestionChip(
    suggestion: SmartSuggestion,
    onClick: () -> Unit
) {
    val categoryColor = when (suggestion.category) {
        SuggestionCategory.FUNCTION -> MaterialTheme.colorScheme.primaryContainer
        SuggestionCategory.CONSTANT -> MaterialTheme.colorScheme.secondaryContainer
        SuggestionCategory.OPERATION -> MaterialTheme.colorScheme.tertiaryContainer
        SuggestionCategory.COMPLETION -> MaterialTheme.colorScheme.surfaceVariant
        SuggestionCategory.PATTERN -> MaterialTheme.colorScheme.errorContainer
    }
    
    val contentColor = when (suggestion.category) {
        SuggestionCategory.FUNCTION -> MaterialTheme.colorScheme.onPrimaryContainer
        SuggestionCategory.CONSTANT -> MaterialTheme.colorScheme.onSecondaryContainer
        SuggestionCategory.OPERATION -> MaterialTheme.colorScheme.onTertiaryContainer
        SuggestionCategory.COMPLETION -> MaterialTheme.colorScheme.onSurfaceVariant
        SuggestionCategory.PATTERN -> MaterialTheme.colorScheme.onErrorContainer
    }
    
    // Compact design for the fixed bar
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = categoryColor,
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = suggestion.icon,
                contentDescription = suggestion.description,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            
            Text(
                text = suggestion.displayText,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Enhanced version with contextual smart actions
 */
@Composable
fun ContextualSmartActions(
    expression: String,
    result: String,
    isError: Boolean,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = remember(expression, result, isError) {
        buildList {
            if (!isError && result != "0" && result.isNotEmpty()) {
                // Actions for when we have a result
                add("Copy result" to "copy:$result")
                add("Use as input" to "input:$result")
                
                // Smart mathematical actions
                val numResult = result.toDoubleOrNull()
                if (numResult != null) {
                    if (numResult > 0) {
                        add("√" to "sqrt($result)")
                        add("²" to "($result)²")
                    }
                    if (numResult.toInt().toDouble() == numResult && numResult in 1.0..10.0) {
                        add("!" to "$result!")
                    }
                }
            }
            
            if (expression.isNotEmpty()) {
                add("Clear" to "clear")
                add("Step-by-step" to "steps")
            }
        }
    }
    
    if (actions.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = modifier
        ) {
            items(actions) { (label, action) ->
                SmallActionButton(
                    label = label,
                    onClick = { onActionClick(action) }
                )
            }
        }
    }
}

@Composable
private fun SmallActionButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.height(24.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}