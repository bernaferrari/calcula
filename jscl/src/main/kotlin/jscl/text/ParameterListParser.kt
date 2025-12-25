package jscl.text

import jscl.math.Generic
import jscl.util.ArrayUtils

class ParameterListParser(
    private val minNumberOfParameters: Int = 1
) : Parser<Array<Generic>> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Array<Generic> {
        val pos0 = p.position.toInt()

        val result = ArrayList<Generic>()

        ParserUtils.tryToParse(p, pos0, '(')

        try {
            result.add(ExpressionParser.parser.parse(p, previousSumElement))
        } catch (e: ParseException) {
            if (minNumberOfParameters > 0) {
                p.position.setValue(pos0)
                throw e
            } else {
                p.exceptionsPool.release(e)
            }
        }

        while (true) {
            try {
                result.add(CommaAndExpression.parser.parse(p, previousSumElement))
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
                break
            }
        }

        ParserUtils.tryToParse(p, pos0, ')')

        @Suppress("UNCHECKED_CAST")
        return ArrayUtils.toArray(result, arrayOfNulls<Generic>(result.size)) as Array<Generic>
    }

    companion object {
        @JvmField
        val parser1: Parser<Array<Generic>> = ParameterListParser()
    }
}
