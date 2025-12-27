package jscl.text

import jscl.NumeralBase
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.text.msg.Messages

class JsclIntegerParser private constructor() : Parser<JsclInteger> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): JsclInteger {
        val pos0 = p.position.toInt()

        val nb = NumeralBaseParser.parser.parse(p, previousSumElement)

        val number: String
        try {
            number = Digits(nb).parse(p, previousSumElement)
        } catch (e: ParseException) {
            p.position.value = pos0
            throw e
        }

        try {
            return nb.toJsclInteger(number)
        } catch (e: NumberFormatException) {
            throw p.exceptionsPool.obtain(p.position.toInt(), p.expression, Messages.msg_8, listOf(number))
        }
    }

    companion object {
        @JvmField
        val parser: Parser<JsclInteger> = JsclIntegerParser()
    }
}
