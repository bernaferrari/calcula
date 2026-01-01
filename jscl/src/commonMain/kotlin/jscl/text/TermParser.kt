package jscl.text

import jscl.math.Generic
import jscl.math.GenericVariable
import jscl.math.JsclInteger
import jscl.math.function.Fraction
import jscl.math.function.Inverse

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:45 PM
 */
internal class TermParser private constructor() : Parser<Generic> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Generic {
        var result: Generic = JsclInteger.valueOf(1)

        var s = UnsignedFactor.parser.parse(p, previousSumElement) as Generic

        // Parse multiplication/division chain using Result-based approach
        // Try multiply first, if that fails try divide, if both fail we're done
        while (true) {
            when (val multiplyResult = MultiplyFactor.parser.tryParse(p, null)) {
                is ParseResult.Success -> {
                    result = result.multiply(s)
                    s = multiplyResult.value
                }
                is ParseResult.Failure -> {
                    p.exceptionsPool.release(multiplyResult.toException())
                    when (val divideResult = DivideFactor.parser.tryParse(p, null)) {
                        is ParseResult.Success -> {
                            val b = divideResult.value
                            s = if (s.compareTo(JsclInteger.valueOf(1)) == 0)
                                Inverse(GenericVariable.content(b, true)).expressionValue()
                            else
                                Fraction(GenericVariable.content(s, true), GenericVariable.content(b, true)).expressionValue()
                        }
                        is ParseResult.Failure -> {
                            p.exceptionsPool.release(divideResult.toException())
                            break
                        }
                    }
                }
            }
        }

        result = result.multiply(s)

        return result
    }

    companion object {
        val parser: Parser<Generic> = TermParser()
    }
}
