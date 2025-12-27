package jscl.math.operator.product

import jscl.math.Generic
import jscl.math.JsclVector
import jscl.math.Variable
import jscl.math.genericArrayOf
import jscl.math.operator.Operator
import jscl.math.operator.VectorOperator
import jscl.mathml.MathML

class VectorProduct : VectorOperator {

    constructor(vector1: Generic?, vector2: Generic?) : super(NAME, genericArrayOf(vector1, vector2))

    private constructor(parameter: Array<Generic>) : super(NAME, parameter)

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        if (parameters!![0] is JsclVector && parameters!![1] is JsclVector) {
            val v1 = parameters!![0] as JsclVector
            val v2 = parameters!![1] as JsclVector
            return v1.vectorProduct(v2)
        }
        return expressionValue()
    }

    override fun bodyToMathML(element: MathML) {
        parameters!![0].toMathML(element, null)
        val e1 = element.element("mo")
        e1.appendChild(element.text("\u2227"))
        element.appendChild(e1)
        parameters!![1].toMathML(element, null)
    }

    override fun newInstance(): Variable {
        return VectorProduct(null, null)
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return VectorProduct(parameters)
    }

    companion object {
        const val NAME = "vector"
    }
}
