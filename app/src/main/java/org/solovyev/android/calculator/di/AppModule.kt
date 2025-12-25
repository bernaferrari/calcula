package org.solovyev.android.calculator.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import com.squareup.otto.Bus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jscl.JsclMathEngine
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.AppBus
import org.solovyev.android.calculator.CalculatorSecurity
import org.solovyev.android.calculator.ErrorReporter
import org.solovyev.android.calculator.language.Languages
import org.solovyev.android.calculator.wizard.CalculatorWizards
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.Checkout
import org.solovyev.android.checkout.Inventory
import org.solovyev.android.checkout.RobotmediaDatabase
import org.solovyev.android.checkout.RobotmediaInventory
import org.solovyev.android.plotter.Plot
import org.solovyev.android.plotter.Plotter
import org.solovyev.android.wizard.Wizards
import java.util.concurrent.Executor
import javax.inject.Singleton

/**
 * Main Hilt module providing application-wide dependencies.
 *
 * Modern DI approach (2025):
 * - AppDispatchers for coroutine dispatchers (replaces @Named Executors)
 * - AppDirectories for file directories (replaces @Named File)
 * - AppPreferences for DataStore (replaces @Named SharedPreferences)
 * - AppCoroutineScope for application-scoped coroutines
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHandler(): Handler = Handler(Looper.getMainLooper())

    @Provides
    @Singleton
    fun provideBus(handler: Handler): Bus = AppBus(handler)

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun provideLanguages(
        @ApplicationContext context: Context,
        preferences: SharedPreferences
    ): Languages = Languages(context as Application, preferences)

    @Provides
    @Singleton
    fun provideErrorReporter(): ErrorReporter = object : ErrorReporter {
        override fun onException(e: Throwable) {
            android.util.Log.e(App.TAG, "Error", e)
        }

        override fun onError(message: String) {
            android.util.Log.e(App.TAG, message)
        }
    }

    @Provides
    @Singleton
    fun provideWizards(@ApplicationContext context: Context): Wizards =
        CalculatorWizards(context as Application)

    @Provides
    @Singleton
    fun provideJsclMathEngine(): JsclMathEngine = JsclMathEngine.getInstance()

    @Provides
    @Singleton
    fun provideBilling(@ApplicationContext context: Context): Billing {
        val application = context as Application
        return Billing(application, object : Billing.DefaultConfiguration() {
            override fun getPublicKey(): String = CalculatorSecurity.getPK()

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

    @Provides
    @Singleton
    fun provideTypeface(@ApplicationContext context: Context): Typeface =
        Typeface.createFromAsset(context.assets, "fonts/Roboto-Regular.ttf")

    @Provides
    @Singleton
    fun providePlotter(@ApplicationContext context: Context): Plotter =
        Plot.newPlotter(context as Application)

    // Legacy SharedPreferences for gradual migration
    // These will be removed once all code migrates to DataStore

    @Provides
    @Singleton
    @PrefsUi
    fun provideUiPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("ui", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    @PrefsFloating
    fun provideFloatingPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("floating-calculator", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    @PrefsTabs
    fun provideTabsPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("tabs", Context.MODE_PRIVATE)
}

// Legacy qualifiers - keep for backward compatibility during migration
@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrefsFloating

@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrefsTabs

@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrefsUi
