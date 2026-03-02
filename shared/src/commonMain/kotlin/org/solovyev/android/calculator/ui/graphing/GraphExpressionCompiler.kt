package org.solovyev.android.calculator.ui.graphing

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NumericWrapper
import jscl.math.function.Constant
import jscl.math.numeric.Complex
import jscl.math.numeric.Numeric
import jscl.math.numeric.Real

internal const val InvalidGraphExpressionMessage = "Invalid expression"

/**
 * Compiles graph expressions once and evaluates them by substituting x numerically.
 * This mirrors the old plotter approach and avoids string-replacement evaluation.
 */
internal class GraphExpressionCompiler(
    private val prepareExpression: (String) -> String = { it }
) {

    fun compile(rawExpression: String): GraphCompilationResult {
        val input = rawExpression.trim()
        if (input.isEmpty()) {
            return GraphCompilationResult.Error(InvalidGraphExpressionMessage)
        }
        return try {
            val prepared = prepareExpression(input)
            val parsed = Expression.valueOf(prepared).expand()
            GraphCompilationResult.Success(
                CompiledGraphExpression(
                    rawExpression = input,
                    preparedExpression = prepared,
                    expression = parsed
                )
            )
        } catch (e: Exception) {
            GraphCompilationResult.Error(errorMessage(e))
        }
    }

    private fun errorMessage(error: Throwable): String {
        return error.message?.takeIf { it.isNotBlank() } ?: InvalidGraphExpressionMessage
    }
}

internal sealed class GraphCompilationResult {
    data class Success(val expression: CompiledGraphExpression) : GraphCompilationResult()
    data class Error(val message: String) : GraphCompilationResult()
}

internal class CompiledGraphExpression(
    val rawExpression: String,
    val preparedExpression: String,
    private val expression: Generic
) {
    private val xVariable = Constant("x")

    fun evaluateAt(x: Double): Double? {
        if (!x.isFinite()) return null
        return try {
            val substituted = expression.substitute(xVariable, NumericWrapper.valueOf(x))
            val numeric = substituted.numeric()
            unwrapGeneric(numeric)?.takeIf { it.isFinite() }
        } catch (_: Exception) {
            null
        }
    }
}

internal fun unwrapGeneric(generic: Generic): Double? {
    return when (generic) {
        is JsclInteger -> generic.doubleValue()
        is NumericWrapper -> unwrapNumeric(generic.content())
        else -> {
            try {
                generic.doubleValue().takeIf { it.isFinite() }
            } catch (_: Exception) {
                null
            }
        }
    }
}

internal fun unwrapNumeric(content: Numeric): Double? {
    return when (content) {
        is Real -> content.doubleValue()
        is Complex -> {
            val imaginary = content.imaginaryPart()
            val real = content.realPart()
            if (real == 0.0 && imaginary != 0.0) {
                null
            } else {
                real
            }
        }
        else -> null
    }
}
