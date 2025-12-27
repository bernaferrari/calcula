@file:Suppress("UNCHECKED_CAST")

package jscl.math

import jscl.math.function.Constant
import jscl.math.function.Fraction
import jscl.math.function.Inverse
import jscl.math.numeric.Real
import jscl.math.polynomial.Polynomial
import jscl.math.polynomial.UnivariatePolynomial
import jscl.mathml.MathML
import jscl.text.ExpressionParser
import jscl.text.ParseException
import jscl.text.Parser
import jscl.text.ParserUtils
import jscl.text.msg.Messages
import jscl.util.ArrayUtils

open class Expression : Generic {
    var size: Int = 0
    private var literals: Array<Literal?>? = null
    private var coefficients: Array<JsclInteger?>? = null

    protected constructor()

    constructor(size: Int) {
        init(size)
    }

    fun size(): Int = size

    fun literal(n: Int): Literal = literals!![n]!!

    fun coef(n: Int): JsclInteger = coefficients!![n]!!

    fun init(size: Int) {
        literals = arrayOfNulls(size)
        coefficients = arrayOfNulls(size)
        this.size = size
    }

    fun resize(size: Int) {
        val length = literals!!.size
        if (size < length) {
            val literal = arrayOfNulls<Literal>(size)
            val coef = arrayOfNulls<JsclInteger>(size)
            System.arraycopy(this.literals!!, length - size, literal, 0, size)
            System.arraycopy(this.coefficients!!, length - size, coef, 0, size)
            this.literals = literal
            this.coefficients = coef
            this.size = size
        }
    }

    fun add(that: Expression): Expression {
        val result = newInstance(size + that.size)
        var i = result.size

        var thisI = this.size
        var thatI = that.size

        var thisLiteral = if (thisI > 0) this.literals!![--thisI] else null
        var thatLiteral = if (thatI > 0) that.literals!![--thatI] else null

        while (thisLiteral != null || thatLiteral != null) {
            val c = when {
                thisLiteral == null -> 1
                thatLiteral == null -> -1
                else -> -thisLiteral.compareTo(thatLiteral)
            }

            when {
                c < 0 -> {
                    val thisCoefficient = this.coefficients!![thisI]
                    --i
                    result.literals!![i] = thisLiteral
                    result.coefficients!![i] = thisCoefficient
                    thisLiteral = if (thisI > 0) literals!![--thisI] else null
                }
                c > 0 -> {
                    val en = that.coefficients!![thatI]
                    --i
                    result.literals!![i] = thatLiteral
                    result.coefficients!![i] = en
                    thatLiteral = if (thatI > 0) that.literals!![--thatI] else null
                }
                else -> {
                    val sum = coefficients!![thisI]!!.add(that.coefficients!![thatI]!!)
                    if (sum.signum() != 0) {
                        --i
                        result.literals!![i] = thisLiteral
                        result.coefficients!![i] = sum
                    }

                    thisLiteral = if (thisI > 0) literals!![--thisI] else null
                    thatLiteral = if (thatI > 0) that.literals!![--thatI] else null
                }
            }
        }

        result.resize(result.size - i)
        return result
    }

    override fun add(that: Generic): Generic {
        return when (that) {
            is Expression -> add(that)
            is JsclInteger, is Rational, is NumericWrapper -> add(valueOf(that))
            else -> that.valueOf(this).add(that)
        }
    }

    fun subtract(expression: Expression): Expression {
        return multiplyAndAdd(Literal.newInstance(), JsclInteger.valueOf(-1), expression)
    }

    override fun subtract(that: Generic): Generic {
        return when (that) {
            is Expression -> subtract(that)
            is JsclInteger, is Rational, is NumericWrapper -> subtract(valueOf(that))
            else -> that.valueOf(this).subtract(that)
        }
    }

