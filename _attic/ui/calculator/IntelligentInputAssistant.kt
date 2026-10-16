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

data class SmartSuggestion(
    val text: String,
    val displayText: String,
    val description: String,
    val icon: ImageVector,
    val category: SuggestionCategory,
    val confidence: Float = 1.0f
)

enum class SuggestionCategory {
    COMPLETION, FUNCTION, CONSTANT, OPERATION, PATTERN
}

object IntelligentInputAssistant {
    
    fun getSuggestions(currentExpression: String): List<SmartSuggestion> {
        if (currentExpression.isEmpty()) return getStarterSuggestions()
        
        val suggestions = mutableListOf<SmartSuggestion>()
        val lastChar = currentExpression.lastOrNull()
        val lastTwo = if (currentExpression.length >= 2) currentExpression.takeLast(2) else ""
        val lastThree = if (currentExpression.length >= 3) currentExpression.takeLast(3) else ""
        
        // Smart function completions with better matching
        when {
            currentExpression.endsWith("sin") -> {
                suggestions.add(SmartSuggestion("(", "sin(", "Complete sine function", Icons.Default.Functions, SuggestionCategory.COMPLETION))
            }
            currentExpression.endsWith("cos") -> {
                suggestions.add(SmartSuggestion("(", "cos(", "Complete cosine function", Icons.Default.Functions, SuggestionCategory.COMPLETION))
            }
            currentExpression.endsWith("tan") -> {
                suggestions.add(SmartSuggestion("(", "tan(", "Complete tangent function", Icons.Default.Functions, SuggestionCategory.COMPLETION))
            }
            currentExpression.endsWith("log") -> {
                suggestions.add(SmartSuggestion("(", "log(", "Complete logarithm", Icons.Default.Functions, SuggestionCategory.COMPLETION))
            }
            currentExpression.endsWith("ln") -> {
                suggestions.add(SmartSuggestion("(", "ln(", "Complete natural log", Icons.Default.Functions, SuggestionCategory.COMPLETION))
            }
            currentExpression.endsWith("sqrt") -> {
                suggestions.add(SmartSuggestion("(", "√(", "Complete square root", Icons.Default.Functions, SuggestionCategory.COMPLETION))
            }
            // Partial matching for better UX
            lastTwo == "si" -> {
                suggestions.add(SmartSuggestion("n(", "sin(", "Sine function", Icons.Default.Functions, SuggestionCategory.FUNCTION))
            }
            lastTwo == "co" -> {
                suggestions.add(SmartSuggestion("s(", "cos(", "Cosine function", Icons.Default.Functions, SuggestionCategory.FUNCTION))
            }
            lastTwo == "ta" -> {
                suggestions.add(SmartSuggestion("n(", "tan(", "Tangent function", Icons.Default.Functions, SuggestionCategory.FUNCTION))
            }
            lastTwo == "lo" -> {
                suggestions.add(SmartSuggestion("g(", "log(", "Logarithm base 10", Icons.Default.Functions, SuggestionCategory.FUNCTION))
            }
            lastTwo == "sq" -> {
                suggestions.add(SmartSuggestion("rt(", "√(", "Square root", Icons.Default.Functions, SuggestionCategory.FUNCTION))
            }
            lastChar == 'l' -> {
                suggestions.add(SmartSuggestion("og(", "log(", "Logarithm", Icons.Default.Functions, SuggestionCategory.FUNCTION))
                suggestions.add(SmartSuggestion("n(", "ln(", "Natural log", Icons.Default.Functions, SuggestionCategory.FUNCTION))
            }
            lastChar == 's' -> {
                suggestions.add(SmartSuggestion("in(", "sin(", "Sine", Icons.Default.Functions, SuggestionCategory.FUNCTION))
                suggestions.add(SmartSuggestion("qrt(", "√(", "Square root", Icons.Default.Functions, SuggestionCategory.FUNCTION))
            }
            lastChar == 'c' -> {
                suggestions.add(SmartSuggestion("os(", "cos(", "Cosine", Icons.Default.Functions, SuggestionCategory.FUNCTION))
            }
            lastChar == 't' -> {
                suggestions.add(SmartSuggestion("an(", "tan(", "Tangent", Icons.Default.Functions, SuggestionCategory.FUNCTION))
            }
        }
        
        // Smart constants
        if (lastChar == 'p' && !currentExpression.endsWith("exp")) {
            suggestions.add(SmartSuggestion("i", "π", "Pi constant", Icons.Default.Circle, SuggestionCategory.CONSTANT))
        }
        if (lastChar == 'e' && currentExpression.length == 1) {
            suggestions.add(SmartSuggestion("", "e", "Euler's number", Icons.Default.Science, SuggestionCategory.CONSTANT))
        }
        
        // Pattern recognition with improved logic
        if (isNumberPattern(currentExpression)) {
            suggestions.addAll(getNumberPatternSuggestions(currentExpression))
        }
        
        // Smart parentheses - always suggest closing if needed
        val openCount = currentExpression.count { it == '(' }
        val closeCount = currentExpression.count { it == ')' }
        if (openCount > closeCount && endsWithNumberOrConstant(currentExpression)) {
            suggestions.add(SmartSuggestion(")", ")", "Close parenthesis", Icons.Default.Close, SuggestionCategory.COMPLETION))
        }
        
        // Common operations for numbers
        if (endsWithNumberOrConstant(currentExpression)) {
            suggestions.addAll(getContextualOperations(currentExpression))
        }
        
        return suggestions.take(4) // Limit to 4 for better layout
    }
    
