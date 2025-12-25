package org.solovyev.android.calculator.ga

import android.app.Application
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import org.solovyev.android.calculator.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Ga @Inject constructor(
    application: Application,
    preferences: SharedPreferences
) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(application)

    init {
        preferences.registerOnSharedPreferenceChangeListener(this)
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

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String?) {
        when (key) {
            Preferences.Gui.mode.key -> {
                reportLayout(Preferences.Gui.mode.getPreferenceNoError(preferences) ?: Preferences.Gui.Mode.simple)
            }
            Preferences.Gui.theme.key -> {
                reportTheme(Preferences.Gui.theme.getPreferenceNoError(preferences) ?: Preferences.Gui.Theme.material_theme)
            }
        }
    }

    fun reportInitially(preferences: SharedPreferences) {
        reportLayout(Preferences.Gui.mode.getPreferenceNoError(preferences) ?: Preferences.Gui.Mode.simple)
        reportTheme(Preferences.Gui.theme.getPreferenceNoError(preferences) ?: Preferences.Gui.Theme.material_theme)
    }

    fun onFloatingCalculatorOpened() {
        analytics.logEvent("floating_calculator_open", null)
    }
}
