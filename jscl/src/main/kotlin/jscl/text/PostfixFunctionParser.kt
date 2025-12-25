package jscl.text

import jscl.math.Generic

open class PostfixFunctionParser(
    private val name: String
) : Parser<String?> {

    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): String? {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        return if (p.position.toInt() < p.expression.length && p.expression.startsWith(name, p.position.toInt())) {
            p.position.add(name.length)
            name
        } else {
            p.position.setValue(pos0)
            null
        }
    }
}
