package jscl.text

import jscl.math.Generic
import jscl.math.JsclVector
import jscl.util.ArrayUtils

class VectorParser private constructor() : Parser<JsclVector> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): JsclVector {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        ParserUtils.tryToParse(p, pos0, '[')

        val result = ArrayList<Generic>()
        try {
            result.add(ExpressionParser.parser.parse(p, previousSumElement))
        } catch (e: ParseException) {
            p.position.value = pos0
            throw e
        }

        while (true) {
            try {
                result.add(CommaAndExpression.parser.parse(p, previousSumElement))
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
                break
            }
        }

        ParserUtils.skipWhitespaces(p)

        ParserUtils.tryToParse(p, pos0, ']')

        @Suppress("UNCHECKED_CAST")
        return JsclVector(ArrayUtils.toArray(result, arrayOfNulls<Generic>(result.size)) as Array<Generic>)
    }

    companion object {
        @JvmField
        val parser: Parser<JsclVector> = VectorParser()
    }
}
