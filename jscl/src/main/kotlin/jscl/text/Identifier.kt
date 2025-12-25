package jscl.text

import jscl.math.Generic
import jscl.text.msg.Messages

class Identifier private constructor() : Parser<String> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): String {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        val result: StringBuilder
        if (p.position.toInt() < p.expression.length && isValidFirstCharacter(p.expression[p.position.toInt()])) {
            result = StringBuilder()
            result.append(p.expression[p.position.toInt()])
            p.position.increment()
        } else {
            throw ParserUtils.makeParseException(p, pos0, Messages.msg_5)
        }

        while (p.position.toInt() < p.expression.length && isValidNotFirstCharacter(p.expression, p.position)) {
            result.append(p.expression[p.position.toInt()])
            p.position.increment()
        }

        return result.toString()
    }

    companion object {
        @JvmField
        val parser: Parser<String> = Identifier()
        private val allowedCharacters = listOf('√', '∞', 'π', '∂', '∏', 'Σ', '∫')

        private fun isValidFirstCharacter(ch: Char): Boolean {
            return Character.isLetter(ch) || allowedCharacters.contains(ch)
        }

        private fun isValidNotFirstCharacter(string: String, position: MutableInt): Boolean {
            val ch = string[position.toInt()]
            return Character.isLetter(ch) || Character.isDigit(ch) || ch == '_'
        }
    }
}
