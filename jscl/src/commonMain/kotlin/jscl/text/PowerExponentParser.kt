package jscl.text

import jscl.math.Generic

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:45 PM
 */
internal class PowerExponentParser private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val pos0 = p.position.toInt()

        // Parse power operator, reset position on failure
        PowerParser.parser.parseOrThrow(p, previousSumElement, pos0)

        // Parse exponent, reset position on failure
        return ExponentParser.parser.parseOrThrow(p, previousSumElement, pos0)
    }

    companion object {
        val parser: Parser<Generic> = PowerExponentParser()
    }
}
