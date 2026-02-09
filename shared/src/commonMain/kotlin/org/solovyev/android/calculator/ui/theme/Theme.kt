@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
package org.solovyev.android.calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
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
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val dynamicTheme = rememberDynamicMaterialThemeState(
        seedColor = seedColor,
        style = PaletteStyle.Vibrant, // Changed to Vibrant for more expressiveness
        isDark = darkTheme, 
        isAmoled = isAmoled,
        specVersion = ColorSpec.SpecVersion.SPEC_2025
    )

    // Override shapes to be more rounded/softer
    val shapes = MaterialTheme.shapes.copy(
        extraSmall = RoundedCornerShape(16.dp),
        small = RoundedCornerShape(24.dp),
        medium = RoundedCornerShape(32.dp),
        large = RoundedCornerShape(48.dp),
        extraLarge = RoundedCornerShape(100.dp) // Full pill/circle
    )

    MaterialTheme(
        colorScheme = dynamicTheme.colorScheme,
        shapes = shapes,
        typography = Typography,
        content = content
    )
}
