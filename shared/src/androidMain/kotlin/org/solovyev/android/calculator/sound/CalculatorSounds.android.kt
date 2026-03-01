package org.solovyev.android.calculator.sound

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Android implementation of SoundPlayer using ToneGenerator.
 *
 * ToneGenerator provides reliable device audio that respects system volume
 * without needing audio files or complex synthesis.
 */
class AndroidSoundPlayer(private val context: Context) : SoundPlayer {

    private var toneGenerator: ToneGenerator? = null
    private var audioManager: AudioManager? = null

    override fun initialize() {
        if (toneGenerator == null) {
            // STREAM_MUSIC respects device volume settings
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        }
        if (audioManager == null) {
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        }
    }

    override fun release() {
        toneGenerator?.release()
        toneGenerator = null
        audioManager = null
    }

    override fun play(type: SoundType, intensity: Int) {
        val volume = (intensity.coerceIn(0, 100) * 255 / 100).coerceIn(0, 255)
        val toneGen = toneGenerator ?: return

        when (type) {
            SoundType.DIGIT -> playDigitSound(toneGen, volume)
            SoundType.OPERATOR -> playOperatorSound(toneGen, volume)
            SoundType.EQUALS -> playEqualsSound(toneGen, volume)
            SoundType.ERROR -> playErrorSound(toneGen, volume)
            SoundType.CLEAR -> playClearSound(toneGen, volume)
        }
    }

    override fun isSilentMode(): Boolean {
        val am = audioManager ?: return false
        return when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT,
            AudioManager.RINGER_MODE_VIBRATE -> true
            else -> false
        }
    }

    private fun playDigitSound(toneGen: ToneGenerator, volume: Int) {
        // DTMF tones provide clean, short beeps
        // DTMF_0 has a nice mechanical click quality
        toneGen.startTone(ToneGenerator.TONE_DTMF_0, SoundDesignSpec.DIGIT_DURATION_MS)
    }

    private fun playOperatorSound(toneGen: ToneGenerator, volume: Int) {
        // PROP_BEEP provides a satisfying "thock" sound
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, SoundDesignSpec.OPERATOR_DURATION_MS)
    }

    private fun playEqualsSound(toneGen: ToneGenerator, volume: Int) {
        // Two-tone effect: confirmation beep followed by acknowledgment
        toneGen.startTone(ToneGenerator.TONE_PROP_ACK, SoundDesignSpec.EQUALS_DURATION_MS)
    }

    private fun playErrorSound(toneGen: ToneGenerator, volume: Int) {
        // NACK tone for soft error indication
        toneGen.startTone(ToneGenerator.TONE_PROP_NACK, SoundDesignSpec.ERROR_DURATION_MS)
    }

    private fun playClearSound(toneGen: ToneGenerator, volume: Int) {
        // SUP_PIP provides a descending tone suitable for "swish"
        toneGen.startTone(ToneGenerator.TONE_SUP_PIP, SoundDesignSpec.CLEAR_DURATION_MS)
    }
}

/**
 * Convenience function to create a SoundPlayer on Android.
 */
fun createSoundPlayer(context: Context): SoundPlayer = AndroidSoundPlayer(context)
