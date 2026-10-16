package com.bernaferrari.calculator.domain.math

import java.math.BigDecimal

internal sealed interface Token {
    data class Num(val value: BigDecimal) : Token
    data class Op(val symbol: Char) : Token
    data class Postfix(val symbol: String) : Token
    data class Func(val name: String) : Token
    data class Const(val name: String) : Token
    data class Root(val degree: Int) : Token
    data object LParen : Token
    data object RParen : Token
}

/**
 * The expression language uses the same characters the user sees: `−` `×` `÷` `π` `√` `²` `sin⁻¹(`.
 * ASCII aliases (`-`, `*`, `/`) are accepted too so pasted text works.
 */
internal object Tokenizer {
    /** Longest names first so `sinh` wins over `sin` and `sin⁻¹` over `sin`. */
    val FUNCTIONS = listOf(
        "sin⁻¹", "cos⁻¹", "tan⁻¹", "sinh", "cosh", "tanh", "sin", "cos", "tan", "log", "abs", "ln",
    )

    fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() || c == ',' -> i++
                c.isDigit() || c == '.' -> {
                    val end = numberEnd(input, i)
                    tokens += Token.Num(parseNumber(input.substring(i, end)))
                    i = end
                }
                c == '+' -> { tokens += Token.Op('+'); i++ }
                c == '−' || c == '-' -> { tokens += Token.Op('−'); i++ }
                c == '×' || c == '*' -> { tokens += Token.Op('×'); i++ }
                c == '÷' || c == '/' -> { tokens += Token.Op('÷'); i++ }
                c == '^' -> { tokens += Token.Op('^'); i++ }
                c == '(' -> { tokens += Token.LParen; i++ }
                c == ')' -> { tokens += Token.RParen; i++ }
                c == '!' || c == '%' || c == '²' || c == '³' -> { tokens += Token.Postfix(c.toString()); i++ }
                input.startsWith("⁻¹", i) -> { tokens += Token.Postfix("⁻¹"); i += 2 }
                c == '√' -> { tokens += Token.Root(2); i++ }
                c == '∛' -> { tokens += Token.Root(3); i++ }
                c == 'π' -> { tokens += Token.Const("π"); i++ }
                c == 'e' -> { tokens += Token.Const("e"); i++ }
                else -> {
                    val name = FUNCTIONS.firstOrNull { input.startsWith(it, i) }
                        ?: throw CalcException(CalcError.SYNTAX)
                    tokens += Token.Func(name)
                    i += name.length
                }
            }
        }
        return tokens
    }

    /** A number is digits with an optional fraction and an optional `E±n` exponent. */
    fun numberEnd(input: String, start: Int): Int {
        var i = start
        while (i < input.length && (input[i].isDigit() || input[i] == '.')) i++
        if (i < input.length && input[i] == 'E') {
            var j = i + 1
            if (j < input.length && input[j] in "+−-") j++
            if (j < input.length && input[j].isDigit()) {
                while (j < input.length && input[j].isDigit()) j++
                i = j
            }
        }
        return i
    }

    private fun parseNumber(text: String): BigDecimal {
        val normalized = text.replace('−', '-').let { if (it.startsWith(".")) "0$it" else it }
        if (normalized.count { it == '.' } > 1) throw CalcException(CalcError.SYNTAX)
        return try {
            BigDecimal(normalized.removeSuffix("."))
        } catch (e: NumberFormatException) {
            throw CalcException(CalcError.SYNTAX)
        }
    }
}
