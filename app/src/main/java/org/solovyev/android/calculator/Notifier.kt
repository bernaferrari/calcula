/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

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
        showMessage(message.getLocalizedMessage())
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
