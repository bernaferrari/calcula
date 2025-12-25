package jscl.text

import jscl.math.Generic
import jscl.text.msg.Messages

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:40 PM
 */
object ParserUtils {

    @JvmStatic
    fun checkInterruption() {
        if (Thread.currentThread().isInterrupted) {
            throw ParseInterruptedException("Interrupted!")
        }
    }

    @JvmStatic
    fun skipWhitespaces(p: Parser.Parameters) {
        val position = p.position
        val expression = p.expression

        while (position.toInt() < expression.length && Character.isWhitespace(expression[position.toInt()])) {
            position.increment()
        }
    }

    @JvmStatic
    @Throws(ParseException::class)
    fun tryToParse(p: Parser.Parameters, pos0: Int, ch: Char) {
        skipWhitespaces(p)

        if (p.position.toInt() < p.expression.length) {
            val actual = p.expression[p.position.toInt()]
            if (actual == ch) {
                p.position.increment()
            } else {
                throwParseException(p, pos0, Messages.msg_12, ch)
            }
        } else {
            throwParseException(p, pos0, Messages.msg_12, ch)
        }
    }

    @JvmStatic
    @Throws(ParseException::class)
    fun tryToParse(p: Parser.Parameters, pos0: Int, s: String) {
        skipWhitespaces(p)

        if (p.position.toInt() < p.expression.length) {
            if (p.expression.startsWith(s, p.position.toInt())) {
                p.position.add(s.length)
            } else {
                throwParseException(p, pos0, Messages.msg_11, s)
            }
        } else {
            throwParseException(p, pos0, Messages.msg_11, s)
        }
    }

    @JvmStatic
    @Throws(ParseException::class)
    fun throwParseException(p: Parser.Parameters, pos0: Int, messageId: String) {
        throw makeParseException(p, pos0, messageId)
    }

    @JvmStatic
    fun makeParseException(p: Parser.Parameters, pos0: Int, messageId: String): ParseException {
        val position = p.position
        val parseException = p.exceptionsPool.obtain(position.toInt(), p.expression, messageId, emptyList<Any>())
        position.setValue(pos0)
        return parseException
    }

    @JvmStatic
    @Throws(ParseException::class)
    fun throwParseException(p: Parser.Parameters, pos0: Int, messageId: String, parameter: Any) {
        val position = p.position
        val parseException = p.exceptionsPool.obtain(position.toInt(), p.expression, messageId, listOf(parameter))
        position.setValue(pos0)
        throw parseException
    }

    @JvmStatic
    @Throws(ParseException::class)
    fun throwParseException(p: Parser.Parameters, pos0: Int, messageId: String, vararg parameters: Any?) {
        throw makeParseException(p, pos0, messageId, *parameters)
    }

    @JvmStatic
    fun makeParseException(p: Parser.Parameters, pos0: Int, messageId: String, vararg parameters: Any?): ParseException {
        val position = p.position
        val parseException = p.exceptionsPool.obtain(position.toInt(), p.expression, messageId, parameters)
        position.setValue(pos0)
        return parseException
    }

    @JvmStatic
    @Throws(ParseException::class)
    internal fun <T> parseWithRollback(
        parser: Parser<T>,
        initialPosition: Int,
        previousSumParser: Generic?,
        p: Parser.Parameters
    ): T {
        val result: T

        try {
            result = parser.parse(p, previousSumParser)
        } catch (e: ParseException) {
            p.position.setValue(initialPosition)
            throw e
        }

        return result
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> copyOf(array: kotlin.Array<out T>, newLength: Int): kotlin.Array<T> {
        return array.copyOf(newLength) as kotlin.Array<T>
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> copyOf(array: kotlin.Array<out T>): kotlin.Array<T> {
        return array.copyOf() as kotlin.Array<T>
    }
}
