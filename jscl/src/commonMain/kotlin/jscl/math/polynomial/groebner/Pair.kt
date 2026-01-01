package jscl.math.polynomial.groebner

import jscl.math.polynomial.Monomial
import jscl.math.polynomial.Polynomial

internal class Pair : Comparable<Pair> {
    val polynomial: Array<Polynomial>
    val monomial: Array<Monomial>
    val scm: Monomial
    val sugar: Int
    var coprime: Boolean = false
    var reduction: Boolean = false
    var principal: Polynomial? = null

    constructor(p1: Polynomial, p2: Polynomial) : this(arrayOf(p1, p2)) {
        val m0 = monomial[0]
        val m1 = monomial[1]
        coprime = m0.gcd(m1).degree() == 0
        @Suppress("UNCHECKED_CAST")
        val cmp = (m0 as Comparable<Any>).compareTo(m1)
        val index = if (cmp < 0) intArrayOf(0, 1) else intArrayOf(1, 0)
        reduction = monomial[index[1]].multiple(monomial[index[0]])
        principal = polynomial[index[1]]
    }

    constructor(polynomial: Array<Polynomial>) {
        this.polynomial = polynomial
        monomial = arrayOf(polynomial[0].head()!!.monomial(), polynomial[1].head()!!.monomial())
        scm = monomial[0].scm(monomial[1])
        sugar = kotlin.math.max(
            polynomial[0].sugar() - polynomial[0].degree(),
            polynomial[1].sugar() - polynomial[1].degree()
        ) + scm.degree()
    }

    override fun compareTo(other: Pair): Int {
        @Suppress("UNCHECKED_CAST")
        val c = (scm as Comparable<Any>).compareTo(other.scm)
        if (c < 0) return -1
        if (c > 0) return 1

        val c1 = polynomial[1].index() - other.polynomial[1].index()
        if (c1 < 0) return -1
        if (c1 > 0) return 1

        val c2 = polynomial[0].index() - other.polynomial[0].index()
        if (c2 < 0) return -1
        if (c2 > 0) return 1

        return 0
    }

    override fun toString(): String {
        return "{${polynomial[0].index()}, ${polynomial[1].index()}}, $sugar, $reduction"
    }
}
