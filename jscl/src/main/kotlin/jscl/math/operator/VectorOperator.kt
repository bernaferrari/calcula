package jscl.math.operator

import jscl.math.Generic
import jscl.math.GenericVariable
import jscl.math.JsclVector
import jscl.math.Variable
import jscl.math.function.Constant
import jscl.mathml.MathML

abstract class VectorOperator(name: String, parameter: Array<Generic>?) : Operator(name, parameter) {

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) {
            bodyToMathML(element)
        } else {
            val e1 = element.element("msup")
            val e2 = element.element("mfenced")
            bodyToMathML(e2)
            e1.appendChild(e2)
            val e3 = element.element("mn")
            e3.appendChild(element.text(exponent.toString()))
            e1.appendChild(e3)
            element.appendChild(e1)
        }
    }

    protected abstract fun bodyToMathML(element: MathML)

    protected open fun operator(element: MathML, name: String) {
        val variable = toVariables(GenericVariable.content(parameters!![1]) as JsclVector)
        val e1 = element.element("msub")
        Constant(name).toMathML(e1, null)
        val e2 = element.element("mrow")
        for (i in variable.indices) {
            variable[i].expressionValue().toMathML(e2, null)
        }
        e1.appendChild(e2)
        element.appendChild(e1)
    }
}
