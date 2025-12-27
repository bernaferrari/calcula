package jscl.math.polynomial

import jscl.math.Variable

internal class DefinedBooleanMonomial : BooleanMonomial {
    constructor(unknown: Array<Variable>, ordering: Ordering) : super(unknown, ordering)

    constructor(length: Int, unknown: Array<Variable>, ordering: Ordering) : super(length, unknown, ordering)

    override fun multiply(monomial: Monomial): Monomial {
        val m = newinstance()
        for (i in unknown.indices) {
            val q = i shr log2p
            val r = (i and pmask) shl log2n
            val a = (element[q] shr r) and nmask
            val b = (monomial.element[q] shr r) and nmask
            var c = a + b
            if (c > 1) c = 1
            m.element[q] = m.element[q] or (c shl r)
            m.degree += c
        }
        return m
    }

    override fun newinstance(): Monomial {
        return DefinedBooleanMonomial(element.size, unknown, ordering)
    }
}
