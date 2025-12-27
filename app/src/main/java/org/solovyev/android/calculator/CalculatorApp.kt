package org.solovyev.android.calculator

import android.app.Application
import android.os.Handler
import android.util.Log
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import org.solovyev.android.calculator.billing.BillingManager
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
class CalculatorApp : Application() {

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

    @Inject
    lateinit var billingManager: BillingManager

    override fun onCreate() {
        super.onCreate()

        // Initialize App - needed for legacy code
        App.init(this, appPreferences)

        MobileAds.initialize(this)
        billingManager.start()

        // Change application's theme/language if needed
        val theme = appPreferences.settings.getThemeBlocking()
        setTheme(theme.theme)

        val language = languages.get(appPreferences.settings.getLanguageBlocking())
        if (!language.isSystem() && language.locale != Locale.getDefault()) {
            Locale.setDefault(language.locale)
        }

        onPostCreate()
    }

    private fun onPostCreate() {

        languages.init()
        // Ensure default locale is set correctly
        Languages.wrapContext(this, appPreferences)

        // Initialize components
        editor.init()
        display.init()
        history.init()

        // Initialize calculator on main for preference observers
        appScope.launchMain {
            calculator.initAsync()
        }

        // Warm up engine in background
        appScope.launchIO { warmUpEngine() }

        // Initialize GA
        appScope.launchIO {
            val gaInstance = ga.get()
            handler.post {
                gaInstance.reportInitially()
            }
        }

        appScope.launchMain {
            appPreferences.settings.onscreenShowAppIcon.collect { show ->
                App.enableComponent(this@CalculatorApp, FloatingCalculatorActivity::class.java, show)
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

    companion object {
        @JvmStatic
        fun get(application: Application): CalculatorApp = application as CalculatorApp
    }
}
