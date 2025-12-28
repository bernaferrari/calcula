package org.solovyev.android.calculator

import android.app.Application
import android.os.Handler
import android.widget.Toast
import androidx.annotation.StringRes
import org.solovyev.common.msg.Message
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Notifier @Inject constructor() {

    @Inject
    lateinit var application: Application

    @Inject
    lateinit var handler: Handler

    fun showMessage(message: Message) {
        showMessage(CalculatorMessages.getLocalizedMessage(message))
    }

    fun showMessage(@StringRes message: Int, vararg parameters: Any) {
        showMessage(application.getString(message, *parameters))
    }

    fun showMessage(@StringRes message: Int) {
        showMessage(application.getString(message))
    }

    fun showMessage(error: Throwable) {
        showMessage(Utils.getErrorMessage(error))
    }

    fun showMessage(message: String) {
        if (App.isUiThread()) {
            Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
            return
        }
        handler.post {
            Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
        }
    }
}
