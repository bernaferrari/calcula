package org.solovyev.android.calculator

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import com.squareup.otto.Bus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.solovyev.android.calculator.language.Languages
import javax.inject.Singleton
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideHandler(): Handler {
        return Handler(Looper.getMainLooper())
    }

    @Provides
    @Singleton
    @ApplicationContext
    fun provideApplicationContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideBus(handler: Handler): Bus {
        return AppBus(handler)
    }

    @Provides
    @Singleton
    fun providePreferences(application: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }

    @Provides
    @Singleton
    fun provideLanguages(application: Application, preferences: SharedPreferences): Languages {
        // Languages needs to be initialized, or we just provide the instance.
        // Legacy code passed it in constructor. Here we create it.
        return Languages(application, preferences)
    }
}
