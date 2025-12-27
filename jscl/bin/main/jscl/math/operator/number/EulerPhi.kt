package jscl.math.operator.number

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NotIntegerException
import jscl.math.Variable
import jscl.math.operator.Operator
import jscl.mathml.MathML

class EulerPhi : Operator {

    constructor(integer: Generic?) : super(NAME, integer?.let { arrayOf(it) })

    private constructor(parameters: Array<Generic>) : super(NAME, parameters)

    override fun getMinParameters(): Int = 1

    override fun selfExpand(): Generic {
        try {
            val en = parameters!![0].integerValue()
            return en.phi()
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    override fun nameToMathML(element: MathML) {
        val e1 = element.element("mi")
        e1.appendChild(element.text("\u03C6"))
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return EulerPhi(null as Generic?)
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return EulerPhi(parameters)
    }

    companion object {
        const val NAME = "eulerphi"
    }
}
