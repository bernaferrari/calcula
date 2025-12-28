package jscl.math

open class NotProductException() : ArithmeticException("Not product!") {
    constructor(s: String) : this()

    companion object {
        private val INSTANCE = NotProductException()

        fun get(): NotProductException = INSTANCE
    }
}
