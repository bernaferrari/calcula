package org.solovyev.android.calculator.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
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
import jscl.AngleUnit
import jscl.NumeralBase
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CalculatorTopBar(
    mode: Preferences.Gui.Mode,
    angleUnit: AngleUnit,
    numeralBase: NumeralBase,
    onModeChange: (Preferences.Gui.Mode) -> Unit,
    onAngleUnitChange: (AngleUnit) -> Unit,
    onNumeralBaseChange: (NumeralBase) -> Unit,
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
    mode: Preferences.Gui.Mode,
    angleUnit: AngleUnit,
    numeralBase: NumeralBase,
    onModeChange: (Preferences.Gui.Mode) -> Unit,
    onAngleUnitChange: (AngleUnit) -> Unit,
    onNumeralBaseChange: (NumeralBase) -> Unit,
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
                contentDescription = stringResource(
                    id = androidx.appcompat.R.string.abc_action_menu_overflow_description
                )
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            MenuSectionHeader(
                text = stringResource(id = R.string.cpp_mode) + ": " +
                    stringResource(id = mode.nameRes)
            )
            MenuOption(
                text = stringResource(id = R.string.cpp_mode_engineer),
                selected = mode == Preferences.Gui.Mode.engineer
            ) {
                menuExpanded = false
                onModeChange(Preferences.Gui.Mode.engineer)
            }
            MenuOption(
                text = stringResource(id = R.string.cpp_mode_simple),
                selected = mode == Preferences.Gui.Mode.simple
            ) {
                menuExpanded = false
                onModeChange(Preferences.Gui.Mode.simple)
            }

            HorizontalDivider()

            MenuSectionHeader(
                text = stringResource(id = R.string.cpp_angles) + ": " +
                    stringResource(id = angleUnitLabel(angleUnit))
            )
            MenuOption(
                text = stringResource(id = R.string.cpp_deg),
                selected = angleUnit == AngleUnit.deg
            ) {
                menuExpanded = false
                onAngleUnitChange(AngleUnit.deg)
            }
            MenuOption(
                text = stringResource(id = R.string.cpp_rad),
                selected = angleUnit == AngleUnit.rad
            ) {
                menuExpanded = false
                onAngleUnitChange(AngleUnit.rad)
            }

            HorizontalDivider()

            MenuSectionHeader(
                text = stringResource(id = R.string.cpp_radix) + ": " +
                    stringResource(id = numeralBaseLabel(numeralBase))
            )
            MenuOption(
                text = stringResource(id = R.string.cpp_bin),
                selected = numeralBase == NumeralBase.bin
            ) {
                menuExpanded = false
                onNumeralBaseChange(NumeralBase.bin)
            }
            MenuOption(
                text = stringResource(id = R.string.cpp_dec),
                selected = numeralBase == NumeralBase.dec
            ) {
                menuExpanded = false
                onNumeralBaseChange(NumeralBase.dec)
            }
            MenuOption(
                text = stringResource(id = R.string.cpp_hex),
                selected = numeralBase == NumeralBase.hex
            ) {
                menuExpanded = false
                onNumeralBaseChange(NumeralBase.hex)
            }

            HorizontalDivider()

            MenuAction(text = stringResource(id = R.string.cpp_settings)) {
                menuExpanded = false
                onOpenSettings()
            }
            MenuAction(text = stringResource(id = R.string.c_history)) {
                menuExpanded = false
                onOpenHistory()
            }
            MenuAction(text = stringResource(id = R.string.cpp_plotter)) {
                menuExpanded = false
                onOpenPlotter()
            }
            MenuAction(text = stringResource(id = R.string.c_conversion_tool)) {
                menuExpanded = false
                onOpenConverter()
            }
            MenuAction(text = stringResource(id = R.string.cpp_about)) {
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

private fun angleUnitLabel(angleUnit: AngleUnit): Int {
    val label = Engine.Preferences.angleUnitName(angleUnit)
    return if (label != 0) label else R.string.cpp_deg
}

private fun numeralBaseLabel(numeralBase: NumeralBase): Int {
    val label = Engine.Preferences.numeralBaseName(numeralBase)
    return if (label != 0) label else R.string.cpp_dec
}
