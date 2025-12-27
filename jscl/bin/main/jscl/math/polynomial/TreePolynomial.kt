package jscl.math.polynomial

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.Literal
import java.util.*

internal class TreePolynomial(monomialFactory: Monomial, coefFactory: Generic?) : Polynomial(monomialFactory, coefFactory) {
    internal val content: SortedMap<Monomial, Generic> = TreeMap(monomialFactory.ordering)
    internal var degree: Int = 0
    internal var mutable = true

    override fun size(): Int {
        return content.size
    }

    override fun iterator(direction: Boolean, current: Monomial?): Iterator<*> {
        return ContentIterator(direction, current)
    }

    internal fun term(entry: Map.Entry<Monomial, Generic>): Term {
        return Term(entry.key, entry.value)
    }

    internal fun term(monomial: Monomial): Term {
        return object : Term(monomial, null as Generic?) {
            override val coef: Generic
                get() = super.coef ?: coefficient(monomial)
        }
    }

    internal fun subContent(monomial: Monomial?, direction: Boolean): SortedMap<Monomial, Generic> {
        if (monomial == null) return content
        return if (direction) content.headMap(monomial) else content.tailMap(monomial)
    }

    override fun subtract(that: Polynomial): Polynomial {
        if (that.signum() == 0) return this
        if (mutable) {
            val q = that as TreePolynomial
            val it = q.content.entries.iterator()
            while (it.hasNext()) {
                val e = it.next()
                val m = e.key
                val a = e.value
                val s = coefficient(m).subtract(a)
                if (s.signum() == 0) content.remove(m)
                else content[m] = s
            }
            degree = Polynomial.degree(this)
            sugar = Math.max(sugar, q.sugar)
            normalized = false
            return this
        } else return copy().subtract(that)
    }

    override fun multiplyAndSubtract(generic: Generic, polynomial: Polynomial): Polynomial {
        if (generic.signum() == 0) return this
        if (generic.compareTo(JsclInteger.valueOf(1)) == 0) return subtract(polynomial)
        if (mutable) {
            val q = polynomial as TreePolynomial
            val it = q.content.entries.iterator()
            while (it.hasNext()) {
                val e = it.next()
                val m = e.key
                val a = e.value.multiply(generic)
                val s = coefficient(m).subtract(a)
                if (s.signum() == 0) content.remove(m)
                else content[m] = s
            }
            degree = Polynomial.degree(this)
            sugar = Math.max(sugar, q.sugar)
            normalized = false
            return this
        } else return copy().multiplyAndSubtract(generic, polynomial)
    }

    override fun multiplyAndSubtract(monomial: Monomial, generic: Generic, polynomial: Polynomial): Polynomial {
        if (generic.signum() == 0) return this
        if (monomial.degree() == 0) return multiplyAndSubtract(generic, polynomial)
        if (mutable) {
            val q = polynomial as TreePolynomial
            val it = q.content.entries.iterator()
            while (it.hasNext()) {
                val e = it.next()
                val m = e.key.multiply(monomial)
                val a = e.value.multiply(generic)
                val s = coefficient(m).subtract(a)
                if (s.signum() == 0) content.remove(m)
                else content[m] = s
            }
            degree = Polynomial.degree(this)
            sugar = Math.max(sugar, q.sugar + monomial.degree())
            normalized = false
            return this
        } else return copy().multiplyAndSubtract(monomial, generic, polynomial)
    }

    override fun multiply(generic: Generic): Polynomial {
        if (generic.signum() == 0) return valueOf(JsclInteger.valueOf(0))
        if (generic.compareTo(JsclInteger.valueOf(1)) == 0) return this
        if (mutable) {
            val it = content.entries.iterator()
            while (it.hasNext()) {
                val e = it.next()
                e.setValue(e.value.multiply(generic))
            }
            normalized = false
            return this
        } else return copy().multiply(generic)
    }

