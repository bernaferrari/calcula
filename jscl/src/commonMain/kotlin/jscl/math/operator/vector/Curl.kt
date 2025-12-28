package jscl.math.operator.vector

import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.JsclVector
import jscl.math.Variable
import jscl.math.operator.Operator
import jscl.math.operator.VectorOperator
import jscl.mathml.MathML

class Curl : VectorOperator {

    constructor(vector: Generic?, variable: Generic?) : super(NAME, genericArrayOf(vector, variable))

    private constructor(parameter: Array<Generic>) : super(NAME, parameter)

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        val variable = toVariables(parameters!![1] as JsclVector)
        if (parameters!![0] is JsclVector) {
            val vector = parameters!![0] as JsclVector
            return vector.curl(variable)
        }
        return expressionValue()
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Curl(parameters)
    }

    override fun bodyToMathML(element: MathML) {
        operator(element, "nabla")
        val e1 = element.element("mo")
        e1.appendChild(element.text("\u2227"))
        element.appendChild(e1)
        parameters!![0].toMathML(element, null)
    }

    override fun newInstance(): Variable {
        return Curl(null, null)
    }

    companion object {
        const val NAME = "curl"
    }
}
