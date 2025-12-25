package jscl.text

import jscl.JsclMathEngine
import jscl.MathContext
import jscl.math.Generic

/**
 * Main parser interface.
 *
 * Aim of parser is to convert input string expression into java objects
 *
 * @param T type of result object of parser
 */
interface Parser<T> {

    /**
     * @param p                  parse parameters
     * @param previousSumElement sum element to the left of last + sign
     * @return parsed object of type T
     * @throws ParseException occurs if object could not be parsed from the string
     */
    @Throws(ParseException::class)
    fun parse(p: Parameters, previousSumElement: Generic?): T

    class Parameters(
        var expression: String,
        val context: MathContext
    ) {
        val position = MutableInt(0)
        val exceptions = ArrayList<ParseException>()
        val exceptionsPool = ExceptionsPool()

        fun reset() {
            position.setValue(0)
            exceptions.clear()
        }

        fun addException(e: ParseException) {
            if (!exceptions.contains(e)) {
                exceptions.add(e)
            }
        }

        companion object {
            private val instance = object : ThreadLocal<Parameters>() {
                override fun initialValue(): Parameters {
                    return Parameters("", JsclMathEngine.getInstance())
                }
            }

            fun get(expression: String): Parameters {
                val parameters = instance.get()!!
                parameters.expression = expression
                parameters.reset()
                return parameters
            }
        }
    }
}
