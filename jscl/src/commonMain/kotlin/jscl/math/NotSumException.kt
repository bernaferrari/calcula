package jscl.math

class NotSumException private constructor() : ArithmeticException("Not sum!") {
    companion object {
        private val INSTANCE = NotSumException()

        fun get(): NotSumException = INSTANCE
    }
}
