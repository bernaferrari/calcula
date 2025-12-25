package jscl.math.polynomial

import jscl.math.Generic
import jscl.math.JsclBoolean
import jscl.math.JsclInteger
import java.util.TreeMap

internal class ArrayPolynomialBoolean : ArrayPolynomialModular {

    constructor(monomialFactory: Monomial) : super(monomialFactory, JsclBoolean.factory)

    constructor(size: Int, monomialFactory: Monomial) : this(monomialFactory) {
        init(size)
    }

    override fun init(size: Int) {
        monomial = arrayOfNulls(size)
        this._size = size
    }

    override fun resize(size: Int) {
        val length = monomial.size
        if (size < length) {
            val newMonomial = arrayOfNulls<Monomial>(size)
            System.arraycopy(monomial, length - size, newMonomial, 0, size)
            monomial = newMonomial
            this._size = size
        }
    }

    override fun subtract(that: Polynomial): Polynomial {
        if (that.signum() == 0) return this
        val q = that as ArrayPolynomialBoolean
        val p = newInstance(_size + q._size) as ArrayPolynomialBoolean
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
                p.monomial[--i] = m1
                m1 = if (i1 > 0) monomial[--i1] else null
            } else if (c > 0) {
                p.monomial[--i] = m2
                m2 = if (i2 > 0) q.monomial[--i2] else null
            } else {
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
        return subtract(polynomial)
    }

    override fun multiplyAndSubtract(monomial: Monomial, generic: Generic, polynomial: Polynomial): Polynomial {
        if (generic.signum() == 0) return this
        return multiplyAndSubtract(generic, polynomial.multiply(monomial))
    }

    override fun multiply(generic: Generic): Polynomial {
        if (generic.signum() == 0) return valueOf(JsclInteger.valueOf(0))
        return this
    }

    override fun multiply(monomial: Monomial): Polynomial {
        if (defined) {
            val map = TreeMap<Monomial, Any?>(ordering)
            for (i in 0 until _size) {
                val m = this.monomial[i]!!.multiply(monomial)
                if (map.containsKey(m)) map.remove(m)
                else map[m] = null
            }
            val p = newInstance(map.size) as ArrayPolynomialBoolean
            val it = map.keys.iterator()
            for (i in 0 until p._size) p.monomial[i] = it.next()
            p._degree = degree(p)
            p.sugar = sugar + monomial.degree()
            return p
        } else {
            if (monomial.degree() == 0) return this
            val p = newInstance(_size) as ArrayPolynomialBoolean
            for (i in 0 until _size) p.monomial[i] = this.monomial[i]!!.multiply(monomial)
            p._degree = _degree + monomial.degree()
            p.sugar = sugar + monomial.degree()
            return p
        }
    }

    override fun getCoef(n: Int): Generic {
        return JsclBoolean(1)
    }

    override fun setCoef(n: Int, generic: Generic) {
        // No-op for boolean
    }

    override fun newInstance(n: Int): ArrayPolynomialGeneric {
        return ArrayPolynomialBoolean(n, monomialFactory)
    }
}