    fun multiplyAndAdd(literal: Literal, coefficient: JsclInteger, that: Expression): Expression {
        if (coefficient.signum() == 0) return this

        val result = newInstance(size + that.size)
        var i = result.size

        var thisI = this.size
        var thatI = that.size

        var thisLiteral = if (thisI > 0) literals!![--thisI] else null
        var thatLiteral = if (thatI > 0) that.literals!![--thatI]?.multiply(literal) else null

        while (thisLiteral != null || thatLiteral != null) {
            val c = when {
                thisLiteral == null -> 1
                thatLiteral == null -> -1
                else -> -thisLiteral.compareTo(thatLiteral)
            }

            when {
                c < 0 -> {
                    val en = coefficients!![thisI]
                    --i
                    result.literals!![i] = thisLiteral
                    result.coefficients!![i] = en
                    thisLiteral = if (thisI > 0) literals!![--thisI] else null
                }
                c > 0 -> {
                    val en = that.coefficients!![thatI]!!.multiply(coefficient)
                    --i
                    result.literals!![i] = thatLiteral
                    result.coefficients!![i] = en
                    thatLiteral = if (thatI > 0) that.literals!![--thatI]?.multiply(literal) else null
                }
                else -> {
                    val en = coefficients!![thisI]!!.add(that.coefficients!![thatI]!!.multiply(coefficient))
                    if (en.signum() != 0) {
                        --i
                        result.literals!![i] = thisLiteral
                        result.coefficients!![i] = en
                    }
                    thisLiteral = if (thisI > 0) literals!![--thisI] else null
                    thatLiteral = if (thatI > 0) that.literals!![--thatI]?.multiply(literal) else null
                }
            }
        }

        result.resize(result.size - i)
        return result
    }

    fun multiply(expression: Expression): Expression {
        var result = newInstance(0)
        for (i in 0 until size) {
            result = result.multiplyAndAdd(literals!![i]!!, coefficients!![i]!!, expression)
        }
        return result
    }

    override fun multiply(that: Generic): Generic {
        return when (that) {
            is Expression -> multiply(that)
            is JsclInteger, is Rational, is NumericWrapper -> multiply(valueOf(that))
            else -> that.multiply(this)
        }
    }

    override fun divide(that: Generic): Generic {
        val a = divideAndRemainder(that)
        if (a[1].signum() == 0) return a[0]
        else throw NotDivisibleException()
    }

    override fun divideAndRemainder(generic: Generic): Array<Generic> {
        return when (generic) {
            is Expression -> {
                val ex = generic
                val l1 = literalScm()
                val l2 = ex.literalScm()
                val l = l1.gcd(l2)
                val va = l.variables()
                if (va.isEmpty()) {
                    if (signum() == 0 && ex.signum() != 0) return arrayOf(this, JsclInteger.valueOf(0))
                    else try {
                        return divideAndRemainder(ex.integerValue())
                    } catch (e: NotIntegerException) {
                        return arrayOf(JsclInteger.valueOf(0), this)
                    }
                } else {
                    val fact = Polynomial.factory(va[0])
                    val p = fact.valueOf(this).divideAndRemainder(fact.valueOf(ex))
                    return arrayOf(p[0].genericValue(), p[1].genericValue())
                }
            }
            is JsclInteger -> {
                try {
                    val ex = newInstance(size)
                    for (i in 0 until size) {
                        ex.literals!![i] = literals!![i]
                        ex.coefficients!![i] = coefficients!![i]!!.divide(generic)
                    }
                    return arrayOf(ex, JsclInteger.valueOf(0))
                } catch (e: NotDivisibleException) {
                    return arrayOf(JsclInteger.valueOf(0), this)
                }
            }
            is Rational, is NumericWrapper -> divideAndRemainder(valueOf(generic))
            else -> generic.valueOf(this).divideAndRemainder(generic)
        }
    }

    override fun gcd(generic: Generic): Generic {
        return when (generic) {
            is Expression -> {
                val that = generic
                val thisL = this.literalScm()
                val thatL = that.literalScm()
                val gcdL = thisL.gcd(thatL)
                val vars = gcdL.variables()
                if (vars.isEmpty()) {
                    if (signum() == 0) that
                    else this.gcd().gcd(that.gcd())
                } else {
                    val p = Polynomial.factory(vars[0])
                    p.valueOf(this).gcd(p.valueOf(that)).genericValue()
                }
            }
            is JsclInteger -> {
                if (generic.signum() == 0) this
                else this.gcd().gcd(generic)
            }
            is Rational, is NumericWrapper -> gcd(valueOf(generic))
            else -> generic.valueOf(this).gcd(generic)
        }
    }

