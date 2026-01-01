package org.solovyev.android.calculator.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.ui.*
import org.solovyev.android.calculator.ui.settings.CalculatorMode
import org.solovyev.android.calculator.ui.settings.AngleUnit
import org.solovyev.android.calculator.ui.settings.NumeralBase

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CalculatorTopBar(
    mode: CalculatorMode,
    angleUnit: AngleUnit,
    numeralBase: NumeralBase,
    onModeChange: (CalculatorMode) -> Unit,
    onAngleUnitChange: (AngleUnit) -> Unit,
    onNumeralBaseChange: (NumeralBase) -> Unit,
    onOpenVariables: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenPlotter: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {},
        actions = {
            CalculatorOverflowMenu(
                mode = mode,
                angleUnit = angleUnit,
                numeralBase = numeralBase,
                onModeChange = onModeChange,
                onAngleUnitChange = onAngleUnitChange,
                onNumeralBaseChange = onNumeralBaseChange,
                onOpenVariables = onOpenVariables,
                onOpenFunctions = onOpenFunctions,
                onOpenSettings = onOpenSettings,
                onOpenHistory = onOpenHistory,
                onOpenPlotter = onOpenPlotter,
                onOpenConverter = onOpenConverter,
                onOpenAbout = onOpenAbout
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
    )
}

@Composable
fun CalculatorOverflowMenu(
    mode: CalculatorMode,
    angleUnit: AngleUnit,
    numeralBase: NumeralBase,
    onModeChange: (CalculatorMode) -> Unit,
    onAngleUnitChange: (AngleUnit) -> Unit,
    onNumeralBaseChange: (NumeralBase) -> Unit,
    onOpenVariables: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenPlotter: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu"
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            MenuAction(text = stringResource(Res.string.cpp_variables)) {
                menuExpanded = false
                onOpenVariables()
            }
            MenuAction(text = stringResource(Res.string.c_functions)) {
                menuExpanded = false
                onOpenFunctions()
            }
            MenuAction(text = stringResource(Res.string.cpp_settings)) {
                menuExpanded = false
                onOpenSettings()
            }
            MenuAction(text = stringResource(Res.string.c_history)) {
                menuExpanded = false
                onOpenHistory()
            }
            MenuAction(text = stringResource(Res.string.cpp_plotter)) {
                menuExpanded = false
                onOpenPlotter()
            }
            MenuAction(text = stringResource(Res.string.c_conversion_tool)) {
                menuExpanded = false
                onOpenConverter()
            }
            MenuAction(text = stringResource(Res.string.cpp_about)) {
                menuExpanded = false
                onOpenAbout()
            }
        }
    }
}

@Composable
private fun MenuSectionHeader(text: String) {
    DropdownMenuItem(
        text = { Text(text = text, style = MaterialTheme.typography.labelLarge) },
        onClick = {},
        enabled = false
    )
}

@Composable
private fun MenuOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = text) },
        onClick = onClick,
        trailingIcon = if (selected) {
            { Icon(imageVector = Icons.Default.Check, contentDescription = null) }
        } else {
            null
        }
    )
}

@Composable
private fun MenuAction(text: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text = text) },
        onClick = onClick
    )
}
