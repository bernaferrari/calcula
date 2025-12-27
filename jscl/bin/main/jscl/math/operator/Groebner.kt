@file:Suppress("UNCHECKED_CAST")

package jscl.math.operator

import jscl.math.*
import jscl.math.function.Constant
import jscl.math.function.ImplicitFunction
import jscl.math.polynomial.Basis
import jscl.math.polynomial.Monomial
import jscl.math.polynomial.Ordering
import jscl.math.polynomial.Polynomial
import jscl.mathml.MathML
import jscl.text.ParseException

class Groebner : Operator {

    constructor(generic: Generic?, variable: Generic?, ordering: Generic?, modulo: Generic?) :
            super(NAME, genericArrayOf(generic, variable, ordering, modulo))

    private constructor(parameters: Array<Generic>) : super(NAME, createParameters(parameters))

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        val generic = (parameters!![0] as JsclVector).elements()
        val variable = toVariables(parameters!![1] as JsclVector)
        val ord = ordering(parameters!![2])
        val m = parameters!![3].integerValue().toInt()
        return PolynomialVector(Basis.compute(generic, variable, ord, m))
    }

    fun transmute(): Operator {
        val p = arrayOf(
            GenericVariable.content(parameters!![0]),
            GenericVariable.content(parameters!![1])
        )
        if (p[0] is JsclVector && p[1] is JsclVector) {
            val generic = (p[0] as JsclVector).elements()
            val variable = toVariables(p[1] as JsclVector)
            val ord = ordering(parameters!![2])
            val m = parameters!![3].integerValue().toInt()
            return Groebner(
                PolynomialVector(Basis(generic, Polynomial.factory(variable, ord, m))),
                p[1],
                parameters!![2],
                parameters!![3]
            )
        }
        return this
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        var n = 4
        if (parameters!![3].signum() == 0) {
            n = 3
            if (ordering(parameters!![2]) == Monomial.lexicographic) n = 2
        }
        if (exponent == 1) {
            nameToMathML(element)
        } else {
            val e1 = element.element("msup")
            nameToMathML(e1)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
        val e1 = element.element("mfenced")
        for (i in 0 until n) {
            parameters!![i].toMathML(e1, null)
        }
        element.appendChild(e1)
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Groebner(parameters).transmute()
    }

    override fun newInstance(): Variable {
        return Groebner(null, null, null, null)
    }

    companion object {
        const val NAME = "groebner"

        @JvmStatic
        private fun createParameters(parameters: Array<Generic>): Array<Generic> {
            val result = arrayOfNulls<Generic>(4)

            try {
                result[0] = parameters[0]
                result[1] = parameters[1]
                result[2] = if (parameters.size > 2) parameters[2] else Expression.valueOf("lex")
                result[3] = if (parameters.size > 3) parameters[3] else JsclInteger.valueOf(0)
            } catch (e: ParseException) {
                throw ArithmeticException(e.message)
            }

            return result as Array<Generic>
        }

        @JvmStatic
        internal fun ordering(generic: Generic): Ordering {
            val v = generic.variableValue()
            if (v.compareTo(Constant("lex")) == 0) return Monomial.lexicographic
            else if (v.compareTo(Constant("tdl")) == 0) return Monomial.totalDegreeLexicographic
            else if (v.compareTo(Constant("drl")) == 0) return Monomial.degreeReverseLexicographic
            else if (v is ImplicitFunction) {
                val g = v.getParameters()
                val k = g!![0].integerValue().toInt()
                if (v.compareTo(
                        ImplicitFunction(
                            "elim",
                            arrayOf(JsclInteger.valueOf(k.toLong())),
                            intArrayOf(0),
                            arrayOf()
                        )
                    ) == 0
                )
                    return Monomial.kthElimination(k)
            }
            throw ArithmeticException()
        }
    }
}

internal class PolynomialVector : JsclVector {
    val basis: Basis

    constructor(basis: Basis) : this(basis, basis.elements())

    constructor(basis: Basis, generic: Array<Generic>) : super(
        if (generic.isNotEmpty()) generic else arrayOf(JsclInteger.valueOf(0))
    ) {
        this.basis = basis
    }

    override fun toString(): String {
        val result = StringBuilder()

        result.append("[")

        for (i in 0 until rows) {
            result.append(basis.polynomial(elements[i])).append(if (i < rows - 1) ", " else "")
        }

        result.append("]")

        return result.toString()
    }

    override fun bodyToMathML(e0: MathML) {
        val e1 = e0.element("mfenced")
        val e2 = e0.element("mtable")
        for (i in 0 until rows) {
            val e3 = e0.element("mtr")
            val e4 = e0.element("mtd")
            basis.polynomial(elements[i]).toMathML(e4, null)
            e3.appendChild(e4)
            e2.appendChild(e3)
        }
        e1.appendChild(e2)
        e0.appendChild(e1)
    }

    override fun newInstance(element: Array<Generic>): Generic {
        return PolynomialVector(basis, element)
    }
}