    override fun gcd(): Generic {
        var result = JsclInteger.valueOf(0)
        for (i in size - 1 downTo 0) {
            result = result.gcd(coefficients!![i]!!)
        }
        return result
    }

    fun literalScm(): Literal {
        var result = Literal.newInstance()
        for (i in 0 until size) {
            result = result.scm(literals!![i]!!)
        }
        return result
    }

    override fun negate(): Generic {
        return multiply(JsclInteger.valueOf(-1))
    }

    override fun signum(): Int {
        return if (size == 0) 0 else coefficients!![0]!!.signum()
    }

    override fun degree(): Int = 0

    override fun antiDerivative(variable: Variable): Generic {
        if (isPolynomial(variable)) {
            return (Polynomial.factory(variable).valueOf(this) as UnivariatePolynomial).antiderivative().genericValue()
        } else {
            try {
                val v = variableValue()
                try {
                    return v.antiDerivative(variable)
                } catch (e: NotIntegrableException) {
                    if (v is Fraction) {
                        val g = v.getParameters()
                        if (g!![1].isConstant(variable)) {
                            return Inverse(g[1]).selfExpand().multiply(g[0].antiDerivative(variable))
                        }
                    }
                }
            } catch (e: NotVariableException) {
                val sumElements = sumValue()
                if (sumElements.size > 1) {
                    var result: Generic = JsclInteger.valueOf(0)
                    for (sumElement in sumElements) {
                        result = result.add(sumElement.antiDerivative(variable))
                    }
                    return result
                } else {
                    val products = sumElements[0].productValue()
                    var constantProduct: Generic = JsclInteger.valueOf(1)
                    var notConstantProduct: Generic = JsclInteger.valueOf(1)
                    for (product in products) {
                        if (product.isConstant(variable)) {
                            constantProduct = constantProduct.multiply(product)
                        } else {
                            notConstantProduct = notConstantProduct.multiply(product)
                        }
                    }
                    if (constantProduct.compareTo(JsclInteger.valueOf(1)) != 0) {
                        return constantProduct.multiply(notConstantProduct.antiDerivative(variable))
                    }
                }
            }
        }
        throw NotIntegrableException(this)
    }

    override fun derivative(variable: Variable): Generic {
        var s: Generic = JsclInteger.valueOf(0)
        val l = literalScm()
        val n = l.size()
        for (i in 0 until n) {
            val v = l.getVariable(i)
            val a = (Polynomial.factory(v).valueOf(this) as UnivariatePolynomial).derivative(variable).genericValue()
            s = s.add(a)
        }
        return s
    }

    override fun substitute(variable: Variable, generic: Generic): Generic {
        val content = literalScm().content { v -> v.substitute(variable, generic) }
        return substitute(content)
    }

    private fun substitute(content: Map<Variable, Generic>): Generic {
        var sum: Generic = JsclInteger.ZERO
        for (i in 0 until size) {
            val literal = literals!![i]!!
            var sumElement: Generic = coefficients!![i]!!
            for (j in 0 until literal.size()) {
                val v = literal.getVariable(j)
                val power = literal.getPower(j)
                val contentVariable = content[v]
                val b = pow(contentVariable!!, power)
                if (Matrix.isMatrixProduct(sumElement, b)) {
                    throw ArithmeticException("Should not be matrix!")
                }
                sumElement = sumElement.multiply(b)
            }
            sum = sum.add(sumElement)
        }
        return sum
    }

    private fun pow(g: Generic, power: Int): Generic {
        return when (power) {
            0 -> JsclInteger.valueOf(1)
            1 -> g
            else -> g.pow(power)
        }
    }

    override fun expand(): Generic {
        return substitute(literalScm().content(EXPAND_CONVERTER))
    }

    override fun factorize(): Generic {
        return Factorization.compute(substitute(literalScm().content(FACTORIZE_CONVERTER)))
    }

