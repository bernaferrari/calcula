@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.bernaferrari.calculator.platform.SystemBarsAppearance
import com.bernaferrari.calculator.domain.model.AppPalette
import com.bernaferrari.calculator.domain.model.ColorStyle

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: AppPalette = AppPalette.INDIGO,
    colorStyle: ColorStyle = ColorStyle.TONAL_SPOT,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val baseScheme = paletteColorScheme(palette, darkTheme, colorStyle)
    // High contrast: true black or white backgrounds and full-strength secondary text.
    val colorScheme = when {
        !highContrast -> baseScheme
        darkTheme -> baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            onSurface = Color.White,
            onSurfaceVariant = Color.White.copy(alpha = 0.86f),
            outlineVariant = baseScheme.outline,
        )
        else -> baseScheme.copy(
            background = Color.White,
            surface = Color.White,
            onSurface = Color.Black,
            onSurfaceVariant = Color.Black.copy(alpha = 0.8f),
            outlineVariant = baseScheme.outline,
        )
    }
    SystemBarsAppearance(darkTheme)

    val font = googleSansFlex()
    CompositionLocalProvider(LocalAppFont provides font) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = calculatorTypography(font),
            content = content,
        )
    }
}
