package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.domain.calculator.TokenizedExpression
import com.bernaferrari.calculator.domain.calculator.TokenType

data class Suggestion(
    val text: String,
    val description: String,
    val type: SuggestionType,
    val value: String? = null
)

enum class SuggestionType {
    CONSTANT,
    FUNCTION,
    RECENT_RESULT,
    PARENTHESIS,
    OPERATOR,
    COMPLETION
}

@Composable
fun SmartSuggestions(
    expression: String,
    recentResults: List<String>,
    onSuggestionClick: (Suggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = remember(expression, recentResults) {
        generateSuggestions(expression, recentResults)
    }
    
    AnimatedVisibility(
        visible = suggestions.isNotEmpty(),
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(suggestions) { suggestion ->
                SuggestionChip(
                    suggestion = suggestion,
                    onClick = { onSuggestionClick(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    suggestion: Suggestion,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = when (suggestion.type) {
            SuggestionType.CONSTANT -> MaterialTheme.colorScheme.tertiaryContainer
            SuggestionType.FUNCTION -> MaterialTheme.colorScheme.secondaryContainer
            SuggestionType.RECENT_RESULT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            SuggestionType.PARENTHESIS -> MaterialTheme.colorScheme.surfaceVariant
            SuggestionType.OPERATOR -> MaterialTheme.colorScheme.primaryContainer
            SuggestionType.COMPLETION -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        },
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = suggestion.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = when (suggestion.type) {
                    SuggestionType.CONSTANT -> MaterialTheme.colorScheme.onTertiaryContainer
                    SuggestionType.FUNCTION -> MaterialTheme.colorScheme.onSecondaryContainer
                    SuggestionType.RECENT_RESULT -> MaterialTheme.colorScheme.onPrimaryContainer
                    SuggestionType.PARENTHESIS -> MaterialTheme.colorScheme.onSurfaceVariant
                    SuggestionType.OPERATOR -> MaterialTheme.colorScheme.onPrimaryContainer
                    SuggestionType.COMPLETION -> MaterialTheme.colorScheme.primary
                }
            )
            
            if (suggestion.description.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = suggestion.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun generateSuggestions(expression: String, recentResults: List<String>): List<Suggestion> {
    val suggestions = mutableListOf<Suggestion>()
    
    if (expression.isEmpty()) {
        // Show recent results when empty
        recentResults.take(3).forEach { result ->
            suggestions.add(
                Suggestion(
                    text = result,
                    description = "recent",
                    type = SuggestionType.RECENT_RESULT,
                    value = result
                )
            )
        }
        return suggestions
    }
    
    // Parse expression to understand context
    val tokens = try {
        TokenizedExpression.parse(expression).getTokens()
    } catch (e: Exception) {
        emptyList()
    }
    
    val lastToken = tokens.lastOrNull()
    val lastChar = expression.lastOrNull()
    
    // Check for incomplete functions
    if (lastChar?.isLetter() == true) {
        val partial = expression.takeLastWhile { it.isLetter() }
        val functionSuggestions = listOf(
            "sin" to "sine",
            "cos" to "cosine", 
            "tan" to "tangent",
            "log" to "logarithm",
            "ln" to "natural log",
            "sqrt" to "square root"
        )
        
        functionSuggestions
            .filter { it.first.startsWith(partial, ignoreCase = true) }
            .forEach { (func, desc) ->
                suggestions.add(
                    Suggestion(
                        text = func,
                        description = desc,
                        type = SuggestionType.FUNCTION,
                        value = func + "("
                    )
                )
            }
    }
    
    // Smart parenthesis completion
    val openParens = expression.count { it == '(' }
    val closeParens = expression.count { it == ')' }
    if (openParens > closeParens) {
        suggestions.add(
            Suggestion(
                text = ")",
                description = "close",
                type = SuggestionType.PARENTHESIS,
                value = ")"
            )
        )
    }
    
    // Suggest operators after numbers
    if (lastToken?.type == TokenType.NUMBER || lastToken?.type == TokenType.PARENTHESIS_CLOSE) {
        listOf(
            "+" to "add",
            "−" to "subtract",
            "×" to "multiply",
            "÷" to "divide",
            "%" to "modulo"
        ).forEach { (op, desc) ->
            suggestions.add(
                Suggestion(
                    text = op,
                    description = desc,
                    type = SuggestionType.OPERATOR,
                    value = " $op "
                )
            )
        }
    }
    
    // Suggest constants
    if (lastToken?.type == TokenType.OPERATOR || expression.isEmpty()) {
        listOf(
            "π" to "3.14159...",
            "e" to "2.71828..."
        ).forEach { (const, desc) ->
            suggestions.add(
                Suggestion(
                    text = const,
                    description = desc,
                    type = SuggestionType.CONSTANT,
                    value = const
                )
            )
        }
    }
    
    // Smart completion for common patterns
    when {
        expression.endsWith("2+2") -> {
            suggestions.add(0, Suggestion("= 4", "complete", SuggestionType.COMPLETION))
        }
        expression.matches(Regex("\\d+\\s*×\\s*\\d+$")) -> {
            suggestions.add(0, Suggestion("²", "square", SuggestionType.OPERATOR, "²"))
        }
    }
    
    return suggestions.take(5) // Limit to 5 suggestions
}