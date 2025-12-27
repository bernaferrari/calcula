package jscl.text

import jscl.math.Generic

class ExpressionParser private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        val minus = MinusParser.parser.parse(p, previousSumElement)

        var result = TermParser.parser.parse(p, previousSumElement)

        if (minus) {
            result = result.negate()
        }

        while (true) {
            try {
                result = result.add(PlusOrMinusTerm.parser.parse(p, result))
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
                break
            }
        }

        return result
    }

    companion object {
        @JvmField
        val parser: Parser<Generic> = ExpressionParser()
    }
}
