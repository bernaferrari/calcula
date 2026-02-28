package org.solovyev.android.calculator.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.solovyev.android.calculator.*
import jscl.JsclMathEngine
import org.solovyev.android.calculator.history.*
import org.solovyev.android.calculator.memory.*
import org.solovyev.android.calculator.preferences.*
import org.solovyev.android.calculator.ui.about.AboutViewModel
import org.solovyev.android.calculator.ui.history.HistoryViewModel
import org.solovyev.android.calculator.ui.settings.SettingsViewModel
import org.solovyev.android.calculator.ui.variables.VariablesViewModel
import org.solovyev.android.calculator.ui.functions.FunctionsViewModel
import org.solovyev.android.calculator.ui.onboarding.OnboardingViewModel
import org.solovyev.android.calculator.ui.graphing.GraphViewModel
import org.solovyev.android.calculator.formulas.FormulaViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule, platformModule)
    }

// Base common module - stripped to essentials
val commonModule = module {
    // Core Singletons
    single<Notifier> { Notifier() }
    single<AppPreferences> { DataStoreAppPreferences(get(), get()) }
    single { JsclMathEngine.getInstance() }
    single<ErrorReporter> { DefaultErrorReporter() }
    single<FunctionsRegistry> { DefaultFunctionsRegistry(get()) }
    single<OperatorsRegistry> { DefaultOperatorsRegistry(get()) }
    single<PostfixFunctionsRegistry> { DefaultPostfixFunctionsRegistry(get()) }
    single<VariablesRegistry> { DefaultVariablesRegistry(get()) }
    singleOf(::Engine)

    singleOf(::Editor)
    singleOf(::Display)
    singleOf(::Calculator)
    singleOf(::Keyboard)
    singleOf(::ToJsclTextProcessor)

    // History and Memory
    single<HistoryDao> { get<CalculatorDatabase>().historyDao() }
    singleOf(::RoomHistory)
    single<Memory> { DataStoreMemory(get()) }

    // ViewModels
    viewModelOf(::CalculatorViewModel)
    viewModelOf(::AboutViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::VariablesViewModel)
    viewModelOf(::FunctionsViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::GraphViewModel)
    viewModelOf(::FormulaViewModel)

}

expect val platformModule: Module
