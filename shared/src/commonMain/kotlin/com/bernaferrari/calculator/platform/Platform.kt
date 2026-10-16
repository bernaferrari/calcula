package com.bernaferrari.calculator.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.bernaferrari.calculator.ui.calculator.Separators

/** The decimal and grouping marks of the person's locale. */
expect fun systemSeparators(): Separators

/** A day in the locale's medium date style, e.g. "Oct 14, 2026" or "14 de out. de 2026". */
expect fun formatDate(epochMillis: Long): String

/** Persistent string storage: SharedPreferences, NSUserDefaults or localStorage. */
expect class KeyValueStore {
    fun get(key: String): String?
    fun put(key: String, value: String?)
}

expect fun keyValueStore(): KeyValueStore

enum class KeySound { Tap, Delete, Equals, Error }

/** Plays the system's key-click sounds, where the platform has them. */
@Composable
expect fun rememberKeySounds(): (KeySound) -> Unit

/** Wallpaper colors (Android 12+), or null where the platform has none. */
@Composable
expect fun dynamicColorScheme(dark: Boolean): ColorScheme?

expect val supportsDynamicColor: Boolean

/** Keeps the status and navigation bar icons readable over the app's background. */
@Composable
expect fun SystemBarsAppearance(darkTheme: Boolean)

/** Whether the OS shows its own "copied" confirmation (Android 13+), so the app shouldn't. */
expect val systemConfirmsCopy: Boolean
