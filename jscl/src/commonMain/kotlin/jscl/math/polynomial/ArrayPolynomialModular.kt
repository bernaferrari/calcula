package jscl.math.polynomial

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.ModularInteger

internal open class ArrayPolynomialModular : ArrayPolynomialGeneric {
    val modulo: Int
    var intCoef: IntArray

    constructor(monomialFactory: Monomial, coefFactory: Generic) : super(monomialFactory, coefFactory) {
        modulo = (coefFactory as ModularInteger).modulo()
        intCoef = IntArray(0)
    }

    constructor(size: Int, monomialFactory: Monomial, coefFactory: Generic) : this(monomialFactory, coefFactory) {
        init(size)
    }

    override fun init(size: Int) {
        monomial = arrayOfNulls(size)
        intCoef = IntArray(size)
        this._size = size
    }

    override fun resize(size: Int) {
        val length = monomial.size
        if (size < length) {
            val newMonomial = arrayOfNulls<Monomial>(size)
            val newCoef = IntArray(size)
            monomial.copyInto(newMonomial, 0, length - size, length - size + size)
            intCoef.copyInto(newCoef, 0, length - size, length - size + size)
            monomial = newMonomial
            intCoef = newCoef
            this._size = size
        }
    }

    override fun subtract(that: Polynomial): Polynomial {
        if (that.signum() == 0) return this
        val q = that as ArrayPolynomialModular
        val p = newInstance(_size + q._size) as ArrayPolynomialModular
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
                val a = intCoef[i1]
                --i
                p.monomial[i] = m1
                p.intCoef[i] = a
                m1 = if (i1 > 0) monomial[--i1] else null
            } else if (c > 0) {
                val a = ((modulo.toLong() - q.intCoef[i2].toLong()) % modulo).toInt()
                --i
                p.monomial[i] = m2
                p.intCoef[i] = a
                m2 = if (i2 > 0) q.monomial[--i2] else null
            } else {
                val a = ((intCoef[i1].toLong() + modulo.toLong() - q.intCoef[i2].toLong()) % modulo).toInt()
                if (a != 0) {
                    --i
                    p.monomial[i] = m1
                    p.intCoef[i] = a
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
        val g = generic.integerValue().toInt()
        if (g == 1) return subtract(polynomial)
        val q = polynomial as ArrayPolynomialModular
        val p = newInstance(_size + q._size) as ArrayPolynomialModular
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
                val a = intCoef[i1]
                --i
                p.monomial[i] = m1
                p.intCoef[i] = a
                m1 = if (i1 > 0) monomial[--i1] else null
            } else if (c > 0) {
                val a = ((modulo.toLong() - (q.intCoef[i2].toLong() * g.toLong()) % modulo) % modulo).toInt()
                --i
                p.monomial[i] = m2
                p.intCoef[i] = a
                m2 = if (i2 > 0) q.monomial[--i2] else null
            } else {
                val a = ((intCoef[i1].toLong() + modulo.toLong() - (q.intCoef[i2].toLong() * g.toLong()) % modulo) % modulo).toInt()
                if (a != 0) {
                    --i
                    p.monomial[i] = m1
                    p.intCoef[i] = a
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
        val g = generic.integerValue().toInt()
        val q = polynomial as ArrayPolynomialModular
        val p = newInstance(_size + q._size) as ArrayPolynomialModular
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
                val a = intCoef[i1]
                --i
                p.monomial[i] = m1
                p.intCoef[i] = a
                m1 = if (i1 > 0) this.monomial[--i1] else null
            } else if (c > 0) {
                val a = ((modulo.toLong() - (q.intCoef[i2].toLong() * g.toLong()) % modulo) % modulo).toInt()
                --i
                p.monomial[i] = m2
                p.intCoef[i] = a
                m2 = if (i2 > 0) q.monomial[--i2]?.multiply(monomial) else null
            } else {
                val a = ((intCoef[i1].toLong() + modulo.toLong() - (q.intCoef[i2].toLong() * g.toLong()) % modulo) % modulo).toInt()
                if (a != 0) {
                    --i
                    p.monomial[i] = m1
                    p.intCoef[i] = a
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

    override fun multiply(generic: Generic): Polynomial {
        if (generic.signum() == 0) return valueOf(JsclInteger.valueOf(0))
        val g = generic.integerValue().toInt()
        if (g == 1) return this
        val p = newInstance(_size) as ArrayPolynomialModular
        for (i in 0 until _size) {
            p.monomial[i] = monomial[i]
            p.intCoef[i] = ((intCoef[i].toLong() * g.toLong()) % modulo).toInt()
        }
        p._degree = _degree
        p.sugar = sugar
        return p
    }

    override fun multiply(monomial: Monomial): Polynomial {
        if (defined) throw UnsupportedOperationException()
        if (monomial.degree() == 0) return this
        val p = newInstance(_size) as ArrayPolynomialModular
        for (i in 0 until _size) {
            p.monomial[i] = this.monomial[i]!!.multiply(monomial)
            p.intCoef[i] = intCoef[i]
        }
        p._degree = _degree + monomial.degree()
        p.sugar = sugar + monomial.degree()
        return p
    }

    override fun coefficient(generic: Generic): Generic {
        return coefFactory!!.valueOf(generic)
    }

    override fun getCoef(n: Int): Generic {
        return ModularInteger(intCoef[n].toLong(), modulo)
    }

    override fun setCoef(n: Int, generic: Generic) {
        intCoef[n] = generic.integerValue().toInt()
    }

    override fun newInstance(n: Int): ArrayPolynomialGeneric {
        return ArrayPolynomialModular(n, monomialFactory, coefFactory!!)
    }
}
