package jscl.text

import jscl.math.Generic
import jscl.util.ArrayUtils

class ParameterListParser(
    private val minNumberOfParameters: Int = 1
) : Parser<Array<Generic>> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Array<Generic> {
        val pos0 = p.position.toInt()

        val result = ArrayList<Generic>()

        ParserUtils.tryToParse(p, pos0, '(')

        // Parse first expression using Result-based approach
        when (val firstExpr = ExpressionParser.parser.tryParse(p, previousSumElement)) {
            is ParseResult.Success -> result.add(firstExpr.value)
            is ParseResult.Failure -> {
                if (minNumberOfParameters > 0) {
                    p.position.value = pos0
                    throw firstExpr.toException()
                } else {
                    p.exceptionsPool.release(firstExpr.toException())
                }
            }
        }

        // Parse additional comma-separated expressions
        CommaAndExpression.parser.parseWhileSuccessful(p, previousSumElement, Unit) { _, expr ->
            result.add(expr)
        }

        ParserUtils.tryToParse(p, pos0, ')')

        @Suppress("UNCHECKED_CAST")
        return ArrayUtils.toArray(result, arrayOfNulls<Generic>(result.size)) as Array<Generic>
    }

    companion object {
        val parser1: Parser<Array<Generic>> = ParameterListParser()
    }
}
