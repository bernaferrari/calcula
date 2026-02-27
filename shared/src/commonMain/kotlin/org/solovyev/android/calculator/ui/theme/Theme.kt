package org.solovyev.android.calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seedColor: Color = Color(0xFF13ABF1), // Default Blue
    useDynamicColor: Boolean = false,
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val seededTheme = rememberDynamicMaterialThemeState(
        seedColor = seedColor,
        style = PaletteStyle.Vibrant, // Changed to Vibrant for more expressiveness
        isDark = darkTheme, 
        isAmoled = isAmoled,
        specVersion = ColorSpec.SpecVersion.SPEC_2025
    )
    val colorScheme = if (useDynamicColor) {
        platformDynamicColorScheme(darkTheme) ?: seededTheme.colorScheme
    } else {
        seededTheme.colorScheme
    }

    // Keep rounded geometry, but avoid oversized radii that make list rows/cards look bloated.
    val shapes = MaterialTheme.shapes.copy(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(28.dp)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = Typography,
        content = content
    )
}