    override fun elementary(): Generic {
        return substitute(literalScm().content(ELEMENTARY_CONVERTER))
    }

    override fun simplify(): Generic {
        return Simplification.compute(this)
    }

    override fun numeric(): Generic {
        return try {
            integerValue().numeric()
        } catch (ex: NotIntegerException) {
            substitute(literalScm().content(NUMERIC_CONVERTER))
        }
    }

    override fun valueOf(generic: Generic): Generic {
        return newInstance(0).init(generic)
    }

    override fun sumValue(): Array<Generic> {
        val result = arrayOfNulls<Generic>(size)
        for (i in result.indices) {
            result[i] = valueOf(literals!![i]!!, coefficients!![i]!!)
        }
        return result as Array<Generic>
    }

    override fun productValue(): Array<Generic> {
        return when {
            size == 0 -> arrayOf(JsclInteger.valueOf(0))
            size == 1 -> {
                val l = literals!![0]!!
                val k = coefficients!![0]!!
                val productElements = l.productValue()
                if (k.compareTo(JsclInteger.valueOf(1)) == 0) {
                    productElements
                } else {
                    val result = arrayOfNulls<Generic>(productElements.size + 1)
                    System.arraycopy(productElements, 0, result, 1, productElements.size)
                    result[0] = k
                    result as Array<Generic>
                }
            }
            else -> throw NotProductException()
        }
    }

    override fun powerValue(): Power {
        return when {
            size == 0 -> Power(JsclInteger.valueOf(0), 1)
            size == 1 -> {
                val l = literals!![0]!!
                val en = coefficients!![0]!!
                when {
                    en.compareTo(JsclInteger.valueOf(1)) == 0 -> l.powerValue()
                    l.degree() == 0 -> en.powerValue()
                    else -> throw NotPowerException()
                }
            }
            else -> throw NotPowerException()
        }
    }

    override fun expressionValue(): Expression = this

    override val isInteger: Boolean
        get() = try {
            integerValue()
            true
        } catch (e: NotIntegerException) {
            false
        }

    override fun integerValue(): JsclInteger {
        return when {
            size == 0 -> JsclInteger.valueOf(0)
            size == 1 -> {
                val l = literals!![0]!!
                val c = coefficients!![0]!!
                if (l.degree() == 0) c
                else throw NotIntegerException.get()
            }
            else -> throw NotIntegerException.get()
        }
    }

    override fun doubleValue(): Double {
        return when {
            size == 0 -> 0.0
            size == 1 -> {
                val l = literals!![0]!!
                val c = coefficients!![0]!!
                if (l.degree() == 0) c.doubleValue()
                else throw NotDoubleException.get()
            }
            else -> throw NotDoubleException.get()
        }
    }

    override fun variableValue(): Variable {
        return when {
            size == 0 -> throw NotVariableException()
            size == 1 -> {
                val l = literals!![0]!!
                val c = coefficients!![0]!!
                if (c.compareTo(JsclInteger.valueOf(1)) == 0) l.variableValue()
                else throw NotVariableException()
            }
            else -> throw NotVariableException()
        }
    }

    override fun variables(): Array<Variable> {
        return literalScm().variables()
    }

    override fun isPolynomial(variable: Variable): Boolean {
        var result = true
        val l = literalScm()
        for (i in 0 until l.size()) {
            val v = l.getVariable(i)
            if (!v.isConstant(variable) && !v.isIdentity(variable)) {
                result = false
                break
            }
        }
        return result
    }

    override fun isConstant(variable: Variable): Boolean {
        val l = literalScm()
        for (i in 0 until l.size()) {
            if (!l.getVariable(i).isConstant(variable)) {
                return false
            }
        }
        return true
    }

    fun grad(variable: Array<Variable>): JsclVector {
        val v = Array<Generic>(variable.size) { i -> derivative(variable[i]) }
        return JsclVector(v)
    }

    fun laplacian(variable: Array<Variable>): Generic {
        return grad(variable).divergence(variable)
    }

