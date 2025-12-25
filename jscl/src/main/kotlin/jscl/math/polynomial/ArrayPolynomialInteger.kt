package jscl.math.polynomial

import jscl.math.Generic
import jscl.math.JsclInteger
import com.ionspin.kotlin.bignum.integer.BigInteger

internal class ArrayPolynomialInteger : ArrayPolynomialGeneric {
    var intCoef: Array<BigInteger?>

    constructor(monomialFactory: Monomial) : super(monomialFactory, JsclInteger.factory) {
        intCoef = emptyArray()
    }

    constructor(size: Int, monomialFactory: Monomial) : this(monomialFactory) {
        init(size)
    }

    override fun init(size: Int) {
        monomial = arrayOfNulls(size)
        intCoef = arrayOfNulls(size)
        this._size = size
    }

    override fun resize(size: Int) {
        val length = monomial.size
        if (size < length) {
            val newMonomial = arrayOfNulls<Monomial>(size)
            val newCoef = arrayOfNulls<BigInteger>(size)
            System.arraycopy(monomial, length - size, newMonomial, 0, size)
            System.arraycopy(intCoef, length - size, newCoef, 0, size)
            monomial = newMonomial
            intCoef = newCoef
            this._size = size
        }
    }

    override fun subtract(that: Polynomial): Polynomial {
        if (that.signum() == 0) return this
        val q = that as ArrayPolynomialInteger
        val p = newInstance(_size + q._size) as ArrayPolynomialInteger
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
                val a = q.intCoef[i2]!!.negate()
                --i
                p.monomial[i] = m2
                p.intCoef[i] = a
                m2 = if (i2 > 0) q.monomial[--i2] else null
            } else {
                val a = intCoef[i1]!!.subtract(q.intCoef[i2]!!)
                if (a.signum().toInt() != 0) {
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
        val g = generic.integerValue().content()
        if (g.compareTo(BigInteger.fromLong(1)) == 0) return subtract(polynomial)
        val q = polynomial as ArrayPolynomialInteger
        val p = newInstance(_size + q._size) as ArrayPolynomialInteger
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
                val a = q.intCoef[i2]!!.multiply(g).negate()
                --i
                p.monomial[i] = m2
                p.intCoef[i] = a
                m2 = if (i2 > 0) q.monomial[--i2] else null
            } else {
                val a = intCoef[i1]!!.subtract(q.intCoef[i2]!!.multiply(g))
                if (a.signum().toInt() != 0) {
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
        val g = generic.integerValue().content()
        val q = polynomial as ArrayPolynomialInteger
        val p = newInstance(_size + q._size) as ArrayPolynomialInteger
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
                val a = q.intCoef[i2]!!.multiply(g).negate()
                --i
                p.monomial[i] = m2
                p.intCoef[i] = a
                m2 = if (i2 > 0) q.monomial[--i2]?.multiply(monomial) else null
            } else {
                val a = intCoef[i1]!!.subtract(q.intCoef[i2]!!.multiply(g))
                if (a.signum().toInt() != 0) {
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
        val g = generic.integerValue().content()
        if (g.compareTo(BigInteger.fromLong(1)) == 0) return this
        val p = newInstance(_size) as ArrayPolynomialInteger
        for (i in 0 until _size) {
            p.monomial[i] = monomial[i]
            p.intCoef[i] = intCoef[i]!!.multiply(g)
        }
        p._degree = _degree
        p.sugar = sugar
        return p
    }

    override fun multiply(monomial: Monomial): Polynomial {
        if (defined) throw UnsupportedOperationException()
        if (monomial.degree() == 0) return this
        val p = newInstance(_size) as ArrayPolynomialInteger
        for (i in 0 until _size) {
            p.monomial[i] = this.monomial[i]!!.multiply(monomial)
            p.intCoef[i] = intCoef[i]
        }
        p._degree = _degree + monomial.degree()
        p.sugar = sugar + monomial.degree()
        return p
    }

    override fun divide(generic: Generic): Polynomial {
        val g = generic.integerValue().content()
        if (g.compareTo(BigInteger.fromLong(1)) == 0) return this
        val p = newInstance(_size) as ArrayPolynomialInteger
        for (i in 0 until _size) {
            p.monomial[i] = monomial[i]
            p.intCoef[i] = intCoef[i]!!.divide(g)
        }
        p._degree = _degree
        p.sugar = sugar
        return p
    }

    override fun gcd(polynomial: Polynomial): Polynomial {
        return valueOf(genericValue().gcd(polynomial.genericValue()))
    }

    override fun gcd(): Generic {
        var a = BigInteger.fromLong(0)
        for (i in _size - 1 downTo 0) {
            a = a.gcd(intCoef[i]!!)
            if (a.compareTo(BigInteger.fromLong(1)) == 0) break
        }
        return JsclInteger(if (a.signum().toInt() == signum()) a else a.negate())
    }

    override fun coefficient(generic: Generic): Generic {
        return coefFactory!!.valueOf(generic)
    }

    override fun getCoef(n: Int): Generic {
        return JsclInteger(intCoef[n]!!)
    }

    override fun setCoef(n: Int, generic: Generic) {
        intCoef[n] = generic.integerValue().content()
    }

    override fun newInstance(n: Int): ArrayPolynomialGeneric {
        return ArrayPolynomialInteger(n, monomialFactory)
    }
}
