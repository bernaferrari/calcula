package jscl.math.function

import jscl.math.*
import jscl.math.polynomial.Polynomial
import jscl.math.polynomial.UnivariatePolynomial
import jscl.mathml.MathML
import jscl.util.ArrayComparator

open class Root : Algebraic {

    protected var subscript: Generic

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    constructor(parameters: Array<Generic>?, subscript: Generic?) : super("root", parameters) {
        this.subscript = subscript ?: JsclInteger.valueOf(0)
    }

    constructor(parameters: Array<Generic>?, s: Int) : this(parameters, JsclInteger.valueOf(s.toLong()))

    constructor(polynomial: UnivariatePolynomial, s: Int) : this(polynomial.normalize().elements(), s)

    override fun getMaxParameters(): Int {
        return Int.MAX_VALUE
    }

    fun subscript(): Generic = subscript

    override fun rootValue(): Root = this

    override fun antiDerivative(variable: Variable): Generic {
        var polynomial = true
        for (parameter in parameters!!) {
            polynomial = parameter.isPolynomial(variable)
            if (!polynomial) {
                break
            }
        }

        return if (polynomial) {
            AntiDerivative.compute(this, variable)
        } else {
            throw NotIntegrableException(this)
        }
    }

    override fun derivative(variable: Variable): Generic {
        return if (compareTo(variable) == 0) {
            JsclInteger.valueOf(1)
        } else {
            val t = TechnicalVariable("t")
            val a = Array(parameters!!.size) { i -> parameters!![i].derivative(variable) }
            val fact = Polynomial.factory(this) as UnivariatePolynomial
            val p = fact.valueof(parameters!!)
            val q = p.derivative().multiply(t.expressionValue()).add(fact.valueof(a)) as UnivariatePolynomial
            val r = Polynomial.factory(t).valueOf(p.resultant(q)) as UnivariatePolynomial
            Root(r.elements(), subscript).selfExpand()
        }
    }

    override fun derivative(n: Int): Generic {
        throw NotRootException()
    }

    override fun substitute(variable: Variable, generic: Generic): Generic {
        val v = newInstance() as Root
        for (i in parameters!!.indices) {
            v.parameters!![i] = parameters!![i].substitute(variable, generic)
        }
        v.subscript = subscript.substitute(variable, generic)
        return if (v.isIdentity(variable)) generic else v.selfExpand()
    }

