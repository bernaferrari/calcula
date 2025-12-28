package jscl.math

import jscl.math.function.Constant
import jscl.mathml.MathML

class TechnicalVariable constructor(
    name: String,
    val subscript: IntArray = intArrayOf()
) : Variable(name) {

    override fun antiDerivative(variable: Variable): Generic {
        throw NotIntegrableException(this)
    }

    override fun derivative(variable: Variable): Generic {
        return if (isIdentity(variable)) JsclInteger.valueOf(1) else JsclInteger.valueOf(0)
    }

    override fun substitute(variable: Variable, generic: Generic): Generic {
        return if (isIdentity(variable)) generic else expressionValue()
    }

    override fun expand(): Generic = expressionValue()

    override fun factorize(): Generic = expressionValue()

    override fun elementary(): Generic = expressionValue()

    override fun simplify(): Generic = expressionValue()

    override fun numeric(): Generic {
        throw ArithmeticException("Could not evaluate numeric value for technical variable!")
    }

    override fun isConstant(variable: Variable): Boolean {
        return !isIdentity(variable)
    }

    override fun compareTo(variable: Variable): Int {
        if (this === variable) return 0
        val c = comparator.compare(this, variable)
        return when {
            c < 0 -> -1
            c > 0 -> 1
            else -> {
                val v = variable as TechnicalVariable
                val nameCompare = name.compareTo(v.name)
                when {
                    nameCompare < 0 -> -1
                    nameCompare > 0 -> 1
                    else -> compareSubscript(subscript, v.subscript)
                }
            }
        }
    }

    fun compareSubscript(c1: IntArray, c2: IntArray): Int {
        if (c1.size < c2.size) return -1
        if (c1.size > c2.size) return 1
        for (i in c1.indices) {
            if (c1[i] < c2[i]) return -1
            if (c1[i] > c2[i]) return 1
        }
        return 0
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append(name)
        if (subscript.size == 1) {
            buffer.append(subscript[0])
        } else {
            for (i in subscript.indices) {
                buffer.append("[").append(subscript[i]).append("]")
            }
        }
        return buffer.toString()
    }

    override fun toJava(): String {
        throw UnsupportedOperationException("Technical variable cannot be converted to Java")
    }

    override fun toMathML(element: MathML, data: Any?) {
        throw UnsupportedOperationException("Technical variable cannot be converted to MathML")
    }

    override fun newInstance(): Variable = TechnicalVariable(name, subscript.copyOf())

    override val constants: Set<Constant>
        get() = emptySet()
}
