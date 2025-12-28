package jscl.text

import jscl.math.Generic
import jscl.math.JsclVector

class CommaAndVector private constructor() : Parser<JsclVector> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): JsclVector {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        ParserUtils.tryToParse(p, pos0, ',')

        return ParserUtils.parseWithRollback(VectorParser.parser, pos0, previousSumElement, p)
    }

    companion object {
        val parser: Parser<JsclVector> = CommaAndVector()
    }
}
