package org.solovyev.android.calculator.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.di.AppPreferences
import javax.inject.Inject

data class WizardSettingsState(
    val mode: Preferences.Gui.Mode,
    val theme: Preferences.Gui.Theme,
    val showAppIcon: Boolean
)

@HiltViewModel
class WizardComposeViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    val state: StateFlow<WizardSettingsState> = combine(
        appPreferences.settings.mode,
        appPreferences.settings.theme,
        appPreferences.settings.onscreenShowAppIcon
    ) { mode, theme, showAppIcon ->
        WizardSettingsState(
            mode = mode,
            theme = theme,
            showAppIcon = showAppIcon
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        WizardSettingsState(
            mode = Preferences.Gui.Mode.simple,
            theme = Preferences.Gui.Theme.material_theme,
            showAppIcon = true
        )
    )

    fun setMode(mode: Preferences.Gui.Mode) = viewModelScope.launch {
        appPreferences.settings.setMode(mode)
    }

    fun setTheme(theme: Preferences.Gui.Theme) = viewModelScope.launch {
        appPreferences.settings.setTheme(theme)
    }

    fun setShowAppIcon(show: Boolean) = viewModelScope.launch {
        appPreferences.settings.setOnscreenShowAppIcon(show)
    }
}
