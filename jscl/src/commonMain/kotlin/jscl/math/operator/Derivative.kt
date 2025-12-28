@file:Suppress("UNCHECKED_CAST")

package jscl.math.operator

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.JsclInteger
import jscl.math.NotIntegerException
import jscl.math.Variable
import jscl.mathml.MathML
import jscl.text.msg.JsclMessage
import jscl.text.msg.Messages
import org.solovyev.common.msg.MessageType

class Derivative : Operator {

    constructor(expression: Generic?, variable: Generic?, value: Generic?, order: Generic?) :
            super(NAME, genericArrayOf(expression, variable, value, order))

    private constructor(parameters: Array<Generic>) : super(NAME, createParameters(parameters))

    override fun getMinParameters(): Int = 2

    override fun getMaxParameters(): Int = 4

    override fun formatUndefinedParameter(i: Int): String {
        return when (i) {
            0 -> "f(x)"
            1 -> "x"
            2 -> "x_point"
            3 -> "order"
            else -> super.formatUndefinedParameter(i)
        }
    }

    override fun selfExpand(): Generic {
        if (JsclMathEngine.getInstance().angleUnits != AngleUnit.rad) {
            JsclMathEngine.getInstance().messageRegistry.addMessage(
                JsclMessage(Messages.msg_25, MessageType.warning)
            )
        }

        val variable = parameters!![1].variableValue()
        try {
            val n = parameters!![3].integerValue().toInt()
            var a = parameters!![0]
            for (i in 0 until n) {
                a = a.derivative(variable)
            }
            return a.substitute(variable, parameters!![2])
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) {
            derivationToMathML(element, false)
        } else {
            val e1 = element.element("msup")
            derivationToMathML(e1, true)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
        val e1 = element.element("mfenced")
        parameters!![0].toMathML(e1, null)
        if (parameters!![2].compareTo(parameters!![1]) != 0) {
            parameters!![2].toMathML(e1, null)
        }
        element.appendChild(e1)
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Derivative(parameters)
    }

    internal fun derivationToMathML(element: MathML, fenced: Boolean) {
        if (fenced) {
            val e1 = element.element("mfenced")
            derivationToMathML(e1)
            element.appendChild(e1)
        } else {
            derivationToMathML(element)
        }
    }

    override fun numeric(): Generic {
        try {
            parameters!![3].integerValue()
            return expand().numeric()
        } catch (e: NotIntegerException) {
        }

        return expressionValue()
    }

    internal fun derivationToMathML(element: MathML) {
        val v = parameters!![1].variableValue()
        var n = 0
        try {
            n = parameters!![3].integerValue().toInt()
        } catch (e: NotIntegerException) {
        }
        if (n == 1) {
            val e1 = element.element("mfrac")
            var e2 = element.element("mo")
            e2.appendChild(element.text("d"))
            e1.appendChild(e2)
            e2 = element.element("mrow")
            var e3 = element.element("mo")
            e3.appendChild(element.text("d"))
            e2.appendChild(e3)
            v.toMathML(e2, null)
            e1.appendChild(e2)
            element.appendChild(e1)
        } else {
            val e1 = element.element("mfrac")
            var e2 = element.element("msup")
            var e3 = element.element("mo")
            e3.appendChild(element.text("d"))
            e2.appendChild(e3)
            parameters!![3].toMathML(e2, null)
            e1.appendChild(e2)
            e2 = element.element("mrow")
            e3 = element.element("mo")
            e3.appendChild(element.text("d"))
            e2.appendChild(e3)
            e3 = element.element("msup")
            parameters!![1].toMathML(e3, null)
            parameters!![3].toMathML(e3, null)
            e2.appendChild(e3)
            e1.appendChild(e2)
            element.appendChild(e1)
        }
    }

    override fun newInstance(): Variable {
        return Derivative(null, null, null, null)
    }

    companion object {
        const val NAME = "∂"

        private fun createParameters(parameters: Array<Generic>): Array<Generic> {
            val result = arrayOfNulls<Generic>(4)

            result[0] = parameters[0]
            result[1] = parameters[1]
            result[2] = if (parameters.size > 2) parameters[2] else parameters[1]
            result[3] = if (parameters.size > 3) parameters[3] else JsclInteger.valueOf(1)

            return result as Array<Generic>
        }
    }
}
