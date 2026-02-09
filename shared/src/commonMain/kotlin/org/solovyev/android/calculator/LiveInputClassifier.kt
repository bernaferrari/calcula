package org.solovyev.android.calculator

/**
 * Classifies editor input to decide how live evaluation should behave.
 */
object LiveInputClassifier {
    private val numberRegex = Regex("""^[+-]?\d+(\.\d+)?$""")
    private val trailingOperatorRegex = Regex("""[+\-×÷^%*/.]$""")
    private val trailingOpenParenRegex = Regex("""\($""")
    private val invalidEndRegex = Regex("""[+\-×÷^%*/.(]$""")

    sealed class Result {
        data object Empty : Result()
        data class NumberOnly(val normalized: String) : Result()
        data class Expression(val normalized: String) : Result()
        data object Incomplete : Result()
    }

    fun classify(rawText: String): Result {
        val text = rawText.trim()
        if (text.isEmpty()) return Result.Empty

        val normalized = normalizeMultiplication(text)

        if (numberRegex.matches(normalized)) {
            return Result.NumberOnly(normalized)
        }

        if (trailingOperatorRegex.containsMatchIn(normalized) ||
            trailingOpenParenRegex.containsMatchIn(normalized) ||
            !parenthesesBalanced(normalized) ||
            invalidEndRegex.containsMatchIn(normalized)
        ) {
            return Result.Incomplete
        }

        return Result.Expression(normalized)
    }

    private fun normalizeMultiplication(text: String): String {
        if (text.indexOf('x', ignoreCase = true) == -1) return text
        var normalized = text
        normalized = normalized.replace(Regex("(\\d)\\s*[xX]\\s*(\\d)"), "$1×$2")
        normalized = normalized.replace(Regex("(\\d)\\s*[xX]\\s*\\("), "$1×(")
        normalized = normalized.replace(Regex("\\)\\s*[xX]\\s*(\\d)"), ")×$1")
        normalized = normalized.replace(Regex("\\)\\s*[xX]\\s*\\("), ")×(")
        return normalized
    }

    private fun parenthesesBalanced(text: String): Boolean {
        var balance = 0
        for (c in text) {
            when (c) {
                '(' -> balance++
                ')' -> {
                    balance--
                    if (balance < 0) return false
                }
            }
        }
        return balance == 0
    }
}
