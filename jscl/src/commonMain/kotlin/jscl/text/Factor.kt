package jscl.text

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.operator.Percent

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:45 PM
 */
internal class Factor private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val minus = MinusParser.parser.parse(p, previousSumElement)

        val result = UnsignedFactor.parser.parse(p, previousSumElement) as Generic

        if (!minus) {
            return result
        }
        return try {
            val variable = result.variableValue()
            if (variable is Percent) {
                return percentWithNegatedContent(variable)
            }
            result.negate()
        } catch (_: Exception) {
            if (result is Expression && result.size() == 1) {
                val literal = result.literal(0)
                if (literal.degree() == 1) {
                    val variable = literal.getVariable(0)
                    if (variable is Percent) {
                        return percentWithNegatedContent(variable)
                    }
                }
            }
            result.negate()
        }
    }

    private fun percentWithNegatedContent(variable: Percent): Generic {
        val params = variable.getParameters() ?: return variable.expressionValue().negate()
        val undefinedParam = JsclInteger.valueOf(Long.MIN_VALUE + 1)
        val base = if (params.size > 1) params[1] else undefinedParam
        val negatedContent = params[0].negate()
        return Percent(negatedContent, base).expressionValue()
    }

    companion object {
        val parser: Parser<Generic> = Factor()
    }
}