    fun dalembertian(variable: Array<Variable>): Generic {
        var a = derivative(variable[0]).derivative(variable[0])
        for (i in 1 until 4) a = a.subtract(derivative(variable[i]).derivative(variable[i]))
        return a
    }

    fun compareTo(expression: Expression): Int {
        var i1 = size
        var i2 = expression.size
        var l1 = if (i1 == 0) null else literals!![--i1]
        var l2 = if (i2 == 0) null else expression.literals!![--i2]
        while (l1 != null || l2 != null) {
            val c = when {
                l1 == null -> -1
                l2 == null -> 1
                else -> l1.compareTo(l2)
            }
            if (c < 0) return -1
            else if (c > 0) return 1
            else {
                val cc = coefficients!![i1]!!.compareTo(expression.coefficients!![i2]!!)
                if (cc < 0) return -1
                else if (cc > 0) return 1
                l1 = if (i1 == 0) null else literals!![--i1]
                l2 = if (i2 == 0) null else expression.literals!![--i2]
            }
        }
        return 0
    }

    override fun compareTo(generic: Generic): Int {
        return when (generic) {
            is Expression -> compareTo(generic)
            is JsclInteger, is Rational, is NumericWrapper -> compareTo(valueOf(generic))
            else -> generic.valueOf(this).compareTo(generic)
        }
    }

    fun init(lit: Literal, integer: JsclInteger) {
        if (integer.signum() != 0) {
            init(1)
            literals!![0] = lit
            coefficients!![0] = integer
        } else init(0)
    }

    fun init(expression: Expression) {
        init(expression.size)
        System.arraycopy(expression.literals!!, 0, literals!!, 0, size)
        System.arraycopy(expression.coefficients!!, 0, coefficients!!, 0, size)
    }

    fun init(integer: JsclInteger) {
        init(Literal.newInstance(), integer)
    }

    fun init(rational: Rational) {
        try {
            init(Literal.newInstance(), rational.integerValue())
        } catch (e: NotIntegerException) {
            init(Literal.valueOf(rational.variableValue()), JsclInteger.valueOf(1))
        }
    }

    fun init(generic: Generic): Expression {
        when (generic) {
            is Expression -> init(generic)
            is JsclInteger -> init(generic)
            is NumericWrapper -> init(generic)
            is Rational -> init(generic)
            else -> throw ArithmeticException("Could not initialize expression with ${generic::class.simpleName}")
        }
        return this
    }

    fun init(numericWrapper: NumericWrapper): Expression {
        val literal = Literal()
        literal.init(ExpressionVariable(numericWrapper), 1)
        init(literal, JsclInteger.ONE)
        return this
    }

    override fun toString(): String {
        val result = StringBuilder()

        if (signum() == 0) {
            result.append("0")
        }

        for (i in 0 until size) {
            val literal = literals!![i]!!
            val coefficient = coefficients!![i]!!

            if (coefficient.signum() > 0 && i > 0) {
                result.append("+")
            }

            if (literal.degree() == 0) {
                result.append(coefficient)
            } else {
                if (coefficient.abs().compareTo(JsclInteger.valueOf(1)) == 0) {
                    if (coefficient.signum() < 0) {
                        result.append("-")
                    }
                } else {
                    result.append(coefficient).append("*")
                }
                result.append(literal)
            }
        }

        return result.toString()
    }

    override fun toJava(): String {
        val result = StringBuilder()
        if (signum() == 0) {
            result.append("JsclDouble.valueOf(0)")
        }

        for (i in 0 until size) {
            var l = literals!![i]!!
            var en = coefficients!![i]!!
            if (i > 0) {
                if (en.signum() < 0) {
                    result.append(".subtract(")
                    en = en.negate()
                } else result.append(".add(")
            }
            if (l.degree() == 0) result.append(en.toJava())
            else {
                if (en.abs().compareTo(JsclInteger.valueOf(1)) == 0) {
                    if (en.signum() > 0) result.append(l.toJava())
                    else if (en.signum() < 0) result.append(l.toJava()).append(".negate()")
                } else result.append(en.toJava()).append(".multiply(").append(l.toJava()).append(")")
            }
            if (i > 0) result.append(")")
        }

        return result.toString()
    }

