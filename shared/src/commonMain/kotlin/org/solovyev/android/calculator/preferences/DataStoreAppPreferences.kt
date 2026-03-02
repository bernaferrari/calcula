package org.solovyev.android.calculator.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.solovyev.android.calculator.AppPreferences
import org.solovyev.android.calculator.ConverterPreferences
import org.solovyev.android.calculator.GuiPreferences
import org.solovyev.android.calculator.HapticsPreferences
import org.solovyev.android.calculator.HistoryPreferences
import org.solovyev.android.calculator.OnscreenPreferences
import org.solovyev.android.calculator.SettingsPreferences
import org.solovyev.android.calculator.ThemePreferences
import org.solovyev.android.calculator.UiPreferences
import org.solovyev.android.calculator.WidgetPreferences
import org.solovyev.android.calculator.WidgetsPreferences
import org.solovyev.android.calculator.WizardPreferences
import org.solovyev.android.calculator.GesturePreferences
import org.solovyev.android.calculator.history.HistoryState
import org.solovyev.android.calculator.sound.SoundPreferences

class DataStoreAppPreferences(
    private val dataStore: DataStore<Preferences>,
    private val roomHistory: org.solovyev.android.calculator.history.RoomHistory
) : AppPreferences {
    override val settings: SettingsPreferences = DataStoreSettingsPreferences(dataStore)
    override val gui: GuiPreferences = DataStoreGuiPreferences(dataStore)
    override val onscreen: OnscreenPreferences = DataStoreOnscreenPreferences(dataStore)
    override val widget: WidgetPreferences = DataStoreWidgetPreferences(dataStore)
    override val widgets: WidgetsPreferences = DataStoreWidgetsPreferences(dataStore)
    override val converter: ConverterPreferences = DataStoreConverterPreferences(dataStore)
    override val ui: UiPreferences = DataStoreUiPreferences(dataStore)
    override val wizard: WizardPreferences = DataStoreWizardPreferences(dataStore)
    override val theme: ThemePreferences = DataStoreThemePreferences(dataStore)
    override val haptics: HapticsPreferences = DataStoreHapticsPreferences(dataStore)
    override val history: HistoryPreferences = DataStoreHistoryPreferences(roomHistory)
    override val sound: SoundPreferences = DataStoreSoundPreferences(dataStore)
    override val gestures: GesturePreferences = DataStoreGesturePreferences(dataStore)
}

class DataStoreUiPreferences(private val dataStore: DataStore<Preferences>) : UiPreferences {
    private val keyShowFixableErrorDialog = booleanPreferencesKey("ui.showFixableErrorDialog")

    override val showFixableErrorDialog: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[keyShowFixableErrorDialog] ?: true
    }

    override fun getShowFixableErrorDialogBlocking(): Boolean {
        return runBlocking {
            showFixableErrorDialog.firstOrNull() ?: true
        }
    }
    
    suspend fun setShowFixableErrorDialog(value: Boolean) {
        dataStore.edit { it[keyShowFixableErrorDialog] = value }
    }
}

class DataStoreSettingsPreferences(private val dataStore: DataStore<Preferences>) : SettingsPreferences {
    private val keyCalculateOnFly = booleanPreferencesKey("settings.calculateOnFly")
    private val keyRpnMode = booleanPreferencesKey("settings.rpnMode")
    private val keyTapeMode = booleanPreferencesKey("settings.tapeMode")
    private val keyBitwiseWordSize = intPreferencesKey("settings.bitwiseWordSize")
    private val keyBitwiseSigned = booleanPreferencesKey("settings.bitwiseSigned")
    private val keyAngleUnit = intPreferencesKey("settings.angleUnit")
    private val keyNumeralBase = intPreferencesKey("settings.numeralBase")
    private val keyOutputPrecision = intPreferencesKey("settings.outputPrecision")
    private val keyOutputNotation = intPreferencesKey("settings.outputNotation")
    private val keyOutputSeparator = intPreferencesKey("settings.outputSeparator")
    private val keyMultiplicationSign = stringPreferencesKey("settings.multiplicationSign")

