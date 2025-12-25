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
            p.position.setValue(pos0)
            throw p.exceptionsPool.obtain(p.position.toInt(), p.expression, Messages.msg_6, listOf(name))
        }

        val subscripts = ArrayList<Generic>()
        while (true) {
            try {
                subscripts.add(Subscript.parser.parse(p, previousSumElement))
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
                break
            }
        }

        var b: IntArray
        try {
            b = Derivation.parser.parse(p, previousSumElement)
        } catch (e: ParseException) {
            p.exceptionsPool.release(e)
            b = IntArray(0)
        }
        try {
            a = ParameterListParser.parser1.parse(p, previousSumElement)
        } catch (e: ParseException) {
            p.position.setValue(pos0)
            throw e
        }

        val derivations = IntArray(a.size)
        for (i in 0 until a.size.coerceAtMost(b.size)) {
            derivations[i] = b[i]
        }

        @Suppress("UNCHECKED_CAST")
        return ImplicitFunction(name, a, derivations, ArrayUtils.toArray(subscripts, arrayOfNulls<Generic>(subscripts.size)) as Array<Generic>)
    }

    companion object {
        @JvmField
        val parser: Parser<Function> = ImplicitFunctionParser()
    }
}

internal class Derivation private constructor() : Parser<IntArray> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): IntArray {
        try {
            return intArrayOf(PrimeCharacters.parser.parse(p, previousSumElement))
        } catch (e: ParseException) {
            p.exceptionsPool.release(e)
        }
        return SuperscriptList.parser.parse(p, previousSumElement)
    }

    companion object {
        @JvmField
        val parser: Parser<IntArray> = Derivation()
    }
}

internal class SuperscriptList private constructor() : Parser<IntArray> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): IntArray {
        val pos0 = p.position.toInt()

        ParserUtils.tryToParse(p, pos0, '{')

        val result = ArrayList<Int>()
        try {
            result.add(IntegerParser.parser.parse(p, previousSumElement))
        } catch (e: ParseException) {
            p.position.setValue(pos0)
            throw e
        }

        while (true) {
            try {
                result.add(CommaAndInteger.parser.parse(p, previousSumElement))
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
                break
            }
        }

        ParserUtils.tryToParse(p, pos0, '}')

        ParserUtils.skipWhitespaces(p)

        return ArrayUtils.toArray(result, IntArray(result.size))
    }

    companion object {
        @JvmField
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
        @JvmField
        val parser: Parser<Int> = CommaAndInteger()
    }
}
