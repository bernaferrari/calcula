package com.bernaferrari.calculator.domain.math

import kotlin.concurrent.Volatile
import kotlinx.serialization.Serializable

/** A function of x the person wrote themselves: `tip(x) = x×0.18`. */
@Serializable
data class UserFunction(val name: String, val body: String)

/**
 * Makes the person's own functions part of the language by rewriting each call into its body
 * before anything is parsed: `tip(50)` becomes `((50)×0.18)`. Calls inside bodies expand too, a
 * few levels deep, so functions can build on each other.
 */
object UserFunctions {
    /** The functions in effect everywhere: calculator, preview, RPN, graph. */
    @Volatile
    var all: List<UserFunction> = emptyList()

    private val reserved = Tokenizer.FUNCTIONS.toSet() + setOf("e", "x", "y", "pi")

    /** Lowercase letters, 2 to 8 of them, and not a name the calculator already uses. */
    fun isValidName(name: String, taken: Collection<String> = emptyList()): Boolean =
        name.length in 2..8 && name.all { it in 'a'..'z' } && name !in reserved && name !in taken

    fun expand(text: String, functions: List<UserFunction> = all): String {
        ExpressionLimits.check(text)
        if (functions.isEmpty()) return text
        val ordered = functions.sortedByDescending { it.name.length }
        var current = text
        repeat(8) {
            val next = expandOnce(current, ordered)
            if (next == current) return current
            current = next
        }
        if (expandOnce(current, ordered) == current) return current
        throw CalcException(CalcError.SYNTAX)
    }

    private fun expandOnce(text: String, functions: List<UserFunction>): String {
        val out = StringBuilder()
        var i = 0
        while (i < text.length) {
            val fn = functions.firstOrNull { f ->
                text.startsWith(f.name + "(", i) && (i == 0 || text[i - 1] !in 'a'..'z')
            }
            if (fn == null) {
                out.append(text[i]); i++; continue
            }
            // The argument runs to the matching parenthesis, or to the end if it isn't closed yet.
            val start = i + fn.name.length + 1
            var depth = 1
            var j = start
            while (j < text.length && depth > 0) {
                if (text[j] == '(') depth++
                if (text[j] == ')') depth--
                if (depth > 0) j++
            }
            val argument = text.substring(start, j.coerceAtMost(text.length))
            if (argument.isBlank()) throw CalcException(CalcError.SYNTAX)
            ExpressionLimits.check(fn.body)
            val body = substitute(fn.body, argument, functions)
            if (out.length + body.length + 2 > ExpressionLimits.MAX_LENGTH) throw CalcException(CalcError.SYNTAX)
            out.append('(').append(body).append(')')
            i = if (j < text.length) j + 1 else text.length
        }
        return out.toString()
    }

    /** Variables can multiply implicitly (`xx`, `xsin(x)`); function names stay intact. */
    private fun substitute(body: String, argument: String, functions: List<UserFunction>): String {
        val names = (Tokenizer.FUNCTIONS + functions.map { it.name }).sortedByDescending { it.length }
        return buildString {
            var i = 0
            while (i < body.length) {
                val name = names.firstOrNull { body.startsWith("$it(", i) }
                val replacement = name ?: if (body[i] == 'x') "($argument)" else body[i].toString()
                if (length + replacement.length > ExpressionLimits.MAX_LENGTH) throw CalcException(CalcError.SYNTAX)
                append(replacement)
                i += name?.length ?: 1
            }
        }
    }
}
