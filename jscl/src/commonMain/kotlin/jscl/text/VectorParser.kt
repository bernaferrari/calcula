package jscl.text

import jscl.math.Generic
import jscl.math.JsclVector
import jscl.util.ArrayUtils

class VectorParser private constructor() : Parser<JsclVector> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): JsclVector {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        ParserUtils.tryToParse(p, pos0, '[')

        val result = ArrayList<Generic>()

        // Parse first expression - failure resets position and propagates
        when (val firstExpr = ExpressionParser.parser.tryParse(p, previousSumElement)) {
            is ParseResult.Success -> result.add(firstExpr.value)
            is ParseResult.Failure -> {
                p.position.value = pos0
                throw firstExpr.toException()
            }
        }

        // Parse additional comma-separated expressions
        CommaAndExpression.parser.parseWhileSuccessful(p, previousSumElement, Unit) { _, expr ->
            result.add(expr)
        }

        ParserUtils.skipWhitespaces(p)

        ParserUtils.tryToParse(p, pos0, ']')

        @Suppress("UNCHECKED_CAST")
        return JsclVector(ArrayUtils.toArray(result, arrayOfNulls<Generic>(result.size)) as Array<Generic>)
    }

    companion object {
        val parser: Parser<JsclVector> = VectorParser()
    }
}
