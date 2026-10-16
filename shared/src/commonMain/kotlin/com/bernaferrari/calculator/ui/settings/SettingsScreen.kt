package com.bernaferrari.calculator.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.SwipeVertical
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import com.bernaferrari.calculator.AppGraph
import com.bernaferrari.calculator.APP_VERSION
import com.bernaferrari.calculator.domain.model.AppPalette
import com.bernaferrari.calculator.ui.calculator.separators
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.domain.input.Calculator
import com.bernaferrari.calculator.domain.math.MathContext
import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.ui.theme.orDefault
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.ui.fadeBottomEdge
import com.bernaferrari.calculator.domain.model.AUTO_DECIMALS
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.domain.model.DecimalSeparator
import com.bernaferrari.calculator.domain.model.ThemeMode
import com.bernaferrari.calculator.domain.model.UserPreferences
import com.bernaferrari.calculator.domain.repository.CalculatorRepository
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: PreferencesRepository = AppGraph.preferences,
    private val history: CalculatorRepository = AppGraph.history,
) : ViewModel() {
    val prefs = preferences.getUserPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    private fun save(block: suspend PreferencesRepository.() -> Unit) = viewModelScope.launch { preferences.block() }

    fun setTheme(mode: ThemeMode) = save { updateThemeMode(mode) }
    fun setPalette(palette: AppPalette) = save { updatePalette(palette) }
    fun setHighContrast(enabled: Boolean) = save { updateHighContrast(enabled) }
    fun setAngleUnit(unit: AngleUnit) = save { updateAngleUnit(unit) }
    fun setDecimalPlaces(places: Int) = save { updateDecimalPlaces(places) }
    fun setDecimalSeparator(separator: DecimalSeparator) = save { updateDecimalSeparator(separator) }
    fun setShowHistory(enabled: Boolean) = save { updateShowHistory(enabled) }
    fun setShowFlickDots(enabled: Boolean) = save { updateShowFlickDots(enabled) }
    fun setHaptics(enabled: Boolean) = save { updateHapticFeedback(enabled) }
    fun setSounds(enabled: Boolean) = save { updateSoundEnabled(enabled) }
    fun setReduceMotion(enabled: Boolean) = save { updateReduceMotion(enabled) }
    fun clearHistory() = viewModelScope.launch { history.clearAllHistory() }
}

