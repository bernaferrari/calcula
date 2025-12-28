package jscl.math.operator.matrix

import jscl.math.Generic
import jscl.math.GenericVariable
import jscl.math.Matrix
import jscl.math.Variable
import jscl.math.operator.Operator
import jscl.mathml.MathML

class Determinant : Operator {

    constructor(matrix: Generic?) : super(NAME, matrix?.let { arrayOf(it) })

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 1

    override fun selfExpand(): Generic {
        if (parameters!![0] is Matrix) {
            val matrix = parameters!![0] as Matrix
            return matrix.determinant()
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

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Determinant(parameters)
    }

    internal fun bodyToMathML(e0: MathML) {
        val m = GenericVariable.content(parameters!![0])
        val e1 = e0.element("mfenced")
        e1.setAttribute("open", "|")
        e1.setAttribute("close", "|")
        if (m is Matrix) {
            val element = m.elements()
            val e2 = e0.element("mtable")
            for (i in element.indices) {
                val e3 = e0.element("mtr")
                for (j in element.indices) {
                    val e4 = e0.element("mtd")
                    element[i][j]!!.toMathML(e4, null)
                    e3.appendChild(e4)
                }
                e2.appendChild(e3)
            }
            e1.appendChild(e2)
        } else {
            m.toMathML(e1, null)
        }
        e0.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Determinant(null as Matrix?)
    }

    companion object {
        const val NAME = "det"
    }
}
