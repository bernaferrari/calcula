package org.solovyev.android.calculator.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import org.solovyev.android.calculator.AppPreferences
import org.solovyev.android.calculator.ConverterPreferences
import org.solovyev.android.calculator.GuiPreferences
import org.solovyev.android.calculator.OnscreenPreferences
import org.solovyev.android.calculator.SettingsPreferences
import org.solovyev.android.calculator.UiPreferences
import org.solovyev.android.calculator.WidgetPreferences
import org.solovyev.android.calculator.WizardPreferences

class DataStoreAppPreferences(private val dataStore: DataStore<Preferences>) : AppPreferences {
    override val settings: SettingsPreferences = DataStoreSettingsPreferences(dataStore)
    override val gui: GuiPreferences = DataStoreGuiPreferences(dataStore)
    override val onscreen: OnscreenPreferences = DataStoreOnscreenPreferences(dataStore)
    override val widget: WidgetPreferences = DataStoreWidgetPreferences(dataStore)
    override val converter: ConverterPreferences = DataStoreConverterPreferences(dataStore)
    override val ui: UiPreferences = DataStoreUiPreferences(dataStore)
    override val wizard: WizardPreferences = DataStoreWizardPreferences(dataStore)
}

class DataStoreUiPreferences(private val dataStore: DataStore<Preferences>) : UiPreferences {
    private val keyShowFixableErrorDialog = booleanPreferencesKey("ui.showFixableErrorDialog")

    override val showFixableErrorDialog: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[keyShowFixableErrorDialog] ?: true
    }

    override fun getShowFixableErrorDialogBlocking(): Boolean {
        // TODO: Refactor consumers to use Flow. Returning default true for now.
        return true
    }
    
    suspend fun setShowFixableErrorDialog(value: Boolean) {
        dataStore.edit { it[keyShowFixableErrorDialog] = value }
    }
}

class DataStoreSettingsPreferences(private val dataStore: DataStore<Preferences>) : SettingsPreferences {
    private val keyCalculateOnFly = booleanPreferencesKey("settings.calculateOnFly")
    private val keyAngleUnit = intPreferencesKey("settings.angleUnit")
    private val keyNumeralBase = intPreferencesKey("settings.numeralBase")
    private val keyOutputPrecision = intPreferencesKey("settings.outputPrecision")
    private val keyOutputNotation = intPreferencesKey("settings.outputNotation")
    private val keyOutputSeparator = intPreferencesKey("settings.outputSeparator")
    private val keyMultiplicationSign = stringPreferencesKey("settings.multiplicationSign")

    override val calculateOnFly: Flow<Boolean> = dataStore.data.map { it[keyCalculateOnFly] ?: true }
    override val angleUnit: Flow<Int> = dataStore.data.map { it[keyAngleUnit] ?: 0 }
    override val numeralBase: Flow<Int> = dataStore.data.map { it[keyNumeralBase] ?: 0 }
    override val outputPrecision: Flow<Int> = dataStore.data.map { it[keyOutputPrecision] ?: 5 }
    override val outputNotation: Flow<Int> = dataStore.data.map { it[keyOutputNotation] ?: 0 }
    override val outputSeparator: Flow<Char> = dataStore.data.map {
        (it[keyOutputSeparator] ?: ' '.code).toChar()
    }
    override val multiplicationSign: Flow<String> = dataStore.data.map { it[keyMultiplicationSign] ?: "×" }

    override fun getCalculateOnFlyBlocking(): Boolean {
        return true
    }

    override suspend fun setCalculateOnFly(value: Boolean) {
        dataStore.edit { it[keyCalculateOnFly] = value }
    }
    override suspend fun setAngleUnit(value: Int) {
        dataStore.edit { it[keyAngleUnit] = value }
    }
    override suspend fun setNumeralBase(value: Int) {
        dataStore.edit { it[keyNumeralBase] = value }
    }
    override suspend fun setOutputPrecision(value: Int) {
        dataStore.edit { it[keyOutputPrecision] = value }
    }
    override suspend fun setOutputNotation(value: Int) {
        dataStore.edit { it[keyOutputNotation] = value }
    }
    override suspend fun setOutputSeparator(value: Char) {
        dataStore.edit { it[keyOutputSeparator] = value.code }
    }
    override suspend fun setMultiplicationSign(value: String) {
        dataStore.edit { it[keyMultiplicationSign] = value }
    }
}

