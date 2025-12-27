package jscl.math

class NotDoubleException private constructor() : ArithmeticException("Not double!") {
    companion object {
        @JvmStatic
        private val INSTANCE = NotDoubleException()

        @JvmStatic
        fun get(): NotDoubleException = INSTANCE
    }
}
