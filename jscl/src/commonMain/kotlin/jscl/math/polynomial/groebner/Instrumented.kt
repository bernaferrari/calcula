package jscl.math.polynomial.groebner

import jscl.math.Generic
import jscl.math.polynomial.Basis
import jscl.math.polynomial.Polynomial

internal class Instrumented(flags: Int) : Standard(flags) {
    val aux: MutableList<Any?> = mutableListOf()

    override fun populate(basis: Basis) {
        val aux = basis.modulo(2147483647)
        val a = basis.elements()
        for (i in a.indices) {
            val p = basis.polynomial(a[i])
            val x = aux.polynomial(a[i])
            if (x.signum() != 0 && p.signum() != 0) add(p, x)
        }
    }

    override fun process(pair: Pair) {
        if (criterion(pair)) return
        val x = reduce(Pair(arrayOf(auxiliary(pair.polynomial[0]), auxiliary(pair.polynomial[1]))), aux)
        if (x.signum() != 0) {
            val p = reduce(pair, polys)
            if (p.signum() != 0) add(p, x)
        }
        npairs++
    }

    fun add(polynomial: Polynomial, auxiliary: Polynomial) {
        add(polynomial)
        aux.add(auxiliary)
    }

    fun auxiliary(polynomial: Polynomial): Polynomial {
        return aux[polynomial.index()] as Polynomial
    }
}
