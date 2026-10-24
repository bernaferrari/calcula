@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.calculator.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.bernaferrari.calculator.platform.SystemBarsAppearance
import com.bernaferrari.calculator.domain.model.AppPalette
import com.bernaferrari.calculator.domain.model.ColorStyle

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: AppPalette = AppPalette.INDIGO,
    colorStyle: ColorStyle = ColorStyle.TONAL_SPOT,
    content: @Composable () -> Unit,
) {
    val colorScheme = paletteColorScheme(palette, darkTheme, colorStyle)
    SystemBarsAppearance(darkTheme)

    val font = googleSansFlex()
    CompositionLocalProvider(LocalAppFont provides font) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = calculatorTypography(font),
            // Menus, snackbars, tooltips and text fields round like the keys, not Material's 4dp.
            shapes = Shapes(extraSmall = RoundedCornerShape(KeyRadius)),
            content = content,
        )
    }
}

/** The keys' resting corner radius; the theme's smallest shape follows it. */
val KeyRadius = 14.dp
