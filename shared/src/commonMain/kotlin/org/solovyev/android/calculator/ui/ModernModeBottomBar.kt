package org.solovyev.android.calculator.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ModernModeBottomBar(
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenGraph: () -> Unit,
    onOpenFunctions: () -> Unit,
    modifier: Modifier = Modifier
) {
    CalculatorFloatingToolbar(
        onFunctions = onOpenFunctions,
        onConverter = onOpenConverter,
        onGraph = onOpenGraph,
        onHistory = onOpenHistory,
        onSettings = onOpenSettings,
        modifier = modifier.fillMaxWidth(),
        layout = FloatingToolbarLayout.HORIZONTAL,
        colorScheme = FloatingToolbarColor.STANDARD
    )
}
