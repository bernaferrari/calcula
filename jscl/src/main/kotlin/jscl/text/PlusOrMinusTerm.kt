package jscl.text

import jscl.math.Generic
import jscl.text.msg.Messages

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:44 PM
 */
internal class PlusOrMinusTerm private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        var minus = false
        val pos1 = p.position.toInt()
        if (pos1 < p.expression.length && (p.expression[pos1] == '+' || MinusParser.isMinus(p.expression[pos1]))) {
            minus = MinusParser.isMinus(p.expression[pos1])
            p.position.increment()
        } else {
            ParserUtils.throwParseException(p, pos0, Messages.msg_10, '+', '-')
        }

        val result = ParserUtils.parseWithRollback(TermParser.parser, pos0, previousSumElement, p)

        return if (minus) result.negate() else result
    }

    companion object {
        @JvmField
        val parser: Parser<Generic> = PlusOrMinusTerm()
    }
}
