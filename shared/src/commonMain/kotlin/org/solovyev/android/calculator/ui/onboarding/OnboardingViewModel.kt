package org.solovyev.android.calculator.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.AppPreferences
import org.solovyev.android.calculator.GuiTheme
import org.solovyev.android.calculator.GuiMode

/**
 * Simple ViewModel for the onboarding flow.
 * Manages preferences updates and tracks completion state.
 */
class OnboardingViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {

    val isOnboardingComplete: StateFlow<Boolean> = appPreferences.wizard.finished
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setTheme(theme: GuiTheme) {
        viewModelScope.launch {
            appPreferences.gui.setTheme(theme.id)
        }
    }

    fun setMode(mode: GuiMode) {
        viewModelScope.launch {
            appPreferences.gui.setMode(mode.id)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            appPreferences.wizard.setFinished(true)
        }
    }

    suspend fun shouldShowOnboarding(): Boolean {
        // Warning: This is suspend. Callers must handle coroutine context.
        return !appPreferences.wizard.finished.first()
    }
}
