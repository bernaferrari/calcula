package jscl.math.operator

import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.JsclInteger
import jscl.math.NotIntegerException
import jscl.math.Variable
import jscl.mathml.MathML

class Sum : Operator {

    constructor(expression: Generic?, variable: Generic?, from: Generic?, to: Generic?) :
            super(NAME, genericArrayOf(expression, variable, from, to))

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 4

    override fun formatUndefinedParameter(i: Int): String {
        return when (i) {
            0 -> "f(i)"
            1 -> "i"
            2 -> "from"
            3 -> "to"
            else -> super.formatUndefinedParameter(i)
        }
    }

    override fun selfExpand(): Generic {
        val variable = parameters!![1].variableValue()
        try {
            val from = parameters!![2].integerValue().toInt()
            val to = parameters!![3].integerValue().toInt()

            var result: Generic = JsclInteger.ZERO
            for (i in from..to) {
                result = result.add(parameters!![0].substitute(variable, JsclInteger.valueOf(i.toLong())))
            }
            return result

        } catch (e: NotIntegerException) {
            // ok
        }
        return expressionValue()
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) {
            bodyToMathML(element)
        } else {
            val e1 = element.element("msup")
            var e2 = element.element("mfenced")
            bodyToMathML(e2)
            e1.appendChild(e2)
            e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Sum(parameters)
    }

    internal fun bodyToMathML(element: MathML) {
        val e1 = element.element("mrow")
        val e2 = element.element("munderover")
        var e3 = element.element("mo")
        e3.appendChild(element.text("\u2211"))
        e2.appendChild(e3)
        e3 = element.element("mrow")
        parameters!![1].toMathML(e3, null)
        val e4 = element.element("mo")
        e4.appendChild(element.text("="))
        e3.appendChild(e4)
        parameters!![2].toMathML(e3, null)
        e2.appendChild(e3)
        parameters!![3].toMathML(e2, null)
        e1.appendChild(e2)
        parameters!![0].toMathML(e1, null)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Sum(null, null, null, null)
    }

    companion object {
        const val NAME = "Σ"
    }
}
