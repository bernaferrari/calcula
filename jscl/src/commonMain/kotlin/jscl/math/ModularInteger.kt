package jscl.math

import jscl.math.function.Constant
import jscl.mathml.MathML
import com.ionspin.kotlin.bignum.integer.BigInteger

open class ModularInteger(content: Long, val modulo: Int) : Generic(), Field {
    val content: Int = (content % modulo).toInt()

    fun content(): Int = content

    fun modulo(): Int = modulo

    fun add(integer: ModularInteger): ModularInteger {
        return newinstance(content.toLong() + integer.content)
    }

    override fun add(that: Generic): Generic {
        return add(that as ModularInteger)
    }

    fun subtract(integer: ModularInteger): ModularInteger {
        return newinstance(content.toLong() + (modulo - integer.content))
    }

    override fun subtract(that: Generic): Generic {
        return subtract(that as ModularInteger)
    }

    fun multiply(integer: ModularInteger): ModularInteger {
        return newinstance(content.toLong() * integer.content)
    }

    override fun multiply(that: Generic): Generic {
        return multiply(that as ModularInteger)
    }

    override fun divide(that: Generic): Generic {
        return multiply(that.inverse())
    }

    override fun inverse(): Generic {
        return newinstance(BigInteger.fromLong(content.toLong()).modInverse(BigInteger.fromLong(modulo.toLong())).intValue().toLong())
    }

    override fun gcd(generic: Generic): Generic {
        throw UnsupportedOperationException()
    }

    override fun gcd(): Generic {
        throw UnsupportedOperationException()
    }

    override fun pow(exponent: Int): Generic {
        throw UnsupportedOperationException()
    }

    override fun negate(): Generic {
        return newinstance((modulo - content).toLong())
    }

    override fun signum(): Int {
        return if (content > 0) 1 else 0
    }

    override fun degree(): Int = 0

    override fun antiDerivative(variable: Variable): Generic {
        throw UnsupportedOperationException()
    }

    override fun derivative(variable: Variable): Generic {
        throw UnsupportedOperationException()
    }

    override fun substitute(variable: Variable, generic: Generic): Generic {
        throw UnsupportedOperationException()
    }

    override fun expand(): Generic {
        throw UnsupportedOperationException()
    }

    override fun factorize(): Generic {
        throw UnsupportedOperationException()
    }

    override fun elementary(): Generic {
        throw UnsupportedOperationException()
    }

    override fun simplify(): Generic {
        throw UnsupportedOperationException()
    }

    override fun numeric(): Generic {
        throw UnsupportedOperationException()
    }

    override fun valueOf(generic: Generic): Generic {
        return if (generic is ModularInteger) {
            newinstance(generic.content.toLong())
        } else {
            newinstance((generic as JsclInteger).content().mod(BigInteger.fromLong(modulo.toLong())).intValue().toLong())
        }
    }

    override fun sumValue(): Array<Generic> {
        throw UnsupportedOperationException()
    }

    override fun productValue(): Array<Generic> {
        throw UnsupportedOperationException()
    }

    override fun powerValue(): Power {
        throw UnsupportedOperationException()
    }

    override fun expressionValue(): Expression {
        return Expression.valueOf(integerValue())
    }

    override fun integerValue(): JsclInteger {
        return JsclInteger.valueOf(content.toLong())
    }

    override fun doubleValue(): Double {
        return content.toDouble()
    }

    override val isInteger: Boolean
        get() = true

    override fun variableValue(): Variable {
        throw UnsupportedOperationException()
    }

    override fun variables(): Array<Variable> {
        throw UnsupportedOperationException()
    }

    override fun isPolynomial(variable: Variable): Boolean {
        throw UnsupportedOperationException()
    }

    override fun isConstant(variable: Variable): Boolean {
        throw UnsupportedOperationException()
    }

    fun compareTo(integer: ModularInteger): Int {
        return when {
            content < integer.content -> -1
            content > integer.content -> 1
            else -> 0
        }
    }

    override fun compareTo(generic: Generic): Int {
        return when (generic) {
            is ModularInteger -> compareTo(generic)
            is JsclInteger -> compareTo(valueOf(generic))
            else -> throw UnsupportedOperationException()
        }
    }

    override fun toString(): String = "$content"

    override fun toJava(): String {
        throw UnsupportedOperationException()
    }

    override fun toMathML(element: MathML, data: Any?) {
        throw UnsupportedOperationException()
    }

    override val constants: Set<Constant>
        get() = emptySet()

    protected open fun newinstance(content: Long): ModularInteger {
        return ModularInteger(content, modulo)
    }

    companion object {
        val booleanFactory = ModularInteger(0, 2)

        fun factory(modulo: Int): ModularInteger {
            return ModularInteger(0, modulo)
        }
    }
}
