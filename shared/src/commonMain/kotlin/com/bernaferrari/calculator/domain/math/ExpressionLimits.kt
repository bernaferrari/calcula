package com.bernaferrari.calculator.domain.math

/** Shared by expansion and both parsers, before recursive work or large allocations. */
internal object ExpressionLimits {
    const val MAX_LENGTH = 4096

    fun check(text: String) {
        if (text.length > MAX_LENGTH) throw CalcException(CalcError.SYNTAX)
        var depth = 0
        for (c in text) {
            if (c == '(' && ++depth > 32) throw CalcException(CalcError.SYNTAX)
            if (c == ')') depth--
        }
    }
}
