package jscl.text

import jscl.math.Generic

class CompoundIdentifier private constructor() : Parser<String> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): String {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        // Parse first identifier, reset position on failure
        val result = StringBuilder()
        result.append(Identifier.parser.parseOrThrow(p, previousSumElement, pos0))

        // Parse additional dot-separated identifiers
        DotAndIdentifier.parser.parseWhileSuccessful(p, previousSumElement, Unit) { _, dotAndId ->
            result.append(".").append(dotAndId)
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

        // Parse identifier, reset position on failure
        return Identifier.parser.parseOrThrow(p, previousSumElement, pos0)
    }

    companion object {
        val parser: Parser<String> = DotAndIdentifier()
    }
}
