package org.solovyev.android.calculator.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jscl.AngleUnit
import jscl.NumeralBase
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.language.Language

enum class SettingsDestination {
    MAIN,
    NUMBER_FORMAT,
    APPEARANCE,
    ONSCREEN,
    WIDGET,
    OTHER
}

@Composable
fun SettingsScreen(
    destination: SettingsDestination,
    adFreePurchased: Boolean,
    onNavigate: (SettingsDestination) -> Unit,
    onStartWizard: () -> Unit,
    onReportBug: () -> Unit,
    onOpenAbout: () -> Unit,
    onSupportProject: () -> Unit,
    viewModel: SettingsComposeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (destination) {
        SettingsDestination.MAIN -> MainSettingsScreen(
            state = state,
            adFreePurchased = adFreePurchased,
            onModeChange = viewModel::setMode,
            onAngleUnitChange = viewModel::setAngleUnit,
            onNumeralBaseChange = viewModel::setNumeralBase,
            onNavigate = onNavigate,
            onStartWizard = onStartWizard,
            onReportBug = onReportBug,
            onOpenAbout = onOpenAbout,
            onSupportProject = onSupportProject
        )
        SettingsDestination.NUMBER_FORMAT -> NumberFormatScreen(
            state = state,
            onNotationChange = viewModel::setOutputNotation,
            onPrecisionChange = viewModel::setOutputPrecision,
            onSeparatorChange = viewModel::setOutputSeparator
        )
        SettingsDestination.APPEARANCE -> AppearanceScreen(
            state = state,
            languages = viewModel.availableLanguages,
            onLanguageChange = viewModel::setLanguage,
            onThemeChange = viewModel::setTheme,
            onVibrateChange = viewModel::setVibrateOnKeypress,
            onHighContrastChange = viewModel::setHighContrast,
            onHighlightExpressionsChange = viewModel::setHighlightExpressions,
            onRotateChange = viewModel::setRotateScreen,
            onKeepScreenOnChange = viewModel::setKeepScreenOn
        )
        SettingsDestination.ONSCREEN -> OnscreenScreen(
            state = state,
            onShowAppIconChange = viewModel::setOnscreenShowAppIcon,
            onThemeChange = viewModel::setOnscreenTheme
        )
        SettingsDestination.WIDGET -> WidgetScreen(
            state = state,
            onThemeChange = viewModel::setWidgetTheme
        )
        SettingsDestination.OTHER -> OtherScreen(
            state = state,
            onCalculateOnFlyChange = viewModel::setCalculateOnFly,
            onShowReleaseNotesChange = viewModel::setShowReleaseNotes,
            onUseBackAsPreviousChange = viewModel::setUseBackAsPrevious,
            onPlotImagChange = viewModel::setPlotImag
        )
    }
}

// ============================================================================
// MAIN SETTINGS SCREEN
// ============================================================================

