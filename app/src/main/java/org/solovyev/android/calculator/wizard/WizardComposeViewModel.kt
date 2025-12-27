package org.solovyev.android.calculator.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.wizard.ListWizardFlow
import org.solovyev.android.wizard.WizardStep
import org.solovyev.android.wizard.Wizards
import javax.inject.Inject

data class WizardSettingsState(
    val mode: Preferences.Gui.Mode,
    val theme: Preferences.Gui.Theme,
    val showAppIcon: Boolean
)

@HiltViewModel
class WizardComposeViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val wizards: Wizards
) : ViewModel() {

    private val _wizardState = MutableStateFlow<WizardState?>(null)
    val wizardState: StateFlow<WizardState?> = _wizardState

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

    fun startWizard(flowName: String, startStep: String?) {
        val wizard = wizards.getWizard(flowName)
        val flow = wizard.flow
        val step = startStep?.let { flow.getStepByName(it) } ?: flow.firstStep
        _wizardState.value = WizardState(wizard, flow, step)
    }

    fun nextStep() {
        val currentState = _wizardState.value ?: return
        val next = (currentState.flow as? ListWizardFlow)?.getNextStep(currentState.step)
        if (next != null) {
            updateStep(currentState, next)
        }
    }

    fun prevStep() {
        val currentState = _wizardState.value ?: return
        val prev = (currentState.flow as? ListWizardFlow)?.getPrevStep(currentState.step)
        if (prev != null) {
            updateStep(currentState, prev)
        }
    }

    private fun updateStep(currentState: WizardState, step: WizardStep) {
        currentState.wizard.saveLastStep(step)
        _wizardState.value = currentState.copy(step = step)
    }

    fun finishWizard(forceFinish: Boolean = false) {
        val currentState = _wizardState.value ?: return
        currentState.wizard.saveFinished(currentState.step, forceFinish)
        // Navigation side effect should be handled by UI observing a finish event or similar
    }

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

data class WizardState(
    val wizard: org.solovyev.android.wizard.Wizard,
    val flow: org.solovyev.android.wizard.WizardFlow,
    val step: WizardStep
)
