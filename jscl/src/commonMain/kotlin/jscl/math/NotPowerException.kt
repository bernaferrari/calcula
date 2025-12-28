package jscl.math

open class NotPowerException() : ArithmeticException("Not power!") {
    constructor(s: String) : this()

    companion object {
        private val INSTANCE = NotPowerException()

        fun get(): NotPowerException = INSTANCE
    }
}
