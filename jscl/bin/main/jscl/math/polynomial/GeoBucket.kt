package jscl.math.polynomial

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.mathml.MathML

internal class GeoBucket : Polynomial {
    val factory: Polynomial
    var content: Array<Polynomial?>
    private var _size: Int
    var mutable: Boolean = true
    var canonicalized: Boolean = true

    constructor(factory: Polynomial) : super(factory.monomialFactory, factory.coefFactory) {
        this.factory = factory
        content = emptyArray()
        _size = 0
    }

    constructor(size: Int, factory: Polynomial) : this(factory) {
        init(size)
    }

    override fun size(): Int = _size

    fun init(size: Int) {
        content = arrayOfNulls(size)
        this._size = size
    }

    fun resize(size: Int) {
        val newContent = arrayOfNulls<Polynomial>(size)
        System.arraycopy(content, 0, newContent, 0, minOf(this._size, size))
        content = newContent
        this._size = size
    }

    override fun iterator(direction: Boolean, current: Monomial?): MutableIterator<Any> {
        return ContentIterator(direction, current)
    }

    fun behead(t: Term, n: Int, i: Int): Term {
        val m = t.monomial()
        val p = factory.valueOf(m).multiply(t.coef())
        content[n] = content[n]!!.subtract(p)
        content[i] = content[i]!!.add(p)
        return Term(m, content[i]!!.coefficient(m))
    }

    fun canonicalize() {
        var s = factory.valueOf(JsclInteger.valueOf(0))
        var sugarVal = 0
        for (i in 0 until _size) {
            val p = content[i] ?: continue
            s = s.add(p)
            sugarVal = maxOf(sugarVal, p.sugar())
            content[i] = null
        }
        resize(log(s.size()) + 1)
        set(s.normalize())
        canonicalized = true
        setSugar(sugarVal)
        mutable = false
    }

    fun polynomial(): Polynomial {
        if (canonicalized) return content[_size - 1]!!
        else throw UnsupportedOperationException()
    }

    fun set(polynomial: Polynomial) {
        content[_size - 1] = polynomial
    }

    override fun subtract(that: Polynomial): Polynomial {
        if (mutable) {
            val q = (that as GeoBucket).polynomial()
            var n = log(q.size())
            if (n >= _size) resize(n + 1)
            var p = content[n]
            var s = (if (p == null) factory.valueOf(JsclInteger.valueOf(0)) else p).subtract(q)
            content[n] = null
            while (n < log(s.size())) {
                n++
                if (n >= _size) resize(n + 1)
                p = content[n]
                if (p != null) s = p.add(s)
                content[n] = null
            }
            content[n] = s
            canonicalized = false
            normalized = false
            return this
        } else return copy().subtract(that)
    }

    override fun multiplyAndSubtract(generic: Generic, polynomial: Polynomial): Polynomial {
        if (mutable) {
            val q = (polynomial as GeoBucket).polynomial()
            var n = log(q.size())
            if (n >= _size) resize(n + 1)
            var p = content[n]
            var s = (if (p == null) factory.valueOf(JsclInteger.valueOf(0)) else p).multiplyAndSubtract(generic, q)
            content[n] = null
            while (n < log(s.size())) {
                n++
                if (n >= _size) resize(n + 1)
                p = content[n]
                if (p != null) s = p.add(s)
                content[n] = null
            }
            content[n] = s
            canonicalized = false
            normalized = false
            return this
        } else return copy().multiplyAndSubtract(generic, polynomial)
    }

    override fun multiplyAndSubtract(monomial: Monomial, generic: Generic, polynomial: Polynomial): Polynomial {
        if (mutable) {
            val q = (polynomial as GeoBucket).polynomial()
            var n = log(q.size())
            if (n >= _size) resize(n + 1)
            var p = content[n]
            var s = (if (p == null) factory.valueOf(JsclInteger.valueOf(0)) else p).multiplyAndSubtract(monomial, generic, q)
            content[n] = null
            while (n < log(s.size())) {
                n++
                if (n >= _size) resize(n + 1)
                p = content[n]
                if (p != null) s = p.add(s)
                content[n] = null
            }
            content[n] = s
            canonicalized = false
            normalized = false
            return this
        } else return copy().multiplyAndSubtract(monomial, generic, polynomial)
    }

    override fun multiply(generic: Generic): Polynomial {
        if (mutable) {
            if (canonicalized) set(polynomial().multiply(generic))
            else for (i in 0 until _size) {
                val p = content[i]
                if (p != null) content[i] = p.multiply(generic)
            }
            normalized = false
            return this
        } else return copy().multiply(generic)
    }

    override fun multiply(monomial: Monomial): Polynomial {
        if (mutable) {
            set(polynomial().multiply(monomial))
            return this
        } else return copy().multiply(monomial)
    }

