package jscl.math.polynomial.groebner

import jscl.math.Generic
import jscl.math.polynomial.Monomial
import jscl.math.polynomial.Polynomial

internal class ReducedRowEchelonForm(list: List<*>) {
    var content: MutableList<Any?> = mutableListOf()

    init {
        content.addAll(list)
    }

    companion object {
        fun compute(list: List<*>): List<*> {
            val f = ReducedRowEchelonForm(list)
            f.compute()
            return f.content
        }
    }

    fun compute() {
        val n = content.size
        for (i in 0 until n) reduce(i, false)
        for (i in n - 1 downTo 0) reduce(i, true)
    }

    fun reduce(pivot: Int, direction: Boolean) {
        var p = polynomial(pivot)
        content[pivot] = p.normalize().freeze().also { p = it }
        if (p.signum() == 0) return
        val m = p.head()!!.monomial()
        val b = if (direction) 0 else pivot + 1
        val n = if (direction) pivot else content.size
        for (i in b until n) {
            val q = polynomial(i)
            val a = q.coefficient(m)
            if (a.signum() != 0) content[i] = q.reduce(a, p)
        }
    }

    fun polynomial(n: Int): Polynomial {
        return content[n] as Polynomial
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append("{")
        val n = content.size
        for (i in 0 until n) {
            val p = polynomial(i)
            buffer.append(if (i > 0) ", " else "").append(p)
        }
        buffer.append("}")
        return buffer.toString()
    }
}