    override val calculateOnFly: Flow<Boolean> = dataStore.data.map { it[keyCalculateOnFly] ?: true }
    override val rpnMode: Flow<Boolean> = dataStore.data.map { it[keyRpnMode] ?: false }
    override val tapeMode: Flow<Boolean> = dataStore.data.map { it[keyTapeMode] ?: false }
    override val bitwiseWordSize: Flow<Int> = dataStore.data.map { (it[keyBitwiseWordSize] ?: 64).coerceIn(1, 64) }
    override val bitwiseSigned: Flow<Boolean> = dataStore.data.map { it[keyBitwiseSigned] ?: true }
    override val angleUnit: Flow<Int> = dataStore.data.map { it[keyAngleUnit] ?: 0 }
    override val numeralBase: Flow<Int> = dataStore.data.map { it[keyNumeralBase] ?: 0 }
    override val outputPrecision: Flow<Int> = dataStore.data.map { it[keyOutputPrecision] ?: 5 }
    override val outputNotation: Flow<Int> = dataStore.data.map { it[keyOutputNotation] ?: 0 }
    override val outputSeparator: Flow<Char> = dataStore.data.map {
        (it[keyOutputSeparator] ?: ' '.code).toChar()
    }
    override val multiplicationSign: Flow<String> = dataStore.data.map { it[keyMultiplicationSign] ?: "×" }

    override fun getCalculateOnFlyBlocking(): Boolean {
        return runBlocking {
            calculateOnFly.firstOrNull() ?: true
        }
    }

