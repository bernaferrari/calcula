package jscl.text

import jscl.NumeralBase
import jscl.math.Generic

class NumeralBaseParser private constructor() : Parser<NumeralBase> {

    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): NumeralBase {
        val pos0 = p.position.toInt()

        var result = p.context.getNumeralBase()

        ParserUtils.skipWhitespaces(p)

        // Try each numeral base prefix until one matches
        for (numeralBase in NumeralBase.values()) {
            val jsclPrefix = numeralBase.getJsclPrefix()
            when (val prefixResult = ParserUtils.tryToParseResult(p, pos0, jsclPrefix)) {
                is ParseResult.Success -> {
                    result = numeralBase
                    break
                }
                is ParseResult.Failure -> p.exceptionsPool.release(prefixResult.toException())
            }
        }

        return result
    }

    companion object {
        val parser: Parser<NumeralBase> = NumeralBaseParser()
    }
}
