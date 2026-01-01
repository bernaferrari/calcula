package org.solovyev.android.calculator

import kotlinx.coroutines.flow.Flow

interface AppPreferences {
    val settings: SettingsPreferences
    val gui: GuiPreferences
    val onscreen: OnscreenPreferences
    val widget: WidgetPreferences
    val converter: ConverterPreferences
    val ui: UiPreferences
    val wizard: WizardPreferences
}



interface UiPreferences {
    val showFixableErrorDialog: Flow<Boolean>
    fun getShowFixableErrorDialogBlocking(): Boolean
}

interface SettingsPreferences {
    val calculateOnFly: Flow<Boolean>
    val angleUnit: Flow<Int>
    val numeralBase: Flow<Int>
    val outputPrecision: Flow<Int>
    val outputNotation: Flow<Int>
    val outputSeparator: Flow<Char>
    val multiplicationSign: Flow<String>

    fun getCalculateOnFlyBlocking(): Boolean

    suspend fun setCalculateOnFly(value: Boolean)
    suspend fun setAngleUnit(value: Int)
    suspend fun setNumeralBase(value: Int)
    suspend fun setOutputPrecision(value: Int)
    suspend fun setOutputNotation(value: Int)
    suspend fun setOutputSeparator(value: Char)
    suspend fun setMultiplicationSign(value: String)
}

interface GuiPreferences {
    val theme: Flow<String>
    val mode: Flow<String>
    val language: Flow<String>
    val showReleaseNotes: Flow<Boolean>
    val useBackAsPrevious: Flow<Boolean>
    val rotateScreen: Flow<Boolean>
    val keepScreenOn: Flow<Boolean>
    val highContrast: Flow<Boolean>
    val vibrateOnKeypress: Flow<Boolean>

    suspend fun setTheme(value: String)
    suspend fun setMode(value: String)
    suspend fun setLanguage(value: String)
    suspend fun setShowReleaseNotes(value: Boolean)
    suspend fun setUseBackAsPrevious(value: Boolean)
    suspend fun setRotateScreen(value: Boolean)
    suspend fun setKeepScreenOn(value: Boolean)
    suspend fun setHighContrast(value: Boolean)
    suspend fun setVibrateOnKeypress(value: Boolean)
}

interface OnscreenPreferences {
    val showAppIcon: Flow<Boolean>
    val theme: Flow<String>
    val startOnBoot: Flow<Boolean>

    suspend fun setShowAppIcon(value: Boolean)
    suspend fun setTheme(value: String)
    suspend fun setStartOnBoot(value: Boolean)
}

interface WidgetPreferences {
    val theme: Flow<String>
    suspend fun setTheme(value: String)
}

interface ConverterPreferences {
    val lastDimension: Flow<Int>
    val lastUnitsFrom: Flow<Int>
    val lastUnitsTo: Flow<Int>

    suspend fun getLastDimension(): Int?
    suspend fun getLastUnitsFrom(): Int?
    suspend fun getLastUnitsTo(): Int?
    suspend fun setLastUsed(dimension: Int, from: Int, to: Int)
}

interface WizardPreferences {
    val finished: Flow<Boolean>
    suspend fun setFinished(value: Boolean)
}
