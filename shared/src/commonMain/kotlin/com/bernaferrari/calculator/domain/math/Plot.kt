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
        /**
         * Compiles [expression], or returns null if it isn't a complete function of x alone.
         * Parameters take their value from [params], 1 when missing.
         */
        fun compile(expression: String, angle: AngleUnit = AngleUnit.RADIANS, params: Map<Char, Double> = emptyMap()): Plot? =
            Field.compile(expression, angle, params, allowY = false)?.let { f -> Plot { x -> f.at(x, 0.0) } }
    }
}

/** A function of both `x` and `y`: one side of an equation minus the other. */
fun interface Field {
    fun at(x: Double, y: Double): Double

    companion object {
        internal fun compile(expression: String, angle: AngleUnit, params: Map<Char, Double> = emptyMap(), allowY: Boolean = true): Field? = try {
            val tokens = Tokenizer.tokenize(expression)
            when {
                tokens.isEmpty() -> null
                !allowY && tokens.any { it == Token.Var('y') } -> null
                else -> PlotParser(tokens, angle, params).parseAll()
            }
        } catch (e: CalcException) {
            null
        }
    }
}

/**
 * What a graph row draws. Plain text is `y =` that function of x; with an `=` it is an equation,
 * which is still a plain function when one side is just `y` (`y = x²`, `x² = y`), and otherwise
 * a curve in x and y, drawn wherever both sides agree: `x² + y² = 9`, `x = 3`, `y² = x`.
 */
sealed interface Curve {
    data class Explicit(val plot: Plot) : Curve
    data class Implicit(val field: Field) : Curve

    companion object {
        fun compile(text: String, angle: AngleUnit = AngleUnit.RADIANS, params: Map<Char, Double> = emptyMap()): Curve? {
            val sides = text.split('=')
            if (sides.size == 1) return Plot.compile(text, angle, params)?.let(::Explicit)
            if (sides.size != 2) return null
            val (left, right) = sides.map { it.trim() }
            if (left.isEmpty() || right.isEmpty()) return null
            if (left == "y") Plot.compile(right, angle, params)?.let { return Explicit(it) }
            if (right == "y") Plot.compile(left, angle, params)?.let { return Explicit(it) }
            val l = Field.compile(left, angle, params) ?: return null
            val r = Field.compile(right, angle, params) ?: return null
            return Implicit { x, y -> l.at(x, y) - r.at(x, y) }
        }

        /** The slider parameters [text] uses, in alphabetical order. */
        fun parameters(text: String): List<Char> = text.split('=').flatMap { side ->
            try {
                Tokenizer.tokenize(side).filterIsInstance<Token.Param>().map { it.name }
            } catch (e: CalcException) {
                emptyList()
            }
        }.distinct().sorted()
    }
}

private class PlotParser(private val tokens: List<Token>, private val angle: AngleUnit, private val params: Map<Char, Double>) {
    private var pos = 0

    private class Term(val f: Field, val percent: Boolean = false)

    private fun peek(): Token? = tokens.getOrNull(pos)
    private fun next(): Token = tokens.getOrNull(pos++) ?: throw CalcException(CalcError.SYNTAX)

