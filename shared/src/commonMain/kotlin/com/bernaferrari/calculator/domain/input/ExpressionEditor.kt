package com.bernaferrari.calculator.domain.input

/** An expression being typed, with the cursor as an index into [text]. */
data class Editor(val text: String = "", val cursor: Int = text.length) {
    val left: Char? get() = text.getOrNull(cursor - 1)
    val isEmpty: Boolean get() = text.isEmpty()
}

/**
 * Pure editing rules for the calculator keyboard. Every function takes an [Editor] and returns the
 * edited one, never throwing: a key that makes no sense in context simply does nothing.
 */
object ExpressionEditor {
    private const val OPERATORS = "+−×÷^"
    private const val POSTFIX_END = "!%²³¹"
    private const val PREFIX_FUNCTIONS = "√∛"

    /** Names that are inserted as `name(` and deleted as a unit. */
    private val FUNCTIONS = listOf("sin⁻¹", "cos⁻¹", "tan⁻¹", "sinh", "cosh", "tanh", "sin", "cos", "tan", "log", "abs", "ln")

    /** True if the character can end an operand, so an operator or `)` may follow it. */
    fun endsOperand(c: Char?): Boolean = c != null && (c.isDigit() || c == '.' || c == ')' || c == 'π' || c == 'e' || c == 'x' || c == 'y' || c in POSTFIX_END)

    fun insertDigit(e: Editor, digit: Char): Editor {
        val start = numberStart(e)
        val number = e.text.substring(start, e.cursor)
        // "0" followed by a digit replaces the zero instead of producing "07".
        if (number == "0" && e.text.getOrNull(e.cursor)?.isDigit() != true) {
            if (digit == '0') return e
            return replace(e, start, e.cursor, digit.toString())
        }
        return insert(e, digit.toString())
    }

    fun insertDecimal(e: Editor): Editor {
        val start = numberStart(e)
        var end = e.cursor
        while (end < e.text.length && (e.text[end].isDigit() || e.text[end] == '.')) end++
        val number = e.text.substring(start, end)
        if ('.' in number || e.text.getOrNull(start - 1) == 'E') return e
        return insert(e, if (e.left?.isDigit() == true) "." else "0.")
    }

    fun insertOperator(e: Editor, op: Char): Editor {
        val left = e.left
        return when {
            left == null || left == '(' || left in PREFIX_FUNCTIONS -> if (op == '−') insert(e, "−") else e
            left in OPERATORS -> {
                // "5×" then "−" means a negative factor; any other operator replaces the previous one.
                if (op == '−' && left in "×÷^") return insert(e, "−")
                var start = e.cursor
                while (start > 0 && e.text[start - 1] in OPERATORS) start--
                if (start == 0 || e.text[start - 1] == '(') return e
                replace(e, start, e.cursor, op.toString())
            }
            else -> insert(e, op.toString())
        }
    }

    fun insertPostfix(e: Editor, symbol: String): Editor = if (endsOperand(e.left) && e.left != '.') insert(e, symbol) else e

    fun insertFunction(e: Editor, name: String): Editor = insert(e, "$name(")

    fun insertRaw(e: Editor, text: String): Editor = insert(e, text)

    /** Inserts a number coming from a previous result, parenthesizing negatives where needed. */
    fun insertNumber(e: Editor, number: String): Editor {
        val needsParens = number.startsWith("−") && (e.left != null && e.left != '(')
        val needsTimes = e.left?.isDigit() == true || e.left == '.'
        return insert(e, (if (needsTimes) "×" else "") + if (needsParens) "($number)" else number)
    }

    fun insertParenthesis(e: Editor): Editor {
        val open = openParentheses(e.text.substring(0, e.cursor))
        return if (open > 0 && endsOperand(e.left)) insert(e, ")") else insert(e, "(")
    }

    fun insertClosingParenthesis(e: Editor): Editor {
        val open = openParentheses(e.text.substring(0, e.cursor))
        return if (open > 0 && endsOperand(e.left)) insert(e, ")") else e
    }

    fun delete(e: Editor): Editor {
        if (e.cursor == 0) return e
        val before = e.text.substring(0, e.cursor)
        val length = when {
            before.endsWith("⁻¹") -> 2
            before.endsWith("(") -> FUNCTIONS.firstOrNull { before.endsWith("$it(") }?.let { it.length + 1 } ?: 1
            else -> 1
        }
        return replace(e, e.cursor - length, e.cursor, "")
    }

    /** Moves the cursor one step left (−1) or right (+1), hopping over function names whole. */
    fun nudge(e: Editor, direction: Int): Editor {
        val before = e.text.substring(0, e.cursor)
        val after = e.text.substring(e.cursor)
        val step = if (direction < 0) {
            when {
                before.isEmpty() -> 0
                before.endsWith("⁻¹") -> -2
                before.endsWith("(") -> -(FUNCTIONS.firstOrNull { before.endsWith("$it(") }?.let { it.length + 1 } ?: 1)
                else -> -1
            }
        } else {
            when {
                after.isEmpty() -> 0
                after.startsWith("⁻¹") -> 2
                else -> FUNCTIONS.firstOrNull { after.startsWith("$it(") }?.let { it.length + 1 } ?: 1
            }
        }
        return e.copy(cursor = e.cursor + step)
    }

    fun moveCursor(e: Editor, position: Int): Editor = e.copy(cursor = snap(e.text, position.coerceIn(0, e.text.length)))

    fun openParentheses(text: String): Int =
        // Each side of a graph's equation balances its own parentheses.
        text.substringAfterLast('=').let { side -> side.count { it == '(' } - side.count { it == ')' } }

    /** Keeps the cursor out of the middle of a function name like `si|n(`. */
    private fun snap(text: String, position: Int): Int {
        for (name in FUNCTIONS) {
            var index = text.indexOf("$name(")
            while (index >= 0) {
                val end = index + name.length + 1
                if (position in (index + 1) until end) return end
                index = text.indexOf("$name(", index + 1)
            }
        }
        if (position > 0 && text.getOrNull(position - 1) == '⁻' && text.getOrNull(position) == '¹') return position + 1
        return position
    }

    private fun numberStart(e: Editor): Int {
        var i = e.cursor
        while (i > 0 && (e.text[i - 1].isDigit() || e.text[i - 1] == '.')) i--
        return i
    }

    private fun insert(e: Editor, s: String): Editor = replace(e, e.cursor, e.cursor, s)

    private fun replace(e: Editor, start: Int, end: Int, s: String): Editor =
        Editor(e.text.substring(0, start) + s + e.text.substring(end), start + s.length)
}
