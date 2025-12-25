package org.solovyev.android.calculator

interface ErrorReporter {
    fun onException(e: Throwable)
    fun onError(message: String)
}
