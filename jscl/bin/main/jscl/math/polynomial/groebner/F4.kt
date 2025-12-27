package jscl.math.polynomial.groebner

import jscl.math.polynomial.Ordering
import jscl.math.polynomial.Polynomial

internal class F4(ordering: Ordering, flags: Int) : Block(ordering, flags) {
    val reduction: MutableList<Any?> = mutableListOf()

    override fun add(list: List<*>) {
        val it = list.iterator()
        while (it.hasNext()) {
            val p = it.next() as Polynomial
            if (p.signum() != 0) add(p)
        }
    }

    override fun process(pairs: List<*>) {
        val list: MutableList<Any?> = mutableListOf()
        val it = pairs.iterator()
        while (it.hasNext()) {
            val pa = it.next() as Pair
            if (criterion(pa)) continue
            list.add(pa)
        }
        add(F4Reduction.compute(list, polys, reduction, flags))
        npairs += list.size
    }
}
