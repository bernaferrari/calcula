package org.solovyev.android.calculator.ui.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Widgets
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.ui.*
import org.solovyev.android.calculator.ui.Res
import org.solovyev.android.calculator.ui.c_calc_ad_free_summary
import org.solovyev.android.calculator.ui.c_calc_ad_free_title
import org.solovyev.android.calculator.ui.c_calc_show_release_notes_title
import org.solovyev.android.calculator.ui.c_calc_use_back_button_as_prev_title
import org.solovyev.android.calculator.ui.cpp_about
import org.solovyev.android.calculator.ui.cpp_angles
import org.solovyev.android.calculator.ui.cpp_appearance
import org.solovyev.android.calculator.ui.cpp_examples
import org.solovyev.android.calculator.ui.cpp_format
import org.solovyev.android.calculator.ui.cpp_help
import org.solovyev.android.calculator.ui.cpp_highlight_expressions
import org.solovyev.android.calculator.ui.cpp_highlight_expressions_summary
import org.solovyev.android.calculator.ui.cpp_introduction
import org.solovyev.android.calculator.ui.cpp_language
import org.solovyev.android.calculator.ui.cpp_number_format
import org.solovyev.android.calculator.ui.cpp_other
import org.solovyev.android.calculator.ui.cpp_plot_imaginary_part
import org.solovyev.android.calculator.ui.cpp_plot_imaginary_part_summary
import org.solovyev.android.calculator.ui.cpp_prefs_advanced
import org.solovyev.android.calculator.ui.cpp_prefs_auto_rotate_screen
import org.solovyev.android.calculator.ui.cpp_prefs_basic
import org.solovyev.android.calculator.ui.cpp_prefs_keep_screen_on
import org.solovyev.android.calculator.ui.cpp_prefs_vibrate_on_keypress
import org.solovyev.android.calculator.ui.cpp_precision
import org.solovyev.android.calculator.ui.cpp_radix
import org.solovyev.android.calculator.ui.cpp_report_problem
import org.solovyev.android.calculator.ui.cpp_rpn_mode
import org.solovyev.android.calculator.ui.cpp_rpn_mode_summary
import org.solovyev.android.calculator.ui.cpp_tape_mode
import org.solovyev.android.calculator.ui.cpp_tape_mode_summary
import org.solovyev.android.calculator.ui.cpp_theme
import org.solovyev.android.calculator.ui.cpp_thousands_separator
import org.solovyev.android.calculator.ui.cpp_widget
import org.solovyev.android.calculator.ui.cpp_high_contrast_text
import org.solovyev.android.calculator.ui.p_calculations_calculate_on_fly_title
import org.solovyev.android.calculator.ui.theme.platformDynamicColorScheme

// ============================================================================
// PREMIUM SETTINGS SCREEN - Material Design 3
// ============================================================================
// Key principles:
// - Consistent 8dp grid system
// - Beautiful typography hierarchy
// - Smooth purposeful animations
// - Full accessibility support
// - Visual polish at every level
// ============================================================================

