package jscl.math.polynomial

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.Literal
import java.util.Arrays
import java.util.TreeMap

internal open class ArrayPolynomialGeneric : Polynomial {
    var coef: Array<Generic?>
    var monomial: Array<Monomial?>
    protected var _size: Int
    protected var _degree: Int

    constructor(monomialFactory: Monomial, coefFactory: Generic?) : super(monomialFactory, coefFactory) {
        coef = emptyArray()
        monomial = emptyArray()
        _size = 0
        _degree = 0
    }

    override fun size(): Int = _size

    override fun degree(): Int = _degree

    constructor(size: Int, monomialFactory: Monomial, coefFactory: Generic?) : this(monomialFactory, coefFactory) {
        init(size)
    }

    open fun init(size: Int) {
        monomial = arrayOfNulls(size)
        coef = arrayOfNulls(size)
        this._size = size
    }

    open fun resize(size: Int) {
        val length = monomial.size
        if (size < length) {
            val newMonomial = arrayOfNulls<Monomial>(size)
            val newCoef = arrayOfNulls<Generic>(size)
            System.arraycopy(monomial, length - size, newMonomial, 0, size)
            System.arraycopy(coef, length - size, newCoef, 0, size)
            monomial = newMonomial
            coef = newCoef
            this._size = size
        }
    }

    override fun iterator(direction: Boolean, current: Monomial?): MutableIterator<Any> {
        return ContentIterator(direction, current)
    }

    fun term(index: Int): Term {
        return object : Term(monomial[index]!!, JsclInteger.valueOf(0)) {
            override val coef: Generic
                get() = getCoef(index)
        }
    }

    fun indexOf(monomial: Monomial?, direction: Boolean): Int {
        if (monomial == null) return if (direction) _size else 0
        val n = Arrays.binarySearch(this.monomial, monomial, ordering)
        return if (n < 0) -n - 1 else if (direction) n else n + 1
    }

