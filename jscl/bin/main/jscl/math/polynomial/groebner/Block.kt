package jscl.math.polynomial.groebner

import jscl.math.Debug
import jscl.math.polynomial.Basis
import jscl.math.polynomial.DegreeOrdering
import jscl.math.polynomial.Ordering
import jscl.math.polynomial.Polynomial

internal open class Block(ordering: Ordering, flags: Int) : Standard(flags) {
    var degree: Boolean = false

    init {
        degree = ordering is DegreeOrdering
    }

    override fun compute() {
        Debug.println("evaluate")
        var degree = 0
        while (!pairs.isEmpty()) {
            val list: MutableList<Any?> = mutableListOf()
            val it = pairs.keys.iterator()
            while (it.hasNext()) {
                val pa = it.next()
                val d = if ((flags and Basis.SUGAR) > 0) pa.sugar else pa.scm.degree()
                if (degree == 0) degree = d
                else if (d > degree || !this.degree) break
                list.add(pa)
            }
            process(list)
            remove(list)
            degree = 0
        }
    }

    override fun add(list: List<*>) {
        super.add(ReducedRowEchelonForm.compute(list))
    }

    open fun process(pairs: List<*>) {
        val list: MutableList<Any?> = mutableListOf()
        val it = pairs.iterator()
        while (it.hasNext()) {
            val pa = it.next() as Pair
            if (criterion(pa)) continue
            val p = reduce(pa, polys)
            if (p.signum() != 0) list.add(p)
            npairs++
        }
        add(list)
    }

    open fun remove(pairs: List<*>) {
        val it = pairs.iterator()
        while (it.hasNext()) remove(it.next() as Pair)
    }
}
