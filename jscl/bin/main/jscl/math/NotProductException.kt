package jscl.math

open class NotProductException() : ArithmeticException("Not product!") {
    constructor(s: String) : this()

    companion object {
        @JvmStatic
        private val INSTANCE = NotProductException()

        @JvmStatic
        fun get(): NotProductException = INSTANCE
    }
}
