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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Clipboard @Inject constructor(application: Application) {

    private val clipboard: ClipboardManager =
        application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun getText(): String {
        val primaryClip = clipboard.primaryClip
        if (primaryClip != null && primaryClip.itemCount > 0) {
            val text = primaryClip.getItemAt(0).text
            return text?.toString() ?: ""
        }
        return ""
    }

    fun setText(text: CharSequence) {
        clipboard.setPrimaryClip(ClipData.newPlainText("", text))
    }

    fun setText(text: String) {
        setText(text as CharSequence)
    }
}