@Composable
private fun MainSettingsScreen(
    state: SettingsUiState,
    adFreePurchased: Boolean,
    onModeChange: (Preferences.Gui.Mode) -> Unit,
    onAngleUnitChange: (AngleUnit) -> Unit,
    onNumeralBaseChange: (NumeralBase) -> Unit,
    onNavigate: (SettingsDestination) -> Unit,
    onStartWizard: () -> Unit,
    onReportBug: () -> Unit,
    onOpenAbout: () -> Unit,
    onSupportProject: () -> Unit
) {
    val context = LocalContext.current
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Support Project Card
        if (!adFreePurchased) {
            item {
                SupportProjectCard(onSupportProject = onSupportProject)
            }
        }

        // Calculator Section
        item {
            val modeTitleText = stringResource(R.string.cpp_mode)
            val modeOptions = listOf(
                stringResource(R.string.cpp_mode_modern),
                stringResource(R.string.cpp_mode_engineer),
                stringResource(R.string.cpp_mode_simple)
            )
            val angleTitleText = stringResource(R.string.cpp_angles)
            val angleOptions = AngleUnit.entries
            val angleOptionLabels = angleOptions.map {
                stringResource(Engine.Preferences.angleUnitName(it))
            }
            val radixTitleText = stringResource(R.string.cpp_radix)
            val radixOptions = NumeralBase.entries
            val radixOptionLabels = radixOptions.map {
                stringResource(Engine.Preferences.numeralBaseName(it))
            }

            PreferenceGroup(title = stringResource(R.string.cpp_prefs_basic)) {
                PreferenceItem(
                    icon = Icons.Rounded.Calculate,
                    title = modeTitleText,
                    summary = modeTitle(state.mode),
                    onClick = {
                        val selected = when (state.mode) {
                            Preferences.Gui.Mode.modern -> 0
                            Preferences.Gui.Mode.engineer -> 1
                            Preferences.Gui.Mode.simple -> 2
                        }
                        listDialog = ListDialogState(
                            title = modeTitleText,
                            options = modeOptions,
                            selectedIndex = selected,
                            onSelected = { index ->
                                val newMode = when (index) {
                                    0 -> Preferences.Gui.Mode.modern
                                    1 -> Preferences.Gui.Mode.engineer
                                    else -> Preferences.Gui.Mode.simple
                                }
                                onModeChange(newMode)
                            }
                        )
                    }
                )
                PreferenceItem(
                    icon = Icons.Rounded.PinDrop,
                    title = angleTitleText,
                    summary = stringResource(Engine.Preferences.angleUnitName(state.angleUnit)),
                    onClick = {
                        listDialog = ListDialogState(
                            title = angleTitleText,
                            options = angleOptionLabels,
                            selectedIndex = angleOptions.indexOf(state.angleUnit),
                            onSelected = { index -> onAngleUnitChange(angleOptions[index]) }
                        )
                    }
                )
                PreferenceItem(
                    icon = Icons.Rounded.Numbers,
                    title = radixTitleText,
                    summary = stringResource(Engine.Preferences.numeralBaseName(state.numeralBase)),
                    onClick = {
                        listDialog = ListDialogState(
                            title = radixTitleText,
                            options = radixOptionLabels,
                            selectedIndex = radixOptions.indexOf(state.numeralBase),
                            onSelected = { index -> onNumeralBaseChange(radixOptions[index]) }
                        )
                    }
                )
                PreferenceItem(
                    icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                    title = stringResource(R.string.cpp_number_format),
                    summary = stringResource(R.string.cpp_examples),
                    showArrow = true,
                    onClick = { onNavigate(SettingsDestination.NUMBER_FORMAT) }
                )
            }
        }

        // Appearance & Advanced Section
        item {
            PreferenceGroup(title = stringResource(R.string.cpp_prefs_advanced)) {
                PreferenceItem(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.cpp_appearance),
                    summary = stringResource(R.string.cpp_theme),
                    showArrow = true,
                    onClick = { onNavigate(SettingsDestination.APPEARANCE) }
                )
                PreferenceItem(
                    icon = Icons.Rounded.PhoneAndroid,
                    title = stringResource(R.string.cpp_floating_calculator),
                    summary = stringResource(R.string.cpp_theme),
                    showArrow = true,
                    onClick = { onNavigate(SettingsDestination.ONSCREEN) }
                )
                PreferenceItem(
                    icon = Icons.Rounded.Widgets,
                    title = stringResource(R.string.cpp_widget),
                    summary = stringResource(R.string.cpp_theme),
                    showArrow = true,
                    onClick = { onNavigate(SettingsDestination.WIDGET) }
                )
                PreferenceItem(
                    icon = Icons.Rounded.Settings,
                    title = stringResource(R.string.cpp_other),
                    showArrow = true,
                    onClick = { onNavigate(SettingsDestination.OTHER) }
                )
            }
        }

        // Help Section
        item {
            PreferenceGroup(title = stringResource(R.string.cpp_help)) {
                PreferenceItem(
                    icon = Icons.Rounded.FlashOn,
                    title = stringResource(R.string.cpp_introduction),
                    showArrow = true,
                    onClick = onStartWizard
                )
                PreferenceItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.cpp_report_problem),
                    showArrow = true,
                    onClick = onReportBug
                )
                PreferenceItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.cpp_about),
                    showArrow = true,
                    onClick = onOpenAbout
                )
            }
        }
    }

    listDialog?.let { dialog ->
        SelectionDialog(
            title = dialog.title,
            options = dialog.options,
            selectedIndex = dialog.selectedIndex,
            onDismiss = { listDialog = null },
            onSelected = {
                dialog.onSelected(it)
                listDialog = null
            }
        )
    }
}

// ============================================================================
// NUMBER FORMAT SCREEN
// ============================================================================

