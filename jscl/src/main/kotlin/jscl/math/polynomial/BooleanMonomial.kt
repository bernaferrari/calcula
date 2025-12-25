package jscl.math.polynomial

import jscl.math.NotDivisibleException
import jscl.math.Variable

internal open class BooleanMonomial : SmallMonomial {
    constructor(unknown: Array<Variable>, ordering: Ordering) : this(((unknown.size - 1) shr log2p) + 1, unknown, ordering)

    constructor(length: Int, unknown: Array<Variable>, ordering: Ordering) : super(length, unknown, ordering)

    override fun multiply(monomial: Monomial): Monomial {
        val m = newinstance()
        for (i in unknown.indices) {
            val q = i shr log2p
            val r = (i and pmask) shl log2n
            val a = (element[q] shr r) and nmask
            val b = (monomial.element[q] shr r) and nmask
            val c = a + b
            if (c > nmask) throw ArithmeticException()
            m.element[q] = m.element[q] or (c shl r)
            m.degree += c
        }
        return m
    }

    override fun multiple(monomial: Monomial, strict: Boolean): Boolean {
        var equal = true
        for (i in unknown.indices) {
            val q = i shr log2p
            val r = (i and pmask) shl log2n
            val a = (element[q] shr r) and nmask
            val b = (monomial.element[q] shr r) and nmask
            if (a < b) return false
            equal = equal and (a == b)
        }
        return !strict || !equal
    }

    override fun divide(monomial: Monomial): Monomial {
        val m = newinstance()
        for (i in unknown.indices) {
            val q = i shr log2p
            val r = (i and pmask) shl log2n
            val a = (element[q] shr r) and nmask
            val b = (monomial.element[q] shr r) and nmask
            val c = a - b
            if (c < 0) throw NotDivisibleException()
            m.element[q] = m.element[q] or (c shl r)
        }
        m.degree = degree - monomial.degree
        return m
    }

    override fun gcd(monomial: Monomial): Monomial {
        val m = newinstance()
        for (i in unknown.indices) {
            val q = i shr log2p
            val r = (i and pmask) shl log2n
            val a = (element[q] shr r) and nmask
            val b = (monomial.element[q] shr r) and nmask
            val c = Math.min(a, b)
            m.element[q] = m.element[q] or (c shl r)
            m.degree += c
        }
        return m
    }

    override fun scm(monomial: Monomial): Monomial {
        val m = newinstance()
        for (i in unknown.indices) {
            val q = i shr log2p
            val r = (i and pmask) shl log2n
            val a = (element[q] shr r) and nmask
            val b = (monomial.element[q] shr r) and nmask
            val c = Math.max(a, b)
            m.element[q] = m.element[q] or (c shl r)
            m.degree += c
        }
        return m
    }

    override fun element(n: Int): Int {
        val index = if (reverse()) unknown.size - 1 - n else n
        val q = index shr log2p
        val r = (index and pmask) shl log2n
        return (element[q] shr r) and nmask
    }

    override fun put(n: Int, integer: Int) {
        val index = if (reverse()) unknown.size - 1 - n else n
        val q = index shr log2p
        val r = (index and pmask) shl log2n
        val a = (element[q] shr r) and nmask
        val c = a + integer
        if (c > nmask) throw ArithmeticException()
        element[q] = element[q] or (c shl r)
        degree += c - a
    }

    override fun newinstance(): Monomial {
        return BooleanMonomial(element.size, unknown, ordering)
    }

    companion object {
        const val log2n = 1
        const val log2p = 5 - log2n
        const val nmask = (1 shl (1 shl log2n)) - 1
        const val pmask = (1 shl log2p) - 1
    }
}
