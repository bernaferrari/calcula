package org.solovyev.android.calculator.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.solovyev.android.calculator.ui.compose.components.CalculatorFloatingToolbar
import org.solovyev.android.calculator.ui.compose.components.FloatingToolbarColor
import org.solovyev.android.calculator.ui.compose.components.FloatingToolbarLayout

/**
 * Bottom bar composable for modern mode.
 * Shows a floating toolbar with advanced functions below the keyboard.
 */
@Composable
fun ModernModeBottomBar(
    viewModel: CalculatorComposeViewModel,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenConverter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        CalculatorFloatingToolbar(
            onCursorLeft = { viewModel.onCursorLeft() },
            onCursorRight = { viewModel.onCursorRight() },
            onFunctions = { viewModel.onOpenFunctions() },
            onConverter = onOpenConverter,
            onHistory = onOpenHistory,
            onSettings = onOpenSettings,
            layout = FloatingToolbarLayout.HORIZONTAL,
            colorScheme = FloatingToolbarColor.STANDARD,
            expanded = true
        )
    }
}
