package jscl.text

import jscl.math.Generic

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:44 PM
 */
internal class MinusParser private constructor() : Parser<Boolean> {

    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Boolean {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        val pos1 = p.position.toInt()
        return if (pos1 < p.expression.length && isMinus(p.expression[pos1])) {
            p.position.increment()
            true
        } else {
            p.position.setValue(pos0)
            false
        }
    }

    companion object {
        @JvmField
        val parser: Parser<Boolean> = MinusParser()

        @JvmStatic
        fun isMinus(c: Char): Boolean {
            return c == '-' || c == '−'
        }
    }
}
