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

    // Helper data classes to group flows (max 5 per combine)
    private data class Group1(
        val mode: String,
        val angleUnit: Int,
        val numeralBase: Int,
        val outputNotation: Int,
        val outputPrecision: Int
    )

    private data class Group2(
        val outputSeparator: Char,
        val theme: String,
        val dynamicColor: Boolean,
        val themeSeed: Int,
        val isAmoled: Boolean,
        val language: String
    )

    private data class Group2Theme(
        val outputSeparator: Char,
        val theme: String,
        val dynamicColor: Boolean
    )

    private data class Group2Meta(
        val themeSeed: Int,
        val isAmoled: Boolean,
        val language: String
    )

    private data class Group3(
        val vibrate: Boolean,
        val highContrast: Boolean,
        val highlight: Boolean,
        val rotate: Boolean,
        val keepOn: Boolean
    )

    private data class Group4(
        val widgetTheme: String,
        val calcOnFly: Boolean,
        val rpnMode: Boolean,
        val tapeMode: Boolean,
        val releaseNotes: Boolean
    )

    private data class Group5(
        val backPrev: Boolean,
        val plotImag: Boolean,
        val latexMode: Boolean,
        val showCalcLatency: Boolean
    )

    private val group2Flow = combine(
        combine(
            preferences.settings.outputSeparator,
            preferences.gui.theme,
            preferences.gui.dynamicColor
        ) { sep, theme, dynamicColor -> Group2Theme(sep, theme, dynamicColor) },
        combine(
            preferences.gui.themeSeed,
            preferences.gui.isAmoled,
            preferences.gui.language
        ) { seed, amoled, lang -> Group2Meta(seed, amoled, lang) }
    ) { theme, meta ->
        Group2(
            outputSeparator = theme.outputSeparator,
            theme = theme.theme,
            dynamicColor = theme.dynamicColor,
            themeSeed = meta.themeSeed,
            isAmoled = meta.isAmoled,
            language = meta.language
        )
    }

    val state: StateFlow<SettingsUiState> = combine(
        combine(
            preferences.gui.mode,
            preferences.settings.angleUnit,
            preferences.settings.numeralBase,
            preferences.settings.outputNotation,
            preferences.settings.outputPrecision
        ) { mode, angle, base, not, prec -> Group1(mode, angle, base, not, prec) },
        group2Flow,

        combine(
            preferences.gui.vibrateOnKeypress,
            preferences.gui.highContrast,
            preferences.gui.highlightExpressions,
            preferences.gui.rotateScreen,
            preferences.gui.keepScreenOn
        ) { vib, hc, hl, rot, keep -> Group3(vib, hc, hl, rot, keep) },

        combine(
            preferences.widget.theme,
            preferences.settings.calculateOnFly,
            preferences.settings.rpnMode,
            preferences.settings.tapeMode,
            preferences.gui.showReleaseNotes
        ) { wTheme, fly, rpnMode, tapeMode, rel ->
            Group4(wTheme, fly, rpnMode, tapeMode, rel)
        },

        combine(
            preferences.gui.useBackAsPrevious,
            preferences.gui.plotImag,
            preferences.gui.latexMode,
            preferences.gui.showCalculationLatency
        ) { back, plot, latex, showLatency -> Group5(back, plot, latex, showLatency) }

    ) { g1, g2, g3, g4, g5 ->
        SettingsUiState(
            mode = modeFromStr(g1.mode),
            angleUnit = angleFromInt(g1.angleUnit),
            numeralBase = baseFromInt(g1.numeralBase),
            outputNotation = notationFromInt(g1.outputNotation),
            outputPrecision = g1.outputPrecision,
            outputSeparator = g2.outputSeparator,
            appearanceMode = appearanceModeFromTheme(g2.theme),
            theme = themeFromStr(g2.theme),
            dynamicColorEnabled = g2.dynamicColor,
            themeSeedColor = g2.themeSeed,
            isAmoledTheme = g2.isAmoled,
            languageCode = g2.language,
            vibrateOnKeypress = g3.vibrate,
            highContrast = g3.highContrast,
            highlightExpressions = g3.highlight,
            rotateScreen = g3.rotate,
            keepScreenOn = g3.keepOn,
            widgetTheme = simpleThemeFromStr(g4.widgetTheme),
            calculateOnFly = g4.calcOnFly,
            rpnMode = g4.rpnMode,
            tapeMode = g4.tapeMode,
            showReleaseNotes = g4.releaseNotes,
            showCalculationLatency = g5.showCalcLatency,
            useBackAsPrevious = g5.backPrev,
            plotImag = g5.plotImag,
            latexMode = g5.latexMode
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
            NumeralBase.OCT -> 2
            NumeralBase.BIN -> 3
        }
        viewModelScope.launch { preferences.settings.setNumeralBase(baseInt) }
    }

    override fun setOutputNotation(notation: OutputNotation) {
        val notInt = when(notation) {
            OutputNotation.PLAIN -> 0
            OutputNotation.SCIENTIFIC -> 1
            OutputNotation.ENGINEERING -> 2
        }
        viewModelScope.launch { preferences.settings.setOutputNotation(notInt) }
    }

    override fun setOutputPrecision(precision: Int) {
        viewModelScope.launch { preferences.settings.setOutputPrecision(precision) }
    }

    override fun setOutputSeparator(separator: Char) {
        viewModelScope.launch { preferences.settings.setOutputSeparator(separator) }
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

    override fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setDynamicColor(enabled) }
    }

    override fun setThemeSeedColor(color: Int) {
        viewModelScope.launch { preferences.gui.setThemeSeed(color) }
    }

    override fun setIsAmoledTheme(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setIsAmoled(enabled) }
    }

    override fun setLanguage(code: String) {
        viewModelScope.launch { preferences.gui.setLanguage(code) }
    }

    override fun setVibrateOnKeypress(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setVibrateOnKeypress(enabled) }
    }

    override fun setHighContrast(enabled: Boolean) {
         viewModelScope.launch { preferences.gui.setHighContrast(enabled) }
    }

    override fun setHighlightExpressions(enabled: Boolean) {
         viewModelScope.launch { preferences.gui.setHighlightExpressions(enabled) }
    }

    override fun setRotateScreen(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setRotateScreen(enabled) }
    }

    override fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setKeepScreenOn(enabled) }
    }

    override fun setWidgetTheme(theme: SimpleTheme) {
        viewModelScope.launch { preferences.widget.setTheme(simpleThemeToStr(theme)) }
    }

    override fun setCalculateOnFly(enabled: Boolean) {
        viewModelScope.launch { preferences.settings.setCalculateOnFly(enabled) }
    }

    override fun setRpnMode(enabled: Boolean) {
        viewModelScope.launch { preferences.settings.setRpnMode(enabled) }
    }

    override fun setTapeMode(enabled: Boolean) {
        viewModelScope.launch { preferences.settings.setTapeMode(enabled) }
    }

    override fun setShowReleaseNotes(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setShowReleaseNotes(enabled) }
    }

    override fun setShowCalculationLatency(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setShowCalculationLatency(enabled) }
    }

    override fun setUseBackAsPrevious(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setUseBackAsPrevious(enabled) }
    }

    override fun setPlotImag(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setPlotImag(enabled) }
    }

    override fun setLatexMode(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setLatexMode(enabled) }
    }

    // --- Mappers ---

    private fun modeFromStr(str: String): CalculatorMode = when(str) {
        "simple" -> CalculatorMode.SIMPLE
        "engineer" -> CalculatorMode.ENGINEER
        "modern" -> CalculatorMode.MODERN
        else -> CalculatorMode.ENGINEER
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

    private fun appearanceModeFromTheme(str: String): AppearanceMode = when (str) {
        "material_light" -> AppearanceMode.LIGHT
        "material_dark" -> AppearanceMode.DARK
        else -> AppearanceMode.SYSTEM
    }
    
    private fun simpleThemeFromStr(str: String): SimpleTheme = when(str) {
        "material_dark" -> SimpleTheme.MATERIAL_DARK
        "material_light" -> SimpleTheme.MATERIAL_LIGHT
        "metro_blue" -> SimpleTheme.METRO_BLUE
        else -> SimpleTheme.DEFAULT
    }

    private fun simpleThemeToStr(theme: SimpleTheme): String = when(theme) {
        SimpleTheme.MATERIAL_DARK -> "material_dark"
        SimpleTheme.MATERIAL_LIGHT -> "material_light"
        SimpleTheme.METRO_BLUE -> "metro_blue"
        SimpleTheme.DEFAULT -> "default"
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
        2 -> NumeralBase.OCT
        3 -> NumeralBase.BIN
        else -> NumeralBase.DEC
    }

    private fun notationFromInt(value: Int): OutputNotation = when(value) {
        0 -> OutputNotation.PLAIN
        1 -> OutputNotation.SCIENTIFIC
        2 -> OutputNotation.ENGINEERING
        else -> OutputNotation.PLAIN
    }
}