    private fun getStarterSuggestions(): List<SmartSuggestion> {
        return listOf(
            SmartSuggestion("π", "π", "Pi constant", Icons.Default.Circle, SuggestionCategory.CONSTANT),
            SmartSuggestion("e", "e", "Euler's number", Icons.Default.Science, SuggestionCategory.CONSTANT),
            SmartSuggestion("sin(", "sin(", "Sine function", Icons.Default.Functions, SuggestionCategory.FUNCTION),
            SmartSuggestion("√(", "√(", "Square root", Icons.Default.Functions, SuggestionCategory.FUNCTION),
            SmartSuggestion("log(", "log(", "Logarithm", Icons.Default.Functions, SuggestionCategory.FUNCTION)
        )
    }
    
    private fun isNumberPattern(expression: String): Boolean {
        val lastToken = expression.split(Regex("[+\\-×÷()]")).lastOrNull()
        return lastToken?.matches(Regex("\\d+")) == true
    }
    
    private fun getNumberPatternSuggestions(expression: String): List<SmartSuggestion> {
        val lastNumber = expression.split(Regex("[+\\-×÷()]")).lastOrNull()?.toIntOrNull() ?: return emptyList()
        
        val suggestions = mutableListOf<SmartSuggestion>()
        
        // Perfect square suggestions
        val sqrt = kotlin.math.sqrt(lastNumber.toDouble())
        if (sqrt == sqrt.toInt().toDouble()) {
            suggestions.add(SmartSuggestion("²", "²", "Square this number", Icons.Default.SquareFoot, SuggestionCategory.OPERATION))
        }
        
        // Factorial suggestion for small numbers
        if (lastNumber in 1..10) {
            suggestions.add(SmartSuggestion("!", "!", "Factorial", Icons.Default.ExploreOff, SuggestionCategory.OPERATION))
        }
        
        return suggestions
    }
    
    private fun needsClosingParenthesis(expression: String): Boolean {
        var openCount = 0
        for (char in expression) {
            when (char) {
                '(' -> openCount++
                ')' -> openCount--
            }
        }
        return openCount > 0
    }
    
    private fun endsWithNumber(expression: String): Boolean {
        return expression.lastOrNull()?.isDigit() == true || expression.endsWith("π") || expression.endsWith("e")
    }
    
