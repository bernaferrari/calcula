@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.calculator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState

// Calculator++ theme colors - expressive and mathematical
private val calculatorPrimary = Color(0xFF6366F1) // Indigo for calculations
private val calculatorAccent = Color(0xFFEC4899) // Pink for scientific functions

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val baseScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            else ->
                rememberDynamicMaterialThemeState(
                    seedColor = calculatorPrimary,
                    style = PaletteStyle.Expressive,
                    isDark = darkTheme,
                    specVersion = ColorSpec.SpecVersion.SPEC_2025,
                ).colorScheme
        }
    // High contrast: true black or white backgrounds and full-strength secondary text.
    val colorScheme = if (!highContrast) {
        baseScheme
    } else if (darkTheme) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            onSurface = Color.White,
            onSurfaceVariant = Color.White.copy(alpha = 0.86f),
            outlineVariant = baseScheme.outline,
        )
    } else {
        baseScheme.copy(
            background = Color.White,
            surface = Color.White,
            onSurface = Color.Black,
            onSurfaceVariant = Color.Black.copy(alpha = 0.8f),
            outlineVariant = baseScheme.outline,
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    return MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
