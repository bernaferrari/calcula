package jscl.math

import jscl.math.function.Constant
import jscl.math.function.IConstant
import jscl.mathml.MathML
import jscl.text.ParserUtils
import jscl.common.math.MathRegistry
import com.ionspin.kotlin.bignum.integer.BigInteger

abstract class Generic : Arithmetic<Generic>, Comparable<Any> {

    open fun getUndefinedConstants(constantsRegistry: MathRegistry<IConstant>): Set<Constant> {
        val result = HashSet<Constant>()

        for (expressionConstant in constants) {
            val registryConstant = constantsRegistry.get(expressionConstant.name) ?: continue
            if (registryConstant.isSystem()) continue
            if (registryConstant.isDefined()) continue
            result.add(expressionConstant)
        }

        return result
    }

    open fun toBigInteger(): BigInteger? = null

    override fun subtract(that: Generic): Generic = add(that.negate())

    @Throws(ArithmeticException::class)
    open fun multiple(generic: Generic): Boolean = remainder(generic).signum() == 0

    open fun divideAndRemainder(generic: Generic): Array<Generic> {
        return try {
            arrayOf(divide(generic), JsclInteger.valueOf(0))
        } catch (e: NotDivisibleException) {
            arrayOf(JsclInteger.valueOf(0), this)
        }
    }

    @Throws(ArithmeticException::class)
    open fun remainder(generic: Generic): Generic = divideAndRemainder(generic)[1]

    open fun inverse(): Generic = JsclInteger.valueOf(1).divide(this)

    abstract fun gcd(generic: Generic): Generic

    open fun scm(generic: Generic): Generic = divide(gcd(generic)).multiply(generic)

    protected abstract fun gcd(): Generic

    open fun gcdAndNormalize(): Array<Generic> {
        var gcd = gcd()

        if (gcd.signum() == 0) {
            return arrayOf(gcd, this)
        }

        if (gcd.signum() != signum()) {
            gcd = gcd.negate()
        }

        return arrayOf(gcd, divide(gcd))
    }

    open fun normalize(): Generic = gcdAndNormalize()[1]

    open fun pow(exponent: Int): Generic {
        require(exponent >= 0)

        var result: Generic = JsclInteger.valueOf(1)

        for (i in 0 until exponent) {
            ParserUtils.checkInterruption()
            result = result.multiply(this)
        }

        return result
    }

    open fun abs(): Generic = if (signum() < 0) negate() else this

    abstract fun negate(): Generic

    abstract fun signum(): Int

    abstract fun degree(): Int

    @Throws(NotIntegrableException::class)
    abstract fun antiDerivative(variable: Variable): Generic

    abstract fun derivative(variable: Variable): Generic

    abstract fun substitute(variable: Variable, generic: Generic): Generic

    abstract fun expand(): Generic

    abstract fun factorize(): Generic

    abstract fun elementary(): Generic

    abstract fun simplify(): Generic

    abstract fun numeric(): Generic

    abstract fun valueOf(generic: Generic): Generic

    abstract fun sumValue(): Array<Generic>

    @Throws(NotProductException::class)
    abstract fun productValue(): Array<Generic>

    @Throws(NotPowerException::class)
    abstract fun powerValue(): Power

    @Throws(NotExpressionException::class)
    abstract fun expressionValue(): Expression

    abstract val isInteger: Boolean

    @Throws(NotIntegerException::class)
    abstract fun integerValue(): JsclInteger

    @Throws(NotDoubleException::class)
    abstract fun doubleValue(): Double

    @Throws(NotVariableException::class)
    abstract fun variableValue(): Variable

    abstract fun variables(): Array<Variable>

    abstract fun isPolynomial(variable: Variable): Boolean

    abstract fun isConstant(variable: Variable): Boolean

    open fun isIdentity(variable: Variable): Boolean {
        return try {
            variableValue().isIdentity(variable)
        } catch (e: NotVariableException) {
            false
        }
    }

    abstract fun compareTo(generic: Generic): Int

    override fun compareTo(other: Any): Int = compareTo(other as Generic)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is Generic) {
            return compareTo(other) == 0
        }

        return false
    }

    abstract fun toJava(): String

    open fun toMathML(): String {
        val document = MathML("math", "-//W3C//DTD MathML 2.0//EN", "http://www.w3.org/TR/MathML2/dtd/mathml2.dtd")
        val e = document.element("math")
        toMathML(e, null)
        return e.toString()
    }

    abstract fun toMathML(element: MathML, data: Any?)

    abstract val constants: Set<Constant>
}

// Helper to create arrays that may contain nulls for registration purposes
// Returns an array with the same size as input; null elements become placeholders
@Suppress("UNCHECKED_CAST")
internal fun genericArrayOf(vararg elements: Generic?): Array<Generic>? {
    // If all null, return null (for initial template registration)
    if (elements.all { it == null }) return null
    // Otherwise, create array preserving non-null elements
    // This handles the case where some parameters are provided
    return elements.filterNotNull().toTypedArray()
}
