package com.bernaferrari.calculator.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.bernaferrari.calculator.ui.calculator.Separators
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun systemSeparators(): Separators {
    val formatter = NSNumberFormatter().apply { locale = NSLocale.currentLocale }
    return Separators(
        decimal = formatter.decimalSeparator.firstOrNull() ?: '.',
        grouping = formatter.groupingSeparator.firstOrNull() ?: ',',
    )
}

actual fun formatDate(epochMillis: Long): String = NSDateFormatter().apply {
    dateStyle = NSDateFormatterMediumStyle
    timeStyle = NSDateFormatterNoStyle
}.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0))

actual class KeyValueStore(private val defaults: NSUserDefaults) {
    actual fun get(key: String): String? = defaults.stringForKey(key)

    actual fun put(key: String, value: String?) {
        if (value == null) defaults.removeObjectForKey(key) else defaults.setObject(value, key)
    }
}

actual fun keyValueStore(): KeyValueStore = KeyValueStore(NSUserDefaults.standardUserDefaults)

@Composable
actual fun rememberKeySounds(): (KeySound) -> Unit = remember {
    { sound ->
        // The keyboard's own clicks: 1104 tap, 1155 delete, 1156 modifier (used for =).
        val id = when (sound) {
            KeySound.Tap -> 1104u
            KeySound.Delete -> 1155u
            KeySound.Equals, KeySound.Error -> 1156u
        }
        AudioServicesPlaySystemSound(id)
    }
}

@Composable
actual fun dynamicColorScheme(dark: Boolean): ColorScheme? = null

actual val supportsDynamicColor: Boolean = false

@Composable
actual fun SystemBarsAppearance(darkTheme: Boolean) = Unit

actual val systemConfirmsCopy: Boolean = false
