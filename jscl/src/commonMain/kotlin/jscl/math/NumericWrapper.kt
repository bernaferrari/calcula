package jscl.math

import jscl.math.function.Constant
import jscl.math.function.Constants
import jscl.math.function.ConstantsRegistry
import jscl.math.numeric.*
import jscl.mathml.MathML
import com.ionspin.kotlin.bignum.integer.BigInteger

class NumericWrapper : Generic, INumeric<NumericWrapper> {

    private val content: Numeric

    constructor(integer: JsclInteger) {
        content = Real.valueOf(integer.content().doubleValue(false))
    }

    constructor(rational: Rational) {
        content = Real.valueOf(rational.numerator().doubleValue(false) / rational.denominator().doubleValue(false))
    }

    constructor(vector: JsclVector) {
        val elements = Array<Numeric>(vector.rows) { i ->
            (vector.elements[i].numeric() as NumericWrapper).content()
        }
        content = Vector(elements)
    }

    constructor(matrix: Matrix) {
        val elements = Array(matrix.rows) { i ->
            Array<Numeric>(matrix.cols) { j ->
                (matrix.elements[i][j]!!.numeric() as NumericWrapper).content()
            }
        }
        content = jscl.math.numeric.Matrix(elements)
    }

    constructor(constant: Constant) {
        val constantFromRegistry = ConstantsRegistry.getInstance().get(constant.name)

        if (constantFromRegistry != null) {
            content = if (constantFromRegistry.name == Constants.I.name) {
                Complex.I
            } else {
                if (constantFromRegistry.getValue() != null) {
                    val value = constantFromRegistry.getDoubleValue()
                        ?: throw ArithmeticException("Constant ${constant.name} has invalid definition: ${constantFromRegistry.getValue()}")
                    Real.valueOf(value)
                } else {
                    throw ArithmeticException("Could not create numeric wrapper: constant in registry doesn't have specified value: ${constant.name}")
                }
            }
        } else {
            throw ArithmeticException("Could not create numeric wrapper: constant is not registered in constants registry: ${constant.name}")
        }
    }

    constructor(numeric: Numeric) {
        content = numeric
    }

    fun content(): Numeric = content

    fun add(wrapper: NumericWrapper): NumericWrapper {
        return NumericWrapper(content.add(wrapper.content))
    }

    override fun add(that: Generic): Generic {
        return when (that) {
            is Expression -> that.add(this)
            is NumericWrapper -> add(that)
            else -> add(valueOf(that) as NumericWrapper)
        }
    }

    fun subtract(wrapper: NumericWrapper): NumericWrapper {
        return NumericWrapper(content.subtract(wrapper.content))
    }

    override fun subtract(that: Generic): Generic {
        return when (that) {
            is Expression -> that.negate().add(this)
            is NumericWrapper -> subtract(that)
            else -> subtract(valueOf(that) as NumericWrapper)
        }
    }

    fun multiply(wrapper: NumericWrapper): NumericWrapper {
        return NumericWrapper(content.multiply(wrapper.content))
    }

    override fun multiply(that: Generic): Generic {
        return when (that) {
            is Expression -> that.multiply(this)
            is NumericWrapper -> multiply(that)
            else -> multiply(valueOf(that) as NumericWrapper)
        }
    }

    fun divide(wrapper: NumericWrapper): NumericWrapper {
        return NumericWrapper(content.divide(wrapper.content))
    }

    override fun divide(that: Generic): Generic {
        return when (that) {
            is Expression -> {
                // Convert this to Expression and divide
                val thisExpr = Expression(1)
                thisExpr.init(this)
                thisExpr.divide(that)
            }
            is NumericWrapper -> divide(that)
            else -> divide(valueOf(that) as NumericWrapper)
        }
    }

    override fun gcd(generic: Generic): Generic = throw ArithmeticException("NumericWrapper gcd not supported")

    override fun gcd(): Generic = throw ArithmeticException("NumericWrapper gcd not supported")

    override fun abs(): NumericWrapper = NumericWrapper(content.abs())

    override fun negate(): NumericWrapper = NumericWrapper(content.negate())

    override fun signum(): Int = content.signum()

    override fun degree(): Int = 0

    override fun antiDerivative(variable: Variable): Generic = throw NotIntegrableException()

    override fun derivative(variable: Variable): Generic = JsclInteger.valueOf(0)

    override fun substitute(variable: Variable, generic: Generic): Generic = this

    override fun expand(): Generic = this

    override fun factorize(): Generic = this

    override fun elementary(): Generic = this

    override fun simplify(): Generic = this

    override fun numeric(): Generic = this

    fun valueOf(wrapper: NumericWrapper): NumericWrapper {
        return NumericWrapper(content.valueOf(wrapper.content))
    }