// Enums and data classes (unchanged from original)
enum class CalculatorMode(val displayName: String) {
    MODERN("Modern"),
    ENGINEER("Engineer")
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
    val mode: CalculatorMode = CalculatorMode.MODERN,
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
    val numberFormatExamples: String = "1,234.56\n0.001234",
    val hapticOnRelease: Boolean = true,
    val gestureAutoActivation: Boolean = false,
    val bottomRightEqualsKey: Boolean = false
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

private data class SettingsSearchResultItem(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val summary: String? = null,
    val onClick: () -> Unit
)

// ============================================================================
// CONSTANTS - Premium Spacing & Animation
// ============================================================================

private val SettingsContentPadding = PaddingValues(
    start = 16.dp,
    top = 16.dp,
    end = 16.dp,
    bottom = 32.dp
)

private val SectionSpacing = 24.dp
private val GroupSpacing = 2.dp
private val ItemPaddingHorizontal = 16.dp
private val ItemPaddingVertical = 12.dp

private enum class PreferencePosition {
    SINGLE,
    TOP,
    MIDDLE,
    BOTTOM
}

// Animation durations following Material 3 guidelines
private const val ANIMATION_DURATION_SHORT = 150
private const val ANIMATION_DURATION_MEDIUM = 250
private const val ANIMATION_DURATION_LONG = 350

private fun matchesSearch(query: String, vararg values: String?): Boolean {
    val normalizedQuery = query.trim().lowercase()
    if (normalizedQuery.isEmpty()) return true
    return values.any { value ->
        value?.lowercase()?.contains(normalizedQuery) == true
    }
}

// ============================================================================
// SETTINGS ACTIONS INTERFACE
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
    fun setGestureAutoActivation(enabled: Boolean)
    fun setHapticOnRelease(enabled: Boolean)
    fun setBottomRightEqualsKey(enabled: Boolean)
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
            onBottomRightEqualsKeyChange = actions::setBottomRightEqualsKey,
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
    onAngleUnitChange: (AngleUnit) -> Unit,
    onNumeralBaseChange: (NumeralBase) -> Unit,
    onNavigate: (SettingsDestination) -> Unit,
    onStartWizard: () -> Unit,
    onReportBug: () -> Unit,
    onOpenAbout: () -> Unit,
    onSupportProject: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val normalizedSearchQuery = searchQuery.trim().lowercase()
    val searchItems = buildList {
        add(
            SettingsSearchResultItem(
                id = "number_format",
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                title = stringResource(Res.string.cpp_number_format),
                summary = stringResource(Res.string.cpp_examples),
                onClick = {
                    searchQuery = ""
                    onNavigate(SettingsDestination.NUMBER_FORMAT)
                }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "appearance",
                icon = Icons.Default.Palette,
                title = stringResource(Res.string.cpp_appearance),
                summary = stringResource(Res.string.cpp_theme),
                onClick = {
                    searchQuery = ""
                    onNavigate(SettingsDestination.APPEARANCE)
                }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "accessibility",
                icon = Icons.Default.TextFields,
                title = stringResource(Res.string.cpp_accessibility),
                onClick = {
                    searchQuery = ""
                    onNavigate(SettingsDestination.ACCESSIBILITY)
                }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "widget",
                icon = Icons.Default.Widgets,
                title = stringResource(Res.string.cpp_widget),
                summary = stringResource(Res.string.cpp_theme),
                onClick = {
                    searchQuery = ""
                    onNavigate(SettingsDestination.WIDGET)
                }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "other",
                icon = Icons.Default.Settings,
                title = stringResource(Res.string.cpp_other),
                onClick = {
                    searchQuery = ""
                    onNavigate(SettingsDestination.OTHER)
                }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "introduction",
                icon = Icons.Default.FlashOn,
                title = stringResource(Res.string.cpp_introduction),
                onClick = {
                    searchQuery = ""
                    onStartWizard()
                }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "report_problem",
                icon = Icons.Default.Info,
                title = stringResource(Res.string.cpp_report_problem),
                onClick = {
                    searchQuery = ""
                    onReportBug()
                }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "about",
                icon = Icons.Default.Info,
                title = stringResource(Res.string.cpp_about),
                onClick = {
                    searchQuery = ""
                    onOpenAbout()
                }
            )
        )
        if (!adFreePurchased) {
            add(
                SettingsSearchResultItem(
                    id = "support_project",
                    icon = Icons.Default.Star,
                    title = stringResource(Res.string.c_calc_ad_free_title),
                    summary = stringResource(Res.string.c_calc_ad_free_summary),
                    onClick = {
                        searchQuery = ""
                        onSupportProject()
                    }
                )
            )
        }
    }
    val filteredSearchItems = if (normalizedSearchQuery.isEmpty()) {
        emptyList()
    } else {
        searchItems.filter { item ->
            item.title.lowercase().contains(normalizedSearchQuery) ||
                item.summary.orEmpty().lowercase().contains(normalizedSearchQuery)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        item {
            SettingsSearchBar(
                query = searchQuery,
                placeholder = stringResource(Res.string.cpp_search_settings),
                onQueryChange = { searchQuery = it },
                onSearch = { searchQuery = it }
            )
        }

        if (normalizedSearchQuery.isNotEmpty()) {
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_settings_search_results),
                    icon = Icons.Default.Search
                ) {
                    if (filteredSearchItems.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(preferenceItemShape(PreferencePosition.SINGLE)),
                            shape = preferenceItemShape(PreferencePosition.SINGLE),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Text(
                                text = stringResource(Res.string.cpp_settings_search_no_results, searchQuery),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                            )
                        }
                    } else {
                        filteredSearchItems.forEachIndexed { index, item ->
                            val position = when {
                                filteredSearchItems.size == 1 -> PreferencePosition.SINGLE
                                index == 0 -> PreferencePosition.TOP
                                index == filteredSearchItems.lastIndex -> PreferencePosition.BOTTOM
                                else -> PreferencePosition.MIDDLE
                            }
                            PreferenceItem(
                                icon = item.icon,
                                title = item.title,
                                summary = item.summary,
                                position = position,
                                onClick = item.onClick
                            )
                        }
                    }
                }
            }
        } else {
            // Support Project Card - Premium styling
            if (!adFreePurchased) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        SupportProjectCard(onSupportProject = onSupportProject)
                    }
                }
            }

            // Calculator Section - Logical grouping
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_prefs_basic),
                    icon = Icons.Default.Calculate
                ) {
                    InlineChoicePreference(
                        icon = Icons.Default.LocationOn,
                        title = stringResource(Res.string.cpp_angles),
                        summary = state.angleUnit.displayName,
                        options = listOf("DEG", "RAD", "GRAD", "TURN"),
                        selectedIndex = AngleUnit.entries.indexOf(state.angleUnit).coerceAtLeast(0),
                        position = PreferencePosition.TOP,
                        onOptionSelected = { index ->
                            onAngleUnitChange(AngleUnit.entries[index])
                        }
                    )
                    InlineChoicePreference(
                        icon = Icons.Default.Edit,
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
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_prefs_advanced),
                    icon = Icons.Default.Tune
                ) {
                    PreferenceItem(
                        icon = Icons.Default.Palette,
                        title = stringResource(Res.string.cpp_appearance),
                        summary = stringResource(Res.string.cpp_theme),
                        position = PreferencePosition.TOP,
                        onClick = { onNavigate(SettingsDestination.APPEARANCE) }
                    )
                    PreferenceItem(
                        icon = Icons.Default.Widgets,
                        title = stringResource(Res.string.cpp_widget),
                        summary = stringResource(Res.string.cpp_theme),
                        position = PreferencePosition.MIDDLE,
                        onClick = { onNavigate(SettingsDestination.WIDGET) }
                    )
                    PreferenceItem(
                        icon = Icons.Default.Settings,
                        title = stringResource(Res.string.cpp_other),
                        position = PreferencePosition.BOTTOM,
                        onClick = { onNavigate(SettingsDestination.OTHER) }
                    )
                }
            }

            // Help Section
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_help),
                    icon = Icons.Default.Info
                ) {
                    PreferenceItem(
                        icon = Icons.Default.FlashOn,
                        title = stringResource(Res.string.cpp_introduction),
                        position = PreferencePosition.TOP,
                        onClick = onStartWizard
                    )
                    PreferenceItem(
                        icon = Icons.Default.Info,
                        title = stringResource(Res.string.cpp_report_problem),
                        position = PreferencePosition.MIDDLE,
                        onClick = onReportBug
                    )
                    PreferenceItem(
                        icon = Icons.Default.Info,
                        title = stringResource(Res.string.cpp_about),
                        position = PreferencePosition.BOTTOM,
                        onClick = onOpenAbout
                    )
                }
            }
        }
    }
}

// ============================================================================
// PREMIUM SEARCH BAR
// ============================================================================

