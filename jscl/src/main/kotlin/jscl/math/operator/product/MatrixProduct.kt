package jscl.math.operator.product

import jscl.math.Generic
import jscl.math.Matrix
import jscl.math.Variable
import jscl.math.genericArrayOf
import jscl.math.operator.Operator
import jscl.math.operator.VectorOperator
import jscl.mathml.MathML

class MatrixProduct : VectorOperator {

    constructor(matrix1: Generic?, matrix2: Generic?) : super(NAME, genericArrayOf(matrix1, matrix2))

    private constructor(parameter: Array<Generic>) : super(NAME, parameter)

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        if (Matrix.isMatrixProduct(parameters!![0], parameters!![1])) {
            return parameters!![0].multiply(parameters!![1])
        }
        return expressionValue()
    }

    override fun toJava(): String {
        val result = StringBuilder()
        result.append(parameters!![0].toJava())
        result.append(".multiply(")
        result.append(parameters!![1].toJava())
        result.append(")")
        return result.toString()
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return MatrixProduct(parameters)
    }

    override fun bodyToMathML(element: MathML) {
        parameters!![0].toMathML(element, null)
        parameters!![1].toMathML(element, null)
    }

    override fun newInstance(): Variable {
        return MatrixProduct(null, null)
    }

    companion object {
        const val NAME = "matrix"
    }
}
