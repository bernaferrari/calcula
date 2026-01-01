package org.solovyev.android.calculator.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.AppPreferences
// import org.solovyev.android.calculator.ui.generated.resources.* // Removed

class SettingsViewModel(
    private val preferences: AppPreferences
) : ViewModel(), SettingsActions {

    val state: StateFlow<SettingsUiState> = combine(
        preferences.gui.mode,
        preferences.settings.angleUnit,
        preferences.settings.numeralBase,
        preferences.gui.theme,
        preferences.gui.vibrateOnKeypress
    ) { modeStr, angleInt, baseInt, themeStr, vibrate ->
        SettingsUiState(
            mode = modeFromStr(modeStr),
            angleUnit = angleFromInt(angleInt),
            numeralBase = baseFromInt(baseInt),
            theme = themeFromStr(themeStr),
            vibrateOnKeypress = vibrate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    // --- Actions ---

    override fun setMode(mode: CalculatorMode) {
        val modeStr = when(mode) {
            CalculatorMode.SIMPLE -> "simple"
            CalculatorMode.ENGINEER -> "engineer"
            CalculatorMode.MODERN -> "modern"
        }
        viewModelScope.launch { preferences.gui.setMode(modeStr) }
    }

    override fun setAngleUnit(unit: AngleUnit) {
        val angleInt = when(unit) {
            AngleUnit.DEG -> 0
            AngleUnit.RAD -> 1
            AngleUnit.GRAD -> 2
            AngleUnit.TURNS -> 3
        }
        viewModelScope.launch { preferences.settings.setAngleUnit(angleInt) }
    }

    override fun setNumeralBase(base: NumeralBase) {
        val baseInt = when(base) {
            NumeralBase.DEC -> 0
            NumeralBase.HEX -> 1
            NumeralBase.BIN -> 2
            NumeralBase.OCT -> 0 // Not supported or default
        }
        viewModelScope.launch { preferences.settings.setNumeralBase(baseInt) }
    }

    override fun setOutputNotation(notation: OutputNotation) {
        // Implement when flow available
    }

    override fun setOutputPrecision(precision: Int) {
         // Implement when flow available
    }

    override fun setOutputSeparator(separator: Char) {
         // Implement when flow available
    }

    override fun setTheme(theme: AppTheme) {
        val themeStr = when(theme) {
            AppTheme.MATERIAL_YOU -> "material_you"
            AppTheme.MATERIAL_BLACK -> "material_black"
            AppTheme.MATERIAL_DARK -> "material_dark"
            AppTheme.MATERIAL_LIGHT -> "material_light"
            AppTheme.METRO_BLUE -> "metro_blue"
            AppTheme.METRO_GREEN -> "metro_green"
            AppTheme.METRO_PURPLE -> "metro_purple"
        }
        viewModelScope.launch { preferences.gui.setTheme(themeStr) }
    }

    override fun setLanguage(code: String) {
        // Implement
    }

    override fun setVibrateOnKeypress(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setVibrateOnKeypress(enabled) }
    }

    override fun setHighContrast(enabled: Boolean) {
         viewModelScope.launch { preferences.gui.setHighContrast(enabled) }
    }

    override fun setHighlightExpressions(enabled: Boolean) {
         // Implement if preference exists
    }

    override fun setRotateScreen(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setRotateScreen(enabled) }
    }

    override fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setKeepScreenOn(enabled) }
    }

    override fun setOnscreenShowAppIcon(enabled: Boolean) {
        viewModelScope.launch { preferences.onscreen.setShowAppIcon(enabled) }
    }

    override fun setOnscreenTheme(theme: SimpleTheme) {
        // Implement
    }

    override fun setWidgetTheme(theme: SimpleTheme) {
        // Implement
    }

    override fun setCalculateOnFly(enabled: Boolean) {
        viewModelScope.launch { preferences.settings.setCalculateOnFly(enabled) }
    }

    override fun setShowReleaseNotes(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setShowReleaseNotes(enabled) }
    }

    override fun setUseBackAsPrevious(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setUseBackAsPrevious(enabled) }
    }

    override fun setPlotImag(enabled: Boolean) {
        // Implement
    }

    // --- Mappers ---

    private fun modeFromStr(str: String): CalculatorMode = when(str) {
        "simple" -> CalculatorMode.SIMPLE
        "engineer" -> CalculatorMode.ENGINEER
        "modern" -> CalculatorMode.MODERN
        else -> CalculatorMode.ENGINEER
    }

    private fun themeFromStr(str: String): AppTheme = when(str) {
        "material_you" -> AppTheme.MATERIAL_YOU
        "material_black" -> AppTheme.MATERIAL_BLACK
        "material_dark" -> AppTheme.MATERIAL_DARK
        "material_light" -> AppTheme.MATERIAL_LIGHT
        "metro_blue" -> AppTheme.METRO_BLUE
        "metro_green" -> AppTheme.METRO_GREEN
        "metro_purple" -> AppTheme.METRO_PURPLE
        else -> AppTheme.MATERIAL_DARK
    }

    private fun angleFromInt(value: Int): AngleUnit = when(value) {
        0 -> AngleUnit.DEG
        1 -> AngleUnit.RAD
        2 -> AngleUnit.GRAD
        3 -> AngleUnit.TURNS
        else -> AngleUnit.DEG
    }

    private fun baseFromInt(value: Int): NumeralBase = when(value) {
        0 -> NumeralBase.DEC
        1 -> NumeralBase.HEX
        2 -> NumeralBase.BIN
        else -> NumeralBase.DEC
    }
}
