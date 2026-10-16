package com.bernaferrari.calculator.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bernaferrari.calculator.R
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

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { preferences.updateThemeMode(mode) }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { preferences.updateDynamicColor(enabled) }

    fun setHaptics(enabled: Boolean) = viewModelScope.launch { preferences.updateHapticFeedback(enabled) }

    fun clearHistory() = viewModelScope.launch { history.clearAllHistory() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onOpenConverter: () -> Unit,
    onOpenProgrammer: () -> Unit,
    onOpenStatistics: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showLicense by remember { mutableStateOf(false) }
    if (showLicense) LicenseDialog(onDismiss = { showLicense = false })

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 24.dp)) {
            item { SectionHeader(stringResource(R.string.settings_appearance)) }
            item {
                // Three plain choices; wallpaper colors are a separate, independent switch.
                val modes = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    modes.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = prefs.themeMode == mode,
                            onClick = { viewModel.setTheme(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                        ) { Text(stringResource(mode.title())) }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_dynamic_color)) },
                        supportingContent = { Text(stringResource(R.string.theme_material_you_description)) },
                        leadingContent = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                        trailingContent = { Switch(checked = prefs.dynamicColor, onCheckedChange = null) },
                        modifier = Modifier.toggleable(prefs.dynamicColor, role = Role.Switch) { viewModel.setDynamicColor(it) },
                    )
                }
            }
            item {
                ListItem(
                    content = { Text(stringResource(R.string.settings_haptics)) },
                    supportingContent = { Text(stringResource(R.string.settings_haptics_description)) },
                    leadingContent = { Icon(Icons.Rounded.Vibration, contentDescription = null) },
                    trailingContent = { Switch(checked = prefs.hapticFeedback, onCheckedChange = null) },
                    modifier = Modifier.toggleable(prefs.hapticFeedback, role = Role.Switch) { viewModel.setHaptics(it) },
                )
            }
            item { SectionHeader(stringResource(R.string.settings_tools)) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.menu_unit_converter)) },
                    leadingContent = { Icon(Icons.Rounded.Straighten, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onOpenConverter),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.menu_programmer)) },
                    leadingContent = { Icon(Icons.Rounded.Code, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onOpenProgrammer),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.menu_statistics)) },
                    leadingContent = { Icon(Icons.Rounded.BarChart, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onOpenStatistics),
                )
            }
            item { SectionHeader(stringResource(R.string.settings_history)) }
            item {
                ListItem(
                    content = { Text(stringResource(R.string.settings_clear_history)) },
                    leadingContent = { Icon(Icons.Rounded.DeleteSweep, contentDescription = null) },
                    colors = ListItemDefaults.colors(headlineColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.clickable { viewModel.clearHistory() },
                )
            }
            item { SectionHeader(stringResource(R.string.settings_about)) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_licenses)) },
                    supportingContent = { Text("Google Sans Flex · SIL Open Font License 1.1") },
                    leadingContent = { Icon(Icons.Rounded.Description, contentDescription = null) },
                    modifier = Modifier.clickable { showLicense = true },
                )
            }
        }
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
        text = {
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        },
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

private fun ThemeMode.title() = when (this) {
    ThemeMode.MATERIAL_YOU -> R.string.theme_material_you
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}
