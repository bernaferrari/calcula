package jscl.text

import jscl.math.Generic

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:45 PM
 */
internal class ExponentParser private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val pos0 = p.position.toInt()

        val minus = MinusParser.parser.parse(p, previousSumElement)

        val result = ParserUtils.parseWithRollback(UnsignedExponent.parser, pos0, previousSumElement, p)
        return if (minus) result.negate() else result
    }

    companion object {
        @JvmField
        val parser: Parser<Generic> = ExponentParser()
    }
}
