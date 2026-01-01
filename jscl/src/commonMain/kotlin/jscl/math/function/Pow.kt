package jscl.math.function

import jscl.math.*
import jscl.mathml.MathML
import kotlin.math.pow

class Pow(generic: Generic?, exponent: Generic?) : Algebraic("pow", if (generic == null || exponent == null) null else arrayOf(generic, exponent)) {

    override fun getMinParameters(): Int {
        return 2
    }

    override fun rootValue(): Root {
        try {
            val v = parameters!![1].variableValue()
            if (v is Inverse) {
                val g = v.parameter()
                try {
                    val d = g.integerValue().toInt()
                    if (d > 0 && d < MAX_ARRAY_SIZE) {
                        val a = Array<Generic>(d + 1) { i ->
                            when {
                                i == 0 -> parameters!![0].negate()
                                i < d -> JsclInteger.ZERO
                                else -> JsclInteger.ONE
                            }
                        }
                        return Root(a, 0)
                    }
                } catch (e: NotIntegerException) {
                    // Value is not an integer
                } catch (e: ArithmeticException) {
                    // Value is too large to fit in Int (kotlin-bignum throws this)
                }
            }
        } catch (e: NotVariableException) {
        }
        throw NotRootException()
    }

    override fun antiDerivative(variable: Variable): Generic {
        try {
            val r = rootValue()
            val g = r.getParameters()
            if (g!![0].isPolynomial(variable)) {
                return AntiDerivative.compute(r, variable)
            } else throw NotIntegrableException(this)
        } catch (e: NotRootException) {
        }
        return super.antiDerivative(variable)
    }

    override fun antiDerivative(n: Int): Generic {
        return if (n == 0) {
            Pow(parameters!![0], parameters!![1].add(JsclInteger.valueOf(1))).selfExpand().multiply(Inverse(parameters!![1].add(JsclInteger.valueOf(1))).selfExpand())
        } else {
            Pow(parameters!![0], parameters!![1]).selfExpand().multiply(Inverse(Ln(parameters!![0]).selfExpand()).selfExpand())
        }
    }

    override fun derivative(n: Int): Generic {
        return if (n == 0) {
            Pow(parameters!![0], parameters!![1].subtract(JsclInteger.valueOf(1))).selfExpand().multiply(parameters!![1])
        } else {
            Pow(parameters!![0], parameters!![1]).selfExpand().multiply(Ln(parameters!![0]).selfExpand())
        }
    }

