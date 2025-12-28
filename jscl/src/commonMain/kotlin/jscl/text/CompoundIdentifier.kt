package jscl.text

import jscl.math.Generic

class CompoundIdentifier private constructor() : Parser<String> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): String {
        val pos0 = p.position.toInt()

        val result: StringBuilder

        ParserUtils.skipWhitespaces(p)
        try {
            val identifier = Identifier.parser.parse(p, previousSumElement)
            result = StringBuilder()
            result.append(identifier)
        } catch (e: ParseException) {
            p.position.value = pos0
            throw e
        }

        while (true) {
            try {
                val dotAndId = DotAndIdentifier.parser.parse(p, previousSumElement)
                // NOTE: '.' must be appended after parsing
                result.append(".").append(dotAndId)
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
                break
            }
        }

        return result.toString()
    }

    companion object {
        val parser: Parser<String> = CompoundIdentifier()
    }
}

internal class DotAndIdentifier private constructor() : Parser<String> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): String {
        val pos0 = p.position.toInt()

        ParserUtils.tryToParse(p, pos0, '.')

        val result: String
        try {
            result = Identifier.parser.parse(p, previousSumElement)
        } catch (e: ParseException) {
            p.position.value = pos0
            throw e
        }

        return result
    }

    companion object {
        val parser: Parser<String> = DotAndIdentifier()
    }
}
