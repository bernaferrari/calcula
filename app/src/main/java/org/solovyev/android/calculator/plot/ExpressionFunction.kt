package org.solovyev.android.calculator.plot

import android.text.TextUtils
import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NumericWrapper
import jscl.math.function.CustomFunction
import jscl.math.numeric.Complex
import jscl.math.numeric.Numeric
import jscl.math.numeric.Real
import org.solovyev.android.plotter.Function

class ExpressionFunction(
    val function: jscl.math.function.Function,
    private val plotImaginary: Boolean = false
) : Function(makeFunctionName(function)) {
    @get:JvmName("arityProperty")
    val arity: Int = function.getMaxParameters()
    private val parameters: Array<Generic?> = arrayOfNulls(arity)

    override fun getArity(): Int = arity

    override fun evaluate(): Float = try {
        unwrap(function.numeric())
    } catch (e: RuntimeException) {
        Float.NaN
    }

    override fun evaluate(x: Float): Float = try {
        parameters[0] = Expression.valueOf(x.toDouble())
        @Suppress("UNCHECKED_CAST")
        function.setParameters(parameters as Array<Generic>)
        unwrap(function.numeric())
    } catch (e: RuntimeException) {
        Float.NaN
    }

    override fun evaluate(x: Float, y: Float): Float = try {
        parameters[0] = Expression.valueOf(x.toDouble())
        parameters[1] = Expression.valueOf(y.toDouble())
        @Suppress("UNCHECKED_CAST")
        function.setParameters(parameters as Array<Generic>)
        unwrap(function.numeric())
    } catch (e: RuntimeException) {
        Float.NaN
    }

    private fun unwrap(numeric: Generic): Float = when (numeric) {
        is JsclInteger -> numeric.intValue().toFloat()
        is NumericWrapper -> unwrap(numeric.content())
        else -> Float.NaN
    }

    private fun unwrap(content: Numeric): Float = when (content) {
        is Real -> if (plotImaginary) 0f else content.doubleValue().toFloat()
        is Complex -> {
            val imag = content.imaginaryPart()
            val real = content.realPart()
            if (plotImaginary) {
                if (!imag.isFinite()) Float.NaN else imag.toFloat()
            } else {
                if (kotlin.math.abs(imag) > COMPLEX_EPS || !real.isFinite()) {
                    Float.NaN
                } else {
                    real.toFloat()
                }
            }
        }
        else -> Float.NaN
    }

    companion object {
        private const val COMPLEX_EPS = 1e-12

        private fun makeFunctionName(function: jscl.math.function.Function): String {
            var name: String = function.name
            if (name.isEmpty()) {
                name = when (function) {
                    is CustomFunction -> function.getContent()
                    else -> function.toString()
                }
                if (name.length > 10) {
                    name = "${name.substring(0, 10)}…"
                }
            }
            return name
        }
    }
}
