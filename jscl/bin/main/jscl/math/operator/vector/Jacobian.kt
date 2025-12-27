package jscl.math.operator.vector

import jscl.math.Generic
import jscl.math.genericArrayOf
import jscl.math.GenericVariable
import jscl.math.JsclVector
import jscl.math.Variable
import jscl.math.function.Constant
import jscl.math.operator.Operator
import jscl.math.operator.VectorOperator
import jscl.mathml.MathML

class Jacobian : VectorOperator {

    constructor(vector: Generic?, variable: Generic?) : super(NAME, genericArrayOf(vector, variable))

    private constructor(parameter: Array<Generic>) : super(NAME, parameter)

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        val variable = toVariables(parameters!![1])
        if (parameters!![0] is JsclVector) {
            val vector = parameters!![0] as JsclVector
            return vector.jacobian(variable)
        }
        return expressionValue()
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Jacobian(parameters)
    }

    override fun bodyToMathML(element: MathML) {
        operator(element, "nabla")
        val e1 = element.element("msup")
        parameters!![0].toMathML(e1, null)
        val e2 = element.element("mo")
        e2.appendChild(element.text("T"))
        e1.appendChild(e2)
        element.appendChild(e1)
    }

    protected override fun operator(element: MathML, name: String) {
        val variable = toVariables(GenericVariable.content(parameters!![1]))
        val e1 = element.element("msubsup")
        Constant(name).toMathML(e1, null)
        var e2 = element.element("mrow")
        for (i in variable.indices) {
            variable[i].expressionValue().toMathML(e2, null)
        }
        e1.appendChild(e2)
        e2 = element.element("mo")
        e2.appendChild(element.text("T"))
        e1.appendChild(e2)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Jacobian(null, null)
    }

    companion object {
        const val NAME = "jacobian"
    }
}
