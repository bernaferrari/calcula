package org.solovyev.android.calculator

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import org.solovyev.android.calculator.language.Languages
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Module
@DisableInstallInCheck
class LegacyModule(private val application: Application) {

    @Provides
    @Singleton
    fun provideApplication(): Application {
        return application
    }
}
