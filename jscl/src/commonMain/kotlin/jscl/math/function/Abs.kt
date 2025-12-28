package jscl.math.function

import jscl.math.*
import jscl.mathml.MathML

class Abs(generic: Generic?) : Function("abs", if (generic != null) arrayOf(generic) else null) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    override fun antiDerivative(n: Int): Generic {
        return Constants.Generic.HALF.multiply(parameters!![0]).multiply(Abs(parameters!![0]).selfExpand())
    }

    override fun derivative(n: Int): Generic {
        return Sgn(parameters!![0]).selfExpand()
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].signum() < 0) {
            return Abs(parameters!![0].negate()).selfExpand()
        }
        try {
            return parameters!![0].integerValue().abs()
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return Sqrt(
            parameters!![0].pow(2)
        ).selfElementary()
    }

    override fun selfSimplify(): Generic {
        if (parameters!![0].signum() < 0) {
            return Abs(parameters!![0].negate()).selfSimplify()
        }
        try {
            return parameters!![0].integerValue().abs()
        } catch (e: NotIntegerException) {
        }
        try {
            val v = parameters!![0].variableValue()
            if (v is Abs) {
                return v.selfSimplify()
            } else if (v is Sgn) {
                return JsclInteger.valueOf(1)
            }
        } catch (e: NotVariableException) {
        }
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        return parameters!![0].abs()
    }

    override fun toJava(): String {
        val result = StringBuilder()
        result.append(parameters!![0].toJava())
        result.append(".abs()")
        return result.toString()
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) bodyToMathML(element)
        else {
            val e1 = element.element("msup")
            bodyToMathML(e1)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
    }

    internal fun bodyToMathML(element: MathML) {
        val e1 = element.element("mfenced")
        e1.setAttribute("open", "|")
        e1.setAttribute("close", "|")
        parameters!![0].toMathML(e1, null)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Abs(null)
    }
}
