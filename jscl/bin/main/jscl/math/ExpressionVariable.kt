package jscl.math

import jscl.mathml.MathML

class ExpressionVariable(generic: Generic?) : GenericVariable(generic) {

    override fun substitute(variable: Variable, generic: Generic): Generic {
        return if (isIdentity(variable)) generic else content!!.substitute(variable, generic)
    }

    override fun elementary(): Generic {
        return content!!.elementary()
    }

    override fun simplify(): Generic {
        return content!!.simplify()
    }

    override fun toString(): String {
        return "($content)"
    }

    override fun toJava(): String {
        return "(${content!!.toJava()})"
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

    fun bodyToMathML(element: MathML) {
        val e1 = element.element("mfenced")
        content!!.toMathML(e1, null)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable = ExpressionVariable(null)
}
