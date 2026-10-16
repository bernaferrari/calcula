package com.bernaferrari.calculator.domain.calculator

data class Token(
    val value: String,
    val type: TokenType,
    val startIndex: Int,
    val endIndex: Int,
)

enum class TokenType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    PARENTHESIS_OPEN,
    PARENTHESIS_CLOSE,
    CONSTANT,
    VARIABLE,
}

class TokenizedExpression {
    private val tokens = mutableListOf<Token>()

    fun addNumber(digit: String) {
        // Check if the last token is a number, if so append to it
        val lastToken = tokens.lastOrNull()
        if (lastToken != null && lastToken.type == TokenType.NUMBER) {
            // Update the existing number token
            val newValue = lastToken.value + digit
            tokens[tokens.size - 1] =
                lastToken.copy(
                    value = newValue,
                    endIndex = lastToken.startIndex + newValue.length,
                )
        } else {
            // Add new number token
            val startIndex = getEndIndex()
            tokens.add(Token(digit, TokenType.NUMBER, startIndex, startIndex + digit.length))
        }
    }

    fun addOperator(operator: String) {
        val startIndex = getEndIndex()
        tokens.add(Token(operator, TokenType.OPERATOR, startIndex, startIndex + operator.length))
    }

    fun addFunction(function: String) {
        val startIndex = getEndIndex()
        tokens.add(Token(function, TokenType.FUNCTION, startIndex, startIndex + function.length))
    }

    fun addParenthesisOpen() {
        val startIndex = getEndIndex()
        tokens.add(Token("(", TokenType.PARENTHESIS_OPEN, startIndex, startIndex + 1))
    }

    fun addParenthesisClose() {
        val startIndex = getEndIndex()
        tokens.add(Token(")", TokenType.PARENTHESIS_CLOSE, startIndex, startIndex + 1))
    }

    fun addConstant(constant: String) {
        val startIndex = getEndIndex()
        tokens.add(Token(constant, TokenType.CONSTANT, startIndex, startIndex + constant.length))
    }

    fun deleteTokenAt(position: Int) {
        val tokenIndex = getTokenIndexAt(position)
        if (tokenIndex != -1) {
            tokens.removeAt(tokenIndex)
            updateIndices()
        }
    }

    fun deleteLastCharacter() {
        val lastToken = tokens.lastOrNull() ?: return

        // For any token with multiple characters, remove just the last character
        if (lastToken.value.length > 1) {
            tokens[tokens.size - 1] =
                lastToken.copy(
                    value = lastToken.value.dropLast(1),
                    endIndex = lastToken.endIndex - 1,
                )
        } else {
            // Remove entire token if it's just one character
            tokens.removeAt(tokens.size - 1)
        }
        updateIndices()
    }

    fun getTokenAt(position: Int): Token? = tokens.find { position >= it.startIndex && position <= it.endIndex }

    fun getTokenIndexAt(position: Int): Int = tokens.indexOfFirst { position >= it.startIndex && position <= it.endIndex }

    fun getCursorPositionAfterToken(position: Int): Int {
        val token = getTokenAt(position)
        return token?.endIndex ?: position
    }

    fun getCursorPositionBeforeToken(position: Int): Int {
        val token = getTokenAt(position)
        return token?.startIndex ?: position
    }

    fun getNextTokenPosition(position: Int): Int {
        val currentToken = getTokenAt(position)
        val nextToken =
            if (currentToken != null) {
                tokens.find { it.startIndex > currentToken.endIndex }
            } else {
                tokens.find { it.startIndex > position }
            }
        return nextToken?.startIndex ?: getEndIndex()
    }

    fun getPreviousTokenPosition(position: Int): Int {
        val currentToken = getTokenAt(position)
        val previousToken =
            if (currentToken != null && position > currentToken.startIndex) {
                currentToken
            } else {
                tokens.findLast { it.endIndex < position }
            }
        return previousToken?.startIndex ?: 0
    }

    override fun toString(): String = tokens.joinToString("") { it.value }

    fun clear() {
        tokens.clear()
    }

    fun isEmpty(): Boolean = tokens.isEmpty()

    fun getTokens(): List<Token> = tokens.toList()

    private fun getEndIndex(): Int = tokens.lastOrNull()?.endIndex ?: 0

    private fun updateIndices() {
        var currentIndex = 0
        tokens.forEach { token ->
            val length = token.value.length
            tokens[tokens.indexOf(token)] =
                token.copy(
                    startIndex = currentIndex,
                    endIndex = currentIndex + length,
                )
            currentIndex += length
        }
    }

    companion object {
        fun parse(expression: String): TokenizedExpression {
            val tokenized = TokenizedExpression()
            var i = 0

            while (i < expression.length) {
                when {
                    expression[i].isWhitespace() -> i++

                    // Numbers
                    expression[i].isDigit() || expression[i] == '.' -> {
                        val start = i
                        while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                            i++
                        }
                        tokenized.addNumber(expression.substring(start, i))
                    }

                    // Functions
                    expression.startsWith("sin", i) -> {
                        tokenized.addFunction("sin")
                        i += 3
                    }
                    expression.startsWith("cos", i) -> {
                        tokenized.addFunction("cos")
                        i += 3
                    }
                    expression.startsWith("tan", i) -> {
                        tokenized.addFunction("tan")
                        i += 3
                    }
                    expression.startsWith("ln", i) -> {
                        tokenized.addFunction("ln")
                        i += 2
                    }
                    expression.startsWith("log", i) -> {
                        tokenized.addFunction("log")
                        i += 3
                    }
                    expression.startsWith("√", i) -> {
                        tokenized.addFunction("√")
                        i += 1
                    }
                    expression.startsWith("ANS", i) -> {
                        tokenized.addConstant("ANS")
                        i += 3
                    }

                    // Constants
                    expression[i] == 'π' -> {
                        tokenized.addConstant("π")
                        i++
                    }
                    expression[i] == 'e' -> {
                        tokenized.addConstant("e")
                        i++
                    }

                    // Operators
                    expression[i] == '+' -> {
                        tokenized.addOperator("+")
                        i++
                    }
                    expression[i] == '-' || expression[i] == '−' -> {
                        tokenized.addOperator("−")
                        i++
                    }
                    expression[i] == '×' || expression[i] == '*' -> {
                        tokenized.addOperator("×")
                        i++
                    }
                    expression[i] == '÷' || expression[i] == '/' -> {
                        tokenized.addOperator("÷")
                        i++
                    }
                    expression[i] == '%' -> {
                        tokenized.addOperator("%")
                        i++
                    }
                    expression[i] == '²' -> {
                        tokenized.addOperator("²")
                        i++
                    }

                    // Parentheses
                    expression[i] == '(' -> {
                        tokenized.addParenthesisOpen()
                        i++
                    }
                    expression[i] == ')' -> {
                        tokenized.addParenthesisClose()
                        i++
                    }

                    else -> i++
                }
            }

            return tokenized
        }
    }
}
