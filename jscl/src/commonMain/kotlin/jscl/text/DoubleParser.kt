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

        // Try parsing digits (optional at this point)
        when (val digitsResult = digitsParser.tryParse(p, previousSumElement)) {
            is ParseResult.Success -> {
                result.append(digitsResult.value)
                digits = true
            }
            is ParseResult.Failure -> p.exceptionsPool.release(digitsResult.toException())
        }

        // Try parsing decimal point (required if no digits yet)
        when (val pointResult = DecimalPoint.parser.tryParse(p, previousSumElement)) {
            is ParseResult.Success -> {
                result.append(".")
                point = true
            }
            is ParseResult.Failure -> {
                if (!digits) {
                    p.position.value = pos0
                    throw pointResult.toException()
                } else {
                    p.exceptionsPool.release(pointResult.toException())
                }
            }
        }

        if (point && nb != NumeralBase.dec) {
            ParserUtils.throwParseException(p, pos0, Messages.msg_15)
        }

        // Try parsing more digits after point (required if no digits before point)
        when (val digitsResult2 = digitsParser.tryParse(p, previousSumElement)) {
            is ParseResult.Success -> result.append(digitsResult2.value)
            is ParseResult.Failure -> {
                if (!digits) {
                    p.position.value = pos0
                    throw digitsResult2.toException()
                } else {
                    p.exceptionsPool.release(digitsResult2.toException())
                }
            }
        }

        // Try parsing exponent (required if no point)
        when (val expResult = ExponentPart.parser.tryParse(p, previousSumElement)) {
            is ParseResult.Success -> {
                result.append(expResult.value)
                exponent = true
            }
            is ParseResult.Failure -> {
                if (!point) {
                    p.position.value = pos0
                    throw expResult.toException()
                } else {
                    p.exceptionsPool.release(expResult.toException())
                }
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
        val parser: Parser<Double> = FloatingPointLiteral()
    }
}

internal class DecimalPoint private constructor() : Parser<Unit?> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Unit? {
        val pos0 = p.position.toInt()

        ParserUtils.skipWhitespaces(p)

        ParserUtils.tryToParse(p, pos0, '.')

        return null
    }

    companion object {
        val parser: Parser<Unit?> = DecimalPoint()
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

        // Parse signed integer, reset position on failure
        result.append(SignedInteger.parser.parseOrThrow(p, previousSumElement, pos0))

        return result.toString()
    }

    companion object {
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

        // Parse integer, reset position on failure
        result.append(IntegerParser.parser.parseOrThrow(p, previousSumElement, pos0))

        return result.toString()
    }

    companion object {
        val parser: Parser<String> = SignedInteger()
    }
}
