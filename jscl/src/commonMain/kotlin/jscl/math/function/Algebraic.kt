package jscl.math.function

import jscl.math.Generic
import jscl.math.NotIntegrableException
import jscl.mathml.MathML

abstract class Algebraic(name: String, parameters: Array<Generic>?) : Function(name, parameters) {

    @Throws(NotRootException::class)
    abstract fun rootValue(): Root

    override fun antiDerivative(n: Int): Generic {
        throw NotIntegrableException(this)
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) bodyToMathML(element, false)
        else {
            val e1 = element.element("msup")
            bodyToMathML(e1, true)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
    }

    internal abstract fun bodyToMathML(element: MathML, fenced: Boolean)
}
