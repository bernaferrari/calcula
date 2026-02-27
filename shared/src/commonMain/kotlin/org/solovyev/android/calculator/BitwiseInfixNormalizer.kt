package org.solovyev.android.calculator

internal object BitwiseInfixNormalizer {
    private val precedenceLevels = listOf(
        listOf("|"),
        listOf("^"),
        listOf("&"),
        listOf("<<", ">>")
    )

    fun normalize(expression: String): String {
        if (!containsBitwiseCandidates(expression)) return expression
        return normalizeSegment(expression)
    }

    private fun normalizeSegment(segment: String): String {
        val trimmed = segment.trim()
        if (trimmed.isEmpty()) return trimmed

        val unwrapped = unwrapOuterGroup(trimmed)
        if (unwrapped != null) {
            return normalizeSegment(unwrapped)
        }

        for (level in precedenceLevels) {
            val split = splitAtTopLevelOperators(trimmed, level) ?: continue
            val normalizedParts = split.parts.map { normalizeSegment(it) }
            var acc = normalizedParts.first()
            for (index in split.operators.indices) {
                val rhs = normalizedParts[index + 1]
                acc = when (split.operators[index]) {
                    "&" -> "and($acc,$rhs)"
                    "|" -> "or($acc,$rhs)"
                    "^" -> "xor($acc,$rhs)"
                    "<<" -> "shl($acc,$rhs)"
                    ">>" -> "shr($acc,$rhs)"
                    else -> return trimmed
                }
            }
            return acc
        }

        val unary = leadingUnaryNot(trimmed)
        if (unary.count > 0) {
            val rest = trimmed.substring(unary.nextIndex).trimStart()
            if (rest.isEmpty()) return trimmed
            var normalized = normalizeSegment(rest)
            repeat(unary.count) {
                normalized = "not($normalized)"
            }
            return normalized
        }

        return trimmed
    }

    private fun containsBitwiseCandidates(expression: String): Boolean {
        if (expression.indexOf('&') >= 0) return true
        if (expression.indexOf('|') >= 0) return true
        if (expression.indexOf('~') >= 0) return true
        if (expression.indexOf("<<") >= 0) return true
        if (expression.indexOf(">>") >= 0) return true
        return expression.indices.any { index ->
            expression[index] == '^' && isBitwiseCaret(expression, index)
        }
    }

    private data class SplitResult(
        val parts: List<String>,
        val operators: List<String>
    )

    private fun splitAtTopLevelOperators(expression: String, operators: List<String>): SplitResult? {
        var depth = 0
        var i = 0
        var start = 0
        val parts = mutableListOf<String>()
        val ops = mutableListOf<String>()

        while (i < expression.length) {
            val ch = expression[i]
            when (ch) {
                '(', '[', '{' -> depth += 1
                ')', ']', '}' -> depth = (depth - 1).coerceAtLeast(0)
            }

            if (depth == 0) {
                val operator = operators.firstOrNull { matchesOperator(expression, i, it) }
                if (operator != null) {
                    parts += expression.substring(start, i)
                    ops += operator
                    i += operator.length
                    start = i
                    continue
                }
            }
            i += 1
        }

        if (ops.isEmpty()) return null

        parts += expression.substring(start)
        if (parts.any { it.trim().isEmpty() }) return null

        return SplitResult(parts = parts, operators = ops)
    }

    private fun matchesOperator(expression: String, index: Int, operator: String): Boolean {
        return when (operator) {
            "<<" -> expression.startsWith("<<", index)
            ">>" -> expression.startsWith(">>", index)
            "&" -> expression[index] == '&' &&
                !expression.startsWith("&&", index) &&
                (index == 0 || expression[index - 1] != '&')
            "|" -> expression[index] == '|' &&
                !expression.startsWith("||", index) &&
                (index == 0 || expression[index - 1] != '|')
            "^" -> expression[index] == '^' && isBitwiseCaret(expression, index)
            else -> false
        }
    }

    private fun isBitwiseCaret(expression: String, index: Int): Boolean {
        if (index <= 0 || index >= expression.lastIndex) return false
        return expression[index - 1].isWhitespace() && expression[index + 1].isWhitespace()
    }

    private fun unwrapOuterGroup(expression: String): String? {
        val open = expression.firstOrNull() ?: return null
        val close = when (open) {
            '(' -> ')'
            '[' -> ']'
            '{' -> '}'
            else -> return null
        }
        if (expression.lastOrNull() != close) return null

        var depth = 0
        expression.forEachIndexed { index, ch ->
            if (ch == open) depth += 1
            if (ch == close) depth -= 1
            if (depth == 0 && index < expression.lastIndex) return null
            if (depth < 0) return null
        }
        if (depth != 0) return null

        return expression.substring(1, expression.lastIndex)
    }

    private data class UnaryPrefix(val count: Int, val nextIndex: Int)

    private fun leadingUnaryNot(expression: String): UnaryPrefix {
        var index = 0
        var count = 0

        while (index < expression.length) {
            while (index < expression.length && expression[index].isWhitespace()) {
                index += 1
            }
            if (index < expression.length && expression[index] == '~') {
                count += 1
                index += 1
            } else {
                break
            }
        }

        return UnaryPrefix(count = count, nextIndex = index)
    }
}
