@file:Suppress("UNCHECKED_CAST")

package jscl.math.operator.vector

import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.JsclInteger
import jscl.math.JsclVector
import jscl.math.Variable
import jscl.math.operator.Operator
import jscl.math.operator.VectorOperator
import jscl.math.operator.product.GeometricProduct
import jscl.mathml.MathML

class Del : VectorOperator {

    constructor(vector: Generic?, variable: Generic?, algebra: Generic?) :
            super(NAME, genericArrayOf(vector, variable, algebra))

    private constructor(parameters: Array<Generic>) : super(NAME, createParameters(parameters))

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        val variable = toVariables(parameters!![1] as JsclVector)
        val algebra = GeometricProduct.algebra(parameters!![2])
        if (parameters!![0] is JsclVector) {
            val vector = parameters!![0] as JsclVector
            return vector.del(variable, algebra)
        }
        return expressionValue()
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Del(parameters)
    }

    override fun bodyToMathML(element: MathML) {
        operator(element, "nabla")
        parameters!![0].toMathML(element, null)
    }

    override fun newInstance(): Variable {
        return Del(null, null, null)
    }

    companion object {
        const val NAME = "del"

        private fun createParameters(parameters: Array<Generic>): Array<Generic> {
            val result = arrayOfNulls<Generic>(3)

            result[0] = parameters[0]
            result[1] = parameters[1]
            result[2] = if (parameters.size > 2) parameters[2] else JsclInteger.valueOf(0)

            return result as Array<Generic>
        }
    }
}