class DataStoreGuiPreferences(private val dataStore: DataStore<Preferences>) : GuiPreferences {
    private val keyTheme = stringPreferencesKey("gui.theme")
    private val keyMode = stringPreferencesKey("gui.mode")
    private val keyLanguage = stringPreferencesKey("gui.language")
    private val keyShowReleaseNotes = booleanPreferencesKey("gui.showReleaseNotes")
    private val keyUseBackAsPrevious = booleanPreferencesKey("gui.useBackAsPrevious")
    private val keyRotateScreen = booleanPreferencesKey("gui.rotateScreen")
    private val keyKeepScreenOn = booleanPreferencesKey("gui.keepScreenOn")
    private val keyHighContrast = booleanPreferencesKey("gui.highContrast")
    private val keyVibrateOnKeypress = booleanPreferencesKey("gui.vibrateOnKeypress")
    private val keyLatexMode = booleanPreferencesKey("gui.latexMode")
    private val keyThemeSeed = intPreferencesKey("gui.themeSeed")
    private val keyIsAmoled = booleanPreferencesKey("gui.isAmoled")

    private val keyHighlightExpressions = booleanPreferencesKey("gui.highlightExpressions")
    private val keyPlotImag = booleanPreferencesKey("gui.plotImag")

    override val theme: Flow<String> = dataStore.data.map { it[keyTheme] ?: "material_theme" }
    override val mode: Flow<String> = dataStore.data.map { it[keyMode] ?: "simple" }
    override val language: Flow<String> = dataStore.data.map { it[keyLanguage] ?: "system" }
    override val showReleaseNotes: Flow<Boolean> = dataStore.data.map { it[keyShowReleaseNotes] ?: true }
    override val useBackAsPrevious: Flow<Boolean> = dataStore.data.map { it[keyUseBackAsPrevious] ?: false }
    override val rotateScreen: Flow<Boolean> = dataStore.data.map { it[keyRotateScreen] ?: true }
    override val keepScreenOn: Flow<Boolean> = dataStore.data.map { it[keyKeepScreenOn] ?: true }
    override val highContrast: Flow<Boolean> = dataStore.data.map { it[keyHighContrast] ?: false }
    override val vibrateOnKeypress: Flow<Boolean> = dataStore.data.map { it[keyVibrateOnKeypress] ?: true }
    override val latexMode: Flow<Boolean> = dataStore.data.map { it[keyLatexMode] ?: false }
    override val themeSeed: Flow<Int> = dataStore.data.map { it[keyThemeSeed] ?: 0xFF13ABF1.toInt() }
    override val isAmoled: Flow<Boolean> = dataStore.data.map { it[keyIsAmoled] ?: false }
    override val highlightExpressions: Flow<Boolean> = dataStore.data.map { it[keyHighlightExpressions] ?: true }
    override val plotImag: Flow<Boolean> = dataStore.data.map { it[keyPlotImag] ?: false }

    override suspend fun setTheme(value: String) { dataStore.edit { it[keyTheme] = value } }
    override suspend fun setMode(value: String) { dataStore.edit { it[keyMode] = value } }
    override suspend fun setLanguage(value: String) { dataStore.edit { it[keyLanguage] = value } }
    override suspend fun setShowReleaseNotes(value: Boolean) { dataStore.edit { it[keyShowReleaseNotes] = value } }
    override suspend fun setUseBackAsPrevious(value: Boolean) { dataStore.edit { it[keyUseBackAsPrevious] = value } }
    override suspend fun setRotateScreen(value: Boolean) { dataStore.edit { it[keyRotateScreen] = value } }
    override suspend fun setKeepScreenOn(value: Boolean) { dataStore.edit { it[keyKeepScreenOn] = value } }
    override suspend fun setHighContrast(value: Boolean) { dataStore.edit { it[keyHighContrast] = value } }
    override suspend fun setVibrateOnKeypress(value: Boolean) { dataStore.edit { it[keyVibrateOnKeypress] = value } }
    override suspend fun setLatexMode(value: Boolean) { dataStore.edit { it[keyLatexMode] = value } }
    override suspend fun setThemeSeed(value: Int) { dataStore.edit { it[keyThemeSeed] = value } }
    override suspend fun setIsAmoled(value: Boolean) { dataStore.edit { it[keyIsAmoled] = value } }
    override suspend fun setHighlightExpressions(value: Boolean) { dataStore.edit { it[keyHighlightExpressions] = value } }
    override suspend fun setPlotImag(value: Boolean) { dataStore.edit { it[keyPlotImag] = value } }
}

