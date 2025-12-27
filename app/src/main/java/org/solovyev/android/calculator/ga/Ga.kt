package org.solovyev.android.calculator.ga

import android.app.Application
import android.text.TextUtils
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.di.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Ga @Inject constructor(
    application: Application,
    private val appPreferences: AppPreferences
) {

    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(application)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        scope.launch {
            appPreferences.settings.mode.collect { mode ->
                reportLayout(mode)
            }
        }
        scope.launch {
            appPreferences.settings.theme.collect { theme ->
                reportTheme(theme)
            }
        }
    }

    private fun reportLayout(mode: Preferences.Gui.Mode) {
        analytics.logEvent("layout", bundleOf("name" to mode.name))
    }

    private fun reportTheme(theme: Preferences.Gui.Theme) {
        analytics.logEvent("theme", bundleOf("name" to theme.name))
    }

    fun onButtonPressed(text: String?) {
        if (TextUtils.isEmpty(text)) {
            return
        }
        analytics.logEvent("click", bundleOf("text" to text))
    }

    fun reportInitially() {
        reportLayout(appPreferences.settings.getModeBlocking())
        reportTheme(appPreferences.settings.getThemeBlocking())
    }

    fun onFloatingCalculatorOpened() {
        analytics.logEvent("floating_calculator_open", null)
    }
}
