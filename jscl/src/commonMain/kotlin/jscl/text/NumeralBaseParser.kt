package jscl.text

import jscl.NumeralBase
import jscl.math.Generic

class NumeralBaseParser private constructor() : Parser<NumeralBase> {

    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): NumeralBase {
        val pos0 = p.position.toInt()

        var result = p.context.getNumeralBase()

        ParserUtils.skipWhitespaces(p)

        for (numeralBase in NumeralBase.values()) {
            try {
                val jsclPrefix = numeralBase.getJsclPrefix()
                ParserUtils.tryToParse(p, pos0, jsclPrefix)
                result = numeralBase
                break
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
            }
        }

        return result
    }

    companion object {
        val parser: Parser<NumeralBase> = NumeralBaseParser()
    }
}
