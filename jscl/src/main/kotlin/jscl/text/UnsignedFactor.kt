package jscl.text

import jscl.math.Generic
import jscl.math.GenericVariable
import jscl.math.JsclInteger
import jscl.math.NotIntegerException
import jscl.math.function.Pow

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

        while (true) {
            try {
                list.add(PowerExponentParser.parser.parse(p, null))
            } catch (e: ParseException) {
                p.exceptionsPool.release(e)
                break
            }
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
                    b.pow(c)
                }
            } catch (e: NotIntegerException) {
                Pow(GenericVariable.content(b, true), GenericVariable.content(generic, true)).expressionValue()
            }
        }

        return generic
    }

    companion object {
        @JvmField
        val parser: Parser<*> = UnsignedFactor()
    }
}
