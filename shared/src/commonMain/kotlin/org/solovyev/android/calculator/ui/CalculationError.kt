package org.solovyev.android.calculator.ui

/**
 * Sealed class representing different types of calculation errors.
 * Provides user-friendly error messages and optional suggestions.
 */
sealed class CalculationError(
    open val userMessage: String,
    open val suggestion: String? = null
) {
    /**
     * Division by zero error
     */
    data class DivisionByZero(
        override val suggestion: String? = null
    ) : CalculationError("Can't divide by zero", suggestion)

    /**
     * Invalid mathematical expression
     */
    data class InvalidExpression(
        val details: String? = null,
        override val suggestion: String? = null
    ) : CalculationError(details ?: "Check your expression", suggestion)

    /**
     * Numeric overflow - result too large
     */
    data class Overflow(
        override val suggestion: String? = "Try a smaller calculation"
    ) : CalculationError("Number too large", suggestion)

    /**
     * Underflow - number too small
     */
    data class Underflow(
        override val suggestion: String? = "Result is too small to represent"
    ) : CalculationError("Number too small", suggestion)

    /**
     * Invalid numeric format (e.g., multiple decimals)
     */
    data class InvalidNumberFormat(
        override val suggestion: String? = null
    ) : CalculationError("Invalid number format", suggestion)

    /**
     * Domain error (e.g., sqrt of negative, log of non-positive)
     */
    data class DomainError(
        val operation: String,
        override val suggestion: String? = null
    ) : CalculationError("Can't $operation", suggestion)

    /**
     * Generic calculation error
     */
    data class Generic(
        val originalMessage: String,
        override val suggestion: String? = null
    ) : CalculationError("Something went wrong", suggestion)

    companion object {
        /**
         * Parses an error message and returns the appropriate CalculationError type
         */
        fun fromMessage(message: String): CalculationError {
            return when {
                message.contains("division by zero", ignoreCase = true) ||
                    message.contains("/0", ignoreCase = true) ->
                    DivisionByZero(suggestion = "Try dividing by a non-zero number")

                message.contains("overflow", ignoreCase = true) ||
                    message.contains("too large", ignoreCase = true) ->
                    Overflow()

                message.contains("underflow", ignoreCase = true) ||
                    message.contains("too small", ignoreCase = true) ->
                    Underflow()

                message.contains("sqrt", ignoreCase = true) &&
                    message.contains("negative", ignoreCase = true) ->
                    DomainError("take square root of negative", "Try using absolute value")

                message.contains("log", ignoreCase = true) &&
                    (message.contains("non-positive", ignoreCase = true) ||
                        message.contains("zero", ignoreCase = true) ||
                        message.contains("negative", ignoreCase = true)) ->
                    DomainError("take logarithm of non-positive number", "Use a positive number")

                message.contains("syntax", ignoreCase = true) ||
                    message.contains("parse", ignoreCase = true) ->
                    InvalidExpression("Check your expression syntax")

                message.contains("unmatched", ignoreCase = true) ||
                    message.contains("parentheses", ignoreCase = true) ||
                    message.contains("bracket", ignoreCase = true) ->
                    InvalidExpression("Check your parentheses", "Make sure all parentheses are closed")

                else -> Generic(message)
            }
        }
    }
}
