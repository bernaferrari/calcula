package jscl.math.polynomial

import jscl.math.Generic
import jscl.math.Rational

internal class ArrayPolynomialRational : ArrayPolynomialGeneric {
    var ratCoef: Array<Rational?>

    constructor(monomialFactory: Monomial) : super(monomialFactory, Rational.factory) {
        ratCoef = emptyArray()
    }

    constructor(size: Int, monomialFactory: Monomial) : this(monomialFactory) {
        init(size)
    }

    override fun init(size: Int) {
        monomial = arrayOfNulls(size)
        ratCoef = arrayOfNulls(_size)
        this._size = size
    }

    override fun resize(size: Int) {
        val length = monomial.size
        if (size < length) {
            val newMonomial = arrayOfNulls<Monomial>(size)
            val newCoef = arrayOfNulls<Rational>(_size)
            monomial.copyInto(newMonomial, 0, length - size, length - size + size)
            ratCoef.copyInto(newCoef, 0, length - _size, length - _size + _size)
            monomial = newMonomial
            ratCoef = newCoef
            this._size = size
        }
    }

    override fun coefficient(generic: Generic): Generic {
        return coefFactory!!.valueOf(generic)
    }

    override fun getCoef(n: Int): Generic {
        return ratCoef[n]!!
    }

    override fun setCoef(n: Int, generic: Generic) {
        ratCoef[n] = generic as Rational
    }

    override fun newInstance(n: Int): ArrayPolynomialGeneric {
        return ArrayPolynomialRational(n, monomialFactory)
    }
}
