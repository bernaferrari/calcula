package jscl.text

import jscl.math.Generic

class Subscript private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val pos0 = p.position.toInt()

        ParserUtils.tryToParse(p, pos0, '[')

        val a: Generic
        try {
            a = ExpressionParser.parser.parse(p, previousSumElement)
        } catch (e: ParseException) {
            p.position.value = pos0
            throw e
        }

        ParserUtils.tryToParse(p, pos0, ']')

        return a
    }

    companion object {
        val parser: Parser<Generic> = Subscript()
    }
}
