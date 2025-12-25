package jscl.text

import jscl.NumeralBase
import jscl.math.Generic
import jscl.math.NumericWrapper
import jscl.math.numeric.Real
import jscl.text.msg.Messages

class DoubleParser private constructor() : Parser<NumericWrapper> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): NumericWrapper {
        val multiTryParser: Parser<Double> = MultiTryParser(parsers.toMutableList())
        return NumericWrapper(Real.valueOf(multiTryParser.parse(p, previousSumElement)))
    }

    companion object {
        @JvmField
        val parser: Parser<NumericWrapper> = DoubleParser()

        private val parsers: List<Parser<Double>> = listOf(
            Singularity.parser,
            FloatingPointLiteral.parser
        )
    }
}

internal class Singularity private constructor() : Parser<Double> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Double {
        val pos0 = p.position.toInt()

        var result = 0.0

        val s = Identifier.parser.parse(p, previousSumElement)
        when (s) {
            "NaN" -> result = Double.NaN
            "Infinity", "∞" -> result = Double.POSITIVE_INFINITY
            else -> ParserUtils.throwParseException(p, pos0, Messages.msg_10, "NaN", "∞")
        }

        return result
    }

    companion object {
        @JvmField
        val parser: Parser<Double> = Singularity()
    }
}

internal class FloatingPointLiteral private constructor() : Parser<Double> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Double {
        val pos0 = p.position.toInt()

        val nb = NumeralBaseParser.parser.parse(p, previousSumElement)

        var digits = false
        var point = false
        var exponent = false

        val digitsParser = Digits(nb)

        val result = StringBuilder()
        try {
            result.append(digitsParser.parse(p, previousSumElement))
            digits = true
        } catch (e: ParseException) {
            p.exceptionsPool.release(e)
        }

        try {
            DecimalPoint.parser.parse(p, previousSumElement)
            result.append(".")
            point = true
        } catch (e: ParseException) {
            if (!digits) {
                p.position.setValue(pos0)
                throw e
            } else {
                p.exceptionsPool.release(e)
            }
        }

        if (point && nb != NumeralBase.dec) {
            ParserUtils.throwParseException(p, pos0, Messages.msg_15)
        }

        try {
            result.append(digitsParser.parse(p, previousSumElement))
        } catch (e: ParseException) {
            if (!digits) {
                p.position.setValue(pos0)
                throw e
            } else {
                p.exceptionsPool.release(e)
            }
        }

        try {
            result.append(ExponentPart.parser.parse(p, previousSumElement))
            exponent = true
        } catch (e: ParseException) {
            if (!point) {
                p.position.setValue(pos0)
                throw e
            } else {
                p.exceptionsPool.release(e)
            }
        }

        if (exponent && nb != NumeralBase.dec) {
            ParserUtils.throwParseException(p, pos0, Messages.msg_15)
        }

        val doubleString = result.toString()
        try {
            return nb.toDouble(doubleString)
        } catch (e: NumberFormatException) {
            throw p.exceptionsPool.obtain(p.position.toInt(), p.expression, Messages.msg_8, listOf(doubleString))
        }
    }

    companion object {
        @JvmField
        val parser: Parser<Double> = FloatingPointLiteral()
    }
}

internal class DecimalPoint private constructor() : Parser<Void?> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Void? {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        ParserUtils.tryToParse(p, pos0, '.')

        return null
    }

    companion object {
        @JvmField
        val parser: Parser<Void?> = DecimalPoint()
    }
}

internal class ExponentPart private constructor() : Parser<String> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): String {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        val result: StringBuilder
        if (p.position.toInt() < p.expression.length && (p.expression[p.position.toInt()] == 'e' || p.expression[p.position.toInt()] == 'E')) {
            result = StringBuilder()
            result.append(p.expression[p.position.toInt()])
            p.position.increment()
        } else {
            throw ParserUtils.makeParseException(p, pos0, Messages.msg_10, 'e', 'E')
        }

        try {
            result.append(SignedInteger.parser.parse(p, previousSumElement))
        } catch (e: ParseException) {
            p.position.setValue(pos0)
            throw e
        }

        return result.toString()
    }

    companion object {
        @JvmField
        val parser: Parser<String> = ExponentPart()
    }
}

internal class SignedInteger private constructor() : Parser<String> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): String {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        val result = StringBuilder()
        val pos1 = p.position.toInt()
        if (pos1 < p.expression.length && (p.expression[pos1] == '+' || MinusParser.isMinus(p.expression[pos1]))) {
            val c = p.expression[pos1]
            p.position.increment()
            result.append(c)
        }

        try {
            result.append(IntegerParser.parser.parse(p, previousSumElement))
        } catch (e: ParseException) {
            p.position.setValue(pos0)
            throw e
        }

        return result.toString()
    }

    companion object {
        @JvmField
        val parser: Parser<String> = SignedInteger()
    }
}
