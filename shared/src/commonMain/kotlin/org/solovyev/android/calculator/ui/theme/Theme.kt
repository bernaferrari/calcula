package org.solovyev.android.calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState
import org.solovyev.android.calculator.ui.LocalCalculatorHighContrast

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seedColor: Color = Color(0xFF13ABF1), // Default Blue
    useDynamicColor: Boolean = false,
    isAmoled: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val seededTheme = rememberDynamicMaterialThemeState(
        seedColor = seedColor,
        style = PaletteStyle.Vibrant, // Changed to Vibrant for more expressiveness
        isDark = darkTheme,
        isAmoled = isAmoled,
        specVersion = ColorSpec.SpecVersion.SPEC_2025
    )
    val baseColorScheme = if (useDynamicColor) {
        platformDynamicColorScheme(darkTheme) ?: seededTheme.colorScheme
    } else {
        seededTheme.colorScheme
    }

    // Apply high contrast if enabled
    val colorScheme = if (highContrast) {
        createHighContrastColorScheme(baseColorScheme, darkTheme)
    } else {
        baseColorScheme
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

/**
 * Creates a true high contrast color scheme (WCAG 2.1 AAA - 7:1 ratio minimum).
 * Uses pure black/white with no transparency for maximum visibility.
 */
private fun createHighContrastColorScheme(baseScheme: ColorScheme, isDark: Boolean): ColorScheme {
    return if (isDark) {
        baseScheme.copy(
            // Primary colors - pure white on pure black for maximum contrast
            primary = Color(0xFFFFFFFF), // White - 21:1 contrast on black
            onPrimary = Color(0xFF000000), // Black
            primaryContainer = Color(0xFF000000),
            onPrimaryContainer = Color(0xFFFFFFFF),

            // Secondary colors - bright yellow for visibility
            secondary = Color(0xFFFFFF00), // Yellow - 20:1 contrast on black
            onSecondary = Color(0xFF000000),
            secondaryContainer = Color(0xFF1A1A1A),
            onSecondaryContainer = Color(0xFFFFFF00),

            // Tertiary colors - bright cyan
            tertiary = Color(0xFF00FFFF), // Cyan
            onTertiary = Color(0xFF000000),
            tertiaryContainer = Color(0xFF1A1A1A),
            onTertiaryContainer = Color(0xFF00FFFF),

            // Surface colors - pure black/white, no transparency
            background = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFF1A1A1A),
            onSurfaceVariant = Color(0xFFFFFFFF),
            surfaceTint = Color(0xFFFFFFFF),

            // Container colors - slightly lighter for boundaries
            surfaceContainer = Color(0xFF1A1A1A),
            surfaceContainerHigh = Color(0xFF2A2A2A),
            surfaceContainerHighest = Color(0xFF3A3A3A),
            surfaceContainerLow = Color(0xFF0A0A0A),
            surfaceContainerLowest = Color(0xFF000000),

            // Error colors - bright red
            error = Color(0xFFFF0000),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFF3A0000),
            onErrorContainer = Color(0xFFFF0000),

            // Outline - white for visibility
            outline = Color(0xFFFFFFFF),
            outlineVariant = Color(0xFFAAAAAA),

            // Inverse - for chips/tags
            inverseSurface = Color(0xFFFFFFFF),
            inverseOnSurface = Color(0xFF000000),
            inversePrimary = Color(0xFF000000),

            // Scrim - no transparency in high contrast
            scrim = Color(0xFF000000),

            // Surface dim/bright for tonal elevation
            surfaceDim = Color(0xFF000000),
            surfaceBright = Color(0xFF2A2A2A)
        )
    } else {
        // Light mode high contrast
        baseScheme.copy(
            // Primary colors - pure black on pure white
            primary = Color(0xFF000000),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFFFFF),
            onPrimaryContainer = Color(0xFF000000),

            // Secondary colors - dark blue
            secondary = Color(0xFF000080), // Navy
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF0F0F0),
            onSecondaryContainer = Color(0xFF000080),

            // Tertiary colors - dark teal
            tertiary = Color(0xFF006666),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFF0F0F0),
            onTertiaryContainer = Color(0xFF006666),

            // Surface colors - pure white/black, no transparency
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF000000),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF000000),
            surfaceVariant = Color(0xFFF0F0F0),
            onSurfaceVariant = Color(0xFF000000),
            surfaceTint = Color(0xFF000000),

            // Container colors - slightly darker for boundaries
            surfaceContainer = Color(0xFFF0F0F0),
            surfaceContainerHigh = Color(0xFFE0E0E0),
            surfaceContainerHighest = Color(0xFFD0D0D0),
            surfaceContainerLow = Color(0xFFFAFAFA),
            surfaceContainerLowest = Color(0xFFFFFFFF),

            // Error colors - dark red
            error = Color(0xFFCC0000),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFE0E0),
            onErrorContainer = Color(0xFFCC0000),

            // Outline - black for visibility
            outline = Color(0xFF000000),
            outlineVariant = Color(0xFF555555),

            // Inverse
            inverseSurface = Color(0xFF000000),
            inverseOnSurface = Color(0xFFFFFFFF),
            inversePrimary = Color(0xFFFFFFFF),

            // Scrim - no transparency
            scrim = Color(0xFF000000),

            // Surface dim/bright
            surfaceDim = Color(0xFFE0E0E0),
            surfaceBright = Color(0xFFFFFFFF)
        )
    }
}
