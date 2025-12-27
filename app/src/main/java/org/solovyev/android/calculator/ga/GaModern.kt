package org.solovyev.android.calculator.ga

import android.app.Application
import android.text.TextUtils
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.di.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modern Kotlin version of Ga using StateFlow and Coroutines for analytics tracking
 * This is an enhanced version with modern patterns - the original Ga.kt should be replaced with this
 */
@Singleton
class GaModern @Inject constructor(
    application: Application,
    private val appPreferences: AppPreferences
) {

    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(application)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // StateFlow for layout mode
    private val _layoutMode = MutableStateFlow(Preferences.Gui.Mode.simple)
    val layoutMode: StateFlow<Preferences.Gui.Mode> = _layoutMode.asStateFlow()

    // StateFlow for theme
    private val _theme = MutableStateFlow(Preferences.Gui.Theme.material_theme)
    val theme: StateFlow<Preferences.Gui.Theme> = _theme.asStateFlow()

    // SharedFlow for button clicks (events that shouldn't be replayed)
    private val _buttonClicks = MutableSharedFlow<String>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val buttonClicks: SharedFlow<String> = _buttonClicks.asSharedFlow()

    init {
        // Initialize current values
        _layoutMode.value = appPreferences.settings.getModeBlocking()
        _theme.value = appPreferences.settings.getThemeBlocking()

        // Collect and report layout changes
        scope.launch {
            appPreferences.settings.mode.drop(1).collect { mode ->
                _layoutMode.value = mode
                reportLayout(mode)
            }
        }

        // Collect and report theme changes
        scope.launch {
            appPreferences.settings.theme.drop(1).collect { theme ->
                _theme.value = theme
                reportTheme(theme)
            }
        }

        // Collect and report button clicks
        scope.launch {
            buttonClicks.collect { text ->
                analytics.logEvent("click", bundleOf("text" to text))
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
        if (TextUtils.isEmpty(text)) return

        scope.launch {
            _buttonClicks.emit(text!!)
        }
    }

    fun reportInitially() {
        val mode = appPreferences.settings.getModeBlocking()
        val theme = appPreferences.settings.getThemeBlocking()
        _layoutMode.value = mode
        _theme.value = theme
        reportLayout(mode)
        reportTheme(theme)
    }

    fun onFloatingCalculatorOpened() {
        analytics.logEvent("floating_calculator_open", null)
    }

    /**
     * Update preferences using modern Kotlin extensions
     */
    fun updateMode(mode: Preferences.Gui.Mode) {
        scope.launch { appPreferences.settings.setMode(mode) }
    }

    /**
     * Update theme using modern Kotlin extensions
     */
    fun updateTheme(theme: Preferences.Gui.Theme) {
        scope.launch { appPreferences.settings.setTheme(theme) }
    }

    fun cleanup() {
        scope.cancel()
    }
}
