package jscl.math.polynomial.groebner

import jscl.math.polynomial.Monomial
import jscl.math.polynomial.Polynomial

internal class Projection : Comparable<Projection> {
    val monomial: Monomial
    val polynomial: Polynomial

    constructor(pair: Pair, index: Int) : this(pair.scm.divide(pair.monomial[index]), pair.polynomial[index])

    constructor(monomial: Monomial, polynomial: Polynomial) {
        this.monomial = monomial
        this.polynomial = polynomial
    }

    fun scm(): Monomial {
        return polynomial.head()!!.monomial().multiply(monomial)
    }

    fun mult(): Polynomial {
        return polynomial.multiply(monomial)
    }

    fun simplify(list: List<*>): Projection {
        var t = monomial
        if (t.degree() > 0) {
            val m = polynomial.head()!!.monomial()
            val n = list.size
            for (i in 0 until n) {
                val ideal = (list[i] as F4Reduction).polys
                val it = ideal.iterator()
                while (it.hasNext()) {
                    val p = it.next() as Polynomial
                    val u = p.head()!!.monomial()
                    if (u.multiple(m, true)) {
                        val uDivM = u.divide(m)
                        if (t.multiple(uDivM, true)) {
                            val pr = Projection(t.divide(uDivM), p).simplify(list)
                            return pr
                        }
                    }
                }
            }
        }
        return this
    }

    override fun compareTo(other: Projection): Int {
        @Suppress("UNCHECKED_CAST")
        val c = (monomial as Comparable<Any>).compareTo(other.monomial)
        if (c < 0) return -1
        if (c > 0) return 1

        val c1 = polynomial.index() - other.polynomial.index()
        if (c1 < 0) return -1
        if (c1 > 0) return 1

        return 0
    }

    override fun toString(): String {
        return "{$monomial, ${polynomial.head()!!.monomial()}}"
    }
}