    override fun subtract(that: Polynomial): Polynomial {
        if (that.signum() == 0) return this
        val q = that as ArrayPolynomialGeneric
        val p = newInstance(_size + q._size)
        var i = p._size
        var i1 = _size
        var i2 = q._size
        var m1: Monomial? = if (i1 > 0) monomial[--i1] else null
        var m2: Monomial? = if (i2 > 0) q.monomial[--i2] else null
        while (m1 != null || m2 != null) {
            val c = when {
                m1 == null -> 1
                m2 == null -> -1
                else -> -(ordering as Comparator<Monomial>).compare(m1, m2)
            }
            if (c < 0) {
                val a = getCoef(i1)
                --i
                p.monomial[i] = m1
                p.setCoef(i, a)
                m1 = if (i1 > 0) monomial[--i1] else null
            } else if (c > 0) {
                val a = q.getCoef(i2).negate()
                --i
                p.monomial[i] = m2
                p.setCoef(i, a)
                m2 = if (i2 > 0) q.monomial[--i2] else null
            } else {
                val a = getCoef(i1).subtract(q.getCoef(i2))
                if (a.signum() != 0) {
                    --i
                    p.monomial[i] = m1
                    p.setCoef(i, a)
                }
                m1 = if (i1 > 0) monomial[--i1] else null
                m2 = if (i2 > 0) q.monomial[--i2] else null
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
        val q = polynomial as ArrayPolynomialGeneric
        val p = newInstance(_size + q._size)
        var i = p._size
        var i1 = _size
        var i2 = q._size
        var m1: Monomial? = if (i1 > 0) monomial[--i1] else null
        var m2: Monomial? = if (i2 > 0) q.monomial[--i2] else null
        while (m1 != null || m2 != null) {
            val c = when {
                m1 == null -> 1
                m2 == null -> -1
                else -> -(ordering as Comparator<Monomial>).compare(m1, m2)
            }
            if (c < 0) {
                val a = getCoef(i1)
                --i
                p.monomial[i] = m1
                p.setCoef(i, a)
                m1 = if (i1 > 0) monomial[--i1] else null
            } else if (c > 0) {
                val a = q.getCoef(i2).multiply(generic).negate()
                --i
                p.monomial[i] = m2
                p.setCoef(i, a)
                m2 = if (i2 > 0) q.monomial[--i2] else null
            } else {
                val a = getCoef(i1).subtract(q.getCoef(i2).multiply(generic))
                if (a.signum() != 0) {
                    --i
                    p.monomial[i] = m1
                    p.setCoef(i, a)
                }
                m1 = if (i1 > 0) monomial[--i1] else null
                m2 = if (i2 > 0) q.monomial[--i2] else null
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
        val q = polynomial as ArrayPolynomialGeneric
        val p = newInstance(_size + q._size)
        var i = p._size
        var i1 = _size
        var i2 = q._size
        var m1: Monomial? = if (i1 > 0) this.monomial[--i1] else null
        var m2: Monomial? = if (i2 > 0) q.monomial[--i2]?.multiply(monomial) else null
        while (m1 != null || m2 != null) {
            val c = when {
                m1 == null -> 1
                m2 == null -> -1
                else -> -(ordering as Comparator<Monomial>).compare(m1, m2)
            }
            if (c < 0) {
                val a = getCoef(i1)
                --i
                p.monomial[i] = m1
                p.setCoef(i, a)
                m1 = if (i1 > 0) this.monomial[--i1] else null
            } else if (c > 0) {
                val a = q.getCoef(i2).multiply(generic).negate()
                --i
                p.monomial[i] = m2
                p.setCoef(i, a)
                m2 = if (i2 > 0) q.monomial[--i2]?.multiply(monomial) else null
            } else {
                val a = getCoef(i1).subtract(q.getCoef(i2).multiply(generic))
                if (a.signum() != 0) {
                    --i
                    p.monomial[i] = m1
                    p.setCoef(i, a)
                }
                m1 = if (i1 > 0) this.monomial[--i1] else null
                m2 = if (i2 > 0) q.monomial[--i2]?.multiply(monomial) else null
            }
        }
        p.resize(p._size - i)
        p._degree = degree(p)
        p.sugar = maxOf(sugar, q.sugar + monomial.degree())
        return p
    }

    override fun multiply(that: Polynomial): Polynomial {
        var p: Polynomial = newInstance(0)
        for (i in 0 until _size) p = p.multiplyAndSubtract(monomial[i]!!, getCoef(i).negate(), that)
        return p
    }

    override fun multiply(generic: Generic): Polynomial {
        if (generic.signum() == 0) return valueOf(JsclInteger.valueOf(0))
        if (generic.compareTo(JsclInteger.valueOf(1)) == 0) return this
        val p = newInstance(_size)
        System.arraycopy(monomial, 0, p.monomial, 0, _size)
        for (i in 0 until _size) p.setCoef(i, getCoef(i).multiply(generic))
        p._degree = _degree
        p.sugar = sugar
        return p
    }

    override fun multiply(monomial: Monomial): Polynomial {
        if (defined) throw UnsupportedOperationException()
        if (monomial.degree() == 0) return this
        val p = newInstance(_size)
        for (i in 0 until _size) {
            p.monomial[i] = this.monomial[i]!!.multiply(monomial)
            p.setCoef(i, getCoef(i))
        }
        p._degree = _degree + monomial.degree()
        p.sugar = sugar + monomial.degree()
        return p
    }

    override fun divide(generic: Generic): Polynomial {
        if (generic.compareTo(JsclInteger.valueOf(1)) == 0) return this
        val p = newInstance(_size)
        System.arraycopy(monomial, 0, p.monomial, 0, _size)
        for (i in 0 until _size) p.setCoef(i, getCoef(i).divide(generic))
        p._degree = _degree
        p.sugar = sugar
        return p
    }

    override fun divide(monomial: Monomial): Polynomial {
        if (monomial.degree() == 0) return this
        val p = newInstance(_size)
        for (i in 0 until _size) {
            p.monomial[i] = this.monomial[i]!!.divide(monomial)
            p.setCoef(i, getCoef(i))
        }
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
        for (i in _size - 1 downTo 0) a = a.gcd(getCoef(i))
        return if (a.signum() == signum()) a else a.negate()
    }

    override fun monomialGcd(): Monomial {
        var m = monomial(tail())
        for (i in 0 until _size) m = m.gcd(monomial[i]!!)
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

    override fun head(): Term? = if (_size > 0) term(_size - 1) else null

    override fun tail(): Term? = if (_size > 0) term(0) else null

    protected open fun getCoef(n: Int): Generic = coef[n]!!

    protected open fun setCoef(n: Int, generic: Generic) {
        coef[n] = generic
    }

    override fun genericValue(): Generic {
        var s: Generic = JsclInteger.valueOf(0)
        for (i in 0 until _size) {
            val m = monomial[i]!!
            val a = getCoef(i).expressionValue()
            s = s.add(if (m.degree() > 0) a.multiply(Expression.valueOf(m.literalValue())) else a)
        }
        return s
    }

    override fun elements(): Array<Generic> {
        val a = arrayOfNulls<Generic>(_size)
        for (i in 0 until _size) a[i] = getCoef(i)
        @Suppress("UNCHECKED_CAST")
        return a as Array<Generic>
    }

    override fun compareTo(other: Polynomial): Int {
        val q = other as ArrayPolynomialGeneric
        var i1 = _size
        var i2 = q._size
        var m1: Monomial? = if (i1 == 0) null else monomial[--i1]
        var m2: Monomial? = if (i2 == 0) null else q.monomial[--i2]
        while (m1 != null || m2 != null) {
            val c = when {
                m1 == null -> -1
                m2 == null -> 1
                else -> ordering.compare(m1, m2)
            }
            if (c < 0) return -1
            else if (c > 0) return 1
            else {
                val cc = getCoef(i1).compareTo(q.getCoef(i2))
                if (cc < 0) return -1
                else if (cc > 0) return 1
                m1 = if (i1 == 0) null else monomial[--i1]
                m2 = if (i2 == 0) null else q.monomial[--i2]
            }
        }
        return 0
    }

    open fun init(polynomial: Polynomial) {
        val q = polynomial as ArrayPolynomialGeneric
        init(q._size)
        System.arraycopy(q.monomial, 0, monomial, 0, _size)
        for (i in 0 until _size) setCoef(i, q.getCoef(i))
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
            monomial[i] = m
            setCoef(i, a)
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
                monomial[0] = monomial(Literal.newInstance())
                setCoef(0, a)
            } else init(0)
            _degree = 0
            sugar = 0
        }
    }

    fun init(monomial: Monomial) {
        init(1)
        this.monomial[0] = monomial
        setCoef(0, coefficient(JsclInteger.valueOf(1)))
        _degree = monomial.degree()
        sugar = monomial.degree()
    }

    open fun newInstance(n: Int): ArrayPolynomialGeneric {
        return ArrayPolynomialGeneric(n, monomialFactory, coefFactory)
    }

    inner class ContentIterator(
        private val direction: Boolean,
        current: Monomial?
    ) : MutableIterator<Any> {
        private var index: Int = indexOf(current, direction)

        override fun hasNext(): Boolean = if (direction) index > 0 else index < _size

        override fun next(): Any = if (direction) term(--index) else term(index++)

        override fun remove() {
            throw UnsupportedOperationException()
        }
    }
}
