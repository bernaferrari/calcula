@file:Suppress("UNCHECKED_CAST")

package jscl.math

import jscl.math.polynomial.Basis
import jscl.math.polynomial.Monomial
import jscl.math.polynomial.Ordering
import jscl.math.polynomial.Polynomial
import jscl.util.ArrayComparator
import jscl.util.ArrayUtils

class Factorization internal constructor(var factory: Polynomial) {
    var result: Generic? = null

    fun computeValue(generic: Generic) {
        Debug.println("factorization")
        val n = factory.valueOf(generic).gcdAndNormalize()
        val m = n[1].monomialGcd()
        var s = n[1].divide(m)
        var a: Generic = JsclInteger.valueOf(1)
        val d = arrayOfNulls<Divisor>(2)
        val p = arrayOfNulls<Monomial>(2)
        val q = arrayOfNulls<Monomial>(2)
        d[1] = Divisor(s.head()!!.monomial())
        loop@ while (d[1]!!.hasNext()) {
            p[1] = d[1]!!.next() as Monomial
            q[1] = d[1]!!.complementary()
            d[0] = Divisor(s.tail()!!.monomial())
            while (d[0]!!.hasNext()) {
                p[0] = d[0]!!.next() as Monomial
                q[0] = d[0]!!.complementary()
                if (p[1]!!.compareTo(p[0]!!) <= 0) continue@loop
                @Suppress("UNCHECKED_CAST")
                Debug.println("${toString(p as Array<Monomial>)} * ${toString(q as Array<Monomial>)} = $s")
                @Suppress("UNCHECKED_CAST")
                if (ArrayComparator.comparator.compare(q as Array<Comparable<*>?>, p as Array<Comparable<*>?>) < 0) {
                    a = a.multiply(expression(s.genericValue()))
                    break@loop
                } else {
                    Debug.increment()
                    @Suppress("UNCHECKED_CAST")
                    val r = remainder(s, polynomial(s, p as Array<Monomial>), terminator(s))
                    Debug.decrement()
                    if (r[0].signum() == 0) {
                        a = a.multiply(expression(r[1].genericValue()))
                        s = r[2]
                        d[1]!!.divide()
                        d[0]!!.divide()
                        continue@loop
                    }
                }
            }
        }
        result = a.multiply(n[0].multiply(m).genericValue())
    }

    fun getValue(): Generic {
        return GenericVariable.content(result!!, true)
    }

    companion object {
        private const val ter = "t"

        fun compute(generic: Generic): Generic {
            return try {
                GenericVariable.content(factorize(generic.integerValue()))
            } catch (e: NotIntegerException) {
                val f = Factorization(Polynomial.factory(generic.variables(), Monomial.iteratorOrdering, -1))
                f.computeValue(generic)
                f.getValue()
            }
        }

        fun factorize(integer: JsclInteger): Generic {
            val n = integer.gcdAndNormalize()
            var s = n[1]
            var a: Generic = JsclInteger.valueOf(1)
            var p: Generic = JsclInteger.valueOf(2)
            while (s.compareTo(JsclInteger.valueOf(1)) > 0) {
                var q = s.divideAndRemainder(p)
                if (q[0].compareTo(p) < 0) {
                    p = s
                    q = s.divideAndRemainder(p)
                }
                if (q[1].signum() == 0) {
                    a = a.multiply(expression(p, true))
                    s = q[0]
                } else p = p.add(JsclInteger.valueOf(1))
            }
            return a.multiply(n[0])
        }

        fun remainder(s: Polynomial, p: Polynomial, t: Array<Generic>): Array<Polynomial> {
            val zero = s.valueOf(JsclInteger.valueOf(0))
            val a = Basis.augment(t, s.remainderUpToCoefficient(p).elements())
            var unk = Basis.augmentUnknown(emptyArray(), p.elements())
            run {
                val u = unk[unk.size - 1]
                unk.copyInto(unk, 1, 0, 0 + unk.size - 1)
                unk[0] = u
            }
            val be = Linearization.compute(Basis.compute(a, unk, Monomial.lexicographic, 0, Basis.DEGREE).elements(), unk)
            for (i in be.indices) {
                val r = substitute(p, be[i], unk)
                try {
                    return arrayOf(zero, r, s.divide(r))
                } catch (e: NotDivisibleException) {
                }
            }
            return arrayOf(s, zero, zero)
        }

        fun substitute(p: Polynomial, a: Array<Generic>, unk: Array<Variable>): Polynomial {
            val s = arrayOf(p.genericValue())
            return p.valueOf(Basis.compute(Basis.augment(a, s), Basis.augmentUnknown(unk, s)).elements()[0])
        }

        fun polynomial(s: Polynomial, monomial: Array<Monomial>): Polynomial {
            var p = s.valueOf(JsclInteger.valueOf(0))
            val it = monomial[1].iterator(monomial[0])
            var i = 0
            while (it.hasNext()) {
                val m = it.next() as Monomial
                val t: Variable = if (it.hasNext()) TechnicalVariable(ter, intArrayOf(i)) else TechnicalVariable(ter)
                p = p.add(p.valueOf(m).multiply(t.expressionValue()))
                i++
            }
            return p
        }

        fun terminator(polynomial: Polynomial): Array<Generic> {
            return arrayOf(
                terminator(polynomial.tail()!!.coef(), TechnicalVariable(ter, intArrayOf(0)), true),
                terminator(polynomial.head()!!.coef().abs(), TechnicalVariable(ter), false)
            )
        }

        fun terminator(generic: Generic, variable: Variable, tail: Boolean): Generic {
            val x = variable.expressionValue()
            var a: Generic = JsclInteger.valueOf(1)
            val it = IntegerDivisor.create(generic.integerValue())
            while (it.hasNext()) {
                val s = it.next() as Generic
                a = a.multiply(x.subtract(s))
                if (!tail) a = a.multiply(x.add(s))
            }
            return a
        }

        fun expression(generic: Generic, integer: Boolean = false): Generic {
            return if (generic.compareTo(JsclInteger.valueOf(1)) == 0) generic
            else GenericVariable.valueOf(generic, integer).expressionValue()
        }

        fun toString(monomial: Array<Monomial>): String {
            return "{${monomial[0]}, ${monomial[1]}}"
        }
    }
}

