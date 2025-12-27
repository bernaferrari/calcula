package jscl.math.polynomial.groebner

import jscl.math.Debug
import jscl.math.Generic
import jscl.math.polynomial.Basis
import jscl.math.polynomial.Monomial
import jscl.math.polynomial.Ordering
import jscl.math.polynomial.Polynomial
import jscl.util.ArrayUtils
import java.util.*

open class Standard(val flags: Int) {
    internal val comparator: Comparator<Pair>
    internal val pairs: MutableMap<Pair, Any?>
    val polys: MutableList<Any?> = mutableListOf()
    val removed: MutableMap<Any?, Any?> = TreeMap()
    var npairs: Int = 0
    var npolys: Int = 0

    init {
        comparator = if ((flags and Basis.SUGAR) > 0) Sugar.comparator else Natural.comparator
        pairs = TreeMap(comparator)
    }

    companion object {
        @JvmStatic
        fun compute(basis: Basis): Basis {
            return compute(basis, 0)
        }

        @JvmStatic
        fun compute(basis: Basis, flags: Int): Basis {
            return compute(basis, flags, (flags and Basis.INSTRUMENTED) > 0)
        }

        @JvmStatic
        internal fun compute(basis: Basis, flags: Int, instrumented: Boolean): Basis {
            val a = if (instrumented) Instrumented(flags) else algorithm(basis.ordering(), flags)
            a.computeValue(basis)
            var newBasis = basis.valueof(a.elements())
            if (instrumented) return compute(newBasis, flags, false)
            return newBasis
        }

        @JvmStatic
        internal fun algorithm(ordering: Ordering, flags: Int): Standard {
            return when (flags and Basis.ALGORITHM) {
                Basis.F4 -> F4(ordering, flags)
                Basis.BLOCK -> Block(ordering, flags)
                else -> Standard(flags)
            }
        }

        @JvmStatic
        internal fun reduce(pair: Pair, ideal: Collection<*>): Polynomial {
            Debug.println(pair)
            return s_polynomial(pair.polynomial[0], pair.polynomial[1]).reduce(ideal, false).normalize().freeze()
        }

        @JvmStatic
        internal fun s_polynomial(p1: Polynomial, p2: Polynomial): Polynomial {
            val m1 = p1.head()!!.monomial()
            val m2 = p2.head()!!.monomial()
            val m = m1.gcd(m2)
            val m1Div = m1.divide(m)
            val m2Div = m2.divide(m)
            return p1.multiply(m2Div).reduce(p1.head()!!.coef(), m1Div, p2)
        }
    }

    open fun computeValue(basis: Basis) {
        Debug.println(basis)
        populate(basis)
        npolys = 0
        compute()
        remove()
        reduce()
        Debug.println("signature = ($npairs, $npolys, ${polys.size})")
    }

    open fun populate(basis: Basis) {
        val list: MutableList<Any?> = mutableListOf()
        val a = basis.elements()
        for (i in a.indices) list.add(basis.polynomial(a[i]))
        add(list)
    }

    open fun add(list: List<*>) {
        val it = list.iterator()
        while (it.hasNext()) {
            val p = it.next() as Polynomial
            if (p.signum() != 0) add(p)
        }
    }

    open fun compute() {
        Debug.println("evaluate")
        while (!pairs.isEmpty()) {
            val pa = pairs.keys.iterator().next()
            process(pa)
            remove(pa)
        }
    }

    internal open fun process(pair: Pair) {
        if (criterion(pair)) return
        val p = reduce(pair, polys)
        if (p.signum() != 0) add(p)
        npairs++
    }

    internal open fun remove(pair: Pair) {
        pairs.remove(pair)
        if (pair.reduction) removed[pair.principal] = null
    }

    open fun add(polynomial: Polynomial) {
        polynomial.setIndex(polys.size)
        Debug.println("(${polynomial.head()!!.monomial()}, ${polynomial.index()})")
        if ((flags and Basis.GM_SETTING) > 0) makePairsGM(polynomial)
        else makePairs(polynomial)
        polys.add(polynomial)
        npolys++
    }

