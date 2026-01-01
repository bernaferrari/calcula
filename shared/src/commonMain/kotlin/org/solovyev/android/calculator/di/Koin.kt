package org.solovyev.android.calculator.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.solovyev.android.calculator.*
import org.solovyev.android.calculator.history.*
import org.solovyev.android.calculator.memory.*
import org.solovyev.android.calculator.preferences.*
import org.solovyev.android.calculator.ui.converter.ConverterViewModel
import org.solovyev.android.calculator.ui.about.AboutViewModel
import org.solovyev.android.calculator.ui.about.AboutActions
import org.solovyev.android.calculator.ui.history.HistoryViewModel
import org.solovyev.android.calculator.ui.settings.SettingsViewModel
import org.solovyev.android.calculator.ui.variables.VariablesViewModel
import org.solovyev.android.calculator.ui.functions.FunctionsViewModel
import org.solovyev.android.calculator.ui.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule, platformModule)
    }

// Base common module
val commonModule = module {
    // Shared Singletons
    single<Notifier> { Notifier() }
    single<AppPreferences> { DataStoreAppPreferences(get()) }
    
    singleOf(::Editor)
    singleOf(::Display)
    singleOf(::Calculator)
    singleOf(::Keyboard)
    
    // History and Memory
    singleOf(::RoomHistory)
    singleOf(::DataStoreMemory)

    // ViewModels
    viewModelOf(::CalculatorViewModel)
    viewModelOf(::ConverterViewModel)
    viewModelOf(::AboutViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::VariablesViewModel)
    viewModelOf(::FunctionsViewModel)
    viewModelOf(::OnboardingViewModel)
}

expect val platformModule: Module
