package org.solovyev.android.calculator

import android.app.Application
import android.content.SharedPreferences
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import com.squareup.otto.Bus
import dagger.Lazy
import org.solovyev.android.calculator.floating.FloatingCalculatorActivity
import org.solovyev.android.calculator.ga.Ga
import org.solovyev.android.calculator.history.History
import org.solovyev.android.calculator.language.Languages
import java.util.Locale
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

class CalculatorApplication : Application(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    @Named(AppModule.THREAD_INIT)
    lateinit var initThread: Executor

    @Inject
    @Named(AppModule.THREAD_UI)
    lateinit var uiThread: Executor

    @Inject
    lateinit var handler: Handler

    // Manual Dagger 2 component (Legacy)
    // We keep this public because many classes access it via getComponent()
    lateinit var component: AppComponent
        private set

    @Inject
    lateinit var editor: Editor

    @Inject
    lateinit var display: Display

    @Inject
    lateinit var bus: Bus

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
    lateinit var ga: Lazy<Ga>

    // TimingLogger is deprecated/hidden in recent Android versions, using simple logging or shim if needed.
    // For now we comment it out to avoid compilation errors if removed from SDK.
    // private val timer = TimingLogger("App", "onCreate")

    override fun onCreate() {
        // timer.reset()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val uiPrefs = AppModule.provideUiPreferences(this)

        val languages = Languages(this, prefs)
        // timer.addSplit("languages")

        onPreCreate(prefs, uiPrefs, languages)
        // timer.addSplit("onPreCreate")

        super.onCreate()
        // timer.addSplit("super.onCreate")

        initDagger(languages)
        // timer.addSplit("initDagger")

        onPostCreate(prefs, languages)
        // timer.addSplit("onPostCreate")
        // timer.dumpToLog()
    }

    private fun initDagger(languages: Languages) {
        component = DaggerAppComponent.builder()
            .legacyModule(LegacyModule(this))
            .build()
        component.inject(this)

        editor.init()
        history.init()
    }

    private fun onPostCreate(prefs: SharedPreferences, languages: Languages) {
        languages.init()

        prefs.registerOnSharedPreferenceChangeListener(this)
        languages.updateContextLocale(this, true)

        calculator.init(initThread)

        initThread.execute { warmUpEngine() }
        
        // Replace AsyncTask with Coroutine or Executor
        // Using existing initThread for GA initialization to match legacy behavior
        // new GaInitializer(prefs).executeOnExecutor(initThread);
        // Converting GaInitializer logic to Runnable:
        initThread.execute {
            val gaInstance = ga.get()
            // onPostExecute logic:
            // We need to run this on UI thread? Original AsyncTask.onPostExecute runs on UI thread.
            // But Ga logic might be thread safe. Let's check original usage.
            // Original: executeOnExecutor(initThread), so background -> UI.
            handler.post {
                gaInstance.reportInitially(prefs)
            }
        }
    }

    private fun warmUpEngine() {
        try {
            // warm-up engine
            val mathEngine = engine.getMathEngine()
            mathEngine.evaluate("1+1")
            mathEngine.evaluate("1*1")
        } catch (e: Throwable) {
            Log.e(App.TAG, e.message, e)
        }
    }

    private fun onPreCreate(prefs: SharedPreferences, uiPrefs: SharedPreferences, languages: Languages) {
        // initializing App before #onCreate as FloatingCalculatorService might be created in #onCreate
        App.init(this, prefs)

        // then we should set default preferences
        Preferences.init(this, prefs)
        UiPreferences.init(prefs, uiPrefs)

        // and change application's theme/language is needed
        val theme = Preferences.Gui.getTheme(prefs)
        setTheme(theme.theme)

        val language = languages.getCurrent()
        if (!language.isSystem() && language.locale != Locale.getDefault()) {
            Locale.setDefault(language.locale)
        }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        if (Preferences.Onscreen.showAppIcon.key == key) {
            val showAppIcon = Preferences.Onscreen.showAppIcon.getPreference(prefs) ?: false
            App.enableComponent(this, FloatingCalculatorActivity::class.java, showAppIcon)
        }
    }
}
