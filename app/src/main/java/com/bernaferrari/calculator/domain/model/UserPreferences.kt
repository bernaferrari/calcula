package com.bernaferrari.calculator.domain.model

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.MATERIAL_YOU,
    val showHistory: Boolean = true,
    val angleUnit: AngleUnit = AngleUnit.RADIANS,
    val decimalPlaces: Int = 10,
    val useCommaDecimalSeparator: Boolean = false,
    val hapticFeedback: Boolean = true,
)