class Linearization(var unknown: Array<Variable>) {
    var result: MutableList<Array<Generic>> = mutableListOf()

    fun process(generic: Array<Generic>) {
        var flag = true
        for (i in generic.indices) {
            val s = generic[i]
            val va = s.variables()
            if (va.size == 1) {
                val t = va[0]
                val p = Polynomial.factory(t).valueOf(s)
                if (p.degree() > 1) {
                    flag = false
                    val r = linearize(p, t)
                    for (j in r.indices) {
                        process(Basis.compute(Basis.augment(arrayOf(r[j].genericValue()), generic), unknown).elements())
                    }
                }
            } else flag = false
        }
        if (flag) result.add(generic)
    }

    fun getValue(): Array<Array<Generic>> {
        return ArrayUtils.toArray(result, arrayOfNulls<Array<Generic>>(result.size)) as Array<Array<Generic>>
    }

    companion object {
        fun compute(generic: Array<Generic>, unknown: Array<Variable>): Array<Array<Generic>> {
            val l = Linearization(unknown)
            Debug.println("linearization")
            Debug.increment()
            l.process(generic)
            Debug.decrement()
            return l.getValue()
        }

        fun linearize(polynomial: Polynomial, variable: Variable): Array<Polynomial> {
            val l: MutableList<Polynomial> = mutableListOf()
            val x = variable.expressionValue()
            var s = polynomial
            try {
                val r = s.valueOf(x)
                s = s.divide(r)
                l.add(r)
                while (true) s = s.divide(r)
            } catch (e: NotDivisibleException) {
            }
            val d = arrayOfNulls<IntegerDivisor>(2)
            val p = arrayOfNulls<Generic>(2)
            val q = arrayOfNulls<Generic>(2)
            d[1] = IntegerDivisor.create(JsclInteger.valueOf(1))
            loop@ while (d[1]!!.hasNext()) {
                p[1] = d[1]!!.next() as Generic
                q[1] = d[1]!!.integer(d[1]!!.complementary())
                d[0] = IntegerDivisor.create(s.tail()!!.coef().integerValue())
                while (d[0]!!.hasNext()) {
                    p[0] = d[0]!!.next() as Generic
                    q[0] = d[0]!!.integer(d[0]!!.complementary())
                    @Suppress("UNCHECKED_CAST")
                    if (ArrayComparator.comparator.compare(q as Array<Comparable<*>?>, p as Array<Comparable<*>?>) < 0) break@loop
                    for (i in 0..1) {
                        val r = s.valueOf(if (i == 0) p[1]!!.multiply(x).subtract(p[0]!!) else p[1]!!.multiply(x).add(p[0]!!))
                        var addedToList = false
                        while (true) {
                            try {
                                s = s.divide(r)
                            } catch (e: NotDivisibleException) {
                                break
                            }
                            d[1]!!.divide()
                            d[0]!!.divide()
                            if (!addedToList) {
                                l.add(r)
                                addedToList = true
                            }
                        }
                    }
                }
            }
            return ArrayUtils.toArray(l, arrayOfNulls<Polynomial>(l.size)) as Array<Polynomial>
        }
    }
}

open class Divisor(var monomial: Monomial) : Iterator<Any> {
    var current: Monomial? = null
    var iterator: Iterator<*>

    init {
        iterator = monomial.divisor()
    }

    fun complementary(): Monomial {
        return monomial.divide(current!!)
    }

    fun divide() {
        monomial = complementary()
        iterator = monomial.divisor(current!!)
    }

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun next(): Any {
        current = iterator.next() as Monomial
        return current!!
    }
}

class IntegerDivisor(generic: Generic, unknown: Array<Variable>, ordering: Ordering) :
    Divisor(Polynomial.factory(unknown, ordering).valueOf(generic).head()!!.monomial()) {

    override fun next(): Any {
        return integer(super.next() as Monomial)
    }

    fun integer(monomial: Monomial): Generic {
        return Expression.valueOf(Literal.valueOf(monomial)).expand()
    }

    companion object {
        fun create(integer: JsclInteger): IntegerDivisor {
            val a = Factorization.factorize(integer)
            return IntegerDivisor(a, a.variables(), Monomial.iteratorOrdering)
        }
    }
}