    override fun multiply(monomial: Monomial): Polynomial {
        if (defined) {
            val p = newinstance()
            val it = content.entries.iterator()
            while (it.hasNext()) {
                val e = it.next()
                val m = e.key.multiply(monomial)
                val a = e.value
                val s = p.coefficient(m).add(a)
                if (s.signum() == 0) p.content.remove(m)
                else p.content[m] = s
            }
            p.degree = Polynomial.degree(p)
            p.sugar = sugar + monomial.degree()
            return p
        } else {
            if (monomial.degree() == 0) return this
            val p = newinstance()
            val it = content.entries.iterator()
            while (it.hasNext()) {
                val e = it.next()
                p.content[e.key.multiply(monomial)] = e.value
            }
            p.degree = degree + monomial.degree()
            p.sugar = sugar + monomial.degree()
            return p
        }
    }

    override fun divide(generic: Generic): Polynomial {
        if (generic.compareTo(JsclInteger.valueOf(1)) == 0) return this
        if (mutable) {
            val it = content.entries.iterator()
            while (it.hasNext()) {
                val e = it.next()
                e.setValue(e.value.divide(generic))
            }
            normalized = false
            return this
        } else return copy().divide(generic)
    }

    override fun divide(monomial: Monomial): Polynomial {
        if (monomial.degree() == 0) return this
        val p = newinstance()
        val it = content.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            p.content[e.key.divide(monomial)] = e.value
        }
        p.degree = degree + monomial.degree()
        p.sugar = sugar + monomial.degree()
        return p
    }

    override fun gcd(polynomial: Polynomial): Polynomial {
        throw UnsupportedOperationException()
    }

    override fun degree(): Int {
        return degree
    }

    override fun valueOf(polynomial: Polynomial): Polynomial {
        val p = newinstance()
        p.init(polynomial)
        return p
    }

    override fun valueOf(generic: Generic): Polynomial {
        val p = newinstance()
        p.init(generic)
        return p
    }

    override fun valueOf(monomial: Monomial): Polynomial {
        val p = newinstance()
        p.init(monomial)
        return p
    }

    override fun freeze(): Polynomial {
        mutable = false
        return this
    }

    override fun head(): Term? {
        return if (content.size > 0) term(content.lastKey()) else null
    }

    override fun tail(): Term? {
        return if (content.size > 0) term(content.firstKey()) else null
    }

    override fun coefficient(monomial: Monomial): Generic {
        val a = content[monomial]
        return a ?: coefficient(JsclInteger.valueOf(0))
    }

    internal fun init(polynomial: Polynomial) {
        val q = polynomial as TreePolynomial
        content.putAll(q.content)
        degree = q.degree
        sugar = q.sugar
    }

    internal fun init(expression: Expression) {
        var sugar = 0
        val n = expression.size()
        for (i in 0 until n) {
            val l = expression.literal(i)
            val en = expression.coef(i)
            val m = monomial(l)
            val l2 = l.divide(m.literalValue())
            val a2 = coefficient(if (l2.degree() > 0) en.multiply(Expression.valueOf(l2)) else en)
            val a1 = coefficient(m)
            val a = a1.add(a2)
            if (a.signum() == 0) content.remove(m)
            else content[m] = a
            sugar = Math.max(sugar, m.degree())
        }
        degree = Polynomial.degree(this)
        this.sugar = sugar
    }

    internal fun init(generic: Generic) {
        if (generic is Expression) {
            init(generic)
        } else {
            val a = coefficient(generic)
            if (a.signum() != 0) content[monomial(Literal.newInstance())] = a
            degree = 0
            sugar = 0
        }
    }

    internal fun init(monomial: Monomial) {
        content[monomial] = coefficient(JsclInteger.valueOf(1))
        degree = monomial.degree()
        sugar = monomial.degree()
    }

    protected fun newinstance(): TreePolynomial {
        return TreePolynomial(monomialFactory, coefFactory)
    }

    internal inner class ContentIterator(private val direction: Boolean, current: Monomial?) : Iterator<Any> {
        private val iterator: Iterator<Map.Entry<Monomial, Generic>>?
        private var map: SortedMap<Monomial, Generic> = TreeMap()

        init {
            if (direction) {
                iterator = null
                map = subContent(current, true)
            } else {
                iterator = subContent(current, false).entries.iterator()
                if (current != null && content.containsKey(current)) iterator.next()
            }
        }

        override fun hasNext(): Boolean {
            return if (direction) map.size > 0 else iterator!!.hasNext()
        }

        override fun next(): Any {
            return if (direction) {
                val m = map.lastKey()
                map = content.headMap(m)
                term(m)
            } else {
                term(iterator!!.next())
            }
        }
    }
}
