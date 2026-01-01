package jscl.math.function

import jscl.math.*
import jscl.mathml.MathML

class Cubic(generic: Generic?) : Algebraic("cubic", if (generic != null) arrayOf(generic) else null) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    override fun rootValue(): Root {
        return Root(
            arrayOf(
                parameters!![0].negate(),
                JsclInteger.valueOf(0),
                JsclInteger.valueOf(0),
                JsclInteger.valueOf(1)
            ),
            0
        )
    }

    override fun antiDerivative(variable: Variable): Generic {
        val r = rootValue()
        val g = r.getParameters()!!
        if (g[0].isPolynomial(variable)) {
            return AntiDerivative.compute(r, variable)
        } else throw NotIntegrableException(this)
    }

    override fun derivative(n: Int): Generic {
        return Constants.Generic.THIRD.multiply(
            Inverse(
                selfExpand().pow(2)
            ).selfExpand()
        )
    }

    override fun selfExpand(): Generic {
        try {
            val en = parameters!![0].integerValue()
            if (en.signum() < 0) {
                // do nothing
            } else {
                val rt = en.nthrt(3)
                if (rt.pow(3).compareTo(en) == 0) return rt
            }
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return selfExpand()
    }

    override fun selfSimplify(): Generic {
        try {
            val en = parameters!![0].integerValue()
            if (en.signum() < 0) return Cubic(en.negate()).selfSimplify().negate()
            else {
                val rt = en.nthrt(3)
                if (rt.pow(3).compareTo(en) == 0) return rt
            }
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).nThRoot(3)
    }

    override fun toJava(): String {
        val buffer = StringBuilder()
        buffer.append(parameters!![0].toJava())
        buffer.append(".pow(")
        buffer.append(Constants.Generic.THIRD.toJava())
        buffer.append(")")
        return buffer.toString()
    }

    override fun bodyToMathML(element: MathML, fenced: Boolean) {
        val e1 = element.element("mroot")
        parameters!![0].toMathML(e1, null)
        JsclInteger.valueOf(3).toMathML(e1, null)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Cubic(null)
    }
}
