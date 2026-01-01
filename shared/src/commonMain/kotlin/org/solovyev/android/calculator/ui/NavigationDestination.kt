package org.solovyev.android.calculator.ui

sealed class NavigationDestination {
    data object Onboarding : NavigationDestination()
    data object Calculator : NavigationDestination()
    data object Settings : NavigationDestination()
    data object History : NavigationDestination()
    data object Variables : NavigationDestination()
    data object Functions : NavigationDestination()
    data object About : NavigationDestination()
    data object Converter : NavigationDestination()
}
