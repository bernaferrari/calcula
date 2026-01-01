package org.solovyev.android.calculator.ui

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
