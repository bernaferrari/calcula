@file:Suppress("UNCHECKED_CAST")

package jscl.math.operator

import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.JsclInteger
import jscl.math.Variable
import jscl.math.function.Root
import jscl.math.polynomial.Polynomial
import jscl.math.polynomial.UnivariatePolynomial
import jscl.mathml.MathML

class Solve : Operator {

    constructor(expression: Generic?, variable: Generic?, subscript: Generic?) :
            super(NAME, genericArrayOf(expression, variable, subscript))

    constructor(parameters: Array<Generic>) : super(NAME, createParameters(parameters))

    override fun getMinParameters(): Int = 2

    override fun getMaxParameters(): Int = 3

    override fun selfExpand(): Generic {
        val variable = parameters!![1].variableValue()

        val subscript = parameters!![2].integerValue().toInt()
        if (parameters!![0].isPolynomial(variable)) {
            return Root(
                Polynomial.factory(variable).valueOf(parameters!![0]) as UnivariatePolynomial,
                subscript
            ).selfExpand()
        }

        return expressionValue()
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        var n = 3
        if (parameters!![2].signum() == 0) n = 2
        if (exponent == 1) {
            nameToMathML(element)
        } else {
            val e1 = element.element("msup")
            nameToMathML(e1)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
        val e1 = element.element("mfenced")
        for (i in 0 until n) {
            parameters!![i].toMathML(e1, null)
        }
        element.appendChild(e1)
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Solve(parameters)
    }

    override fun newInstance(): Variable {
        return Solve(null, null, null)
    }

    companion object {
        const val NAME = "solve"

        private fun createParameters(parameters: Array<Generic>): Array<Generic> {
            val result = arrayOfNulls<Generic>(3)

            result[0] = parameters[0]
            result[1] = parameters[1]
            result[2] = if (parameters.size > 2) parameters[2] else JsclInteger.valueOf(0)

            return result as Array<Generic>
        }
    }
}
