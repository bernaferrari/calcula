package org.solovyev.android.calculator.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material.icons.rounded.Check
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.ui.Res
import org.solovyev.android.calculator.ui.* // For c_var_my etc if needed, but Res properties might be nested?
// import org.solovyev.android.calculator.ui.generated.resources.* // Removed

// ============================================================================
// KMP Settings Data Types
// ============================================================================

enum class CalculatorMode(val displayName: String) {
    MODERN("Modern"),
    ENGINEER("Engineer"),
    SIMPLE("Simple")
}

enum class AngleUnit(val displayName: String) {
    DEG("Degrees"),
    RAD("Radians"),
    GRAD("Gradians"),
    TURNS("Turns")
}

enum class NumeralBase(val displayName: String) {
    DEC("Decimal"),
    HEX("Hexadecimal"),
    OCT("Octal"),
    BIN("Binary")
}

enum class OutputNotation(val displayName: String) {
    PLAIN("Plain"),
    ENGINEERING("Engineering"),
    SCIENTIFIC("Scientific")
}

enum class AppTheme(val displayName: String) {
    MATERIAL_YOU("Material You"),
    MATERIAL_DARK("Material Dark"),
    MATERIAL_BLACK("Material Black"),
    MATERIAL_LIGHT("Material Light"),
    METRO_BLUE("Metro Blue"),
    METRO_GREEN("Metro Green"),
    METRO_PURPLE("Metro Purple")
}

enum class SimpleTheme(val displayName: String) {
    DEFAULT("Default"),
    MATERIAL_DARK("Material Dark"),
    MATERIAL_LIGHT("Material Light"),
    METRO_BLUE("Metro Blue")
}

data class SettingsUiState(
    val mode: CalculatorMode = CalculatorMode.SIMPLE,
    val angleUnit: AngleUnit = AngleUnit.DEG,
    val numeralBase: NumeralBase = NumeralBase.DEC,
    val outputNotation: OutputNotation = OutputNotation.PLAIN,
    val outputPrecision: Int = 5,
    val outputSeparator: Char = ' ',
    val theme: AppTheme = AppTheme.MATERIAL_YOU,
    val themeSeedColor: Int = 0xFF13ABF1.toInt(),
    val isAmoledTheme: Boolean = false,
    val languageCode: String = "system",
    val vibrateOnKeypress: Boolean = true,
    val highContrast: Boolean = false,
    val highlightExpressions: Boolean = true,
    val rotateScreen: Boolean = true,
    val keepScreenOn: Boolean = true,
    val onscreenShowAppIcon: Boolean = true,
    val onscreenTheme: SimpleTheme = SimpleTheme.DEFAULT,
    val widgetTheme: SimpleTheme = SimpleTheme.DEFAULT,
    val calculateOnFly: Boolean = true,
    val showReleaseNotes: Boolean = true,
    val useBackAsPrevious: Boolean = false,
    val plotImag: Boolean = false,
    val latexMode: Boolean = false,
    val numberFormatExamples: String = "1,234.56\n0.001234"
)

data class Language(val code: String, val displayName: String)

enum class SettingsDestination {
    MAIN,
    NUMBER_FORMAT,
    APPEARANCE,
    ONSCREEN,
    WIDGET,
    OTHER
}

// ============================================================================
// Settings Actions Interface
// ============================================================================

