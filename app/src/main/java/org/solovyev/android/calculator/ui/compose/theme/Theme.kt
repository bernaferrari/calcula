package org.solovyev.android.calculator.ui.compose.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicMaterialThemeState
import org.solovyev.android.calculator.Preferences

// Seed colors for theming
private val BlueSeed = Color(0xFF1565C0)       // Blue 800
private val DeepBlueSeed = Color(0xFF0A3980)   // Deep Blue
private val TealSeed = Color(0xFF009688)       // Teal 500
private val GreenSeed = Color(0xFF2E7D32)      // Green 800
private val PurpleSeed = Color(0xFF7B1FA2)     // Purple 700
private val BlackSeed = Color(0xFF212121)      // Grey 900

/**
 * Main theme composable that accepts the app's Preferences.Gui.Theme enum.
 * 
 * @param theme The theme preference from settings (defaults to material_theme)
 * @param useDynamicColor If true, uses Material You (system) colors on Android 12+
 * @param content The content to display
 */
@Composable
fun CalculatorTheme(
    theme: Preferences.Gui.Theme? = null,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    
    // Use provided theme or default based on system dark mode
    val resolvedTheme = theme ?: if (systemDarkTheme) {
        Preferences.Gui.Theme.material_theme
    } else {
        Preferences.Gui.Theme.material_light_theme
    }
    
    // Determine if this theme is light or dark
    val isLightTheme = resolvedTheme.light
    
    val colorScheme = when {
        // Material You - use system dynamic colors (also when material_you_theme is selected)
        (useDynamicColor || resolvedTheme == Preferences.Gui.Theme.material_you_theme) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isLightTheme) {
                dynamicLightColorScheme(context)
            } else {
                dynamicDarkColorScheme(context)
            }
        }
        // Use materialkolor to generate harmonious color scheme
        else -> {
            val (seedColor, isDark) = when (resolvedTheme) {
                Preferences.Gui.Theme.material_theme -> DeepBlueSeed to true
                Preferences.Gui.Theme.material_black_theme -> BlackSeed to true
                Preferences.Gui.Theme.material_light_theme -> BlueSeed to false
                Preferences.Gui.Theme.material_you_theme -> BlueSeed to true // Fallback for < S
                Preferences.Gui.Theme.metro_blue_theme -> BlueSeed to !systemDarkTheme.not()
                Preferences.Gui.Theme.metro_green_theme -> GreenSeed to !systemDarkTheme.not()
                Preferences.Gui.Theme.metro_purple_theme -> PurpleSeed to !systemDarkTheme.not()
                Preferences.Gui.Theme.default_theme -> BlueSeed to systemDarkTheme
                Preferences.Gui.Theme.violet_theme -> PurpleSeed to true
                Preferences.Gui.Theme.light_blue_theme -> TealSeed to true
            }
            
            rememberDynamicMaterialThemeState(
                seedColor = seedColor,
                isDark = isDark,
                isAmoled = resolvedTheme == Preferences.Gui.Theme.material_black_theme,
                style = PaletteStyle.TonalSpot,
            ).colorScheme
        }
    }

    // Update status bar appearance
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = isLightTheme
            windowInsetsController.isAppearanceLightNavigationBars = isLightTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CalculatorTypography,
        content = content
    )
}
