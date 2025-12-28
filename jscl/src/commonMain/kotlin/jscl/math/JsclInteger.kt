package jscl.math

import jscl.JsclMathEngine
import jscl.math.function.Constant
import jscl.mathml.MathML
import com.ionspin.kotlin.bignum.integer.BigInteger

class JsclInteger(private val content: BigInteger) : Generic() {

    fun content(): BigInteger = content

    fun add(integer: JsclInteger): JsclInteger = JsclInteger(content.add(integer.content))

    override fun add(that: Generic): Generic {
        if (isZero()) {
            return that
        }
        return if (that is JsclInteger) {
            add(that)
        } else {
            that.valueOf(this).add(that)
        }
    }

    private fun isZero(): Boolean = content == ZERO.content

    fun subtract(that: JsclInteger): JsclInteger {
        if (isZero()) {
            return that.negate()
        }
        return JsclInteger(content.subtract(that.content))
    }

    override fun subtract(that: Generic): Generic {
        return if (that is JsclInteger) {
            subtract(that)
        } else {
            that.valueOf(this).subtract(that)
        }
    }

    fun multiply(integer: JsclInteger): JsclInteger = JsclInteger(content.multiply(integer.content))

    override fun multiply(that: Generic): Generic {
        if (isOne()) {
            return that
        }
        return if (that is JsclInteger) {
            multiply(that)
        } else {
            that.multiply(this)
        }
    }

    private fun isOne(): Boolean = content == ONE.content

    @Throws(NotDivisibleException::class)
    fun divide(that: JsclInteger): JsclInteger {
        if (isZero()) {
            return ZERO
        }
        val e = divideAndRemainder(that)
        return if (e[1].signum() == 0) {
            e[0]
        } else {
            throw NotDivisibleException()
        }
    }

    @Throws(NotDivisibleException::class)
    override fun divide(that: Generic): Generic {
        return if (that is JsclInteger) {
            divide(that)
        } else {
            that.valueOf(this).divide(that)
        }
    }

