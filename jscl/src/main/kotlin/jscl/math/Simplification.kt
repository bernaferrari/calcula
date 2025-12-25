package jscl.math

import jscl.math.function.*
import jscl.math.polynomial.Basis
import jscl.math.polynomial.Monomial
import jscl.math.polynomial.Polynomial
import jscl.math.polynomial.UnivariatePolynomial

class Simplification private constructor() {

    private val cache = mutableMapOf<Variable, Generic>()
    private val constraints = mutableListOf<Constraint>()
    var result: Generic? = null
    var linear: Boolean = false

    fun computeValue(generic: Generic) {
        Debug.println("simplification")
        Debug.increment()

        val t: Variable = TechnicalVariable("t")
        linear = false
        process(Constraint(t, t.expressionValue().subtract(generic), false))
        val p = polynomial(t)

        result = when (p?.degree()) {
            0 -> generic
            1 -> Root(p, 0).selfSimplify()
            else -> {
                linear(generic)
                result
            }
        }

        Debug.decrement()
    }

    fun linear(generic: Generic) {
        val t: Variable = TechnicalVariable("t")
        linear = true
        constraints.clear()
        process(Constraint(t, t.expressionValue().subtract(generic), false))
        val p = polynomial(t)
        result = when (p?.degree()) {
            0 -> generic
            else -> Root(p!!, 0).selfSimplify()
        }
    }

    fun branch(generic: Generic, polynomial: UnivariatePolynomial): Int {
        val n = polynomial.degree()
        val t: Variable = TechnicalVariable("t")
        linear = true
        for (i in 0 until n) {
            constraints.clear()
            process(Constraint(t, t.expressionValue().subtract(generic.subtract(Root(polynomial, i).expressionValue())), false))
            val a = polynomial(t)?.solve()
            if (a != null && a.signum() == 0) {
                return i
            }
        }
        return n
    }

    fun polynomial(t: Variable): UnivariatePolynomial? {
        val fact = Polynomial.factory(t)
        val n = constraints.size
        var a = arrayOfNulls<Generic>(n)
        var unk = arrayOfNulls<Variable>(n)
        if (linear) {
            var j = 0
            for (constraint in constraints) {
                if (constraint.reduce) {
                    a[j] = constraint.generic
                    unk[j] = constraint.unknown
                    j++
                }
            }
            var k = 0
            for (c in constraints) {
                if (!c.reduce) {
                    a[j] = c.generic
                    unk[j] = c.unknown
                    j++
                    k++
                }
            }
            a = solve(a.filterNotNull().toTypedArray(), unk.filterNotNull().toTypedArray(), k).map { it as Generic? }.toTypedArray()
            for (anA in a) {
                if (anA != null) {
                    val p = fact.valueOf(anA) as UnivariatePolynomial
                    if (p.degree() == 1) return p
                }
            }
            return null
        } else {
            for (i in 0 until n) {
                val c = constraints[i]
                a[i] = c.generic
                unk[i] = c.unknown
            }
            a = solve(a.filterNotNull().toTypedArray(), unk.filterNotNull().toTypedArray(), n).map { it as Generic? }.toTypedArray()
            return fact.valueOf(a[0]!!) as UnivariatePolynomial
        }
    }

    fun solve(generic: Array<Generic>, unknown: Array<Variable>, n: Int): Array<Generic> {
        val unk = Basis.augmentUnknown(unknown, generic)
        return Basis.compute(generic, unk, Monomial.kthElimination(n)).elements()
    }

    fun process(c: Constraint) {
        constraints.add(c)

        var n1 = 0
        var n2 = 0
        do {
            n1 = n2
            n2 = constraints.size
            for (i in n1 until n2) {
                subProcess(constraints[i])
            }
            n2 = constraints.size
        } while (n1 < n2)
    }

    fun subProcess(c: Constraint) {
        for (v in c.generic!!.variables()) {
            if (constraints.contains(Constraint(v))) {
                continue
            }

            var result: Constraint? = null

            when (v) {
                is Fraction -> {
                    val parameters = v.getParameters()
                    result = Constraint(v, v.expressionValue().multiply(parameters!![1]).subtract(parameters!![0]), false)
                }
                is Sqrt -> {
                    if (linear) {
                        result = linearConstraint(v)
                    }
                    if (result == null) {
                        val parameters = v.getParameters()
                        result = Constraint(v, v.expressionValue().pow(2).subtract(parameters!![0]), true)
                    }
                }
                is Cubic -> {
                    if (linear) {
                        result = linearConstraint(v)
                    }
                    if (result == null) {
                        val parameters = v.getParameters()
                        result = Constraint(v, v.expressionValue().pow(3).subtract(parameters!![0]), true)
                    }
                }
                is Pow -> {
                    try {
                        val r = v.rootValue()
                        val d = r.degree()

                        if (linear) {
                            result = linearConstraint(v)
                        }

                        if (result == null) {
                            val parameters = r.getParameters()
                            result = Constraint(v, v.expressionValue().pow(d).subtract(parameters!![0].negate()), d > 1)
                        }
                    } catch (e: NotRootException) {
                        result = linearConstraint(v)
                    }
                }
                is Root -> {
                    try {
                        val r = v
                        val d = r.degree()
                        val n = r.subscript().integerValue().toInt()

                        if (linear) {
                            result = linearConstraint(v)
                        }

                        if (result == null) {
                            val parameters = r.getParameters()
                            result = Constraint(v, Root.sigma(parameters!!, d - n).multiply(JsclInteger.valueOf(-1).pow(d - n)).multiply(parameters!![d]).subtract(parameters!![n]), d > 1)
                        }
                    } catch (e: NotIntegerException) {
                        result = linearConstraint(v)
                    }
                }
                else -> {
                    result = linearConstraint(v)
                }
            }

            if (result != null) {
                constraints.add(result)
            }
        }
    }

    private fun linearConstraint(v: Variable): Constraint? {
        var s = cache[v]
        if (s == null) {
            s = v.simplify()
            cache[v] = s
        }

        val a = v.expressionValue().subtract(s)
        return if (a.signum() != 0) {
            Constraint(v, a, false)
        } else {
            null
        }
    }

    fun getValue(): Generic = result!!

    companion object {
        @JvmStatic
        fun compute(generic: Generic): Generic {
            val s = Simplification()
            s.computeValue(generic)
            return s.getValue()
        }
    }
}

class Constraint(
    val unknown: Variable,
    val generic: Generic? = null,
    val reduce: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        return unknown.compareTo((other as Constraint).unknown) == 0
    }

    override fun hashCode(): Int {
        return unknown.hashCode()
    }
}
