package jscl.text

import jscl.math.ExpressionVariable
import jscl.math.Generic

class BracketedExpression private constructor() : Parser<ExpressionVariable> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): ExpressionVariable {
        val pos0 = p.position.toInt()

        ParserUtils.tryToParse(p, pos0, '(')

        // Parse expression, reset position on failure
        val result = ExpressionParser.parser.parseOrThrow(p, previousSumElement, pos0)

        ParserUtils.tryToParse(p, pos0, ')')

        return ExpressionVariable(result)
    }

    companion object {
        val parser: Parser<ExpressionVariable> = BracketedExpression()
    }
}
