package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.model.AngleUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

/**
 * A function of `x` compiled once into a tree of Double operations, for plotting: the same
 * language and grammar as [CalcEngine] (implicit multiplication, smart percent, postfix powers,
 * auto-closed parentheses), but fast enough to sample hundreds of points per frame.
 * Undefined points (√ of a negative, tan at 90°…) come out as NaN, which the plot skips.
 */
fun interface Plot {
    fun at(x: Double): Double

    companion object {
        /** Compiles [expression], or returns null if it isn't a complete function. */
        fun compile(expression: String, angle: AngleUnit = AngleUnit.RADIANS): Plot? = try {
            val tokens = Tokenizer.tokenize(expression)
            if (tokens.isEmpty()) null else PlotParser(tokens, angle).parseAll()
        } catch (e: CalcException) {
            null
        }
    }
}

private class PlotParser(private val tokens: List<Token>, private val angle: AngleUnit) {
    private var pos = 0

    private class Term(val f: Plot, val percent: Boolean = false)

    private fun peek(): Token? = tokens.getOrNull(pos)
    private fun next(): Token = tokens.getOrNull(pos++) ?: throw CalcException(CalcError.SYNTAX)

    fun parseAll(): Plot {
        val f = additive().f
        if (pos != tokens.size) throw CalcException(CalcError.SYNTAX)
        return f
    }

    private fun additive(): Term {
        var left = multiplicative()
        while (true) {
            val op = (peek() as? Token.Op)?.symbol
            if (op != '+' && op != '−') break
            pos++
            val right = multiplicative()
            val l = left.f
            val r = right.f
            val amount: Plot = if (right.percent) Plot { x -> l.at(x) * r.at(x) } else r
            left = Term(if (op == '+') Plot { x -> l.at(x) + amount.at(x) } else Plot { x -> l.at(x) - amount.at(x) })
        }
        return left
    }

    private fun multiplicative(): Term {
        var left = unary()
        while (true) {
            val token = peek()
            val l = left.f
            left = when {
                token is Token.Op && token.symbol == '×' -> { pos++; val r = unary().f; Term(Plot { x -> l.at(x) * r.at(x) }) }
                token is Token.Op && token.symbol == '÷' -> { pos++; val r = unary().f; Term(Plot { x -> l.at(x) / r.at(x) }) }
                token != null && token.startsOperand() -> { val r = power().f; Term(Plot { x -> l.at(x) * r.at(x) }) }
                else -> return left
            }
        }
    }

    private fun unary(): Term {
        val token = peek()
        if (token is Token.Op && token.symbol == '−') {
            pos++
            val inner = unary()
            val f = inner.f
            return Term(Plot { x -> -f.at(x) }, inner.percent)
        }
        if (token is Token.Op && token.symbol == '+') {
            pos++
            return unary()
        }
        return power()
    }

    private fun power(): Term {
        val base = postfix()
        val token = peek()
        if (token is Token.Op && token.symbol == '^') {
            pos++
            if (peek() == null) throw CalcException(CalcError.SYNTAX)
            val b = base.f
            val e = unary().f
            return Term(Plot { x -> pow(b.at(x), e.at(x)) })
        }
        return base
    }

    private fun postfix(): Term {
        var term = primary()
        while (true) {
            val token = peek() as? Token.Postfix ?: return term
            pos++
            val f = term.f
            term = when (token.symbol) {
                "%" -> Term(Plot { x -> f.at(x) / 100.0 }, percent = true)
                "!" -> Term(Plot { x -> factorial(f.at(x)) })
                "²" -> Term(Plot { x -> f.at(x).let { it * it } })
                "³" -> Term(Plot { x -> f.at(x).let { it * it * it } })
                "⁻¹" -> Term(Plot { x -> 1.0 / f.at(x) })
                else -> throw CalcException(CalcError.SYNTAX)
            }
        }
    }

    private fun primary(): Term = when (val token = next()) {
        is Token.Num -> token.value.toDouble().let { v -> Term(Plot { v }) }
        is Token.Const -> (if (token.name == "π") PI else kotlin.math.E).let { v -> Term(Plot { v }) }
        is Token.Var -> Term(Plot { x -> x })
        is Token.LParen -> Term(group())
        is Token.Func -> {
            val arg = if (peek() is Token.LParen) { pos++; group() } else power().f
            Term(function(token.name, arg))
        }
        is Token.Root -> {
            val arg = postfix().f
            Term(if (token.degree == 2) Plot { x -> sqrt(arg.at(x)) } else Plot { x -> cbrt(arg.at(x)) })
        }
        else -> throw CalcException(CalcError.SYNTAX)
    }

    private fun group(): Plot {
        if (peek() == null) throw CalcException(CalcError.SYNTAX)
        val inner = additive().f
        when (peek()) {
            is Token.RParen -> pos++
            null -> Unit
            else -> throw CalcException(CalcError.SYNTAX)
        }
        return inner
    }

    private val toRadians = if (angle == AngleUnit.DEGREES) PI / 180.0 else 1.0
    private val fromRadians = 1.0 / toRadians

    private fun function(name: String, a: Plot): Plot = when (name) {
        "sin" -> Plot { x -> sin(a.at(x) * toRadians) }
        "cos" -> Plot { x -> cos(a.at(x) * toRadians) }
        "tan" -> Plot { x -> tan(a.at(x) * toRadians) }
        "sin⁻¹" -> Plot { x -> asin(a.at(x)) * fromRadians }
        "cos⁻¹" -> Plot { x -> acos(a.at(x)) * fromRadians }
        "tan⁻¹" -> Plot { x -> atan(a.at(x)) * fromRadians }
        "sinh" -> Plot { x -> sinh(a.at(x)) }
        "cosh" -> Plot { x -> cosh(a.at(x)) }
        "tanh" -> Plot { x -> tanh(a.at(x)) }
        "ln" -> Plot { x -> ln(a.at(x)) }
        "log" -> Plot { x -> log10(a.at(x)) }
        "abs" -> Plot { x -> abs(a.at(x)) }
        else -> throw CalcException(CalcError.SYNTAX)
    }

    private fun Token.startsOperand(): Boolean =
        this is Token.Num || this is Token.Const || this is Token.Var || this is Token.LParen || this is Token.Func || this is Token.Root

    /** Real powers, with odd roots of negatives kept real: (−8)^(1/3) = −2. */
    private fun pow(b: Double, e: Double): Double {
        if (b >= 0 || e == floor(e)) return b.pow(e)
        val reciprocal = 1.0 / e
        val n = kotlin.math.round(reciprocal)
        return if (abs(reciprocal - n) < 1e-9 && n.toLong() % 2L != 0L) -((-b).pow(e)) else Double.NaN
    }

    private fun factorial(v: Double): Double {
        if (v < 0 || v != floor(v) || v > 170) return Double.NaN
        var r = 1.0
        for (i in 2..v.toInt()) r *= i
        return r
    }
}