@Composable
private fun NumberFormatScreen(
    state: SettingsUiState,
    onNotationChange: (Engine.Notation) -> Unit,
    onPrecisionChange: (Int) -> Unit,
    onSeparatorChange: (Char) -> Unit
) {
    val context = LocalContext.current
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }
    var precisionDialog by remember { mutableStateOf(false) }
    val separatorNames = stringArrayResource(R.array.cpp_thousands_separator_names).toList()
    val separatorValues = stringArrayResource(R.array.cpp_thousands_separators).toList()
    val formatTitleText = stringResource(R.string.cpp_format)
    val separatorTitleText = stringResource(R.string.cpp_thousands_separator)
    val notationOptions = Engine.Notation.entries
    val notationOptionLabels = notationOptions.map { it.getName(context).toString() }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(R.string.cpp_number_format)) {
                PreferenceItem(
                    icon = Icons.Rounded.Tune,
                    title = formatTitleText,
                    summary = state.outputNotation.getName(LocalContext.current).toString(),
                    onClick = {
                        listDialog = ListDialogState(
                            title = formatTitleText,
                            options = notationOptionLabels,
                            selectedIndex = notationOptions.indexOf(state.outputNotation),
                            onSelected = { index -> onNotationChange(notationOptions[index]) }
                        )
                    }
                )
                PreferenceItem(
                    icon = Icons.Rounded.Numbers,
                    title = stringResource(R.string.cpp_precision),
                    summary = state.outputPrecision.toString(),
                    onClick = { precisionDialog = true }
                )
                PreferenceItem(
                    icon = Icons.Rounded.TextFields,
                    title = separatorTitleText,
                    summary = separatorSummary(state.outputSeparator),
                    onClick = {
                        val selectedIndex = separatorValues.indexOf(
                            when (state.outputSeparator) {
                                '\u0000' -> ""
                                else -> state.outputSeparator.toString()
                            }
                        ).coerceAtLeast(0)
                        listDialog = ListDialogState(
                            title = separatorTitleText,
                            options = separatorNames,
                            selectedIndex = selectedIndex,
                            onSelected = { index ->
                                val raw = separatorValues[index]
                                onSeparatorChange(if (raw.isEmpty()) '\u0000' else raw.first())
                            }
                        )
                    }
                )
            }
        }

        // Examples Card
        item {
            PreferenceGroup(title = stringResource(R.string.cpp_examples)) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = state.numberFormatExamples,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }

    listDialog?.let { dialog ->
        SelectionDialog(
            title = dialog.title,
            options = dialog.options,
            selectedIndex = dialog.selectedIndex,
            onDismiss = { listDialog = null },
            onSelected = {
                dialog.onSelected(it)
                listDialog = null
            }
        )
    }

    if (precisionDialog) {
        PrecisionDialog(
            value = state.outputPrecision,
            onDismiss = { precisionDialog = false },
            onConfirm = {
                onPrecisionChange(it)
                precisionDialog = false
            }
        )
    }
}

// ============================================================================
// APPEARANCE SCREEN
// ============================================================================

@Composable
private fun AppearanceScreen(
    state: SettingsUiState,
    languages: List<Language>,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (Preferences.Gui.Theme) -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onHighlightExpressionsChange: (Boolean) -> Unit,
    onRotateChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }
    val languageTitleText = stringResource(R.string.cpp_language)
    val languageOptions = languages.map { it.getName(context) }
    val themeTitleText = stringResource(R.string.cpp_theme)
    val themeOptions = listOf(
        Preferences.Gui.Theme.material_you_theme,
        Preferences.Gui.Theme.material_theme,
        Preferences.Gui.Theme.material_black_theme,
        Preferences.Gui.Theme.material_light_theme,
        Preferences.Gui.Theme.metro_blue_theme,
        Preferences.Gui.Theme.metro_green_theme,
        Preferences.Gui.Theme.metro_purple_theme
    )
    val themeOptionLabels = themeOptions.map { it.getName(context) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(R.string.cpp_appearance)) {
                PreferenceItem(
                    icon = Icons.Rounded.Language,
                    title = languageTitleText,
                    summary = languages.firstOrNull { it.code == state.languageCode }
                        ?.getName(context)
                        ?: "",
                    onClick = {
                        listDialog = ListDialogState(
                            title = languageTitleText,
                            options = languageOptions,
                            selectedIndex = languages.indexOfFirst { it.code == state.languageCode }.coerceAtLeast(0),
                            onSelected = { index -> onLanguageChange(languages[index].code) }
                        )
                    }
                )
                PreferenceItem(
                    icon = Icons.Rounded.LightMode,
                    title = themeTitleText,
                    summary = state.theme.getName(context),
                    onClick = {
                        listDialog = ListDialogState(
                            title = themeTitleText,
                            options = themeOptionLabels,
                            selectedIndex = themeOptions.indexOf(state.theme).coerceAtLeast(0),
                            onSelected = { index -> onThemeChange(themeOptions[index]) }
                        )
                    }
                )
            }
        }

        item {
            PreferenceGroup(title = stringResource(R.string.cpp_prefs_advanced)) {
                SwitchPreference(
                    icon = Icons.Rounded.Vibration,
                    title = stringResource(R.string.cpp_prefs_vibrate_on_keypress),
                    checked = state.vibrateOnKeypress,
                    onCheckedChange = onVibrateChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.Contrast,
                    title = stringResource(R.string.cpp_high_contrast_text),
                    checked = state.highContrast,
                    onCheckedChange = onHighContrastChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.Keyboard,
                    title = stringResource(R.string.cpp_highlight_expressions),
                    summary = stringResource(R.string.cpp_highlight_expressions_summary),
                    checked = state.highlightExpressions,
                    onCheckedChange = onHighlightExpressionsChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.ScreenRotation,
                    title = stringResource(R.string.cpp_prefs_auto_rotate_screen),
                    checked = state.rotateScreen,
                    onCheckedChange = onRotateChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.Fullscreen,
                    title = stringResource(R.string.cpp_prefs_keep_screen_on),
                    checked = state.keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange
                )
            }
        }
    }

    listDialog?.let { dialog ->
        SelectionDialog(
            title = dialog.title,
            options = dialog.options,
            selectedIndex = dialog.selectedIndex,
            onDismiss = { listDialog = null },
            onSelected = {
                dialog.onSelected(it)
                listDialog = null
            }
        )
    }
}

