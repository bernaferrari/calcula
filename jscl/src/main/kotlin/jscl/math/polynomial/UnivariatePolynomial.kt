package jscl.math.polynomial

import jscl.math.*
import jscl.math.function.Inverse
import jscl.util.ArrayUtils
import java.util.*

open class UnivariatePolynomial protected constructor(
    internal val variable: Variable,
    coefFactory: Generic?
) : Polynomial(Monomial.factory(arrayOf(variable)), coefFactory) {

    protected var content: Array<Generic?> = arrayOfNulls(8)
    protected var degree: Int = 0

    constructor(variable: Variable) : this(variable, null)

    fun variable(): Variable {
        return variable
    }

    override fun size(): Int {
        return degree + 1
    }

    override fun iterator(direction: Boolean, current: Monomial?): Iterator<*> {
        return ContentIterator(direction, current)
    }

    internal fun term(n: Int): Term {
        return Term(monomial(Literal.valueOf(variable, n)), get(n))
    }

    internal fun indexOf(monomial: Monomial?, direction: Boolean): Int {
        if (monomial == null) return if (direction) degree + 1 else 0
        return monomial.degree()
    }

    override fun add(that: Polynomial): Polynomial {
        val p = newinstance()
        val q = that as UnivariatePolynomial
        val d = Math.max(degree, q.degree)
        for (i in d downTo 0) {
            p.put(i, get(i).add(q.get(i)))
        }
        return p
    }

    override fun subtract(that: Polynomial): Polynomial {
        val p = newinstance()
        val q = that as UnivariatePolynomial
        val d = Math.max(degree, q.degree)
        for (i in d downTo 0) {
            p.put(i, get(i).subtract(q.get(i)))
        }
        return p
    }

    override fun multiply(that: Polynomial): Polynomial {
        val p = newinstance()
        val q = that as UnivariatePolynomial
        for (i in degree downTo 0) {
            for (j in q.degree downTo 0) {
                p.put(i + j, get(i).multiply(q.get(j)))
            }
        }
        return p
    }

    open override fun multiply(generic: Generic): Polynomial {
        val p = newinstance()
        for (i in degree downTo 0) {
            p.put(i, get(i).multiply(generic))
        }
        return p
    }

    open fun multiply(monomial: Monomial, generic: Generic): Polynomial {
        val p = newinstance()
        val d = monomial.degree()
        for (i in degree downTo 0) {
            p.put(i + d, get(i).multiply(generic))
        }
        for (i in d - 1 downTo 0) {
            p.put(i, JsclInteger.valueOf(0))
        }
        return p
    }

    override fun multiply(monomial: Monomial): Polynomial {
        return multiply(monomial, JsclInteger.valueOf(1))
    }

    override fun divide(generic: Generic): Polynomial {
        val p = newinstance()
        for (i in degree downTo 0) {
            p.put(i, get(i).divide(generic))
        }
        return p
    }

    override fun divide(monomial: Monomial): Polynomial {
        val p = newinstance()
        val d = monomial.degree()
        for (i in d - 1 downTo 0) {
            if (get(i).signum() != 0) throw NotDivisibleException()
        }
        for (i in degree downTo d) {
            p.put(i - d, get(i))
        }
        return p
    }

    override fun divideAndRemainder(polynomial: Polynomial): Array<Polynomial> {
        val p = arrayOf<UnivariatePolynomial>(newinstance(), this)
        val q = polynomial as UnivariatePolynomial
        if (p[1].signum() == 0) return p as Array<Polynomial>
        for (i in p[1].degree - q.degree downTo 0) {
            p[0].put(i, p[1].get(i + q.degree).divide(q.get(q.degree)))
            val r = newinstance()
            for (j in i + q.degree - 1 downTo 0) {
                val a = p[1].get(j)
                r.put(j, a.subtract(q.get(j - i).multiply(p[0].get(i))))
            }
            p[1] = r
        }
        return p as Array<Polynomial>
    }

    override fun remainderUpToCoefficient(polynomial: Polynomial): Polynomial {
        var p: UnivariatePolynomial = this
        val q = polynomial as UnivariatePolynomial
        if (p.signum() == 0) return p
        for (i in p.degree - q.degree downTo 0) {
            val r = newinstance()
            for (j in i + q.degree - 1 downTo 0) {
                val a = p.get(j).multiply(q.get(q.degree))
                r.put(j, a.subtract(q.get(j - i).multiply(p.get(i + q.degree))))
            }
            p = r
        }
        return p
    }

    override fun gcd(polynomial: Polynomial): Polynomial {
        var p: UnivariatePolynomial = this
        var q = polynomial as UnivariatePolynomial
        if (p.signum() == 0) return q
        else if (q.signum() == 0) return p
        if (p.degree < q.degree) {
            val r = p
            p = q
            q = r
        }
        var d = p.degree - q.degree
        var phi: Generic = JsclInteger.valueOf(-1)
        var beta: Generic = JsclInteger.valueOf(-1).pow(d + 1)
        val a1 = p.gcdAndNormalize()
        val a2 = q.gcdAndNormalize()
        val gcd1 = a1[0].genericValue()
        val gcd2 = a2[0].genericValue()
        p = a1[1] as UnivariatePolynomial
        q = a2[1] as UnivariatePolynomial
        while (q.degree > 0) {
            var r = p.remainderUpToCoefficient(q).divide(beta) as UnivariatePolynomial
            phi = if (d > 1) q.get(q.degree).negate().pow(d).divide(phi.pow(d - 1))
            else q.get(q.degree).negate().pow(d).multiply(phi.pow(1 - d))
            p = q
            q = r
            d = p.degree - q.degree
            beta = p.get(p.degree).negate().multiply(phi.pow(d))
        }
        if (q.signum() == 0) {
            p = p.normalize() as UnivariatePolynomial
        } else {
            p = newinstance()
            p.put(0, JsclInteger.valueOf(1))
        }
        return p.multiply(gcd1.gcd(gcd2))
    }

    override fun gcd(): Generic {
        var a = coefficient(JsclInteger.valueOf(0))
        for (i in degree downTo 0) a = a.gcd(get(i))
        return if (a.signum() == signum()) a else a.negate()
    }

    override fun monomialGcd(): Monomial {
        return monomial(tail())
    }

    override fun degree(): Int {
        return degree
    }

    fun valueof(generic: Array<Generic>): UnivariatePolynomial {
        val p = newinstance()
        p.init(generic)
        return p
    }

    override fun valueOf(polynomial: Polynomial): Polynomial {
        return valueOf(polynomial.genericValue())
    }

    override fun valueOf(generic: Generic): Polynomial {
        val p = newinstance()
        p.init(generic)
        return p
    }

    override fun valueOf(monomial: Monomial): Polynomial {
        throw UnsupportedOperationException()
    }

    override fun freeze(): Polynomial {
        return this
    }

    override fun head(): Term {
        return term(degree)
    }

    override fun coefficient(monomial: Monomial): Generic {
        return term(monomial.degree()).coef()
    }

    fun reduce(generic: Generic, monomial: Monomial, polynomial: Polynomial, inPlace: Boolean): Polynomial {
        throw UnsupportedOperationException()
    }

    override fun genericValue(): Generic {
        var s: Generic = JsclInteger.valueOf(0)
        for (i in degree downTo 0) {
            val a = get(i).expressionValue()
            s = s.add(if (i > 0) a.multiply(Expression.valueOf(Literal.valueOf(variable, i))) else a)
        }
        return s
    }

    override fun elements(): Array<Generic> {
        val a = arrayOfNulls<Generic>(degree + 1)
        for (i in degree downTo 0) a[i] = get(i)
        @Suppress("UNCHECKED_CAST")
        return a as Array<Generic>
    }

    fun derivative(variable: Variable): UnivariatePolynomial {
        return derivative().multiply(this.variable.derivative(variable)) as UnivariatePolynomial
    }

    fun substitute(generic: Generic): Generic {
        var s: Generic = JsclInteger.valueOf(0)
        for (i in degree downTo 0) {
            s = s.add(get(i).multiply(generic.pow(i)))
        }
        return s
    }

    fun solve(): Generic? {
        return if (degree == 1) {
            get(0).multiply(Inverse(get(1)).selfExpand()).negate()
        } else null
    }

    fun identification(polynomial: UnivariatePolynomial): Array<Generic> {
        var p: UnivariatePolynomial = this
        var q = polynomial
        if (p.degree < q.degree || (p.degree == 0 && q.signum() == 0)) {
            val r = p
            p = q
            q = r
        }
        val r = p.remainderUpToCoefficient(q) as UnivariatePolynomial
        val a = arrayOfNulls<Generic>(r.degree + 1)
        for (i in r.degree downTo 0) a[r.degree - i] = r.get(i)
        @Suppress("UNCHECKED_CAST")
        return a as Array<Generic>
    }

    fun resultant(polynomial: UnivariatePolynomial): Generic {
        var p: UnivariatePolynomial = this
        var q = polynomial
        if (p.degree < q.degree || (p.degree == 0 && q.signum() == 0)) {
            val r = p
            p = q
            q = r
        }
        var d = p.degree - q.degree
        var phi: Generic = JsclInteger.valueOf(-1)
        var beta: Generic = JsclInteger.valueOf(-1).pow(d + 1)
        while (q.degree > 0) {
            val r = p.remainderUpToCoefficient(q).divide(beta) as UnivariatePolynomial
            phi = if (d > 1) q.get(q.degree).negate().pow(d).divide(phi.pow(d - 1))
            else q.get(q.degree).negate().pow(d).multiply(phi.pow(1 - d))
            p = q
            q = r
            d = p.degree - q.degree
            beta = p.get(p.degree).negate().multiply(phi.pow(d))
        }
        return q.get(0)
    }

    fun remainderSequence(polynomial: UnivariatePolynomial): Array<UnivariatePolynomial> {
        var p: UnivariatePolynomial = this
        var q = polynomial
        if (p.degree < q.degree || (p.degree == 0 && q.signum() == 0)) {
            val r = p
            p = q
            q = r
        }
        val s = arrayOfNulls<UnivariatePolynomial>(q.degree + 1)
        s[q.degree] = q
        var d = p.degree - q.degree
        var phi: Generic = JsclInteger.valueOf(-1)
        var beta: Generic = JsclInteger.valueOf(-1).pow(d + 1)
        while (q.degree > 0) {
            val r = p.remainderUpToCoefficient(q).divide(beta) as UnivariatePolynomial
            phi = if (d > 1) q.get(q.degree).negate().pow(d).divide(phi.pow(d - 1))
            else q.get(q.degree).negate().pow(d).multiply(phi.pow(1 - d))
            p = q
            q = r
            s[q.degree] = q
            d = p.degree - q.degree
            beta = p.get(p.degree).negate().multiply(phi.pow(d))
        }
        @Suppress("UNCHECKED_CAST")
        return s as Array<UnivariatePolynomial>
    }

    fun squarefree(): UnivariatePolynomial {
        return divide(gcd(derivative())) as UnivariatePolynomial
    }

    fun squarefreeDecomposition(): Array<UnivariatePolynomial> {
        return SquarefreeDecomposition.compute(this)
    }

    fun antiderivative(): UnivariatePolynomial {
        val p = newinstance()
        for (i in degree downTo 0) {
            p.put(i + 1, get(i).multiply(Inverse(JsclInteger.valueOf((i + 1).toLong())).selfExpand()))
        }
        return p
    }

    fun derivative(): UnivariatePolynomial {
        val p = newinstance()
        for (i in degree - 1 downTo 0) {
            p.put(i, get(i + 1).multiply(JsclInteger.valueOf((i + 1).toLong())))
        }
        return p
    }

    override fun compareTo(other: Polynomial): Int {
        val p = other as UnivariatePolynomial
        val d = Math.max(degree, p.degree)
        for (i in d downTo 0) {
            val a1 = get(i)
            val a2 = p.get(i)
            val c = a1.compareTo(a2)
            if (c < 0) return -1
            else if (c > 0) return 1
        }
        return 0
    }

    internal fun init(generic: Array<Generic>) {
        for (i in generic.indices) put(i, coefficient(generic[i]))
    }

    internal fun init(expression: Expression) {
        val n = expression.size()
        for (i in 0 until n) {
            val l = expression.literal(i)
            val en = expression.coef(i)
            val m = monomial(l)
            val l2 = l.divide(m.literalValue())
            if (l2.degree() > 0) put(m.degree(), coefficient(en.multiply(Expression.valueOf(l2))))
            else put(m.degree(), coefficient(en))
        }
    }

    protected fun init(generic: Generic) {
        if (generic is Expression) {
            init(generic)
        } else put(0, coefficient(generic))
    }

    internal fun put(n: Int, generic: Generic) {
        val a = generic.add(get(n))
        if (a.signum() == 0) {
            if (n <= degree) content[n] = null
            if (n == degree) {
                var newDegree = n
                while (newDegree > 0 && content[newDegree] == null) newDegree--
                degree = newDegree
            }
        } else {
            if (n >= content.size) resize(n)
            content[n] = a
            degree = Math.max(degree, n)
        }
    }

    internal fun resize(n: Int) {
        var length = content.size shl 1
        while (n >= length) length = length shl 1
        val newContent = arrayOfNulls<Generic>(length)
        System.arraycopy(content, 0, newContent, 0, content.size)
        content = newContent
    }

    operator fun get(n: Int): Generic {
        val a = if (n < 0 || n > degree) null else content[n]
        return a ?: JsclInteger.valueOf(0)
    }

    internal open fun newinstance(): UnivariatePolynomial {
        return UnivariatePolynomial(variable, coefFactory)
    }

    internal inner class ContentIterator(private val direction: Boolean, current: Monomial?) : Iterator<Any> {
        private var index: Int

        init {
            index = if (direction) {
                indexOf(current, true)
            } else {
                val idx = indexOf(current, false)
                if (current != null && get(idx).signum() != 0) idx + 1 else idx
            }
            seek()
        }

        internal fun seek() {
            if (direction) {
                while (index > 0 && get(index).signum() == 0) index--
            } else {
                while (index <= degree && get(index).signum() == 0) index++
            }
        }

        override fun hasNext(): Boolean {
            return if (direction) index > 0 else index <= degree
        }

        override fun next(): Any {
            val t = if (direction) term(--index) else term(index++)
            seek()
            return t
        }
    }
}

