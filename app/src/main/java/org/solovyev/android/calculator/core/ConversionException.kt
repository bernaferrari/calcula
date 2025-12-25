package org.solovyev.android.calculator.core

/**
 * Exception thrown when a number conversion fails
 * (e.g., when converting between different numeral bases)
 */
class ConversionException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    constructor() : this(null, null)
    constructor(message: String) : this(message, null)
    constructor(cause: Throwable) : this(null, cause)
}
