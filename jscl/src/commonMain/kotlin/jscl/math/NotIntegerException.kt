package jscl.math

class NotIntegerException private constructor() : ArithmeticException("Not integer!") {
    companion object {
        private val INSTANCE = NotIntegerException()

        fun get(): NotIntegerException = INSTANCE
    }
}
