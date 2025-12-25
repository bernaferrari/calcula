package jscl.math.operator.product

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.JsclVector
import jscl.math.Variable
import jscl.math.function.ImplicitFunction
import jscl.math.genericArrayOf
import jscl.math.operator.Operator
import jscl.math.operator.VectorOperator
import jscl.mathml.MathML

class GeometricProduct : VectorOperator {

    constructor(vector1: Generic?, vector2: Generic?, algebra: Generic?) :
            super(NAME, genericArrayOf(vector1, vector2, algebra))

    private constructor(parameters: Array<Generic>) : super(NAME, createParameters(parameters))

    override fun getMinParameters(): Int = 3

    override fun selfExpand(): Generic {
        val algebra = algebra(parameters!![2])
        if (parameters!![0] is JsclVector && parameters!![1] is JsclVector) {
            val v1 = parameters!![0] as JsclVector
            val v2 = parameters!![1] as JsclVector
            return v1.geometricProduct(v2, algebra)
        }
        return expressionValue()
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return GeometricProduct(parameters)
    }

    override fun bodyToMathML(element: MathML) {
        parameters!![0].toMathML(element, null)
        parameters!![1].toMathML(element, null)
    }

    override fun newInstance(): Variable {
        return GeometricProduct(null, null, null)
    }

    companion object {
        const val NAME = "geometric"

        @JvmStatic
        private fun createParameters(parameters: Array<Generic>): Array<Generic> {
            val result = arrayOfNulls<Generic>(3)

            result[0] = parameters[0]
            result[1] = parameters[1]
            result[2] = if (parameters.size > 2) parameters[2] else JsclInteger.valueOf(0)

            return result as Array<Generic>
        }

        @JvmStatic
        fun algebra(generic: Generic): IntArray? {
            if (generic.signum() == 0) return null
            val v = generic.variableValue()
            if (v is ImplicitFunction) {
                val g = v.getParameters()
                val p = g!![0].integerValue().toInt()
                val q = g!![1].integerValue().toInt()
                if (v.compareTo(
                        ImplicitFunction(
                            "cl",
                            arrayOf(JsclInteger.valueOf(p.toLong()), JsclInteger.valueOf(q.toLong())),
                            intArrayOf(0, 0),
                            arrayOf()
                        )
                    ) == 0
                )
                    return intArrayOf(p, q)
            }
            throw ArithmeticException()
        }
    }
}
