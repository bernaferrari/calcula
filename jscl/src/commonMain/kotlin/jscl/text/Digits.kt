package jscl.text

import jscl.NumeralBase
import jscl.math.Generic
import jscl.text.msg.Messages
import jscl.text.ParserUtils.makeParseException
import jscl.text.ParserUtils.skipWhitespaces

class Digits(
    private val nb: NumeralBase
) : Parser<String> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): String {
        val pos0 = p.position.toInt()

        skipWhitespaces(p)

        val result: StringBuilder
        if (p.position.toInt() < p.expression.length && nb.getAcceptableCharacters().contains(p.expression[p.position.toInt()])) {
            result = StringBuilder(2)
            result.append(p.expression[p.position.toInt()])
            p.position.increment()
        } else {
            throw makeParseException(p, pos0, Messages.msg_9)
        }

        while (p.position.toInt() < p.expression.length && nb.getAcceptableCharacters().contains(p.expression[p.position.toInt()])) {
            result.append(p.expression[p.position.toInt()])
            p.position.increment()
        }

        return result.toString()
    }
}
