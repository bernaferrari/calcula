package jscl.math

import jscl.math.function.Conjugate
import jscl.math.function.Constant
import jscl.math.function.Fraction
import jscl.mathml.MathML
import jscl.util.ArrayComparator

open class JsclVector(
    @JvmField internal val elements: Array<Generic>
) : Generic() {

    @JvmField
    internal val rows: Int = elements.size

    fun elements(): Array<Generic> = elements

    fun add(vector: JsclVector): JsclVector {
        val result = newInstance() as JsclVector
        for (i in 0 until rows) {
            result.elements[i] = elements[i].add(vector.elements[i])
        }
        return result
    }

    override fun add(that: Generic): Generic {
        return if (that is JsclVector) {
            add(that)
        } else {
            add(valueOf(that) as JsclVector)
        }
    }

    fun subtract(vector: JsclVector): JsclVector {
        val result = newInstance() as JsclVector
        for (i in 0 until rows) {
            result.elements[i] = elements[i].subtract(vector.elements[i])
        }
        return result
    }

    override fun subtract(that: Generic): Generic {
        return if (that is JsclVector) {
            subtract(that)
        } else {
            subtract(valueOf(that) as JsclVector)
        }
    }

    override fun multiply(that: Generic): Generic {
        return when (that) {
            is JsclVector -> scalarProduct(that)
            is Matrix -> that.transpose().multiply(this)
            else -> {
                val result = newInstance() as JsclVector
                for (i in 0 until rows) {
                    result.elements[i] = elements[i].multiply(that)
                }
                result
            }
        }
    }

    override fun divide(that: Generic): Generic {
        return when (that) {
            is JsclVector -> throw ArithmeticException("Unable to divide vector by vector!")
            is Matrix -> multiply(that.inverse())
            else -> {
                val result = newInstance() as JsclVector
                for (i in 0 until rows) {
                    result.elements[i] = try {
                        elements[i].divide(that)
                    } catch (e: NotDivisibleException) {
                        Fraction(elements[i], that).selfExpand()
                    }
                }
                result
            }
        }
    }

    override fun gcd(generic: Generic): Generic = throw ArithmeticException("Vector gcd not supported")

    override fun gcd(): Generic = throw ArithmeticException("Vector gcd not supported")

    override fun negate(): Generic {
        val result = newInstance() as JsclVector
        for (i in 0 until rows) {
            result.elements[i] = elements[i].negate()
        }
        return result
    }

    override fun signum(): Int {
        for (i in 0 until rows) {
            val c = elements[i].signum()
            if (c < 0) return -1
            else if (c > 0) return 1
        }
        return 0
    }

    override fun degree(): Int = 0

    override fun antiDerivative(variable: Variable): Generic {
        val result = newInstance() as JsclVector
        for (i in 0 until rows) {
            result.elements[i] = elements[i].antiDerivative(variable)
        }
        return result
    }

    override fun derivative(variable: Variable): Generic {
        val result = newInstance() as JsclVector
        for (i in 0 until rows) {
            result.elements[i] = elements[i].derivative(variable)
        }
        return result
    }

    override fun substitute(variable: Variable, generic: Generic): Generic {
        val result = newInstance() as JsclVector
        for (i in 0 until rows) {
            result.elements[i] = elements[i].substitute(variable, generic)
        }
        return result
    }

    override fun expand(): Generic {
        val v = newInstance() as JsclVector
        for (i in 0 until rows) v.elements[i] = elements[i].expand()
        return v
    }

    override fun factorize(): Generic {
        val result = newInstance() as JsclVector
        for (i in 0 until rows) {
            result.elements[i] = elements[i].factorize()
        }
        return result
    }

    override fun elementary(): Generic {
        val result = newInstance() as JsclVector
        for (i in 0 until rows) {
            result.elements[i] = elements[i].elementary()
        }
        return result
    }

    override fun simplify(): Generic {
        val result = newInstance() as JsclVector
        for (i in 0 until rows) {
            result.elements[i] = elements[i].simplify()
        }
        return result
    }

    override fun numeric(): Generic {
        return NumericWrapper(this)
    }

    override fun valueOf(generic: Generic): Generic {
        if (generic is JsclVector || generic is Matrix) {
            throw ArithmeticException("Unable to create vector: vector of vectors or vector of matrices are forbidden!")
        }
        val v = unity(rows).multiply(generic) as JsclVector
        return newInstance(v.elements)
    }

    override fun sumValue(): Array<Generic> = arrayOf(this)

    override fun productValue(): Array<Generic> = arrayOf(this)

    override fun powerValue(): Power = Power(this, 1)

    override fun expressionValue(): Expression {
        throw NotExpressionException()
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

    override fun variables(): Array<Variable> = emptyArray()

    override fun isPolynomial(variable: Variable): Boolean = false

    override fun isConstant(variable: Variable): Boolean = false

    fun magnitude2(): Generic = scalarProduct(this)

    fun scalarProduct(vector: JsclVector): Generic {
        var result: Generic = JsclInteger.valueOf(0)
        for (i in 0 until rows) {
            result = result.add(elements[i].multiply(vector.elements[i]))
        }
        return result
    }

    fun vectorProduct(vector: JsclVector): JsclVector {
        val result = newInstance() as JsclVector
        @Suppress("UNCHECKED_CAST")
        val m = arrayOf(
            arrayOf<Generic?>(JsclInteger.valueOf(0), elements[2].negate(), elements[1]),
            arrayOf<Generic?>(elements[2], JsclInteger.valueOf(0), elements[0].negate()),
            arrayOf<Generic?>(elements[1].negate(), elements[0], JsclInteger.valueOf(0))
        )
        val v2 = Matrix(m).multiply(vector) as JsclVector
        for (i in 0 until rows) {
            result.elements[i] = if (i < v2.rows) v2.elements[i] else JsclInteger.valueOf(0)
        }
        return result
    }

    fun complexProduct(vector: JsclVector): JsclVector {
        return product(Clifford(0, 1).operator(), vector)
    }

    fun quaternionProduct(vector: JsclVector): JsclVector {
        return product(Clifford(0, 2).operator(), vector)
    }

    fun geometricProduct(vector: JsclVector, algebra: IntArray?): JsclVector {
        return product(Clifford(algebra ?: intArrayOf(Clifford.log2e(rows), 0)).operator(), vector)
    }

    internal fun product(product: Array<IntArray>, vector: JsclVector): JsclVector {
        val v = newInstance() as JsclVector
        for (i in 0 until rows) v.elements[i] = JsclInteger.valueOf(0)
        for (i in 0 until rows) {
            for (j in 0 until rows) {
                val a = elements[i].multiply(vector.elements[j])
                val k = kotlin.math.abs(product[i][j]) - 1
                v.elements[k] = v.elements[k].add(if (product[i][j] < 0) a.negate() else a)
            }
        }
        return v
    }

    fun divergence(variable: Array<Variable>): Generic {
        var a: Generic = JsclInteger.valueOf(0)
        for (i in 0 until rows) a = a.add(elements[i].derivative(variable[i]))
        return a
    }

    fun curl(variable: Array<Variable>): JsclVector {
        val v = newInstance() as JsclVector
        v.elements[0] = elements[2].derivative(variable[1]).subtract(elements[1].derivative(variable[2]))
        v.elements[1] = elements[0].derivative(variable[2]).subtract(elements[2].derivative(variable[0]))
        v.elements[2] = elements[1].derivative(variable[0]).subtract(elements[0].derivative(variable[1]))
        for (i in 3 until rows) v.elements[i] = elements[i]
        return v
    }

    fun jacobian(variable: Array<Variable>): Matrix {
        val m = Matrix(Array(rows) { arrayOfNulls<Generic>(variable.size) })
        for (i in 0 until rows) {
            for (j in variable.indices) {
                m.elements[i][j] = elements[i].derivative(variable[j])
            }
        }
        return m
    }

    fun del(variable: Array<Variable>, algebra: IntArray?): Generic {
        return differential(Clifford(algebra ?: intArrayOf(Clifford.log2e(rows), 0)).operator(), variable)
    }

    internal fun differential(product: Array<IntArray>, variable: Array<Variable>): JsclVector {
        val v = newInstance() as JsclVector
        for (i in 0 until rows) v.elements[i] = JsclInteger.valueOf(0)
        val l = Clifford.log2e(rows)
        for (i in 1..l) {
            for (j in 0 until rows) {
                val a = elements[j].derivative(variable[i - 1])
                val k = kotlin.math.abs(product[i][j]) - 1
                v.elements[k] = v.elements[k].add(if (product[i][j] < 0) a.negate() else a)
            }
        }
        return v
    }

    fun conjugate(): Generic {
        val v = newInstance() as JsclVector
        for (i in 0 until rows) {
            v.elements[i] = Conjugate(elements[i]).selfExpand()
        }
        return v
    }

    fun compareTo(vector: JsclVector): Int {
        @Suppress("UNCHECKED_CAST")
        return ArrayComparator.comparator.compare(
            elements as Array<Comparable<*>?>,
            vector.elements as Array<Comparable<*>?>
        )
    }

    override fun compareTo(generic: Generic): Int {
        return if (generic is JsclVector) {
            compareTo(generic)
        } else {
            compareTo(valueOf(generic) as JsclVector)
        }
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append("[")
        for (i in 0 until rows) {
            result.append(elements[i]).append(if (i < rows - 1) ", " else "")
        }
        result.append("]")
        return result.toString()
    }

    override fun toJava(): String {
        val result = StringBuilder()
        result.append("new Vector(new Numeric[] {")
        for (i in 0 until rows) {
            result.append(elements[i].toJava()).append(if (i < rows - 1) ", " else "")
        }
        result.append("})")
        return result.toString()
    }

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
        get() {
            val result = HashSet<Constant>(elements.size)
            for (el in elements) {
                result.addAll(el.constants)
            }
            return result
        }

    protected open fun bodyToMathML(e0: MathML) {
        val e1 = e0.element("mfenced")
        val e2 = e0.element("mtable")
        for (i in 0 until rows) {
            val e3 = e0.element("mtr")
            val e4 = e0.element("mtd")
            elements[i].toMathML(e4, null)
            e3.appendChild(e4)
            e2.appendChild(e3)
        }
        e1.appendChild(e2)
        e0.appendChild(e1)
    }

    internal open fun newInstance(): Generic {
        return newInstance(arrayOfNulls<Generic>(rows) as Array<Generic>)
    }

    internal open fun newInstance(element: Array<Generic>): Generic {
        return JsclVector(element)
    }

    companion object {
        @JvmStatic
        fun unity(dimension: Int): JsclVector {
            val result = JsclVector(arrayOfNulls<Generic>(dimension) as Array<Generic>)
            for (i in 0 until result.rows) {
                result.elements[i] = if (i == 0) JsclInteger.valueOf(1) else JsclInteger.valueOf(0)
            }
            return result
        }
    }
}
