package jscl.text

import jscl.math.Generic

class MultiTryParser<T>(
    private val parsers: List<Parser<out T>>
) : Parser<T> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): T {
        // Try each parser in order until one succeeds
        for ((index, parser) in parsers.withIndex()) {
            when (val result = parser.tryParse(p, previousSumElement)) {
                is ParseResult.Success -> return result.value
                is ParseResult.Failure -> {
                    val exception = result.toException()
                    p.addException(exception)
                    // If this is the last parser, throw the exception
                    if (index == parsers.lastIndex) {
                        throw exception
                    }
                }
            }
        }

        // Should never reach here if parsers is non-empty
        throw IllegalStateException("No parsers provided")
    }
}
