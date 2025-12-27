package jscl.math

import jscl.math.function.Constant
import jscl.math.function.Fraction
import jscl.math.function.Inverse
import jscl.mathml.MathML
import com.ionspin.kotlin.bignum.integer.BigInteger

class Rational(
    @JvmField val numerator: BigInteger,
    @JvmField val denominator: BigInteger
) : Generic(), Field {

    fun numerator(): BigInteger = numerator

    fun denominator(): BigInteger = denominator

    fun add(rational: Rational): Rational {
        val gcd = denominator.gcd(rational.denominator)
        val c = denominator.divide(gcd)
        val c2 = rational.denominator.divide(gcd)
        return Rational(
            numerator.multiply(c2).add(rational.numerator.multiply(c)),
            denominator.multiply(c2)
        ).reduce()
    }

    internal fun reduce(): Rational {
        var gcd = numerator.gcd(denominator)
        if (gcd.signum() != denominator.signum()) gcd = gcd.negate()
        return if (gcd.signum() == 0) this else Rational(numerator.divide(gcd), denominator.divide(gcd))
    }

    override fun add(that: Generic): Generic {
        return when (that) {
            is Rational -> add(that)
            is JsclInteger -> add(valueOf(that) as Rational)
            else -> that.valueOf(this).add(that)
        }
    }

    fun multiply(rational: Rational): Rational {
        val gcd = numerator.gcd(rational.denominator)
        val gcd2 = denominator.gcd(rational.numerator)
        return Rational(
            numerator.divide(gcd).multiply(rational.numerator.divide(gcd2)),
            denominator.divide(gcd2).multiply(rational.denominator.divide(gcd))
        )
    }

    override fun multiply(that: Generic): Generic {
        return when (that) {
            is Rational -> multiply(that)
            is JsclInteger -> multiply(valueOf(that) as Rational)
            else -> that.multiply(this)
        }
    }

    @Throws(NotDivisibleException::class)
    override fun divide(that: Generic): Generic {
        return when (that) {
            is Rational -> multiply(that.inverse() as Rational)
            is JsclInteger -> divide(valueOf(that))
            else -> that.valueOf(this).divide(that)
        }
    }

    override fun inverse(): Generic {
        return if (signum() < 0) {
            Rational(denominator.negate(), numerator.negate())
        } else {
            Rational(denominator, numerator)
        }
    }

    fun gcd(rational: Rational): Rational =
        Rational(numerator.gcd(rational.numerator), scm(denominator, rational.denominator))

    override fun gcd(generic: Generic): Generic {
        return when (generic) {
            is Rational -> gcd(generic)
            is JsclInteger -> gcd(valueOf(generic) as Rational)
            else -> generic.valueOf(this).gcd(generic)
        }
    }

    override fun gcd(): Generic = throw ArithmeticException("Rational gcd not supported")

    override fun pow(exponent: Int): Generic {
        if (exponent == 0) return Rational(BigInteger.ONE, BigInteger.ONE)
        var result = this
        for (i in 1 until exponent) {
            result = result.multiply(this)
        }
        return result
    }

    override fun negate(): Generic = Rational(numerator.negate(), denominator)

    override fun signum(): Int = numerator.signum()

    override fun degree(): Int = 0

    @Throws(NotIntegrableException::class)
    override fun antiDerivative(variable: Variable): Generic = multiply(variable.expressionValue())

    override fun derivative(variable: Variable): Generic = JsclInteger.valueOf(0)

    override fun substitute(variable: Variable, generic: Generic): Generic = this

    override fun expand(): Generic = this

    override fun factorize(): Generic = expressionValue().factorize()

    override fun elementary(): Generic = this

    override fun simplify(): Generic = reduce()

    override fun numeric(): Generic = NumericWrapper(this)

    override fun valueOf(generic: Generic): Generic {
        return when (generic) {
            is Rational -> Rational(generic.numerator, generic.denominator)
            is Expression -> {
                val sign = generic.signum() < 0
                val g = (if (sign) generic.negate() else generic).variableValue() as Fraction
                val params = g.getParameters()!!
                val num = (if (sign) params[0].negate() else params[0]) as JsclInteger
                val denom = params[1] as JsclInteger
                Rational(num.content(), denom.content())
            }
            else -> {
                val en = generic as JsclInteger
                Rational(en.content(), BigInteger.ONE)
            }
        }
    }

    override fun sumValue(): Array<Generic> {
        return try {
            if (integerValue().signum() == 0) emptyArray() else arrayOf(this)
        } catch (e: NotIntegerException) {
            arrayOf(this)
        }
    }

    @Throws(NotProductException::class)
    override fun productValue(): Array<Generic> {
        return try {
            if (integerValue().compareTo(JsclInteger.valueOf(1)) == 0) {
                emptyArray()
            } else {
                arrayOf(this)
            }
        } catch (e: NotIntegerException) {
            arrayOf(this)
        }
    }

    @Throws(NotPowerException::class)
    override fun powerValue(): Power = Power(this, 1)

    @Throws(NotExpressionException::class)
    override fun expressionValue(): Expression = Expression.valueOf(this)

    @Throws(NotIntegerException::class)
    override fun integerValue(): JsclInteger {
        return if (denominator.compareTo(BigInteger.ONE) == 0) {
            JsclInteger(numerator)
        } else {
            throw NotIntegerException.get()
        }
    }

    @Throws(NotDoubleException::class)
    override fun doubleValue(): Double = numerator.doubleValue(false) / denominator.doubleValue(false)

    override val isInteger: Boolean
        get() = try {
            integerValue()
            true
        } catch (e: NotIntegerException) {
            false
        }

    @Throws(NotVariableException::class)
    override fun variableValue(): Variable {
        try {
            integerValue()
            throw NotVariableException()
        } catch (e: NotIntegerException) {
            return if (numerator.compareTo(BigInteger.ONE) == 0) {
                Inverse(JsclInteger(denominator))
            } else {
                Fraction(JsclInteger(numerator), JsclInteger(denominator))
            }
        }
    }

    override fun variables(): Array<Variable> = emptyArray()

    override fun isPolynomial(variable: Variable): Boolean = true

    override fun isConstant(variable: Variable): Boolean = true

    fun compareTo(rational: Rational): Int {
        val c = denominator.compareTo(rational.denominator)
        return when {
            c < 0 -> -1
            c > 0 -> 1
            else -> numerator.compareTo(rational.numerator)
        }
    }

    override fun compareTo(generic: Generic): Int {
        return when (generic) {
            is Rational -> compareTo(generic)
            is JsclInteger -> compareTo(valueOf(generic) as Rational)
            else -> generic.valueOf(this).compareTo(generic)
        }
    }

    override fun toString(): String {
        return try {
            integerValue().toString()
        } catch (e: NotIntegerException) {
            "$numerator/$denominator"
        }
    }

    override fun toJava(): String = "JsclDouble.valueOf($numerator/$denominator)"

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) {
            bodyToMathML(element)
        } else {
            val e1 = element.element("msup")
            bodyToMathML(e1)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
    }

    override val constants: Set<Constant>
        get() = emptySet()

    internal fun bodyToMathML(element: MathML) {
        try {
            val e1 = element.element("mn")
            e1.appendChild(element.text(integerValue().toString()))
            element.appendChild(e1)
        } catch (e: NotIntegerException) {
            val e1 = element.element("mfrac")
            val e2 = element.element("mn")
            e2.appendChild(element.text(numerator.toString()))
            e1.appendChild(e2)
            val e3 = element.element("mn")
            e3.appendChild(element.text(denominator.toString()))
            e1.appendChild(e3)
            element.appendChild(e1)
        }
    }

    override fun toBigInteger(): BigInteger? {
        return try {
            integerValue().toBigInteger()
        } catch (e: NotIntegerException) {
            null
        }
    }

    companion object {
        @JvmField
        val factory = Rational(BigInteger.ZERO, BigInteger.ONE)

        @JvmStatic
        internal fun scm(b1: BigInteger, b2: BigInteger): BigInteger =
            b1.multiply(b2).divide(b1.gcd(b2))
    }
}
