package org.solovyev.android.calculator.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.solovyev.android.calculator.CalculatorViewModel
import org.solovyev.android.calculator.buttons.CppSpecialButton

@Composable
fun ModernModeBottomBar(
    viewModel: CalculatorViewModel,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenConverter: () -> Unit,
    modifier: Modifier = Modifier
) {
    CalculatorFloatingToolbar(
        onCursorLeft = { viewModel.moveCursorLeft() },
        onCursorRight = { viewModel.moveCursorRight() },
        onFunctions = { viewModel.onSpecialClick(CppSpecialButton.functions.action) },
        onConverter = onOpenConverter,
        onHistory = onOpenHistory,
        onSettings = onOpenSettings,
        modifier = modifier.fillMaxWidth(),
        layout = FloatingToolbarLayout.HORIZONTAL,
        colorScheme = FloatingToolbarColor.STANDARD
    )
}