@Composable
private fun SettingsSearchBar(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(ANIMATION_DURATION_SHORT),
        label = "search_background"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(ANIMATION_DURATION_SHORT),
        label = "search_border"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(Res.string.cpp_search),
                tint = if (isFocused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )

            BasicTextField(
                value = query,
                onValueChange = {
                    onQueryChange(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch(query) }
                ),
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            )

            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                IconButton(
                    onClick = {
                        onQueryChange("")
                        onSearch("")
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(Res.string.cpp_a11y_clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val formatTitle = stringResource(Res.string.cpp_format)
    val separatorTitle = stringResource(Res.string.cpp_thousands_separator)

    val separatorNames = listOf(
        stringResource(Res.string.cpp_separator_none),
        stringResource(Res.string.cpp_separator_space),
        stringResource(Res.string.cpp_separator_comma),
        stringResource(Res.string.cpp_separator_apostrophe)
    )
    val separatorValues = listOf('\u0000', ' ', ',', '\'')
    val notationOptions = OutputNotation.entries
    val searchItems = listOf(
        SettingsSearchResultItem(
            id = "number_format_notation",
            icon = Icons.Default.Tune,
            title = stringResource(Res.string.cpp_format),
            summary = state.outputNotation.displayName,
            onClick = {
                listDialog = ListDialogState(
                    title = formatTitle,
                    options = notationOptions.map { it.displayName },
                    selectedIndex = notationOptions.indexOf(state.outputNotation),
                    onSelected = { index -> onNotationChange(notationOptions[index]) }
                )
            }
        ),
        SettingsSearchResultItem(
            id = "number_format_precision",
            icon = Icons.Default.Edit,
            title = stringResource(Res.string.cpp_precision),
            summary = state.outputPrecision.toString(),
            onClick = { precisionDialog = true }
        ),
        SettingsSearchResultItem(
            id = "number_format_separator",
            icon = Icons.Default.TextFields,
            title = stringResource(Res.string.cpp_thousands_separator),
            summary = separatorSummary(state.outputSeparator.symbol),
            onClick = {
                val selectedIndex = separatorValues.indexOf(state.outputSeparator.symbol).coerceAtLeast(0)
                listDialog = ListDialogState(
                    title = separatorTitle,
                    options = separatorNames,
                    selectedIndex = selectedIndex,
                    onSelected = { index -> onSeparatorChange(separatorValues[index]) }
                )
            }
        )
    )
    val filteredSearchItems = searchItems.filter {
        matchesSearch(searchQuery, it.title, it.summary)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        if (searchQuery.isNotBlank()) {
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_settings_search_results),
                    icon = Icons.Default.Search
                ) {
                    if (filteredSearchItems.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(preferenceItemShape(PreferencePosition.SINGLE)),
                            shape = preferenceItemShape(PreferencePosition.SINGLE),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Text(
                                text = stringResource(Res.string.cpp_settings_search_no_results, searchQuery),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                            )
                        }
                    } else {
                        filteredSearchItems.forEachIndexed { index, item ->
                            val position = when {
                                filteredSearchItems.size == 1 -> PreferencePosition.SINGLE
                                index == 0 -> PreferencePosition.TOP
                                index == filteredSearchItems.lastIndex -> PreferencePosition.BOTTOM
                                else -> PreferencePosition.MIDDLE
                            }
                            PreferenceItem(
                                icon = item.icon,
                                title = item.title,
                                summary = item.summary,
                                position = position,
                                onClick = item.onClick
                            )
                        }
                    }
                }
            }
        } else {
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_number_format),
                    icon = Icons.Default.Tune
                ) {
                    PreferenceItem(
                        icon = Icons.Default.Tune,
                        title = stringResource(Res.string.cpp_format),
                        summary = state.outputNotation.displayName,
                        position = PreferencePosition.TOP,
                        onClick = {
                            listDialog = ListDialogState(
                                title = formatTitle,
                                options = notationOptions.map { it.displayName },
                                selectedIndex = notationOptions.indexOf(state.outputNotation),
                                onSelected = { index -> onNotationChange(notationOptions[index]) }
                            )
                        }
                    )
                    PreferenceItem(
                        icon = Icons.Default.Edit,
                        title = stringResource(Res.string.cpp_precision),
                        summary = state.outputPrecision.toString(),
                        position = PreferencePosition.MIDDLE,
                        onClick = { precisionDialog = true }
                    )
                    PreferenceItem(
                        icon = Icons.Default.TextFields,
                        title = stringResource(Res.string.cpp_thousands_separator),
                        summary = separatorSummary(state.outputSeparator.symbol),
                        position = PreferencePosition.BOTTOM,
                        onClick = {
                            val selectedIndex = separatorValues.indexOf(state.outputSeparator.symbol).coerceAtLeast(0)
                            listDialog = ListDialogState(
                                title = separatorTitle,
                                options = separatorNames,
                                selectedIndex = selectedIndex,
                                onSelected = { index -> onSeparatorChange(separatorValues[index]) }
                            )
                        }
                    )
                }
            }

            // Examples Card - Premium preview
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_examples),
                    icon = Icons.Default.Calculate
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = preferenceItemShape(PreferencePosition.SINGLE),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.numberFormatExamples.lines().forEach { line ->
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
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
// APPEARANCE SCREEN - Premium Theme Preview
// ============================================================================

@Composable
@OptIn(ExperimentalAnimationApi::class)
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
    onBottomRightEqualsKeyChange: (Boolean) -> Unit,
    actions: SettingsActions
) {
    var listDialog by remember { mutableStateOf<ListDialogState?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val languageTitle = stringResource(Res.string.cpp_language)
    val applyAppearanceMode: (AppearanceMode) -> Unit = { mode ->
        onAppearanceModeChange(mode)
        onThemeChange(
            when (mode) {
                AppearanceMode.SYSTEM -> AppTheme.MATERIAL_YOU
                AppearanceMode.LIGHT -> AppTheme.MATERIAL_LIGHT
                AppearanceMode.DARK -> AppTheme.MATERIAL_DARK
            }
        )
    }
    val languageSummary = languages.firstOrNull { it.code == state.languageCode }?.displayName
        ?: stringResource(Res.string.cpp_theme_system)
    val searchItems = buildList {
        add(
            SettingsSearchResultItem(
                id = "appearance_mode",
                icon = Icons.Default.Palette,
                title = stringResource(Res.string.cpp_theme),
                summary = state.appearanceMode.displayName,
                onClick = {
                    val currentIndex = AppearanceMode.entries.indexOf(state.appearanceMode).coerceAtLeast(0)
                    val nextMode = AppearanceMode.entries[(currentIndex + 1) % AppearanceMode.entries.size]
                    applyAppearanceMode(nextMode)
                }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "appearance_dynamic_colors",
                icon = Icons.Default.BrightnessAuto,
                title = stringResource(Res.string.cpp_auto),
                summary = if (state.dynamicColorEnabled) {
                    stringResource(Res.string.cpp_enable)
                } else {
                    stringResource(Res.string.cpp_theme)
                },
                onClick = { onDynamicColorChange(!state.dynamicColorEnabled) }
            )
        )
        if (languages.isNotEmpty()) {
            add(
                SettingsSearchResultItem(
                    id = "appearance_language",
                    icon = Icons.Default.Language,
                    title = languageTitle,
                    summary = languageSummary,
                    onClick = {
                        listDialog = ListDialogState(
                            title = languageTitle,
                            options = languages.map { it.displayName },
                            selectedIndex = languages.indexOfFirst { it.code == state.languageCode }.coerceAtLeast(0),
                            onSelected = { index -> onLanguageChange(languages[index].code) }
                        )
                    }
                )
            )
        }
        add(
            SettingsSearchResultItem(
                id = "appearance_vibrate",
                icon = Icons.Default.Vibration,
                title = stringResource(Res.string.cpp_prefs_vibrate_on_keypress),
                onClick = { onVibrateChange(!state.vibrateOnKeypress) }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "appearance_haptic_release",
                icon = Icons.Default.Vibration,
                title = stringResource(Res.string.cpp_settings_haptic_release),
                summary = stringResource(Res.string.cpp_settings_haptic_release_summary),
                onClick = { actions.setHapticOnRelease(!state.hapticOnRelease) }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "appearance_slide_to_activate",
                icon = Icons.Default.Speed,
                title = stringResource(Res.string.cpp_settings_slide_to_activate),
                summary = stringResource(Res.string.cpp_settings_slide_to_activate_summary),
                onClick = { actions.setGestureAutoActivation(!state.gestureAutoActivation) }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "appearance_classic_equals",
                icon = Icons.Default.Keyboard,
                title = stringResource(Res.string.cpp_settings_classic_equals_key),
                summary = stringResource(Res.string.cpp_settings_classic_equals_key_summary),
                onClick = { onBottomRightEqualsKeyChange(!state.bottomRightEqualsKey) }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "appearance_high_contrast",
                icon = Icons.Default.Contrast,
                title = stringResource(Res.string.cpp_high_contrast_text),
                onClick = { onHighContrastChange(!state.highContrast) }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "appearance_highlight",
                icon = Icons.Default.Keyboard,
                title = stringResource(Res.string.cpp_highlight_expressions),
                summary = stringResource(Res.string.cpp_highlight_expressions_summary),
                onClick = { onHighlightExpressionsChange(!state.highlightExpressions) }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "appearance_rotate",
                icon = Icons.Default.ScreenRotation,
                title = stringResource(Res.string.cpp_prefs_auto_rotate_screen),
                onClick = { onRotateChange(!state.rotateScreen) }
            )
        )
        add(
            SettingsSearchResultItem(
                id = "appearance_keep_screen_on",
                icon = Icons.Default.Fullscreen,
                title = stringResource(Res.string.cpp_prefs_keep_screen_on),
                onClick = { onKeepScreenOnChange(!state.keepScreenOn) }
            )
        )
    }
    val filteredSearchItems = searchItems.filter {
        matchesSearch(searchQuery, it.title, it.summary)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        if (searchQuery.isNotBlank()) {
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_settings_search_results),
                    icon = Icons.Default.Search
                ) {
                    if (filteredSearchItems.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(preferenceItemShape(PreferencePosition.SINGLE)),
                            shape = preferenceItemShape(PreferencePosition.SINGLE),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Text(
                                text = stringResource(Res.string.cpp_settings_search_no_results, searchQuery),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                            )
                        }
                    } else {
                        filteredSearchItems.forEachIndexed { index, item ->
                            val position = when {
                                filteredSearchItems.size == 1 -> PreferencePosition.SINGLE
                                index == 0 -> PreferencePosition.TOP
                                index == filteredSearchItems.lastIndex -> PreferencePosition.BOTTOM
                                else -> PreferencePosition.MIDDLE
                            }
                            PreferenceItem(
                                icon = item.icon,
                                title = item.title,
                                summary = item.summary,
                                position = position,
                                onClick = item.onClick
                            )
                        }
                    }
                }
            }
        } else {
            // Theme Preview - Hero section
            item {
                AnimatedContent(
                    targetState = state.theme to state.themeSeedColor,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(ANIMATION_DURATION_MEDIUM)) with
                            fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                    },
                    label = "theme_preview"
                ) { (theme, seedColor) ->
                    PremiumThemePreview(
                        theme = theme,
                        seedColor = seedColor
                    )
                }
            }

            // Theme Selection
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_appearance),
                    icon = Icons.Default.Palette
                ) {
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
                        onModeSelected = applyAppearanceMode
                    )

                    if (languages.isNotEmpty()) {
                        PreferenceItem(
                            icon = Icons.Default.Language,
                            title = languageTitle,
                            summary = languageSummary,
                            position = PreferencePosition.BOTTOM,
                            onClick = {
                                listDialog = ListDialogState(
                                    title = languageTitle,
                                    options = languages.map { it.displayName },
                                    selectedIndex = languages.indexOfFirst { it.code == state.languageCode }.coerceAtLeast(0),
                                    onSelected = { index -> onLanguageChange(languages[index].code) }
                                )
                            }
                        )
                    }
                }
            }

            // Display Settings
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_prefs_advanced),
                    icon = Icons.Default.Settings
                ) {
                    SwitchPreference(
                        icon = Icons.Default.Vibration,
                        title = stringResource(Res.string.cpp_prefs_vibrate_on_keypress),
                        checked = state.vibrateOnKeypress,
                        position = PreferencePosition.TOP,
                        onCheckedChange = onVibrateChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.Vibration,
                        title = stringResource(Res.string.cpp_settings_haptic_release),
                        summary = stringResource(Res.string.cpp_settings_haptic_release_summary),
                        checked = state.hapticOnRelease,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = actions::setHapticOnRelease
                    )
                    SwitchPreference(
                        icon = Icons.Default.Speed,
                        title = stringResource(Res.string.cpp_settings_slide_to_activate),
                        summary = stringResource(Res.string.cpp_settings_slide_to_activate_summary),
                        checked = state.gestureAutoActivation,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = actions::setGestureAutoActivation
                    )
                    SwitchPreference(
                        icon = Icons.Default.Keyboard,
                        title = stringResource(Res.string.cpp_settings_classic_equals_key),
                        summary = stringResource(Res.string.cpp_settings_classic_equals_key_summary),
                        checked = state.bottomRightEqualsKey,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = onBottomRightEqualsKeyChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.Contrast,
                        title = stringResource(Res.string.cpp_high_contrast_text),
                        checked = state.highContrast,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = onHighContrastChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.Keyboard,
                        title = stringResource(Res.string.cpp_highlight_expressions),
                        summary = stringResource(Res.string.cpp_highlight_expressions_summary),
                        checked = state.highlightExpressions,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = onHighlightExpressionsChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.ScreenRotation,
                        title = stringResource(Res.string.cpp_prefs_auto_rotate_screen),
                        checked = state.rotateScreen,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = onRotateChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.Fullscreen,
                        title = stringResource(Res.string.cpp_prefs_keep_screen_on),
                        checked = state.keepScreenOn,
                        position = PreferencePosition.BOTTOM,
                        onCheckedChange = onKeepScreenOnChange
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
}

// ============================================================================
// PREMIUM THEME PREVIEW
// ============================================================================

@Composable
private fun PremiumThemePreview(
    theme: AppTheme,
    seedColor: Int
) {
    val (backgroundColor, accentColor) = if (theme == AppTheme.MATERIAL_YOU) {
        MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.primary
    } else {
        getThemePreviewColors(theme, seedColor)
    }

    val isLight = theme == AppTheme.MATERIAL_LIGHT || theme == AppTheme.MATERIAL_YOU
    val textColor = if (isLight) Color(0xFF1A1A1F) else Color(0xFFFFFFFF)
    val secondaryTextColor = if (isLight) Color(0xFF6B6B7B) else Color(0xFFB0B0B5)
    val buttonColor = if (isLight) Color(0xFFE8E8EC) else Color(0xFF2C2C2E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Expression preview
            Text(
                text = "12 * 8 + 5",
                style = MaterialTheme.typography.titleMedium,
                color = secondaryTextColor,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Result preview
            Text(
                text = "101",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = textColor,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Button row preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AC Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "AC",
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Function buttons
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(buttonColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val label = when (index) {
                            0 -> "/"
                            1 -> "*"
                            else -> "-"
                        }
                        Text(
                            label,
                            color = accentColor,
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp
                        )
                    }
                }

                // Equals button
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(48.dp)
                        .background(accentColor, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "=",
                        color = if (isLight) Color.White else Color.Black,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val fontScaleTitle = stringResource(Res.string.cpp_accessibility_font_scale)
    var fontScaleDialog by remember { mutableStateOf<ListDialogState?>(null) }
    val fontScaleOptions = listOf(0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.4f)
    val searchItems = listOf(
        SettingsSearchResultItem(
            id = "accessibility_high_contrast",
            icon = Icons.Default.Contrast,
            title = stringResource(Res.string.cpp_high_contrast_text),
            summary = if (state.highContrast) {
                stringResource(Res.string.cpp_enable)
            } else {
                stringResource(Res.string.cpp_disable)
            },
            onClick = { onHighContrastChange(!state.highContrast) }
        ),
        SettingsSearchResultItem(
            id = "accessibility_reduce_motion",
            icon = Icons.Default.Speed,
            title = stringResource(Res.string.cpp_accessibility_reduce_motion),
            summary = if (state.reduceMotion) {
                stringResource(Res.string.cpp_enable)
            } else {
                stringResource(Res.string.cpp_disable)
            },
            onClick = { onReduceMotionChange(!state.reduceMotion) }
        ),
        SettingsSearchResultItem(
            id = "accessibility_font_scale",
            icon = Icons.Default.TextFields,
            title = fontScaleTitle,
            summary = "${(state.fontScale * 100).toInt()}%",
            onClick = {
                fontScaleDialog = ListDialogState(
                    title = fontScaleTitle,
                    options = fontScaleOptions.map { "${(it * 100).toInt()}%" },
                    selectedIndex = fontScaleOptions.indexOfFirst { kotlin.math.abs(it - state.fontScale) < 0.01f }
                        .coerceAtLeast(0),
                    onSelected = { index -> onFontScaleChange(fontScaleOptions[index]) }
                )
            }
        )
    )
    val filteredSearchItems = searchItems.filter {
        matchesSearch(searchQuery, it.title, it.summary)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        if (searchQuery.isNotBlank()) {
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_settings_search_results),
                    icon = Icons.Default.Search
                ) {
                    if (filteredSearchItems.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(preferenceItemShape(PreferencePosition.SINGLE)),
                            shape = preferenceItemShape(PreferencePosition.SINGLE),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Text(
                                text = stringResource(Res.string.cpp_settings_search_no_results, searchQuery),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                            )
                        }
                    } else {
                        filteredSearchItems.forEachIndexed { index, item ->
                            val position = when {
                                filteredSearchItems.size == 1 -> PreferencePosition.SINGLE
                                index == 0 -> PreferencePosition.TOP
                                index == filteredSearchItems.lastIndex -> PreferencePosition.BOTTOM
                                else -> PreferencePosition.MIDDLE
                            }
                            PreferenceItem(
                                icon = item.icon,
                                title = item.title,
                                summary = item.summary,
                                position = position,
                                onClick = item.onClick
                            )
                        }
                    }
                }
            }
        } else {
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_accessibility),
                    icon = Icons.Default.Contrast
                ) {
                    SwitchPreference(
                        icon = Icons.Default.Contrast,
                        title = stringResource(Res.string.cpp_high_contrast_text),
                        checked = state.highContrast,
                        position = PreferencePosition.TOP,
                        onCheckedChange = onHighContrastChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.Speed,
                        title = stringResource(Res.string.cpp_accessibility_reduce_motion),
                        summary = stringResource(Res.string.cpp_accessibility_reduce_motion_summary),
                        checked = state.reduceMotion,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = onReduceMotionChange
                    )
                    FontScalePreference(
                        currentScale = state.fontScale,
                        position = PreferencePosition.BOTTOM,
                        onScaleChange = onFontScaleChange
                    )
                }
            }

            // Preview card
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_settings_preview),
                    icon = Icons.Default.TextFields
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = preferenceItemShape(PreferencePosition.SINGLE),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    Res.string.cpp_settings_sample_text_at,
                                    (state.fontScale * 100).toInt()
                                ),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp * state.fontScale
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(Res.string.cpp_settings_sample_text_subtitle),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp * state.fontScale
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    fontScaleDialog?.let { dialog ->
        SelectionDialog(
            title = dialog.title,
            options = dialog.options,
            selectedIndex = dialog.selectedIndex,
            onDismiss = { fontScaleDialog = null },
            onSelected = {
                dialog.onSelected(it)
                fontScaleDialog = null
            }
        )
    }
}