    internal open fun criterion(pair: Pair): Boolean {
        return if ((flags and Basis.GM_SETTING) > 0) false else b_criterion(pair)
    }

    open fun makePairs(polynomial: Polynomial) {
        val it = polys.iterator()
        while (it.hasNext()) {
            val p = it.next() as Polynomial
            val pa = Pair(p, polynomial)
            if (!pa.coprime) pairs[pa] = null
        }
    }

    internal open fun b_criterion(pair: Pair): Boolean {
        val it = polys.iterator()
        while (it.hasNext()) {
            val p = it.next() as Polynomial
            if (pair.scm.multiple(p.head()!!.monomial())) {
                val pa1 = Pair(sort(pair.polynomial[0], p))
                val pa2 = Pair(sort(pair.polynomial[1], p))
                if (considered(pa1) && considered(pa2)) return true
            }
        }
        return false
    }

    internal open fun considered(pair: Pair): Boolean {
        return !pairs.containsKey(pair)
    }

    open fun sort(p1: Polynomial, p2: Polynomial): Array<Polynomial> {
        return if (p1.index() < p2.index()) arrayOf(p1, p2) else arrayOf(p2, p1)
    }

    open fun makePairsGM(polynomial: Polynomial) {
        val list: MutableList<Any?> = ArrayList()
        val it1 = pairs.keys.iterator()
        while (it1.hasNext()) {
            val pa = it1.next()
            val p1 = Pair(arrayOf(pa.polynomial[0], polynomial))
            val p2 = Pair(arrayOf(pa.polynomial[1], polynomial))
            if (multiple(pa, p1) && multiple(pa, p2)) list.add(pa)
        }
        val n = list.size
        for (i in 0 until n) {
            val pa = list[i] as Pair
            remove(pa)
        }
        val map: MutableMap<Pair, Any?> = TreeMap(
            if ((flags and Basis.SUGAR) > 0 && (flags and Basis.FUSSY) > 0) Sugar.comparator else Natural.comparator
        )
        val it2 = polys.iterator()
        while (it2.hasNext()) {
            val p = it2.next() as Polynomial
            val pa = Pair(p, polynomial)
            pairs[pa] = null
            map[pa] = null
        }
        val list2 = ArrayUtils.toList(map.keys)
        val n2 = list2.size
        for (i in 0 until n2) {
            val pa = list2[i] as Pair
            for (j in i + 1 until n2) {
                val pa2 = list2[j] as Pair
                if (pa2.scm.multiple(pa.scm)) remove(pa2)
            }
            if (pa.coprime) remove(pa)
        }
    }

    internal open fun multiple(p1: Pair, p2: Pair): Boolean {
        return p1.scm.multiple(p2.scm, true) && (if ((flags and Basis.SUGAR) > 0 && (flags and Basis.FUSSY) > 0) Sugar.comparator.compare(p1, p2) > 0 else true)
    }

    open fun remove() {
        val it = polys.iterator()
        while (it.hasNext()) if (removed.containsKey(it.next())) it.remove()
    }

    open fun reduce() {
        Debug.println("reduce")
        val map: MutableMap<Any?, Any?> = TreeMap()
        val size = polys.size
        for (i in 0 until size) {
            var p = polys[i] as Polynomial
            polys[i] = p.reduce(polys, true).normalize().freeze().also { p = it }
            Debug.println("(${p.head()!!.monomial()})")
            map[p] = null
        }
        polys.clear()
        polys.addAll(map.keys)
    }

    open fun elements(): Array<Generic> {
        val size = polys.size
        val a = arrayOfNulls<Generic>(size)
        for (i in 0 until size) {
            a[i] = (polys[i] as Polynomial).genericValue()
        }
        return a.requireNoNulls()
    }
}
