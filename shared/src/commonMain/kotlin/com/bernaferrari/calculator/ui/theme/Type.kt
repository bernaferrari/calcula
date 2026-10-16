package com.bernaferrari.calculator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bernaferrari.calculator.resources.Res
import com.bernaferrari.calculator.resources.google_sans_flex_bold
import com.bernaferrari.calculator.resources.google_sans_flex_medium
import com.bernaferrari.calculator.resources.google_sans_flex_regular
import com.bernaferrari.calculator.resources.google_sans_flex_semibold
import org.jetbrains.compose.resources.Font

/** Google Sans Flex (OFL), the M3 Expressive typeface. Glyphs it lacks (π, √, superscripts) fall back to the system font. */
/** The loaded app typeface; provided by the theme. */
val LocalAppFont = staticCompositionLocalOf<FontFamily> { FontFamily.Default }

val GoogleSansFlex: FontFamily
    @Composable get() = LocalAppFont.current

@Composable
fun googleSansFlex(): FontFamily = FontFamily(
    Font(Res.font.google_sans_flex_regular, FontWeight.Normal),
    Font(Res.font.google_sans_flex_medium, FontWeight.Medium),
    Font(Res.font.google_sans_flex_semibold, FontWeight.SemiBold),
    Font(Res.font.google_sans_flex_bold, FontWeight.Bold),
)

@Composable
fun calculatorTypography(family: FontFamily): Typography {
    return remember(family) {
        val base = Typography()
        Typography(
            displayLarge = base.displayLarge.copy(fontFamily = family),
            displayMedium = base.displayMedium.copy(fontFamily = family),
            displaySmall = base.displaySmall.copy(fontFamily = family),
            headlineLarge = base.headlineLarge.copy(fontFamily = family),
            headlineMedium = base.headlineMedium.copy(fontFamily = family),
            headlineSmall = base.headlineSmall.copy(fontFamily = family),
            titleLarge = base.titleLarge.copy(fontFamily = family),
            titleMedium = base.titleMedium.copy(fontFamily = family),
            titleSmall = base.titleSmall.copy(fontFamily = family),
            bodyLarge = base.bodyLarge.copy(fontFamily = family),
            bodyMedium = base.bodyMedium.copy(fontFamily = family),
            bodySmall = base.bodySmall.copy(fontFamily = family),
            labelLarge = base.labelLarge.copy(fontFamily = family),
            labelMedium = base.labelMedium.copy(fontFamily = family),
            labelSmall = base.labelSmall.copy(fontFamily = family),
        )
    }
}
