package jscl.math.function

import jscl.math.*

class Ln(generic: Generic?) : Function("ln", if (generic != null) arrayOf(generic) else null) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    override fun antiDerivative(n: Int): Generic {
        return parameters!![0].multiply(Ln(parameters!![0]).selfExpand().subtract(JsclInteger.ONE))
    }

    override fun derivative(n: Int): Generic {
        return Inverse(parameters!![0]).selfExpand()
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].compareTo(JsclInteger.valueOf(1)) == 0) {
            return JsclInteger.valueOf(0)
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return selfExpand()
    }

    override fun selfSimplify(): Generic {
        try {
            val en = parameters!![0].integerValue()
            if (en.signum() < 0) {
                return Constants.Generic.I_BY_PI.add(Ln(en.negate()).selfSimplify())
            } else {
                val a = en.factorize()
                val p = a.productValue()
                var s: Generic = JsclInteger.valueOf(0)
                for (i in p.indices) {
                    val o = p[i].powerValue()
                    val exponent = JsclInteger.valueOf(o.exponent().toLong())
                    s = s.add(exponent.multiply(Ln(o.value(true)).expressionValue()))
                }
                return s
            }
        } catch (e: NotIntegerException) {
        }
        try {
            val v = parameters!![0].variableValue()
            if (v is Sqrt) {
                val g = v.getParameters()!!
                return Constants.Generic.HALF.multiply(Ln(g[0]).selfSimplify())
            }
        } catch (e: NotVariableException) {
        }
        val n = Fraction.separateCoefficient(parameters!![0])
        if (n[0].compareTo(JsclInteger.valueOf(1)) == 0 && n[1].compareTo(JsclInteger.valueOf(1)) == 0) {
            // do nothing
        } else {
            return Ln(n[2]).selfSimplify().add(
                Ln(n[0]).selfSimplify()
            ).subtract(
                Ln(n[1]).selfSimplify()
            )
        }
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).ln()
    }

    override fun newInstance(): Variable {
        return Ln(null)
    }
}
