package jscl.math.operator

import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.JsclVector
import jscl.math.Variable
import jscl.math.polynomial.Polynomial

class Coefficient : Operator {

    constructor(expression: Generic?, variable: Generic?) : super(NAME, genericArrayOf(expression, variable))

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        val variable = parameters!![1].variableValue()
        return if (parameters!![0].isPolynomial(variable)) {
            JsclVector(Polynomial.factory(variable).valueOf(parameters!![0]).elements())
        } else {
            expressionValue()
        }
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Coefficient(parameters)
    }

    override fun newInstance(): Variable {
        return Coefficient(null, null)
    }

    companion object {
        const val NAME = "coef"
    }
}
