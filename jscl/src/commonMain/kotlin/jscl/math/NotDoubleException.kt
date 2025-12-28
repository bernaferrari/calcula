package jscl.math

class NotDoubleException private constructor() : ArithmeticException("Not double!") {
    companion object {
        private val INSTANCE = NotDoubleException()

        fun get(): NotDoubleException = INSTANCE
    }
}
