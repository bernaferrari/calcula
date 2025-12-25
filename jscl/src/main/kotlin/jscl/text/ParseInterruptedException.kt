package jscl.text

/**
 * User: serso
 * Date: 10/27/11
 * Time: 11:39 PM
 */
class ParseInterruptedException : RuntimeException {

    constructor() : super()

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super(cause)
}
