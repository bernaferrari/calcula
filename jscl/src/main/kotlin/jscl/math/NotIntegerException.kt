package jscl.math

class NotIntegerException private constructor() : ArithmeticException("Not integer!") {
    companion object {
        @JvmStatic
        private val INSTANCE = NotIntegerException()

        @JvmStatic
        fun get(): NotIntegerException = INSTANCE
    }
}
