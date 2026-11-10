package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.model.AngleUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json

/** Parsed after custom-function expansion. Every angle-dependent operation captures its own mode. */
@Serializable
internal sealed interface Expression {
    @Serializable @SerialName("n") data class Number(
        val coefficient: String,
        val scale: Int,
        val radius: String? = "0,0",
        val symbol: String? = null,
    ) : Expression {
        fun value(): CalcEngine.Value = try {
            val center = Decimal.fromUnscaled(coefficient, scale)
            CalcEngine.Value(center, error = decodeRadius(radius),
                symbolic = symbol?.let(SymbolicNumber::restore) ?: if (radius == "0,0") SymbolicNumber.fromDecimal(center) else null)
        } catch (_: IllegalArgumentException) {
            throw CalcException(CalcError.SYNTAX)
        }
    }

    @Serializable @SerialName("c") data class Constant(val name: String) : Expression
    @Serializable @SerialName("u") data class Unary(val op: String, val input: Expression) : Expression
    @Serializable @SerialName("b") data class Binary(val op: Char, val left: Expression, val right: Expression) : Expression
    @Serializable @SerialName("f") data class Call(val name: String, val input: Expression, val angle: AngleUnit) : Expression

    companion object {
        private const val MAX_NODES = 512
        private const val MAX_SAVED_LENGTH = 32_768

        fun number(value: CalcEngine.Value): Number =
            Number(value.v.unscaled.toString(), value.v.scale(), encodeRadius(value.error), value.symbolic?.save())

        fun encodeRadius(radius: Decimal?): String? = radius?.let { "${it.unscaled},${it.scale()}" }

        fun decodeRadius(radius: String?): Decimal? = radius?.split(',')?.let {
            require(it.size == 2)
            Decimal.fromUnscaled(it[0], it[1].toInt()).also { value -> require(value.signum() >= 0) }
        }

        fun save(expression: Expression): String? =
            if (expression.fitsBudget()) Json.encodeToString(expression).takeIf { it.length <= MAX_SAVED_LENGTH } else null

        fun restore(saved: String): Expression? {
            if (saved.length > MAX_SAVED_LENGTH) return null
            var depth = 0
            // The encoding only has numeric strings, fixed operator names and symbolic tags.
            for (char in saved) {
                if (char == '{' && ++depth > 128) return null
                if (char == '}') depth--
            }
            return runCatching { Json.decodeFromString<Expression>(saved).takeIf { it.fitsBudget() } }.getOrNull()
        }

        fun Expression.fitsBudget(): Boolean {
            val pending = ArrayDeque<Pair<Expression, Int>>()
            pending.addLast(this to 1)
            var count = 0
            while (pending.isNotEmpty()) {
                val (node, depth) = pending.removeLast()
                if (++count > MAX_NODES || depth > 128) return false
                when (node) {
                    is Binary -> { pending.addLast(node.left to depth + 1); pending.addLast(node.right to depth + 1) }
                    is Unary -> pending.addLast(node.input to depth + 1)
                    is Call -> pending.addLast(node.input to depth + 1)
                    else -> Unit
                }
            }
            return true
        }
    }
}
