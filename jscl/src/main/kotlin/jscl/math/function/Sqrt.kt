package jscl.math.function

import jscl.math.*
import jscl.mathml.MathML

class Sqrt(parameter: Generic?) : Algebraic("√", if (parameter != null) arrayOf(parameter) else null) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    override fun rootValue(): Root {
        return Root(arrayOf(parameters!![0].negate(), JsclInteger.valueOf(0), JsclInteger.valueOf(1)), 0)
    }

    override fun antiDerivative(variable: Variable): Generic {
        val r = rootValue()
        val g = r.getParameters()!!
        return if (g[0].isPolynomial(variable)) {
            AntiDerivative.compute(r, variable)
        } else {
            throw NotIntegrableException(this)
        }
    }

    override fun derivative(n: Int): Generic {
        return Constants.Generic.HALF.multiply(Inverse(selfExpand()).selfExpand())
    }

    fun imaginary(): Boolean {
        val param = requireNotNull(parameters)[0]
        return param.compareTo(JsclInteger.valueOf(-1)) == 0
    }

    override fun selfExpand(): Generic {
        try {
            val p = parameters!![0].integerValue()
            return if (p.signum() < 0) {
                // result will be complex => evaluate
                expressionValue()
            } else {
                val sqrt = p.sqrt()
                if (sqrt.pow(2).compareTo(p) == 0) {
                    sqrt
                } else {
                    expressionValue()
                }
            }
        } catch (e: NotIntegerException) {
            return expressionValue()
        }
    }

    override fun selfElementary(): Generic {
        return selfExpand()
    }

    override fun selfSimplify(): Generic {
        var result: Generic? = null
        try {
            val p = parameters!![0].integerValue()
            if (p.signum() < 0) {
                return Constants.Generic.I.multiply(Sqrt(p.negate()).selfSimplify())
            } else {
                val sqrt = p.sqrt()
                if (sqrt.pow(2).compareTo(p) == 0) {
                    return sqrt
                }
            }
            result = simplify0(p)
        } catch (e: NotIntegerException) {
            result = simplify0(parameters!![0])
        }

        return result ?: expressionValue()
    }

    private fun simplifyFractions(): Generic? {
        val n = Fraction.separateCoefficient(parameters!![0])

        if (n[0].compareTo(JsclInteger.valueOf(1)) != 0 || n[1].compareTo(JsclInteger.valueOf(1)) != 0) {
            // n
            val numerator = Sqrt(n[0]).selfSimplify()
            // d
            val denominator = Sqrt(n[1]).selfSimplify()
            // fraction = n / d
            val fraction = Fraction(numerator, denominator).selfSimplify()
            return Sqrt(n[2]).selfSimplify().multiply(fraction)
        }

        return null
    }

    private fun simplify0(generic: Generic): Generic? {
        try {
            // let's try to present sqrt expression as products
            val products = generic.factorize().productValue()

            var result: Generic = JsclInteger.valueOf(1)
            for (product in products) {
                // and try sqrt for each product
                val power = product.powerValue()
                val q = power.value(true)
                val c = power.exponent()
                result = result.multiply(q.pow(c / 2).multiply(Sqrt(q).expressionValue().pow(c % 2)))
            }
            return result
        } catch (e: NotPowerException) {
            return simplifyFractions()
        } catch (e: NotProductException) {
            return simplifyFractions()
        }
    }

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).sqrt()
    }

    override fun toJava(): String {
        return if (parameters!![0].compareTo(JsclInteger.valueOf(-1)) == 0) {
            "Complex.valueOf(0, 1)"
        } else {
            val result = StringBuilder()
            result.append(parameters!![0].toJava())
            result.append(".").append(name).append("()")
            result.toString()
        }
    }

    override fun toString(): String {
        val parameter = requireNotNull(parameters)[0]
        return try {
            if (JsclInteger.ONE.negate() == parameter.integerValue()) {
                Constants.I.name
            } else {
                super.toString()
            }
        } catch (e: NotIntegerException) {
            return super.toString()
        }
    }

    override fun bodyToMathML(element: MathML, fenced: Boolean) {
        if (parameters!![0].compareTo(JsclInteger.valueOf(-1)) == 0) {
            val e1 = element.element("mi")
            e1.appendChild(element.text(/*"\u2148"*/"i"))
            element.appendChild(e1)
        } else {
            val e1 = element.element("msqrt")
            parameters!![0].toMathML(e1, null)
            element.appendChild(e1)
        }
    }

    override fun newInstance(): Variable {
        return Sqrt(null)
    }
}
