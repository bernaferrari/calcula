package jscl.math.function

import jscl.math.*
import jscl.mathml.MathML

class Comparison(name: String, expression1: Generic?, expression2: Generic?) : Function(name, if (expression1 != null && expression2 != null) arrayOf(expression1, expression2) else null) {

    private val operator: Int = names.indexOf(name).also {
        if (it < 0) {
            throw ArithmeticException("$name comparison function doesn't exist!")
        }
    }

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    override fun getMinParameters(): Int {
        return 2
    }

    override fun antiDerivative(n: Int): Generic {
        throw NotIntegrableException(this)
    }

    override fun derivative(n: Int): Generic {
        return JsclInteger.valueOf(0)
    }

    override fun selfExpand(): Generic {
        try {
            return compare(parameters!![0].integerValue(), parameters!![1].integerValue())
        } catch (e: NotIntegerException) {
        }
        return expressionValue()
    }

    override fun selfElementary(): Generic {
        return expressionValue()
    }

    override fun selfSimplify(): Generic {
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        return compare(parameters!![0] as NumericWrapper, parameters!![1] as NumericWrapper)
    }

    private fun compare(a1: JsclInteger, a2: JsclInteger): JsclInteger {
        return JsclInteger.valueOf(if (compare(a1 as Generic, a2)) 1 else 0)
    }

    private fun compare(a1: NumericWrapper, a2: NumericWrapper): NumericWrapper {
        return NumericWrapper(JsclInteger.valueOf(if (compare(a1, a2 as Generic)) 1 else 0))
    }

    private fun compare(a1: Generic, a2: Generic): Boolean {
        return when (operator) {
            0 -> a1.compareTo(a2) == 0
            1 -> a1.compareTo(a2) <= 0
            2 -> a1.compareTo(a2) >= 0
            3 -> a1.compareTo(a2) != 0
            4 -> a1.compareTo(a2) < 0
            5 -> a1.compareTo(a2) > 0
            6 -> a1.compareTo(a2) == 0
            else -> false
        }
    }

    override fun toJava(): String {
        val result = StringBuilder()
        result.append(parameters!![0].toJava()).append(easj[operator]).append(parameters!![1].toJava())
        return result.toString()
    }

    override fun toMathML(element: MathML, data: Any?) {
        parameters!![0].toMathML(element, null)
        val e1 = element.element("mo")
        e1.appendChild(element.text(easm[operator]))
        element.appendChild(e1)
        parameters!![1].toMathML(element, null)
    }

    override fun newInstance(): Variable {
        return Comparison(name, null, null)
    }

    companion object {
        @JvmField
        val names = listOf("eq", "le", "ge", "ne", "lt", "gt", "ap")
        private val eass = arrayOf("=", "<=", ">=", "<>", "<", ">", "~")
        private val easj = arrayOf("==", "<=", ">=", "!=", "<", ">", "==")
        private val easm = arrayOf("=", "\u2264", "\u2265", "\u2260", "<", ">", "\u2248")
    }
}
