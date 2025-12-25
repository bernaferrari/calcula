package jscl.math.operator

import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.JsclInteger
import jscl.math.Variable
import jscl.math.function.Constants
import jscl.mathml.MathML

class Limit : Operator {

    constructor(expression: Generic?, variable: Generic?, limit: Generic?, direction: Generic?) :
            super(NAME, genericArrayOf(expression, variable, limit, direction))

    private constructor(parameters: Array<Generic>) : super(NAME, createParameters(parameters))

    override fun getMinParameters(): Int = 3

    override fun getMaxParameters(): Int = 4

    override fun selfExpand(): Generic {
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
        return Limit(parameters)
    }

    internal fun bodyToMathML(element: MathML) {
        val c = parameters!![3].signum()
        val e1 = element.element("mrow")
        val e2 = element.element("munder")
        var e3 = element.element("mo")
        e3.appendChild(element.text("lim"))
        e2.appendChild(e3)
        e3 = element.element("mrow")
        parameters!![1].toMathML(e3, null)
        var e4 = element.element("mo")
        e4.appendChild(element.text("\u2192"))
        e3.appendChild(e4)
        if (c == 0) {
            parameters!![2].toMathML(e3, null)
        } else {
            e4 = element.element("msup")
            parameters!![2].toMathML(e4, null)
            val e5 = element.element("mo")
            if (c < 0) {
                e5.appendChild(element.text("-"))
            } else if (c > 0) {
                e5.appendChild(element.text("+"))
            }
            e4.appendChild(e5)
            e3.appendChild(e4)
        }
        e2.appendChild(e3)
        e1.appendChild(e2)
        parameters!![0].toMathML(e1, null)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Limit(null, null, null, null)
    }

    companion object {
        const val NAME = "lim"

        @JvmStatic
        private fun createParameters(parameters: Array<Generic>): Array<Generic> {
            val result = arrayOfNulls<Generic>(4)

            result[0] = parameters[0]
            result[1] = parameters[1]
            result[2] = parameters[2]
            result[3] = if (parameters.size > 3 && (parameters[2].compareTo(Constants.Generic.INF) != 0 && parameters[2].compareTo(
                    Constants.Generic.INF.negate()
                ) != 0)
            ) JsclInteger.valueOf(parameters[3].signum().toLong()) else JsclInteger.valueOf(0)

            return result as Array<Generic>
        }
    }
}