    override fun toMathML(element: MathML, data: Any?) {
        val e1 = element.element("mrow")
        if (signum() == 0) {
            val e2 = element.element("mn")
            e2.appendChild(element.text("0"))
            e1.appendChild(e2)
        }
        for (i in 0 until size) {
            val l = literals!![i]!!
            val en = coefficients!![i]!!
            if (en.signum() > 0 && i > 0) {
                val e2 = element.element("mo")
                e2.appendChild(element.text("+"))
                e1.appendChild(e2)
            }
            if (l.degree() == 0) separateSign(e1, en)
            else {
                if (en.abs().compareTo(JsclInteger.valueOf(1)) == 0) {
                    if (en.signum() < 0) {
                        val e2 = element.element("mo")
                        e2.appendChild(element.text("-"))
                        e1.appendChild(e2)
                    }
                } else separateSign(e1, en)
                l.toMathML(e1, null)
            }
        }
        element.appendChild(e1)
    }

    override val constants: Set<Constant>
        get() {
            val result = HashSet<Constant>()
            for (literal in literals!!) {
                if (literal != null) {
                    for (variable in literal.variables()) {
                        result.addAll(variable.constants)
                    }
                }
            }
            return result
        }

    private fun newInstance(n: Int): Expression = Expression(n)

    companion object {
        private val FACTORIZE_CONVERTER: (Variable) -> Generic = { variable -> variable.factorize() }
        private val ELEMENTARY_CONVERTER: (Variable) -> Generic = { variable -> variable.elementary() }
        private val EXPAND_CONVERTER: (Variable) -> Generic = { variable -> variable.expand() }
        private val NUMERIC_CONVERTER: (Variable) -> Generic = { variable -> variable.numeric() }

        @JvmStatic
        fun variables(elements: Array<Generic>): Array<Variable> {
            val result = mutableListOf<Variable>()
            for (element in elements) {
                for (variable in element.variables()) {
                    if (variable !in result) {
                        result.add(variable)
                    }
                }
            }
            return ArrayUtils.toArray(result, arrayOfNulls<Variable>(result.size)) as Array<Variable>
        }

        @JvmStatic
        fun valueOf(variable: Variable): Expression = valueOf(Literal.valueOf(variable))

        @JvmStatic
        fun valueOf(literal: Literal): Expression = valueOf(literal, JsclInteger.valueOf(1))

        @JvmStatic
        fun valueOf(integer: JsclInteger): Expression = valueOf(Literal.newInstance(), integer)

        @JvmStatic
        fun valueOf(literal: Literal, integer: JsclInteger): Expression {
            val result = Expression()
            result.init(literal, integer)
            return result
        }

        @JvmStatic
        fun valueOf(rational: Rational): Expression {
            val ex = Expression()
            ex.init(rational)
            return ex
        }

        @JvmStatic
        fun valueOf(constant: Constant): Expression {
            val expression = Expression(1)
            val literal = Literal()
            literal.init(constant, 1)
            expression.init(literal, JsclInteger.ONE)
            return expression
        }

        @JvmStatic
        fun valueOf(value: Double): Expression {
            val expression = Expression(1)
            val literal = Literal()
            literal.init(DoubleVariable(NumericWrapper(Real.valueOf(value))), 1)
            expression.init(literal, JsclInteger.ONE)
            return expression
        }

        @JvmStatic
        @Throws(ParseException::class)
        fun valueOf(expression: String): Expression {
            val p = Parser.Parameters.get(expression)
            val generic = ExpressionParser.parser.parse(p, null)
            ParserUtils.skipWhitespaces(p)

            val index = p.position.toInt()
            if (index < expression.length) {
                throw ParseException(index, expression, Messages.msg_1, index + 1)
            }

            return Expression().init(generic)
        }

        @JvmStatic
        fun separateSign(element: MathML, generic: Generic) {
            if (generic.signum() < 0) {
                val e1 = element.element("mo")
                e1.appendChild(element.text("-"))
                element.appendChild(e1)
                generic.negate().toMathML(element, null)
            } else {
                generic.toMathML(element, null)
            }
        }
    }
}
