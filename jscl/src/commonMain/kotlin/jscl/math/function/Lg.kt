package jscl.math.function

import jscl.math.*

class Lg(generic: Generic?) : Function("lg", if (generic != null) arrayOf(generic) else null) {

    companion object {
        private val ONE = JsclInteger.valueOf(1)
        private val ZERO = JsclInteger.valueOf(0)
    }

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    override fun antiDerivative(n: Int): Generic {
        // tmp = ln(x) - 1
        val tmp = Ln(parameters!![0]).expressionValue().subtract(ONE)

        // ln10 = ln (10)
        val ln10 = Ln(JsclInteger.valueOf(10L)).expressionValue()
        return Fraction(parameters!![0].multiply(tmp), ln10).expressionValue()
    }

    override fun derivative(n: Int): Generic {
        return Inverse(parameters!![0].multiply(Ln(JsclInteger.valueOf(10L)).expressionValue())).expressionValue()
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
        val coefficients = Fraction.separateCoefficient(parameters!![0])
        val a = coefficients[0]
        val b = coefficients[1]
        val c = coefficients[2]

        val aOne = a.compareTo(ONE) == 0
        val bOne = b.compareTo(ONE) == 0
        val cOne = c.compareTo(ONE) == 0

        if (aOne && bOne && cOne) {
            return ZERO
        } else {
            if (aOne && bOne) {
                return expressionValue()
            } else if (bOne && cOne) {
                return expressionValue()
            } else {
                // lg ( a * c / b ) = lg ( c ) + lg( a ) - lg (b)
                val lga = lg(a, aOne)
                val lgb = lg(b, bOne)
                val lgc = lg(c, cOne)
                return lgc.add(lga).subtract(lgb)
            }
        }
    }

    private fun lg(a: Generic, aOne: Boolean): Generic {
        return if (aOne) {
            ZERO
        } else {
            Lg(a).selfSimplify()
        }
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).lg()
    }

    override fun newInstance(): Variable {
        return Lg(null)
    }
}
