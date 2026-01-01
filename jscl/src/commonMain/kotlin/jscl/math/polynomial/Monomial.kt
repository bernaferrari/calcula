package jscl.math.polynomial

import jscl.math.Literal
import jscl.math.NotDivisibleException
import jscl.math.Variable
import jscl.math.function.Fraction
import jscl.math.function.Pow
import jscl.mathml.MathML

open class Monomial internal constructor(
    val unknown: Array<Variable>,
    val ordering: Ordering,
    length: Int
) : Comparable<Monomial> {

    internal val element: IntArray = IntArray(length)
    internal var degree: Int = 0

    constructor(unknown: Array<Variable>, ordering: Ordering) : this(unknown, ordering, unknown.size)

    open fun multiply(monomial: Monomial): Monomial {
        val m = newinstance()
        for (i in unknown.indices) {
            m.element[i] = element[i] + monomial.element[i]
        }
        m.degree = degree + monomial.degree
        return m
    }

    fun multiple(monomial: Monomial): Boolean {
        return multiple(monomial, false)
    }

    open fun multiple(monomial: Monomial, strict: Boolean): Boolean {
        var equal = true
        for (i in unknown.indices) {
            if (element[i] < monomial.element[i]) return false
            equal = equal and (element[i] == monomial.element[i])
        }
        return if (strict) !equal else true
    }

    open fun divide(monomial: Monomial): Monomial {
        val m = newinstance()
        for (i in unknown.indices) {
            val n = element[i] - monomial.element[i]
            if (n < 0) throw NotDivisibleException()
            m.element[i] = n
        }
        m.degree = degree - monomial.degree
        return m
    }

    open fun gcd(monomial: Monomial): Monomial {
        val m = newinstance()
        for (i in unknown.indices) {
            val n = kotlin.math.min(element[i], monomial.element[i])
            m.element[i] = n
            m.degree += n
        }
        return m
    }

    open fun scm(monomial: Monomial): Monomial {
        val m = newinstance()
        for (i in unknown.indices) {
            val n = kotlin.math.max(element[i], monomial.element[i])
            m.element[i] = n
            m.degree += n
        }
        return m
    }

    fun degree(): Int {
        return degree
    }

    fun valueof(monomial: Monomial): Monomial {
        val m = newinstance()
        monomial.element.copyInto(m.element, 0, 0, 0 + m.element.size)
        m.degree = monomial.degree
        return m
    }

    fun valueof(literal: Literal): Monomial {
        val m = newinstance()
        m.init(literal)
        return m
    }

    fun literalValue(): Literal {
        return Literal.valueOf(this)
    }

    open fun element(n: Int): Int {
        return element[n]
    }

    fun iterator(): Iterator<*> {
        return iterator(newinstance())
    }

    fun iterator(beginning: Monomial): Iterator<*> {
        return MonomialIterator(beginning, this)
    }

    fun divisor(): Iterator<*> {
        return divisor(newinstance())
    }

    fun divisor(beginning: Monomial): Iterator<*> {
        return MonomialDivisor(beginning, this)
    }

    override fun compareTo(other: Monomial): Int {
        return ordering.compare(this, other)
    }

    internal fun init(literal: Literal) {
        val s = literal.size()
        for (i in 0 until s) {
            val v = literal.getVariable(i)
            val c = literal.getPower(i)
            val n = variable(v, unknown)
            if (n < unknown.size) put(n, c)
        }
    }

    internal open fun put(n: Int, integer: Int) {
        element[n] += integer
        degree += integer
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        if (degree == 0) buffer.append("1")
        var b = false
        for (i in unknown.indices) {
            val c = element(i)
            if (c > 0) {
                if (b) buffer.append("*")
                else b = true
                val v = unknown[i]
                if (c == 1) buffer.append(v)
                else {
                    if (v is Fraction || v is Pow) {
                        buffer.append("(").append(v).append(")")
                    } else buffer.append(v)
                    buffer.append("^").append(c)
                }
            }
        }
        return buffer.toString()
    }

    fun toMathML(element: MathML, data: Any?) {
        if (degree == 0) {
            val e1 = element.element("mn")
            e1.appendChild(element.text("1"))
            element.appendChild(e1)
        }
        for (i in unknown.indices) {
            val c = element(i)
            if (c > 0) {
                unknown[i].toMathML(element, c)
            }
        }
    }

    protected open fun newinstance(): Monomial {
        return Monomial(unknown, ordering, element.size)
    }

    companion object {
        val lexicographic: Ordering = Lexicographic.ordering
        val totalDegreeLexicographic: Ordering = TotalDegreeLexicographic.ordering
        val degreeReverseLexicographic: Ordering = DegreeReverseLexicographic.ordering
        val iteratorOrdering: Ordering = totalDegreeLexicographic

        fun kthElimination(k: Int): Ordering {
            return KthElimination(k, 1)
        }

        internal fun factory(unknown: Array<Variable>): Monomial {
            return factory(unknown, lexicographic)
        }

        internal fun factory(unknown: Array<Variable>, ordering: Ordering): Monomial {
            return factory(unknown, ordering, 0)
        }

        internal fun factory(unknown: Array<Variable>, ordering: Ordering, power_size: Int): Monomial {
            return when (power_size) {
                Basis.POWER_8 -> SmallMonomial(unknown, small(ordering))
                Basis.POWER_2 -> BooleanMonomial(unknown, small(ordering))
                Basis.POWER_2_DEFINED -> DefinedBooleanMonomial(unknown, small(ordering))
                else -> Monomial(unknown, ordering)
            }
        }

        internal fun small(ordering: Ordering): Ordering {
            return when (ordering) {
                lexicographic -> SmallMonomial.lexicographic
                totalDegreeLexicographic -> SmallMonomial.totalDegreeLexicographic
                degreeReverseLexicographic -> SmallMonomial.degreeReverseLexicographic
                else -> throw UnsupportedOperationException()
            }
        }

        internal fun variable(v: Variable, unknown: Array<Variable>): Int {
            var i = 0
            while (i < unknown.size) {
                if (unknown[i] == v) break
                i++
            }
            return i
        }
    }
}

