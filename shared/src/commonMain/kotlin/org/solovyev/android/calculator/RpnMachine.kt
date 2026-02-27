package org.solovyev.android.calculator

data class RpnSnapshot(
    val stack: List<String>,
    val entry: String
) {
    val visibleStack: List<String>
        get() = if (entry.isEmpty()) stack else stack + entry

    val x: String
        get() = if (entry.isNotEmpty()) entry else stack.lastOrNull().orEmpty()
}

sealed class RpnActionResult {
    data object Success : RpnActionResult()
    data class Error(val message: String) : RpnActionResult()
}

class RpnMachine(
    private val evaluator: suspend (String) -> String?,
    private val stackLimit: Int = 64
) {
    private val stack = mutableListOf<String>()
    private var entry: String = ""

    fun snapshot(): RpnSnapshot = RpnSnapshot(stack = stack.toList(), entry = entry)

    fun clear() {
        stack.clear()
        entry = ""
    }

    fun setEntry(value: String) {
        entry = value
    }

    fun appendDigit(digit: String) {
        val updated = when {
            digit == "." && entry.contains(".") -> entry
            digit == "." && entry.isEmpty() -> "0."
            entry == "0" && digit != "." -> digit
            else -> entry + digit
        }
        entry = updated
    }

    fun appendRaw(token: String) {
        if (token.isEmpty()) return
        entry += token
    }

    fun backspace(): Boolean {
        if (entry.isNotEmpty()) {
            entry = entry.dropLast(1)
            return true
        }
        if (stack.isNotEmpty()) {
            stack.removeLast()
            return true
        }
        return false
    }

    fun toggleSign() {
        if (entry.isNotEmpty()) {
            entry = if (entry.startsWith("-")) entry.removePrefix("-") else "-$entry"
            return
        }

        if (stack.isEmpty()) return
        val x = stack.removeLast()
        stack.add(if (x.startsWith("-")) x.removePrefix("-") else "-$x")
    }

    suspend fun enter() {
        val e = entry.trim()
        when {
            e.isNotEmpty() -> {
                stack.add(e)
                entry = ""
            }
            stack.isNotEmpty() -> stack.add(stack.last())
        }
        trimStack()
    }

    suspend fun applyBinaryOperator(operator: String): RpnActionResult {
        commitEntry()
        if (stack.size < 2) {
            return RpnActionResult.Error("RPN requires at least two stack values")
        }

        val b = stack.removeLast()
        val a = stack.removeLast()
        val expression = "($a)${normalizeOperator(operator)}($b)"
        val result = evaluateExpression(expression)
        if (result == null) {
            stack.add(a)
            stack.add(b)
            return RpnActionResult.Error("Invalid RPN operation")
        }

        stack.add(result)
        trimStack()
        return RpnActionResult.Success
    }

    suspend fun applyUnaryFunction(function: String): RpnActionResult {
        commitEntry()
        if (stack.isEmpty()) {
            return RpnActionResult.Error("RPN requires at least one stack value")
        }

        val x = stack.removeLast()
        val expression = when (function) {
            "sqrt" -> "sqrt($x)"
            else -> "$function($x)"
        }
        val result = evaluateExpression(expression)
        if (result == null) {
            stack.add(x)
            return RpnActionResult.Error("Invalid RPN function")
        }

        stack.add(result)
        trimStack()
        return RpnActionResult.Success
    }

    suspend fun applyUnaryExpression(template: String): RpnActionResult {
        commitEntry()
        if (stack.isEmpty()) {
            return RpnActionResult.Error("RPN requires at least one stack value")
        }

        val x = stack.removeLast()
        val expression = template.replace("%s", x)
        val result = evaluateExpression(expression)
        if (result == null) {
            stack.add(x)
            return RpnActionResult.Error("Invalid RPN operation")
        }

        stack.add(result)
        trimStack()
        return RpnActionResult.Success
    }

    suspend fun applyBinaryExpression(template: String): RpnActionResult {
        commitEntry()
        if (stack.size < 2) {
            return RpnActionResult.Error("RPN requires at least two stack values")
        }

        val b = stack.removeLast()
        val a = stack.removeLast()
        val expression = template
            .replaceFirst("%s", a)
            .replaceFirst("%s", b)
        val result = evaluateExpression(expression)
        if (result == null) {
            stack.add(a)
            stack.add(b)
            return RpnActionResult.Error("Invalid RPN operation")
        }

        stack.add(result)
        trimStack()
        return RpnActionResult.Success
    }

    private suspend fun evaluateExpression(expression: String): String? {
        val evaluated = evaluator(expression)?.trim().orEmpty()
        return evaluated.takeIf { it.isNotEmpty() }
    }

    private fun commitEntry() {
        val e = entry.trim()
        if (e.isEmpty()) return
        stack.add(e)
        entry = ""
    }

    private fun trimStack() {
        if (stack.size <= stackLimit) return
        val overflow = stack.size - stackLimit
        repeat(overflow) { stack.removeFirst() }
    }

    private fun normalizeOperator(operator: String): String = when (operator) {
        "×", "*" -> "*"
        "÷", "/" -> "/"
        "−", "-" -> "-"
        else -> operator
    }
}
