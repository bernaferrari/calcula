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
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
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
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.ui.Res
import org.solovyev.android.calculator.ui.* // For c_var_my etc if needed, but Res properties might be nested?
import org.solovyev.android.calculator.ui.theme.platformDynamicColorScheme
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

enum class AppearanceMode(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class SimpleTheme(val displayName: String) {
    DEFAULT("Default"),
    MATERIAL_DARK("Material Dark"),
    MATERIAL_LIGHT("Material Light"),
    METRO_BLUE("Metro Blue")
}

enum class OutputSeparator(val displayName: String, val symbol: Char) {
    NONE("None", '\u0000'),
    SPACE("Space", ' '),
    COMMA("Comma", ','),
    DOT("Dot", '.'),
    UNDERSCORE("Underscore", '_')
}

enum class MultiplicationSign(val displayName: String, val symbol: String) {
    DOT("Dot", "·"),
    CROSS("Cross", "×"),
    STAR("Star", "*")
}

data class SettingsUiState(
    val mode: CalculatorMode = CalculatorMode.SIMPLE,
    val angleUnit: AngleUnit = AngleUnit.DEG,
    val numeralBase: NumeralBase = NumeralBase.DEC,
    val outputNotation: OutputNotation = OutputNotation.PLAIN,
    val outputPrecision: Int = 5,
    val outputSeparator: OutputSeparator = OutputSeparator.SPACE,
    val multiplicationSign: MultiplicationSign = MultiplicationSign.CROSS,
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val theme: AppTheme = AppTheme.MATERIAL_YOU,
    val themeSeedColor: Int = 0xFF13ABF1.toInt(),
    val dynamicColorEnabled: Boolean = true,
    val isAmoledTheme: Boolean = false,
    val languageCode: String = "system",
    val vibrateOnKeypress: Boolean = true,
    val highContrast: Boolean = false,
    val highlightExpressions: Boolean = true,
    val rotateScreen: Boolean = true,
    val keepScreenOn: Boolean = true,
    val widgetTheme: SimpleTheme = SimpleTheme.DEFAULT,
    val calculateOnFly: Boolean = true,
    val rpnMode: Boolean = false,
    val tapeMode: Boolean = false,
    val showReleaseNotes: Boolean = true,
    val showCalculationLatency: Boolean = false,
    val useBackAsPrevious: Boolean = false,
    val plotImag: Boolean = false,
    val latexMode: Boolean = false,
    val bitwiseWordSize: Int = 32,
    val bitwiseSigned: Boolean = false,
    val reduceMotion: Boolean = false,
    val fontScale: Float = 1.0f,
    val numberFormatExamples: String = "1,234.56\n0.001234"
)

data class Language(val code: String, val displayName: String)

enum class SettingsDestination {
    MAIN,
    NUMBER_FORMAT,
    APPEARANCE,
    ACCESSIBILITY,
    WIDGET,
    OTHER
}

private val SettingsContentPadding = PaddingValues(
    start = 16.dp,
    top = 20.dp,
    end = 16.dp,
    bottom = 20.dp
)

private enum class PreferencePosition {
    SINGLE,
    TOP,
    MIDDLE,
    BOTTOM
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
    fun setOutputSeparator(separator: OutputSeparator)
    fun setMultiplicationSign(sign: MultiplicationSign)
    fun setAppearanceMode(mode: AppearanceMode)
    fun setTheme(theme: AppTheme)
    fun setDynamicColor(enabled: Boolean)
    fun setThemeSeedColor(color: Int)
    fun setIsAmoledTheme(enabled: Boolean)
    fun setLanguage(code: String)
    fun setVibrateOnKeypress(enabled: Boolean)
    fun setHighContrast(enabled: Boolean)
    fun setHighlightExpressions(enabled: Boolean)
    fun setRotateScreen(enabled: Boolean)
    fun setKeepScreenOn(enabled: Boolean)
    fun setWidgetTheme(theme: SimpleTheme)
    fun setCalculateOnFly(enabled: Boolean)
    fun setRpnMode(enabled: Boolean)
    fun setTapeMode(enabled: Boolean)
    fun setShowReleaseNotes(enabled: Boolean)
    fun setShowCalculationLatency(enabled: Boolean)
    fun setUseBackAsPrevious(enabled: Boolean)
    fun setPlotImag(enabled: Boolean)
    fun setLatexMode(enabled: Boolean)
    fun setReduceMotion(enabled: Boolean)
    fun setFontScale(scale: Float)
    fun setBitwiseWordSize(size: Int)
    fun setBitwiseSigned(signed: Boolean)
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
            onSeparatorChange = { separator -> actions.setOutputSeparator(separator) }
        )
        SettingsDestination.APPEARANCE -> AppearanceScreen(
            state = state,
            languages = languages,
            onLanguageChange = actions::setLanguage,
            onAppearanceModeChange = actions::setAppearanceMode,
            onThemeChange = actions::setTheme,
            onDynamicColorChange = actions::setDynamicColor,
            onVibrateChange = actions::setVibrateOnKeypress,
            onHighContrastChange = actions::setHighContrast,
            onHighlightExpressionsChange = actions::setHighlightExpressions,
            onRotateChange = actions::setRotateScreen,
            onKeepScreenOnChange = actions::setKeepScreenOn,
            actions = actions
        )
        SettingsDestination.ACCESSIBILITY -> AccessibilityScreen(
            state = state,
            onHighContrastChange = actions::setHighContrast,
            onReduceMotionChange = actions::setReduceMotion,
            onFontScaleChange = actions::setFontScale
        )
        SettingsDestination.WIDGET -> WidgetScreen(
            state = state,
            onThemeChange = actions::setWidgetTheme
        )
        SettingsDestination.OTHER -> OtherScreen(
            state = state,
            onCalculateOnFlyChange = actions::setCalculateOnFly,
            onRpnModeChange = actions::setRpnMode,
            onTapeModeChange = actions::setTapeMode,
            onShowReleaseNotesChange = actions::setShowReleaseNotes,
            onShowCalculationLatencyChange = actions::setShowCalculationLatency,
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
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
                InlineChoicePreference(
                    icon = Icons.Filled.Calculate,
                    title = stringResource(Res.string.cpp_mode),
                    summary = state.mode.displayName,
                    options = CalculatorMode.entries.map { it.displayName },
                    selectedIndex = CalculatorMode.entries.indexOf(state.mode).coerceAtLeast(0),
                    position = PreferencePosition.TOP,
                    onOptionSelected = { index ->
                        onModeChange(CalculatorMode.entries[index])
                    }
                )
                InlineChoicePreference(
                    icon = Icons.Filled.LocationOn,
                    title = stringResource(Res.string.cpp_angles),
                    summary = state.angleUnit.displayName,
                    options = listOf("DEG", "RAD", "GRAD", "TURN"),
                    selectedIndex = AngleUnit.entries.indexOf(state.angleUnit).coerceAtLeast(0),
                    position = PreferencePosition.MIDDLE,
                    onOptionSelected = { index ->
                        onAngleUnitChange(AngleUnit.entries[index])
                    }
                )
                InlineChoicePreference(
                    icon = Icons.Filled.Edit,
                    title = stringResource(Res.string.cpp_radix),
                    summary = state.numeralBase.displayName,
                    options = listOf("DEC", "HEX", "OCT", "BIN"),
                    selectedIndex = NumeralBase.entries.indexOf(state.numeralBase).coerceAtLeast(0),
                    position = PreferencePosition.MIDDLE,
                    onOptionSelected = { index ->
                        onNumeralBaseChange(NumeralBase.entries[index])
                    }
                )
                PreferenceItem(
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    title = stringResource(Res.string.cpp_number_format),
                    summary = stringResource(Res.string.cpp_examples),
                    position = PreferencePosition.BOTTOM,
                    onClick = { onNavigate(SettingsDestination.NUMBER_FORMAT) }
                )
            }
        }

        // Appearance & Advanced Section
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_prefs_advanced)) {
                PreferenceItem(
                    icon = Icons.Filled.Palette,
                    title = stringResource(Res.string.cpp_appearance),
                    summary = stringResource(Res.string.cpp_theme),
                    position = PreferencePosition.TOP,
                    onClick = { onNavigate(SettingsDestination.APPEARANCE) }
                )
                PreferenceItem(
                    icon = Icons.Filled.Widgets,
                    title = stringResource(Res.string.cpp_widget),
                    summary = stringResource(Res.string.cpp_theme),
                    position = PreferencePosition.MIDDLE,
                    onClick = { onNavigate(SettingsDestination.WIDGET) }
                )
                PreferenceItem(
                    icon = Icons.Filled.Settings,
                    title = stringResource(Res.string.cpp_other),
                    position = PreferencePosition.BOTTOM,
                    onClick = { onNavigate(SettingsDestination.OTHER) }
                )
            }
        }

        // Help Section
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_help)) {
                PreferenceItem(
                    icon = Icons.Filled.FlashOn,
                    title = stringResource(Res.string.cpp_introduction),
                    position = PreferencePosition.TOP,
                    onClick = onStartWizard
                )
                PreferenceItem(
                    icon = Icons.Filled.Info,
                    title = stringResource(Res.string.cpp_report_problem),
                    position = PreferencePosition.MIDDLE,
                    onClick = onReportBug
                )
                PreferenceItem(
                    icon = Icons.Filled.Info,
                    title = stringResource(Res.string.cpp_about),
                    position = PreferencePosition.BOTTOM,
                    onClick = onOpenAbout
                )
            }
        }
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
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_number_format)) {
                PreferenceItem(
                    icon = Icons.Filled.Tune,
                    title = stringResource(Res.string.cpp_format),
                    summary = state.outputNotation.displayName,
                    position = PreferencePosition.TOP,
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
                    icon = Icons.Filled.Edit,
                    title = stringResource(Res.string.cpp_precision),
                    summary = state.outputPrecision.toString(),
                    position = PreferencePosition.MIDDLE,
                    onClick = { precisionDialog = true }
                )
                PreferenceItem(
                    icon = Icons.Filled.TextFields,
                    title = stringResource(Res.string.cpp_thousands_separator),
                    summary = separatorSummary(state.outputSeparator.symbol),
                    position = PreferencePosition.BOTTOM,
                    onClick = {
                        val separatorValueSymbols = listOf('\u0000', ' ', ',', '\'')
                        val selectedIndex = separatorValueSymbols.indexOf(state.outputSeparator.symbol).coerceAtLeast(0)
                        listDialog = ListDialogState(
                            title = "Thousands Separator",
                            options = separatorNames,
                            selectedIndex = selectedIndex,
                            onSelected = { index -> onSeparatorChange(separatorValueSymbols[index]) }
                        )
                    }
                )
            }
        }

        // Examples Card
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_examples)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = preferenceItemShape(PreferencePosition.SINGLE),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        text = state.numberFormatExamples,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    )
                }
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
    onAppearanceModeChange: (AppearanceMode) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onHighlightExpressionsChange: (Boolean) -> Unit,
    onRotateChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    actions: SettingsActions // Access to new actions
) {
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ThemePreview(state.theme, state.themeSeedColor)
        }

        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_appearance)) {
                ThemeSelector(
                    currentSeedColor = state.themeSeedColor,
                    dynamicColorEnabled = state.dynamicColorEnabled,
                    onDynamicColorChange = onDynamicColorChange,
                    onSeedColorChange = actions::setThemeSeedColor,
                    position = PreferencePosition.TOP
                )

                AppearanceModeSegmentedPreference(
                    selectedMode = state.appearanceMode,
                    position = if (languages.isNotEmpty()) PreferencePosition.MIDDLE else PreferencePosition.BOTTOM,
                    onModeSelected = { mode ->
                        onAppearanceModeChange(mode)
                        onThemeChange(
                            when (mode) {
                                AppearanceMode.SYSTEM -> AppTheme.MATERIAL_YOU
                                AppearanceMode.LIGHT -> AppTheme.MATERIAL_LIGHT
                                AppearanceMode.DARK -> AppTheme.MATERIAL_DARK
                            }
                        )
                    }
                )
                
                if (languages.isNotEmpty()) {
                    PreferenceItem(
                        icon = Icons.Filled.Language,
                        title = stringResource(Res.string.cpp_language),
                        summary = languages.firstOrNull { it.code == state.languageCode }?.displayName ?: "System",
                        position = PreferencePosition.BOTTOM,
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
                    icon = Icons.Filled.Vibration,
                    title = stringResource(Res.string.cpp_prefs_vibrate_on_keypress),
                    checked = state.vibrateOnKeypress,
                    position = PreferencePosition.TOP,
                    onCheckedChange = onVibrateChange
                )
                SwitchPreference(
                    icon = Icons.Filled.Contrast,
                    title = stringResource(Res.string.cpp_high_contrast_text),
                    checked = state.highContrast,
                    position = PreferencePosition.MIDDLE,
                    onCheckedChange = onHighContrastChange
                )
                SwitchPreference(
                    icon = Icons.Filled.Keyboard,
                    title = stringResource(Res.string.cpp_highlight_expressions),
                    summary = stringResource(Res.string.cpp_highlight_expressions_summary),
                    checked = state.highlightExpressions,
                    position = PreferencePosition.MIDDLE,
                    onCheckedChange = onHighlightExpressionsChange
                )
                SwitchPreference(
                    icon = Icons.Filled.ScreenRotation,
                    title = stringResource(Res.string.cpp_prefs_auto_rotate_screen),
                    checked = state.rotateScreen,
                    position = PreferencePosition.MIDDLE,
                    onCheckedChange = onRotateChange
                )
                SwitchPreference(
                    icon = Icons.Filled.Fullscreen,
                    title = stringResource(Res.string.cpp_prefs_keep_screen_on),
                    checked = state.keepScreenOn,
                    position = PreferencePosition.BOTTOM,
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
private fun AppearanceModeSegmentedPreference(
    selectedMode: AppearanceMode,
    position: PreferencePosition,
    onModeSelected: (AppearanceMode) -> Unit
) {
    val options = listOf(
        Triple(AppearanceMode.SYSTEM, "System", Icons.Filled.BrightnessAuto),
        Triple(AppearanceMode.LIGHT, "Light", Icons.Filled.LightMode),
        Triple(AppearanceMode.DARK, "Dark", Icons.Filled.DarkMode)
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = preferenceItemShape(position),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, (mode, label, icon) ->
                    SegmentedButton(
                        selected = selectedMode == mode,
                        onClick = { onModeSelected(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        icon = {
                            SegmentedButtonDefaults.Icon(active = selectedMode == mode) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            }
                        }
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSelector(
    currentSeedColor: Int,
    dynamicColorEnabled: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    onSeedColorChange: (Int) -> Unit,
    position: PreferencePosition = PreferencePosition.SINGLE
) {
    val colors = listOf(
        0xFF13ABF1,
        0xFFE91E63,
        0xFFF44336,
        0xFFFF9800,
        0xFF4CAF50,
        0xFF009688,
        0xFF673AB7,
        0xFF3F51B5
    )
    val dynamicColorAvailable = platformDynamicColorScheme(darkTheme = false) != null

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = preferenceItemShape(position),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    val dynamicSelected = dynamicColorEnabled
                    Surface(
                        modifier = Modifier
                            .size(width = 64.dp, height = 44.dp)
                            .clip(RoundedCornerShape(if (dynamicSelected) 16.dp else 12.dp))
                            .clickable(enabled = dynamicColorAvailable, role = Role.Button) {
                                onDynamicColorChange(true)
                            },
                        color = if (dynamicColorAvailable) {
                            MaterialTheme.colorScheme.primary.copy(alpha = if (dynamicSelected) 1f else 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Auto",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (dynamicColorAvailable) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
                items(colors) { colorInt ->
                    val color = Color(colorInt)
                    val isSelected = !dynamicColorEnabled && currentSeedColor == colorInt.toInt()

                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(if (isSelected) 16.dp else 12.dp))
                            .clickable(role = Role.Button) {
                                onDynamicColorChange(false)
                                onSeedColorChange(colorInt.toInt())
                            }
                            .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(16.dp)) else Modifier),
                        color = color
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    }
                }
            }
            if (!dynamicColorAvailable) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dynamic colors require Android 12+",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// ACCESSIBILITY SCREEN
// ============================================================================

@Composable
private fun AccessibilityScreen(
    state: SettingsUiState,
    onHighContrastChange: (Boolean) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onFontScaleChange: (Float) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = "Accessibility") {
                SwitchPreference(
                    icon = Icons.Filled.Contrast,
                    title = stringResource(Res.string.cpp_high_contrast_text),
                    checked = state.highContrast,
                    position = PreferencePosition.TOP,
                    onCheckedChange = onHighContrastChange
                )
                SwitchPreference(
                    icon = Icons.Filled.Speed,
                    title = "Reduce Motion",
                    summary = "Minimize animations throughout the app",
                    checked = state.reduceMotion,
                    position = PreferencePosition.MIDDLE,
                    onCheckedChange = onReduceMotionChange
                )
                PreferenceItem(
                    icon = Icons.Filled.TextFields,
                    title = "Font Scale",
                    summary = "${(state.fontScale * 100).toInt()}%",
                    position = PreferencePosition.BOTTOM,
                    onClick = { }
                )
            }
        }
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
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_widget)) {
                PreferenceItem(
                    icon = Icons.Filled.Palette,
                    title = stringResource(Res.string.cpp_theme),
                    summary = state.widgetTheme.displayName,
                    position = PreferencePosition.SINGLE,
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
    onRpnModeChange: (Boolean) -> Unit,
    onTapeModeChange: (Boolean) -> Unit,
    onShowReleaseNotesChange: (Boolean) -> Unit,
    onShowCalculationLatencyChange: (Boolean) -> Unit,
    onUseBackAsPreviousChange: (Boolean) -> Unit,
    onPlotImagChange: (Boolean) -> Unit,
    onLatexModeChange: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(Res.string.cpp_other)) {
                SwitchPreference(
                    icon = Icons.Filled.Speed,
                    title = stringResource(Res.string.p_calculations_calculate_on_fly_title),
                    checked = state.calculateOnFly,
                    position = PreferencePosition.TOP,
                    onCheckedChange = onCalculateOnFlyChange
                )
                SwitchPreference(
                    icon = Icons.Filled.Calculate,
                    title = stringResource(Res.string.cpp_rpn_mode),
                    summary = stringResource(Res.string.cpp_rpn_mode_summary),
                    checked = state.rpnMode,
                    position = PreferencePosition.MIDDLE,
                    onCheckedChange = onRpnModeChange
                )
                SwitchPreference(
                    icon = Icons.Filled.History,
                    title = stringResource(Res.string.cpp_tape_mode),
                    summary = stringResource(Res.string.cpp_tape_mode_summary),
                    checked = state.tapeMode,
                    position = PreferencePosition.MIDDLE,
                    onCheckedChange = onTapeModeChange
                )
                SwitchPreference(
                    icon = Icons.Filled.History,
                    title = stringResource(Res.string.c_calc_show_release_notes_title),
                    checked = state.showReleaseNotes,
                    position = PreferencePosition.MIDDLE,
                    onCheckedChange = onShowReleaseNotesChange
                )
                SwitchPreference(
                    icon = Icons.Filled.Speed,
                    title = "Show calculation latency",
                    summary = "Display timing diagnostics for calculations",
                    checked = state.showCalculationLatency,
                    position = PreferencePosition.MIDDLE,
                    onCheckedChange = onShowCalculationLatencyChange
                )
                SwitchPreference(
                    icon = Icons.Filled.Keyboard,
                    title = stringResource(Res.string.c_calc_use_back_button_as_prev_title),
                    checked = state.useBackAsPrevious,
                    position = PreferencePosition.MIDDLE,
                    onCheckedChange = onUseBackAsPreviousChange
                )
                SwitchPreference(
                    icon = Icons.Filled.Calculate,
                    title = stringResource(Res.string.cpp_plot_imaginary_part),
                    summary = stringResource(Res.string.cpp_plot_imaginary_part_summary),
                    checked = state.plotImag,
                    position = PreferencePosition.MIDDLE,
                    onCheckedChange = onPlotImagChange
                )
                SwitchPreference(
                    icon = Icons.Filled.Code,
                    title = "LaTeX Output Mode",
                    summary = "Generate LaTeX syntax instead of calculations",
                    checked = state.latexMode,
                    position = PreferencePosition.BOTTOM,
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
        shape = RoundedCornerShape(20.dp),
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
                    imageVector = Icons.Filled.Star,
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InlineChoicePreference(
    icon: ImageVector,
    title: String,
    summary: String?,
    options: List<String>,
    selectedIndex: Int,
    position: PreferencePosition = PreferencePosition.SINGLE,
    onOptionSelected: (Int) -> Unit
) {
    val resolvedSelectedIndex = selectedIndex.coerceIn(0, (options.size - 1).coerceAtLeast(0))
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(preferenceItemShape(position)),
        shape = preferenceItemShape(position),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (!summary.isNullOrBlank()) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    options.forEachIndexed { index, label ->
                        ToggleButton(
                            checked = resolvedSelectedIndex == index,
                            onCheckedChange = { onOptionSelected(index) },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            colors = ToggleButtonDefaults.toggleButtonColors(
                                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = if (options.size >= 4) 12.sp else 13.sp
                                )
                            )
                        }
                    }
                }
            }
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
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun PreferenceItem(
    icon: ImageVector,
    title: String,
    summary: String? = null,
    enabled: Boolean = true,
    position: PreferencePosition = PreferencePosition.SINGLE,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(preferenceItemShape(position))
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f),
        shape = preferenceItemShape(position),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        ListItem(
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
            } else null
        )
    }
}

@Composable
private fun SwitchPreference(
    icon: ImageVector,
    title: String,
    summary: String? = null,
    checked: Boolean,
    position: PreferencePosition = PreferencePosition.SINGLE,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(preferenceItemShape(position))
            .clickable { onCheckedChange(!checked) },
        shape = preferenceItemShape(position),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        ListItem(
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
}

private fun preferenceItemShape(position: PreferencePosition): RoundedCornerShape = when (position) {
    PreferencePosition.SINGLE -> RoundedCornerShape(20.dp)
    PreferencePosition.TOP -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
    PreferencePosition.MIDDLE -> RoundedCornerShape(10.dp)
    PreferencePosition.BOTTOM -> RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
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
            shape = RoundedCornerShape(20.dp),
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
            shape = RoundedCornerShape(20.dp),
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
    val (backgroundColor, accentColor) = if (theme == AppTheme.MATERIAL_YOU) {
        MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.primary
    } else {
        getThemePreviewColors(theme, seedColor)
    }
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
                    Icon(Icons.Filled.Calculate, null, tint = textColor)
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
