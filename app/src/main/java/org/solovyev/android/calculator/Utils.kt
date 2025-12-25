package org.solovyev.android.calculator

object Utils {
    fun getErrorMessage(error: Throwable): String {
        return error.localizedMessage ?: error.javaClass.simpleName
    }
}