interface SettingsActions {
    fun setMode(mode: CalculatorMode)
    fun setAngleUnit(unit: AngleUnit)
    fun setNumeralBase(base: NumeralBase)
    fun setOutputNotation(notation: OutputNotation)
    fun setOutputPrecision(precision: Int)
    fun setOutputSeparator(separator: Char)
    fun setTheme(theme: AppTheme)
    fun setThemeSeedColor(color: Int)
    fun setIsAmoledTheme(enabled: Boolean)
    fun setLanguage(code: String)
    fun setVibrateOnKeypress(enabled: Boolean)
    fun setHighContrast(enabled: Boolean)
    fun setHighlightExpressions(enabled: Boolean)
    fun setRotateScreen(enabled: Boolean)
    fun setKeepScreenOn(enabled: Boolean)
    fun setOnscreenShowAppIcon(enabled: Boolean)
    fun setOnscreenTheme(theme: SimpleTheme)
    fun setWidgetTheme(theme: SimpleTheme)
    fun setCalculateOnFly(enabled: Boolean)
    fun setShowReleaseNotes(enabled: Boolean)
    fun setUseBackAsPrevious(enabled: Boolean)
    fun setPlotImag(enabled: Boolean)
    fun setLatexMode(enabled: Boolean)
}

// ============================================================================
// MAIN SETTINGS SCREEN
// ============================================================================

@Composable
fun SettingsScreen(
    destination: SettingsDestination,
    state: SettingsUiState,
    actions: SettingsActions,
    languages: List<Language> = emptyList(),
    adFreePurchased: Boolean = true,
    onNavigate: (SettingsDestination) -> Unit,
    onStartWizard: () -> Unit = {},
    onReportBug: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onSupportProject: () -> Unit = {}
) {
    when (destination) {
        SettingsDestination.MAIN -> MainSettingsScreen(
            state = state,
            adFreePurchased = adFreePurchased,
            onModeChange = actions::setMode,
            onAngleUnitChange = actions::setAngleUnit,
            onNumeralBaseChange = actions::setNumeralBase,
            onNavigate = onNavigate,
            onStartWizard = onStartWizard,
            onReportBug = onReportBug,
            onOpenAbout = onOpenAbout,
            onSupportProject = onSupportProject
        )
        SettingsDestination.NUMBER_FORMAT -> NumberFormatScreen(
            state = state,
            onNotationChange = actions::setOutputNotation,
            onPrecisionChange = actions::setOutputPrecision,
            onSeparatorChange = actions::setOutputSeparator
        )
        SettingsDestination.APPEARANCE -> AppearanceScreen(
            state = state,
            languages = languages,
            onLanguageChange = actions::setLanguage,
            onThemeChange = actions::setTheme,
            onVibrateChange = actions::setVibrateOnKeypress,
            onHighContrastChange = actions::setHighContrast,
            onHighlightExpressionsChange = actions::setHighlightExpressions,
            onRotateChange = actions::setRotateScreen,
            onKeepScreenOnChange = actions::setKeepScreenOn,
            actions = actions
        )
        SettingsDestination.ONSCREEN -> OnscreenScreen(
            state = state,
            onShowAppIconChange = actions::setOnscreenShowAppIcon,
            onThemeChange = actions::setOnscreenTheme
        )
        SettingsDestination.WIDGET -> WidgetScreen(
            state = state,
            onThemeChange = actions::setWidgetTheme
        )
        SettingsDestination.OTHER -> OtherScreen(
            state = state,
            onCalculateOnFlyChange = actions::setCalculateOnFly,
            onShowReleaseNotesChange = actions::setShowReleaseNotes,
            onUseBackAsPreviousChange = actions::setUseBackAsPrevious,
            onPlotImagChange = actions::setPlotImag,
            onLatexModeChange = actions::setLatexMode
        )
    }
}

