package jscl.text

import jscl.math.Generic
import jscl.text.msg.Messages

class PrimeCharacters private constructor() : Parser<Int> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Int {
        val pos0 = p.position.toInt()

        var result = 0

        ParserUtils.skipWhitespaces(p)

        if (p.position.toInt() < p.expression.length && p.expression[p.position.toInt()] == '\'') {
            p.position.increment()
            result = 1
        } else {
            ParserUtils.throwParseException(p, pos0, Messages.msg_12, '\'')
        }

        while (p.position.toInt() < p.expression.length && p.expression[p.position.toInt()] == '\'') {
            p.position.increment()
            result++
        }

        return result
    }

    companion object {
        val parser: Parser<Int> = PrimeCharacters()
    }
}