    private fun endsWithNumberOrConstant(expression: String): Boolean {
        return expression.lastOrNull()?.isDigit() == true || 
               expression.endsWith("π") || 
               expression.endsWith("e") ||
               expression.endsWith(")")
    }
    
    private fun getOperationSuggestions(): List<SmartSuggestion> {
        return listOf(
            SmartSuggestion(" + ", "+", "Addition", Icons.Default.Add, SuggestionCategory.OPERATION),
            SmartSuggestion(" × ", "×", "Multiplication", Icons.Default.Close, SuggestionCategory.OPERATION),
            SmartSuggestion(" ÷ ", "÷", "Division", Icons.Default.Remove, SuggestionCategory.OPERATION),
            SmartSuggestion("²", "²", "Square", Icons.Default.SquareFoot, SuggestionCategory.OPERATION)
        )
    }
    
    private fun getContextualOperations(expression: String): List<SmartSuggestion> {
        val lastNumber = getLastNumber(expression)
        val suggestions = mutableListOf<SmartSuggestion>()
        
        // Basic operations
        suggestions.add(SmartSuggestion(" + ", "+", "Add", Icons.Default.Add, SuggestionCategory.OPERATION))
        suggestions.add(SmartSuggestion(" × ", "×", "Multiply", Icons.Default.Close, SuggestionCategory.OPERATION))
        
        // Smart operations based on context
        if (lastNumber != null) {
            if (lastNumber > 0) {
                suggestions.add(SmartSuggestion("²", "²", "Square", Icons.Default.SquareFoot, SuggestionCategory.OPERATION))
            }
            if (lastNumber in 1.0..10.0 && lastNumber.toInt().toDouble() == lastNumber) {
                suggestions.add(SmartSuggestion("!", "!", "Factorial", Icons.Default.Functions, SuggestionCategory.OPERATION))
            }
        }
        
        return suggestions.take(2) // Keep it compact
    }
    
    private fun getLastNumber(expression: String): Double? {
        val tokens = expression.split(Regex("[+\\-×÷()]"))
        val lastToken = tokens.lastOrNull()?.trim()
        return when {
            lastToken == "π" -> kotlin.math.PI
            lastToken == "e" -> kotlin.math.E
            lastToken?.toDoubleOrNull() != null -> lastToken.toDoubleOrNull()
            else -> null
        }
    }
}

@Composable
fun IntelligentSuggestionsBar(
    expression: String,
    onSuggestionClick: (SmartSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions by remember(expression) {
        derivedStateOf { IntelligentInputAssistant.getSuggestions(expression) }
    }
    
    AnimatedVisibility(
        visible = suggestions.isNotEmpty(),
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Smart suggestions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
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
    }
}

@Composable
private fun SuggestionChip(
    suggestion: SmartSuggestion,
    onClick: () -> Unit
) {
    val categoryColor = when (suggestion.category) {
        SuggestionCategory.FUNCTION -> MaterialTheme.colorScheme.primaryContainer
        SuggestionCategory.CONSTANT -> MaterialTheme.colorScheme.tertiaryContainer
        SuggestionCategory.OPERATION -> MaterialTheme.colorScheme.secondaryContainer
        SuggestionCategory.COMPLETION -> MaterialTheme.colorScheme.surfaceVariant
        SuggestionCategory.PATTERN -> MaterialTheme.colorScheme.errorContainer
    }
    
    val contentColor = when (suggestion.category) {
        SuggestionCategory.FUNCTION -> MaterialTheme.colorScheme.onPrimaryContainer
        SuggestionCategory.CONSTANT -> MaterialTheme.colorScheme.onTertiaryContainer
        SuggestionCategory.OPERATION -> MaterialTheme.colorScheme.onSecondaryContainer
        SuggestionCategory.COMPLETION -> MaterialTheme.colorScheme.onSurfaceVariant
        SuggestionCategory.PATTERN -> MaterialTheme.colorScheme.onErrorContainer
    }
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = categoryColor,
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = suggestion.icon,
                contentDescription = suggestion.description,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = suggestion.displayText,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}