class DataStoreOnscreenPreferences(private val dataStore: DataStore<Preferences>) : OnscreenPreferences {
    private val keyShowAppIcon = booleanPreferencesKey("onscreen_show_app_icon")
    private val keyTheme = stringPreferencesKey("onscreen.theme")
    private val keyStartOnBoot = booleanPreferencesKey("onscreen_start_on_boot")

    override val showAppIcon: Flow<Boolean> = dataStore.data.map { it[keyShowAppIcon] ?: true }
    override val theme: Flow<String> = dataStore.data.map { it[keyTheme] ?: "default_theme" }
    override val startOnBoot: Flow<Boolean> = dataStore.data.map { it[keyStartOnBoot] ?: false }

    override suspend fun setShowAppIcon(value: Boolean) { dataStore.edit { it[keyShowAppIcon] = value } }
    override suspend fun setTheme(value: String) { dataStore.edit { it[keyTheme] = value } }
    override suspend fun setStartOnBoot(value: Boolean) { dataStore.edit { it[keyStartOnBoot] = value } }
}

class DataStoreWidgetPreferences(private val dataStore: DataStore<Preferences>) : WidgetPreferences {
    private val keyTheme = stringPreferencesKey("widget.theme")
    override val theme: Flow<String> = dataStore.data.map { it[keyTheme] ?: "default_theme" }
    override suspend fun setTheme(value: String) { dataStore.edit { it[keyTheme] = value } }
}

class DataStoreConverterPreferences(private val dataStore: DataStore<Preferences>) : ConverterPreferences {
    private val keyLastDimension = intPreferencesKey("converter.lastDimension")
    private val keyLastUnitsFrom = intPreferencesKey("converter.lastUnitsFrom")
    private val keyLastUnitsTo = intPreferencesKey("converter.lastUnitsTo")

    override val lastDimension: Flow<Int> = dataStore.data.map { it[keyLastDimension] ?: -1 }
    override val lastUnitsFrom: Flow<Int> = dataStore.data.map { it[keyLastUnitsFrom] ?: -1 }
    override val lastUnitsTo: Flow<Int> = dataStore.data.map { it[keyLastUnitsTo] ?: -1 }

    override suspend fun getLastDimension(): Int? =
        dataStore.data.map { it[keyLastDimension] }.firstOrNull()?.takeIf { it >= 0 }

    override suspend fun getLastUnitsFrom(): Int? =
        dataStore.data.map { it[keyLastUnitsFrom] }.firstOrNull()?.takeIf { it >= 0 }

    override suspend fun getLastUnitsTo(): Int? =
        dataStore.data.map { it[keyLastUnitsTo] }.firstOrNull()?.takeIf { it >= 0 }

    override suspend fun setLastUsed(dimension: Int, from: Int, to: Int) {
        dataStore.edit {
            it[keyLastDimension] = dimension
            it[keyLastUnitsFrom] = from
            it[keyLastUnitsTo] = to
        }
    }
}

class DataStoreWizardPreferences(private val dataStore: DataStore<Preferences>) : WizardPreferences {
    private val keyFinished = booleanPreferencesKey("wizard.finished")
    override val finished: Flow<Boolean> = dataStore.data.map { it[keyFinished] ?: false }
    override suspend fun setFinished(value: Boolean) {
        dataStore.edit { it[keyFinished] = value }
    }
}
