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

        try {
            PowerParser.parser.parse(p, previousSumElement)
        } catch (e: ParseException) {
            p.position.value = pos0
            throw e
        }

        val result: Generic
        try {
            result = ExponentParser.parser.parse(p, previousSumElement)
        } catch (e: ParseException) {
            p.position.value = pos0
            throw e
        }

        return result
    }

    companion object {
        @JvmField
        val parser: Parser<Generic> = PowerExponentParser()
    }
}
