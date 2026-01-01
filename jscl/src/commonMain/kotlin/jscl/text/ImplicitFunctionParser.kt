package jscl.text

import jscl.math.Generic
import jscl.math.function.Function
import jscl.math.function.FunctionsRegistry
import jscl.math.function.ImplicitFunction
import jscl.math.operator.matrix.OperatorsRegistry
import jscl.text.msg.Messages
import jscl.util.ArrayUtils

class ImplicitFunctionParser private constructor() : Parser<Function> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Function {
        val pos0 = p.position.toInt()
        val a: Array<Generic>

        val name = ParserUtils.parseWithRollback(CompoundIdentifier.parser, pos0, previousSumElement, p)
        if (FunctionsRegistry.getInstance().getNames().contains(name) || OperatorsRegistry.getInstance().getNames().contains(name)) {
            p.position.value = pos0
            throw p.exceptionsPool.obtain(p.position.toInt(), p.expression, Messages.msg_6, listOf(name))
        }

        // Parse subscripts using Result-based approach
        val subscripts = ArrayList<Generic>()
        Subscript.parser.parseWhileSuccessful(p, previousSumElement, Unit) { _, subscript ->
            subscripts.add(subscript)
        }

        // Parse optional derivation
        val b = when (val derivResult = Derivation.parser.tryParse(p, previousSumElement)) {
            is ParseResult.Success -> derivResult.value
            is ParseResult.Failure -> {
                p.exceptionsPool.release(derivResult.toException())
                IntArray(0)
            }
        }

        // Parse parameter list, reset position on failure
        a = ParameterListParser.parser1.parseOrThrow(p, previousSumElement, pos0)

        val derivations = IntArray(a.size)
        for (i in 0 until a.size.coerceAtMost(b.size)) {
            derivations[i] = b[i]
        }

        @Suppress("UNCHECKED_CAST")
        return ImplicitFunction(name, a, derivations, ArrayUtils.toArray(subscripts, arrayOfNulls<Generic>(subscripts.size)) as Array<Generic>)
    }

    companion object {
        val parser: Parser<Function> = ImplicitFunctionParser()
    }
}

internal class Derivation private constructor() : Parser<IntArray> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): IntArray {
        // Try prime characters first, then fall back to superscript list
        return when (val primeResult = PrimeCharacters.parser.tryParse(p, previousSumElement)) {
            is ParseResult.Success -> intArrayOf(primeResult.value)
            is ParseResult.Failure -> {
                p.exceptionsPool.release(primeResult.toException())
                SuperscriptList.parser.parse(p, previousSumElement)
            }
        }
    }

    companion object {
        val parser: Parser<IntArray> = Derivation()
    }
}

internal class SuperscriptList private constructor() : Parser<IntArray> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): IntArray {
        val pos0 = p.position.toInt()

        ParserUtils.tryToParse(p, pos0, '{')

        // Parse first integer, reset position on failure
        val result = ArrayList<Int>()
        result.add(IntegerParser.parser.parseOrThrow(p, previousSumElement, pos0))

        // Parse additional comma-separated integers
        CommaAndInteger.parser.parseWhileSuccessful(p, previousSumElement, Unit) { _, integer ->
            result.add(integer)
        }

        ParserUtils.tryToParse(p, pos0, '}')

        ParserUtils.skipWhitespaces(p)

        return ArrayUtils.toArray(result, IntArray(result.size))
    }

    companion object {
        val parser: Parser<IntArray> = SuperscriptList()
    }
}

internal class CommaAndInteger private constructor() : Parser<Int> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Int {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        return ParserUtils.parseWithRollback(IntegerParser.parser, pos0, previousSumElement, p)
    }

    companion object {
        val parser: Parser<Int> = CommaAndInteger()
    }
}
