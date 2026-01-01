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
        val position = Position(0)
        val exceptions = ArrayList<ParseException>()
        val exceptionsPool = ExceptionsPool()

        fun reset() {
            position.value = 0
            exceptions.clear()
        }

        fun addException(e: ParseException) {
            if (!exceptions.contains(e)) {
                exceptions.add(e)
            }
        }

        companion object {
            // Using a simple approach instead of ThreadLocal for KMP compatibility
            // ThreadLocal doesn't exist in Kotlin/Native, so we create new parameters each time
            // This is fine for parsing as it's typically short-lived
            fun get(expression: String): Parameters {
                val parameters = Parameters(expression, JsclMathEngine.getInstance())
                parameters.reset()
                return parameters
            }
        }
    }
}