@Composable
private fun FontScalePreference(
    currentScale: Float,
    position: PreferencePosition,
    onScaleChange: (Float) -> Unit
) {
    var sliderValue by remember(currentScale) { mutableFloatStateOf(currentScale) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(preferenceItemShape(position)),
        shape = preferenceItemShape(position),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(Res.string.cpp_accessibility_font_scale),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${(sliderValue * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onScaleChange(sliderValue) },
                valueRange = 0.8f..1.4f,
                steps = 5,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.cpp_settings_font_small),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(Res.string.cpp_settings_font_large),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val simpleThemes = SimpleTheme.entries
    val themeTitle = stringResource(Res.string.cpp_theme)
    val searchItems = listOf(
        SettingsSearchResultItem(
            id = "widget_theme",
            icon = Icons.Default.Palette,
            title = themeTitle,
            summary = state.widgetTheme.displayName,
            onClick = {
                listDialog = ListDialogState(
                    title = themeTitle,
                    options = simpleThemes.map { it.displayName },
                    selectedIndex = simpleThemes.indexOf(state.widgetTheme).coerceAtLeast(0),
                    onSelected = { index -> onThemeChange(simpleThemes[index]) }
                )
            }
        )
    )
    val filteredSearchItems = searchItems.filter {
        matchesSearch(searchQuery, it.title, it.summary)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        if (searchQuery.isNotBlank()) {
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_settings_search_results),
                    icon = Icons.Default.Search
                ) {
                    if (filteredSearchItems.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(preferenceItemShape(PreferencePosition.SINGLE)),
                            shape = preferenceItemShape(PreferencePosition.SINGLE),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Text(
                                text = stringResource(Res.string.cpp_settings_search_no_results, searchQuery),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                            )
                        }
                    } else {
                        filteredSearchItems.forEachIndexed { index, item ->
                            val position = when {
                                filteredSearchItems.size == 1 -> PreferencePosition.SINGLE
                                index == 0 -> PreferencePosition.TOP
                                index == filteredSearchItems.lastIndex -> PreferencePosition.BOTTOM
                                else -> PreferencePosition.MIDDLE
                            }
                            PreferenceItem(
                                icon = item.icon,
                                title = item.title,
                                summary = item.summary,
                                position = position,
                                onClick = item.onClick
                            )
                        }
                    }
                }
            }
        } else {
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_widget),
                    icon = Icons.Default.Widgets
                ) {
                    PreferenceItem(
                        icon = Icons.Default.Palette,
                        title = themeTitle,
                        summary = state.widgetTheme.displayName,
                        position = PreferencePosition.SINGLE,
                        onClick = {
                            listDialog = ListDialogState(
                                title = themeTitle,
                                options = simpleThemes.map { it.displayName },
                                selectedIndex = simpleThemes.indexOf(state.widgetTheme).coerceAtLeast(0),
                                onSelected = { index -> onThemeChange(simpleThemes[index]) }
                            )
                        }
                    )
                }
            }

            // Widget preview
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_settings_preview),
                    icon = Icons.Default.Widgets
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = when (state.widgetTheme) {
                            SimpleTheme.DEFAULT -> MaterialTheme.colorScheme.surface
                            SimpleTheme.MATERIAL_DARK -> Color(0xFF1C1B1F)
                            SimpleTheme.MATERIAL_LIGHT -> Color(0xFFFFFFFF)
                            SimpleTheme.METRO_BLUE -> Color(0xFF111111)
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "2 + 2",
                                style = MaterialTheme.typography.bodyMedium,
                                color = when (state.widgetTheme) {
                                    SimpleTheme.MATERIAL_LIGHT -> Color(0xFF6B6B7B)
                                    else -> Color(0xFFB0B0B5)
                                }
                            )
                            Text(
                                text = "4",
                                style = MaterialTheme.typography.headlineMedium,
                                color = when (state.widgetTheme) {
                                    SimpleTheme.MATERIAL_LIGHT -> Color(0xFF1A1A1F)
                                    else -> Color(0xFFFFFFFF)
                                }
                            )
                        }
                    }
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
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchItems = listOf(
        SettingsSearchResultItem(
            id = "other_calculate_on_fly",
            icon = Icons.Default.Speed,
            title = stringResource(Res.string.p_calculations_calculate_on_fly_title),
            onClick = { onCalculateOnFlyChange(!state.calculateOnFly) }
        ),
        SettingsSearchResultItem(
            id = "other_rpn_mode",
            icon = Icons.Default.Calculate,
            title = stringResource(Res.string.cpp_rpn_mode),
            summary = stringResource(Res.string.cpp_rpn_mode_summary),
            onClick = { onRpnModeChange(!state.rpnMode) }
        ),
        SettingsSearchResultItem(
            id = "other_tape_mode",
            icon = Icons.Default.History,
            title = stringResource(Res.string.cpp_tape_mode),
            summary = stringResource(Res.string.cpp_tape_mode_summary),
            onClick = { onTapeModeChange(!state.tapeMode) }
        ),
        SettingsSearchResultItem(
            id = "other_release_notes",
            icon = Icons.Default.History,
            title = stringResource(Res.string.c_calc_show_release_notes_title),
            onClick = { onShowReleaseNotesChange(!state.showReleaseNotes) }
        ),
        SettingsSearchResultItem(
            id = "other_latency",
            icon = Icons.Default.Speed,
            title = stringResource(Res.string.cpp_settings_show_calculation_latency),
            summary = stringResource(Res.string.cpp_settings_show_calculation_latency_summary),
            onClick = { onShowCalculationLatencyChange(!state.showCalculationLatency) }
        ),
        SettingsSearchResultItem(
            id = "other_back_as_prev",
            icon = Icons.Default.Keyboard,
            title = stringResource(Res.string.c_calc_use_back_button_as_prev_title),
            onClick = { onUseBackAsPreviousChange(!state.useBackAsPrevious) }
        ),
        SettingsSearchResultItem(
            id = "other_plot_imag",
            icon = Icons.Default.Calculate,
            title = stringResource(Res.string.cpp_plot_imaginary_part),
            summary = stringResource(Res.string.cpp_plot_imaginary_part_summary),
            onClick = { onPlotImagChange(!state.plotImag) }
        ),
        SettingsSearchResultItem(
            id = "other_latex_mode",
            icon = Icons.Default.Code,
            title = stringResource(Res.string.cpp_settings_latex_output_mode),
            summary = stringResource(Res.string.cpp_settings_latex_output_mode_summary),
            onClick = { onLatexModeChange(!state.latexMode) }
        )
    )
    val filteredSearchItems = searchItems.filter {
        matchesSearch(searchQuery, it.title, it.summary)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = SettingsContentPadding,
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        if (searchQuery.isNotBlank()) {
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_settings_search_results),
                    icon = Icons.Default.Search
                ) {
                    if (filteredSearchItems.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(preferenceItemShape(PreferencePosition.SINGLE)),
                            shape = preferenceItemShape(PreferencePosition.SINGLE),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Text(
                                text = stringResource(Res.string.cpp_settings_search_no_results, searchQuery),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                            )
                        }
                    } else {
                        filteredSearchItems.forEachIndexed { index, item ->
                            val position = when {
                                filteredSearchItems.size == 1 -> PreferencePosition.SINGLE
                                index == 0 -> PreferencePosition.TOP
                                index == filteredSearchItems.lastIndex -> PreferencePosition.BOTTOM
                                else -> PreferencePosition.MIDDLE
                            }
                            PreferenceItem(
                                icon = item.icon,
                                title = item.title,
                                summary = item.summary,
                                position = position,
                                onClick = item.onClick
                            )
                        }
                    }
                }
            }
        } else {
            item {
                PreferenceGroup(
                    title = stringResource(Res.string.cpp_other),
                    icon = Icons.Default.Settings
                ) {
                    SwitchPreference(
                        icon = Icons.Default.Speed,
                        title = stringResource(Res.string.p_calculations_calculate_on_fly_title),
                        checked = state.calculateOnFly,
                        position = PreferencePosition.TOP,
                        onCheckedChange = onCalculateOnFlyChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.Calculate,
                        title = stringResource(Res.string.cpp_rpn_mode),
                        summary = stringResource(Res.string.cpp_rpn_mode_summary),
                        checked = state.rpnMode,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = onRpnModeChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.History,
                        title = stringResource(Res.string.cpp_tape_mode),
                        summary = stringResource(Res.string.cpp_tape_mode_summary),
                        checked = state.tapeMode,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = onTapeModeChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.History,
                        title = stringResource(Res.string.c_calc_show_release_notes_title),
                        checked = state.showReleaseNotes,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = onShowReleaseNotesChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.Speed,
                        title = stringResource(Res.string.cpp_settings_show_calculation_latency),
                        summary = stringResource(Res.string.cpp_settings_show_calculation_latency_summary),
                        checked = state.showCalculationLatency,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = onShowCalculationLatencyChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.Keyboard,
                        title = stringResource(Res.string.c_calc_use_back_button_as_prev_title),
                        checked = state.useBackAsPrevious,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = onUseBackAsPreviousChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.Calculate,
                        title = stringResource(Res.string.cpp_plot_imaginary_part),
                        summary = stringResource(Res.string.cpp_plot_imaginary_part_summary),
                        checked = state.plotImag,
                        position = PreferencePosition.MIDDLE,
                        onCheckedChange = onPlotImagChange
                    )
                    SwitchPreference(
                        icon = Icons.Default.Code,
                        title = stringResource(Res.string.cpp_settings_latex_output_mode),
                        summary = stringResource(Res.string.cpp_settings_latex_output_mode_summary),
                        checked = state.latexMode,
                        position = PreferencePosition.BOTTOM,
                        onCheckedChange = onLatexModeChange
                    )
                }
            }
        }
    }
}

