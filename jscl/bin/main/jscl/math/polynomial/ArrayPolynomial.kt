package jscl.math.polynomial

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.Literal
import java.util.Arrays
import java.util.TreeMap

internal class ArrayPolynomial : Polynomial {
    var content: Array<Term?>
    protected var _size: Int
    protected var _degree: Int

    constructor(monomialFactory: Monomial, coefFactory: Generic) : super(monomialFactory, coefFactory) {
        content = emptyArray()
        _size = 0
        _degree = 0
    }

    override fun size(): Int = _size

    override fun degree(): Int = _degree

    constructor(size: Int, monomialFactory: Monomial, coefFactory: Generic) : this(monomialFactory, coefFactory) {
        init(size)
    }

    fun init(size: Int) {
        content = arrayOfNulls(size)
        this._size = size
    }

    fun resize(size: Int) {
        val length = content.size
        if (size < length) {
            val newContent = arrayOfNulls<Term>(size)
            System.arraycopy(content, length - size, newContent, 0, size)
            content = newContent
            this._size = size
        }
    }

    override fun iterator(direction: Boolean, current: Monomial?): MutableIterator<Any> {
        return ContentIterator(direction, current)
    }

    fun indexOf(monomial: Monomial?, direction: Boolean): Int {
        if (monomial == null) return if (direction) _size else 0
        val n = Arrays.binarySearch(content, Term(monomial, coefFactory!!))
        return if (n < 0) -n - 1 else if (direction) n else n + 1
    }

    override fun subtract(that: Polynomial): Polynomial {
        if (that.signum() == 0) return this
        val q = that as ArrayPolynomial
        val p = newInstance(_size + q._size)
        var i = p._size
        var i1 = _size
        var i2 = q._size
        var t1: Term? = if (i1 > 0) content[--i1] else null
        var t2: Term? = if (i2 > 0) q.content[--i2] else null
        while (t1 != null || t2 != null) {
            val c = when {
                t1 == null -> 1
                t2 == null -> -1
                else -> -(ordering as Comparator<Monomial>).compare(t1.monomial(), t2.monomial())
            }
            if (c < 0) {
                p.content[--i] = t1
                t1 = if (i1 > 0) content[--i1] else null
            } else if (c > 0) {
                p.content[--i] = t2!!.negate()
                t2 = if (i2 > 0) q.content[--i2] else null
            } else {
                val t = t1!!.subtract(t2!!)
                if (t.signum() != 0) p.content[--i] = t
                t1 = if (i1 > 0) content[--i1] else null
                t2 = if (i2 > 0) q.content[--i2] else null
            }
        }
        p.resize(p._size - i)
        p._degree = degree(p)
        p.sugar = maxOf(sugar, q.sugar)
        return p
    }

    override fun multiplyAndSubtract(generic: Generic, polynomial: Polynomial): Polynomial {
        if (generic.signum() == 0) return this
        if (generic.compareTo(JsclInteger.valueOf(1)) == 0) return subtract(polynomial)
        val q = polynomial as ArrayPolynomial
        val p = newInstance(_size + q._size)
        var i = p._size
        var i1 = _size
        var i2 = q._size
        var t1: Term? = if (i1 > 0) content[--i1] else null
        var t2: Term? = if (i2 > 0) q.content[--i2]?.multiply(generic) else null
        while (t1 != null || t2 != null) {
            val c = when {
                t1 == null -> 1
                t2 == null -> -1
                else -> -(ordering as Comparator<Monomial>).compare(t1.monomial(), t2.monomial())
            }
            if (c < 0) {
                p.content[--i] = t1
                t1 = if (i1 > 0) content[--i1] else null
            } else if (c > 0) {
                p.content[--i] = t2!!.negate()
                t2 = if (i2 > 0) q.content[--i2]?.multiply(generic) else null
            } else {
                val t = t1!!.subtract(t2!!)
                if (t.signum() != 0) p.content[--i] = t
                t1 = if (i1 > 0) content[--i1] else null
                t2 = if (i2 > 0) q.content[--i2]?.multiply(generic) else null
            }
        }
        p.resize(p._size - i)
        p._degree = degree(p)
        p.sugar = maxOf(sugar, q.sugar)
        return p
    }

    override fun multiplyAndSubtract(monomial: Monomial, generic: Generic, polynomial: Polynomial): Polynomial {
        if (defined) throw UnsupportedOperationException()
        if (generic.signum() == 0) return this
        if (monomial.degree() == 0) return multiplyAndSubtract(generic, polynomial)
        val q = polynomial as ArrayPolynomial
        val p = newInstance(_size + q._size)
        var i = p._size
        var i1 = _size
        var i2 = q._size
        var t1: Term? = if (i1 > 0) content[--i1] else null
        var t2: Term? = if (i2 > 0) q.content[--i2]?.multiply(monomial, generic) else null
        while (t1 != null || t2 != null) {
            val c = when {
                t1 == null -> 1
                t2 == null -> -1
                else -> -(ordering as Comparator<Monomial>).compare(t1.monomial(), t2.monomial())
            }
            if (c < 0) {
                p.content[--i] = t1
                t1 = if (i1 > 0) content[--i1] else null
            } else if (c > 0) {
                p.content[--i] = t2!!.negate()
                t2 = if (i2 > 0) q.content[--i2]?.multiply(monomial, generic) else null
            } else {
                val t = t1!!.subtract(t2!!)
                if (t.signum() != 0) p.content[--i] = t
                t1 = if (i1 > 0) content[--i1] else null
                t2 = if (i2 > 0) q.content[--i2]?.multiply(monomial, generic) else null
            }
        }
        p.resize(p._size - i)
        p._degree = degree(p)
        p.sugar = maxOf(sugar, q.sugar + monomial.degree())
        return p
    }

