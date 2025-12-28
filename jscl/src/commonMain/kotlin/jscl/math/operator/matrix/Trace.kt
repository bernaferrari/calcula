package jscl.math.operator.matrix

import jscl.math.Generic
import jscl.math.Matrix
import jscl.math.Variable
import jscl.math.operator.Operator
import jscl.mathml.MathML

class Trace : Operator {

    constructor(matrix: Generic?) : super(NAME, matrix?.let { arrayOf(it) })

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 1

    override fun selfExpand(): Generic {
        if (parameters!![0] is Matrix) {
            val matrix = parameters!![0] as Matrix
            return matrix.trace()
        }
        return expressionValue()
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) {
            val e1 = element.element("mo")
            e1.appendChild(element.text("tr"))
            element.appendChild(e1)
        } else {
            val e1 = element.element("msup")
            var e2 = element.element("mo")
            e2.appendChild(element.text("tr"))
            e1.appendChild(e2)
            e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
        parameters!![0].toMathML(element, null)
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Trace(parameters)
    }

    override fun newInstance(): Variable {
        return Trace(null as Matrix?)
    }

    companion object {
        const val NAME = "trace"
    }
}
