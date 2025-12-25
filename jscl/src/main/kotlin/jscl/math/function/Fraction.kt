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
        return Root(arrayOf(parameters!![0].negate(), parameters!![1]), 0)
    }

    override fun antiDerivative(variable: Variable): Generic {
        if (parameters!![0].isPolynomial(variable) && parameters!![1].isPolynomial(variable)) {
            return AntiDerivative.compute(this, variable)
        } else throw NotIntegrableException(this)
    }

    override fun derivative(n: Int): Generic {
        return if (n == 0) {
            Inverse(parameters!![1]).selfExpand()
        } else {
            parameters!![0].multiply(Inverse(parameters!![1]).selfExpand().pow(2).negate())
        }
    }

    fun integer(): Boolean {
        try {
            if (parameters!![0] != null && parameters!![1] != null) {
                parameters!![0].integerValue().toInt()
                parameters!![1].integerValue().toInt()
                return true
            }
        } catch (e: NotIntegerException) {
        }
        return false
    }

    override fun selfExpand(): Generic {
        if (parameters!![0].compareTo(JsclInteger.valueOf(1)) == 0) {
            return Inverse(parameters!![1]).selfExpand()
        }
        try {
            return parameters!![0].divide(parameters!![1])
        } catch (e: NotDivisibleException) {
        } catch (e: ArithmeticException) {
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return selfExpand()
    }

    override fun selfSimplify(): Generic {
        if (parameters!![0].signum() < 0) {
            return Fraction(parameters!![0].negate(), parameters!![1]).selfSimplify().negate()
        }
        if (parameters!![1].signum() < 0) {
            return Fraction(parameters!![0].negate(), parameters!![1].negate()).selfSimplify()
        }
        return selfExpand()
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).divide(parameters!![1] as NumericWrapper)
    }

    override fun toString(): String {
        val result = StringBuilder()

        try {
            parameters!![0].powerValue()
            result.append(parameters!![0])
        } catch (e: NotPowerException) {
            result.append(GenericVariable.valueOf(parameters!![0]))
        }

        result.append("/")

        try {
            val v = parameters!![1].variableValue()
            if (v is Fraction) {
                result.append(GenericVariable.valueOf(parameters!![1]))
            } else {
                result.append(v)
            }
        } catch (e: NotVariableException) {
            try {
                parameters!![1].abs().powerValue()
                result.append(parameters!![1])
            } catch (e2: NotPowerException) {
                result.append(GenericVariable.valueOf(parameters!![1]))
            }
        }
        return result.toString()
    }

    override fun toJava(): String {
        val result = StringBuilder()
        result.append(parameters!![0].toJava())
        result.append(".divide(")
        result.append(parameters!![1].toJava())
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
        parameters!![0].toMathML(e1, null)
        parameters!![1].toMathML(e1, null)
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
        @JvmStatic
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
