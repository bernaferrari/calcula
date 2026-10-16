package com.bernaferrari.calculator.ui.theme

import com.bernaferrari.calculator.AppGraph
import com.bernaferrari.calculator.platform.supportsDynamicColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.calculator.domain.model.ThemeMode
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ThemeViewModel(
        private val preferencesRepository: PreferencesRepository = AppGraph.preferences,
    ) : ViewModel() {
        private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
        private val dynamicColor = MutableStateFlow(true)
        private val highContrast = MutableStateFlow(false)

        init {
            observeThemePreferences()
        }

        private fun observeThemePreferences() {
            viewModelScope.launch {
                preferencesRepository
                    .getUserPreferences()
                    .catch { /* Handle error silently, use default */ }
                    .collect { preferences ->
                        _themeMode.value = preferences.themeMode
                        dynamicColor.value = preferences.dynamicColor
                        highContrast.value = preferences.highContrast
                    }
            }
        }

        @Composable
        fun shouldUseDarkTheme(): Boolean {
            val currentTheme by themeMode.collectAsState()
            val systemInDarkTheme = isSystemInDarkTheme()

            return when (currentTheme) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemInDarkTheme
                ThemeMode.MATERIAL_YOU -> systemInDarkTheme
            }
        }

        @Composable
        fun shouldUseDynamicColor(): Boolean {
            val enabled by dynamicColor.collectAsState()
            return enabled && supportsDynamicColor
        }

        @Composable
        fun shouldUseHighContrast(): Boolean {
            val enabled by highContrast.collectAsState()
            return enabled
        }

        fun updateThemeMode(themeMode: ThemeMode) {
            viewModelScope.launch {
                preferencesRepository.updateThemeMode(themeMode)
            }
        }
    }
