@file:OptIn(ExperimentalForeignApi::class)

package org.solovyev.android.calculator.di

import kotlinx.cinterop.ExperimentalForeignApi

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import org.solovyev.android.calculator.history.CalculatorDatabase
import org.solovyev.android.calculator.history.CalculatorDatabaseConstructor
import org.solovyev.android.calculator.preferences.createDataStore
import org.solovyev.android.calculator.ResourceProvider
import org.solovyev.android.calculator.IosResourceProvider
import org.solovyev.android.calculator.ui.about.AboutActions
import org.solovyev.android.calculator.ui.about.IosAboutActions
import org.solovyev.android.calculator.AppInfo
import org.solovyev.android.calculator.IosAppInfo
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val platformModule: Module = module {
    // DataStore
    single { createDataStore() }

    // Database
    single {
        val dbFile = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )?.path + "/calculator.db"

        Room.databaseBuilder<CalculatorDatabase>(
            name = dbFile,
            factory = { CalculatorDatabaseConstructor.initialize() }
        ).setDriver(BundledSQLiteDriver())
            .build()
    }

    single { get<CalculatorDatabase>().historyDao() }

    // UI Services
    single<ResourceProvider> { IosResourceProvider() }
    single<AboutActions> { IosAboutActions() }
    single<AppInfo> { IosAppInfo() }
}
