package org.solovyev.android.calculator.ui

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the Calculator app.
 * Using Kotlin Serialization for compile-time safety and future deep-linking support.
 */

@Serializable
data object OnboardingRoute

@Serializable
data object CalculatorRoute

@Serializable
data object SettingsRoute

@Serializable
data object HistoryRoute

@Serializable
data object VariablesRoute

@Serializable
data object FunctionsRoute

@Serializable
data object AboutRoute

@Serializable
data object ConverterRoute

@Serializable
data object OperatorsRoute

@Serializable
data object GraphRoute
