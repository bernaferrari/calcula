package org.solovyev.android.calculator.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import jscl.AngleUnit
import jscl.NumeralBase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Preferences as LegacyPreferences
import org.solovyev.android.calculator.language.Languages

class SettingsPreferencesStore(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val MODE = stringPreferencesKey("gui.mode")
        private val ANGLE_UNIT = stringPreferencesKey("engine.angleUnit")
        private val NUMERAL_BASE = stringPreferencesKey("engine.numeralBase")
        private val THEME = stringPreferencesKey("gui.theme")
        private val LANGUAGE = stringPreferencesKey("gui.language")
        private val VIBRATE = booleanPreferencesKey("gui.vibrateOnKeypress")
        private val HIGH_CONTRAST = booleanPreferencesKey("gui.highContrast")
        private val HIGHLIGHT_EXPRESSIONS = booleanPreferencesKey(
            "org.solovyev.android.calculator.CalculatorModel_color_display"
        )
        private val ROTATE_SCREEN = booleanPreferencesKey("gui.rotateScreen")
        private val KEEP_SCREEN_ON = booleanPreferencesKey("gui.keepScreenOn")
        private val CALCULATE_ON_FLY = booleanPreferencesKey("calculations_calculate_on_fly")
        private val SHOW_RELEASE_NOTES = booleanPreferencesKey("gui.showReleaseNotes")
        private val USE_BACK_AS_PREVIOUS = booleanPreferencesKey("gui.useBackAsPrevious")

        private val ONSCREEN_SHOW_APP_ICON = booleanPreferencesKey("onscreen_show_app_icon")
        private val ONSCREEN_THEME = stringPreferencesKey("onscreen.theme")
        private val WIDGET_THEME = stringPreferencesKey("widget.theme")

        private val OUTPUT_NOTATION = stringPreferencesKey("engine.output.notation")
        private val OUTPUT_PRECISION = intPreferencesKey("engine.output.precision")
        private val OUTPUT_SEPARATOR = stringPreferencesKey("engine.output.separator")
        private val MULTIPLICATION_SIGN = stringPreferencesKey("engine.multiplicationSign")
        private val PLOT_IMAG = booleanPreferencesKey("graph_plot_imag")
    }

    val mode: Flow<LegacyPreferences.Gui.Mode> = dataStore.data.map { prefs ->
        prefs[MODE]?.let { LegacyPreferences.Gui.Mode.valueOf(it) }
            ?: LegacyPreferences.Gui.Mode.simple
    }

    val angleUnit: Flow<AngleUnit> = dataStore.data.map { prefs ->
        prefs[ANGLE_UNIT]?.let { AngleUnit.valueOf(it) } ?: AngleUnit.deg
    }

    val numeralBase: Flow<NumeralBase> = dataStore.data.map { prefs ->
        prefs[NUMERAL_BASE]?.let { NumeralBase.valueOf(it) } ?: NumeralBase.dec
    }

    val theme: Flow<LegacyPreferences.Gui.Theme> = dataStore.data.map { prefs ->
        prefs[THEME]?.let { LegacyPreferences.Gui.Theme.valueOf(it) }
            ?: LegacyPreferences.Gui.Theme.material_theme
    }

    val language: Flow<String> = dataStore.data.map { prefs ->
        prefs[LANGUAGE] ?: Languages.SYSTEM_LANGUAGE_CODE
    }

    val vibrateOnKeypress: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[VIBRATE] ?: true
    }

    val highContrast: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[HIGH_CONTRAST] ?: false
    }

    val highlightExpressions: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[HIGHLIGHT_EXPRESSIONS] ?: true
    }

    val rotateScreen: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ROTATE_SCREEN] ?: true
    }

    val keepScreenOn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEEP_SCREEN_ON] ?: true
    }

    val calculateOnFly: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[CALCULATE_ON_FLY] ?: true
    }

    val showReleaseNotes: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SHOW_RELEASE_NOTES] ?: true
    }

    val useBackAsPrevious: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[USE_BACK_AS_PREVIOUS] ?: false
    }

    val onscreenShowAppIcon: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ONSCREEN_SHOW_APP_ICON] ?: true
    }

    val onscreenTheme: Flow<LegacyPreferences.SimpleTheme> = dataStore.data.map { prefs ->
        prefs[ONSCREEN_THEME]?.let { LegacyPreferences.SimpleTheme.valueOf(it) }
            ?: LegacyPreferences.SimpleTheme.default_theme
    }

    val widgetTheme: Flow<LegacyPreferences.SimpleTheme> = dataStore.data.map { prefs ->
        prefs[WIDGET_THEME]?.let { LegacyPreferences.SimpleTheme.valueOf(it) }
            ?: LegacyPreferences.SimpleTheme.default_theme
    }

    val outputNotation: Flow<Engine.Notation> = dataStore.data.map { prefs ->
        prefs[OUTPUT_NOTATION]?.let { Engine.Notation.valueOf(it) }
            ?: Engine.Notation.dec
    }

    val outputPrecision: Flow<Int> = dataStore.data.map { prefs ->
        prefs[OUTPUT_PRECISION] ?: 5
    }

    val outputSeparator: Flow<Char> = dataStore.data.map { prefs ->
        val separator = prefs[OUTPUT_SEPARATOR]
        separator?.firstOrNull() ?: jscl.JsclMathEngine.GROUPING_SEPARATOR_DEFAULT
    }

    val multiplicationSign: Flow<String> = dataStore.data.map { prefs ->
        prefs[MULTIPLICATION_SIGN] ?: (Engine.Preferences.multiplicationSign.defaultValue ?: "×")
    }

    val plotImag: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PLOT_IMAG] ?: false
    }

    fun getModeBlocking(): LegacyPreferences.Gui.Mode = runBlocking { mode.first() }

    fun getAngleUnitBlocking(): AngleUnit = runBlocking { angleUnit.first() }

    fun getNumeralBaseBlocking(): NumeralBase = runBlocking { numeralBase.first() }

    fun getThemeBlocking(): LegacyPreferences.Gui.Theme = runBlocking { theme.first() }

    fun getLanguageBlocking(): String = runBlocking { language.first() }

    fun getUseBackAsPreviousBlocking(): Boolean = runBlocking { useBackAsPrevious.first() }

    fun getOnscreenShowAppIconBlocking(): Boolean = runBlocking { onscreenShowAppIcon.first() }

    fun getShowReleaseNotesBlocking(): Boolean = runBlocking { showReleaseNotes.first() }

    fun getRotateScreenBlocking(): Boolean = runBlocking { rotateScreen.first() }

    fun getKeepScreenOnBlocking(): Boolean = runBlocking { keepScreenOn.first() }

    fun getCalculateOnFlyBlocking(): Boolean = runBlocking { calculateOnFly.first() }

    fun vibrateOnKeypressBlocking(): Boolean = runBlocking { vibrateOnKeypress.first() }

    fun getHighContrastBlocking(): Boolean = runBlocking { highContrast.first() }

    fun getHighlightExpressionsBlocking(): Boolean = runBlocking { highlightExpressions.first() }

    fun getOnscreenThemeBlocking(): LegacyPreferences.SimpleTheme =
        runBlocking { onscreenTheme.first() }

    fun getWidgetThemeBlocking(): LegacyPreferences.SimpleTheme =
        runBlocking { widgetTheme.first() }

    fun getOutputSeparatorBlocking(): Char =
        runBlocking { outputSeparator.first() }

    fun getPlotImagBlocking(): Boolean =
        runBlocking { plotImag.first() }

    suspend fun setMode(mode: LegacyPreferences.Gui.Mode) {
        dataStore.edit { it[MODE] = mode.name }
    }

    suspend fun setAngleUnit(unit: AngleUnit) {
        dataStore.edit { it[ANGLE_UNIT] = unit.name }
    }

    suspend fun setNumeralBase(base: NumeralBase) {
        dataStore.edit { it[NUMERAL_BASE] = base.name }
    }

    suspend fun setTheme(theme: LegacyPreferences.Gui.Theme) {
        dataStore.edit { it[THEME] = theme.name }
    }

    suspend fun setLanguage(code: String) {
        dataStore.edit { it[LANGUAGE] = code }
    }

    suspend fun setVibrateOnKeypress(enabled: Boolean) {
        dataStore.edit { it[VIBRATE] = enabled }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        dataStore.edit { it[HIGH_CONTRAST] = enabled }
    }

    suspend fun setHighlightExpressions(enabled: Boolean) {
        dataStore.edit { it[HIGHLIGHT_EXPRESSIONS] = enabled }
    }

    suspend fun setRotateScreen(enabled: Boolean) {
        dataStore.edit { it[ROTATE_SCREEN] = enabled }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        dataStore.edit { it[KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setCalculateOnFly(enabled: Boolean) {
        dataStore.edit { it[CALCULATE_ON_FLY] = enabled }
    }

    suspend fun setShowReleaseNotes(enabled: Boolean) {
        dataStore.edit { it[SHOW_RELEASE_NOTES] = enabled }
    }

    suspend fun setUseBackAsPrevious(enabled: Boolean) {
        dataStore.edit { it[USE_BACK_AS_PREVIOUS] = enabled }
    }

    suspend fun setOnscreenShowAppIcon(enabled: Boolean) {
        dataStore.edit { it[ONSCREEN_SHOW_APP_ICON] = enabled }
    }

    suspend fun setOnscreenTheme(theme: LegacyPreferences.SimpleTheme) {
        dataStore.edit { it[ONSCREEN_THEME] = theme.name }
    }

    suspend fun setWidgetTheme(theme: LegacyPreferences.SimpleTheme) {
        dataStore.edit { it[WIDGET_THEME] = theme.name }
    }

    suspend fun setOutputNotation(notation: Engine.Notation) {
        dataStore.edit { it[OUTPUT_NOTATION] = notation.name }
    }

    suspend fun setOutputPrecision(precision: Int) {
        dataStore.edit { it[OUTPUT_PRECISION] = precision }
    }

    suspend fun setOutputSeparator(separator: Char) {
        dataStore.edit { it[OUTPUT_SEPARATOR] = separator.toString() }
    }

    suspend fun setMultiplicationSign(sign: String) {
        dataStore.edit { it[MULTIPLICATION_SIGN] = sign }
    }

    suspend fun setPlotImag(enabled: Boolean) {
        dataStore.edit { it[PLOT_IMAG] = enabled }
    }
}
