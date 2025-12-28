package jscl.math.operator.vector

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.JsclVector
import jscl.math.Variable
import jscl.math.operator.Operator
import jscl.math.operator.VectorOperator
import jscl.mathml.MathML

class Grad : VectorOperator {

    constructor(expression: Generic?, variable: Generic?) : super(NAME, genericArrayOf(expression, variable))

    private constructor(parameter: Array<Generic>) : super(NAME, parameter)

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        val variable = toVariables(parameters!![1] as JsclVector)
        val expression = parameters!![0].expressionValue()
        return expression.grad(variable)
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Grad(parameters)
    }

    override fun bodyToMathML(element: MathML) {
        operator(element, "nabla")
        parameters!![0].toMathML(element, null)
    }

    override fun newInstance(): Variable {
        return Grad(null, null)
    }

    companion object {
        const val NAME = "grad"
    }
}
