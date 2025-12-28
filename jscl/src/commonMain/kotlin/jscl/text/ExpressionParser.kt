package jscl.text

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.operator.Percent

class ExpressionParser private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val minus = MinusParser.parser.parse(p, previousSumElement)

        var result = TermParser.parser.parse(p, previousSumElement)

        if (minus) {
            result = negatePercentIfNeeded(result) ?: result.negate()
        }

        while (true) {
            try {
                result = result.add(PlusOrMinusTerm.parser.parse(p, result))
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
                break
            }
        }

        return result
    }

    companion object {
        val parser: Parser<Generic> = ExpressionParser()

        private fun negatePercentIfNeeded(generic: Generic): Generic? {
            return try {
                val variable = generic.variableValue()
                if (variable is Percent) {
                    percentWithNegatedContent(variable)
                } else {
                    null
                }
            } catch (_: Exception) {
                if (generic is Expression && generic.size() == 1) {
                    val literal = generic.literal(0)
                    if (literal.degree() == 1) {
                        val variable = literal.getVariable(0)
                        if (variable is Percent) {
                            return percentWithNegatedContent(variable)
                        }
                    }
                }
                null
            }
        }

        private fun percentWithNegatedContent(variable: Percent): Generic {
            val params = variable.getParameters() ?: return variable.expressionValue().negate()
            val undefinedParam = JsclInteger.valueOf(Long.MIN_VALUE + 1)
            val base = if (params.size > 1) params[1] else undefinedParam
            val negatedContent = params[0].negate()
            return Percent(negatedContent, base).expressionValue()
        }
    }
}
