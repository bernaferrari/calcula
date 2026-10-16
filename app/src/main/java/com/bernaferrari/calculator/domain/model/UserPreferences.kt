package com.bernaferrari.calculator.domain.model

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Colors from the wallpaper (Android 12+). */
    val dynamicColor: Boolean = true,
    val showHistory: Boolean = true,
    val angleUnit: AngleUnit = AngleUnit.DEGREES,
    /** Fixed number of decimals for results, or [AUTO_DECIMALS] for up to 15 significant digits. */
    val decimalPlaces: Int = AUTO_DECIMALS,
    val decimalSeparator: DecimalSeparator = DecimalSeparator.AUTO,
    val hapticFeedback: Boolean = true,
    val soundEnabled: Boolean = false,
    /** Tiny dots on keys that hide flick functions. */
    val showFlickDots: Boolean = true,
    val reduceMotion: Boolean = false,
    val highContrast: Boolean = false,
    /** Whether the person has already flicked a key, so the swipe tip can go away for good. */
    val swipeHintSeen: Boolean = false,
)

const val AUTO_DECIMALS = -1

/** AUTO follows the locale; DOT and COMMA force `1,234.5` or `1.234,5`. */
enum class DecimalSeparator { AUTO, DOT, COMMA }
