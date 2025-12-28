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

        while (true) {
            try {
                val b = MultiplyFactor.parser.parse(p, null)
                result = result.multiply(s)
                s = b
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
                try {
                    val b = DivideFactor.parser.parse(p, null)
                    s = if (s.compareTo(JsclInteger.valueOf(1)) == 0)
                        Inverse(GenericVariable.content(b, true)).expressionValue()
                    else
                        Fraction(GenericVariable.content(s, true), GenericVariable.content(b, true)).expressionValue()
                } catch (e2: ParseException) {
                    p.exceptionsPool.release(e2)
                    break
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
