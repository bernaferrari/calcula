package org.solovyev.android.calculator.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.solovyev.android.calculator.*
import org.solovyev.android.calculator.history.*
import org.solovyev.android.calculator.language.AndroidLanguages
import org.solovyev.android.calculator.language.Languages
import org.solovyev.android.calculator.preferences.createDataStore
import org.solovyev.android.calculator.ui.about.AboutActions
import org.solovyev.android.calculator.ui.about.AndroidAboutActions

actual val platformModule: Module = module {
    // DataStore
    single { createDataStore(androidContext()) }

    // Database
    single {
        val dbFile = androidContext().getDatabasePath("calculator.db")
        Room.databaseBuilder<CalculatorDatabase>(
            context = androidContext(),
            name = dbFile.absolutePath
        ).setDriver(BundledSQLiteDriver())
            .build()
    }

    single { get<CalculatorDatabase>().historyDao() }
    single<History> { RoomHistory(get()) }
    single<Languages> { AndroidLanguages(androidContext() as android.app.Application, get()) }

    // UI Services
    single<ResourceProvider> { AndroidResourceProvider(androidContext()) }
    single<AboutActions> { AndroidAboutActions(get()) }
    single<AppInfo> { AndroidAppInfo(get()) }
}
