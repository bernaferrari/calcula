package org.solovyev.android.calculator.navigation

import kotlinx.serialization.Serializable

// Custom sealed interface for navigation keys (replacing Navigation 3's NavKey)
sealed interface NavKey

@Serializable
data object Calculator : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object HistoryKey : NavKey

@Serializable
data object About : NavKey

@Serializable
data class Wizard(val flowName: String, val startStep: String? = null) : NavKey
