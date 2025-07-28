package com.bernaferrari.calculator.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.calculator.domain.calculator.ExpressionEvaluator
import com.bernaferrari.calculator.domain.calculator.CalculatorButtonConfig
import com.bernaferrari.calculator.domain.calculator.TokenizedExpression
import com.bernaferrari.calculator.domain.calculator.TokenType
import com.bernaferrari.calculator.domain.model.*
import com.bernaferrari.calculator.domain.repository.CalculatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val repository: CalculatorRepository,
    private val evaluator: ExpressionEvaluator
) : ViewModel() {
    
    private val _state = MutableStateFlow(CalculatorState())
    val state = _state.asStateFlow()
    
    private var openParenthesesCount = 0
    private var tokenizedExpression = TokenizedExpression()
    private var cursorPosition = 0
    
    // Undo/Redo stacks
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private var lastSavedExpression = ""
    
    // Memory functions
    private var memoryValue = BigDecimal.ZERO
    private var hasMemoryValue = false
    
    init {
        loadHistory()
    }
    
    fun onButtonClick(button: CalculatorButton) {
        when (button.function) {
            ButtonFunction.ZERO -> appendToExpression("0")
            ButtonFunction.ONE -> appendToExpression("1")
            ButtonFunction.TWO -> appendToExpression("2")
            ButtonFunction.THREE -> appendToExpression("3")
            ButtonFunction.FOUR -> appendToExpression("4")
            ButtonFunction.FIVE -> appendToExpression("5")
            ButtonFunction.SIX -> appendToExpression("6")
            ButtonFunction.SEVEN -> appendToExpression("7")
            ButtonFunction.EIGHT -> appendToExpression("8")
            ButtonFunction.NINE -> appendToExpression("9")
            
            ButtonFunction.DOT -> appendDecimalPoint()
            ButtonFunction.PLUS -> appendOperator(" + ")
            ButtonFunction.MINUS -> appendOperator(" - ")
            ButtonFunction.MULTIPLY -> appendOperator(" × ")
            ButtonFunction.DIVIDE -> appendOperator(" ÷ ")
            ButtonFunction.PERCENT -> appendOperator(" % ")
            
            ButtonFunction.EQUALS -> calculateResult()
            ButtonFunction.CLEAR -> clearAll()
            ButtonFunction.BACKSPACE -> backspace()
            
            ButtonFunction.PARENTHESIS -> handleParenthesis()
            ButtonFunction.PI -> appendToExpression("π")
            ButtonFunction.E -> appendToExpression("e")
            
            // Memory functions
            ButtonFunction.MEMORY_STORE -> memoryStore()
            ButtonFunction.MEMORY_RECALL -> memoryRecall()
            ButtonFunction.MEMORY_CLEAR -> memoryClear()
            ButtonFunction.MEMORY_PLUS -> memoryPlus()
            ButtonFunction.MEMORY_MINUS -> memoryMinus()
            ButtonFunction.ANS -> appendToExpression(_state.value.lastAnswer)
            
            ButtonFunction.SIN -> appendFunction("sin(")
            ButtonFunction.COS -> appendFunction("cos(")
            ButtonFunction.TAN -> appendFunction("tan(")
            ButtonFunction.LN -> appendFunction("ln(")
            ButtonFunction.LOG -> appendFunction("log(")
            ButtonFunction.SQRT -> appendFunction("√(")
            ButtonFunction.SQUARE -> {
                // For x², wrap the current expression in parentheses and add ²
                val expr = _state.value.currentExpression
                if (expr.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        currentExpression = "($expr)²"
                    )
                    evaluateExpression("($expr)²", isPreview = true)
                } else {
                    appendToExpression("²")
                }
            }
            
            else -> { /* Handle other functions */ }
        }
    }
    
    fun onSecondaryFunction(button: CalculatorButton) {
        val secondaryFunction = CalculatorButtonConfig.getSecondaryFunction(button.id)
        when (secondaryFunction) {
            ButtonFunction.SIN -> appendFunction("sin(")
            ButtonFunction.COS -> appendFunction("cos(")
            ButtonFunction.TAN -> appendFunction("tan(")
            ButtonFunction.LN -> appendFunction("ln(")
            ButtonFunction.LOG -> appendFunction("log(")
            ButtonFunction.SQRT -> appendFunction("√(")
            ButtonFunction.PI -> appendToExpression("π")
            ButtonFunction.E -> appendToExpression("e")
            ButtonFunction.X_VAR -> appendToExpression("x")
            else -> onButtonClick(button)
        }
    }
    
    private fun appendToExpression(text: String) {
        // Check if we need implicit multiplication
        val needsImplicitMultiply = shouldAddImplicitMultiply(text)
        
        if (needsImplicitMultiply) {
            tokenizedExpression.addOperator("×")
        }
        
        // Update tokenized expression
        when {
            text in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".") -> {
                tokenizedExpression.addNumber(text)
            }
            text in listOf("π", "e") -> {
                tokenizedExpression.addConstant(text)
            }
            text == "ANS" -> {
                tokenizedExpression.addConstant("ANS")
            }
        }
        
        val newExpression = tokenizedExpression.toString()
        cursorPosition = newExpression.length
        
        _state.value = _state.value.copy(
            currentExpression = newExpression,
            cursorPosition = cursorPosition,
            isError = false,
            errorMessage = ""
        )
        
        // Save for undo
        saveForUndo()
        
        // Real-time calculation preview
        evaluateExpression(newExpression, isPreview = true)
    }
    
    private fun appendOperator(operator: String) {
        val currentExpression = _state.value.currentExpression
        
        // Prevent multiple consecutive operators
        if (currentExpression.isNotEmpty() && !currentExpression.last().isDigit() && 
            currentExpression.last() != ')' && currentExpression.last() != 'π' && 
            currentExpression.last() != 'e') {
            return
        }
        
        tokenizedExpression.addOperator(operator.trim())
        
        val newExpression = tokenizedExpression.toString()
        cursorPosition = newExpression.length
        
        _state.value = _state.value.copy(
            currentExpression = newExpression,
            cursorPosition = cursorPosition,
            isError = false,
            errorMessage = ""
        )
        
        evaluateExpression(newExpression, isPreview = true)
    }
    
    private fun appendDecimalPoint() {
        val currentExpression = _state.value.currentExpression
        
        // Find the last number in the expression
        val lastNumberStart = currentExpression.indexOfLast { 
            it in listOf('+', '-', '×', '÷', '(', ' ') 
        } + 1
        
        val lastNumber = if (lastNumberStart < currentExpression.length) {
            currentExpression.substring(lastNumberStart).trim()
        } else ""
        
        // Only add decimal point if the last number doesn't already have one
        if (!lastNumber.contains('.')) {
            appendToExpression(".")
        }
    }
    
    private fun appendFunction(functionName: String) {
        // Check if we need implicit multiplication
        if (shouldAddImplicitMultiply(functionName)) {
            tokenizedExpression.addOperator("×")
        }
        
        tokenizedExpression.addFunction(functionName.removeSuffix("("))
        tokenizedExpression.addParenthesisOpen()
        openParenthesesCount++
        
        val newExpression = tokenizedExpression.toString()
        cursorPosition = newExpression.length
        
        _state.value = _state.value.copy(
            currentExpression = newExpression,
            cursorPosition = cursorPosition,
            isError = false,
            errorMessage = ""
        )
        
        evaluateExpression(newExpression, isPreview = true)
    }
    
    private fun handleParenthesis() {
        val currentExpression = _state.value.currentExpression
        
        // If empty or after operator, add both parentheses
        if (currentExpression.isEmpty() || 
            currentExpression.last() in listOf('+', '-', '×', '÷', '(')) {
            
            tokenizedExpression.addParenthesisOpen()
            tokenizedExpression.addParenthesisClose()
            
            // Move cursor between parentheses
            val newExpression = tokenizedExpression.toString()
            cursorPosition = newExpression.length - 1
            
            _state.value = _state.value.copy(
                currentExpression = newExpression,
                cursorPosition = cursorPosition,
                isError = false,
                errorMessage = ""
            )
            
            saveForUndo()
            evaluateExpression(newExpression, isPreview = true)
            return
        }
        
        // Otherwise handle smart parenthesis
        val shouldAddClosing = openParenthesesCount > 0 && 
            (currentExpression.last().isDigit() || 
             currentExpression.last() in listOf(')', 'π', 'e'))
        
        if (shouldAddClosing) {
            tokenizedExpression.addParenthesisClose()
            openParenthesesCount--
        } else {
            // Check if we need implicit multiplication before opening parenthesis
            if (shouldAddImplicitMultiply("(")) {
                tokenizedExpression.addOperator("×")
            }
            tokenizedExpression.addParenthesisOpen()
            openParenthesesCount++
        }
        
        val newExpression = tokenizedExpression.toString()
        cursorPosition = newExpression.length
        
        _state.value = _state.value.copy(
            currentExpression = newExpression,
            cursorPosition = cursorPosition,
            isError = false,
            errorMessage = ""
        )
        
        evaluateExpression(newExpression, isPreview = true)
    }
    
    private fun backspace() {
        deleteAtCursor()
    }
    
    private fun clearAll() {
        openParenthesesCount = 0
        tokenizedExpression = TokenizedExpression()
        cursorPosition = 0
        _state.value = CalculatorState()
    }
    
    private fun calculateResult() {
        val expression = _state.value.currentExpression
        if (expression.isEmpty()) return
        
        // Auto-close any open parentheses
        val finalExpression = expression + ")".repeat(openParenthesesCount)
        
        evaluateExpression(finalExpression, isPreview = false)
    }
    
    private fun evaluateExpression(expression: String, isPreview: Boolean) {
        if (expression.isEmpty()) {
            _state.value = _state.value.copy(currentResult = "0")
            return
        }
        
        evaluator.evaluate(expression)
            .onSuccess { result ->
                val formattedResult = formatResult(result)
                
                _state.value = _state.value.copy(
                    currentResult = formattedResult,
                    isError = false,
                    errorMessage = ""
                )
                
                if (!isPreview) {
                    // Save to history and update last answer
                    saveToHistory(expression, formattedResult)
                    evaluator.setVariable("ans", result)
                    
                    // Clear expression after calculation
                    _state.value = _state.value.copy(
                        currentExpression = "",
                        lastAnswer = formattedResult
                    )
                    openParenthesesCount = 0
                }
            }
            .onFailure { error ->
                if (!isPreview) {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = when (error) {
                            is ArithmeticException -> "Math Error"
                            is IllegalArgumentException -> "Syntax Error"
                            else -> "Error"
                        }
                    )
                }
            }
    }
    
    private fun formatResult(result: BigDecimal): String {
        return try {
            // Remove trailing zeros and decimal point if not needed
            val stripped = result.stripTrailingZeros()
            val plainString = stripped.toPlainString()
            
            when {
                // For very large numbers, use scientific notation
                plainString.length > 15 && stripped.abs() >= BigDecimal("1E12") -> {
                    result.toString() // This will use scientific notation
                }
                // For very small numbers, use scientific notation
                stripped.abs() < BigDecimal("0.0001") && stripped.compareTo(BigDecimal.ZERO) != 0 -> {
                    result.toString() // This will use scientific notation
                }
                // For normal numbers, use plain string
                else -> plainString
            }
        } catch (e: Exception) {
            "Error"
        }
    }
    
    private fun saveToHistory(expression: String, result: String) {
        viewModelScope.launch {
            val history = CalculationHistory(
                expression = expression,
                result = result,
                timestamp = System.currentTimeMillis()
            )
            repository.insertCalculation(history)
        }
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            repository.getRecentHistory().collect { history ->
                _state.value = _state.value.copy(history = history)
            }
        }
    }
    
    fun toggleInverseMode() {
        _state.value = _state.value.copy(
            isInverseMode = !_state.value.isInverseMode
        )
    }
    
    fun toggleAngleUnit() {
        val newUnit = if (_state.value.angleUnit == AngleUnit.RADIANS) {
            AngleUnit.DEGREES
        } else {
            AngleUnit.RADIANS
        }
        
        _state.value = _state.value.copy(angleUnit = newUnit)
    }
    
    // Memory functions implementation
    private fun memoryStore() {
        val currentResult = _state.value.currentResult
        if (currentResult != "0" && !_state.value.isError) {
            try {
                memoryValue = BigDecimal(currentResult)
                hasMemoryValue = true
                _state.value = _state.value.copy(hasMemoryValue = true)
            } catch (e: Exception) {
                // Invalid number, don't store
            }
        }
    }
    
    private fun memoryRecall() {
        if (hasMemoryValue) {
            val formattedMemory = formatResult(memoryValue)
            appendToExpression(formattedMemory)
        }
    }
    
    private fun memoryClear() {
        memoryValue = BigDecimal.ZERO
        hasMemoryValue = false
        _state.value = _state.value.copy(hasMemoryValue = false)
    }
    
    private fun memoryPlus() {
        val currentResult = _state.value.currentResult
        if (currentResult != "0" && !_state.value.isError) {
            try {
                val currentValue = BigDecimal(currentResult)
                memoryValue = memoryValue.add(currentValue)
                hasMemoryValue = true
                _state.value = _state.value.copy(hasMemoryValue = true)
            } catch (e: Exception) {
                // Invalid number, don't add
            }
        }
    }
    
    private fun memoryMinus() {
        val currentResult = _state.value.currentResult
        if (currentResult != "0" && !_state.value.isError) {
            try {
                val currentValue = BigDecimal(currentResult)
                memoryValue = memoryValue.subtract(currentValue)
                hasMemoryValue = true
                _state.value = _state.value.copy(hasMemoryValue = true)
            } catch (e: Exception) {
                // Invalid number, don't subtract
            }
        }
    }
    
    fun undo() {
        if (undoStack.isNotEmpty()) {
            // Save current state to redo stack
            redoStack.add(_state.value.currentExpression)
            
            // Pop from undo stack
            val previousExpression = undoStack.removeAt(undoStack.lastIndex)
            
            // Update expression without saving to undo
            tokenizedExpression = TokenizedExpression.parse(previousExpression)
            cursorPosition = previousExpression.length
            lastSavedExpression = previousExpression
            
            _state.value = _state.value.copy(
                currentExpression = previousExpression,
                cursorPosition = cursorPosition,
                isError = false,
                errorMessage = ""
            )
            
            if (previousExpression.isNotEmpty()) {
                evaluateExpression(previousExpression, isPreview = true)
            } else {
                _state.value = _state.value.copy(currentResult = "0")
            }
        }
    }
    
    fun redo() {
        if (redoStack.isNotEmpty()) {
            // Save current state to undo stack
            undoStack.add(_state.value.currentExpression)
            
            // Pop from redo stack
            val nextExpression = redoStack.removeAt(redoStack.lastIndex)
            
            // Update expression without saving to undo
            tokenizedExpression = TokenizedExpression.parse(nextExpression)
            cursorPosition = nextExpression.length
            lastSavedExpression = nextExpression
            
            _state.value = _state.value.copy(
                currentExpression = nextExpression,
                cursorPosition = cursorPosition,
                isError = false,
                errorMessage = ""
            )
            
            if (nextExpression.isNotEmpty()) {
                evaluateExpression(nextExpression, isPreview = true)
            } else {
                _state.value = _state.value.copy(currentResult = "0")
            }
        }
    }
    
    private fun saveForUndo() {
        val currentExpression = _state.value.currentExpression
        
        // Only save if expression has changed significantly
        if (currentExpression != lastSavedExpression && 
            (currentExpression.isEmpty() || 
             lastSavedExpression.isEmpty() ||
             kotlin.math.abs(currentExpression.length - lastSavedExpression.length) > 2)) {
            
            if (lastSavedExpression.isNotEmpty()) {
                undoStack.add(lastSavedExpression)
                // Limit undo stack size
                if (undoStack.size > 50) {
                    undoStack.removeAt(0)
                }
            }
            
            lastSavedExpression = currentExpression
            redoStack.clear() // Clear redo stack on new input
        }
    }
    
    private fun shouldAddImplicitMultiply(nextInput: String): Boolean {
        val lastToken = tokenizedExpression.getTokens().lastOrNull() ?: return false
        
        // Add implicit multiply after numbers before constants, functions, or opening parenthesis
        if (lastToken.type == TokenType.NUMBER) {
            return nextInput in listOf("π", "e", "(") || 
                   nextInput.matches(Regex("[a-zA-Z]+")) // Functions
        }
        
        // Add implicit multiply after closing parenthesis
        if (lastToken.type == TokenType.PARENTHESIS_CLOSE) {
            return nextInput in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "π", "e", "(") ||
                   nextInput.matches(Regex("[a-zA-Z]+")) // Functions
        }
        
        // Add implicit multiply after constants
        if (lastToken.type == TokenType.CONSTANT) {
            return nextInput in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "(") ||
                   nextInput.matches(Regex("[a-zA-Z]+")) // Functions
        }
        
        return false
    }
    
    fun updateExpression(expression: String) {
        // Parse the expression into tokens
        tokenizedExpression = TokenizedExpression.parse(expression)
        
        _state.value = _state.value.copy(
            currentExpression = expression,
            isError = false,
            errorMessage = ""
        )
        
        // Real-time calculation preview
        if (expression.isNotEmpty()) {
            evaluateExpression(expression, isPreview = true)
        } else {
            _state.value = _state.value.copy(currentResult = "0")
        }
        
        // Update parentheses count
        openParenthesesCount = expression.count { it == '(' } - expression.count { it == ')' }
    }
    
    fun updateCursorPosition(position: Int) {
        cursorPosition = position
    }
    
    fun moveCursorLeft() {
        if (cursorPosition > 0) {
            cursorPosition = tokenizedExpression.getPreviousTokenPosition(cursorPosition)
            _state.value = _state.value.copy(cursorPosition = cursorPosition)
        }
    }
    
    fun moveCursorRight() {
        val expression = _state.value.currentExpression
        if (cursorPosition < expression.length) {
            cursorPosition = tokenizedExpression.getNextTokenPosition(cursorPosition)
            _state.value = _state.value.copy(cursorPosition = cursorPosition)
        }
    }
    
    fun deleteAtCursor() {
        if (_state.value.currentExpression.isNotEmpty()) {
            tokenizedExpression.deleteLastCharacter()
            
            val newExpression = tokenizedExpression.toString()
            cursorPosition = newExpression.length
            
            _state.value = _state.value.copy(
                currentExpression = newExpression,
                cursorPosition = cursorPosition,
                isError = false,
                errorMessage = ""
            )
            
            saveForUndo()
            
            if (newExpression.isNotEmpty()) {
                evaluateExpression(newExpression, isPreview = true)
            } else {
                _state.value = _state.value.copy(currentResult = "0")
            }
        }
    }
}