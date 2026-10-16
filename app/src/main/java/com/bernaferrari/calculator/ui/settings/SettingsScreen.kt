package com.bernaferrari.calculator.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.material3.RadioButton
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
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 32.dp)) {
            item { SectionHeader(stringResource(R.string.settings_appearance)) }
            ThemeMode.entries.forEach { mode ->
                item {
                    val selected = prefs.themeMode == mode
                    ListItem(
                        content = { Text(stringResource(mode.title())) },
                        supportingContent = { Text(stringResource(mode.description())) },
                        leadingContent = { RadioButton(selected = selected, onClick = null) },
                        modifier = Modifier.selectable(selected, role = Role.RadioButton) { viewModel.setTheme(mode) },
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
        }
    }
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

private fun ThemeMode.description() = when (this) {
    ThemeMode.MATERIAL_YOU -> R.string.theme_material_you_description
    ThemeMode.SYSTEM -> R.string.theme_system_description
    ThemeMode.LIGHT -> R.string.theme_light_description
    ThemeMode.DARK -> R.string.theme_dark_description
}
