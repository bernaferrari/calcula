@file:Suppress("UNCHECKED_CAST")

package jscl.math

import jscl.math.function.Fraction
import jscl.math.function.Pow
import jscl.math.polynomial.Monomial
import jscl.mathml.MathML
import jscl.common.collections.SortedMutableMap

class Literal internal constructor() : Comparable<Any> {

    private lateinit var variables: Array<Variable>
    private lateinit var powers: IntArray
    private var degree: Int = 0
    private var size: Int = 0

    constructor(size: Int) : this() {
        init(size)
    }

    fun size(): Int = size

    fun getVariable(i: Int): Variable = variables[i]

    fun getPower(i: Int): Int = powers[i]

    internal fun init(size: Int) {
        variables = Array(size) { null as Variable? } as Array<Variable>
        powers = IntArray(size)
        this.size = size
    }

    internal fun resize(size: Int) {
        if (size < variables.size) {
            val variable = Array<Variable>(size) { i -> variables[i] }
            val power = IntArray(size) { i -> powers[i] }
            this.variables = variable
            this.powers = power
            this.size = size
        }
    }

    fun multiply(that: Literal): Literal {
        val result = newInstance(size + that.size)
        var i = 0

        var thisI = 0
        var thatI = 0

        var thisVariable: Variable? = if (thisI < this.size) this.variables[thisI] else null
        var thatVariable: Variable? = if (thatI < that.size) that.variables[thatI] else null

        while (thisVariable != null || thatVariable != null) {
            val c = when {
                thisVariable == null -> 1
                thatVariable == null -> -1
                else -> thisVariable.compareTo(thatVariable)
            }

            when {
                c < 0 -> {
                    val s = powers[thisI]
                    result.variables[i] = thisVariable!!
                    result.powers[i] = s
                    result.degree += s
                    i++
                    thisI++
                    thisVariable = if (thisI < size) variables[thisI] else null
                }
                c > 0 -> {
                    val s = that.powers[thatI]
                    result.variables[i] = thatVariable!!
                    result.powers[i] = s
                    result.degree += s
                    i++
                    thatI++
                    thatVariable = if (thatI < that.size) that.variables[thatI] else null
                }
                else -> {
                    val s = powers[thisI] + that.powers[thatI]

                    result.variables[i] = thisVariable!!
                    result.powers[i] = s
                    result.degree += s

                    i++
                    thisI++
                    thatI++

                    thisVariable = if (thisI < this.size) this.variables[thisI] else null
                    thatVariable = if (thatI < that.size) that.variables[thatI] else null
                }
            }
        }

        result.resize(i)
        return result
    }

    @Throws(ArithmeticException::class)
    fun divide(literal: Literal): Literal {
        val l = newInstance(size + literal.size)
        var i = 0
        var i1 = 0
        var i2 = 0
        var v1: Variable? = if (i1 < size) variables[i1] else null
        var v2: Variable? = if (i2 < literal.size) literal.variables[i2] else null

        while (v1 != null || v2 != null) {
            val c = when {
                v1 == null -> 1
                v2 == null -> -1
                else -> v1.compareTo(v2)
            }

            when {
                c < 0 -> {
                    val s = powers[i1]
                    l.variables[i] = v1!!
                    l.powers[i] = s
                    l.degree += s
                    i++
                    i1++
                    v1 = if (i1 < size) variables[i1] else null
                }
                c > 0 -> throw NotDivisibleException()
                else -> {
                    val s = powers[i1] - literal.powers[i2]
                    if (s < 0) {
                        throw NotDivisibleException()
                    } else if (s != 0) {
                        l.variables[i] = v1!!
                        l.powers[i] = s
                        l.degree += s
                        i++
                    }
                    i1++
                    i2++
                    v1 = if (i1 < size) variables[i1] else null
                    v2 = if (i2 < literal.size) literal.variables[i2] else null
                }
            }
        }
        l.resize(i)
        return l
    }

    fun gcd(that: Literal): Literal {
        val result = newInstance(minOf(this.size, that.size))
        var i = 0

        var thisI = 0
        var thatI = 0

        var thisVariable: Variable? = if (thisI < this.size) this.variables[thisI] else null
        var thatVariable: Variable? = if (thatI < that.size) that.variables[thatI] else null

        while (thisVariable != null || thatVariable != null) {
            val c = when {
                thisVariable == null -> 1
                thatVariable == null -> -1
                else -> thisVariable.compareTo(thatVariable)
            }

            when {
                c < 0 -> {
                    thisI++
                    thisVariable = if (thisI < this.size) this.variables[thisI] else null
                }
                c > 0 -> {
                    thatI++
                    thatVariable = if (thatI < that.size) that.variables[thatI] else null
                }
                else -> {
                    val minPower = minOf(this.powers[thisI], that.powers[thatI])

                    result.variables[i] = thisVariable!!
                    result.powers[i] = minPower
                    result.degree += minPower

                    i++
                    thisI++
                    thatI++

                    thisVariable = if (thisI < this.size) this.variables[thisI] else null
                    thatVariable = if (thatI < that.size) that.variables[thatI] else null
                }
            }
        }

        result.resize(i)
        return result
    }

