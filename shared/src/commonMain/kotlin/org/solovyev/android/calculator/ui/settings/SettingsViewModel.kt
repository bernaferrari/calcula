package org.solovyev.android.calculator.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.AppPreferences

class SettingsViewModel(
    private val preferences: AppPreferences
) : ViewModel(), SettingsActions {

    val state: StateFlow<SettingsUiState> = combine(
        preferences.gui.mode,
        preferences.settings.angleUnit,
        preferences.settings.outputPrecision,
        preferences.gui.theme,
        preferences.gui.language,
        preferences.gui.vibrateOnKeypress,
        preferences.gui.highlightExpressions
    ) { flows: Array<Any> ->
        val mode = flows[0] as String
        val angleUnit = flows[1] as Int
        val precision = flows[2] as Int
        val theme = flows[3] as String
        val language = flows[4] as String
        val vibrate = flows[5] as Boolean
        val highlight = flows[6] as Boolean
        SettingsUiState(
            mode = modeFromStr(mode),
            angleUnit = angleFromInt(angleUnit),
            outputPrecision = precision.coerceIn(0, 10),
            theme = themeFromStr(theme),
            languageCode = language,
            vibrateOnKeypress = vibrate,
            highlightExpressions = highlight
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

    override fun setOutputPrecision(precision: Int) {
        viewModelScope.launch { preferences.settings.setOutputPrecision(precision.coerceIn(0, 10)) }
    }

    override fun setTheme(theme: AppTheme) {
        val themeStr = when(theme) {
            AppTheme.MATERIAL_YOU -> "material_theme"
            AppTheme.MATERIAL_BLACK -> "material_black"
            AppTheme.MATERIAL_DARK -> "material_dark"
            AppTheme.MATERIAL_LIGHT -> "material_light"
            AppTheme.METRO_BLUE -> "metro_blue"
            AppTheme.METRO_GREEN -> "metro_green"
            AppTheme.METRO_PURPLE -> "metro_purple"
        }
        viewModelScope.launch { preferences.gui.setTheme(themeStr) }
    }

    override fun setAppearanceMode(mode: AppearanceMode) {
        val themeStr = when (mode) {
            AppearanceMode.SYSTEM -> "material_theme"
            AppearanceMode.LIGHT -> "material_light"
            AppearanceMode.DARK -> "material_dark"
        }
        viewModelScope.launch { preferences.gui.setTheme(themeStr) }
    }

    override fun setLanguage(code: String) {
        viewModelScope.launch { preferences.gui.setLanguage(code) }
    }

    override fun setVibrateOnKeypress(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setVibrateOnKeypress(enabled) }
    }

    override fun setHighlightExpressions(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setHighlightExpressions(enabled) }
    }

    // --- Mappers ---

    private fun modeFromStr(str: String): CalculatorMode = when(str) {
        "simple" -> CalculatorMode.SIMPLE
        "engineer" -> CalculatorMode.ENGINEER
        "modern" -> CalculatorMode.MODERN
        else -> CalculatorMode.SIMPLE
    }

    private fun themeFromStr(str: String): AppTheme = when(str) {
        "material_theme" -> AppTheme.MATERIAL_YOU
        "material_you" -> AppTheme.MATERIAL_YOU
        "material_black" -> AppTheme.MATERIAL_BLACK
        "material_dark" -> AppTheme.MATERIAL_DARK
        "material_light" -> AppTheme.MATERIAL_LIGHT
        "metro_blue" -> AppTheme.METRO_BLUE
        "metro_green" -> AppTheme.METRO_GREEN
        "metro_purple" -> AppTheme.METRO_PURPLE
        else -> AppTheme.MATERIAL_YOU
    }

    private fun angleFromInt(value: Int): AngleUnit = when(value) {
        0 -> AngleUnit.DEG
        1 -> AngleUnit.RAD
        2 -> AngleUnit.GRAD
        3 -> AngleUnit.TURNS
        else -> AngleUnit.DEG
    }

    // --- Stub implementations for interface compatibility ---

    override fun setNumeralBase(base: NumeralBase) {}
    override fun setOutputNotation(notation: OutputNotation) {}
    override fun setOutputSeparator(separator: Char) {}
    override fun setOutputSeparator(separator: OutputSeparator) {}
    override fun setMultiplicationSign(sign: MultiplicationSign) {}
    override fun setDynamicColor(enabled: Boolean) {}
    override fun setThemeSeedColor(color: Int) {}
    override fun setIsAmoledTheme(enabled: Boolean) {}
    override fun setHighContrast(enabled: Boolean) {}
    override fun setRotateScreen(enabled: Boolean) {}
    override fun setKeepScreenOn(enabled: Boolean) {}
    override fun setWidgetTheme(theme: SimpleTheme) {}
    override fun setCalculateOnFly(enabled: Boolean) {}
    override fun setRpnMode(enabled: Boolean) {}
    override fun setTapeMode(enabled: Boolean) {}
    override fun setShowReleaseNotes(enabled: Boolean) {}
    override fun setShowCalculationLatency(enabled: Boolean) {}
    override fun setUseBackAsPrevious(enabled: Boolean) {}
    override fun setPlotImag(enabled: Boolean) {}
    override fun setLatexMode(enabled: Boolean) {}
    override fun setReduceMotion(enabled: Boolean) {}
    override fun setFontScale(scale: Float) {}
    override fun setBitwiseWordSize(size: Int) {}
    override fun setBitwiseSigned(signed: Boolean) {}
}
