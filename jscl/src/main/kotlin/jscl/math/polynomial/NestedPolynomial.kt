package jscl.math.polynomial

import jscl.math.*
import jscl.math.function.Constant
import jscl.mathml.MathML
import java.util.*

internal class NestedPolynomial : UnivariatePolynomial {
    constructor(variable: Array<Variable>) : this(variable[0], PolynomialWrapper.factory(variable))

    constructor(variable: Variable, coefFactory: Generic?) : super(variable, coefFactory)

    override fun newinstance(): UnivariatePolynomial {
        return NestedPolynomial(variable, coefFactory)
    }

    companion object {
        @JvmStatic
        fun factory(variable: Array<Variable>): NestedPolynomial {
            return NestedPolynomial(variable)
        }
    }
}

internal class PolynomialWrapper(internal val content: Polynomial) : Generic() {

    fun content(): Polynomial {
        return content
    }

    fun add(wrapper: PolynomialWrapper): PolynomialWrapper {
        return PolynomialWrapper(content.add(wrapper.content))
    }

    override fun add(that: Generic): Generic {
        return if (that is PolynomialWrapper) {
            add(that)
        } else {
            add(valueOf(that) as PolynomialWrapper)
        }
    }

    fun subtract(wrapper: PolynomialWrapper): PolynomialWrapper {
        return PolynomialWrapper(content.subtract(wrapper.content))
    }

    override fun subtract(that: Generic): Generic {
        return if (that is PolynomialWrapper) {
            subtract(that)
        } else {
            subtract(valueOf(that) as PolynomialWrapper)
        }
    }

    fun multiply(wrapper: PolynomialWrapper): PolynomialWrapper {
        return PolynomialWrapper(content.multiply(wrapper.content))
    }

    override fun multiply(that: Generic): Generic {
        return if (that is PolynomialWrapper) {
            multiply(that)
        } else {
            multiply(valueOf(that) as PolynomialWrapper)
        }
    }

    fun divide(wrapper: PolynomialWrapper): PolynomialWrapper {
        return PolynomialWrapper(content.divide(wrapper.content))
    }

    override fun divide(that: Generic): Generic {
        return if (that is PolynomialWrapper) {
            divide(that)
        } else {
            divide(valueOf(that) as PolynomialWrapper)
        }
    }

    fun gcd(wrapper: PolynomialWrapper): PolynomialWrapper {
        return PolynomialWrapper(content.gcd(wrapper.content))
    }

    override fun gcd(generic: Generic): Generic {
        return if (generic is PolynomialWrapper) {
            gcd(generic)
        } else {
            gcd(valueOf(generic) as PolynomialWrapper)
        }
    }

    override fun gcd(): Generic {
        return content.gcd()
    }

    override fun negate(): Generic {
        return PolynomialWrapper(content.negate())
    }

    override fun signum(): Int {
        return content.signum()
    }

    override fun degree(): Int {
        return content.degree()
    }

    override fun antiDerivative(variable: Variable): Generic {
        return null!!
    }

    override fun derivative(variable: Variable): Generic {
        return null!!
    }

    override fun substitute(variable: Variable, generic: Generic): Generic {
        return null!!
    }

    override fun expand(): Generic {
        return null!!
    }

    override fun factorize(): Generic {
        return null!!
    }

    override fun elementary(): Generic {
        return null!!
    }

    override fun simplify(): Generic {
        return null!!
    }

    override fun numeric(): Generic {
        return null!!
    }

    override fun valueOf(generic: Generic): Generic {
        return if (generic is PolynomialWrapper) {
            PolynomialWrapper(content.valueOf(generic.content))
        } else {
            PolynomialWrapper(content.valueOf(generic))
        }
    }

    override fun sumValue(): Array<Generic> {
        throw NotSumException.get()
    }

    override fun productValue(): Array<Generic> {
        throw NotProductException.get()
    }

    override fun powerValue(): Power {
        throw NotPowerException.get()
    }

    override fun expressionValue(): Expression {
        return content.genericValue().expressionValue()
    }

    override fun integerValue(): JsclInteger {
        throw NotIntegerException.get()
    }

    override fun doubleValue(): Double {
        throw NotDoubleException.get()
    }

    override val isInteger: Boolean
        get() = false

    override fun variableValue(): Variable {
        throw NotVariableException()
    }

    override fun variables(): Array<Variable> {
        return emptyArray()
    }

    override fun isPolynomial(variable: Variable): Boolean {
        return false
    }

    override fun isConstant(variable: Variable): Boolean {
        return false
    }

    fun compareTo(wrapper: PolynomialWrapper): Int {
        return content.compareTo(wrapper.content)
    }

    override fun compareTo(other: Generic): Int {
        return if (other is PolynomialWrapper) {
            compareTo(other)
        } else {
            compareTo(valueOf(other) as PolynomialWrapper)
        }
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        if (signum() < 0) buffer.append("-").append(negate())
        else buffer.append("(").append(content).append(")")
        return buffer.toString()
    }

    override fun toJava(): String {
        return ""
    }

    override fun toMathML(element: MathML, data: Any?) {
    }

    override val constants: Set<Constant>
        get() = content.genericValue().constants

    companion object {
        @JvmStatic
        fun factory(variable: Array<Variable>): Generic? {
            return if (variable.size > 1) {
                val var2 = Array(variable.size - 1) { i -> variable[i + 1] }
                PolynomialWrapper(NestedPolynomial.factory(var2))
            } else null
        }
    }
}
