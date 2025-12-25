package jscl.math.polynomial.groebner

import jscl.math.Debug
import jscl.math.polynomial.Basis
import jscl.math.polynomial.Monomial
import jscl.math.polynomial.Polynomial
import jscl.math.polynomial.Term
import java.util.*

internal class F4Reduction(
    val ideal: Collection<*>,
    val list: MutableList<*>,
    val flags: Int
) {
    val polys: MutableList<Any?> = mutableListOf()
    val content: MutableList<Any?> = mutableListOf()
    val considered: MutableMap<Monomial, Any?> = TreeMap()
    val head: MutableMap<Monomial, Any?> = TreeMap()
    val proj: MutableMap<Projection, Any?> = TreeMap()

    companion object {
        fun compute(pairs: List<*>, ideal: Collection<*>, list: MutableList<*>, flags: Int): List<*> {
            val r = F4Reduction(ideal, list, flags)
            r.compute(pairs)
            return r.content
        }
    }

    fun compute(pairs: List<*>) {
        val it = pairs.iterator()
        while (it.hasNext()) {
            val pa = it.next() as Pair
            considered[pa.scm] = null
            add(pa)
        }
        process()
        if ((flags and Basis.F4_SIMPLIFY) > 0) (list as MutableList<Any?>).add(this)
    }

    fun add(pair: Pair) {
        Debug.println(pair)
        val pr = arrayOf(Projection(pair, 0), Projection(pair, 1))
        for (i in pr.indices)
            if (!proj.containsKey(pr[i])) {
                add(pr[i].simplify(list))
                proj[pr[i]] = null
            }
    }

    fun add(projection: Projection) {
        val p = projection.mult()
        val scm = projection.scm()
        head[scm] = null
        val it = p.iterator(scm)
        while (it.hasNext()) {
            val t = it.next() as? Term ?: continue
            val m1 = t.monomial()
            if (considered.containsKey(m1)) continue
            else considered[m1] = null
            val iq = ideal.iterator()
            while (iq.hasNext()) {
                val q = iq.next() as Polynomial
                val m2 = q.head()!!.monomial()
                if (m1.multiple(m2)) {
                    val m = m1.divide(m2)
                    add(Projection(m, q).simplify(list))
                    break
                }
            }
        }
        content.add(p)
    }

    fun process() {
        val list = ReducedRowEchelonForm.compute(content)
        content.clear()
        val n = list.size
        for (i in 0 until n) {
            val p = list[i] as Polynomial
            if (p.signum() != 0) {
                val m = p.head()!!.monomial()
                if (!head.containsKey(m)) content.add(p)
                else {
                    val pCopy = if (p.index() != -1) p.copy() else p
                    pCopy.setIndex(polys.size)
                    polys.add(pCopy)
                }
            }
        }
    }
}
