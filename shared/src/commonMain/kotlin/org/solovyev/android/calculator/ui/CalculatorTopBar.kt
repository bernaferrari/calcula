package org.solovyev.android.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
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
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.cpp_app_name),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
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
    val entries = listOf(
        CalculatorMenuEntry.Action(
            label = stringResource(Res.string.cpp_variables),
            icon = Icons.Filled.TextFields,
            onClick = {
                menuExpanded = false
                onOpenVariables()
            }
        ),
        CalculatorMenuEntry.Action(
            label = stringResource(Res.string.c_functions),
            icon = Icons.Filled.Code,
            onClick = {
                menuExpanded = false
                onOpenFunctions()
            }
        ),
        CalculatorMenuEntry.Divider,
        CalculatorMenuEntry.Action(
            label = stringResource(Res.string.c_history),
            icon = Icons.Filled.History,
            onClick = {
                menuExpanded = false
                onOpenHistory()
            }
        ),
        CalculatorMenuEntry.Action(
            label = stringResource(Res.string.cpp_plotter),
            icon = Icons.Filled.Speed,
            onClick = {
                menuExpanded = false
                onOpenPlotter()
            }
        ),
        CalculatorMenuEntry.Action(
            label = stringResource(Res.string.c_conversion_tool),
            icon = Icons.Filled.Tune,
            onClick = {
                menuExpanded = false
                onOpenConverter()
            }
        ),
        CalculatorMenuEntry.Divider,
        CalculatorMenuEntry.Action(
            label = stringResource(Res.string.cpp_settings),
            icon = Icons.Filled.Settings,
            onClick = {
                menuExpanded = false
                onOpenSettings()
            }
        ),
        CalculatorMenuEntry.Action(
            label = stringResource(Res.string.cpp_about),
            icon = Icons.Filled.Info,
            onClick = {
                menuExpanded = false
                onOpenAbout()
            }
        )
    )

    Box(modifier = modifier) {
        CalculatorOverflowIconButton(onClick = { menuExpanded = true })
        CalculatorOverflowDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            entries = entries
        )
    }
}
