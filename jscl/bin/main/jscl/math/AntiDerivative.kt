package jscl.math

import jscl.math.function.*
import jscl.math.polynomial.Monomial
import jscl.math.polynomial.Polynomial
import jscl.math.polynomial.UnivariatePolynomial

class AntiDerivative internal constructor(variable: Variable) {
    var factory: UnivariatePolynomial
    var syzygy: PolynomialWithSyzygy
    var result: Generic? = null

    init {
        factory = Polynomial.factory(variable) as UnivariatePolynomial
        syzygy = PolynomialWithSyzygy.factory(variable) as PolynomialWithSyzygy
    }

    fun compute(fraction: Fraction) {
        Debug.println("antiDerivative")
        Debug.increment()
        val g = fraction.getParameters()
        var r = reduce(g!![0], g[1])
        r = divideAndRemainder(r[0], r[1])
        val s = Inverse(r[2]).selfExpand()
        val p = r[0].multiply(s)
        val a = r[1].multiply(s)
        result = p.antiDerivative(factory.variable).add(hermite(a, g[1]))
        Debug.decrement()
    }

    fun reduce(n: Generic, d: Generic): Array<Generic> {
        Debug.println("reduce($n, $d)")
        val pn = factory.valueOf(n)
        val pd = factory.valueOf(d)
        val gcd = pn.gcd(pd)
        return arrayOf(
            pn.divide(gcd).genericValue(),
            pd.divide(gcd).genericValue()
        )
    }

    fun divideAndRemainder(n: Generic, d: Generic): Array<Generic> {
        Debug.println("divideAndRemainder($n, $d)")
        val pn = syzygy.valueof(n, 0)
        val pd = syzygy.valueof(d, 1)
        val pr = pn.remainderUpToCoefficient(pd) as PolynomialWithSyzygy
        return arrayOf(
            pr.syzygy[1]!!.genericValue().negate(),
            pr.genericValue(),
            pr.syzygy[0]!!.genericValue()
        )
    }

    fun bezout(a: Generic, b: Generic): Array<Generic> {
        Debug.println("bezout($a, $b)")
        val pa = syzygy.valueof(a, 0)
        val pb = syzygy.valueof(b, 1)
        val gcd = pa.gcd(pb) as PolynomialWithSyzygy
        return arrayOf(
            gcd.syzygy[0]!!.genericValue(),
            gcd.syzygy[1]!!.genericValue(),
            gcd.genericValue()
        )
    }

    fun hermite(a: Generic, d: Generic): Generic {
        Debug.println("hermite($a, $d)")
        val sd = (factory.valueOf(d) as UnivariatePolynomial).squarefreeDecomposition()
        val m = sd.size - 1
        if (m < 2) return trager(a, d)
        else {
            var u = sd[0].genericValue()
            for (i in 1 until m) {
                u = u.multiply(sd[i].genericValue().pow(i))
            }
            val v = sd[m].genericValue()
            val vprime = sd[m].derivative().genericValue()
            val uvprime = u.multiply(vprime)
            var r = bezout(uvprime, v)
            var bVar = r[0].multiply(a)
            var c = r[1].multiply(a)
            var s = r[2]
            r = divideAndRemainder(bVar, v)
            bVar = r[1]
            c = c.multiply(r[2]).add(r[0].multiply(uvprime))
            s = Inverse(s.multiply(r[2]).multiply(JsclInteger.valueOf((1 - m).toLong()))).selfExpand()
            bVar = bVar.multiply(s)
            c = c.multiply(s)
            val bprime = (factory.valueOf(bVar) as UnivariatePolynomial).derivative().genericValue()
            return Fraction(bVar, v.pow(m - 1)).selfExpand().add(
                hermite(
                    JsclInteger.valueOf((1 - m).toLong()).multiply(c).subtract(u.multiply(bprime)),
                    u.multiply(v.pow(m - 1))
                )
            )
        }
    }

    fun trager(a: Generic, d: Generic): Generic {
        Debug.println("trager($a, $d)")
        val t: Variable = TechnicalVariable("t")
        val pd = factory.valueOf(d) as UnivariatePolynomial
        val pa = factory.valueOf(a).subtract(pd.derivative().multiply(t.expressionValue())) as UnivariatePolynomial
        val rs = pd.remainderSequence(pa)
        val fact = Polynomial.factory(t) as UnivariatePolynomial
        for (i in rs.indices) {
            val value = rs[i]
            rs[i] = fact.valueOf(if (i > 0) value.normalize() else value) as UnivariatePolynomial
        }
        val q = rs[0].squarefreeDecomposition()
        val m = q.size - 1
        var s: Generic = JsclInteger.valueOf(0)
        for (i in 1..m) {
            for (j in 0 until q[i].degree()) {
                val a2 = Root(q[i], j).selfExpand()
                s = s.add(a2.multiply(Ln(if (i == pd.degree()) d else rs[i].substitute(a2)).selfExpand()))
            }
        }
        return s
    }

