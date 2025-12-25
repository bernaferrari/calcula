package jscl.text

import jscl.NumeralBase
import jscl.math.Generic
import jscl.text.msg.Messages

class IntegerParser private constructor() : Parser<Int> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Int {
        val pos0 = p.position.toInt()

        val nb = NumeralBaseParser.parser.parse(p, previousSumElement)

        ParserUtils.skipWhitespaces(p)
        val result: StringBuilder
        if (p.position.toInt() < p.expression.length && nb.getAcceptableCharacters().contains(p.expression[p.position.toInt()])) {
            val c = p.expression[p.position.toInt()]
            p.position.increment()
            result = StringBuilder()
            result.append(c)
        } else {
            p.position.setValue(pos0)
            throw p.exceptionsPool.obtain(p.position.toInt(), p.expression, Messages.msg_7)
        }

        while (p.position.toInt() < p.expression.length && nb.getAcceptableCharacters().contains(p.expression[p.position.toInt()])) {
            val c = p.expression[p.position.toInt()]
            p.position.increment()
            result.append(c)
        }

        val number = result.toString()
        try {
            return nb.toInteger(number)
        } catch (e: NumberFormatException) {
            throw p.exceptionsPool.obtain(p.position.toInt(), p.expression, Messages.msg_8, listOf(number))
        }
    }

    companion object {
        @JvmField
        val parser: Parser<Int> = IntegerParser()
    }
}