    override suspend fun setCalculateOnFly(value: Boolean) {
        dataStore.edit { it[keyCalculateOnFly] = value }
    }
    override suspend fun setRpnMode(value: Boolean) {
        dataStore.edit { it[keyRpnMode] = value }
    }
    override suspend fun setTapeMode(value: Boolean) {
        dataStore.edit { it[keyTapeMode] = value }
    }
    override suspend fun setBitwiseWordSize(value: Int) {
        dataStore.edit { it[keyBitwiseWordSize] = value.coerceIn(1, 64) }
    }
    override suspend fun setBitwiseSigned(value: Boolean) {
        dataStore.edit { it[keyBitwiseSigned] = value }
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
    private val keyDynamicColor = booleanPreferencesKey("gui.dynamicColor")
    private val keyMode = stringPreferencesKey("gui.mode")
    private val keyLanguage = stringPreferencesKey("gui.language")
    private val keyShowReleaseNotes = booleanPreferencesKey("gui.showReleaseNotes")
    private val keyUseBackAsPrevious = booleanPreferencesKey("gui.useBackAsPrevious")
    private val keyRotateScreen = booleanPreferencesKey("gui.rotateScreen")
    private val keyKeepScreenOn = booleanPreferencesKey("gui.keepScreenOn")
    private val keyHighContrast = booleanPreferencesKey("gui.highContrast")
    private val keyReduceMotion = booleanPreferencesKey("gui.reduceMotion")
    private val keyFontScale = floatPreferencesKey("gui.fontScale")
    private val keyVibrateOnKeypress = booleanPreferencesKey("gui.vibrateOnKeypress")
    private val keyShowCalculationLatency = booleanPreferencesKey("gui.showCalculationLatency")
    private val keyLatexMode = booleanPreferencesKey("gui.latexMode")
    private val keyThemeSeed = intPreferencesKey("gui.themeSeed")
    private val keyIsAmoled = booleanPreferencesKey("gui.isAmoled")

    private val keyHighlightExpressions = booleanPreferencesKey("gui.highlightExpressions")
    private val keyPlotImag = booleanPreferencesKey("gui.plotImag")
    private val keyCalculatorTabsState = stringPreferencesKey("gui.calculatorTabsState")

    override val theme: Flow<String> = dataStore.data.map { it[keyTheme] ?: "material_theme" }
    override val dynamicColor: Flow<Boolean> = dataStore.data.map { it[keyDynamicColor] ?: true }
    override val mode: Flow<String> = dataStore.data.map { it[keyMode] ?: "simple" }
    override val language: Flow<String> = dataStore.data.map { it[keyLanguage] ?: "system" }
    override val showReleaseNotes: Flow<Boolean> = dataStore.data.map { it[keyShowReleaseNotes] ?: true }
    override val useBackAsPrevious: Flow<Boolean> = dataStore.data.map { it[keyUseBackAsPrevious] ?: false }
    override val rotateScreen: Flow<Boolean> = dataStore.data.map { it[keyRotateScreen] ?: true }
    override val keepScreenOn: Flow<Boolean> = dataStore.data.map { it[keyKeepScreenOn] ?: true }
    override val highContrast: Flow<Boolean> = dataStore.data.map { it[keyHighContrast] ?: false }
    override val reduceMotion: Flow<Boolean> = dataStore.data.map { it[keyReduceMotion] ?: false }
    override val fontScale: Flow<Float> = dataStore.data.map { (it[keyFontScale] ?: 1.0f).coerceIn(0.8f, 1.6f) }
    override val vibrateOnKeypress: Flow<Boolean> = dataStore.data.map { it[keyVibrateOnKeypress] ?: true }
    override val showCalculationLatency: Flow<Boolean> = dataStore.data.map { it[keyShowCalculationLatency] ?: false }
    override val latexMode: Flow<Boolean> = dataStore.data.map { it[keyLatexMode] ?: false }
    override val themeSeed: Flow<Int> = dataStore.data.map { it[keyThemeSeed] ?: 0xFF13ABF1.toInt() }
    override val isAmoled: Flow<Boolean> = dataStore.data.map { it[keyIsAmoled] ?: false }
    override val highlightExpressions: Flow<Boolean> = dataStore.data.map { it[keyHighlightExpressions] ?: true }
    override val plotImag: Flow<Boolean> = dataStore.data.map { it[keyPlotImag] ?: false }
    val calculatorTabsState: Flow<String?> = dataStore.data.map { it[keyCalculatorTabsState] }

    override suspend fun setTheme(value: String) { dataStore.edit { it[keyTheme] = value } }
    override suspend fun setDynamicColor(value: Boolean) { dataStore.edit { it[keyDynamicColor] = value } }
    override suspend fun setMode(value: String) { dataStore.edit { it[keyMode] = value } }
    override suspend fun setLanguage(value: String) { dataStore.edit { it[keyLanguage] = value } }
    override suspend fun setShowReleaseNotes(value: Boolean) { dataStore.edit { it[keyShowReleaseNotes] = value } }
    override suspend fun setUseBackAsPrevious(value: Boolean) { dataStore.edit { it[keyUseBackAsPrevious] = value } }
    override suspend fun setRotateScreen(value: Boolean) { dataStore.edit { it[keyRotateScreen] = value } }
    override suspend fun setKeepScreenOn(value: Boolean) { dataStore.edit { it[keyKeepScreenOn] = value } }
    override suspend fun setHighContrast(value: Boolean) { dataStore.edit { it[keyHighContrast] = value } }
    override suspend fun setReduceMotion(value: Boolean) { dataStore.edit { it[keyReduceMotion] = value } }
    override suspend fun setFontScale(value: Float) { dataStore.edit { it[keyFontScale] = value.coerceIn(0.8f, 1.6f) } }
    override suspend fun setVibrateOnKeypress(value: Boolean) { dataStore.edit { it[keyVibrateOnKeypress] = value } }
    override suspend fun setShowCalculationLatency(value: Boolean) { dataStore.edit { it[keyShowCalculationLatency] = value } }
    override suspend fun setLatexMode(value: Boolean) { dataStore.edit { it[keyLatexMode] = value } }
    override suspend fun setThemeSeed(value: Int) { dataStore.edit { it[keyThemeSeed] = value } }
    override suspend fun setIsAmoled(value: Boolean) { dataStore.edit { it[keyIsAmoled] = value } }
    override suspend fun setHighlightExpressions(value: Boolean) { dataStore.edit { it[keyHighlightExpressions] = value } }
    override suspend fun setPlotImag(value: Boolean) { dataStore.edit { it[keyPlotImag] = value } }
    suspend fun setCalculatorTabsState(value: String) { dataStore.edit { it[keyCalculatorTabsState] = value } }
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
    private val keyLastFromCurrency = stringPreferencesKey("converter.lastFromCurrency")
    private val keyLastToCurrency = stringPreferencesKey("converter.lastToCurrency")
    private val keyLastAmount = stringPreferencesKey("converter.lastAmount")

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

    override suspend fun getLastFromCurrency(): String? =
        dataStore.data.map { it[keyLastFromCurrency] }.firstOrNull() ?: "USD"

    override suspend fun getLastToCurrency(): String? =
        dataStore.data.map { it[keyLastToCurrency] }.firstOrNull() ?: "EUR"

    override suspend fun getLastAmount(): String? =
        dataStore.data.map { it[keyLastAmount] }.firstOrNull() ?: "100"

    override suspend fun setLastUsed(fromCurrency: String, toCurrency: String, amount: String) {
        dataStore.edit {
            it[keyLastFromCurrency] = fromCurrency
            it[keyLastToCurrency] = toCurrency
            it[keyLastAmount] = amount
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

class DataStoreWidgetsPreferences(private val dataStore: DataStore<Preferences>) : WidgetsPreferences {
    private val keyCalculatorOpacity = floatPreferencesKey("widgets.calculatorOpacity")
    private val keyQuickCalcOpacity = floatPreferencesKey("widgets.quickCalcOpacity")
    private val keyHistoryOpacity = floatPreferencesKey("widgets.historyOpacity")
    private val keyConverterOpacity = floatPreferencesKey("widgets.converterOpacity")
    private val keySmartStackOpacity = floatPreferencesKey("widgets.smartStackOpacity")

    override suspend fun getCalculatorOpacity(): Float = 
        dataStore.data.map { it[keyCalculatorOpacity] }.firstOrNull() ?: 1.0f
    override suspend fun getQuickCalcOpacity(): Float = 
        dataStore.data.map { it[keyQuickCalcOpacity] }.firstOrNull() ?: 1.0f
    override suspend fun getHistoryOpacity(): Float = 
        dataStore.data.map { it[keyHistoryOpacity] }.firstOrNull() ?: 1.0f
    override suspend fun getConverterOpacity(): Float = 
        dataStore.data.map { it[keyConverterOpacity] }.firstOrNull() ?: 1.0f
    override suspend fun getSmartStackOpacity(): Float = 
        dataStore.data.map { it[keySmartStackOpacity] }.firstOrNull() ?: 1.0f

    override suspend fun setCalculatorOpacity(value: Float) { 
        dataStore.edit { it[keyCalculatorOpacity] = value.coerceIn(0.3f, 1.0f) } 
    }
    override suspend fun setQuickCalcOpacity(value: Float) { 
        dataStore.edit { it[keyQuickCalcOpacity] = value.coerceIn(0.3f, 1.0f) } 
    }
    override suspend fun setHistoryOpacity(value: Float) { 
        dataStore.edit { it[keyHistoryOpacity] = value.coerceIn(0.3f, 1.0f) } 
    }
    override suspend fun setConverterOpacity(value: Float) { 
        dataStore.edit { it[keyConverterOpacity] = value.coerceIn(0.3f, 1.0f) } 
    }
    override suspend fun setSmartStackOpacity(value: Float) { 
        dataStore.edit { it[keySmartStackOpacity] = value.coerceIn(0.3f, 1.0f) } 
    }
}

class DataStoreThemePreferences(private val dataStore: DataStore<Preferences>) : ThemePreferences {
    private val keyUseDynamicColors = booleanPreferencesKey("theme.useDynamicColors")
    private val keyIsLightTheme = booleanPreferencesKey("theme.isLightTheme")

    override suspend fun useDynamicColors(): Boolean = 
        dataStore.data.map { it[keyUseDynamicColors] }.firstOrNull() ?: true
    override suspend fun isLightTheme(): Boolean = 
        dataStore.data.map { it[keyIsLightTheme] }.firstOrNull() ?: false

    override suspend fun setUseDynamicColors(value: Boolean) { 
        dataStore.edit { it[keyUseDynamicColors] = value } 
    }
    override suspend fun setLightTheme(value: Boolean) { 
        dataStore.edit { it[keyIsLightTheme] = value } 
    }
}

class DataStoreHapticsPreferences(private val dataStore: DataStore<Preferences>) : HapticsPreferences {
    private val keyEnabled = booleanPreferencesKey("haptics.enabled")
    private val keyHapticOnRelease = booleanPreferencesKey("haptics.onRelease")

    override val hapticOnReleaseEnabled: Flow<Boolean> = dataStore.data.map { it[keyHapticOnRelease] ?: true }

    override suspend fun isEnabled(): Boolean =
        dataStore.data.map { it[keyEnabled] }.firstOrNull() ?: true

    override suspend fun setEnabled(value: Boolean) {
        dataStore.edit { it[keyEnabled] = value }
    }

    override suspend fun isHapticOnReleaseEnabled(): Boolean =
        dataStore.data.map { it[keyHapticOnRelease] }.firstOrNull() ?: true

    override suspend fun setHapticOnReleaseEnabled(value: Boolean) {
        dataStore.edit { it[keyHapticOnRelease] = value }
    }
}

/**
 * HistoryPreferences implementation that delegates to RoomHistory.
 * Note: This class should be instantiated with RoomHistory from Koin.
 */
class DataStoreHistoryPreferences(
    private val roomHistory: org.solovyev.android.calculator.history.RoomHistory
) : HistoryPreferences {

    override fun observeRecent(): Flow<List<HistoryState>> =
        roomHistory.observeRecent()

    override suspend fun clearRecent() =
        roomHistory.clearRecent()
}

/**
 * DataStore implementation of SoundPreferences.
 */
class DataStoreSoundPreferences(private val dataStore: DataStore<Preferences>) : SoundPreferences {
    private val keyEnabled = booleanPreferencesKey("sound.enabled")
    private val keyIntensity = intPreferencesKey("sound.intensity")
    private val keyRespectSilentMode = booleanPreferencesKey("sound.respectSilentMode")

    override val enabled: Flow<Boolean> = dataStore.data.map { it[keyEnabled] ?: true }
    override val intensity: Flow<Int> = dataStore.data.map { (it[keyIntensity] ?: 70).coerceIn(0, 100) }
    override val respectSilentMode: Flow<Boolean> = dataStore.data.map { it[keyRespectSilentMode] ?: true }

    override suspend fun isEnabled(): Boolean =
        dataStore.data.map { it[keyEnabled] }.firstOrNull() ?: true

    override suspend fun getIntensity(): Int =
        (dataStore.data.map { it[keyIntensity] }.firstOrNull() ?: 70).coerceIn(0, 100)

    override suspend fun shouldRespectSilentMode(): Boolean =
        dataStore.data.map { it[keyRespectSilentMode] }.firstOrNull() ?: true

    override suspend fun setEnabled(value: Boolean) {
        dataStore.edit { it[keyEnabled] = value }
    }

    override suspend fun setIntensity(value: Int) {
        dataStore.edit { it[keyIntensity] = value.coerceIn(0, 100) }
    }

    override suspend fun setRespectSilentMode(value: Boolean) {
        dataStore.edit { it[keyRespectSilentMode] = value }
    }
}

/**
 * DataStore implementation of GesturePreferences.
 */
class DataStoreGesturePreferences(private val dataStore: DataStore<Preferences>) : GesturePreferences {
    private val keyGestureAutoActivation = booleanPreferencesKey("gestures.autoActivation")
    private val keyBottomRightEquals = booleanPreferencesKey("gestures.bottomRightEquals")
    private val keyLayerUpEnabled = booleanPreferencesKey("gestures.layer.upEnabled")
    private val keyLayerDownEnabled = booleanPreferencesKey("gestures.layer.downEnabled")
    private val keyLayerEngineerEnabled = booleanPreferencesKey("gestures.layer.engineerEnabled")

    override val gestureAutoActivationEnabled: Flow<Boolean> = dataStore.data.map { it[keyGestureAutoActivation] ?: false }
    override val bottomRightEqualsEnabled: Flow<Boolean> = dataStore.data.map { it[keyBottomRightEquals] ?: false }
    override val layerUpEnabled: Flow<Boolean> = dataStore.data.map { it[keyLayerUpEnabled] ?: true }
    override val layerDownEnabled: Flow<Boolean> = dataStore.data.map { it[keyLayerDownEnabled] ?: true }
    override val layerEngineerEnabled: Flow<Boolean> = dataStore.data.map { it[keyLayerEngineerEnabled] ?: true }

    override suspend fun isGestureAutoActivationEnabled(): Boolean =
        dataStore.data.map { it[keyGestureAutoActivation] }.firstOrNull() ?: false

    override suspend fun setGestureAutoActivationEnabled(enabled: Boolean) {
        dataStore.edit { it[keyGestureAutoActivation] = enabled }
    }

    override suspend fun isBottomRightEqualsEnabled(): Boolean =
        dataStore.data.map { it[keyBottomRightEquals] }.firstOrNull() ?: false

    override suspend fun setBottomRightEqualsEnabled(enabled: Boolean) {
        dataStore.edit { it[keyBottomRightEquals] = enabled }
    }

    override suspend fun isLayerUpEnabled(): Boolean =
        dataStore.data.map { it[keyLayerUpEnabled] }.firstOrNull() ?: true

    override suspend fun setLayerUpEnabled(enabled: Boolean) {
        dataStore.edit { it[keyLayerUpEnabled] = enabled }
    }

    override suspend fun isLayerDownEnabled(): Boolean =
        dataStore.data.map { it[keyLayerDownEnabled] }.firstOrNull() ?: true

    override suspend fun setLayerDownEnabled(enabled: Boolean) {
        dataStore.edit { it[keyLayerDownEnabled] = enabled }
    }

    override suspend fun isLayerEngineerEnabled(): Boolean =
        dataStore.data.map { it[keyLayerEngineerEnabled] }.firstOrNull() ?: true

    override suspend fun setLayerEngineerEnabled(enabled: Boolean) {
        dataStore.edit { it[keyLayerEngineerEnabled] = enabled }
    }
}
