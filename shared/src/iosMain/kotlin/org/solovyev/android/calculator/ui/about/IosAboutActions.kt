package org.solovyev.android.calculator.ui.about

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosAboutActions : AboutActions {
    override fun openPlayStore() {
        openUrl("itms-apps://apps.apple.com/app/id443369350")
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
        openUrl("mailto:se.solovyev@gmail.com")
    }

    override fun openWebsite() {
        openUrl("http://se.solovyev.org")
    }

    private fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }
}