    fun parseAll(): Field {
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
            val amount: Field = if (right.percent) Field { x, y -> l.at(x, y) * r.at(x, y) } else r
            left = Term(if (op == '+') Field { x, y -> l.at(x, y) + amount.at(x, y) } else Field { x, y -> l.at(x, y) - amount.at(x, y) })
        }
        return left
    }

    private fun multiplicative(): Term {
        var left = unary()
        while (true) {
            val token = peek()
            val l = left.f
            left = when {
                token is Token.Op && token.symbol == '×' -> { pos++; val r = unary().f; Term(Field { x, y -> l.at(x, y) * r.at(x, y) }) }
                token is Token.Op && token.symbol == '÷' -> { pos++; val r = unary().f; Term(Field { x, y -> l.at(x, y) / r.at(x, y) }) }
                token != null && token.startsOperand() -> { val r = power().f; Term(Field { x, y -> l.at(x, y) * r.at(x, y) }) }
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
            return Term(Field { x, y -> -f.at(x, y) }, inner.percent)
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
            return Term(Field { x, y -> pow(b.at(x, y), e.at(x, y)) })
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
                "%" -> Term(Field { x, y -> f.at(x, y) / 100.0 }, percent = true)
                "!" -> Term(Field { x, y -> factorial(f.at(x, y)) })
                "²" -> Term(Field { x, y -> f.at(x, y).let { it * it } })
                "³" -> Term(Field { x, y -> f.at(x, y).let { it * it * it } })
                "⁻¹" -> Term(Field { x, y -> 1.0 / f.at(x, y) })
                else -> throw CalcException(CalcError.SYNTAX)
            }
        }
    }

    private fun primary(): Term = when (val token = next()) {
        is Token.Num -> token.value.toDouble().let { v -> Term(Field { _, _ -> v }) }
        is Token.Const -> (if (token.name == "π") PI else kotlin.math.E).let { v -> Term(Field { _, _ -> v }) }
        is Token.Param -> (params[token.name] ?: 1.0).let { v -> Term(Field { _, _ -> v }) }
        is Token.Var -> Term(if (token.name == 'y') Field { _, y -> y } else Field { x, _ -> x })
        is Token.LParen -> Term(group())
        is Token.Func -> {
            val arg = if (peek() is Token.LParen) { pos++; group() } else power().f
            Term(function(token.name, arg))
        }
        is Token.Root -> {
            val arg = postfix().f
            Term(if (token.degree == 2) Field { x, y -> sqrt(arg.at(x, y)) } else Field { x, y -> cbrt(arg.at(x, y)) })
        }
        else -> throw CalcException(CalcError.SYNTAX)
    }

    private fun group(): Field {
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

    private fun function(name: String, a: Field): Field = when (name) {
        "sin" -> Field { x, y -> sin(a.at(x, y) * toRadians) }
        "cos" -> Field { x, y -> cos(a.at(x, y) * toRadians) }
        "tan" -> Field { x, y -> tan(a.at(x, y) * toRadians) }
        "sin⁻¹" -> Field { x, y -> asin(a.at(x, y)) * fromRadians }
        "cos⁻¹" -> Field { x, y -> acos(a.at(x, y)) * fromRadians }
        "tan⁻¹" -> Field { x, y -> atan(a.at(x, y)) * fromRadians }
        "sinh" -> Field { x, y -> sinh(a.at(x, y)) }
        "cosh" -> Field { x, y -> cosh(a.at(x, y)) }
        "tanh" -> Field { x, y -> tanh(a.at(x, y)) }
        "ln" -> Field { x, y -> ln(a.at(x, y)) }
        "log" -> Field { x, y -> log10(a.at(x, y)) }
        "abs" -> Field { x, y -> abs(a.at(x, y)) }
        "sinh⁻¹" -> Field { x, y -> kotlin.math.asinh(a.at(x, y)) }
        "cosh⁻¹" -> Field { x, y -> kotlin.math.acosh(a.at(x, y)) }
        "tanh⁻¹" -> Field { x, y -> kotlin.math.atanh(a.at(x, y)) }
        "sec" -> Field { x, y -> 1.0 / cos(a.at(x, y) * toRadians) }
        "csc" -> Field { x, y -> 1.0 / sin(a.at(x, y) * toRadians) }
        "cot" -> Field { x, y -> 1.0 / tan(a.at(x, y) * toRadians) }
        "floor" -> Field { x, y -> floor(a.at(x, y)) }
        "ceil" -> Field { x, y -> kotlin.math.ceil(a.at(x, y)) }
        "round" -> Field { x, y -> a.at(x, y).let { v -> if (v < 0) -floor(-v + 0.5) else floor(v + 0.5) } }
        "sgn" -> Field { x, y -> kotlin.math.sign(a.at(x, y)) }
        "log₂" -> Field { x, y -> kotlin.math.log2(a.at(x, y)) }
        else -> throw CalcException(CalcError.SYNTAX)
    }

    private fun Token.startsOperand(): Boolean =
        this is Token.Num || this is Token.Const || this is Token.Var || this is Token.Param || this is Token.LParen || this is Token.Func || this is Token.Root

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
