package jscl.math.operator.product

import jscl.math.Generic
import jscl.math.Matrix
import jscl.math.Variable
import jscl.math.genericArrayOf
import jscl.math.operator.Operator
import jscl.math.operator.VectorOperator
import jscl.mathml.MathML

class TensorProduct : VectorOperator {

    constructor(matrix1: Generic?, matrix2: Generic?) : super(NAME, genericArrayOf(matrix1, matrix2))

    private constructor(parameter: Array<Generic>) : super(NAME, parameter)

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        if (parameters!![0] is Matrix && parameters!![1] is Matrix) {
            val m1 = parameters!![0] as Matrix
            val m2 = parameters!![1] as Matrix
            return m1.tensorProduct(m2)
        }
        return expressionValue()
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return TensorProduct(parameters)
    }

    override fun bodyToMathML(element: MathML) {
        parameters!![0].toMathML(element, null)
        val e1 = element.element("mo")
        e1.appendChild(element.text("*"))
        element.appendChild(e1)
        parameters!![1].toMathML(element, null)
    }

    override fun newInstance(): Variable {
        return TensorProduct(null, null)
    }

    companion object {
        const val NAME = "tensor"
    }
}
