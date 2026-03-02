package org.solovyev.android.calculator

import kotlinx.coroutines.flow.Flow
import org.solovyev.android.calculator.sound.SoundPreferences

interface AppPreferences {
    val settings: SettingsPreferences
    val gui: GuiPreferences
    val onscreen: OnscreenPreferences
    val widget: WidgetPreferences
    val widgets: WidgetsPreferences
    val converter: ConverterPreferences
    val ui: UiPreferences
    val wizard: WizardPreferences
    val theme: ThemePreferences
    val haptics: HapticsPreferences
    val history: HistoryPreferences
    val sound: SoundPreferences
    val gestures: GesturePreferences
}



interface UiPreferences {
    val showFixableErrorDialog: Flow<Boolean>
    fun getShowFixableErrorDialogBlocking(): Boolean
}

interface SettingsPreferences {
    val calculateOnFly: Flow<Boolean>
    val rpnMode: Flow<Boolean>
    val tapeMode: Flow<Boolean>
    val bitwiseWordSize: Flow<Int>
    val bitwiseSigned: Flow<Boolean>
    val angleUnit: Flow<Int>
    val numeralBase: Flow<Int>
    val outputPrecision: Flow<Int>
    val outputNotation: Flow<Int>
    val outputSeparator: Flow<Char>
    val multiplicationSign: Flow<String>

    fun getCalculateOnFlyBlocking(): Boolean

    suspend fun setCalculateOnFly(value: Boolean)
    suspend fun setRpnMode(value: Boolean)
    suspend fun setTapeMode(value: Boolean)
    suspend fun setBitwiseWordSize(value: Int)
    suspend fun setBitwiseSigned(value: Boolean)
    suspend fun setAngleUnit(value: Int)
    suspend fun setNumeralBase(value: Int)
    suspend fun setOutputPrecision(value: Int)
    suspend fun setOutputNotation(value: Int)
    suspend fun setOutputSeparator(value: Char)
    suspend fun setMultiplicationSign(value: String)
}

interface GuiPreferences {
    val theme: Flow<String>
    val dynamicColor: Flow<Boolean>
    val mode: Flow<String>
    val language: Flow<String>
    val showReleaseNotes: Flow<Boolean>
    val useBackAsPrevious: Flow<Boolean>
    val rotateScreen: Flow<Boolean>
    val keepScreenOn: Flow<Boolean>
    val highContrast: Flow<Boolean>
    val reduceMotion: Flow<Boolean>
    val fontScale: Flow<Float>
    val vibrateOnKeypress: Flow<Boolean>
    val showCalculationLatency: Flow<Boolean>

    val latexMode: Flow<Boolean>
    val themeSeed: Flow<Int>
    val isAmoled: Flow<Boolean>

    val highlightExpressions: Flow<Boolean>
    val plotImag: Flow<Boolean>

    suspend fun setHighlightExpressions(value: Boolean)
    suspend fun setPlotImag(value: Boolean)

    suspend fun setTheme(value: String)
    suspend fun setDynamicColor(value: Boolean)
    suspend fun setMode(value: String)
    suspend fun setLanguage(value: String)
    suspend fun setShowReleaseNotes(value: Boolean)
    suspend fun setUseBackAsPrevious(value: Boolean)
    suspend fun setRotateScreen(value: Boolean)
    suspend fun setKeepScreenOn(value: Boolean)
    suspend fun setHighContrast(value: Boolean)
    suspend fun setReduceMotion(value: Boolean)
    suspend fun setFontScale(value: Float)
    suspend fun setVibrateOnKeypress(value: Boolean)
    suspend fun setShowCalculationLatency(value: Boolean)

    suspend fun setLatexMode(value: Boolean)
    suspend fun setThemeSeed(value: Int)
    suspend fun setIsAmoled(value: Boolean)
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
    
    // Currency conversion support for widgets
    suspend fun getLastFromCurrency(): String?
    suspend fun getLastToCurrency(): String?
    suspend fun getLastAmount(): String?
    suspend fun setLastUsed(fromCurrency: String, toCurrency: String, amount: String)
}

interface WizardPreferences {
    val finished: Flow<Boolean>
    suspend fun setFinished(value: Boolean)
}

/**
 * Widget-specific preferences for appearance and opacity.
 */
interface WidgetsPreferences {
    suspend fun getCalculatorOpacity(): Float
    suspend fun getQuickCalcOpacity(): Float
    suspend fun getHistoryOpacity(): Float
    suspend fun getConverterOpacity(): Float
    suspend fun getSmartStackOpacity(): Float
    
    suspend fun setCalculatorOpacity(value: Float)
    suspend fun setQuickCalcOpacity(value: Float)
    suspend fun setHistoryOpacity(value: Float)
    suspend fun setConverterOpacity(value: Float)
    suspend fun setSmartStackOpacity(value: Float)
}

/**
 * Theme preferences for dynamic colors and light/dark mode.
 */
interface ThemePreferences {
    suspend fun useDynamicColors(): Boolean
    suspend fun isLightTheme(): Boolean
    
    suspend fun setUseDynamicColors(value: Boolean)
    suspend fun setLightTheme(value: Boolean)
}

/**
 * Haptics preferences for vibration feedback.
 */
interface HapticsPreferences {
    val hapticOnReleaseEnabled: Flow<Boolean>

    suspend fun isEnabled(): Boolean
    suspend fun setEnabled(value: Boolean)

    /**
     * Whether to trigger haptic feedback when a gesture is released.
     * This applies to both THRESHOLD and STICK_AND_RELEASE swipe modes.
     */
    suspend fun isHapticOnReleaseEnabled(): Boolean
    suspend fun setHapticOnReleaseEnabled(value: Boolean)
}

/**
 * Gesture preferences for swipe and slide activation settings.
 */
interface GesturePreferences {
    /**
     * When enabled, gestures auto-activate when dragging past 80% threshold.
     * When disabled (default), user must lift finger to activate after passing threshold.
     */
    val gestureAutoActivationEnabled: Flow<Boolean>
    /**
     * When enabled, restore the classic bottom-right "=" key on modern/engineer keyboards.
     * When disabled (default), the key is replaced by "ƒ" and "=" is available via display and long-press.
     */
    val bottomRightEqualsEnabled: Flow<Boolean>
    /**
     * Enables swipe-up gesture layer across the keyboard.
     */
    val layerUpEnabled: Flow<Boolean>
    /**
     * Enables swipe-down gesture layer across the keyboard.
     */
    val layerDownEnabled: Flow<Boolean>
    /**
     * Enables engineer keyboard layer when GUI mode is Engineer.
     */
    val layerEngineerEnabled: Flow<Boolean>

    suspend fun isGestureAutoActivationEnabled(): Boolean
    suspend fun setGestureAutoActivationEnabled(enabled: Boolean)
    suspend fun isBottomRightEqualsEnabled(): Boolean
    suspend fun setBottomRightEqualsEnabled(enabled: Boolean)
    suspend fun isLayerUpEnabled(): Boolean
    suspend fun setLayerUpEnabled(enabled: Boolean)
    suspend fun isLayerDownEnabled(): Boolean
    suspend fun setLayerDownEnabled(enabled: Boolean)
    suspend fun isLayerEngineerEnabled(): Boolean
    suspend fun setLayerEngineerEnabled(enabled: Boolean)
}

/**
 * History preferences for accessing recent calculations.
 */
interface HistoryPreferences {
    fun observeRecent(): Flow<List<org.solovyev.android.calculator.history.HistoryState>>
    suspend fun clearRecent()
}
