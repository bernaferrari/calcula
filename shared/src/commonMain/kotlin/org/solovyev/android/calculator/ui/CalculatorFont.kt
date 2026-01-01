package org.solovyev.android.calculator.ui

import androidx.compose.ui.text.font.FontFamily

/**
 * Platform-specific calculator font family.
 * Android uses custom font, iOS uses system default.
 */
expect val CalculatorFontFamily: FontFamily