// ============================================================================
// ONSCREEN SCREEN
// ============================================================================

@Composable
private fun OnscreenScreen(
    state: SettingsUiState,
    onShowAppIconChange: (Boolean) -> Unit,
    onThemeChange: (Preferences.SimpleTheme) -> Unit
) {
    val context = LocalContext.current
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }
    val simpleThemes = listOf(
        Preferences.SimpleTheme.default_theme,
        Preferences.SimpleTheme.material_theme,
        Preferences.SimpleTheme.material_light_theme,
        Preferences.SimpleTheme.metro_blue_theme
    )
    val simpleThemeNames = stringArrayResource(R.array.cpp_simple_theme_names).toList()
    val themeTitleText = stringResource(R.string.cpp_theme)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(R.string.cpp_floating_calculator)) {
                SwitchPreference(
                    icon = Icons.Rounded.Visibility,
                    title = stringResource(R.string.cpp_enable),
                    checked = state.onscreenShowAppIcon,
                    onCheckedChange = onShowAppIconChange
                )
                PreferenceItem(
                    icon = Icons.Rounded.Palette,
                    title = themeTitleText,
                    summary = simpleThemeSummary(state.onscreenTheme),
                    enabled = state.onscreenShowAppIcon,
                    onClick = {
                        listDialog = ListDialogState(
                            title = themeTitleText,
                            options = simpleThemeNames,
                            selectedIndex = simpleThemes.indexOf(state.onscreenTheme).coerceAtLeast(0),
                            onSelected = { index -> onThemeChange(simpleThemes[index]) }
                        )
                    }
                )
            }
        }
    }

    listDialog?.let { dialog ->
        SelectionDialog(
            title = dialog.title,
            options = dialog.options,
            selectedIndex = dialog.selectedIndex,
            onDismiss = { listDialog = null },
            onSelected = {
                dialog.onSelected(it)
                listDialog = null
            }
        )
    }
}

// ============================================================================
// WIDGET SCREEN
// ============================================================================

@Composable
private fun WidgetScreen(
    state: SettingsUiState,
    onThemeChange: (Preferences.SimpleTheme) -> Unit
) {
    val context = LocalContext.current
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }
    val simpleThemes = listOf(
        Preferences.SimpleTheme.default_theme,
        Preferences.SimpleTheme.material_theme,
        Preferences.SimpleTheme.material_light_theme,
        Preferences.SimpleTheme.metro_blue_theme
    )
    val simpleThemeNames = stringArrayResource(R.array.cpp_simple_theme_names).toList()
    val themeTitleText = stringResource(R.string.cpp_theme)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(R.string.cpp_widget)) {
                PreferenceItem(
                    icon = Icons.Rounded.Palette,
                    title = themeTitleText,
                    summary = simpleThemeSummary(state.widgetTheme),
                    onClick = {
                        listDialog = ListDialogState(
                            title = themeTitleText,
                            options = simpleThemeNames,
                            selectedIndex = simpleThemes.indexOf(state.widgetTheme).coerceAtLeast(0),
                            onSelected = { index -> onThemeChange(simpleThemes[index]) }
                        )
                    }
                )
            }
        }
    }

    listDialog?.let { dialog ->
        SelectionDialog(
            title = dialog.title,
            options = dialog.options,
            selectedIndex = dialog.selectedIndex,
            onDismiss = { listDialog = null },
            onSelected = {
                dialog.onSelected(it)
                listDialog = null
            }
        )
    }
}