    override fun multiply(that: Polynomial): Polynomial {
        var p = valueOf(JsclInteger.valueOf(0))
        for (i in 0 until _size) {
            val t = content[i]!!
            p = p.multiplyAndSubtract(t.monomial(), t.coef().negate(), that)
        }
        return p
    }

    override fun multiply(generic: Generic): Polynomial {
        if (generic.signum() == 0) return valueOf(JsclInteger.valueOf(0))
        if (generic.compareTo(JsclInteger.valueOf(1)) == 0) return this
        val p = newInstance(_size)
        for (i in 0 until _size) p.content[i] = content[i]!!.multiply(generic)
        p._degree = _degree
        p.sugar = sugar
        return p
    }

    override fun multiply(monomial: Monomial): Polynomial {
        if (defined) throw UnsupportedOperationException()
        if (monomial.degree() == 0) return this
        val p = newInstance(_size)
        for (i in 0 until _size) p.content[i] = content[i]!!.multiply(monomial)
        p._degree = _degree + monomial.degree()
        p.sugar = sugar + monomial.degree()
        return p
    }

    override fun divide(generic: Generic): Polynomial {
        if (generic.compareTo(JsclInteger.valueOf(1)) == 0) return this
        val p = newInstance(_size)
        for (i in 0 until _size) p.content[i] = content[i]!!.divide(generic)
        p._degree = _degree
        p.sugar = sugar
        return p
    }

    override fun divide(monomial: Monomial): Polynomial {
        if (monomial.degree() == 0) return this
        val p = newInstance(_size)
        for (i in 0 until _size) p.content[i] = content[i]!!.divide(monomial)
        p._degree = _degree - monomial.degree()
        p.sugar = sugar - monomial.degree()
        return p
    }

    override fun gcd(polynomial: Polynomial): Polynomial {
        throw UnsupportedOperationException()
    }

    override fun gcd(): Generic {
        if (field) return coefficient(tail())
        var a = coefficient(JsclInteger.valueOf(0))
        for (i in _size - 1 downTo 0) a = a.gcd(content[i]!!.coef())
        return if (a.signum() == signum()) a else a.negate()
    }

    override fun monomialGcd(): Monomial {
        var m = monomial(tail())
        for (i in 0 until _size) m = m.gcd(content[i]!!.monomial())
        return m
    }

    override fun valueOf(polynomial: Polynomial): Polynomial {
        val p = newInstance(0)
        p.init(polynomial)
        return p
    }

    override fun valueOf(generic: Generic): Polynomial {
        val p = newInstance(0)
        p.init(generic)
        return p
    }

    override fun valueOf(monomial: Monomial): Polynomial {
        val p = newInstance(0)
        p.init(monomial)
        return p
    }

    override fun freeze(): Polynomial = this

    override fun head(): Term? = if (_size > 0) content[_size - 1] else null

    override fun tail(): Term? = if (_size > 0) content[0] else null

    fun init(polynomial: Polynomial) {
        val q = polynomial as ArrayPolynomial
        init(q._size)
        System.arraycopy(q.content, 0, content, 0, _size)
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
        init(map.size)
        var sugarVal = 0
        val it = map.entries.iterator()
        for (i in 0 until _size) {
            val e = it.next()
            val m = e.key
            val a = e.value
            content[i] = Term(m, a)
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
                init(1)
                content[0] = Term(monomial(Literal.newInstance()), a)
            } else init(0)
            _degree = 0
            sugar = 0
        }
    }

    fun init(monomial: Monomial) {
        init(1)
        content[0] = Term(monomial, coefficient(JsclInteger.valueOf(1)))
        _degree = monomial.degree()
        sugar = monomial.degree()
    }

    fun newInstance(n: Int): ArrayPolynomial {
        return ArrayPolynomial(n, monomialFactory, coefFactory!!)
    }

    inner class ContentIterator(
        private val direction: Boolean,
        current: Monomial?
    ) : MutableIterator<Any> {
        private var index: Int = indexOf(current, direction)

        override fun hasNext(): Boolean = if (direction) index > 0 else index < _size

        override fun next(): Any = if (direction) content[--index]!! else content[index++]!!

        override fun remove() {
            throw UnsupportedOperationException()
        }
    }
}
