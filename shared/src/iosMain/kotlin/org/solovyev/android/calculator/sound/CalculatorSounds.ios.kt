package org.solovyev.android.calculator.sound

/**
 * iOS implementation of SoundPlayer using AudioServices.
 *
 * Uses system sound services for lightweight audio feedback.
 * Note: Full implementation would use AudioServicesPlaySystemSound
 * or AVAudioPlayer for custom tones.
 */
class IosSoundPlayer : SoundPlayer {

    private var isInitialized = false

    override fun initialize() {
        isInitialized = true
        // iOS: Would initialize AudioServices here
        // AudioServicesCreateSystemSoundID() for custom sounds
    }

    override fun release() {
        isInitialized = false
        // iOS: Would dispose sound IDs here
    }

    override fun play(type: SoundType, intensity: Int) {
        if (!isInitialized) return

        val volume = intensity.coerceIn(0, 100)
        if (volume == 0) return

        when (type) {
            SoundType.DIGIT -> playDigitSound()
            SoundType.OPERATOR -> playOperatorSound()
            SoundType.EQUALS -> playEqualsSound()
            SoundType.ERROR -> playErrorSound()
            SoundType.CLEAR -> playClearSound()
        }
    }

    override fun isSilentMode(): Boolean {
        // iOS: Would check AVAudioSession.sharedInstance().secondaryAudioShouldBeSilencedHint
        // or UIApplication.sharedApplication().applicationState
        return false // Placeholder
    }

    private fun playDigitSound() {
        // iOS: AudioServicesPlaySystemSound(kSystemSoundID_Vibrate) for subtle feedback
        // or custom sound ID
    }

    private fun playOperatorSound() {
        // iOS: AudioServicesPlaySystemSound with custom sound
    }

    private fun playEqualsSound() {
        // iOS: AudioServicesPlaySystemSound with confirmation sound
    }

    private fun playErrorSound() {
        // iOS: AudioServicesPlaySystemSound with error sound
    }

    private fun playClearSound() {
        // iOS: AudioServicesPlaySystemSound with clear sound
    }
}

/**
 * Convenience function to create a SoundPlayer on iOS.
 */
fun createSoundPlayer(): SoundPlayer = IosSoundPlayer()
