package jscl.math.polynomial

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.Literal
import jscl.util.ArrayUtils
import java.util.TreeMap

internal class ListPolynomial(
    monomialFactory: Monomial,
    coefFactory: Generic
) : Polynomial(monomialFactory, coefFactory) {

    val content: MutableList<Any> = ArrayDeque()
    private var _degree: Int = 0
    var mutable: Boolean = true

    override fun size(): Int = content.size

    override fun degree(): Int = _degree

    override fun iterator(direction: Boolean, current: Monomial?): MutableIterator<Any> {
        return ContentIterator(direction, current)
    }

    fun indexOf(monomial: Monomial?, direction: Boolean): Int {
        if (monomial == null) return if (direction) content.size else 0
        @Suppress("UNCHECKED_CAST")
        val n = ArrayUtils.binarySearch(content as List<Term>, Term(monomial, null))
        return if (n < 0) -n - 1 else if (direction) n else n + 1
    }

    override fun subtract(that: Polynomial): Polynomial {
        if (that.signum() == 0) return this
        if (mutable) {
            val q = that as ListPolynomial
            val it1 = content.listIterator(content.size)
            val it2 = q.content.listIterator(q.content.size)
            var t1: Term? = if (it1.hasPrevious()) it1.previous() as Term else null
            var t2: Term? = if (it2.hasPrevious()) it2.previous() as Term else null
            while (t2 != null) {
                val c = when {
                    t1 == null -> 1
                    t2 == null -> -1
                    else -> -ordering.compare(t1.monomial(), t2.monomial())
                }
                if (c < 0) {
                    t1 = if (it1.hasPrevious()) it1.previous() as Term else null
                } else {
                    if (c > 0) {
                        if (t1 != null) it1.next()
                        it1.add(t2.negate())
                    } else {
                        val t = t1!!.subtract(t2)
                        if (t.signum() == 0) it1.remove()
                        else it1.set(t)
                    }
                    t1 = if (it1.hasPrevious()) it1.previous() as Term else null
                    t2 = if (it2.hasPrevious()) it2.previous() as Term else null
                }
            }
            _degree = degree(this)
            sugar = maxOf(sugar, q.sugar)
            normalized = false
            return this
        } else return copy().subtract(that)
    }

    override fun multiplyAndSubtract(generic: Generic, polynomial: Polynomial): Polynomial {
        if (generic.signum() == 0) return this
        if (generic.compareTo(JsclInteger.valueOf(1)) == 0) return subtract(polynomial)
        if (mutable) {
            val q = polynomial as ListPolynomial
            val it1 = content.listIterator(content.size)
            val it2 = q.content.listIterator(q.content.size)
            var t1: Term? = if (it1.hasPrevious()) it1.previous() as Term else null
            var t2: Term? = if (it2.hasPrevious()) (it2.previous() as Term).multiply(generic) else null
            while (t2 != null) {
                val c = when {
                    t1 == null -> 1
                    t2 == null -> -1
                    else -> -ordering.compare(t1.monomial(), t2.monomial())
                }
                if (c < 0) {
                    t1 = if (it1.hasPrevious()) it1.previous() as Term else null
                } else {
                    if (c > 0) {
                        if (t1 != null) it1.next()
                        it1.add(t2.negate())
                    } else {
                        val t = t1!!.subtract(t2)
                        if (t.signum() == 0) it1.remove()
                        else it1.set(t)
                    }
                    t1 = if (it1.hasPrevious()) it1.previous() as Term else null
                    t2 = if (it2.hasPrevious()) (it2.previous() as Term).multiply(generic) else null
                }
            }
            _degree = degree(this)
            sugar = maxOf(sugar, q.sugar)
            normalized = false
            return this
        } else return copy().multiplyAndSubtract(generic, polynomial)
    }

    override fun multiplyAndSubtract(monomial: Monomial, generic: Generic, polynomial: Polynomial): Polynomial {
        if (defined) throw UnsupportedOperationException()
        if (generic.signum() == 0) return this
        if (monomial.degree() == 0) return multiplyAndSubtract(generic, polynomial)
        if (mutable) {
            val q = polynomial as ListPolynomial
            val it1 = content.listIterator(content.size)
            val it2 = q.content.listIterator(q.content.size)
            var t1: Term? = if (it1.hasPrevious()) it1.previous() as Term else null
            var t2: Term? = if (it2.hasPrevious()) (it2.previous() as Term).multiply(monomial, generic) else null
            while (t2 != null) {
                val c = when {
                    t1 == null -> 1
                    t2 == null -> -1
                    else -> -ordering.compare(t1.monomial(), t2.monomial())
                }
                if (c < 0) {
                    t1 = if (it1.hasPrevious()) it1.previous() as Term else null
                } else {
                    if (c > 0) {
                        if (t1 != null) it1.next()
                        it1.add(t2.negate())
                    } else {
                        val t = t1!!.subtract(t2)
                        if (t.signum() == 0) it1.remove()
                        else it1.set(t)
                    }
                    t1 = if (it1.hasPrevious()) it1.previous() as Term else null
                    t2 = if (it2.hasPrevious()) (it2.previous() as Term).multiply(monomial, generic) else null
                }
            }
            _degree = degree(this)
            sugar = maxOf(sugar, q.sugar + monomial.degree())
            normalized = false
            return this
        } else return copy().multiplyAndSubtract(monomial, generic, polynomial)
    }

    override fun multiply(generic: Generic): Polynomial {
        if (generic.signum() == 0) return valueOf(JsclInteger.valueOf(0))
        if (generic.compareTo(JsclInteger.valueOf(1)) == 0) return this
        if (mutable) {
            val it = content.listIterator()
            while (it.hasNext()) it.set((it.next() as Term).multiply(generic))
            normalized = false
            return this
        } else return copy().multiply(generic)
    }

    override fun multiply(monomial: Monomial): Polynomial {
        if (defined) throw UnsupportedOperationException()
        if (monomial.degree() == 0) return this
        if (mutable) {
            val it = content.listIterator()
            while (it.hasNext()) it.set((it.next() as Term).multiply(monomial))
            _degree += monomial.degree()
            sugar += monomial.degree()
            return this
        } else return copy().multiply(monomial)
    }

    override fun divide(generic: Generic): Polynomial {
        if (generic.compareTo(JsclInteger.valueOf(1)) == 0) return this
        if (mutable) {
            val it = content.listIterator()
            while (it.hasNext()) it.set((it.next() as Term).divide(generic))
            normalized = false
            return this
        } else return copy().divide(generic)
    }

    override fun divide(monomial: Monomial): Polynomial {
        if (monomial.degree() == 0) return this
        if (mutable) {
            val it = content.listIterator()
            while (it.hasNext()) it.set((it.next() as Term).divide(monomial))
            _degree -= monomial.degree()
            sugar -= monomial.degree()
            return this
        } else return copy().divide(monomial)
    }

    override fun gcd(polynomial: Polynomial): Polynomial {
        throw UnsupportedOperationException()
    }

    override fun valueOf(polynomial: Polynomial): Polynomial {
        val p = newinstance(0)
        p.init(polynomial)
        return p
    }

    override fun valueOf(generic: Generic): Polynomial {
        val p = newinstance(0)
        p.init(generic)
        return p
    }

    override fun valueOf(monomial: Monomial): Polynomial {
        val p = newinstance(0)
        p.init(monomial)
        return p
    }

    override fun freeze(): Polynomial {
        mutable = false
        return this
    }

    override fun head(): Term? {
        val size = content.size
        return if (size > 0) content[size - 1] as Term else null
    }

    override fun tail(): Term? {
        val size = content.size
        return if (size > 0) content[0] as Term else null
    }

    fun init(polynomial: Polynomial) {
        val q = polynomial as ListPolynomial
        content.addAll(q.content)
        _degree = q._degree
        sugar = q.sugar
    }

    fun init(expression: Expression) {
        val map = TreeMap<Monomial, Generic>(ordering)
        val n = expression.size()
        for (i in 0 until n) {
            var l = expression.literal(i)
            val en = expression.coef(i)
            val m = monomial(l)
            l = l.divide(m.literalValue())
            val a2 = coefficient(if (l.degree() > 0) en.multiply(Expression.valueOf(l)) else en)
            val a1 = map[m]
            val a = if (a1 == null) a2 else a1.add(a2)
            if (a.signum() == 0) map.remove(m)
            else map[m] = a
        }
        var sugarVal = 0
        val it = map.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            val m = e.key
            val a = e.value
            content.add(Term(m, a))
            sugarVal = maxOf(sugarVal, m.degree())
        }
        _degree = degree(this)
        sugar = sugarVal
    }

    fun init(generic: Generic) {
        if (generic is Expression) {
            init(generic)
        } else {
            val a = coefficient(generic)
            if (a.signum() != 0) {
                content.add(Term(monomial(Literal.newInstance()), a))
            }
            _degree = 0
            sugar = 0
        }
    }

    fun init(monomial: Monomial) {
        content.add(Term(monomial, coefficient(JsclInteger.valueOf(1))))
        _degree = monomial.degree()
        sugar = monomial.degree()
    }

    fun newinstance(n: Int): ListPolynomial {
        return ListPolynomial(monomialFactory, coefFactory!!)
    }

    inner class ContentIterator(
        private val direction: Boolean,
        current: Monomial?
    ) : MutableListIterator<Any> {
        private val iterator: MutableListIterator<Any> = content.listIterator(indexOf(current, direction))

        override fun hasNext(): Boolean = if (direction) iterator.hasPrevious() else iterator.hasNext()

        override fun next(): Any = if (direction) iterator.previous() else iterator.next()

        override fun hasPrevious(): Boolean = if (direction) iterator.hasNext() else iterator.hasPrevious()

        override fun previous(): Any = if (direction) iterator.next() else iterator.previous()

        override fun nextIndex(): Int = if (direction) iterator.previousIndex() else iterator.nextIndex()

        override fun previousIndex(): Int = if (direction) iterator.nextIndex() else iterator.previousIndex()

        override fun remove() {
            iterator.remove()
        }

        override fun set(element: Any) {
            iterator.set(element)
        }

        override fun add(element: Any) {
            iterator.add(element)
        }
    }
}
