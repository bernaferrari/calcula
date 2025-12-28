package jscl.math.function

import jscl.math.*
import jscl.mathml.MathML

open class Fraction(numerator: Generic?, denominator: Generic?) : Algebraic("frac", if (numerator != null && denominator != null) arrayOf(numerator, denominator) else null) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    override fun getMinParameters(): Int {
        return 2
    }

    override fun rootValue(): Root {
        val params = requireNotNull(parameters)
        return Root(arrayOf(params[0].negate(), params[1]), 0)
    }

    override fun antiDerivative(variable: Variable): Generic {
        val params = requireNotNull(parameters)
        if (params[0].isPolynomial(variable) && params[1].isPolynomial(variable)) {
            return AntiDerivative.compute(this, variable)
        } else throw NotIntegrableException(this)
    }

    override fun derivative(n: Int): Generic {
        val params = requireNotNull(parameters)
        return if (n == 0) {
            Inverse(params[1]).selfExpand()
        } else {
            params[0].multiply(Inverse(params[1]).selfExpand().pow(2).negate())
        }
    }

    fun integer(): Boolean {
        try {
            val params = requireNotNull(parameters)
            params[0].integerValue().toInt()
            params[1].integerValue().toInt()
            return true
        } catch (e: NotIntegerException) {
        }
        return false
    }

    override fun selfExpand(): Generic {
        val params = requireNotNull(parameters)
        val nonFinite = nonFiniteDivideResult(params[0], params[1])
        if (nonFinite != null) {
            return nonFinite
        }
        if (params[0].compareTo(JsclInteger.valueOf(1)) == 0) {
            return Inverse(params[1]).selfExpand()
        }
        try {
            return params[0].divide(params[1])
        } catch (e: NotDivisibleException) {
        } catch (e: ArithmeticException) {
        }
        return expressionValue()
    }

    private fun nonFiniteDivideResult(numerator: Generic, denominator: Generic): Generic? {
        val left = numericDoubleOrNull(numerator) ?: return null
        val right = numericDoubleOrNull(denominator) ?: return null
        val result = left / right
        return if (result.isNaN() || result.isInfinite()) {
            NumericWrapper.valueOf(result)
        } else {
            null
        }
    }

    private fun numericDoubleOrNull(generic: Generic): Double? {
        val numeric = try {
            generic.numeric()
        } catch (_: Exception) {
            return null
        }
        val wrapper = numeric as? NumericWrapper ?: return null
        val content = wrapper.content()
        return if (content is jscl.math.numeric.Real) {
            content.doubleValue()
        } else {
            null
        }
    }

    override fun selfElementary(): Generic {
        return selfExpand()
    }

    override fun selfSimplify(): Generic {
        val params = requireNotNull(parameters)
        if (params[0].signum() < 0) {
            return Fraction(params[0].negate(), params[1]).selfSimplify().negate()
        }
        if (params[1].signum() < 0) {
            return Fraction(params[0].negate(), params[1].negate()).selfSimplify()
        }
        return selfExpand()
    }

    override fun selfNumeric(): Generic {
        val params = requireNotNull(parameters)
        return (params[0] as NumericWrapper).divide(params[1] as NumericWrapper)
    }

    override fun toString(): String {
        val result = StringBuilder()
        val params = requireNotNull(parameters)

        try {
            params[0].powerValue()
            result.append(params[0])
        } catch (e: NotPowerException) {
            result.append(GenericVariable.valueOf(params[0]))
        }

        result.append("/")

        try {
            val v = params[1].variableValue()
            if (v is Fraction) {
                result.append(GenericVariable.valueOf(params[1]))
            } else {
                result.append(v)
            }
        } catch (e: NotVariableException) {
            try {
                params[1].abs().powerValue()
                result.append(params[1])
            } catch (e2: NotPowerException) {
                result.append(GenericVariable.valueOf(params[1]))
            }
        }
        return result.toString()
    }

    override fun toJava(): String {
        val result = StringBuilder()
        val params = requireNotNull(parameters)
        result.append(params[0].toJava())
        result.append(".divide(")
        result.append(params[1].toJava())
        result.append(")")
        return result.toString()
    }

    override fun bodyToMathML(element: MathML, fenced: Boolean) {
        if (fenced) {
            val e1 = element.element("mfenced")
            bodyToMathML(e1)
            element.appendChild(e1)
        } else {
            bodyToMathML(element)
        }
    }

    internal fun bodyToMathML(element: MathML) {
        val e1 = element.element("mfrac")
        val params = requireNotNull(parameters)
        params[0].toMathML(e1, null)
        params[1].toMathML(e1, null)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Fraction(null, null)
    }

    companion object {
        /**
         * @param generic any generic value
         * @return array of 3 elements where
         * a[0] =
         */
        fun separateCoefficient(generic: Generic): Array<Generic> {
            if (generic.signum() < 0) {
                val n = separateCoefficient(generic.negate())
                return arrayOf(n[0], n[1], n[2].negate())
            }

            try {
                val v = generic.variableValue()
                if (v is Fraction) {
                    val parameters = v.getParameters()!!

                    // v = n / d

                    // numerator
                    val n = parameters[0].expressionValue()

                    // denumerator
                    val d = parameters[1].expressionValue()

                    // na = [gcd(n), n/(gcd(n))]
                    val na = n.gcdAndNormalize()
                    // nd = [gcd(d), d/(gcd(d))]
                    val nd = d.gcdAndNormalize()
                    return arrayOf(na[0], nd[0], Fraction(na[1], nd[1]).selfExpand())
                }
            } catch (e: NotVariableException) {
                try {
                    val a = generic.expressionValue()
                    val n = a.gcdAndNormalize()
                    return arrayOf(n[0], JsclInteger.valueOf(1), n[1])
                } catch (e2: NotExpressionException) {
                }
            }

            return arrayOf(JsclInteger.valueOf(1), JsclInteger.valueOf(1), generic)
        }
    }
}
