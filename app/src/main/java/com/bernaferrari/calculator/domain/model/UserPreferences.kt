package com.bernaferrari.calculator.domain.model

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.MATERIAL_YOU,
    val showHistory: Boolean = true,
    val angleUnit: AngleUnit = AngleUnit.DEGREES,
    val decimalPlaces: Int = 10,
    val useCommaDecimalSeparator: Boolean = false,
    val hapticFeedback: Boolean = true,
    val soundEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
    val highContrast: Boolean = false,
    /** Whether the person has already flicked a key, so the swipe tip can go away for good. */
    val swipeHintSeen: Boolean = false,
)
