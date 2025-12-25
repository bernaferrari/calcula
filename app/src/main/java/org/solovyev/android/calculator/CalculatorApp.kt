package org.solovyev.android.calculator

import android.app.Application
import android.content.SharedPreferences
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import org.solovyev.android.calculator.di.AppCoroutineScope
import org.solovyev.android.calculator.di.AppDispatchers
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.floating.FloatingCalculatorActivity
import org.solovyev.android.calculator.ga.Ga
import org.solovyev.android.calculator.history.History
import org.solovyev.android.calculator.language.Languages
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class CalculatorApp : Application(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var dispatchers: AppDispatchers

    @Inject
    lateinit var appScope: AppCoroutineScope

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var handler: Handler

    @Inject
    lateinit var editor: Editor

    @Inject
    lateinit var display: Display

    @Inject
    lateinit var calculator: Calculator

    @Inject
    lateinit var engine: Engine

    @Inject
    lateinit var keyboard: Keyboard

    @Inject
    lateinit var history: History

    @Inject
    lateinit var broadcaster: Broadcaster

    @Inject
    lateinit var errorReporter: ErrorReporter

    @Inject
    lateinit var launcher: ActivityLauncher

    @Inject
    lateinit var languages: Languages

    @Inject
    lateinit var ga: dagger.Lazy<Ga>

    // Legacy SharedPreferences - keep for backward compatibility during migration
    @Inject
    lateinit var legacyPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        // Initialize App - needed for legacy code
        App.init(this, prefs)

        // Set default preferences
        Preferences.init(this, prefs)

        // Initialize legacy UI preferences
        val uiPrefs = getSharedPreferences("ui", MODE_PRIVATE)
        UiPreferences.init(prefs, uiPrefs)

        // Change application's theme/language if needed
        val theme = Preferences.Gui.getTheme(prefs)
        setTheme(theme.theme)

        val language = languages.getCurrent()
        if (!language.isSystem() && language.locale != Locale.getDefault()) {
            Locale.setDefault(language.locale)
        }

        onPostCreate(prefs)
    }

    private fun onPostCreate(prefs: SharedPreferences) {
        languages.init()
        prefs.registerOnSharedPreferenceChangeListener(this)
        languages.updateContextLocale(this, true)

        // Initialize components
        editor.init()
        history.init()

        // Initialize calculator with coroutine scope
        appScope.launchIO {
            calculator.initAsync()
        }

        // Warm up engine in background
        appScope.launchIO { warmUpEngine() }

        // Initialize GA
        appScope.launchIO {
            val gaInstance = ga.get()
            handler.post {
                gaInstance.reportInitially(prefs)
            }
        }
    }

    private fun warmUpEngine() {
        try {
            val mathEngine = engine.getMathEngine()
            mathEngine.evaluate("1+1")
            mathEngine.evaluate("1*1")
        } catch (e: Throwable) {
            Log.e(App.TAG, e.message, e)
        }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        if (key == Preferences.Onscreen.showAppIcon.key) {
            val showAppIcon = Preferences.Onscreen.showAppIcon.getPreference(prefs) ?: false
            App.enableComponent(this, FloatingCalculatorActivity::class.java, showAppIcon)
        }
    }

    companion object {
        @JvmStatic
        fun get(application: Application): CalculatorApp = application as CalculatorApp
    }
}
