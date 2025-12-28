package jscl.math.numeric

import jscl.math.NotDivisibleException
import jscl.math.NotDoubleException
import jscl.util.ArrayComparator

open class Matrix(private val m: Array<Array<Numeric>>) : Numeric() {

    private val rows: Int = m.size
    private val cols: Int = if (m.isNotEmpty()) m[0].size else 0

    fun elements(): Array<Array<Numeric>> {
        return m
    }

    fun add(matrix: Matrix): Matrix {
        val result = newInstance()
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result.m[i][j] = this.m[i][j].add(matrix.m[i][j])
            }
        }
        return result
    }

    override fun add(that: Numeric): Numeric {
        return when (that) {
            is Matrix -> add(that)
            else -> add(valueOf(that) as Matrix)
        }
    }

    fun subtract(matrix: Matrix): Matrix {
        val result = newInstance()
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result.m[i][j] = this.m[i][j].subtract(matrix.m[i][j])
            }
        }
        return result
    }

    override fun subtract(that: Numeric): Numeric {
        return when (that) {
            is Matrix -> subtract(that)
            else -> subtract(valueOf(that) as Matrix)
        }
    }

    fun multiply(matrix: Matrix): Matrix {
        if (cols != matrix.rows) throw ArithmeticException()
        val result = newInstance(Array(rows) { Array(matrix.cols) { Real.ZERO } })
        for (i in 0 until rows) {
            for (j in 0 until matrix.cols) {
                result.m[i][j] = Real.ZERO
                for (k in 0 until cols) {
                    result.m[i][j] = result.m[i][j].add(this.m[i][k].multiply(matrix.m[k][j]))
                }
            }
        }
        return result
    }

    override fun multiply(that: Numeric): Numeric {
        return when (that) {
            is Matrix -> multiply(that)
            is Vector -> {
                val v = that.newInstance(Array(rows) { Real.ZERO })
                if (cols != that.n) throw ArithmeticException()
                for (i in 0 until rows) {
                    v.element[i] = Real.ZERO
                    for (k in 0 until cols) {
                        v.element[i] = v.element[i].add(m[i][k].multiply(that.element[k]))
                    }
                }
                v
            }
            else -> {
                val result = newInstance()
                for (i in 0 until rows) {
                    for (j in 0 until cols) {
                        result.m[i][j] = this.m[i][j].multiply(that)
                    }
                }
                result
            }
        }
    }

    override fun divide(that: Numeric): Numeric {
        return when (that) {
            is Matrix -> multiply(that.inverse() as Matrix)
            is Vector -> throw ArithmeticException()
            else -> {
                val result = newInstance()
                for (i in 0 until rows) {
                    for (j in 0 until cols) {
                        result.m[i][j] = this.m[i][j].divide(that)
                    }
                }
                result
            }
        }
    }

    override fun negate(): Numeric {
        val result = newInstance()
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result.m[i][j] = this.m[i][j].negate()
            }
        }
        return result
    }

    override fun signum(): Int {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val c = m[i][j].signum()
                when {
                    c < 0 -> return -1
                    c > 0 -> return 1
                }
            }
        }

        return 0
    }

    override fun valueOf(numeric: Numeric): Numeric {
        if (numeric is Matrix || numeric is Vector) {
            throw ArithmeticException()
        } else {
            val result = identity(rows, cols).multiply(numeric) as Matrix
            return newInstance(result.m)
        }
    }

    fun vectors(): Array<Numeric> {
        val v = Array<Vector>(rows) { Vector(Array(cols) { Real.ZERO }) }
        for (i in 0 until rows) {
            v[i] = Vector(Array(cols) { Real.ZERO })
            for (j in 0 until cols) {
                v[i].element[j] = m[i][j]
            }
        }
        @Suppress("UNCHECKED_CAST")
        return v as Array<Numeric>
    }

    fun transpose(): Numeric {
        val result = newInstance(Array(cols) { Array(rows) { Real.ZERO } })
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result.m[j][i] = this.m[i][j]
            }
        }
        return result
    }

    fun trace(): Numeric {
        var s: Numeric = Real.ZERO
        for (i in 0 until rows) {
            s = s.add(m[i][i])
        }
        return s
    }

    override fun inverse(): Numeric {
        val result = newInstance()
        for (i in 0 until rows) {
            for (j in 0 until rows) {
                result.m[i][j] = inverseElement(i, j)
            }
        }
        return result.transpose().divide(determinant())
    }

    internal fun inverseElement(k: Int, l: Int): Numeric {
        val result = newInstance()

        for (i in 0 until rows) {
            for (j in 0 until rows) {
                result.m[i][j] = if (i == k) {
                    Real.valueOf(if (j == l) 1.0 else 0.0)
                } else {
                    this.m[i][j]
                }
            }
        }

        return result.determinant()
    }

    fun determinant(): Numeric {
        return when {
            rows > 1 -> {
                var a: Numeric = Real.ZERO
                for (i in 0 until rows) {
                    if (m[i][0].signum() != 0) {
                        val result = newInstance(Array(rows - 1) { Array(rows - 1) { Real.ZERO } })
                        for (j in 0 until rows - 1) {
                            for (k in 0 until rows - 1) {
                                result.m[j][k] = this.m[if (j < i) j else j + 1][k + 1]
                            }
                        }
                        a = if (i % 2 == 0) {
                            a.add(this.m[i][0].multiply(result.determinant()))
                        } else {
                            a.subtract(this.m[i][0].multiply(result.determinant()))
                        }
                    }
                }
                a
            }
            rows > 0 -> m[0][0]
            else -> Real.ZERO
        }
    }

    override fun ln(): Numeric {
        throw ArithmeticException()
    }

    override fun lg(): Numeric {
        throw ArithmeticException()
    }

    override fun exp(): Numeric {
        throw ArithmeticException()
    }

    override fun conjugate(): Numeric {
        val result = newInstance()
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result.m[i][j] = this.m[i][j].conjugate()
            }
        }
        return result
    }

    fun compareTo(matrix: Matrix): Int {
        @Suppress("UNCHECKED_CAST")
        return ArrayComparator.comparator.compare(vectors() as Array<Comparable<*>?>, matrix.vectors() as Array<Comparable<*>?>)
    }

    override fun compareTo(other: Numeric): Int {
        return when (other) {
            is Matrix -> compareTo(other)
            else -> compareTo(valueOf(other) as Matrix)
        }
    }

    override fun doubleValue(): Double {
        throw NotDoubleException.get()
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append("{")
        for (i in 0 until rows) {
            result.append("{")
            for (j in 0 until cols) {
                result.append(m[i][j])
                if (j < cols - 1) {
                    result.append(", ")
                }
            }
            result.append("}")
            if (i < rows - 1) {
                result.append(",\n")
            }
        }
        result.append("}")
        return result.toString()
    }

    protected open fun newInstance(): Matrix {
        return newInstance(Array(rows) { Array(cols) { Real.ZERO } })
    }

    protected open fun newInstance(element: Array<Array<Numeric>>): Matrix {
        return Matrix(element)
    }

    companion object {
        fun identity(dimension: Int): Matrix {
            return identity(dimension, dimension)
        }

        fun identity(n: Int, p: Int): Matrix {
            val result = Matrix(Array(n) { Array(p) { Real.ZERO } })
            for (i in 0 until n) {
                for (j in 0 until p) {
                    result.m[i][j] = if (i == j) {
                        Real.ONE
                    } else {
                        Real.ZERO
                    }
                }
            }
            return result
        }
    }
}
