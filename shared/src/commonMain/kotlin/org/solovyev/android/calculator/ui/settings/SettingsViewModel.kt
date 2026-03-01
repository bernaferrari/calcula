package org.solovyev.android.calculator.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.AppPreferences

class SettingsViewModel(
    private val preferences: AppPreferences
) : ViewModel(), SettingsActions {
    private val reduceMotionState = MutableStateFlow(false)
    private val fontScaleState = MutableStateFlow(1.0f)

    val state: StateFlow<SettingsUiState> = combine(
        preferences.gui.mode,
        preferences.settings.angleUnit,
        preferences.settings.numeralBase,
        preferences.settings.outputNotation,
        preferences.settings.outputPrecision,
        preferences.settings.outputSeparator,
        preferences.settings.multiplicationSign,
        preferences.gui.theme,
        preferences.gui.dynamicColor,
        preferences.gui.themeSeed,
        preferences.gui.isAmoled,
        preferences.gui.language,
        preferences.gui.vibrateOnKeypress,
        preferences.gui.highContrast,
        preferences.gui.highlightExpressions,
        preferences.gui.rotateScreen,
        preferences.gui.keepScreenOn,
        preferences.widget.theme,
        preferences.settings.calculateOnFly,
        preferences.settings.rpnMode,
        preferences.settings.tapeMode,
        preferences.gui.showReleaseNotes,
        preferences.gui.showCalculationLatency,
        preferences.gui.useBackAsPrevious,
        preferences.gui.plotImag,
        preferences.gui.latexMode,
        preferences.settings.bitwiseWordSize,
        preferences.settings.bitwiseSigned,
        reduceMotionState,
        fontScaleState,
        preferences.haptics.hapticOnReleaseEnabled,
        preferences.gestures.gestureAutoActivationEnabled,
        preferences.gestures.bottomRightEqualsEnabled
    ) { flows: Array<Any> ->
        val mode = flows[0] as String
        val angleUnit = flows[1] as Int
        val numeralBase = flows[2] as Int
        val outputNotation = flows[3] as Int
        val precision = flows[4] as Int
        val outputSeparator = flows[5] as Char
        val multiplicationSign = flows[6] as String
        val theme = flows[7] as String
        val dynamicColor = flows[8] as Boolean
        val themeSeed = flows[9] as Int
        val isAmoled = flows[10] as Boolean
        val language = flows[11] as String
        val vibrate = flows[12] as Boolean
        val highContrast = flows[13] as Boolean
        val highlight = flows[14] as Boolean
        val rotateScreen = flows[15] as Boolean
        val keepScreenOn = flows[16] as Boolean
        val widgetTheme = flows[17] as String
        val calculateOnFly = flows[18] as Boolean
        val rpnMode = flows[19] as Boolean
        val tapeMode = flows[20] as Boolean
        val showReleaseNotes = flows[21] as Boolean
        val showCalculationLatency = flows[22] as Boolean
        val useBackAsPrevious = flows[23] as Boolean
        val plotImag = flows[24] as Boolean
        val latexMode = flows[25] as Boolean
        val bitwiseWordSize = flows[26] as Int
        val bitwiseSigned = flows[27] as Boolean
        val reduceMotion = flows[28] as Boolean
        val fontScale = flows[29] as Float
        val hapticOnRelease = flows[30] as Boolean
        val gestureAutoActivation = flows[31] as Boolean
        val bottomRightEqualsKey = flows[32] as Boolean
        SettingsUiState(
            mode = modeFromStr(mode),
            angleUnit = angleFromInt(angleUnit),
            numeralBase = numeralBaseFromInt(numeralBase),
            outputNotation = outputNotationFromInt(outputNotation),
            outputPrecision = precision.coerceIn(0, 10),
            outputSeparator = outputSeparatorFromChar(outputSeparator),
            multiplicationSign = multiplicationSignFromString(multiplicationSign),
            appearanceMode = appearanceModeFromTheme(theme),
            theme = themeFromStr(theme),
            dynamicColorEnabled = dynamicColor,
            themeSeedColor = themeSeed,
            isAmoledTheme = isAmoled,
            languageCode = language,
            vibrateOnKeypress = vibrate,
            highContrast = highContrast,
            highlightExpressions = highlight,
            rotateScreen = rotateScreen,
            keepScreenOn = keepScreenOn,
            widgetTheme = widgetThemeFromStr(widgetTheme),
            calculateOnFly = calculateOnFly,
            rpnMode = rpnMode,
            tapeMode = tapeMode,
            showReleaseNotes = showReleaseNotes,
            showCalculationLatency = showCalculationLatency,
            useBackAsPrevious = useBackAsPrevious,
            plotImag = plotImag,
            latexMode = latexMode,
            bitwiseWordSize = bitwiseWordSize,
            bitwiseSigned = bitwiseSigned,
            reduceMotion = reduceMotion,
            fontScale = fontScale,
            hapticOnRelease = hapticOnRelease,
            gestureAutoActivation = gestureAutoActivation,
            bottomRightEqualsKey = bottomRightEqualsKey
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    // --- Actions ---

    override fun setMode(mode: CalculatorMode) {
        val modeStr = when(mode) {
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

    override fun setHapticOnRelease(enabled: Boolean) {
        viewModelScope.launch { preferences.haptics.setHapticOnReleaseEnabled(enabled) }
    }

    override fun setGestureAutoActivation(enabled: Boolean) {
        viewModelScope.launch { preferences.gestures.setGestureAutoActivationEnabled(enabled) }
    }

    override fun setBottomRightEqualsKey(enabled: Boolean) {
        viewModelScope.launch { preferences.gestures.setBottomRightEqualsEnabled(enabled) }
    }

    // --- Mappers ---

    private fun modeFromStr(str: String): CalculatorMode = when(str) {
        "engineer" -> CalculatorMode.ENGINEER
        "modern" -> CalculatorMode.MODERN
        else -> CalculatorMode.MODERN
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
        "material_dark", "material_black" -> AppearanceMode.DARK
        else -> AppearanceMode.SYSTEM
    }

    private fun angleFromInt(value: Int): AngleUnit = when(value) {
        0 -> AngleUnit.DEG
        1 -> AngleUnit.RAD
        2 -> AngleUnit.GRAD
        3 -> AngleUnit.TURNS
        else -> AngleUnit.DEG
    }

    private fun numeralBaseFromInt(value: Int): NumeralBase = when (value) {
        1 -> NumeralBase.HEX
        2 -> NumeralBase.OCT
        3 -> NumeralBase.BIN
        else -> NumeralBase.DEC
    }

    private fun outputNotationFromInt(value: Int): OutputNotation = when (value) {
        1 -> OutputNotation.ENGINEERING
        2 -> OutputNotation.SCIENTIFIC
        else -> OutputNotation.PLAIN
    }

    private fun outputSeparatorFromChar(value: Char): OutputSeparator = when (value) {
        '\u0000' -> OutputSeparator.NONE
        ' ' -> OutputSeparator.SPACE
        ',' -> OutputSeparator.COMMA
        '.' -> OutputSeparator.DOT
        '_' -> OutputSeparator.UNDERSCORE
        else -> OutputSeparator.SPACE
    }

    private fun multiplicationSignFromString(value: String): MultiplicationSign = when (value) {
        "·" -> MultiplicationSign.DOT
        "*" -> MultiplicationSign.STAR
        else -> MultiplicationSign.CROSS
    }

    private fun widgetThemeFromStr(value: String): SimpleTheme = when (value) {
        "material_dark" -> SimpleTheme.MATERIAL_DARK
        "material_light" -> SimpleTheme.MATERIAL_LIGHT
        "metro_blue" -> SimpleTheme.METRO_BLUE
        else -> SimpleTheme.DEFAULT
    }

    private fun widgetThemeToStr(theme: SimpleTheme): String = when (theme) {
        SimpleTheme.DEFAULT -> "default_theme"
        SimpleTheme.MATERIAL_DARK -> "material_dark"
        SimpleTheme.MATERIAL_LIGHT -> "material_light"
        SimpleTheme.METRO_BLUE -> "metro_blue"
    }

    override fun setNumeralBase(base: NumeralBase) {
        val value = when (base) {
            NumeralBase.DEC -> 0
            NumeralBase.HEX -> 1
            NumeralBase.OCT -> 2
            NumeralBase.BIN -> 3
        }
        viewModelScope.launch { preferences.settings.setNumeralBase(value) }
    }

    override fun setOutputNotation(notation: OutputNotation) {
        val value = when (notation) {
            OutputNotation.PLAIN -> 0
            OutputNotation.ENGINEERING -> 1
            OutputNotation.SCIENTIFIC -> 2
        }
        viewModelScope.launch { preferences.settings.setOutputNotation(value) }
    }

    override fun setOutputSeparator(separator: Char) {
        viewModelScope.launch { preferences.settings.setOutputSeparator(separator) }
    }

    override fun setOutputSeparator(separator: OutputSeparator) {
        setOutputSeparator(separator.symbol)
    }

    override fun setMultiplicationSign(sign: MultiplicationSign) {
        viewModelScope.launch { preferences.settings.setMultiplicationSign(sign.symbol) }
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

    override fun setHighContrast(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setHighContrast(enabled) }
    }

    override fun setRotateScreen(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setRotateScreen(enabled) }
    }

    override fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { preferences.gui.setKeepScreenOn(enabled) }
    }

    override fun setWidgetTheme(theme: SimpleTheme) {
        viewModelScope.launch { preferences.widget.setTheme(widgetThemeToStr(theme)) }
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

    override fun setReduceMotion(enabled: Boolean) {
        reduceMotionState.value = enabled
    }

    override fun setFontScale(scale: Float) {
        fontScaleState.value = scale.coerceIn(0.8f, 1.6f)
    }

    override fun setBitwiseWordSize(size: Int) {
        viewModelScope.launch { preferences.settings.setBitwiseWordSize(size) }
    }

    override fun setBitwiseSigned(signed: Boolean) {
        viewModelScope.launch { preferences.settings.setBitwiseSigned(signed) }
    }
}
