package jscl.math.operator.matrix

import jscl.math.Generic
import jscl.math.Matrix
import jscl.math.Variable
import jscl.math.operator.Operator
import jscl.mathml.MathML

class Transpose : Operator {

    constructor(matrix: Generic?) : super(NAME, matrix?.let { arrayOf(it) })

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 1

    override fun selfExpand(): Generic {
        if (parameters!![0] is Matrix) {
            val matrix = parameters!![0] as Matrix
            return matrix.transpose()
        }
        return expressionValue()
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) {
            bodyToMathML(element)
        } else {
            val e1 = element.element("msup")
            bodyToMathML(e1)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
    }

    internal fun bodyToMathML(element: MathML) {
        val e1 = element.element("msup")
        parameters!![0].toMathML(e1, null)
        val e2 = element.element("mo")
        e2.appendChild(element.text("T"))
        e1.appendChild(e2)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Transpose(null as Matrix?)
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Transpose(parameters)
    }

    companion object {
        const val NAME = "tran"
    }
}
