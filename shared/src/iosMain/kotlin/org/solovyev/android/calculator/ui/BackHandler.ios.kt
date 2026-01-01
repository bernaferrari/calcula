package org.solovyev.android.calculator.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS typically, or handle gesture if needed.
    // iOS relies on UI buttons for navigation mostly.
}