    override fun valueOf(generic: Generic): Generic {
        return when (generic) {
            is NumericWrapper -> valueOf(generic)
            is JsclInteger -> NumericWrapper(generic)
            else -> {
                // Try to convert to numeric first
                val numeric = generic.numeric()
                if (numeric is NumericWrapper) {
                    numeric
                } else {
                    throw ArithmeticException("Could not create numeric wrapper for class: ${generic::class.simpleName}")
                }
            }
        }
    }

    override fun sumValue(): Array<Generic> = arrayOf(this)

    override fun productValue(): Array<Generic> = arrayOf(this)

    override fun powerValue(): Power = Power(this, 1)

    override fun expressionValue(): Expression {
        throw NotExpressionException()
    }

    override fun integerValue(): JsclInteger {
        if (content is Real) {
            val doubleValue = content.doubleValue()
            if (kotlin.math.floor(doubleValue) == doubleValue) {
                return JsclInteger.valueOf(doubleValue.toInt().toLong())
            } else {
                throw NotIntegerException.get()
            }
        } else {
            throw NotIntegerException.get()
        }
    }

    override fun doubleValue(): Double = content.doubleValue()

    override val isInteger: Boolean
        get() {
            if (content is Real) {
                val value = content.doubleValue()
                return kotlin.math.floor(value) == value
            }
            return false
        }

    override fun variableValue(): Variable {
        throw NotVariableException()
    }

    override fun variables(): Array<Variable> = emptyArray()

    override fun isPolynomial(variable: Variable): Boolean = true

    override fun isConstant(variable: Variable): Boolean = true

    override fun sgn(): NumericWrapper = NumericWrapper(content.sgn())

    override fun ln(): NumericWrapper = NumericWrapper(content.ln())

    override fun lg(): NumericWrapper = NumericWrapper(content.lg())

    override fun exp(): NumericWrapper = NumericWrapper(content.exp())

    override fun inverse(): NumericWrapper = NumericWrapper(content.inverse())

    override fun pow(exponent: Int): NumericWrapper = NumericWrapper(content.pow(exponent))

    fun pow(generic: Generic): Generic = NumericWrapper(content.pow((generic as NumericWrapper).content))

    override fun sqrt(): NumericWrapper = NumericWrapper(content.sqrt())

    override fun nThRoot(n: Int): NumericWrapper = NumericWrapper(content.nThRoot(n))

    fun conjugate(): Generic = NumericWrapper(content.conjugate())

    override fun acos(): NumericWrapper = NumericWrapper(content.acos())

    override fun asin(): NumericWrapper = NumericWrapper(content.asin())

    override fun atan(): NumericWrapper = NumericWrapper(content.atan())

    override fun acot(): NumericWrapper = NumericWrapper(content.acot())

    override fun cos(): NumericWrapper = NumericWrapper(content.cos())

    override fun sin(): NumericWrapper = NumericWrapper(content.sin())

    override fun tan(): NumericWrapper = NumericWrapper(content.tan())

    override fun cot(): NumericWrapper = NumericWrapper(content.cot())

    override fun acosh(): NumericWrapper = NumericWrapper(content.acosh())

    override fun asinh(): NumericWrapper = NumericWrapper(content.asinh())

    override fun atanh(): NumericWrapper = NumericWrapper(content.atanh())

    override fun acoth(): NumericWrapper = NumericWrapper(content.acoth())

    override fun cosh(): NumericWrapper = NumericWrapper(content.cosh())

    override fun sinh(): NumericWrapper = NumericWrapper(content.sinh())

    override fun tanh(): NumericWrapper = NumericWrapper(content.tanh())

    override fun coth(): NumericWrapper = NumericWrapper(content.coth())

    fun compareTo(wrapper: NumericWrapper): Int = content.compareTo(wrapper.content)

    override fun compareTo(generic: Generic): Int {
        return if (generic is NumericWrapper) {
            compareTo(generic)
        } else {
            compareTo(valueOf(generic) as NumericWrapper)
        }
    }

    override fun toString(): String = content.toString()

    override fun toJava(): String = "JsclDouble.valueOf(${(content as Real).doubleValue()})"

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

    fun bodyToMathML(element: MathML) {
        val e1 = element.element("mn")
        e1.appendChild(element.text((content as Real).doubleValue().toString()))
        element.appendChild(e1)
    }

    override fun toBigInteger(): BigInteger = content.toBigInteger()!!

    companion object {
        fun root(subscript: Int, parameter: Array<Generic>): Generic {
            val param = Array<Numeric>(parameter.size) { i ->
                (parameter[i] as NumericWrapper).content
            }
            return NumericWrapper(Numeric.root(subscript, param))
        }

        fun valueOf(value: Long): Generic {
            return NumericWrapper(JsclInteger(BigInteger.fromLong(value)))
        }

        fun valueOf(value: Double): Generic {
            return NumericWrapper(Real.valueOf(value))
        }
    }
}
