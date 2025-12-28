package jscl.math.polynomial

import jscl.math.NotDivisibleException
import jscl.math.Variable

internal open class SmallMonomial : Monomial {
    constructor(unknown: Array<Variable>, ordering: Ordering) : this(((unknown.size - 1) shr log2p) + 1, unknown, ordering)

    constructor(length: Int, unknown: Array<Variable>, ordering: Ordering) : super(unknown, ordering, length)

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
        return if (strict) !equal else true
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

    internal fun reverse(): Boolean {
        return ordering is DegreeReverseLexicographic
    }

    override fun newinstance(): Monomial {
        return SmallMonomial(element.size, unknown, ordering)
    }

    companion object {
        val lexicographic: Ordering = SmallLexicographic.ordering
        val totalDegreeLexicographic: Ordering = SmallTotalDegreeLexicographic.ordering
        val degreeReverseLexicographic: Ordering = SmallDegreeReverseLexicographic.ordering

        const val log2n = 3
        const val log2p = 5 - log2n
        const val nmask = (1 shl (1 shl log2n)) - 1
        const val pmask = (1 shl log2p) - 1
    }
}

internal open class SmallLexicographic internal constructor() : Ordering() {
    override fun compare(m1: Monomial, m2: Monomial): Int {
        val c1 = m1.element
        val c2 = m2.element
        val n = c1.size
        for (i in n - 1 downTo 0) {
            val l1 = c1[i].toLong() and 0xffffffffL
            val l2 = c2[i].toLong() and 0xffffffffL
            if (l1 < l2) return -1
            else if (l1 > l2) return 1
        }
        return 0
    }

    companion object {
        val ordering: Ordering = SmallLexicographic()
    }
}

internal class SmallTotalDegreeLexicographic private constructor() : SmallLexicographic(), DegreeOrdering {
    override fun compare(m1: Monomial, m2: Monomial): Int {
        return when {
            m1.degree < m2.degree -> -1
            m1.degree > m2.degree -> 1
            else -> super.compare(m1, m2)
        }
    }

    companion object {
        val ordering: Ordering = SmallTotalDegreeLexicographic()
    }
}

internal class SmallDegreeReverseLexicographic private constructor() : Ordering(), DegreeOrdering {
    override fun compare(m1: Monomial, m2: Monomial): Int {
        if (m1.degree < m2.degree) return -1
        else if (m1.degree > m2.degree) return 1
        else {
            val c1 = m1.element
            val c2 = m2.element
            val n = c1.size
            for (i in n - 1 downTo 0) {
                val l1 = c1[i].toLong() and 0xffffffffL
                val l2 = c2[i].toLong() and 0xffffffffL
                if (l1 > l2) return -1
                else if (l1 < l2) return 1
            }
            return 0
        }
    }

    companion object {
        val ordering: Ordering = SmallDegreeReverseLexicographic()
    }
}
