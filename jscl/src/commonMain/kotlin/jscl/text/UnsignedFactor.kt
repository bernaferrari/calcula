package jscl.text

import jscl.math.Generic
import jscl.math.GenericVariable
import jscl.math.JsclInteger
import jscl.math.NotIntegerException
import jscl.math.NumericWrapper
import jscl.math.function.Pow
import jscl.math.numeric.Real

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:45 PM
 */
internal class UnsignedFactor private constructor() : Parser<Any> {

    @Throws(ParseException::class)
    override fun parse(p: Parser.Parameters, previousSumElement: Generic?): Any {
        val list = ArrayList<Generic>()

        var generic = UnsignedExponent.parser.parse(p, previousSumElement)

        list.add(generic)

        // Parse additional power exponents using Result-based approach
        PowerExponentParser.parser.parseWhileSuccessful(p, null, Unit) { _, exponent ->
            list.add(exponent)
        }

        val it = list.listIterator(list.size)
        generic = it.previous()
        while (it.hasPrevious()) {
            val b = it.previous()
            generic = try {
                val c = generic.integerValue().toInt()
                if (c < 0) {
                    Pow(GenericVariable.content(b, true), JsclInteger.valueOf(c.toLong())).expressionValue()
                } else {
                    if (c == 0) {
                        val baseValue = numericDoubleOrNull(b)
                        if (baseValue != null && baseValue.isNaN()) {
                            NumericWrapper.valueOf(Double.NaN)
                        } else {
                            b.pow(c)
                        }
                    } else {
                        b.pow(c)
                    }
                }
            } catch (e: NotIntegerException) {
                Pow(GenericVariable.content(b, true), GenericVariable.content(generic, true)).expressionValue()
            }
        }

        return generic
    }

    private fun numericDoubleOrNull(generic: Generic): Double? {
        val numeric = try {
            generic.numeric()
        } catch (_: Exception) {
            return null
        }
        val wrapper = numeric as? NumericWrapper ?: return null
        val content = wrapper.content()
        return if (content is Real) {
            content.doubleValue()
        } else {
            null
        }
    }

    companion object {
        val parser: Parser<*> = UnsignedFactor()
    }
}
