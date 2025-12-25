package org.solovyev.android.calculator

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Handler
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import jscl.JsclMathEngine
import org.solovyev.android.calculator.wizard.CalculatorWizards
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.Checkout
import org.solovyev.android.checkout.Inventory
import org.solovyev.android.checkout.RobotmediaDatabase
import org.solovyev.android.checkout.RobotmediaInventory
import org.solovyev.android.plotter.Plot
import org.solovyev.android.plotter.Plotter
import org.solovyev.android.wizard.Wizards
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Named
import javax.inject.Singleton

@Module
@DisableInstallInCheck
object AppModule {

    // single thread, should be used during the startup
    const val THREAD_INIT = "thread-init"

    // UI application thread
    const val THREAD_UI = "thread-ui"

    // multiple threads
    const val THREAD_BACKGROUND = "thread-background"

    const val DIR_FILES = "dir-files"
    const val PREFS_FLOATING = "prefs-floating"
    const val PREFS_TABS = "prefs-tabs"
    const val PREFS_UI = "prefs-ui"

    @Provides
    @Singleton
    @Named(PREFS_FLOATING)
    fun provideFloatingPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences("floating-calculator", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    @Named(PREFS_TABS)
    fun provideTabsPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences("tabs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    @Named(PREFS_UI)
    fun provideUiPreferences(application: Application): SharedPreferences {
        return provideUiPreferencesStatic(application)
    }

    @JvmStatic
    fun provideUiPreferencesStatic(application: Application): SharedPreferences {
        return application.getSharedPreferences("ui", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    @Named(THREAD_INIT)
    fun provideInitThread(): Executor {
        return Executors.newSingleThreadExecutor { r -> Thread(r, "Init") }
    }

    @Provides
    @Singleton
    @Named(THREAD_BACKGROUND)
    fun provideBackgroundThread(): Executor {
        return Executors.newFixedThreadPool(5, object : ThreadFactory {
            private val counter = AtomicInteger()

            override fun newThread(r: Runnable): Thread {
                return Thread(r, "Background #${counter.getAndIncrement()}")
            }
        })
    }

    @Provides
    @Singleton
    fun provideErrorReporter(): ErrorReporter {
        return object : ErrorReporter {
            override fun onException(e: Throwable) {
                // No-op implementation
            }

            override fun onError(message: String) {
                // No-op implementation
            }
        }
    }

    @Provides
    @Singleton
    fun provideWizards(application: Application): Wizards {
        return CalculatorWizards(application)
    }

    @Provides
    @Singleton
    @Named(THREAD_UI)
    fun provideUiThread(handler: Handler): Executor {
        return Executor { command ->
            if (App.isUiThread()) {
                command.run()
            } else {
                handler.post(command)
            }
        }
    }

    @Provides
    @Singleton
    fun provideJsclMathEngine(): JsclMathEngine {
        return JsclMathEngine.getInstance()
    }

    @Provides
    @Singleton
    fun provideBilling(application: Application): Billing {
        return Billing(application, object : Billing.DefaultConfiguration() {
            override fun getPublicKey(): String {
                return CalculatorSecurity.getPK()
            }

            override fun getFallbackInventory(
                checkout: Checkout,
                onLoadExecutor: Executor
            ): Inventory? {
                return if (RobotmediaDatabase.exists(application)) {
                    RobotmediaInventory(checkout, onLoadExecutor)
                } else {
                    null
                }
            }
        })
    }

    @Singleton
    @Provides
    fun provideTypeface(application: Application): Typeface {
        return Typeface.createFromAsset(application.assets, "fonts/Roboto-Regular.ttf")
    }

    @Singleton
    @Provides
    @Named(DIR_FILES)
    fun provideFilesDir(
        application: Application,
        @Named(THREAD_INIT) initThread: Executor
    ): File {
        val filesDir = makeFilesDir(application)
        initThread.execute {
            if (!filesDir.exists() && !filesDir.mkdirs()) {
                Log.e(App.TAG, "Can't create files dirs")
            }
        }
        return filesDir
    }

    @Provides
    @Singleton
    fun providePlotter(application: Application): Plotter {
        return Plot.newPlotter(application)
    }

    private fun makeFilesDir(application: Application): File {
        return application.filesDir ?: File(application.applicationInfo.dataDir, "files")
    }
}
