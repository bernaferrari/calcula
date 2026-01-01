package jscl.text

import jscl.math.Generic
import jscl.text.msg.Messages

/**
 * User: serso
 * Date: 10/27/11
 * Time: 2:40 PM
 */
object ParserUtils {

    fun checkInterruption() {
        // In Kotlin Multiplatform, Thread interruption doesn't exist in common code.
        // This is now a no-op. If needed, use coroutine cancellation instead.
    }

    fun skipWhitespaces(p: Parser.Parameters) {
        val position = p.position
        val expression = p.expression

        while (position.toInt() < expression.length && expression[position.toInt()].isWhitespace()) {
            position.increment()
        }
    }

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

    /**
     * Try to parse a string, returning a ParseResult instead of throwing.
     */
    fun tryToParseResult(p: Parser.Parameters, pos0: Int, s: String): ParseResult<Unit> {
        skipWhitespaces(p)

        return if (p.position.toInt() < p.expression.length && p.expression.startsWith(s, p.position.toInt())) {
            p.position.add(s.length)
            ParseResult.Success(Unit)
        } else {
            val exception = p.exceptionsPool.obtain(p.position.toInt(), p.expression, Messages.msg_11, listOf(s))
            p.position.value = pos0
            ParseResult.fromException(exception)
        }
    }

    @Throws(ParseException::class)
    fun throwParseException(p: Parser.Parameters, pos0: Int, messageId: String) {
        throw makeParseException(p, pos0, messageId)
    }

    fun makeParseException(p: Parser.Parameters, pos0: Int, messageId: String): ParseException {
        val position = p.position
        val parseException = p.exceptionsPool.obtain(position.toInt(), p.expression, messageId, emptyList<Any>())
        position.value = pos0
        return parseException
    }

    @Throws(ParseException::class)
    fun throwParseException(p: Parser.Parameters, pos0: Int, messageId: String, parameter: Any) {
        val position = p.position
        val parseException = p.exceptionsPool.obtain(position.toInt(), p.expression, messageId, listOf(parameter))
        position.value = pos0
        throw parseException
    }

    @Throws(ParseException::class)
    fun throwParseException(p: Parser.Parameters, pos0: Int, messageId: String, vararg parameters: Any?) {
        throw makeParseException(p, pos0, messageId, *parameters)
    }

    fun makeParseException(p: Parser.Parameters, pos0: Int, messageId: String, vararg parameters: Any?): ParseException {
        val position = p.position
        val parseException = p.exceptionsPool.obtain(position.toInt(), p.expression, messageId, parameters)
        position.value = pos0
        return parseException
    }

    @Throws(ParseException::class)
    internal fun <T> parseWithRollback(
        parser: Parser<T>,
        initialPosition: Int,
        previousSumParser: Generic?,
        p: Parser.Parameters
    ): T {
        // Use parseOrThrow from ParseResult for consistent behavior
        return parser.parseOrThrow(p, previousSumParser, initialPosition)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> copyOf(array: kotlin.Array<out T>, newLength: Int): kotlin.Array<T> {
        return array.copyOf(newLength) as kotlin.Array<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> copyOf(array: kotlin.Array<out T>): kotlin.Array<T> {
        return array.copyOf() as kotlin.Array<T>
    }
}
