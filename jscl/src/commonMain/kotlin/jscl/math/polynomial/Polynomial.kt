package jscl.math.polynomial

import jscl.math.*
import jscl.math.function.Constant
import jscl.mathml.MathML

abstract class Polynomial internal constructor(
    internal val monomialFactory: Monomial,
    internal val coefFactory: Generic?
) : Arithmetic<Polynomial>, Comparable<Polynomial> {

    internal val ordering: Ordering = monomialFactory.ordering
    internal val defined: Boolean = monomialFactory is DefinedBooleanMonomial
    internal val field: Boolean = coefFactory is Field
    internal var normalized: Boolean = false
    internal var sugar: Int = 0
    internal var index: Int = -1

    abstract fun size(): Int

    fun ordering(): Ordering {
        return ordering
    }

    fun iterator(): Iterator<*> {
        return iterator(false)
    }

    fun iterator(direction: Boolean): Iterator<*> {
        return iterator(direction, null)
    }

    fun iterator(current: Monomial): Iterator<*> {
        return iterator(true, current)
    }

    abstract fun iterator(direction: Boolean, current: Monomial?): Iterator<*>

    override fun add(that: Polynomial): Polynomial {
        return multiplyAndSubtract(coefficient(JsclInteger.valueOf(-1)), that)
    }

    abstract override fun subtract(that: Polynomial): Polynomial

    open fun multiplyAndSubtract(generic: Generic, polynomial: Polynomial): Polynomial {
        return subtract(polynomial.multiply(generic))
    }

    open fun multiplyAndSubtract(monomial: Monomial, generic: Generic, polynomial: Polynomial): Polynomial {
        return subtract(polynomial.multiply(monomial).multiply(generic))
    }

    override fun multiply(that: Polynomial): Polynomial {
        var p: Polynomial = valueOf(JsclInteger.valueOf(0))
        val it = iterator()
        while (it.hasNext()) {
            val t = it.next() as Term
            p = p.multiplyAndSubtract(t.monomial(), t.coef().negate(), that)
        }
        return p
    }

    abstract fun multiply(generic: Generic): Polynomial

    abstract fun multiply(monomial: Monomial): Polynomial

    fun multiple(polynomial: Polynomial): Boolean {
        return remainder(polynomial).signum() == 0
    }

    override fun divide(that: Polynomial): Polynomial {
        val p = divideAndRemainder(that)
        if (p[1].signum() == 0) return p[0]
        else throw NotDivisibleException()
    }

    abstract fun divide(generic: Generic): Polynomial

    abstract fun divide(monomial: Monomial): Polynomial

    open fun divideAndRemainder(polynomial: Polynomial): Array<Polynomial> {
        val p = arrayOf(valueOf(JsclInteger.valueOf(0)), this)
        val q = polynomial
        var it = p[1].iterator(true)
        while (it.hasNext()) {
            val t = it.next() as Term
            val m1 = t.monomial()
            val m2 = q.head()!!.monomial()
            if (m1.multiple(m2)) {
                val m = m1.divide(m2)
                val c1 = t.coef()
                val c2 = q.head()!!.coef()
                val c = c1.divide(c2)
                p[0] = p[0].multiplyAndSubtract(m, c, valueOf(JsclInteger.valueOf(-1)))
                p[1] = p[1].multiplyAndSubtract(m, c, q)
                it = p[1].iterator(true)
            }
        }
        return p
    }

    fun remainder(polynomial: Polynomial): Polynomial {
        return divideAndRemainder(polynomial)[1]
    }

    open fun remainderUpToCoefficient(polynomial: Polynomial): Polynomial {
        var p: Polynomial = this
        val q = polynomial
        var it = p.iterator(true)
        while (it.hasNext()) {
            val t = it.next() as Term
            val m1 = t.monomial()
            val m2 = q.head()!!.monomial()
            if (m1.multiple(m2)) {
                val m = m1.divide(m2)
                val c1 = t.coef()
                val c2 = q.head()!!.coef()
                p = p.multiply(c2).multiplyAndSubtract(m, c1, q)
                it = p.iterator(true)
            }
        }
        return p
    }

    abstract fun gcd(polynomial: Polynomial): Polynomial

    fun scm(polynomial: Polynomial): Polynomial {
        return divide(gcd(polynomial)).multiply(polynomial)
    }

    open fun gcd(): Generic {
        if (field) return coefficient(tail())
        var a = coefficient(JsclInteger.valueOf(0))
        val it = iterator()
        while (it.hasNext()) a = a.gcd((it.next() as Term).coef())
        return if (a.signum() == signum()) a else a.negate()
    }

    fun gcdAndNormalize(): Array<Polynomial> {
        val gcd = gcd()
        return arrayOf(valueOf(gcd), if (gcd.signum() == 0) this else divide(gcd))
    }

    fun normalize(): Polynomial {
        if (normalized) return this
        else {
            val p = gcdAndNormalize()[1]
            p.normalized = true
            return p
        }
    }

    open fun monomialGcd(): Monomial {
        var m = monomial(tail())
        val it = iterator()
        while (it.hasNext()) m = m.gcd((it.next() as Term).monomial())
        return m
    }

    fun pow(exponent: Int): Polynomial {
        var a: Polynomial = valueOf(JsclInteger.valueOf(1))
        for (i in 0 until exponent) a = a.multiply(this)
        return a
    }

    fun abs(): Polynomial {
        return if (signum() < 0) negate() else this
    }

    fun negate(): Polynomial {
        return multiply(coefficient(JsclInteger.valueOf(-1)))
    }

    fun signum(): Int {
        return coefficient(tail()).signum()
    }

    abstract fun degree(): Int

    abstract fun valueOf(polynomial: Polynomial): Polynomial

    abstract fun valueOf(generic: Generic): Polynomial

    abstract fun valueOf(monomial: Monomial): Polynomial

    fun copy(): Polynomial {
        return valueOf(this)
    }

    abstract fun freeze(): Polynomial

    open fun head(): Term? {
        val it = iterator(true)
        return if (it.hasNext()) it.next() as Term else null
    }

    open fun tail(): Term? {
        val it = iterator()
        return if (it.hasNext()) it.next() as Term else null
    }

    open fun coefficient(monomial: Monomial): Generic {
        val it = iterator(false, monomial)
        val t = if (it.hasNext()) it.next() as Term? else null
        return coefficient(if (t == null || ordering.compare(t.monomial(), monomial) == 0) t else null)
    }

    internal fun monomial(term: Term?): Monomial {
        return term?.monomial() ?: monomial(Literal.newInstance())
    }

    internal fun coefficient(term: Term?): Generic {
        return term?.coef() ?: coefficient(JsclInteger.valueOf(0))
    }

    protected fun monomial(literal: Literal): Monomial {
        return monomialFactory.valueof(literal)
    }

    protected open fun coefficient(generic: Generic): Generic {
        return coefFactory?.valueOf(generic) ?: generic
    }

    fun reduce(ideal: Collection<*>, tail: Boolean): Polynomial {
        var p: Polynomial = this
        var localTail = tail
        var it = if (localTail) p.iterator(p.head()!!.monomial()) else p.iterator(true)
        loop@ while (it.hasNext()) {
            val t = it.next() as Term
            val m1 = t.monomial()
            val iq = ideal.iterator()
            while (iq.hasNext()) {
                val q = iq.next() as Polynomial
                val m2 = q.head()!!.monomial()
                if (m1.multiple(m2)) {
                    val m = m1.divide(m2)
                    p = p.reduce(t.coef(), m, q)
                    it = if (localTail) p.iterator(m1) else p.iterator(true)
                    continue@loop
                }
            }
            localTail = true
        }
        return p
    }

    fun reduce(generic: Generic, monomial: Monomial, polynomial: Polynomial): Polynomial {
        return if (field) {
            multiplyAndSubtract(monomial, generic.divide(polynomial.head()!!.coef()), polynomial)
        } else {
            var c1 = generic
            var c2 = polynomial.head()!!.coef()
            val c = c1.gcd(c2)
            c1 = c1.divide(c)
            c2 = c2.divide(c)
            multiply(c2).multiplyAndSubtract(monomial, c1, polynomial).normalize()
        }
    }

    fun reduce(generic: Generic, polynomial: Polynomial): Polynomial {
        return reduce(generic, monomial(Literal.newInstance()), polynomial)
    }

    open fun sugar(): Int {
        return sugar
    }

    open fun index(): Int {
        return index
    }

    open fun setSugar(n: Int) {
        sugar = n
    }

    open fun setIndex(n: Int) {
        if (index != -1) throw ArithmeticException()
        index = n
    }

    open fun genericValue(): Generic {
        var s: Generic = JsclInteger.valueOf(0)
        val it = iterator()
        while (it.hasNext()) {
            val t = it.next() as Term
            val m = t.monomial()
            val a = t.coef().expressionValue()
            s = s.add(if (m.degree() > 0) a.multiply(Expression.valueOf(m.literalValue())) else a)
        }
        return s
    }

    open fun elements(): Array<Generic> {
        val size = size()
        val a = arrayOfNulls<Generic>(size)
        val it = iterator()
        for (i in 0 until size) a[i] = (it.next() as Term).coef()
        @Suppress("UNCHECKED_CAST")
        return a as Array<Generic>
    }

    override fun compareTo(other: Polynomial): Int {
        var it1 = iterator(true)
        var it2 = other.iterator(true)
        var t1: Term? = if (it1.hasNext()) it1.next() as Term else null
        var t2: Term? = if (it2.hasNext()) it2.next() as Term else null
        while (t1 != null || t2 != null) {
            var c = when {
                t1 == null -> 1
                t2 == null -> -1
                else -> ordering.compare(t1.monomial(), t2.monomial())
            }
            if (c < 0) return -1
            else if (c > 0) return 1
            else {
                c = t1!!.coef().compareTo(t2!!.coef())
                if (c < 0) return -1
                else if (c > 0) return 1
                t1 = if (it1.hasNext()) it1.next() as Term else null
                t2 = if (it2.hasNext()) it2.next() as Term else null
            }
        }
        return 0
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Polynomial) {
            compareTo(other) == 0
        } else false
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        if (signum() == 0) buffer.append("0")
        var i = 0
        val it = iterator()
        while (it.hasNext()) {
            val t = it.next() as Term
            val m = t.monomial()
            var a = t.coef()
            if (a is Expression)
                a = if (a.signum() > 0) GenericVariable.valueOf(a).expressionValue() else GenericVariable.valueOf(a.negate()).expressionValue().negate()
            if (a.signum() > 0 && i > 0) buffer.append("+")
            if (m.degree() == 0) buffer.append(a)
            else {
                if (a.abs().compareTo(JsclInteger.valueOf(1)) == 0) {
                    if (a.signum() < 0) buffer.append("-")
                } else buffer.append(a).append("*")
                buffer.append(m)
            }
            i++
        }
        return buffer.toString()
    }

    open fun toMathML(element: MathML, data: Any?) {
        val e1 = element.element("mrow")
        if (signum() == 0) {
            val e2 = element.element("mn")
            e2.appendChild(element.text("0"))
            e1.appendChild(e2)
        }
        var i = 0
        val it = iterator()
        while (it.hasNext()) {
            val t = it.next() as Term
            val m = t.monomial()
            var a = t.coef()
            if (a is Expression)
                a = if (a.signum() > 0) GenericVariable.valueOf(a).expressionValue() else GenericVariable.valueOf(a.negate()).expressionValue().negate()
            if (a.signum() > 0 && i > 0) {
                val e2 = element.element("mo")
                e2.appendChild(element.text("+"))
                e1.appendChild(e2)
            }
            if (m.degree() == 0) Expression.separateSign(e1, a)
            else {
                if (a.abs().compareTo(JsclInteger.valueOf(1)) == 0) {
                    if (a.signum() < 0) {
                        val e2 = element.element("mo")
                        e2.appendChild(element.text("-"))
                        e1.appendChild(e2)
                    }
                } else Expression.separateSign(e1, a)
                m.toMathML(e1, null)
            }
            i++
        }
        element.appendChild(e1)
    }

    open fun getConstants(): Set<Constant> {
        return emptySet()
    }

    companion object {
        internal fun degree(polynomial: Polynomial): Int {
            return polynomial.monomial(polynomial.head()).degree()
        }

        fun factory(variable: Variable): Polynomial {
            return UnivariatePolynomial(variable)
        }

        fun factory(variable: Array<Variable>): Polynomial {
            return NestedPolynomial(variable)
        }

        fun factory(unknown: Array<Variable>, ordering: Ordering): Polynomial {
            return factory(unknown, ordering, 0)
        }

        fun factory(unknown: Array<Variable>, ordering: Ordering, modulo: Int): Polynomial {
            return factory(unknown, ordering, modulo, 0)
        }

        fun factory(unknown: Array<Variable>, ordering: Ordering, modulo: Int, flags: Int): Polynomial {
            return factory(Monomial.factory(unknown, ordering, flags and Basis.POWER_SIZE), modulo, flags and Basis.DATA_STRUCT, (flags and Basis.GEO_BUCKETS) > 0)
        }

        internal fun factory(monomialFactory: Monomial, modulo: Int, data_struct: Int, buckets: Boolean): Polynomial {
            if (buckets) return GeoBucket(factory(monomialFactory, modulo, data_struct, false))
            else return when (data_struct) {
                Basis.ARRAY -> ArrayPolynomial(monomialFactory, generic(modulo)!!)
                Basis.TREE -> TreePolynomial(monomialFactory, generic(modulo)!!)
                Basis.LIST -> ListPolynomial(monomialFactory, generic(modulo)!!)
                else -> when (modulo) {
                    -1 -> ArrayPolynomialGeneric(monomialFactory, generic(modulo))
                    0 -> ArrayPolynomialInteger(monomialFactory)
                    1 -> ArrayPolynomialRational(monomialFactory)
                    2 -> ArrayPolynomialBoolean(monomialFactory)
                    else -> ArrayPolynomialModular(monomialFactory, ModularInteger.factory(modulo))
                }
            }
        }

        internal fun generic(modulo: Int): Generic? {
            return when (modulo) {
                -1 -> null
                0 -> JsclInteger.factory
                1 -> Rational.factory
                2 -> JsclBoolean.factory
                else -> ModularInteger.factory(modulo)
            }
        }

        internal fun factory(polynomial: Polynomial, modulo: Int): Polynomial {
            val m = polynomial.monomialFactory
            return factory(m.unknown, m.ordering, modulo)
        }
    }
}
