package com.bernaferrari.calculator.ui.calculator

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.bernaferrari.calculator.R

class SoundEffectsManager(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    
    // Sound IDs - these would be loaded from resources
    private var numberSound: Int = 0
    private var operatorSound: Int = 0
    private var deleteSound: Int = 0
    private var equalsSound: Int = 0
    private var errorSound: Int = 0
    private var celebrationSound: Int = 0
    
    var isEnabled = true
    
    init {
        // Note: These sound files would need to be added to res/raw/
        // numberSound = soundPool.load(context, R.raw.number_tap, 1)
        // operatorSound = soundPool.load(context, R.raw.operator_tap, 1)
        // deleteSound = soundPool.load(context, R.raw.delete, 1)
        // equalsSound = soundPool.load(context, R.raw.equals, 1)
        // errorSound = soundPool.load(context, R.raw.error, 1)
        // celebrationSound = soundPool.load(context, R.raw.celebration, 1)
    }
    
    fun playNumberTap() {
        if (isEnabled && numberSound != 0) {
            soundPool.play(numberSound, 0.7f, 0.7f, 1, 0, 1f)
        }
    }
    
    fun playOperatorTap() {
        if (isEnabled && operatorSound != 0) {
            soundPool.play(operatorSound, 0.8f, 0.8f, 1, 0, 1f)
        }
    }
    
    fun playDelete() {
        if (isEnabled && deleteSound != 0) {
            soundPool.play(deleteSound, 0.6f, 0.6f, 1, 0, 1f)
        }
    }
    
    fun playEquals() {
        if (isEnabled && equalsSound != 0) {
            soundPool.play(equalsSound, 1f, 1f, 1, 0, 1f)
        }
    }
    
    fun playError() {
        if (isEnabled && errorSound != 0) {
            soundPool.play(errorSound, 0.8f, 0.8f, 1, 0, 1f)
        }
    }
    
    fun playCelebration() {
        if (isEnabled && celebrationSound != 0) {
            soundPool.play(celebrationSound, 1f, 1f, 1, 0, 1f)
        }
    }
    
    fun release() {
        soundPool.release()
    }
}

@Composable
fun rememberSoundEffects(): SoundEffectsManager {
    val context = LocalContext.current
    return remember { SoundEffectsManager(context) }
}