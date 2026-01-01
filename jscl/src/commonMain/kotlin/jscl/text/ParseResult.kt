package jscl.text

import jscl.math.Generic

/**
 * Result type for parsing operations.
 *
 * This provides a more Kotlin-idiomatic alternative to exception-based control flow
 * in parsing. Use [Success] when parsing succeeds, [Failure] when it fails.
 *
 * Example usage:
 * ```kotlin
 * when (val result = parser.tryParse(p, null)) {
 *     is ParseResult.Success -> processValue(result.value)
 *     is ParseResult.Failure -> handleError(result)
 * }
 * ```
 */
sealed class ParseResult<out T> {

    /**
     * Successful parse result containing the parsed value.
     */
    data class Success<T>(val value: T) : ParseResult<T>()

    /**
     * Failed parse result containing error information.
     */
    data class Failure(
        val position: Int,
        val expression: String,
        val messageCode: String,
        val parameters: List<Any> = emptyList()
    ) : ParseResult<Nothing>() {

        /**
         * Convert this failure to a ParseException for interop with existing code.
         */
        fun toException(): ParseException {
            return ParseException(position, expression, messageCode, *parameters.toTypedArray())
        }
    }

    /**
     * Returns true if this is a successful result.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this is a failure result.
     */
    val isFailure: Boolean get() = this is Failure

    /**
     * Returns the value if successful, or null if failed.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    /**
     * Returns the value if successful, or the result of [defaultValue] if failed.
     */
    inline fun getOrElse(defaultValue: () -> @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> defaultValue()
    }

    /**
     * Maps the successful value using [transform].
     */
    inline fun <R> map(transform: (T) -> R): ParseResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    /**
     * Flat maps the successful value using [transform].
     */
    inline fun <R> flatMap(transform: (T) -> ParseResult<R>): ParseResult<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    companion object {
        /**
         * Creates a Failure from a ParseException.
         */
        fun fromException(e: ParseException): Failure {
            return Failure(
                position = e.position,
                expression = e.expression,
                messageCode = e.getMessageCode(),
                parameters = e.getParameters()
            )
        }
    }
}

/**
 * Try to parse using the given parser, returning a ParseResult instead of throwing.
 */
inline fun <T> Parser<T>.tryParse(
    p: Parser.Parameters,
    previousSumElement: Generic?
): ParseResult<T> {
    return try {
        ParseResult.Success(parse(p, previousSumElement))
    } catch (e: ParseException) {
        ParseResult.fromException(e)
    }
}

/**
 * Try to parse, and if successful execute [onSuccess].
 * Returns the result for chaining.
 */
inline fun <T> Parser<T>.tryParseAndDo(
    p: Parser.Parameters,
    previousSumElement: Generic?,
    onSuccess: (T) -> Unit
): ParseResult<T> {
    val result = tryParse(p, previousSumElement)
    if (result is ParseResult.Success) {
        onSuccess(result.value)
    }
    return result
}

/**
 * Parse repeatedly while successful, accumulating results.
 * Stops on first failure (which is not an error, just signals end of sequence).
 */
inline fun <T, R> Parser<T>.parseWhileSuccessful(
    p: Parser.Parameters,
    previousSumElement: Generic?,
    initial: R,
    accumulate: (R, T) -> R
): R {
    var result = initial
    while (true) {
        when (val parseResult = tryParse(p, previousSumElement)) {
            is ParseResult.Success -> {
                result = accumulate(result, parseResult.value)
            }
            is ParseResult.Failure -> {
                p.exceptionsPool.release(parseResult.toException())
                break
            }
        }
    }
    return result
}

/**
 * Parse or throw, resetting position on failure.
 * Use this for mandatory parse steps where failure should propagate.
 */
@Throws(ParseException::class)
inline fun <T> Parser<T>.parseOrThrow(
    p: Parser.Parameters,
    previousSumElement: Generic?,
    resetPosition: Int
): T {
    return when (val result = tryParse(p, previousSumElement)) {
        is ParseResult.Success -> result.value
        is ParseResult.Failure -> {
            p.position.value = resetPosition
            throw result.toException()
        }
    }
}