    fun getValue(): Generic = result!!

    companion object {
        @JvmStatic
        fun compute(fraction: Fraction, variable: Variable): Generic {
            val s = AntiDerivative(variable)
            s.compute(fraction)
            return s.getValue()
        }

        @JvmStatic
        @Throws(NotIntegrableException::class)
        fun compute(root: Root, variable: Variable): Generic {
            val d = root.degree()
            val a = root.getParameters()
            var b = d > 0
            b = b && a!![0].negate().isIdentity(variable)
            for (i in 1 until d) b = b && a!![i].signum() == 0
            b = b && a!![d].compareTo(JsclInteger.valueOf(1)) == 0
            if (b) {
                return Pow(
                    a!![0].negate(),
                    Inverse(JsclInteger.valueOf(d.toLong())).selfExpand()
                ).antiDerivative(0)
            } else {
                throw NotIntegrableException()
            }
        }
    }
}

class PolynomialWithSyzygy(variable: Variable) : UnivariatePolynomial(variable) {
    @JvmField
    var syzygy: Array<Polynomial?> = arrayOfNulls(2)

    override fun subtract(that: Polynomial): Polynomial {
        val p2 = that as PolynomialWithSyzygy
        val p = super.subtract(p2) as PolynomialWithSyzygy
        for (i in syzygy.indices) p.syzygy[i] = syzygy[i]!!.subtract(p2.syzygy[i]!!)
        return p
    }

    override fun multiply(generic: Generic): Polynomial {
        val p = super.multiply(generic) as PolynomialWithSyzygy
        for (i in syzygy.indices) p.syzygy[i] = syzygy[i]!!.multiply(generic)
        return p
    }

    override fun multiply(monomial: Monomial, generic: Generic): Polynomial {
        val p = super.multiply(monomial, generic) as PolynomialWithSyzygy
        for (i in syzygy.indices) p.syzygy[i] = syzygy[i]!!.multiply(monomial).multiply(generic)
        return p
    }

    override fun divide(generic: Generic): Polynomial {
        val p = super.divide(generic) as PolynomialWithSyzygy
        for (i in syzygy.indices) p.syzygy[i] = syzygy[i]!!.divide(generic)
        return p
    }

    override fun remainderUpToCoefficient(polynomial: Polynomial): Polynomial {
        var p = this
        val q = polynomial as PolynomialWithSyzygy
        if (p.signum() == 0) return p
        val d = p.degree()
        val d2 = q.degree()
        for (i in d - d2 downTo 0) {
            var c1 = p[i + d2]
            var c2 = q[d2]
            val c = c1.gcd(c2)
            c1 = c1.divide(c)
            c2 = c2.divide(c)
            p = p.multiply(c2).subtract(q.multiply(monomial(Literal.valueOf(variable, i)), c1)).normalize() as PolynomialWithSyzygy
        }
        return p
    }

    override fun gcd(polynomial: Polynomial): Polynomial {
        var p: Polynomial = this
        var q = polynomial
        while (q.signum() != 0) {
            val r = p.remainderUpToCoefficient(q)
            p = q
            q = r
        }
        return p
    }

    override fun gcd(): Generic {
        var a = super.gcd()
        for (i in syzygy.indices) a = a.gcd(syzygy[i]!!.gcd())
        return if (a.signum() == signum()) a else a.negate()
    }

    fun valueof(generic: Generic, n: Int): PolynomialWithSyzygy {
        val p = newinstance() as PolynomialWithSyzygy
        p.init(generic, n)
        return p
    }

    fun init(generic: Generic, n: Int) {
        init(generic)
        for (i in syzygy.indices) {
            syzygy[i] = Polynomial.factory(variable).valueOf(JsclInteger.valueOf(if (i == n) 1 else 0))
        }
    }

    override fun newinstance(): UnivariatePolynomial {
        return PolynomialWithSyzygy(variable)
    }

    companion object {
        @JvmStatic
        fun factory(variable: Variable): Polynomial {
            return PolynomialWithSyzygy(variable)
        }
    }
}
