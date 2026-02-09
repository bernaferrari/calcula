package org.solovyev.android.calculator

class DefaultErrorReporter : ErrorReporter {
    override fun onException(e: Throwable) {
        println("Calculator error: ${e.message}")
        e.printStackTrace()
    }

    override fun onError(message: String) {
        println("Calculator error: $message")
    }
}
