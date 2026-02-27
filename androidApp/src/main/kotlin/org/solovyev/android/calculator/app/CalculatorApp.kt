package org.solovyev.android.calculator.app

import android.app.Application
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.solovyev.android.calculator.*
import org.solovyev.android.calculator.di.commonModule
import org.solovyev.android.calculator.di.platformModule
import org.solovyev.android.calculator.history.History
import org.solovyev.android.calculator.language.Languages
import java.util.Locale

class CalculatorApp : Application(), KoinComponent {

    val appPreferences: AppPreferences by inject()
    val editor: Editor by inject()
    val display: Display by inject()
    val calculator: Calculator by inject()
    val engine: Engine by inject()
    val history: History by inject()
    val languages: Languages by inject()
    // Notifier is needed by many components but we don't need to inject it here unless we use it
    
    val handler = Handler(Looper.getMainLooper())
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin
        startKoin {
            androidContext(this@CalculatorApp)
            modules(commonModule, platformModule)
        }

        // Initialize App - needed for legacy code if any still exists
        // App.init(this, appPreferences)

        // Initialize components
        editor.init()
        display.init()
        appScope.launch {
            engine.initAsync()
        }
        appScope.launch {
            appPreferences.settings.calculateOnFly.collect { enabled ->
                calculator.setCalculateOnFly(enabled)
            }
        }

        // Warm up engine in background
        Thread {
            warmUpEngine()
        }.start()
    }

    private fun warmUpEngine() {
        try {
            val mathEngine = engine.getMathEngine()
            mathEngine.evaluate("1+1")
            mathEngine.evaluate("1*1")
        } catch (e: Throwable) {
            android.util.Log.e("CalculatorApp", "Engine warmup failed", e)
        }
    }

    companion object {
        fun get(application: Application): CalculatorApp = application as CalculatorApp
    }
}
