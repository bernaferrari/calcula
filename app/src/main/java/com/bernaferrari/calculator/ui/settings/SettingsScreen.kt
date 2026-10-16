package com.bernaferrari.calculator.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bernaferrari.calculator.BuildConfig
import com.bernaferrari.calculator.R
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.domain.model.AUTO_DECIMALS
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.domain.model.DecimalSeparator
import com.bernaferrari.calculator.domain.model.ThemeMode
import com.bernaferrari.calculator.domain.model.UserPreferences
import com.bernaferrari.calculator.domain.repository.CalculatorRepository
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PreferencesRepository,
    private val history: CalculatorRepository,
) : ViewModel() {
    val prefs = preferences.getUserPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    private fun save(block: suspend PreferencesRepository.() -> Unit) = viewModelScope.launch { preferences.block() }

    fun setTheme(mode: ThemeMode) = save { updateThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = save { updateDynamicColor(enabled) }
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
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    var showLicense by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("settings", isScreen = true).fillMaxSize()) {
      Column(Modifier.fillMaxSize()) {
        // Pinned header, so scrolled content never slides under the status bar.
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp).height(56.dp)) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)
        }
        LazyColumn(Modifier.weight(1f).clipToBounds(), contentPadding = PaddingValues(bottom = bottomInset + 32.dp)) {

            item {
                val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                SettingsSection(
                    stringResource(R.string.settings_appearance),
                    listOfNotNull(
                        { p ->
                            ThemePicker(
                                position = p,
                                selected = when (prefs.themeMode) {
                                    ThemeMode.LIGHT -> PreviewKind.Light
                                    ThemeMode.DARK -> PreviewKind.Dark
                                    else -> PreviewKind.System
                                },
                                label = {
                                    stringResource(
                                        when (it) {
                                            PreviewKind.System -> R.string.theme_system
                                            PreviewKind.Light -> R.string.theme_light
                                            PreviewKind.Dark -> R.string.theme_dark
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
                        if (dynamicAvailable) {
                            { p ->
                                SwitchRow(p, Icons.Outlined.Palette, stringResource(R.string.settings_dynamic_color), stringResource(R.string.settings_dynamic_color_description), prefs.dynamicColor, viewModel::setDynamicColor)
                            }
                        } else {
                            null
                        },
                        { p ->
                            SwitchRow(p, Icons.Outlined.Contrast, stringResource(R.string.settings_high_contrast), stringResource(R.string.settings_high_contrast_description), prefs.highContrast, viewModel::setHighContrast)
                        },
                    ),
                )
            }

            item {
                SettingsSection(
                    stringResource(R.string.settings_calculator),
                    listOf(
                        { p ->
                            ChoiceRow(
                                p, Icons.Outlined.Explore, stringResource(R.string.settings_angle_unit),
                                listOf(AngleUnit.DEGREES, AngleUnit.RADIANS), prefs.angleUnit,
                                label = { stringResource(if (it == AngleUnit.DEGREES) R.string.settings_degrees else R.string.settings_radians) },
                                onSelect = viewModel::setAngleUnit,
                            )
                        },
                        { p ->
                            ChoiceRow(
                                p, Icons.Outlined.FormatListNumbered, stringResource(R.string.settings_decimal_places),
                                DecimalChoices, if (prefs.decimalPlaces in DecimalChoices) prefs.decimalPlaces else AUTO_DECIMALS,
                                label = { if (it == AUTO_DECIMALS) stringResource(R.string.settings_auto) else it.toString() },
                                onSelect = viewModel::setDecimalPlaces,
                            )
                        },
                        { p ->
                            ChoiceRow(
                                p, Icons.Outlined.Language, stringResource(R.string.settings_decimal_separator),
                                DecimalSeparator.entries.toList(), prefs.decimalSeparator,
                                label = {
                                    when (it) {
                                        DecimalSeparator.AUTO -> stringResource(R.string.settings_auto)
                                        DecimalSeparator.DOT -> "1,234.5"
                                        DecimalSeparator.COMMA -> "1.234,5"
                                    }
                                },
                                onSelect = viewModel::setDecimalSeparator,
                            )
                        },
                        { p ->
                            SwitchRow(p, Icons.Outlined.History, stringResource(R.string.settings_show_tape), stringResource(R.string.settings_show_tape_description), prefs.showHistory, viewModel::setShowHistory)
                        },
                        { p ->
                            SwitchRow(p, Icons.Outlined.SwipeVertical, stringResource(R.string.settings_flick_dots), stringResource(R.string.settings_flick_dots_description), prefs.showFlickDots, viewModel::setShowFlickDots)
                        },
                    ),
                )
            }

            item {
                SettingsSection(
                    stringResource(R.string.settings_feedback),
                    listOf(
                        { p -> SwitchRow(p, Icons.Outlined.Vibration, stringResource(R.string.settings_haptics), stringResource(R.string.settings_haptics_description), prefs.hapticFeedback, viewModel::setHaptics) },
                        { p -> SwitchRow(p, Icons.AutoMirrored.Outlined.VolumeUp, stringResource(R.string.settings_sounds), stringResource(R.string.settings_sounds_description), prefs.soundEnabled, viewModel::setSounds) },
                        { p -> SwitchRow(p, Icons.Outlined.Animation, stringResource(R.string.settings_reduce_motion), stringResource(R.string.settings_reduce_motion_description), prefs.reduceMotion, viewModel::setReduceMotion) },
                    ),
                )
            }

            item {
                SettingsSection(
                    stringResource(R.string.settings_more),
                    listOf(
                        { p -> ActionRow(p, Icons.Outlined.Straighten, stringResource(R.string.menu_unit_converter), onClick = onOpenConverter) },
                        { p -> ActionRow(p, Icons.Outlined.DeleteSweep, stringResource(R.string.settings_clear_history), destructive = true, showChevron = false, onClick = { confirmClear = true }) },
                    ),
                )
            }

            item {
                SettingsSection(
                    stringResource(R.string.settings_about),
                    listOf(
                        { p -> ActionRow(p, Icons.Outlined.Info, stringResource(R.string.app_name), subtitle = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME), onClick = null) },
                        { p -> ActionRow(p, Icons.Outlined.Description, stringResource(R.string.settings_licenses), subtitle = "Google Sans Flex · SIL OFL 1.1", onClick = { showLicense = true }) },
                    ),
                )
            }
        }
      }
    }

    if (showLicense) LicenseDialog(onDismiss = { showLicense = false })
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.settings_clear_history_title)) },
            text = { Text(stringResource(R.string.settings_clear_history_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearHistory(); confirmClear = false }) {
                    Text(stringResource(R.string.history_clear), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun LicenseDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val text = remember {
        runCatching { context.assets.open("licenses/GoogleSansFlex-OFL.txt").bufferedReader().use { it.readText() } }.getOrDefault("")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dismiss)) } },
        title = { Text("Google Sans Flex") },
        text = { Box(Modifier.verticalScroll(rememberScrollState())) { Text(text, style = MaterialTheme.typography.bodySmall) } },
    )
}
