package jscl.text

import jscl.math.Generic
import jscl.text.msg.Messages

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:45 PM
 */
internal class PowerParser private constructor() : Parser<Unit?> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Unit? {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        if (p.position.toInt() < p.expression.length && p.expression[p.position.toInt()] == '^') {
            p.position.increment()
        } else {
            if (isDoubleStar(p.expression, p.position.toInt())) {
                p.position.increment()
                p.position.increment()
            } else {
                ParserUtils.throwParseException(p, pos0, Messages.msg_10, '^', "**")
            }
        }

        return null
    }

    private fun isDoubleStar(string: String, position: Int): Boolean {
        return position + 1 < string.length && MultiplyFactor.isMultiplication(string[position]) && MultiplyFactor.isMultiplication(string[position + 1])
    }

    companion object {
        val parser: Parser<Unit?> = PowerParser()
    }
}
