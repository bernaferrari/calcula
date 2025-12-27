package org.solovyev.android.calculator.di

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jscl.JsclMathEngine
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.ErrorReporter
import org.solovyev.android.calculator.language.Languages
import org.solovyev.android.calculator.wizard.CalculatorWizards
import org.solovyev.android.plotter.Plot
import org.solovyev.android.plotter.Plotter
import org.solovyev.android.wizard.Wizards
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
    fun provideLanguages(
        @ApplicationContext context: Context,
        appPreferences: AppPreferences
    ): Languages = Languages(context as Application, appPreferences)

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
    fun provideTypeface(@ApplicationContext context: Context): Typeface =
        Typeface.createFromAsset(context.assets, "fonts/Roboto-Regular.ttf")

    @Provides
    @Singleton
    fun providePlotter(@ApplicationContext context: Context): Plotter =
        Plot.newPlotter(context as Application)

}
