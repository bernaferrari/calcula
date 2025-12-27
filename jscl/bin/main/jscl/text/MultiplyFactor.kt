package jscl.text

import jscl.math.Generic
import jscl.text.msg.Messages

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:45 PM
 */
internal class MultiplyFactor private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)
        val pos1 = p.position.toInt()
        if (pos1 < p.expression.length && isMultiplication(p.expression[pos1])) {
            p.position.increment()
        } else {
            ParserUtils.throwParseException(p, pos0, Messages.msg_10, '*', '/')
        }

        return ParserUtils.parseWithRollback(Factor.parser, pos0, previousSumElement, p)
    }

    companion object {
        @JvmField
        val parser: Parser<Generic> = MultiplyFactor()

        @JvmStatic
        fun isMultiplication(c: Char): Boolean {
            return c == '*' || c == '×' || c == '∙'
        }
    }
}
