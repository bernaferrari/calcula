package jscl.text

import jscl.math.ExpressionVariable
import jscl.math.Generic

class BracketedExpression private constructor() : Parser<ExpressionVariable> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): ExpressionVariable {
        val pos0 = p.position.toInt()

        ParserUtils.tryToParse(p, pos0, '(')

        val result: Generic
        try {
            result = ExpressionParser.parser.parse(p, previousSumElement)
        } catch (e: ParseException) {
            p.position.setValue(pos0)
            throw e
        }

        ParserUtils.tryToParse(p, pos0, ')')

        return ExpressionVariable(result)
    }

    companion object {
        @JvmField
        val parser: Parser<ExpressionVariable> = BracketedExpression()
    }
}
