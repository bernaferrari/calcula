package jscl.text

import jscl.math.Generic
import jscl.math.function.Constant
import jscl.util.ArrayUtils

class ConstantParser private constructor() : Parser<Constant> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Constant {
        val name = CompoundIdentifier.parser.parse(p, previousSumElement)

        // Parse subscripts using Result-based approach
        val l = ArrayList<Generic>()
        Subscript.parser.parseWhileSuccessful(p, previousSumElement, Unit) { _, subscript ->
            l.add(subscript)
        }

        // Parse optional prime
        val prime = when (val primeResult = Prime.parser.tryParse(p, previousSumElement)) {
            is ParseResult.Success -> primeResult.value
            is ParseResult.Failure -> {
                p.exceptionsPool.release(primeResult.toException())
                0
            }
        }

        @Suppress("UNCHECKED_CAST")
        return Constant(name, prime, ArrayUtils.toArray(l, arrayOfNulls<Generic>(l.size)) as Array<Generic>)
    }

    companion object {
        val parser: Parser<Constant> = ConstantParser()
    }
}

internal class Prime private constructor() : Parser<Int> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Int {
        return internalParser.parse(p, previousSumElement)
    }

    companion object {
        val parser: Parser<Int> = Prime()

        private val parsers = ArrayList<Parser<out Int>>(
            listOf(
                PrimeCharacters.parser,
                Superscript.parser
            )
        )

        private val internalParser: Parser<Int> = MultiTryParser(parsers)
    }
}

internal class Superscript private constructor() : Parser<Int> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Int {
        val pos0 = p.position.toInt()

        ParserUtils.tryToParse(p, pos0, '{')

        // Parse integer, reset position on failure
        val result = IntegerParser.parser.parseOrThrow(p, previousSumElement, pos0)

        ParserUtils.tryToParse(p, pos0, '}')

        return result
    }

    companion object {
        val parser: Parser<Int> = Superscript()
    }
}
