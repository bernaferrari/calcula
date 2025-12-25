package jscl.math.operator.vector

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.Variable
import jscl.math.operator.Operator
import jscl.math.operator.VectorOperator
import jscl.mathml.MathML

class Laplacian : VectorOperator {

    constructor(vector: Generic?, variable: Generic?) : super(NAME, genericArrayOf(vector, variable))

    private constructor(parameter: Array<Generic>) : super(NAME, parameter)

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        val variable = toVariables(parameters!![1])
        val expression = parameters!![0].expressionValue()
        return expression.laplacian(variable)
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Laplacian(parameters)
    }

    override fun bodyToMathML(element: MathML) {
        operator(element, "Delta")
        parameters!![0].toMathML(element, null)
    }

    override fun newInstance(): Variable {
        return Laplacian(null, null)
    }

    companion object {
        const val NAME = "laplacian"
    }
}
