package org.solovyev.android.calculator.ui.about

import android.content.Context
import android.content.Intent
import android.net.Uri

class AndroidAboutActions(private val context: Context) : AboutActions {
    override fun openPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.solovyev.android.calculator"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }.onFailure {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.solovyev.android.calculator")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    override fun openSourceCode() {
        openUrl("https://github.com/serso/android-calculatorpp")
    }

    override fun openFacebook() {
        openUrl("https://www.facebook.com/calculatorpp")
    }

    override fun openDeveloperWebsite() {
        openUrl("http://se.solovyev.org")
    }

    override fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:se.solovyev@gmail.com"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun openWebsite() {
        openUrl("http://se.solovyev.org")
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
