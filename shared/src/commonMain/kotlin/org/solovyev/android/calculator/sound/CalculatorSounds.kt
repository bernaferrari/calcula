package org.solovyev.android.calculator.sound

import androidx.compose.runtime.compositionLocalOf

/**
 * Sound type enumeration for calculator interactions.
 */
enum class SoundType {
    /**
     * Light mechanical click for digit presses.
     * Like Cherry MX Blue keyboard.
     */
    DIGIT,

    /**
     * Medium "thock" for operator presses.
     * Deeper than digits.
     */
    OPERATOR,

    /**
     * Satisfying "clunk" for equals.
     * Two-tone with subtle reverb.
     */
    EQUALS,

    /**
     * Soft "bonk" for errors.
     * Non-jarring, low frequency.
     */
    ERROR,

    /**
     * Quick "swish" for clear.
     * Descending tone.
     */
    CLEAR
}

/**
 * Sound preferences for calculator audio feedback.
 */
interface SoundPreferences {
    /**
     * Whether sound feedback is enabled.
     */
    val enabled: kotlinx.coroutines.flow.Flow<Boolean>

    /**
     * Sound intensity/volume level (0-100).
     */
    val intensity: kotlinx.coroutines.flow.Flow<Int>

    /**
     * Whether to respect device silent mode.
     */
    val respectSilentMode: kotlinx.coroutines.flow.Flow<Boolean>

    suspend fun isEnabled(): Boolean
    suspend fun getIntensity(): Int
    suspend fun shouldRespectSilentMode(): Boolean

    suspend fun setEnabled(value: Boolean)
    suspend fun setIntensity(value: Int)
    suspend fun setRespectSilentMode(value: Boolean)
}

/**
 * Platform-independent sound player interface.
 */
interface SoundPlayer {
    /**
     * Play a sound of the specified type.
     *
     * @param type The type of sound to play
     * @param intensity Sound intensity (0-100), affects volume
     */
    fun play(type: SoundType, intensity: Int = 100)

    /**
     * Initialize the sound player. Must be called before playing sounds.
     */
    fun initialize()

    /**
     * Release resources. Should be called when no longer needed.
     */
    fun release()

    /**
     * Check if the device is in silent mode.
     */
    fun isSilentMode(): Boolean
}

/**
 * Calculator sound manager that coordinates sound playback with preferences.
 */
class CalculatorSoundManager(
    private val soundPlayer: SoundPlayer,
    private val preferences: SoundPreferences
) {
    private var isInitialized = false

    fun initialize() {
        if (!isInitialized) {
            soundPlayer.initialize()
            isInitialized = true
        }
    }

    fun release() {
        if (isInitialized) {
            soundPlayer.release()
            isInitialized = false
        }
    }

    /**
     * Play a sound if sounds are enabled and conditions are met.
     */
    suspend fun play(type: SoundType) {
        if (!preferences.isEnabled()) return

        val respectSilent = preferences.shouldRespectSilentMode()
        if (respectSilent && soundPlayer.isSilentMode()) return

        val intensity = preferences.getIntensity()
        if (intensity > 0) {
            soundPlayer.play(type, intensity)
        }
    }

    /**
     * Play a sound without suspending (fire and forget).
     */
    fun playBlocking(type: SoundType, intensity: Int, respectSilent: Boolean) {
        if (respectSilent && soundPlayer.isSilentMode()) return
        if (intensity > 0) {
            soundPlayer.play(type, intensity)
        }
    }
}

/**
 * Sound design specification (documentation only).
 *
 * DIGIT Sounds:
 * - Light mechanical click
 * - Like Cherry MX Blue keyboard
 * - Short duration: 30-50ms
 * - Frequency: 800-1000Hz
 *
 * OPERATOR Sounds:
 * - Medium "thock"
 * - Deeper than digits
 * - Duration: 40-60ms
 * - Frequency: 400-600Hz
 *
 * EQUALS Sound:
 * - Satisfying "clunk"
 * - Two-tone: high then low
 * - Duration: 80-100ms
 * - Subtle reverb effect
 *
 * ERROR Sound:
 * - Soft "bonk"
 * - Non-jarring
 * - Duration: 60ms
 * - Low frequency: 200-300Hz
 *
 * CLEAR Sound:
 * - Quick "swish"
 * - Descending tone
 * - Duration: 100ms
 */
/**
 * CompositionLocal to provide the sound manager to composables.
 */
val LocalCalculatorSoundManager = compositionLocalOf<CalculatorSoundManager?> { null }

/**
 * CompositionLocal to provide whether sounds are enabled.
 */
val LocalCalculatorSoundsEnabled = compositionLocalOf { true }

/**
 * Sound design specification (documentation only).
 */
object SoundDesignSpec {
    const val DIGIT_DURATION_MS = 40
    const val DIGIT_FREQUENCY_HZ = 900

    const val OPERATOR_DURATION_MS = 50
    const val OPERATOR_FREQUENCY_HZ = 500

    const val EQUALS_DURATION_MS = 90
    const val EQUALS_FREQUENCY_HIGH_HZ = 800
    const val EQUALS_FREQUENCY_LOW_HZ = 400

    const val ERROR_DURATION_MS = 60
    const val ERROR_FREQUENCY_HZ = 250

    const val CLEAR_DURATION_MS = 100
    const val CLEAR_FREQUENCY_START_HZ = 600
    const val CLEAR_FREQUENCY_END_HZ = 300
}
