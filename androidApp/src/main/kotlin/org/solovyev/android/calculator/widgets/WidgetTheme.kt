package org.solovyev.android.calculator.widgets

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

/**
 * Simplified widget theme support - ColorProviders removed for build compatibility.
 */
object WidgetTheme {

    /**
     * Creates a semi-transparent background color based on opacity preference.
     */
    fun getBackgroundColor(isLightTheme: Boolean, opacity: Float = 1.0f): ColorProvider {
        val baseColor = if (isLightTheme) {
            Color(0xFFF5F5F5)
        } else {
            Color(0xFF121212)
        }
        val alpha = (opacity * 255).toInt().coerceIn(0, 255)
        return ColorProvider(baseColor.copy(alpha = alpha / 255f))
    }

    /**
     * Gets text color for display with error state support.
     */
    fun getDisplayTextColor(isLightTheme: Boolean, isError: Boolean): ColorProvider {
        return when {
            isError && isLightTheme -> ColorProvider(Color(0xFFB00020))
            isError && !isLightTheme -> ColorProvider(Color(0xFFFFB4A9))
            isLightTheme -> ColorProvider(Color(0xFF1C1B1F))
            else -> ColorProvider(Color(0xFFE6E1E5))
        }
    }

    /**
     * Gets button background color.
     */
    fun getButtonBackgroundColor(isLightTheme: Boolean, isOperation: Boolean = false): ColorProvider {
        return when {
            isOperation && isLightTheme -> ColorProvider(Color(0xFFE8DEF8))
            isOperation && !isLightTheme -> ColorProvider(Color(0xFF4A4458))
            isLightTheme -> ColorProvider(Color(0xFFE7E0EC))
            else -> ColorProvider(Color(0xFF2C2F33))
        }
    }

    /**
     * Gets button text color.
     */
    fun getButtonTextColor(isLightTheme: Boolean, isOperation: Boolean = false): ColorProvider {
        return when {
            isOperation && isLightTheme -> ColorProvider(Color(0xFF1D192B))
            isOperation && !isLightTheme -> ColorProvider(Color(0xFFE8DEF8))
            isLightTheme -> ColorProvider(Color(0xFF1C1B1F))
            else -> ColorProvider(Color(0xFFE6E1E5))
        }
    }
}

/**
 * Data class for widget appearance preferences.
 */
data class WidgetAppearancePreferences(
    val useDynamicColors: Boolean = true,
    val isLightTheme: Boolean = false,
    val backgroundOpacity: Float = 1.0f,
    val buttonColor: Int? = null,
    val cornerRadius: WidgetCornerRadius = WidgetCornerRadius.MEDIUM,
    val enableHaptics: Boolean = true
)

enum class WidgetCornerRadius {
    SMALL,
    MEDIUM,
    LARGE,
    FULL
}