@Composable
private fun MainSettingsScreen(
    state: SettingsUiState,
    adFreePurchased: Boolean,
    onModeChange: (CalculatorMode) -> Unit,
    onAngleUnitChange: (AngleUnit) -> Unit,
    onNumeralBaseChange: (NumeralBase) -> Unit,
    onNavigate: (SettingsDestination) -> Unit,
    onStartWizard: () -> Unit,
    onReportBug: () -> Unit,
    onOpenAbout: () -> Unit,
    onSupportProject: () -> Unit
) {
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }

    val modeOptions = CalculatorMode.entries.map { it.displayName }
    val angleOptions = AngleUnit.entries
    val radixOptions = NumeralBase.entries

    val modeTitle = stringResource(Res.string.cpp_mode)
    val anglesTitle = stringResource(Res.string.cpp_angles)
    val radixTitle = stringResource(Res.string.cpp_radix)

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
            PreferenceGroup(title = stringResource(Res.string.cpp_prefs_basic)) {
                PreferenceItem(
                    icon = Icons.Rounded.Calculate,
                    title = stringResource(Res.string.cpp_mode),
                    summary = state.mode.displayName,
                    onClick = {
                        listDialog = ListDialogState(
                            title = modeTitle,
                            options = modeOptions,
                            selectedIndex = CalculatorMode.entries.indexOf(state.mode),
                            onSelected = { index -> onModeChange(CalculatorMode.entries[index]) }
                        )
                    }
                )
                PreferenceItem(
                    icon = Icons.Rounded.PinDrop,
                    title = stringResource(Res.string.cpp_angles),
                    summary = state.angleUnit.displayName,
                    onClick = {
                        listDialog = ListDialogState(
                            title = anglesTitle,
                            options = angleOptions.map { it.displayName },
                            selectedIndex = angleOptions.indexOf(state.angleUnit),
                            onSelected = { index -> onAngleUnitChange(angleOptions[index]) }
                        )
                    }
                )
                PreferenceItem(
                    icon = Icons.Rounded.Numbers,
                    title = stringResource(Res.string.cpp_radix),
                    summary = state.numeralBase.displayName,
                    onClick = {
                        listDialog = ListDialogState(
                            title = radixTitle,
                            options = radixOptions.map { it.displayName },
                            selectedIndex = radixOptions.indexOf(state.numeralBase),
                            onSelected = { index -> onNumeralBaseChange(radixOptions[index]) }
                        )
                    }
                )
                PreferenceItem(
                    icon = Icons.AutoMirrored.Rounded.FormatListBulleted,
                    title = stringResource(Res.string.cpp_number_format),
                    summary = stringResource(Res.string.cpp_examples),
                    showArrow = true,
                    onClick = { onNavigate(SettingsDestination.NUMBER_FORMAT) }
                )
            }
        }

        // Appearance & Advanced Section
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_prefs_advanced)) {
                PreferenceItem(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(Res.string.cpp_appearance),
                    summary = stringResource(Res.string.cpp_theme),
                    showArrow = true,
                    onClick = { onNavigate(SettingsDestination.APPEARANCE) }
                )
                PreferenceItem(
                    icon = Icons.Rounded.PhoneAndroid,
                    title = stringResource(Res.string.cpp_floating_calculator),
                    summary = stringResource(Res.string.cpp_theme),
                    showArrow = true,
                    onClick = { onNavigate(SettingsDestination.ONSCREEN) }
                )
                PreferenceItem(
                    icon = Icons.Rounded.Widgets,
                    title = stringResource(Res.string.cpp_widget),
                    summary = stringResource(Res.string.cpp_theme),
                    showArrow = true,
                    onClick = { onNavigate(SettingsDestination.WIDGET) }
                )
                PreferenceItem(
                    icon = Icons.Rounded.Settings,
                    title = stringResource(Res.string.cpp_other),
                    showArrow = true,
                    onClick = { onNavigate(SettingsDestination.OTHER) }
                )
            }
        }

        // Help Section
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_help)) {
                PreferenceItem(
                    icon = Icons.Rounded.FlashOn,
                    title = stringResource(Res.string.cpp_introduction),
                    showArrow = true,
                    onClick = onStartWizard
                )
                PreferenceItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(Res.string.cpp_report_problem),
                    showArrow = true,
                    onClick = onReportBug
                )
                PreferenceItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(Res.string.cpp_about),
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
    onNotationChange: (OutputNotation) -> Unit,
    onPrecisionChange: (Int) -> Unit,
    onSeparatorChange: (Char) -> Unit
) {
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }
    var precisionDialog by remember { mutableStateOf(false) }
    
    val separatorNames = listOf("None", "Space", "Comma", "Apostrophe")
    val separatorValues = listOf('\u0000', ' ', ',', '\'')
    val notationOptions = OutputNotation.entries

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_number_format)) {
                PreferenceItem(
                    icon = Icons.Rounded.Tune,
                    title = stringResource(Res.string.cpp_format),
                    summary = state.outputNotation.displayName,
                    onClick = {
                        listDialog = ListDialogState(
                            title = "Format",
                            options = notationOptions.map { it.displayName },
                            selectedIndex = notationOptions.indexOf(state.outputNotation),
                            onSelected = { index -> onNotationChange(notationOptions[index]) }
                        )
                    }
                )
                PreferenceItem(
                    icon = Icons.Rounded.Numbers,
                    title = stringResource(Res.string.cpp_precision),
                    summary = state.outputPrecision.toString(),
                    onClick = { precisionDialog = true }
                )
                PreferenceItem(
                    icon = Icons.Rounded.TextFields,
                    title = stringResource(Res.string.cpp_thousands_separator),
                    summary = separatorSummary(state.outputSeparator),
                    onClick = {
                        val selectedIndex = separatorValues.indexOf(state.outputSeparator).coerceAtLeast(0)
                        listDialog = ListDialogState(
                            title = "Thousands Separator",
                            options = separatorNames,
                            selectedIndex = selectedIndex,
                            onSelected = { index -> onSeparatorChange(separatorValues[index]) }
                        )
                    }
                )
            }
        }

        // Examples Card
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_examples)) {
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
    onThemeChange: (AppTheme) -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onHighlightExpressionsChange: (Boolean) -> Unit,
    onRotateChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    actions: SettingsActions // Access to new actions
) {
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }
    
    // Simplified Theme Options for Light/Dark/System
    // We Map MATERIAL_YOU -> System, MATERIAL_LIGHT -> Light, MATERIAL_DARK -> Dark
    // ignoring other legacy themes for this selector
    val themeModeOptions = listOf(
        AppTheme.MATERIAL_YOU to "System",
        AppTheme.MATERIAL_LIGHT to "Light",
        AppTheme.MATERIAL_DARK to "Dark"
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ThemePreview(state.theme, state.themeSeedColor)
        }

        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_appearance)) {
                // Color Picker
                ThemeSelector(
                    currentSeedColor = state.themeSeedColor,
                    isAmoled = state.isAmoledTheme,
                    onSeedColorChange = actions::setThemeSeedColor,
                    onAmoledChange = { actions.setIsAmoledTheme(it) }
                )

                HorizontalDivider()

                // Light/Dark Mode
                 PreferenceItem(
                    icon = Icons.Rounded.LightMode,
                    title = "Mode",
                    summary = themeModeOptions.find { it.first == state.theme }?.second ?: "Dark",
                    onClick = {
                        listDialog = ListDialogState(
                            title = "Mode",
                            options = themeModeOptions.map { it.second },
                            selectedIndex = themeModeOptions.indexOfFirst { it.first == state.theme }.coerceAtLeast(0),
                            onSelected = { index -> onThemeChange(themeModeOptions[index].first) }
                        )
                    }
                )
                
                if (languages.isNotEmpty()) {
                    PreferenceItem(
                        icon = Icons.Rounded.Language,
                        title = stringResource(Res.string.cpp_language),
                        summary = languages.firstOrNull { it.code == state.languageCode }?.displayName ?: "System",
                        onClick = {
                            listDialog = ListDialogState(
                                title = "Language",
                                options = languages.map { it.displayName },
                                selectedIndex = languages.indexOfFirst { it.code == state.languageCode }.coerceAtLeast(0),
                                onSelected = { index -> onLanguageChange(languages[index].code) }
                            )
                        }
                    )
                }
            }
        }

        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_prefs_advanced)) {
                SwitchPreference(
                    icon = Icons.Rounded.Vibration,
                    title = stringResource(Res.string.cpp_prefs_vibrate_on_keypress),
                    checked = state.vibrateOnKeypress,
                    onCheckedChange = onVibrateChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.Contrast,
                    title = stringResource(Res.string.cpp_high_contrast_text),
                    checked = state.highContrast,
                    onCheckedChange = onHighContrastChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.Keyboard,
                    title = stringResource(Res.string.cpp_highlight_expressions),
                    summary = stringResource(Res.string.cpp_highlight_expressions_summary),
                    checked = state.highlightExpressions,
                    onCheckedChange = onHighlightExpressionsChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.ScreenRotation,
                    title = stringResource(Res.string.cpp_prefs_auto_rotate_screen),
                    checked = state.rotateScreen,
                    onCheckedChange = onRotateChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.Fullscreen,
                    title = stringResource(Res.string.cpp_prefs_keep_screen_on),
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

@Composable
private fun ThemeSelector(
    currentSeedColor: Int,
    isAmoled: Boolean,
    onSeedColorChange: (Int) -> Unit,
    onAmoledChange: (Boolean) -> Unit
) {
    val colors = listOf(
        0xFF13ABF1, // Blue (Default)
        0xFFE91E63, // Pink
        0xFFF44336, // Red
        0xFFFF9800, // Orange
        0xFF4CAF50, // Green
        0xFF009688, // Teal
        0xFF673AB7, // Deep Purple
        0xFF3F51B5, // Indigo
        0xFF795548, // Brown
        0xFF607D8B  // Blue Grey
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Color Scheme",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(colors) { colorInt ->
                val color = Color(colorInt)
                val isSelected = currentSeedColor == colorInt.toInt()
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onSeedColorChange(colorInt.toInt()) }
                        .then(
                            if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onAmoledChange(!isAmoled) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AMOLED Black",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Pure black background for OLED screens",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isAmoled,
                onCheckedChange = onAmoledChange
            )
        }
    }
}

