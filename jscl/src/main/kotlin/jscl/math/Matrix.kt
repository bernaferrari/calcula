@file:Suppress("UNCHECKED_CAST")

package jscl.math

import jscl.math.function.Conjugate
import jscl.math.function.Constant
import jscl.math.function.Fraction
import jscl.math.function.trigonometric.Cos
import jscl.math.function.trigonometric.Sin
import jscl.mathml.MathML
import jscl.util.ArrayComparator

open class Matrix(
    @JvmField internal val elements: Array<Array<Generic?>>
) : Generic() {

    @JvmField internal val rows: Int = elements.size
    @JvmField internal val cols: Int = if (elements.isNotEmpty()) elements[0].size else 0

    fun elements(): Array<Array<Generic?>> = elements

    fun add(matrix: Matrix): Matrix {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[i][j] = elements[i][j]!!.add(matrix.elements[i][j]!!)
            }
        }
        return m
    }

    override fun add(that: Generic): Generic {
        return if (that is Matrix) {
            add(that)
        } else {
            add(valueOf(that) as Matrix)
        }
    }

    fun subtract(matrix: Matrix): Matrix {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[i][j] = elements[i][j]!!.subtract(matrix.elements[i][j]!!)
            }
        }
        return m
    }

    override fun subtract(that: Generic): Generic {
        return if (that is Matrix) {
            subtract(that)
        } else {
            subtract(valueOf(that) as Matrix)
        }
    }

    fun multiply(matrix: Matrix): Matrix {
        if (cols != matrix.rows) {
            throw ArithmeticException("Unable to multiply matrix by matrix: number of columns of left matrix doesn't match number of rows of right matrix!")
        }
        val m = newInstance(Array(rows) { arrayOfNulls<Generic>(matrix.cols) }) as Matrix
        for (i in 0 until rows) {
            for (j in 0 until matrix.cols) {
                m.elements[i][j] = JsclInteger.valueOf(0)
                for (k in 0 until cols) {
                    m.elements[i][j] = m.elements[i][j]!!.add(elements[i][k]!!.multiply(matrix.elements[k][j]!!))
                }
            }
        }
        return m
    }

    override fun multiply(that: Generic): Generic {
        return when (that) {
            is Matrix -> multiply(that)
            is JsclVector -> {
                val v = that.newInstance(arrayOfNulls<Generic>(rows) as Array<Generic>) as JsclVector
                val v2 = that
                if (cols != v2.rows) {
                    throw ArithmeticException("Unable to multiply matrix by vector: number of matrix columns doesn't match number of vector rows!")
                }
                for (i in 0 until rows) {
                    v.elements[i] = JsclInteger.valueOf(0)
                    for (k in 0 until cols) {
                        v.elements[i] = v.elements[i].add(elements[i][k]!!.multiply(v2.elements[k]))
                    }
                }
                v
            }
            else -> {
                val m = newInstance() as Matrix
                for (i in 0 until rows) {
                    for (j in 0 until cols) {
                        m.elements[i][j] = elements[i][j]!!.multiply(that)
                    }
                }
                m
            }
        }
    }

    override fun divide(that: Generic): Generic {
        return when (that) {
            is Matrix -> multiply(that.inverse())
            is JsclVector -> throw ArithmeticException("Unable to divide matrix by vector: matrix could not be divided by vector!")
            else -> {
                val m = newInstance() as Matrix
                for (i in 0 until rows) {
                    for (j in 0 until cols) {
                        m.elements[i][j] = try {
                            elements[i][j]!!.divide(that)
                        } catch (e: NotDivisibleException) {
                            Fraction(elements[i][j]!!, that).selfExpand()
                        }
                    }
                }
                m
            }
        }
    }

    override fun gcd(generic: Generic): Generic = throw ArithmeticException("Matrix gcd not supported")

    override fun gcd(): Generic = throw ArithmeticException("Matrix gcd not supported")

    override fun negate(): Generic {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[i][j] = elements[i][j]!!.negate()
            }
        }
        return m
    }

    override fun signum(): Int {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val c = elements[i][j]!!.signum()
                if (c < 0) return -1
                else if (c > 0) return 1
            }
        }
        return 0
    }

    override fun degree(): Int = 0

    override fun antiDerivative(variable: Variable): Generic {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[i][j] = elements[i][j]!!.antiDerivative(variable)
            }
        }
        return m
    }

    override fun derivative(variable: Variable): Generic {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[i][j] = elements[i][j]!!.derivative(variable)
            }
        }
        return m
    }

    override fun substitute(variable: Variable, generic: Generic): Generic {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[i][j] = elements[i][j]!!.substitute(variable, generic)
            }
        }
        return m
    }

    override fun expand(): Generic {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[i][j] = elements[i][j]!!.expand()
            }
        }
        return m
    }

    override fun factorize(): Generic {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[i][j] = elements[i][j]!!.factorize()
            }
        }
        return m
    }

    override fun elementary(): Generic {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[i][j] = elements[i][j]!!.elementary()
            }
        }
        return m
    }

    override fun simplify(): Generic {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[i][j] = elements[i][j]!!.simplify()
            }
        }
        return m
    }

    override fun numeric(): Generic = NumericWrapper(this)

    override fun valueOf(generic: Generic): Generic {
        if (generic is Matrix || generic is JsclVector) {
            throw ArithmeticException("Unable to create matrix: matrix of vectors and matrix of matrices are forbidden")
        }
        val m = identity(rows, cols).multiply(generic) as Matrix
        return newInstance(m.elements)
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

    fun vectors(): Array<JsclVector> {
        return Array(rows) { i ->
            JsclVector(Array(cols) { j -> elements[i][j]!! })
        }
    }

    fun tensorProduct(matrix: Matrix): Generic {
        val m = newInstance(Array(rows * matrix.rows) { arrayOfNulls<Generic>(cols * matrix.cols) }) as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                for (k in 0 until matrix.rows) {
                    for (l in 0 until matrix.cols) {
                        m.elements[i * matrix.rows + k][j * matrix.cols + l] = elements[i][j]!!.multiply(matrix.elements[k][l]!!)
                    }
                }
            }
        }
        return m
    }

    fun transpose(): Matrix {
        val m = newInstance(Array(cols) { arrayOfNulls<Generic>(rows) }) as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[j][i] = elements[i][j]
            }
        }
        return m
    }

    fun trace(): Generic {
        var s: Generic = JsclInteger.valueOf(0)
        for (i in 0 until rows) {
            s = s.add(elements[i][i]!!)
        }
        return s
    }

    override fun inverse(): Generic {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until rows) {
                m.elements[i][j] = inverseElement(i, j)
            }
        }
        return m.transpose().divide(determinant())
    }

    fun inverseElement(k: Int, l: Int): Generic {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until rows) {
                m.elements[i][j] = if (i == k) JsclInteger.valueOf(if (j == l) 1 else 0) else elements[i][j]
            }
        }
        return m.determinant()
    }

    fun determinant(): Generic {
        if (rows > 1) {
            var a: Generic = JsclInteger.valueOf(0)
            for (i in 0 until rows) {
                if (elements[i][0]!!.signum() != 0) {
                    val m = newInstance(Array(rows - 1) { arrayOfNulls<Generic>(rows - 1) }) as Matrix
                    for (j in 0 until rows - 1) {
                        for (k in 0 until rows - 1) {
                            m.elements[j][k] = elements[if (j < i) j else j + 1][k + 1]
                        }
                    }
                    a = if (i % 2 == 0) {
                        a.add(elements[i][0]!!.multiply(m.determinant()))
                    } else {
                        a.subtract(elements[i][0]!!.multiply(m.determinant()))
                    }
                }
            }
            return a
        } else if (rows > 0) {
            return elements[0][0]!!
        } else {
            return JsclInteger.valueOf(0)
        }
    }

    fun conjugate(): Generic {
        val m = newInstance() as Matrix
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                m.elements[i][j] = Conjugate(elements[i][j]!!).selfExpand()
            }
        }
        return m
    }

    fun compareTo(matrix: Matrix): Int {
        @Suppress("UNCHECKED_CAST")
        return ArrayComparator.comparator.compare(
            vectors() as Array<Comparable<*>?>,
            matrix.vectors() as Array<Comparable<*>?>
        )
    }

    override fun compareTo(generic: Generic): Int {
        return if (generic is Matrix) {
            compareTo(generic)
        } else {
            compareTo(valueOf(generic) as Matrix)
        }
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append("[")
        for (i in 0 until rows) {
            result.append("[")
            for (j in 0 until cols) {
                result.append(elements[i][j]).append(if (j < cols - 1) ", " else "")
            }
            result.append("]").append(if (i < rows - 1) ",\n" else "")
        }
        result.append("]")
        return result.toString()
    }

    override fun toJava(): String {
        val result = StringBuilder()
        result.append("new Matrix(new Numeric[][] {")
        for (i in 0 until rows) {
            result.append("{")
            for (j in 0 until cols) {
                result.append(elements[i][j]!!.toJava()).append(if (j < cols - 1) ", " else "")
            }
            result.append("}").append(if (i < rows - 1) ", " else "")
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
            val result = HashSet<Constant>()
            for (element in elements) {
                for (generic in element) {
                    result.addAll(generic!!.constants)
                }
            }
            return result
        }

    protected fun bodyToMathML(e0: MathML) {
        val e1 = e0.element("mfenced")
        val e2 = e0.element("mtable")
        for (i in 0 until rows) {
            val e3 = e0.element("mtr")
            for (j in 0 until cols) {
                val e4 = e0.element("mtd")
                elements[i][j]!!.toMathML(e4, null)
                e3.appendChild(e4)
            }
            e2.appendChild(e3)
        }
        e1.appendChild(e2)
        e0.appendChild(e1)
    }

    protected fun newInstance(): Generic {
        return newInstance(Array(rows) { arrayOfNulls<Generic>(cols) })
    }

    companion object {
        @JvmStatic
        fun isMatrixProduct(a: Generic, b: Generic): Boolean {
            return (a is Matrix && b is Matrix) ||
                    (a is Matrix && b is JsclVector) ||
                    (a is JsclVector && b is Matrix)
        }

        @JvmStatic
        @JvmOverloads
        fun identity(dimension: Int, p: Int = dimension): Matrix {
            val m = Matrix(Array(dimension) { arrayOfNulls<Generic>(p) })
            for (i in 0 until dimension) {
                for (j in 0 until p) {
                    m.elements[i][j] = if (i == j) JsclInteger.valueOf(1) else JsclInteger.valueOf(0)
                }
            }
            return m
        }

        @JvmStatic
        fun frame(vector: Array<JsclVector>): Matrix {
            val m = Matrix(Array(if (vector.isNotEmpty()) vector[0].rows else 0) { arrayOfNulls<Generic>(vector.size) })
            for (i in 0 until m.rows) {
                for (j in 0 until m.cols) {
                    m.elements[i][j] = vector[j].elements[i]
                }
            }
            return m
        }

        @JvmStatic
        @JvmOverloads
        fun rotation(dimension: Int, axis1: Int, axis2: Int = 2, angle: Generic): Matrix {
            val m = Matrix(Array(dimension) { arrayOfNulls<Generic>(dimension) })
            for (i in 0 until m.rows) {
                for (j in 0 until m.cols) {
                    m.elements[i][j] = when {
                        i == axis1 && j == axis1 -> Cos(angle).selfExpand()
                        i == axis1 && j == axis2 -> Sin(angle).selfExpand().negate()
                        i == axis2 && j == axis1 -> Sin(angle).selfExpand()
                        i == axis2 && j == axis2 -> Cos(angle).selfExpand()
                        i == j -> JsclInteger.valueOf(1)
                        else -> JsclInteger.valueOf(0)
                    }
                }
            }
            return m
        }

        @JvmStatic
        fun newInstance(element: Array<Array<Generic?>>): Generic {
            return Matrix(element)
        }
    }
}