    private fun divideAndRemainder(that: JsclInteger): Array<JsclInteger> {
        return try {
            val (quotient, remainder) = content.divideAndRemainder(that.content)
            arrayOf(JsclInteger(quotient), JsclInteger(remainder))
        } catch (e: ArithmeticException) {
            throw NotDivisibleException()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun divideAndRemainder(generic: Generic): Array<Generic> {
        return if (generic is JsclInteger) {
            divideAndRemainder(generic) as Array<Generic>
        } else {
            generic.valueOf(this).divideAndRemainder(generic)
        }
    }

    @Throws(ArithmeticException::class)
    fun remainder(integer: JsclInteger): JsclInteger = JsclInteger(content.remainder(integer.content))

    @Throws(ArithmeticException::class)
    override fun remainder(generic: Generic): Generic {
        return if (generic is JsclInteger) {
            remainder(generic)
        } else {
            generic.valueOf(this).remainder(generic)
        }
    }

    fun gcd(integer: JsclInteger): JsclInteger = JsclInteger(content.gcd(integer.content))

    override fun gcd(generic: Generic): Generic {
        return if (generic is JsclInteger) {
            gcd(generic)
        } else {
            generic.valueOf(this).gcd(generic)
        }
    }

    override fun gcd(): Generic = JsclInteger(BigInteger.fromLong(signum().toLong()))

    override fun pow(exponent: Int): Generic {
        if (exponent == 0) {
            return ONE
        }
        return JsclInteger(content.pow(exponent))
    }

    override fun negate(): JsclInteger = JsclInteger(content.negate())

    override fun signum(): Int = content.signum()

    override fun degree(): Int = 0

    fun mod(that: JsclInteger): JsclInteger = JsclInteger(content.mod(that.content))

    fun modPow(exponent: JsclInteger, modulus: JsclInteger): JsclInteger =
        JsclInteger(bigIntModPow(content, exponent.content, modulus.content))

    fun modInverse(integer: JsclInteger): JsclInteger = JsclInteger(content.modInverse(integer.content))

    fun phi(): JsclInteger {
        if (signum() == 0) return this
        val a = factorize()
        val p = a.productValue()
        var s: Generic = valueOf(1)
        for (i in p.indices) {
            val o = p[i].powerValue()
            val q = o.value(true)
            val c = o.exponent()
            s = s.multiply(q.subtract(valueOf(1)).multiply(q.pow(c - 1)))
        }
        return s.integerValue()
    }

    fun primitiveRoots(): Array<JsclInteger> {
        val phi = phi()
        val a = phi.factorize()
        val p = a.productValue()
        val d = Array(p.size) { i ->
            phi.divide(p[i].powerValue().value(true).integerValue())
        }
        var k = 0
        val n = this
        var m = valueOf(1)
        val r = Array<JsclInteger?>(phi.phi().toInt()) { null }
        while (m.compareTo(n) < 0) {
            var b = m.gcd(n).compareTo(valueOf(1)) == 0
            for (i in d.indices) {
                b = b && m.modPow(d[i], n).compareTo(valueOf(1)) > 0
            }
            if (b) r[k++] = m
            m = m.add(valueOf(1))
        }
        @Suppress("UNCHECKED_CAST")
        return if (k > 0) r.copyOf(k) as Array<JsclInteger> else emptyArray()
    }

    fun sqrt(): JsclInteger = nthrt(2)

    fun nthrt(n: Int): JsclInteger {
        if (signum() == 0) {
            return valueOf(0)
        } else if (signum() < 0) {
            if (n % 2 == 0) {
                throw ArithmeticException("Could not calculate root of negative argument: $this of odd order: $n")
            } else {
                return negate().nthrt(n).negate()
            }
        } else {
            var x0: Generic
            var x: Generic = this
            do {
                x0 = x
                x = divideAndRemainder(x.pow(n - 1))[0].add(x.multiply(JsclInteger.valueOf((n - 1).toLong()))).divideAndRemainder(JsclInteger.valueOf(n.toLong()))[0]
            } while (x.compareTo(x0) < 0)
            return x0.integerValue()
        }
    }

    @Throws(NotIntegrableException::class)
    override fun antiDerivative(variable: Variable): Generic = multiply(variable.expressionValue())

    override fun derivative(variable: Variable): Generic = valueOf(0)

    override fun substitute(variable: Variable, generic: Generic): Generic = this

    override fun expand(): Generic = this

    override fun factorize(): Generic = Factorization.compute(this)

    override fun elementary(): Generic = this

    override fun simplify(): Generic = this

    override fun numeric(): Generic = NumericWrapper(this)

    override fun valueOf(generic: Generic): Generic = JsclInteger((generic as JsclInteger).content)

    override fun sumValue(): Array<Generic> =
        if (content.signum() == 0) emptyArray() else arrayOf(this)

    @Throws(NotProductException::class)
    override fun productValue(): Array<Generic> =
        if (content.compareTo(BigInteger.ONE) == 0) emptyArray() else arrayOf(this)

    @Throws(NotPowerException::class)
    override fun powerValue(): Power {
        if (content.signum() < 0) throw NotPowerException()
        return Power(this, 1)
    }

    @Throws(NotExpressionException::class)
    override fun expressionValue(): Expression = Expression.valueOf(this)

    @Throws(NotIntegerException::class)
    override fun integerValue(): JsclInteger = this

    override val isInteger: Boolean = true

    @Throws(NotVariableException::class)
    override fun variableValue(): Variable {
        throw NotVariableException()
    }

    override fun variables(): Array<Variable> = emptyArray()

    override fun isPolynomial(variable: Variable): Boolean = true

    override fun isConstant(variable: Variable): Boolean = true

    fun intValue(): Int = content.intValue()

    fun toInt(): Int = content.intValue()

    fun compareTo(integer: JsclInteger): Int = content.compareTo(integer.content)

    override fun compareTo(generic: Generic): Int {
        return if (generic is JsclInteger) {
            compareTo(generic)
        } else {
            generic.valueOf(this).compareTo(generic)
        }
    }

    override fun toString(): String = JsclMathEngine.getInstance().format(content)

    override fun toJava(): String = "JsclDouble.valueOf($content)"

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

    override val constants: Set<Constant> = emptySet()

    internal fun bodyToMathML(element: MathML) {
        val e1 = element.element("mn")
        e1.appendChild(element.text(content.toString()))
        element.appendChild(e1)
    }

    override fun toBigInteger(): BigInteger = content

    @Throws(NotDoubleException::class)
    override fun doubleValue(): Double = content.doubleValue(false)

    companion object {
        val factory = JsclInteger(BigInteger.fromLong(0))

        val ZERO = JsclInteger(BigInteger.fromLong(0))

        val ONE = JsclInteger(BigInteger.fromLong(1))

        fun valueOf(value: Long): JsclInteger {
            return when (value.toInt()) {
                0 -> ZERO
                1 -> ONE
                else -> JsclInteger(BigInteger.fromLong(value))
            }
        }

        fun valueOf(str: String): JsclInteger = JsclInteger(BigInteger.parseString(str))

        /**
         * Modular exponentiation using square-and-multiply algorithm.
         * Computes (base^exponent) mod modulus efficiently.
         */
        private fun bigIntModPow(base: BigInteger, exponent: BigInteger, modulus: BigInteger): BigInteger {
            if (modulus == BigInteger.ONE) return BigInteger.ZERO
            var result = BigInteger.ONE
            var b = base.mod(modulus)
            var exp = exponent
            val two = BigInteger.fromInt(2)
            while (exp > BigInteger.ZERO) {
                if (exp.mod(two) == BigInteger.ONE) {
                    result = (result * b).mod(modulus)
                }
                exp = exp.shr(1)
                b = (b * b).mod(modulus)
            }
            return result
        }
    }
}