// ============================================================================
// ONSCREEN SCREEN
// ============================================================================

@Composable
private fun OnscreenScreen(
    state: SettingsUiState,
    onShowAppIconChange: (Boolean) -> Unit,
    onThemeChange: (SimpleTheme) -> Unit
) {
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }
    val simpleThemes = SimpleTheme.entries

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_floating_calculator)) {
                SwitchPreference(
                    icon = Icons.Rounded.Visibility,
                    title = stringResource(Res.string.cpp_enable),
                    checked = state.onscreenShowAppIcon,
                    onCheckedChange = onShowAppIconChange
                )
                PreferenceItem(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(Res.string.cpp_theme),
                    summary = state.onscreenTheme.displayName,
                    enabled = state.onscreenShowAppIcon,
                    onClick = {
                        listDialog = ListDialogState(
                            title = "Theme",
                            options = simpleThemes.map { it.displayName },
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
    onThemeChange: (SimpleTheme) -> Unit
) {
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }
    val simpleThemes = SimpleTheme.entries

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_widget)) {
                PreferenceItem(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(Res.string.cpp_theme),
                    summary = state.widgetTheme.displayName,
                    onClick = {
                        listDialog = ListDialogState(
                            title = "Theme",
                            options = simpleThemes.map { it.displayName },
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
    onPlotImagChange: (Boolean) -> Unit,
    onLatexModeChange: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_other)) {
                SwitchPreference(
                    icon = Icons.Rounded.Speed,
                    title = stringResource(Res.string.p_calculations_calculate_on_fly_title),
                    checked = state.calculateOnFly,
                    onCheckedChange = onCalculateOnFlyChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.History,
                    title = stringResource(Res.string.c_calc_show_release_notes_title),
                    checked = state.showReleaseNotes,
                    onCheckedChange = onShowReleaseNotesChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.Keyboard,
                    title = stringResource(Res.string.c_calc_use_back_button_as_prev_title),
                    checked = state.useBackAsPrevious,
                    onCheckedChange = onUseBackAsPreviousChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.ShowChart,
                    title = stringResource(Res.string.cpp_plot_imaginary_part),
                    summary = stringResource(Res.string.cpp_plot_imaginary_part_summary),
                    checked = state.plotImag,
                    onCheckedChange = onPlotImagChange
                )
                SwitchPreference(
                    icon = Icons.Rounded.Code,
                    title = "LaTeX Output Mode",
                    summary = "Generate LaTeX syntax instead of calculations",
                    checked = state.latexMode,
                    onCheckedChange = onLatexModeChange
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
                    text = stringResource(Res.string.c_calc_ad_free_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.c_calc_ad_free_summary),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun PreferenceGroup(
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
fun PreferenceItem(
    icon: ImageVector,
    title: String,
    summary: String? = null,
    showArrow: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = if (summary != null) {
            {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        trailingContent = if (showArrow) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null
    )
}

@Composable
fun SwitchPreference(
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
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = if (summary != null) {
            {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

// ============================================================================
// DIALOGS
// ============================================================================

private data class ListDialogState(
    val title: String,
    val options: List<String>,
    val selectedIndex: Int,
    val onSelected: (Int) -> Unit
)

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
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .selectable(
                                selected = index == selectedIndex,
                                onClick = { onSelected(index) },
                                role = Role.RadioButton
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
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
    var sliderValue by remember { mutableStateOf(value.toFloat()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Precision",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = sliderValue.toInt().toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..16f,
                    steps = 15,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(onClick = { onConfirm(sliderValue.toInt()) }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

// ============================================================================
// HELPERS
// ============================================================================

private fun separatorSummary(separator: Char): String {
    return when (separator) {
        '\u0000' -> "None"
        ' ' -> "Space"
        ',' -> "Comma (,)"
        '\'' -> "Apostrophe (')"
        else -> separator.toString()
    }
}

@Composable
private fun ThemePreview(theme: AppTheme, seedColor: Int = 0xFF13ABF1.toInt()) {
    val (backgroundColor, accentColor) = getThemePreviewColors(theme, seedColor)
    val isLight = theme == AppTheme.MATERIAL_LIGHT || theme == AppTheme.MATERIAL_YOU // Approximation
    val textColor = if (isLight) Color.Black else Color.White
    val buttonColor = if (isLight) Color.LightGray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header: Theme Name
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = textColor.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Mock Display
            Text(
                text = "1,234.56",
                style = MaterialTheme.typography.displayMedium,
                color = textColor,
                modifier = Modifier.align(Alignment.End)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mock Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AC Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(accentColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AC", color = accentColor, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Op Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(buttonColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Calculate, null, tint = textColor)
                }
                
                // Equals Button
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(48.dp)
                        .background(accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("=", color = if (isLight) Color.White else Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Helper to provide approximate colors for the preview
// Ideally these would come from the actual theme definition
private fun getThemePreviewColors(theme: AppTheme, seedColor: Int): Pair<Color, Color> {
    return when (theme) {
        AppTheme.MATERIAL_YOU -> Pair(Color(0xFFF2F0F4), Color(seedColor)) // M3 Light-ish with seed
        AppTheme.MATERIAL_DARK -> Pair(Color(0xFF1C1B1F), Color(0xFFD0BCFF))
        AppTheme.MATERIAL_BLACK -> Pair(Color(0xFF000000), Color(0xFFBB86FC))
        AppTheme.MATERIAL_LIGHT -> Pair(Color(0xFFFFFFFF), Color(0xFF6200EE))
        AppTheme.METRO_BLUE -> Pair(Color(0xFF111111), Color(0xFF00ADEF))
        AppTheme.METRO_GREEN -> Pair(Color(0xFF111111), Color(0xFF009900))
        AppTheme.METRO_PURPLE -> Pair(Color(0xFF111111), Color(0xFFAA00FF))
    }
}
