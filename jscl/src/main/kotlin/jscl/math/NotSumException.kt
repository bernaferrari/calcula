package jscl.math

class NotSumException private constructor() : ArithmeticException("Not sum!") {
    companion object {
        @JvmStatic
        private val INSTANCE = NotSumException()

        @JvmStatic
        fun get(): NotSumException = INSTANCE
    }
}
