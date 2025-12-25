package jscl.math.numeric

import jscl.math.NotDivisibleException
import jscl.math.NotDoubleException
import jscl.util.ArrayComparator

open class Vector(internal val element: Array<Numeric>) : Numeric() {

    internal val n: Int = element.size

    fun elements(): Array<Numeric> {
        return element
    }

    fun add(vector: Vector): Vector {
        val v = newInstance()
        for (i in 0 until n) {
            v.element[i] = element[i].add(vector.element[i])
        }
        return v
    }

    override fun add(that: Numeric): Numeric {
        return when (that) {
            is Vector -> add(that)
            else -> add(valueOf(that) as Vector)
        }
    }

    fun subtract(vector: Vector): Vector {
        val v = newInstance()
        for (i in 0 until n) {
            v.element[i] = element[i].subtract(vector.element[i])
        }
        return v
    }

    override fun subtract(that: Numeric): Numeric {
        return when (that) {
            is Vector -> subtract(that)
            else -> subtract(valueOf(that) as Vector)
        }
    }

    override fun multiply(that: Numeric): Numeric {
        return when (that) {
            is Vector -> scalarProduct(that)
            is Matrix -> that.transpose().multiply(this)
            else -> {
                val v = newInstance()
                for (i in 0 until n) {
                    v.element[i] = element[i].multiply(that)
                }
                v
            }
        }
    }

    override fun divide(that: Numeric): Numeric {
        return when (that) {
            is Vector -> throw ArithmeticException()
            is Matrix -> multiply(that.inverse())
            else -> {
                val v = newInstance()
                for (i in 0 until n) {
                    v.element[i] = element[i].divide(that)
                }
                v
            }
        }
    }

    override fun negate(): Numeric {
        val v = newInstance()
        for (i in 0 until n) {
            v.element[i] = element[i].negate()
        }
        return v
    }

    override fun signum(): Int {
        for (i in 0 until n) {
            val c = element[i].signum()
            when {
                c < 0 -> return -1
                c > 0 -> return 1
            }
        }
        return 0
    }

    override fun valueOf(numeric: Numeric): Numeric {
        if (numeric is Vector || numeric is Matrix) {
            throw ArithmeticException()
        } else {
            val v = unity(n).multiply(numeric) as Vector
            return newInstance(v.element)
        }
    }

    fun magnitude2(): Numeric {
        return scalarProduct(this)
    }

    fun scalarProduct(vector: Vector): Numeric {
        var a: Numeric = Real.ZERO
        for (i in 0 until n) {
            a = a.add(element[i].multiply(vector.element[i]))
        }
        return a
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
        val v = newInstance()
        for (i in 0 until n) {
            v.element[i] = element[i].conjugate()
        }
        return v
    }

    fun compareTo(vector: Vector): Int {
        @Suppress("UNCHECKED_CAST")
        return ArrayComparator.comparator.compare(element as Array<Comparable<*>?>, vector.element as Array<Comparable<*>?>)
    }

    override fun compareTo(other: Numeric): Int {
        return when (other) {
            is Vector -> compareTo(other)
            else -> compareTo(valueOf(other) as Vector)
        }
    }

    override fun doubleValue(): Double {
        throw NotDoubleException.get()
    }

    override fun toString(): String {
        val result = StringBuilder()

        result.append("[")

        for (i in 0 until n) {
            result.append(element[i])
            if (i < n - 1) {
                result.append(", ")
            }
        }

        result.append("]")

        return result.toString()
    }

    internal open fun newInstance(): Vector {
        return newInstance(Array(n) { Real.ZERO })
    }

    internal open fun newInstance(element: Array<Numeric>): Vector {
        return Vector(element)
    }

    companion object {
        @JvmStatic
        fun unity(dimension: Int): Vector {
            val v = Vector(Array(dimension) { Real.ZERO })
            for (i in 0 until v.n) {
                v.element[i] = if (i == 0) Real.ONE else Real.ZERO
            }
            return v
        }
    }
}
