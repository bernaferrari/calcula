package jscl.math.operator

import jscl.math.*
import jscl.math.function.Pow
import jscl.mathml.MathML
import jscl.text.ParserUtils

/**
 * User: serso
 * Date: 12/15/11
 * Time: 10:32 PM
 */
class DoubleFactorial : PostfixFunction {

    constructor(expression: Generic?) : super(NAME, expression?.let { arrayOf<Generic>(it) })

    private constructor(parameter: Array<Generic>) : super(NAME, ParserUtils.copyOf<Generic>(parameter, 1))

    override fun getMinParameters(): Int = 1

    override fun selfExpand(): Generic {
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        val parameter = parameters!![0]
        if (parameter.isInteger) {
            val n = parameter.integerValue().toInt()
            if (n < 0) {
                throw ArithmeticException("Cannot take factorial from negative integer!")
            }

            val result: Generic
            if (n == 0) {
                result = JsclInteger.valueOf(1)
            } else {
                val i: Int
                if (n % 2 != 0) {
                    // odd
                    i = 1
                } else {
                    // even
                    i = 2
                }

                var res: Generic = JsclInteger.valueOf(i.toLong())
                var j = i
                while (j < n) {
                    ParserUtils.checkInterruption()
                    res = res.multiply(JsclInteger.valueOf((j + 2).toLong()))
                    j += 2
                }
                result = res
            }

            if (result is JsclInteger) {
                return NumericWrapper(result)
            } else {
                throw NotIntegerException.get()
            }

        } else {
            throw NotIntegerException.get()
        }
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) {
            bodyToMathML(element)
        } else {
            val e1 = element.element("msup")
            bodyToMathML(e1)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
    }

    override fun newInstance(parameters: Array<Generic>): Operator {
        return DoubleFactorial(parameters)
    }

    internal fun bodyToMathML(element: MathML) {
        val e1 = element.element("mrow")
        try {
            val en = parameters!![0].integerValue()
            en.toMathML(e1, null)
        } catch (e: NotIntegerException) {
            try {
                val v = parameters!![0].variableValue()
                if (v is Pow) {
                    GenericVariable.valueOf(parameters!![0]).toMathML(e1, null)
                } else {
                    v.toMathML(e1, null)
                }
            } catch (e2: NotVariableException) {
                GenericVariable.valueOf(parameters!![0]).toMathML(e1, null)
            }
        }
        val e2 = element.element("mo")
        e2.appendChild(element.text("!!"))
        e1.appendChild(e2)
        element.appendChild(e1)
    }

    override fun newInstance(): Variable {
        return DoubleFactorial(null as Generic?)
    }

    companion object {
        const val NAME = "!!"
    }
}
