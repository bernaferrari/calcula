package jscl.math.function

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.mathml.MathML

/**
 * User: serso
 * Date: 11/12/11
 * Time: 3:48 PM
 */
abstract class AbstractDms protected constructor(
    name: String,
    degrees: Generic?,
    minutes: Generic?,
    seconds: Generic?
) : Algebraic(name, createParameters(degrees, minutes, seconds)) {

    override fun getMaxParameters(): Int {
        return 3
    }

    override fun rootValue(): Root {
        throw UnsupportedOperationException("Root for $name() is not supported!")
    }

    override fun bodyToMathML(element: MathML, fenced: Boolean) {
        val child = element.element(name)
        parameters!![0].toMathML(child, null)
        // todo serso: add other parameters
        element.appendChild(child)
    }

    override fun selfExpand(): Generic {
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return selfExpand()
    }

    override fun selfSimplify(): Generic {
        return selfExpand()
    }

    override fun selfNumeric(): Generic {
        var degrees = parameters!![0]

        if (parameters!!.size > 1 && parameters!![1] != null) {
            val minutes = parameters!![1]
            degrees = degrees.add(minutes.divide(JsclInteger.valueOf(60)))
        }

        if (parameters!!.size > 2 && parameters!![2] != null) {
            val seconds = parameters!![2]
            degrees = degrees.add(seconds.divide(JsclInteger.valueOf(60 * 60)))
        }

        return degrees
    }

    override fun derivative(n: Int): Generic {
        throw UnsupportedOperationException("Derivative for $name() is not supported!")
    }

    companion object {
        private fun createParameters(
            degrees: Generic?,
            minutes: Generic?,
            seconds: Generic?
        ): Array<Generic> {
            val result = arrayOfNulls<Generic>(3)

            setDefaultValue(result, degrees, 0)
            setDefaultValue(result, minutes, 1)
            setDefaultValue(result, seconds, 2)

            @Suppress("UNCHECKED_CAST")
            return result as Array<Generic>
        }

        private fun setDefaultValue(
            parameters: Array<Generic?>,
            parameter: Generic?,
            position: Int
        ) {
            parameters[position] = parameter ?: JsclInteger.valueOf(0)
        }
    }
}
