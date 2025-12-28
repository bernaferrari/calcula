package jscl.text

import jscl.math.Generic
import jscl.math.function.Constant
import jscl.util.ArrayUtils

class ConstantParser private constructor() : Parser<Constant> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Constant {
        val name = CompoundIdentifier.parser.parse(p, previousSumElement)

        val l = ArrayList<Generic>()
        while (true) {
            try {
                l.add(Subscript.parser.parse(p, previousSumElement))
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
                break
            }
        }

        var prime = 0
        try {
            prime = Prime.parser.parse(p, previousSumElement)
        } catch (e: ParseException) {
            p.exceptionsPool.release(e)
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

        val result: Int
        try {
            result = IntegerParser.parser.parse(p, previousSumElement)
        } catch (e: ParseException) {
            p.position.value = pos0
            throw e
        }

        ParserUtils.tryToParse(p, pos0, '}')

        return result
    }

    companion object {
        val parser: Parser<Int> = Superscript()
    }
}
