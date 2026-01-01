package jscl.text

import jscl.math.Generic

class Subscript private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val pos0 = p.position.toInt()

        ParserUtils.tryToParse(p, pos0, '[')

        // Parse expression, reset position on failure
        val a = ExpressionParser.parser.parseOrThrow(p, previousSumElement, pos0)

        ParserUtils.tryToParse(p, pos0, ']')

        return a
    }

    companion object {
        val parser: Parser<Generic> = Subscript()
    }
}