    fun scm(that: Literal): Literal {
        val result = newInstance(this.size + that.size)
        var i = 0

        var thisI = 0
        var thatI = 0

        var thisVariable: Variable? = if (thisI < this.size) this.variables[thisI] else null
        var thatVariable: Variable? = if (thatI < that.size) that.variables[thatI] else null

        while (thisVariable != null || thatVariable != null) {
            val c = when {
                thisVariable == null -> 1
                thatVariable == null -> -1
                else -> thisVariable.compareTo(thatVariable)
            }

            when {
                c < 0 -> {
                    val thisPower = this.powers[thisI]

                    result.variables[i] = thisVariable!!
                    result.powers[i] = thisPower
                    result.degree += thisPower

                    i++
                    thisI++
                    thisVariable = if (thisI < size) variables[thisI] else null
                }
                c > 0 -> {
                    val thatPower = that.powers[thatI]

                    result.variables[i] = thatVariable!!
                    result.powers[i] = thatPower
                    result.degree += thatPower

                    i++
                    thatI++
                    thatVariable = if (thatI < that.size) that.variables[thatI] else null
                }
                else -> {
                    val maxPower = maxOf(this.powers[thisI], that.powers[thatI])

                    result.variables[i] = thisVariable!!
                    result.powers[i] = maxPower
                    result.degree += maxPower

                    i++
                    thisI++
                    thatI++

                    thisVariable = if (thisI < this.size) this.variables[thisI] else null
                    thatVariable = if (thatI < that.size) that.variables[thatI] else null
                }
            }
        }

        result.resize(i)
        return result
    }

    @Throws(NotProductException::class)
    fun productValue(): Array<Generic> {
        val a = Array<Generic>(size) { i -> variables[i].expressionValue().pow(powers[i]) }
        return a
    }

    @Throws(NotPowerException::class)
    fun powerValue(): Power {
        return when {
            size == 0 -> Power(JsclInteger.valueOf(1), 1)
            size == 1 -> {
                val v = variables[0]
                val c = powers[0]
                Power(v.expressionValue(), c)
            }
            else -> throw NotPowerException()
        }
    }

    @Throws(NotVariableException::class)
    fun variableValue(): Variable {
        return when {
            size == 0 -> throw NotVariableException()
            size == 1 -> {
                val v = variables[0]
                val c = powers[0]
                if (c == 1) v else throw NotVariableException()
            }
            else -> throw NotVariableException()
        }
    }

    fun variables(): Array<Variable> {
        val va = Array<Variable>(size) { i -> variables[i] }
        return va
    }

    fun degree(): Int = degree

    fun compareTo(that: Literal): Int {
        var thisI = this.size
        var thatI = that.size

        var thisVariable: Variable? = if (thisI == 0) null else this.variables[--thisI]
        var thatVariable: Variable? = if (thatI == 0) null else that.variables[--thatI]

        while (thisVariable != null || thatVariable != null) {
            val c = when {
                thisVariable == null -> -1
                thatVariable == null -> 1
                else -> thisVariable.compareTo(thatVariable)
            }

            when {
                c < 0 -> return -1
                c > 0 -> return 1
                else -> {
                    val thisPower = this.powers[thisI]
                    val thatPower = that.powers[thatI]
                    when {
                        thisPower < thatPower -> return -1
                        thisPower > thatPower -> return 1
                    }

                    thisVariable = if (thisI == 0) null else this.variables[--thisI]
                    thatVariable = if (thatI == 0) null else that.variables[--thatI]
                }
            }
        }
        return 0
    }

    override fun compareTo(other: Any): Int = compareTo(other as Literal)

    internal fun init(variable: Variable, pow: Int) {
        if (pow != 0) {
            init(1)
            variables[0] = variable
            powers[0] = pow
            degree = pow
        } else {
            init(0)
        }
    }

    internal fun init(monomial: Monomial) {
        val map = SortedMutableMap.naturalOrder<Variable, Int>()
        val unk = monomial.unknown
        for (i in unk.indices) {
            val c = monomial.element(i)
            if (c > 0) map[unk[i]] = c
        }
        init(map.size)
        val it = map.entries.iterator()
        for (i in 0 until map.size) {
            val e = it.next()
            val v = e.key
            val c = e.value
            variables[i] = v
            powers[i] = c
            degree += c
        }
    }

    fun content(c: (Variable) -> Generic): Map<Variable, Generic> {
        val result = SortedMutableMap.naturalOrder<Variable, Generic>()

        for (i in 0 until size) {
            result[variables[i]] = c(variables[i])
        }

        return result
    }

    override fun toString(): String {
        val result = StringBuilder()

        if (degree == 0) {
            result.append("1")
        }

        for (i in 0 until size) {
            if (i > 0) {
                result.append("*")
            }

            val variable = variables[i]
            val power = powers[i]
            if (power == 1) {
                result.append(variable)
            } else {
                if (variable is Fraction || variable is Pow) {
                    result.append("(").append(variable).append(")")
                } else {
                    result.append(variable)
                }
                result.append("^").append(power)
            }
        }
        return result.toString()
    }

    fun toJava(): String {
        val buffer = StringBuilder()
        if (degree == 0) buffer.append("JsclDouble.valueOf(1)")
        for (i in 0 until size) {
            if (i > 0) buffer.append(".multiply(")
            val v = variables[i]
            val c = powers[i]
            buffer.append(v.toJava())
            if (c != 1) {
                buffer.append(".pow(").append(c).append(")")
            }
            if (i > 0) buffer.append(")")
        }
        return buffer.toString()
    }

    fun toMathML(element: MathML, data: Any?) {
        if (degree == 0) {
            val e1 = element.element("mn")
            e1.appendChild(element.text("1"))
            element.appendChild(e1)
        }
        for (i in 0 until size) {
            val v = variables[i]
            val c = powers[i]
            v.toMathML(element, c)
        }
    }

    private fun newInstance(n: Int): Literal = Literal(n)

    companion object {
        fun newInstance(): Literal = Literal(0)

        fun valueOf(variable: Variable): Literal = valueOf(variable, 1)

        fun valueOf(variable: Variable, power: Int): Literal {
            val l = Literal()
            l.init(variable, power)
            return l
        }

        fun valueOf(monomial: Monomial): Literal {
            val l = Literal()
            l.init(monomial)
            return l
        }
    }
}
