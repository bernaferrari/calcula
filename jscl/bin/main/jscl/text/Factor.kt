package jscl.text

import jscl.math.Generic

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:45 PM
 */
internal class Factor private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val minus = MinusParser.parser.parse(p, previousSumElement)

        val result = UnsignedFactor.parser.parse(p, previousSumElement) as Generic

        return if (minus) result.negate() else result
    }

    companion object {
        @JvmField
        val parser: Parser<Generic> = Factor()
    }
}
