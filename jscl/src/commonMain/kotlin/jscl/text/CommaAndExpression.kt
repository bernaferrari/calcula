package jscl.text

import jscl.math.Generic

class CommaAndExpression private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        ParserUtils.tryToParse(p, pos0, ',')

        return ParserUtils.parseWithRollback(ExpressionParser.parser, pos0, previousSumElement, p)
    }

    companion object {
        val parser: Parser<Generic> = CommaAndExpression()
    }
}