internal class SquarefreeDecomposition {
    private val list: MutableList<UnivariatePolynomial> = mutableListOf()

    fun init(polynomial: UnivariatePolynomial) {
        list.add(polynomial)
    }

    fun process(polynomial: UnivariatePolynomial) {
        val r = polynomial.gcd(polynomial.derivative()) as UnivariatePolynomial
        val s = polynomial.divide(r) as UnivariatePolynomial
        list.add(s.divide(s.gcd(r)) as UnivariatePolynomial)
        if (r.degree() == 0) {
            // done
        } else process(r)
    }

    fun getValue(): Array<UnivariatePolynomial> {
        @Suppress("UNCHECKED_CAST")
        return ArrayUtils.toArray(list, arrayOfNulls<UnivariatePolynomial>(list.size)) as Array<UnivariatePolynomial>
    }

    companion object {
        @JvmStatic
        fun compute(polynomial: UnivariatePolynomial): Array<UnivariatePolynomial> {
            val sd = SquarefreeDecomposition()
            val p = polynomial.gcdAndNormalize()
            sd.init(p[0] as UnivariatePolynomial)
            sd.process(p[1] as UnivariatePolynomial)
            return sd.getValue()
        }

        @JvmStatic
        fun factory(variable: Variable): UnivariatePolynomial {
            return UnivariatePolynomial(variable)
        }
    }
}
