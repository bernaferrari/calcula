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