// ============================================================================
// OTHER SCREEN
// ============================================================================

@Composable
private fun OtherScreen(
    state: SettingsUiState,
    onCalculateOnFlyChange: (Boolean) -> Unit,
    onShowReleaseNotesChange: (Boolean) -> Unit,
    onUseBackAsPreviousChange: (Boolean) -> Unit,
    onPlotImagChange: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(R.string.cpp_other)) {
                SwitchPreference(
                    icon = Icons.Rounded.Speed,
                    title = stringResource(R.string.p_calculations_calculate_on_fly_title),
                    checked = state.calculateOnFly,
                    onCheckedChange = onCalculateOnFlyChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.History,
                    title = stringResource(R.string.c_calc_show_release_notes_title),
                    checked = state.showReleaseNotes,
                    onCheckedChange = onShowReleaseNotesChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.Keyboard,
                    title = stringResource(R.string.c_calc_use_back_button_as_prev_title),
                    checked = state.useBackAsPrevious,
                    onCheckedChange = onUseBackAsPreviousChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.ShowChart,
                    title = stringResource(R.string.cpp_plot_imaginary_part),
                    summary = stringResource(R.string.cpp_plot_imaginary_part_summary),
                    checked = state.plotImag,
                    onCheckedChange = onPlotImagChange
                )
            }
        }
    }
}

// ============================================================================
// COMPONENTS
// ============================================================================

@Composable
private fun SupportProjectCard(onSupportProject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onSupportProject
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.c_calc_ad_free_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.c_calc_ad_free_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun PreferenceGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun PreferenceItem(
    icon: ImageVector,
    title: String,
    summary: String? = null,
    enabled: Boolean = true,
    showArrow: Boolean = false,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = summary?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = if (showArrow) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun SwitchPreference(
    icon: ImageVector,
    title: String,
    summary: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable { onCheckedChange(!checked) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = summary?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// ============================================================================
// DIALOGS
// ============================================================================

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelected: (Int) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 24.dp)) {
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Options
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    itemsIndexed(options) { index, option ->
                        val isSelected = index == selectedIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSelected,
                                    onClick = { onSelected(index) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.cpp_cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun PrecisionDialog(
    value: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var precision by remember { mutableStateOf(value.coerceIn(1, 15)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.cpp_precision),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Value display
                Text(
                    text = precision.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Slider
                Slider(
                    value = precision.toFloat(),
                    onValueChange = { precision = it.toInt() },
                    valueRange = 1f..15f,
                    steps = 13,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "1",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "15",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.cpp_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(onClick = { onConfirm(precision) }) {
                        Text(text = stringResource(R.string.cpp_done))
                    }
                }
            }
        }
    }
}

// ============================================================================
// HELPERS
// ============================================================================

@Composable
private fun modeTitle(mode: Preferences.Gui.Mode): String {
    return when (mode) {
        Preferences.Gui.Mode.modern -> stringResource(R.string.cpp_mode_modern)
        Preferences.Gui.Mode.engineer -> stringResource(R.string.cpp_mode_engineer)
        Preferences.Gui.Mode.simple -> stringResource(R.string.cpp_mode_simple)
    }
}

@Composable
private fun separatorSummary(separator: Char): String {
    return when (separator) {
        '\'' -> stringResource(R.string.cpp_thousands_separator_apostrophe)
        ' ' -> stringResource(R.string.cpp_thousands_separator_space)
        0.toChar() -> stringResource(R.string.cpp_thousands_separator_no)
        else -> stringResource(R.string.cpp_thousands_separator_no)
    }
}

@Composable
private fun simpleThemeSummary(theme: Preferences.SimpleTheme): String {
    return when (theme) {
        Preferences.SimpleTheme.material_theme -> stringResource(R.string.cpp_theme_dark)
        Preferences.SimpleTheme.material_light_theme -> stringResource(R.string.cpp_theme_light)
        Preferences.SimpleTheme.metro_blue_theme -> stringResource(R.string.cpp_theme_metro_blue)
        Preferences.SimpleTheme.default_theme -> stringResource(R.string.cpp_theme_app)
    }
}

private data class ListDialogState(
    val title: String,
    val options: List<String>,
    val selectedIndex: Int,
    val onSelected: (Int) -> Unit
)