// ============================================================================
// PREMIUM COMPONENTS
// ============================================================================

@Composable
private fun SupportProjectCard(onSupportProject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onSupportProject
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with gradient effect
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.c_calc_ad_free_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.c_calc_ad_free_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
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
        Column(
            modifier = Modifier.padding(
                horizontal = ItemPaddingHorizontal,
                vertical = ItemPaddingVertical
            )
        ) {
            // Header with icon
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

            // Connected expressive toggle group
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                options.forEachIndexed { index, label ->
                    val isSelected = resolvedSelectedIndex == index
                    ToggleButton(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            if (checked) onOptionSelected(index)
                        },
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
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { role = Role.RadioButton }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceGroup(
    title: String,
    icon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Column {
        // Premium section header
        Row(
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(GroupSpacing)) {
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
    highlighted: Boolean = false,
    position: PreferencePosition = PreferencePosition.SINGLE,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = preferenceItemShape(position)
    val visualState = rememberPreferenceVisualState(highlighted)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .alpha(if (enabled) 1f else 0.5f)
            .semantics {
                role = Role.Button
                contentDescription = title
            },
        shape = shape,
        color = visualState.containerColor
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = visualState.iconContainerColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = visualState.iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = visualState.titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = if (summary != null) {
                {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = visualState.summaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
    val shape = preferenceItemShape(position)
    val visualState = rememberPreferenceVisualState(highlighted = checked)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onCheckedChange(!checked) }
            .semantics {
                role = Role.Switch
                contentDescription = title
            },
        shape = shape,
        color = visualState.containerColor
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = visualState.iconContainerColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = visualState.iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = visualState.titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = if (summary != null) {
                {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = visualState.summaryColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else null,
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppearanceModeSegmentedPreference(
    selectedMode: AppearanceMode,
    position: PreferencePosition,
    onModeSelected: (AppearanceMode) -> Unit
) {
    val options = listOf(
        Triple(AppearanceMode.SYSTEM, stringResource(Res.string.cpp_theme_system), Icons.Default.BrightnessAuto),
        Triple(AppearanceMode.LIGHT, stringResource(Res.string.cpp_theme_light), Icons.Default.LightMode),
        Triple(AppearanceMode.DARK, stringResource(Res.string.cpp_theme_dark), Icons.Default.DarkMode)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = preferenceItemShape(position),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = ItemPaddingHorizontal,
                vertical = ItemPaddingVertical
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                options.forEachIndexed { index, (mode, label, icon) ->
                    val isSelected = selectedMode == mode
                    ToggleButton(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            if (checked) onModeSelected(mode)
                        },
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
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .semantics { role = Role.RadioButton }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
        Column(
            modifier = Modifier.padding(
                horizontal = ItemPaddingHorizontal,
                vertical = ItemPaddingVertical
            )
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dynamic color option
                item {
                    val dynamicSelected = dynamicColorEnabled
                    val animatedCornerRadius by animateColorAsState(
                        targetValue = if (dynamicSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        },
                        label = "dynamic_border"
                    )

                    Surface(
                        modifier = Modifier
                            .size(width = 68.dp, height = 48.dp)
                            .clip(RoundedCornerShape(if (dynamicSelected) 18.dp else 14.dp))
                            .clickable(
                                enabled = dynamicColorAvailable,
                                role = Role.Button
                            ) {
                                onDynamicColorChange(true)
                            }
                            .then(
                                if (dynamicSelected) {
                                    Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(18.dp)
                                    )
                                } else Modifier
                            ),
                        color = if (dynamicColorAvailable) {
                            MaterialTheme.colorScheme.primary.copy(alpha = if (dynamicSelected) 1f else 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        shape = RoundedCornerShape(if (dynamicSelected) 18.dp else 14.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(Res.string.cpp_auto),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (dynamicSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (dynamicColorAvailable) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                // Color swatches
                items(colors) { colorInt ->
                    val color = Color(colorInt)
                    val isSelected = !dynamicColorEnabled && currentSeedColor == colorInt.toInt()

                    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "color_scale"
                    )

                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(if (isSelected) 16.dp else 12.dp))
                            .clickable(role = Role.Button) {
                                onDynamicColorChange(false)
                                onSeedColorChange(colorInt.toInt())
                            }
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        RoundedCornerShape(16.dp)
                                    )
                                } else Modifier
                            ),
                        color = color,
                        shape = RoundedCornerShape(if (isSelected) 16.dp else 12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!dynamicColorAvailable) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.cpp_settings_dynamic_colors_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// PREMIUM DIALOGS WITH ANIMATIONS
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
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialScale = 0.85f
            ) + fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT)),
            exit = scaleOut(
                animationSpec = tween(ANIMATION_DURATION_SHORT),
                targetScale = 0.95f
            ) + fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 20.dp)
                ) {
                    // Dialog header
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Options list
                    options.forEachIndexed { index, option ->
                        val isSelected = index == selectedIndex
                        val backgroundColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            } else {
                                Color.Transparent
                            },
                            animationSpec = tween(ANIMATION_DURATION_SHORT),
                            label = "option_background"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(backgroundColor)
                                .selectable(
                                    selected = isSelected,
                                    onClick = { onSelected(index) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cancel button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                stringResource(Res.string.cpp_cancel),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PrecisionDialog(
    value: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(value.toFloat()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialScale = 0.85f
            ) + fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT)),
            exit = scaleOut(
                animationSpec = tween(ANIMATION_DURATION_SHORT),
                targetScale = 0.95f
            ) + fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.cpp_precision),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Animated value display
                    AnimatedContent(
                        targetState = sliderValue.toInt(),
                        transitionSpec = {
                            slideInVertically { it } + fadeIn() with
                            slideOutVertically { -it } + fadeOut()
                        },
                        label = "precision_value"
                    ) { value ->
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 0f..16f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "16",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                stringResource(Res.string.cpp_cancel),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = { onConfirm(sliderValue.toInt()) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                stringResource(Res.string.cpp_ok),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// HELPERS
// ============================================================================

private data class PreferenceVisualState(
    val containerColor: Color,
    val iconContainerColor: Color,
    val iconTint: Color,
    val titleColor: Color,
    val summaryColor: Color
)

@Composable
private fun rememberPreferenceVisualState(highlighted: Boolean): PreferenceVisualState {
    val containerColor by animateColorAsState(
        targetValue = if (highlighted) {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(ANIMATION_DURATION_SHORT),
        label = "preference_container"
    )
    val iconContainerColor by animateColorAsState(
        targetValue = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        },
        animationSpec = tween(ANIMATION_DURATION_SHORT),
        label = "preference_icon_container"
    )
    val iconTint by animateColorAsState(
        targetValue = if (highlighted) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(ANIMATION_DURATION_SHORT),
        label = "preference_icon_tint"
    )
    val titleColor by animateColorAsState(
        targetValue = if (highlighted) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(ANIMATION_DURATION_SHORT),
        label = "preference_title"
    )
    val summaryColor by animateColorAsState(
        targetValue = if (highlighted) {
            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(ANIMATION_DURATION_SHORT),
        label = "preference_summary"
    )

    return PreferenceVisualState(
        containerColor = containerColor,
        iconContainerColor = iconContainerColor,
        iconTint = iconTint,
        titleColor = titleColor,
        summaryColor = summaryColor
    )
}

private fun preferenceItemShape(position: PreferencePosition): RoundedCornerShape = when (position) {
    PreferencePosition.SINGLE -> RoundedCornerShape(16.dp)
    PreferencePosition.TOP -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
    PreferencePosition.MIDDLE -> RoundedCornerShape(8.dp)
    PreferencePosition.BOTTOM -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
}

@Composable
private fun separatorSummary(separator: Char): String {
    return when (separator) {
        '\u0000' -> stringResource(Res.string.cpp_separator_none)
        ' ' -> stringResource(Res.string.cpp_separator_space)
        ',' -> stringResource(Res.string.cpp_separator_comma_with_symbol)
        '\'' -> stringResource(Res.string.cpp_separator_apostrophe_with_symbol)
        else -> separator.toString()
    }
}

private fun getThemePreviewColors(theme: AppTheme, seedColor: Int): Pair<Color, Color> {
    return when (theme) {
        AppTheme.MATERIAL_YOU -> Pair(Color(0xFFF2F0F4), Color(seedColor))
        AppTheme.MATERIAL_DARK -> Pair(Color(0xFF1C1B1F), Color(0xFFD0BCFF))
        AppTheme.MATERIAL_BLACK -> Pair(Color(0xFF000000), Color(0xFFBB86FC))
        AppTheme.MATERIAL_LIGHT -> Pair(Color(0xFFFFFFFF), Color(0xFF6750A4))
        AppTheme.METRO_BLUE -> Pair(Color(0xFF111111), Color(0xFF00ADEF))
        AppTheme.METRO_GREEN -> Pair(Color(0xFF111111), Color(0xFF00C853))
        AppTheme.METRO_PURPLE -> Pair(Color(0xFF111111), Color(0xFFAA00FF))
    }
}

// ============================================================================
// SEARCH RESULTS HIGHLIGHTING
// ============================================================================

@Composable
private fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    highlightColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    if (query.isEmpty()) {
        Text(
            text = text,
            style = style,
            modifier = modifier
        )
        return
    }

    val annotatedString = buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var startIndex = 0

        while (true) {
            val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (matchIndex == -1) {
                append(text.substring(startIndex))
                break
            }

            // Append text before match
            append(text.substring(startIndex, matchIndex))

            // Append highlighted match
            withStyle(
                SpanStyle(
                    background = highlightColor,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append(text.substring(matchIndex, matchIndex + query.length))
            }

            startIndex = matchIndex + query.length
        }
    }

    Text(
        text = annotatedString,
        style = style,
        modifier = modifier
    )
}
