package org.solovyev.android.calculator.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.NumeralBase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.language.Language
import org.solovyev.android.calculator.language.Languages
import kotlin.math.pow
import kotlin.math.sqrt
import javax.inject.Inject

data class SettingsUiState(
    val mode: Preferences.Gui.Mode,
    val angleUnit: AngleUnit,
    val numeralBase: NumeralBase,
    val theme: Preferences.Gui.Theme,
    val languageCode: String,
    val vibrateOnKeypress: Boolean,
    val highContrast: Boolean,
    val highlightExpressions: Boolean,
    val rotateScreen: Boolean,
    val keepScreenOn: Boolean,
    val calculateOnFly: Boolean,
    val showReleaseNotes: Boolean,
    val useBackAsPrevious: Boolean,
    val onscreenShowAppIcon: Boolean,
    val onscreenTheme: Preferences.SimpleTheme,
    val widgetTheme: Preferences.SimpleTheme,
    val outputNotation: Engine.Notation,
    val outputPrecision: Int,
    val outputSeparator: Char,
    val numberFormatExamples: String
)

@HiltViewModel
class SettingsComposeViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val languages: Languages,
    private val engine: JsclMathEngine
) : ViewModel() {

    val availableLanguages: List<Language> = languages.getList()

    private val mainFlow = combine(
        appPreferences.settings.mode,
        appPreferences.settings.angleUnit,
        appPreferences.settings.numeralBase,
        appPreferences.settings.theme,
        appPreferences.settings.language
    ) { mode, angleUnit, numeralBase, theme, languageCode ->
        MainSnapshot(mode, angleUnit, numeralBase, theme, languageCode)
    }

    private val toggleFlow = combine(
        appPreferences.settings.vibrateOnKeypress,
        appPreferences.settings.highContrast,
        appPreferences.settings.highlightExpressions,
        appPreferences.settings.rotateScreen,
        appPreferences.settings.keepScreenOn,
        appPreferences.settings.calculateOnFly
    ) { values ->
        ToggleSnapshot(
            values[0],
            values[1],
            values[2],
            values[3],
            values[4],
            values[5]
        )
    }

    private val uiFlow = combine(
        appPreferences.settings.showReleaseNotes,
        appPreferences.settings.useBackAsPrevious,
        appPreferences.settings.onscreenShowAppIcon,
        appPreferences.settings.onscreenTheme,
        appPreferences.settings.widgetTheme
    ) { showReleaseNotes, useBackAsPrevious, onscreenShowAppIcon, onscreenTheme, widgetTheme ->
        UiSnapshot(showReleaseNotes, useBackAsPrevious, onscreenShowAppIcon, onscreenTheme, widgetTheme)
    }

    private val outputFlow = combine(
        appPreferences.settings.outputNotation,
        appPreferences.settings.outputPrecision,
        appPreferences.settings.outputSeparator
    ) { outputNotation, outputPrecision, outputSeparator ->
        OutputSnapshot(outputNotation, outputPrecision, outputSeparator)
    }

    val state: StateFlow<SettingsUiState> = combine(
        mainFlow,
        toggleFlow,
        uiFlow,
        outputFlow
    ) { main, toggles, ui, output ->
        SettingsUiState(
            mode = main.mode,
            angleUnit = main.angleUnit,
            numeralBase = main.numeralBase,
            theme = main.theme,
            languageCode = main.languageCode,
            vibrateOnKeypress = toggles.vibrateOnKeypress,
            highContrast = toggles.highContrast,
            highlightExpressions = toggles.highlightExpressions,
            rotateScreen = toggles.rotateScreen,
            keepScreenOn = toggles.keepScreenOn,
            calculateOnFly = toggles.calculateOnFly,
            showReleaseNotes = ui.showReleaseNotes,
            useBackAsPrevious = ui.useBackAsPrevious,
            onscreenShowAppIcon = ui.onscreenShowAppIcon,
            onscreenTheme = ui.onscreenTheme,
            widgetTheme = ui.widgetTheme,
            outputNotation = output.outputNotation,
            outputPrecision = output.outputPrecision,
            outputSeparator = output.outputSeparator,
            numberFormatExamples = buildExamples()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState(
            mode = Preferences.Gui.Mode.simple,
            angleUnit = AngleUnit.deg,
            numeralBase = NumeralBase.dec,
            theme = Preferences.Gui.Theme.material_theme,
            languageCode = Languages.SYSTEM_LANGUAGE_CODE,
            vibrateOnKeypress = true,
            highContrast = false,
            highlightExpressions = true,
            rotateScreen = true,
            keepScreenOn = true,
            calculateOnFly = true,
            showReleaseNotes = true,
            useBackAsPrevious = false,
            onscreenShowAppIcon = true,
            onscreenTheme = Preferences.SimpleTheme.default_theme,
            widgetTheme = Preferences.SimpleTheme.default_theme,
            outputNotation = Engine.Notation.dec,
            outputPrecision = 5,
            outputSeparator = jscl.JsclMathEngine.GROUPING_SEPARATOR_DEFAULT,
            numberFormatExamples = ""
        )
    )

    private data class MainSnapshot(
        val mode: Preferences.Gui.Mode,
        val angleUnit: AngleUnit,
        val numeralBase: NumeralBase,
        val theme: Preferences.Gui.Theme,
        val languageCode: String
    )

    private data class ToggleSnapshot(
        val vibrateOnKeypress: Boolean,
        val highContrast: Boolean,
        val highlightExpressions: Boolean,
        val rotateScreen: Boolean,
        val keepScreenOn: Boolean,
        val calculateOnFly: Boolean
    )

    private data class UiSnapshot(
        val showReleaseNotes: Boolean,
        val useBackAsPrevious: Boolean,
        val onscreenShowAppIcon: Boolean,
        val onscreenTheme: Preferences.SimpleTheme,
        val widgetTheme: Preferences.SimpleTheme
    )

    private data class OutputSnapshot(
        val outputNotation: Engine.Notation,
        val outputPrecision: Int,
        val outputSeparator: Char
    )

    fun setMode(mode: Preferences.Gui.Mode) = viewModelScope.launch {
        appPreferences.settings.setMode(mode)
    }

    fun setAngleUnit(unit: AngleUnit) = viewModelScope.launch {
        appPreferences.settings.setAngleUnit(unit)
    }

    fun setNumeralBase(base: NumeralBase) = viewModelScope.launch {
        appPreferences.settings.setNumeralBase(base)
    }

    fun setTheme(theme: Preferences.Gui.Theme) = viewModelScope.launch {
        appPreferences.settings.setTheme(theme)
    }

    fun setLanguage(code: String) = viewModelScope.launch {
        appPreferences.settings.setLanguage(code)
    }

    fun setVibrateOnKeypress(enabled: Boolean) = viewModelScope.launch {
        appPreferences.settings.setVibrateOnKeypress(enabled)
    }

    fun setHighContrast(enabled: Boolean) = viewModelScope.launch {
        appPreferences.settings.setHighContrast(enabled)
    }

    fun setHighlightExpressions(enabled: Boolean) = viewModelScope.launch {
        appPreferences.settings.setHighlightExpressions(enabled)
    }

    fun setRotateScreen(enabled: Boolean) = viewModelScope.launch {
        appPreferences.settings.setRotateScreen(enabled)
    }

    fun setKeepScreenOn(enabled: Boolean) = viewModelScope.launch {
        appPreferences.settings.setKeepScreenOn(enabled)
    }

    fun setCalculateOnFly(enabled: Boolean) = viewModelScope.launch {
        appPreferences.settings.setCalculateOnFly(enabled)
    }

    fun setShowReleaseNotes(enabled: Boolean) = viewModelScope.launch {
        appPreferences.settings.setShowReleaseNotes(enabled)
    }

    fun setUseBackAsPrevious(enabled: Boolean) = viewModelScope.launch {
        appPreferences.settings.setUseBackAsPrevious(enabled)
    }

    fun setOnscreenShowAppIcon(enabled: Boolean) = viewModelScope.launch {
        appPreferences.settings.setOnscreenShowAppIcon(enabled)
    }

    fun setOnscreenTheme(theme: Preferences.SimpleTheme) = viewModelScope.launch {
        appPreferences.settings.setOnscreenTheme(theme)
    }

    fun setWidgetTheme(theme: Preferences.SimpleTheme) = viewModelScope.launch {
        appPreferences.settings.setWidgetTheme(theme)
    }

    fun setOutputNotation(notation: Engine.Notation) = viewModelScope.launch {
        appPreferences.settings.setOutputNotation(notation)
    }

    fun setOutputPrecision(precision: Int) = viewModelScope.launch {
        appPreferences.settings.setOutputPrecision(precision)
    }

    fun setOutputSeparator(separator: Char) = viewModelScope.launch {
        appPreferences.settings.setOutputSeparator(separator)
    }

    private fun buildExamples(): String = buildString {
        append("     1/3 = ").append(engine.format(1.0 / 3)).append("\n")
        append("      √2 = ").append(engine.format(sqrt(2.0))).append("\n")
        append("\n")
        append("    1000 = ").append(engine.format(1000.0)).append("\n")
        append(" 1000000 = ").append(engine.format(1000000.0)).append("\n")
        append("   11^10 = ").append(engine.format(11.0.pow(10.0))).append("\n")
        append("   10^24 = ").append(engine.format(10.0.pow(24.0))).append("\n")
        append("\n")
        append("   0.001 = ").append(engine.format(0.001)).append("\n")
        append("0.000001 = ").append(engine.format(0.000001)).append("\n")
        append("  11^−10 = ").append(engine.format(11.0.pow(-10.0))).append("\n")
        append("  10^−24 = ").append(engine.format(10.0.pow(-24.0)))
    }
}