internal open class MonomialIterator(beginning: Monomial, private val monomial: Monomial) : Iterator<Any> {
    protected val current: Monomial = monomial.valueof(beginning)
    private var carry: Boolean = false

    init {
        if (ordering.compare(current, monomial) > 0) carry = true
    }

    override fun hasNext(): Boolean {
        return !carry
    }

    override fun next(): Any {
        val m = monomial.valueof(current)
        if (ordering.compare(current, monomial) < 0) increment()
        else carry = true
        return m
    }

    internal open fun increment() {
        var s = 0
        var n = 0
        while (n < current.element.size && current.element[n] == 0) n++
        if (n < current.element.size) {
            s = current.element[n]
            current.element[n] = 0
            n++
        }
        if (n < current.element.size) {
            current.element[n]++
            fill(s - 1)
        } else {
            current.degree++
            fill(s + 1)
        }
    }

    private fun fill(s: Int) {
        current.element[0] = s
    }

    companion object {
        val ordering: Ordering = Monomial.iteratorOrdering
    }
}

internal class MonomialDivisor(beginning: Monomial, monomial: Monomial) : MonomialIterator(beginning, monomial) {
    private val monomial: Monomial

    init {
        this.monomial = monomial
        if (hasNext()) seek()
    }

    internal fun seek() {
        var n = current.element.size
        while (n > 0) {
            n--
            if (current.element[n] > monomial.element[n]) break
        }
        val p = n
        while (n > 0) {
            n--
            current.element[p] += current.element[n]
            current.element[n] = 0
        }
        if (p < current.element.size && current.element[p] > monomial.element[p]) increment()
    }

    override fun increment() {
        var s = 0
        var n = 0
        while (n < current.element.size && current.element[n] == 0) n++
        if (n < current.element.size) {
            s = current.element[n]
            current.element[n] = 0
            n++
        }
        while (n < current.element.size && current.element[n] == monomial.element[n]) {
            s += current.element[n]
            current.element[n] = 0
            n++
        }
        if (n < current.element.size) {
            current.element[n]++
            fill(s - 1)
        } else {
            current.degree++
            fill(s + 1)
        }
    }

    private fun fill(s: Int) {
        var remaining = s
        for (i in current.element.indices) {
            val d = kotlin.math.min(monomial.element[i] - current.element[i], remaining)
            current.element[i] += d
            remaining -= d
        }
    }
}

internal open class Lexicographic internal constructor() : Ordering() {
    override fun compare(m1: Monomial, m2: Monomial): Int {
        val c1 = m1.element
        val c2 = m2.element
        val n = c1.size
        for (i in n - 1 downTo 0) {
            if (c1[i] < c2[i]) return -1
            else if (c1[i] > c2[i]) return 1
        }
        return 0
    }

    companion object {
        val ordering: Ordering = Lexicographic()
    }
}

internal class TotalDegreeLexicographic internal constructor() : Lexicographic(), DegreeOrdering {
    override fun compare(m1: Monomial, m2: Monomial): Int {
        return when {
            m1.degree < m2.degree -> -1
            m1.degree > m2.degree -> 1
            else -> super.compare(m1, m2)
        }
    }

    companion object {
        val ordering: Ordering = TotalDegreeLexicographic()
    }
}

internal class DegreeReverseLexicographic private constructor() : Ordering(), DegreeOrdering {
    override fun compare(m1: Monomial, m2: Monomial): Int {
        if (m1.degree < m2.degree) return -1
        else if (m1.degree > m2.degree) return 1
        else {
            val c1 = m1.element
            val c2 = m2.element
            val n = c1.size
            for (i in 0 until n) {
                if (c1[i] > c2[i]) return -1
                else if (c1[i] < c2[i]) return 1
            }
            return 0
        }
    }

    companion object {
        val ordering: Ordering = DegreeReverseLexicographic()
    }
}

internal class KthElimination(private val k: Int, direction: Int) : Ordering() {
    override fun compare(m1: Monomial, m2: Monomial): Int {
        val c1 = m1.element
        val c2 = m2.element
        val n = c1.size
        val k = n - this.k
        for (i in n - 1 downTo k) {
            if (c1[i] < c2[i]) return -1
            else if (c1[i] > c2[i]) return 1
        }
        return DegreeReverseLexicographic.ordering.compare(m1, m2)
    }
}
