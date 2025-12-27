package jscl.math

open class NotPowerException() : ArithmeticException("Not power!") {
    constructor(s: String) : this()

    companion object {
        @JvmStatic
        private val INSTANCE = NotPowerException()

        @JvmStatic
        fun get(): NotPowerException = INSTANCE
    }
}
