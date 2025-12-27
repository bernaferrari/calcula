package jscl.text

import jscl.math.Generic

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:45 PM
 */
internal class UnsignedExponent private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val content = PrimaryExpressionParser.parser.parse(p, previousSumElement)
        return PostfixFunctionsParser(content).parse(p, previousSumElement)
    }

    companion object {
        @JvmField
        val parser: Parser<Generic> = UnsignedExponent()
    }
}
