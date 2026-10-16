package com.bernaferrari.calculator.ui.calculator

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.*
import kotlin.math.*

class SoundEffectsManager(private val context: Context) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    
    var isEnabled = true
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private fun generateTone(frequency: Double, duration: Int, volume: Float = 0.5f) {
        if (!isEnabled) return
        
        scope.launch {
            val sampleRate = 44100
            val samples = (duration * sampleRate / 1000.0).toInt()
            val buffer = ShortArray(samples)
            
            // Generate sine wave with envelope
            for (i in 0 until samples) {
                val time = i.toDouble() / sampleRate
                // Envelope for smooth attack and decay
                val envelope = when {
                    i < samples * 0.1 -> i.toDouble() / (samples * 0.1) // Attack
                    i > samples * 0.7 -> (samples - i).toDouble() / (samples * 0.3) // Decay
                    else -> 1.0 // Sustain
                }
                
                val sample = sin(2 * PI * frequency * time) * envelope * volume * Short.MAX_VALUE
                buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            
            // Play the generated tone
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            
            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            
            delay(duration.toLong())
            audioTrack.stop()
            audioTrack.release()
        }
    }
    
    private fun generateChord(frequencies: List<Double>, duration: Int, volume: Float = 0.4f) {
        if (!isEnabled) return
        
        scope.launch {
            val sampleRate = 44100
            val samples = (duration * sampleRate / 1000.0).toInt()
            val buffer = ShortArray(samples)
            
            for (i in 0 until samples) {
                val time = i.toDouble() / sampleRate
                var sample = 0.0
                
                // Mix multiple frequencies
                frequencies.forEach { freq ->
                    sample += sin(2 * PI * freq * time) * volume / frequencies.size
                }
                
                // Envelope
                val envelope = when {
                    i < samples * 0.05 -> i.toDouble() / (samples * 0.05)
                    i > samples * 0.8 -> (samples - i).toDouble() / (samples * 0.2)
                    else -> 1.0
                }
                
                sample *= envelope * Short.MAX_VALUE
                buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            
            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            
            delay(duration.toLong())
            audioTrack.stop()
            audioTrack.release()
        }
    }
    
    fun playNumberTap() {
        // Soft, pleasant click for numbers
        generateTone(800.0, 80, 0.3f)
    }
    
    fun playOperatorTap() {
        // Slightly higher pitch for operators
        generateTone(1000.0, 100, 0.4f)
    }
    
    fun playFunctionTap() {
        // Distinctive sound for mathematical functions
        generateTone(1200.0, 120, 0.35f)
    }
    
    fun playMemoryTap() {
        // Unique sound for memory operations
        generateChord(listOf(600.0, 800.0), 150, 0.3f)
    }
    
    fun playDelete() {
        // Descending tone for deletion
        scope.launch {
            generateTone(600.0, 60, 0.25f)
            delay(40)
            generateTone(400.0, 60, 0.25f)
        }
    }
    
    fun playEquals() {
        // Satisfying chord for equals
        generateChord(listOf(523.25, 659.25, 783.99), 200, 0.4f) // C-E-G major chord
    }
    
    fun playError() {
        // Dissonant sound for errors
        scope.launch {
            generateTone(300.0, 100, 0.3f)
            delay(50)
            generateTone(250.0, 100, 0.3f)
        }
    }
    
    fun playCelebration() {
        // Ascending arpeggio for celebration
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.5) // C-E-G-C
            notes.forEach { freq ->
                generateTone(freq, 150, 0.4f)
                delay(80)
            }
        }
    }
    
    fun playSecondaryFunction() {
        // Special sound for secondary function activation
        generateTone(1400.0, 100, 0.3f)
    }
    
    fun release() {
        scope.cancel()
    }
}

@Composable
fun rememberSoundEffects(): SoundEffectsManager {
    val context = LocalContext.current
    return remember { SoundEffectsManager(context) }
}