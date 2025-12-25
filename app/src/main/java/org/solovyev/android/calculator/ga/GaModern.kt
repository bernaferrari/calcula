package org.solovyev.android.calculator.ga

import android.app.Application
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.core.content.edit
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import org.solovyev.android.calculator.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modern Kotlin version of Ga using StateFlow and Coroutines for analytics tracking
 * This is an enhanced version with modern patterns - the original Ga.kt should be replaced with this
 */
@Singleton
class GaModern @Inject constructor(
    application: Application,
    private val preferences: SharedPreferences
) : SharedPreferences.OnSharedPreferenceChangeListener {

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
        preferences.registerOnSharedPreferenceChangeListener(this)

        // Initialize current values
        _layoutMode.value = Preferences.Gui.mode.getPreferenceNoError(preferences) ?: Preferences.Gui.Mode.simple
        _theme.value = Preferences.Gui.theme.getPreferenceNoError(preferences) ?: Preferences.Gui.Theme.material_theme

        // Collect and report layout changes
        scope.launch {
            layoutMode.drop(1).collect { mode ->
                reportLayout(mode)
            }
        }

        // Collect and report theme changes
        scope.launch {
            theme.drop(1).collect { theme ->
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

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String?) {
        when (key) {
            Preferences.Gui.mode.key -> {
                _layoutMode.value = Preferences.Gui.mode.getPreferenceNoError(preferences) ?: Preferences.Gui.Mode.simple
            }
            Preferences.Gui.theme.key -> {
                _theme.value = Preferences.Gui.theme.getPreferenceNoError(preferences) ?: Preferences.Gui.Theme.material_theme
            }
        }
    }

    fun reportInitially(preferences: SharedPreferences) {
        val mode = Preferences.Gui.mode.getPreferenceNoError(preferences) ?: Preferences.Gui.Mode.simple
        val theme = Preferences.Gui.theme.getPreferenceNoError(preferences) ?: Preferences.Gui.Theme.material_theme

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
        preferences.edit {
            putString(Preferences.Gui.mode.key, mode.name)
        }
    }

    /**
     * Update theme using modern Kotlin extensions
     */
    fun updateTheme(theme: Preferences.Gui.Theme) {
        preferences.edit {
            putString(Preferences.Gui.theme.key, theme.name)
        }
    }

    fun cleanup() {
        preferences.unregisterOnSharedPreferenceChangeListener(this)
        scope.cancel()
    }
}
