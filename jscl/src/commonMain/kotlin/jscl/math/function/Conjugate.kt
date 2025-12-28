package jscl.math.function

import jscl.math.*
import jscl.math.function.Constants.Generic.I
import jscl.mathml.MathML

class Conjugate(generic: Generic?) : Function("conjugate", if (generic != null) arrayOf(generic) else null) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    override fun antiDerivative(n: Int): Generic {
        return Constants.Generic.HALF.multiply(selfExpand().pow(2))
    }

    override fun derivative(n: Int): Generic {
        return JsclInteger.valueOf(1)
    }

    override fun selfExpand(): Generic {
        try {
            return parameters!![0].integerValue()
        } catch (e: NotIntegerException) {
        }
        if (parameters!![0] is Matrix) {
            return (parameters!![0] as Matrix).conjugate()
        } else if (parameters!![0] is JsclVector) {
            return (parameters!![0] as JsclVector).conjugate()
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        try {
            return parameters!![0].integerValue()
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    override fun selfSimplify(): Generic {
        try {
            return parameters!![0].integerValue()
        } catch (e: NotIntegerException) {
        }

        if (parameters!![0].signum() < 0) {
            return Conjugate(parameters!![0].negate()).selfSimplify().negate()
        } else if (parameters!![0].compareTo(I) == 0) {
            return I.negate()
        }

        try {
            val v = parameters!![0].variableValue()
            if (v is Conjugate) {
                val g = v.getParameters()!!
                return g[0]
            } else if (v is Exp) {
                val g = v.getParameters()!!
                return Exp(Conjugate(g[0]).selfSimplify()).selfSimplify()
            } else if (v is Ln) {
                val g = v.getParameters()!!
                return Ln(Conjugate(g[0]).selfSimplify()).selfSimplify()
            } else if (v is Lg) {
                val g = v.getParameters()!!
                return Lg(Conjugate(g[0]).selfSimplify()).selfSimplify()
            }
        } catch (e: NotVariableException) {
            val a = parameters!![0].sumValue()
            if (a.size > 1) {
                var s: Generic = JsclInteger.valueOf(0)
                for (i in a.indices) {
                    s = s.add(Conjugate(a[i]).selfSimplify())
                }
                return s
            } else {
                val p = a[0].productValue()
                var s: Generic = JsclInteger.valueOf(1)
                for (i in p.indices) {
                    val o = p[i].powerValue()
                    s = s.multiply(Conjugate(o.value()).selfSimplify().pow(o.exponent()))
                }
                return s
            }
        }
        val n = Fraction.separateCoefficient(parameters!![0])
        if (n[0].compareTo(JsclInteger.valueOf(1)) == 0 && n[1].compareTo(JsclInteger.valueOf(1)) == 0) {
            // do nothing
        } else {
            return Conjugate(n[2]).selfSimplify().multiply(
                Fraction(n[0], n[1]).selfSimplify()
            )
        }
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).conjugate()
    }

    override fun toJava(): String {
        val buffer = StringBuffer()
        buffer.append(parameters!![0].toJava())
        buffer.append(".conjugate()")
        return buffer.toString()
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) bodyToMathML(element)
        else {
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

    internal fun bodyToMathML(element: MathML) {
        val e1 = element.element("mover")
        parameters!![0].toMathML(e1, null)
        val e2 = element.element("mo")
        e2.appendChild(element.text("_"))
        e1.appendChild(e2)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Conjugate(null)
    }
}
