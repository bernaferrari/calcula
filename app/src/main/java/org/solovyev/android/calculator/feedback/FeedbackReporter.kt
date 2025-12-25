package org.solovyev.android.calculator.feedback

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import org.solovyev.android.calculator.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackReporter @Inject constructor(
    private val context: Application
) {

    fun report() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Intent.EXTRA_EMAIL, arrayOf("se.solovyev@gmail.com"))
            val version = getVersion()
            putExtra(
                Intent.EXTRA_SUBJECT,
                "${context.getString(R.string.cpp_app_name)} $version // ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE}) ${Build.VERSION.SDK_INT}"
            )
            type = "plain/html"
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("FeedbackReporter", e.message, e)
        }
    }

    private fun getVersion(): String {
        return try {
            context.packageManager.getPackageInfo(context.applicationInfo.packageName, 0).versionName ?: "x.x.x"
        } catch (e: PackageManager.NameNotFoundException) {
            "x.x.x"
        }
    }
}
