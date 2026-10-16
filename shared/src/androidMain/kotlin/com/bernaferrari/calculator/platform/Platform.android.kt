package com.bernaferrari.calculator.platform

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import com.bernaferrari.calculator.ui.calculator.Separators
import java.text.DateFormat
import java.text.DecimalFormatSymbols
import java.util.Date

/** Set once from the Application, before anything reads storage. */
object AndroidPlatform {
    @SuppressLint("StaticFieldLeak")
    internal lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}

actual fun systemSeparators(): Separators {
    val symbols = DecimalFormatSymbols.getInstance()
    return Separators(symbols.decimalSeparator, symbols.groupingSeparator)
}

actual fun formatDate(epochMillis: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))

actual class KeyValueStore(private val prefs: SharedPreferences) {
    actual fun get(key: String): String? = prefs.getString(key, null)

    actual fun put(key: String, value: String?) = prefs.edit { if (value == null) remove(key) else putString(key, value) }
}

actual fun keyValueStore(): KeyValueStore =
    KeyValueStore(AndroidPlatform.context.getSharedPreferences("calculator", Context.MODE_PRIVATE))

@Composable
actual fun rememberKeySounds(): (KeySound) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        val audio = context.getSystemService(AudioManager::class.java)
        val play: (KeySound) -> Unit = { sound ->
            val effect = when (sound) {
                KeySound.Tap -> AudioManager.FX_KEYPRESS_STANDARD
                KeySound.Delete -> AudioManager.FX_KEYPRESS_DELETE
                KeySound.Equals -> AudioManager.FX_KEYPRESS_RETURN
                KeySound.Error -> AudioManager.FX_KEYPRESS_INVALID
            }
            audio?.playSoundEffect(effect, 0.6f)
        }
        play
    }
}

@Composable
actual fun wallpaperColorScheme(dark: Boolean): ColorScheme? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    return if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
}

actual val supportsDynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
actual fun SystemBarsAppearance(darkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

actual val systemConfirmsCopy: Boolean = Build.VERSION.SDK_INT >= 33