    override fun divide(generic: Generic): Polynomial {
        if (mutable) {
            if (canonicalized) set(polynomial().divide(generic))
            else for (i in 0 until _size) {
                val p = content[i]
                if (p != null) content[i] = p.divide(generic)
            }
            normalized = false
            return this
        } else return copy().divide(generic)
    }

    override fun divide(monomial: Monomial): Polynomial {
        if (mutable) {
            set(polynomial().divide(monomial))
            return this
        } else return copy().divide(monomial)
    }

    override fun gcd(polynomial: Polynomial): Polynomial {
        throw UnsupportedOperationException()
    }

    override fun gcd(): Generic {
        if (field) return coefficient(tail())
        return if (canonicalized) polynomial().gcd() else coefficient(JsclInteger.valueOf(0))
    }

    override fun degree(): Int = polynomial().degree()

    fun valueof(bucket: GeoBucket): Polynomial {
        return valueOf(bucket.polynomial().copy())
    }

    override fun valueOf(polynomial: Polynomial): Polynomial {
        return if (polynomial is GeoBucket) {
            valueof(polynomial)
        } else {
            val b = GeoBucket(log(polynomial.size()) + 1, factory)
            b.set(polynomial)
            b
        }
    }

    override fun valueOf(generic: Generic): Polynomial {
        return valueOf(factory.valueOf(generic))
    }

    override fun valueOf(monomial: Monomial): Polynomial {
        return valueOf(factory.valueOf(monomial))
    }

    override fun freeze(): Polynomial {
        canonicalize()
        return this
    }

    override fun head(): Term? {
        return if (canonicalized) polynomial().head() else super.head()
    }

    override fun tail(): Term? {
        return if (canonicalized) polynomial().tail() else super.tail()
    }

    override fun coefficient(monomial: Monomial): Generic {
        return if (canonicalized) polynomial().coefficient(monomial) else super.coefficient(monomial)
    }

    override fun sugar(): Int {
        return polynomial().sugar()
    }

    override fun index(): Int {
        return polynomial().index()
    }

    override fun setSugar(n: Int) {
        polynomial().setSugar(n)
    }

    override fun setIndex(n: Int) {
        polynomial().setIndex(n)
    }

    override fun genericValue(): Generic {
        return polynomial().genericValue()
    }

    override fun elements(): Array<Generic> {
        return polynomial().elements()
    }

    override fun compareTo(other: Polynomial): Int {
        val bucket = other as GeoBucket
        return polynomial().compareTo(bucket.polynomial())
    }

    override fun toString(): String {
        return if (canonicalized) polynomial().toString()
        else {
            val buffer = StringBuilder()
            buffer.append("{")
            for (i in 0 until _size) {
                val p = content[i]
                buffer.append(p ?: factory.valueOf(JsclInteger.valueOf(0))).append(if (i < _size - 1) ", " else "")
            }
            buffer.append("}")
            buffer.toString()
        }
    }

    override fun toMathML(element: MathML, data: Any?) {
        if (canonicalized) polynomial().toMathML(element, data)
        else {
            val e1 = element.element("mfenced")
            val e2 = element.element("mtable")
            for (i in 0 until _size) {
                val e3 = element.element("mtr")
                val e4 = element.element("mtd")
                val p = content[i]
                (p ?: factory.valueOf(JsclInteger.valueOf(0))).toMathML(e4, null)
                e3.appendChild(e4)
                e2.appendChild(e3)
            }
            e1.appendChild(e2)
            element.appendChild(e1)
        }
    }

    inner class ContentIterator(
        private val direction: Boolean,
        current: Monomial?
    ) : MutableIterator<Any> {
        private var term: Term?

        init {
            term = Term(current, coefficient(JsclInteger.valueOf(0)))
            seek()
        }

        private fun seek() {
            while (true) {
                var n = 0
                var t: Term? = null
                for (i in 0 until _size) {
                    val p = content[i] ?: continue
                    val it = p.iterator(direction, term?.monomial())
                    val u: Term? = if (it.hasNext()) it.next() as Term else null
                    if (u == null) continue
                    if (t == null || (if (direction) -1 else 1) * ordering.compare(t.monomial(), u.monomial()) > 0) {
                        t = u
                        n = i
                    } else if (ordering.compare(t.monomial(), u.monomial()) == 0) {
                        t = behead(t, n, i)
                        n = i
                    }
                }
                if (t == null || t.coef().signum() != 0) {
                    term = t
                    return
                }
            }
        }

        override fun hasNext(): Boolean {
            return term != null
        }

        override fun next(): Any {
            val t = term!!
            seek()
            return t
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }
    }

    companion object {
        @JvmStatic
        fun log(n: Int): Int {
            var i = 0
            var num = n
            while (num > 3) {
                num = num shr 2
                i++
            }
            return i
        }
    }
}
