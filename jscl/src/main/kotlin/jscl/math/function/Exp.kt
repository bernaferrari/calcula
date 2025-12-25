package jscl.math.function

import jscl.math.*
import jscl.math.polynomial.Polynomial
import jscl.mathml.MathML

class Exp(generic: Generic?) : Function("exp", if (generic != null) arrayOf(generic) else null) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    override fun antiDerivative(variable: Variable): Generic {
        val s = parameters!![0]
        if (s.isPolynomial(variable)) {
            val p = Polynomial.factory(variable).valueOf(s)
            if (p.degree() == 1) {
                val a = p.elements()
                return Inverse(a[1]).selfExpand().multiply(antiDerivative(0))
            } else throw NotIntegrableException(this)
        } else throw NotIntegrableException(this)
    }

    override fun antiDerivative(n: Int): Generic {
        return selfExpand()
    }

    override fun derivative(n: Int): Generic {
        return selfExpand()
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].signum() < 0) {
            return Inverse(Exp(parameters!![0].negate()).selfExpand()).selfExpand()
        } else if (parameters!![0].signum() == 0) {
            return JsclInteger.valueOf(1)
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return selfExpand()
    }

    override fun selfSimplify(): Generic {
        if (parameters!![0].signum() < 0) {
            return Inverse(Exp(parameters!![0].negate()).selfSimplify()).selfSimplify()
        } else if (parameters!![0].signum() == 0) {
            return JsclInteger.valueOf(1)
        } else if (parameters!![0].compareTo(Constants.Generic.I_BY_PI) == 0) {
            return JsclInteger.valueOf(-1)
        }

        try {
            val v = parameters!![0].variableValue()
            if (v is Lg) {
                val g = v.getParameters()!!
                return g[0]
            }
        } catch (e: NotVariableException) {
            val sumElements = parameters!![0].sumValue()
            if (sumElements.size > 1) {
                var result: Generic = JsclInteger.valueOf(1)
                for (sumElement in sumElements) {
                    result = result.multiply(Exp(sumElement).selfSimplify())
                }
                return result
            }
        }

        val n = Fraction.separateCoefficient(parameters!![0])
        if (n[0].compareTo(JsclInteger.valueOf(1)) == 0 && n[1].compareTo(JsclInteger.valueOf(1)) == 0) {
            // do nothing
        } else {
            return Pow(
                Exp(n[2]).selfSimplify(),
                Fraction(n[0], n[1]).selfSimplify()
            ).selfSimplify()
        }
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).exp()
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

    internal fun bodyToMathML(element: MathML, fenced: Boolean) {
        if (fenced) {
            val e1 = element.element("mfenced")
            bodyToMathML(e1)
            element.appendChild(e1)
        } else {
            bodyToMathML(element)
        }
    }

    internal fun bodyToMathML(element: MathML) {
        val e1 = element.element("msup")
        val e2 = element.element("mi")
        e2.appendChild(element.text(/*"\u2147"*/"e"))
        e1.appendChild(e2)
        parameters!![0].toMathML(e1, null)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Exp(null)
    }
}