    override fun selfExpand(): Generic {
        if (isZero()) return JsclInteger.valueOf(0)
        try {
            val s = subscript.integerValue().toInt()
            when (degree()) {
                1 -> return Fraction(parameters!![0], parameters!![1]).selfExpand().negate()
            }
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return selfExpand()
    }

    override fun selfSimplify(): Generic {
        if (isZero()) return JsclInteger.valueOf(0)
        try {
            val s = subscript.integerValue().toInt()
            when (degree()) {
                1 -> return linear(parameters!!)
                2 -> return quadratic(parameters!!, s)
                3 -> return cubic(parameters!!, s)
                4 -> return quartic(parameters!!, s)
                else -> if (isNth() && s == 0) return nth(parameters!!)
            }
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    private fun isZero(): Boolean {
        var b = degree() > 0
        for (i in 0 until degree()) b = b && parameters!![i].signum() == 0
        b = b && parameters!![degree()].signum() != 0
        return b
    }

    private fun isNth(): Boolean {
        var b = degree() > 0
        for (i in 1 until degree()) b = b && parameters!![i].signum() == 0
        b = b && parameters!![degree()].signum() != 0
        return b
    }

    fun degree(): Int {
        return parameters!!.size - 1
    }

    override fun selfNumeric(): Generic {
        return NumericWrapper.root(subscript.integerValue().toInt(), parameters!!)
    }

    override fun compareTo(variable: Variable): Int {
        if (this === variable) return 0
        var c = comparator.compare(this, variable)
        if (c < 0) return -1
        else if (c > 0) return 1
        else {
            val v = variable as Root
            @Suppress("UNCHECKED_CAST")
            c = ArrayComparator.comparator.compare(parameters as Array<Comparable<*>?>, v.parameters as Array<Comparable<*>?>)
            if (c < 0) return -1
            else if (c > 0) return 1
            else return subscript.compareTo(v.subscript)
        }
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(name)
        buffer.append("[").append(subscript).append("]")
        buffer.append("(")
        for (i in parameters!!.indices) {
            buffer.append(parameters!![i]).append(if (i < parameters!!.size - 1) ", " else "")
        }
        buffer.append(")")
        return buffer.toString()
    }

    override fun toJava(): String {
        val buffer = StringBuffer()
        buffer.append("Numeric.").append(name).append("(")
        buffer.append(subscript.integerValue().toInt())
        buffer.append(", new Numeric[] {")
        for (i in parameters!!.indices) {
            buffer.append(parameters!![i].toJava()).append(if (i < parameters!!.size - 1) ", " else "")
        }
        buffer.append("})")
        return buffer.toString()
    }

    override fun toMathML(element: MathML, data: Any?) {
        val e1: MathML
        val exponent = if (data is Int) data else 1
        if (exponent == 1) {
            e1 = element.element("msub")
            nameToMathML(e1)
            subscript.toMathML(e1, null)
            element.appendChild(e1)
        } else {
            e1 = element.element("msubsup")
            nameToMathML(e1)
            subscript.toMathML(e1, null)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
        val e2 = element.element("mfenced")
        for (i in parameters!!.indices) {
            parameters!![i].toMathML(e2, null)
        }
        element.appendChild(e2)
    }

    override fun bodyToMathML(element: MathML, fenced: Boolean) {
        // Not used for Root
    }

    override fun newInstance(): Variable {
        return Root(Array(parameters!!.size) { JsclInteger.valueOf(0) }, null)
    }

    companion object {
        fun nth(parameter: Array<Generic>): Generic {
            val degree = parameter.size - 1
            val a = Fraction(parameter[0], parameter[degree]).selfSimplify()
            return Pow(
                a.negate(),
                Inverse(JsclInteger.valueOf(degree.toLong())).selfSimplify()
            ).selfSimplify()
        }

        fun linear(parameter: Array<Generic>): Generic {
            val a = Fraction(parameter[0], parameter[1]).selfSimplify()
            return a.negate()
        }

        fun quadratic(parameter: Array<Generic>, subscript: Int): Generic {
            val a = Fraction(parameter[1], parameter[2]).selfSimplify()
            val b = Fraction(parameter[0], parameter[2]).selfSimplify()
            val y = Sqrt(
                a.pow(2).subtract(JsclInteger.valueOf(4).multiply(b))
            ).selfSimplify()
            return when (subscript) {
                0 -> Fraction(
                    a.subtract(y),
                    JsclInteger.valueOf(2)
                ).selfSimplify().negate()
                else -> Fraction(
                    a.add(y),
                    JsclInteger.valueOf(2)
                ).selfSimplify().negate()
            }
        }

        fun cubic(parameter: Array<Generic>, subscript: Int): Generic {
            val a = Fraction(parameter[2], parameter[3]).selfSimplify()
            val b = Fraction(parameter[1], parameter[3]).selfSimplify()
            val c = Fraction(parameter[0], parameter[3]).selfSimplify()
            val y = Array(2) { i ->
                Cubic(
                    Root(
                        arrayOf(
                            a.pow(6).subtract(JsclInteger.valueOf(9).multiply(a.pow(4)).multiply(b)).add(JsclInteger.valueOf(27).multiply(a.pow(2)).multiply(b.pow(2))).subtract(JsclInteger.valueOf(27).multiply(b.pow(3))),
                            JsclInteger.valueOf(2).multiply(a.pow(3)).subtract(JsclInteger.valueOf(9).multiply(a).multiply(b)).add(JsclInteger.valueOf(27).multiply(c)),
                            JsclInteger.valueOf(1)
                        ),
                        i
                    ).selfSimplify()
                ).selfSimplify()
            }
            return when (subscript) {
                0 -> Fraction(
                    a.subtract(y[0]).subtract(y[1]),
                    JsclInteger.valueOf(3)
                ).selfSimplify().negate()
                1 -> Fraction(
                    a.subtract(Constants.Generic.J.multiply(y[0])).subtract(Constants.Generic.J_BAR.multiply(y[1])),
                    JsclInteger.valueOf(3)
                ).selfSimplify().negate()
                else -> Fraction(
                    a.subtract(Constants.Generic.J_BAR.multiply(y[0])).subtract(Constants.Generic.J.multiply(y[1])),
                    JsclInteger.valueOf(3)
                ).selfSimplify().negate()
            }
        }

        fun quartic(parameter: Array<Generic>, subscript: Int): Generic {
            val a = Fraction(parameter[3], parameter[4]).selfSimplify()
            val b = Fraction(parameter[2], parameter[4]).selfSimplify()
            val c = Fraction(parameter[1], parameter[4]).selfSimplify()
            val d = Fraction(parameter[0], parameter[4]).selfSimplify()
            val y = Array(3) { i ->
                Sqrt(
                    Root(
                        arrayOf(
                            a.pow(6).subtract(JsclInteger.valueOf(8).multiply(a.pow(4)).multiply(b)).add(JsclInteger.valueOf(16).multiply(a.pow(2)).multiply(b.pow(2))).add(JsclInteger.valueOf(16).multiply(a.pow(3)).multiply(c)).subtract(JsclInteger.valueOf(64).multiply(a).multiply(b).multiply(c)).add(JsclInteger.valueOf(64).multiply(c.pow(2))),
                            JsclInteger.valueOf(-3).multiply(a.pow(4)).add(JsclInteger.valueOf(16).multiply(a.pow(2)).multiply(b)).subtract(JsclInteger.valueOf(16).multiply(b.pow(2))).subtract(JsclInteger.valueOf(16).multiply(a).multiply(c)).add(JsclInteger.valueOf(64).multiply(d)),
                            JsclInteger.valueOf(3).multiply(a.pow(2)).subtract(JsclInteger.valueOf(8).multiply(b)),
                            JsclInteger.valueOf(-1)
                        ),
                        i
                    ).selfSimplify()
                ).selfSimplify()
            }
            return when (subscript) {
                0 -> Fraction(
                    a.add(y[0]).subtract(y[1]).subtract(y[2]),
                    JsclInteger.valueOf(4)
                ).selfSimplify().negate()
                1 -> Fraction(
                    a.subtract(y[0]).subtract(y[1]).add(y[2]),
                    JsclInteger.valueOf(4)
                ).selfSimplify().negate()
                2 -> Fraction(
                    a.add(y[0]).add(y[1]).add(y[2]),
                    JsclInteger.valueOf(4)
                ).selfSimplify().negate()
                else -> Fraction(
                    a.subtract(y[0]).add(y[1]).subtract(y[2]),
                    JsclInteger.valueOf(4)
                ).selfSimplify().negate()
            }
        }

        fun sigma(parameter: Array<Generic>, n: Int): Generic {
            val s = Sigma(parameter, n)
            s.compute()
            return s.getValue()
        }
    }
}

internal class Sigma(parameter: Array<Generic>, private val n: Int) {
    private val root: Array<Generic>
    private var generic: Generic = JsclInteger.valueOf(0)
    private val place: BooleanArray

    init {
        root = Array(parameter.size - 1) { i -> Root(parameter, i).expressionValue() }
        place = BooleanArray(root.size)
    }

    fun compute() {
        generic = JsclInteger.valueOf(0)
        compute(0, n)
    }

    private fun compute(p: Int, nn: Int) {
        if (nn > 0) {
            for (i in p until root.size) {
                place[i] = true
                compute(i + 1, nn - 1)
                place[i] = false
            }
        } else {
            var s: Generic = JsclInteger.valueOf(1)
            for (i in root.indices) {
                if (place[i]) s = s.multiply(root[i])
            }
            generic = generic.add(s)
        }
    }

    fun getValue(): Generic = generic
}