    override fun selfExpand(): Generic {
        val oddRoot = oddNegativeRootResult()
        if (oddRoot != null) {
            return oddRoot
        }
        val nonFinite = nonFinitePowResult()
        if (nonFinite != null) {
            return nonFinite
        }
        if (parameters!![0].compareTo(JsclInteger.valueOf(1)) == 0) {
            return JsclInteger.valueOf(1)
        }
        if (parameters!![1].signum() < 0) {
            return Pow(Inverse(parameters!![0]).selfExpand(), parameters!![1].negate()).selfExpand()
        }
        try {
            val c = parameters!![1].integerValue().toInt()
            return parameters!![0].pow(c)
        } catch (e: NotIntegerException) {
        }
        try {
            val r = rootValue()
            val d = r.degree()
            val g = r.getParameters()
            val a = g!![0].negate()
            try {
                val en = a.integerValue()
                if (en.signum() < 0) {
                    // do nothing
                } else {
                    val rt = en.nthrt(d)
                    if (rt.pow(d).compareTo(en) == 0) return rt
                }
            } catch (e: NotIntegerException) {
            }
        } catch (e: NotRootException) {
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return Exp(
            Ln(
                parameters!![0]
            ).selfElementary().multiply(
                parameters!![1]
            )
        ).selfElementary()
    }

    override fun selfSimplify(): Generic {
        // a ^ b
        val oddRoot = oddNegativeRootResult()
        if (oddRoot != null) {
            return oddRoot
        }
        val nonFinite = nonFinitePowResult()
        if (nonFinite != null) {
            return nonFinite
        }

        // a = 1 => for any b: 1 ^ b = 1
        if (parameters!![0].compareTo(JsclInteger.ONE) == 0) {
            return JsclInteger.valueOf(1)
        }

        // b < 0 => a ^ b = (1 / a) ^ (-b)
        if (parameters!![1].signum() < 0) {
            return Pow(Inverse(parameters!![0]).selfSimplify(), parameters!![1].negate()).selfSimplify()
        }

        try {
            // if b is integer => just calculate the result
            val intPower = parameters!![1].integerValue().toInt()
            return parameters!![0].pow(intPower)
        } catch (e: NotIntegerException) {
        }

        try {
            val r = rootValue()
            val d = r.degree()
            val g = r.getParameters()
            val a = g!![0].negate()
            try {
                val en = a.integerValue()
                if (en.signum() < 0) {
                    // do nothing
                } else {
                    val rt = en.nthrt(d)
                    if (rt.pow(d).compareTo(en) == 0) return rt
                }
            } catch (e: NotIntegerException) {
            }
            when (d) {
                2 -> return Sqrt(a).selfSimplify()
                3, 4, 6 -> if (a.compareTo(JsclInteger.valueOf(-1)) == 0) return root_minus_1(d)!!
            }
        } catch (e: NotRootException) {
            val n = Fraction.separateCoefficient(parameters!![1])
            if (n[0].compareTo(JsclInteger.ONE) == 0 && n[1].compareTo(JsclInteger.ONE) == 0) {
                // do nothing
            } else {
                return Pow(
                    Pow(
                        Pow(
                            parameters!![0],
                            n[2]
                        ).selfSimplify(),
                        Inverse(
                            n[1]
                        ).selfSimplify()
                    ).selfSimplify(),
                    n[0]
                ).selfSimplify()
            }
        }
        return expressionValue()
    }

    private fun nonFinitePowResult(): Generic? {
        val base = numericDoubleOrNull(parameters!![0]) ?: return null
        val exponent = numericDoubleOrNull(parameters!![1]) ?: return null

        if (base.isNaN() || exponent.isNaN()) {
            return NumericWrapper.valueOf(Double.NaN)
        }

        if (base.isInfinite() || exponent.isInfinite()) {
            return NumericWrapper.valueOf(base.pow(exponent))
        }

        val result = base.pow(exponent)
        return if (result.isNaN() || result.isInfinite()) {
            NumericWrapper.valueOf(result)
        } else {
            null
        }
    }

    private fun oddNegativeRootResult(): Generic? {
        val baseValue = numericDoubleOrNull(parameters!![0]) ?: return null
        if (baseValue.isNaN() || baseValue.isInfinite()) {
            return null
        }
        if (baseValue >= 0.0) {
            return null
        }
        val exponentValue = parseRationalExponent(parameters!![1]) ?: return null
        val (numerator, denominator) = exponentValue
        if (denominator <= 0 || denominator % 2 == 0) {
            return null
        }
        val absBase = kotlin.math.abs(baseValue)
        val root = jscl.math.numeric.Real.valueOf(absBase).nThRoot(denominator)
        val signedRoot = root.negate()
        val power = if (numerator == 1) {
            signedRoot
        } else {
            val exponent = kotlin.math.abs(numerator)
            val powResult = signedRoot.pow(exponent)
            if (numerator < 0) {
                jscl.math.numeric.Real.ONE.divide(powResult)
            } else {
                powResult
            }
        }
        return NumericWrapper(power)
    }

    private fun parseRationalExponent(exponent: Generic): Pair<Int, Int>? {
        val fromVariable = try {
            val v = exponent.variableValue()
            if (v is Fraction) {
                val params = v.getParameters() ?: return null
                val numerator = params[0].integerValue().toInt()
                val denominator = params[1].integerValue().toInt()
                return numerator to denominator
            }
            null
        } catch (_: Exception) {
            null
        }
        if (fromVariable != null) {
            return fromVariable
        }
        return try {
            val expression = exponent.expressionValue()
            if (expression.size() != 1) return null
            val literal = expression.literal(0)
            if (literal.degree() != 1) return null
            val coef = expression.coef(0)
            val variable = literal.getVariable(0)
            if (variable is Fraction) {
                val params = variable.getParameters() ?: return null
                val numerator = params[0].integerValue().toInt()
                val denominator = params[1].integerValue().toInt()
                val signedNumerator = numerator * coef.signum()
                signedNumerator to denominator
            } else {
                null
            }
        } catch (_: Exception) {
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

    override fun selfNumeric(): Generic {
        return (parameters!![0] as NumericWrapper).pow(parameters!![1])
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        try {
            val en = parameters!![0].integerValue()
            if (en.signum() < 0) buffer.append(GenericVariable.valueOf(en, true))
            else buffer.append(en)
        } catch (e: NotIntegerException) {
            try {
                val v = parameters!![0].variableValue()
                if (v is Fraction || v is Pow) {
                    buffer.append(GenericVariable.valueOf(parameters!![0]))
                } else buffer.append(v)
            } catch (e2: NotVariableException) {
                try {
                    val o = parameters!![0].powerValue()
                    if (o.exponent() == 1) buffer.append(o.value(true))
                    else buffer.append(GenericVariable.valueOf(parameters!![0]))
                } catch (e3: NotPowerException) {
                    buffer.append(GenericVariable.valueOf(parameters!![0]))
                }
            }
        }
        buffer.append("^")
        try {
            val en = parameters!![1].integerValue()
            buffer.append(en)
        } catch (e: NotIntegerException) {
            try {
                val v = parameters!![1].variableValue()
                if (v is Fraction) {
                    buffer.append(GenericVariable.valueOf(parameters!![1]))
                } else buffer.append(v)
            } catch (e2: NotVariableException) {
                try {
                    parameters!![1].powerValue()
                    buffer.append(parameters!![1])
                } catch (e3: NotPowerException) {
                    buffer.append(GenericVariable.valueOf(parameters!![1]))
                }
            }
        }
        return buffer.toString()
    }

    override fun toJava(): String {
        val buffer = StringBuilder()
        buffer.append(parameters!![0].toJava())
        buffer.append(".pow(")
        buffer.append(parameters!![1].toJava())
        buffer.append(")")
        return buffer.toString()
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
        val e1 = element.element("msup")
        try {
            val v = parameters!![0].variableValue()
            if (v is Fraction || v is Pow || v is Exp) {
                GenericVariable.valueOf(parameters!![0]).toMathML(e1, null)
            } else parameters!![0].toMathML(e1, null)
        } catch (e2: NotVariableException) {
            try {
                val o = parameters!![0].powerValue()
                if (o.exponent() == 1) o.value(true).toMathML(e1, null)
                else GenericVariable.valueOf(parameters!![0]).toMathML(e1, null)
            } catch (e3: NotPowerException) {
                GenericVariable.valueOf(parameters!![0]).toMathML(e1, null)
            }
        }
        parameters!![1].toMathML(e1, null)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return Pow(null, null)
    }

    override val constants: Set<Constant>
        get() {
            val result = HashSet<Constant>()
            for (parameter in parameters!!) {
                result.addAll(parameter.constants)
            }
            return result
        }

    companion object {
        private const val MAX_ARRAY_SIZE = 10000

        fun root_minus_1(d: Int): Generic? {
            return when (d) {
                1 -> JsclInteger.valueOf(-1)
                2 -> Constants.Generic.I
                3 -> Constants.Generic.J_BAR.negate()
                4 -> Sqrt(Constants.Generic.HALF).expressionValue().multiply(JsclInteger.valueOf(1).add(Constants.Generic.I))
                6 -> Constants.Generic.HALF.multiply(Sqrt(JsclInteger.valueOf(3)).expressionValue().add(Constants.Generic.I))
                else -> null
            }
        }
    }
}
