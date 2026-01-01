package jscl.math.operator

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.NotIntegrableException
import jscl.math.Variable
import jscl.mathml.MathML
import jscl.text.msg.JsclMessage
import jscl.text.msg.Messages
import jscl.common.msg.MessageType

class Integral : Operator {

    constructor(expression: Generic?, variable: Generic?, n1: Generic?, n2: Generic?) :
            super(NAME, genericArrayOf(expression, variable, n1, n2))

    protected constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 4

    override fun selfExpand(): Generic {
        if (JsclMathEngine.getInstance().angleUnits != AngleUnit.rad) {
            JsclMathEngine.getInstance().messageRegistry.addMessage(
                JsclMessage(Messages.msg_24, MessageType.warning)
            )
        }

        val variable = parameters!![1].variableValue()
        try {
            val a = parameters!![0].antiDerivative(variable)
            return a.substitute(variable, parameters!![3]).subtract(a.substitute(variable, parameters!![2]))
        } catch (e: NotIntegrableException) {
        }
        return expressionValue()
    }

    override fun formatUndefinedParameter(i: Int): String {
        return when (i) {
            0 -> "f(x)"
            1 -> "x"
            2 -> "a"
            3 -> "b"
            else -> super.formatUndefinedParameter(i)
        }
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
        return Integral(parameters)
    }

    internal fun bodyToMathML(element: MathML) {
        val v = parameters!![1].variableValue()
        val e1 = element.element("mrow")
        var e2 = element.element("msubsup")
        var e3 = element.element("mo")
        e3.appendChild(element.text("\u222B"))
        e2.appendChild(e3)
        parameters!![2].toMathML(e2, null)
        parameters!![3].toMathML(e2, null)
        e1.appendChild(e2)
        parameters!![0].toMathML(e1, null)
        e2 = element.element("mo")
        e2.appendChild(element.text("d"))
        e1.appendChild(e2)
        v.toMathML(e1, null)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Integral(null, null, null, null)
    }

    companion object {
        const val NAME = "∫ab"
    }
}