private val DecimalChoices = listOf(AUTO_DECIMALS, 2, 4, 6, 10)

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: SettingsViewModel = viewModel { SettingsViewModel() },
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    var confirmClear by remember { mutableStateOf(false) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("settings", isScreen = true).fillMaxSize()) {
      Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal))) {
        // Pinned header, so scrolled content never slides under the status bar.
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp).height(56.dp)) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
            Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.titleLarge)
        }
        // On wide screens the list keeps a readable width, centred.
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 640.dp).fillMaxSize().clipToBounds().fadeBottomEdge(bottomInset + 12.dp), contentPadding = PaddingValues(bottom = bottomInset + 32.dp)) {

            item {
                val palette = prefs.palette.orDefault()
                SettingsSection(
                    stringResource(Res.string.settings_appearance),
                    listOfNotNull(
                        { p ->
                            ThemePicker(
                                position = p,
                                selected = when (prefs.themeMode) {
                                    ThemeMode.LIGHT -> PreviewKind.Light
                                    ThemeMode.DARK -> PreviewKind.Dark
                                    else -> PreviewKind.System
                                },
                                palette = palette,
                                label = {
                                    stringResource(
                                        when (it) {
                                            PreviewKind.System -> Res.string.theme_system
                                            PreviewKind.Light -> Res.string.theme_light
                                            PreviewKind.Dark -> Res.string.theme_dark
                                        },
                                    )
                                },
                                onSelect = {
                                    viewModel.setTheme(
                                        when (it) {
                                            PreviewKind.System -> ThemeMode.SYSTEM
                                            PreviewKind.Light -> ThemeMode.LIGHT
                                            PreviewKind.Dark -> ThemeMode.DARK
                                        },
                                    )
                                },
                            )
                        },
                        { p -> PaletteRow(p, palette, label = { stringResource(it.title()) }, onSelect = viewModel::setPalette) },
                        { p ->
                            SwitchRow(p, Icons.Outlined.Contrast, stringResource(Res.string.settings_high_contrast), stringResource(Res.string.settings_high_contrast_description), prefs.highContrast, viewModel::setHighContrast)
                        },
                    ),
                )
            }

            item {
                SettingsSection(
                    stringResource(Res.string.settings_calculator),
                    listOf(
                        { p ->
                            ChoiceRow(
                                p, Icons.Outlined.Explore, stringResource(Res.string.settings_angle_unit),
                                listOf(AngleUnit.DEGREES, AngleUnit.RADIANS), prefs.angleUnit,
                                label = { stringResource(if (it == AngleUnit.DEGREES) Res.string.settings_degrees else Res.string.settings_radians) },
                                onSelect = viewModel::setAngleUnit,
                            )
                        },
                        { p ->
                            ChoiceRow(
                                p, Icons.Outlined.FormatListNumbered, stringResource(Res.string.settings_decimal_places),
                                DecimalChoices, if (prefs.decimalPlaces in DecimalChoices) prefs.decimalPlaces else AUTO_DECIMALS,
                                label = { if (it == AUTO_DECIMALS) stringResource(Res.string.settings_decimals_full) else it.toString() },
                                onSelect = viewModel::setDecimalPlaces,
                                // A live example says more than "Auto" ever could.
                                subtitle = decimalsExample(prefs.decimalPlaces, prefs.decimalSeparator),
                            )
                        },
                        { p ->
                            ChoiceRow(
                                p, Icons.Outlined.Language, stringResource(Res.string.settings_decimal_separator),
                                DecimalSeparator.entries.toList(), prefs.decimalSeparator,
                                label = {
                                    when (it) {
                                        DecimalSeparator.AUTO -> stringResource(Res.string.settings_separator_region)
                                        DecimalSeparator.DOT -> "1,234.5"
                                        DecimalSeparator.COMMA -> "1.234,5"
                                    }
                                },
                                onSelect = viewModel::setDecimalSeparator,
                                subtitle = stringResource(
                                    Res.string.settings_separator_example,
                                    DisplayFormatter(prefs.decimalSeparator.separators()).number("1234567.8"),
                                ),
                            )
                        },
                        { p ->
                            SwitchRow(p, Icons.Outlined.History, stringResource(Res.string.settings_show_tape), stringResource(Res.string.settings_show_tape_description), prefs.showHistory, viewModel::setShowHistory)
                        },
                        { p ->
                            SwitchRow(p, Icons.Outlined.SwipeVertical, stringResource(Res.string.settings_flick_dots), stringResource(Res.string.settings_flick_dots_description), prefs.showFlickDots, viewModel::setShowFlickDots)
                        },
                    ),
                )
            }

            item {
                SettingsSection(
                    stringResource(Res.string.settings_feedback),
                    listOf(
                        { p -> SwitchRow(p, Icons.Outlined.Vibration, stringResource(Res.string.settings_haptics), stringResource(Res.string.settings_haptics_description), prefs.hapticFeedback, viewModel::setHaptics) },
                        { p -> SwitchRow(p, Icons.AutoMirrored.Outlined.VolumeUp, stringResource(Res.string.settings_sounds), stringResource(Res.string.settings_sounds_description), prefs.soundEnabled, viewModel::setSounds) },
                        { p -> SwitchRow(p, Icons.Outlined.Animation, stringResource(Res.string.settings_reduce_motion), stringResource(Res.string.settings_reduce_motion_description), prefs.reduceMotion, viewModel::setReduceMotion) },
                    ),
                )
            }

            item {
                SettingsSection(
                    stringResource(Res.string.settings_more),
                    listOf(
                        { p -> ActionRow(p, Icons.Outlined.Straighten, stringResource(Res.string.menu_unit_converter), onClick = onOpenConverter) },
                        { p -> ActionRow(p, Icons.Outlined.DeleteSweep, stringResource(Res.string.settings_clear_history), destructive = true, showChevron = false, onClick = { confirmClear = true }) },
                    ),
                )
            }

            item {
                SettingsSection(
                    stringResource(Res.string.settings_about),
                    listOf(
                        { p ->
                            ActionRow(
                                p, Icons.Outlined.Info, stringResource(Res.string.about_row),
                                subtitle = stringResource(Res.string.settings_version, APP_VERSION),
                                onClick = onOpenAbout,
                                modifier = Modifier.containerTransform("about"),
                            )
                        },
                    ),
                )
            }
        }
        }
      }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(Res.string.settings_clear_history_title)) },
            text = { Text(stringResource(Res.string.settings_clear_history_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearHistory(); confirmClear = false }) {
                    Text(stringResource(Res.string.history_clear), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

/** A palette's name in the person's language. */
internal fun AppPalette.title(): StringResource = when (this) {
    AppPalette.WALLPAPER -> Res.string.palette_wallpaper
    AppPalette.INDIGO -> Res.string.palette_indigo
    AppPalette.BLUE -> Res.string.palette_blue
    AppPalette.TEAL -> Res.string.palette_teal
    AppPalette.GREEN -> Res.string.palette_green
    AppPalette.AMBER -> Res.string.palette_amber
    AppPalette.ORANGE -> Res.string.palette_orange
    AppPalette.RED -> Res.string.palette_red
    AppPalette.PINK -> Res.string.palette_pink
    AppPalette.PURPLE -> Res.string.palette_purple
    AppPalette.GRAPHITE -> Res.string.palette_graphite
}

/** "2 ÷ 3 = 0.67", in the chosen decimals and separators. */
private fun decimalsExample(decimals: Int, separator: DecimalSeparator): String {
    val two = Decimal(2).divide(Decimal(3), MathContext(40))
    val formatter = DisplayFormatter(separator.separators())
    return "2 ÷ 3 = " + formatter.number(Calculator.formatResult(two, decimals))
}
