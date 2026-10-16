package com.bernaferrari.calculator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bernaferrari.calculator.R

/** Google Sans Flex (OFL), the M3 Expressive typeface. Glyphs it lacks (π, √, superscripts) fall back to the system font. */
val GoogleSansFlex = FontFamily(
    Font(R.font.google_sans_flex_regular, FontWeight.Normal),
    Font(R.font.google_sans_flex_medium, FontWeight.Medium),
    Font(R.font.google_sans_flex_semibold, FontWeight.SemiBold),
    Font(R.font.google_sans_flex_bold, FontWeight.Bold),
)

private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = GoogleSansFlex),
    displayMedium = base.displayMedium.copy(fontFamily = GoogleSansFlex),
    displaySmall = base.displaySmall.copy(fontFamily = GoogleSansFlex),
    headlineLarge = base.headlineLarge.copy(fontFamily = GoogleSansFlex),
    headlineMedium = base.headlineMedium.copy(fontFamily = GoogleSansFlex),
    headlineSmall = base.headlineSmall.copy(fontFamily = GoogleSansFlex),
    titleLarge = base.titleLarge.copy(fontFamily = GoogleSansFlex),
    titleMedium = base.titleMedium.copy(fontFamily = GoogleSansFlex),
    titleSmall = base.titleSmall.copy(fontFamily = GoogleSansFlex),
    bodyLarge = base.bodyLarge.copy(fontFamily = GoogleSansFlex),
    bodyMedium = base.bodyMedium.copy(fontFamily = GoogleSansFlex),
    bodySmall = base.bodySmall.copy(fontFamily = GoogleSansFlex),
    labelLarge = base.labelLarge.copy(fontFamily = GoogleSansFlex),
    labelMedium = base.labelMedium.copy(fontFamily = GoogleSansFlex),
    labelSmall = base.labelSmall.copy(fontFamily = GoogleSansFlex),
)
