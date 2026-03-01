package org.solovyev.android.calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState
import org.solovyev.android.calculator.ui.LocalCalculatorHighContrast

// =============================================================================
// CALCULATOR THEME CONFIGURATION
// =============================================================================
// Comprehensive theming system supporting:
// - Material 3 dynamic theming with Material You
// - High contrast accessibility mode (WCAG AAA)
// - AMOLED-optimized true black dark mode
// - Multiple seed color palettes
// - Calculator-specific color roles
// =============================================================================

/**
 * Theme mode options for the calculator
 */
enum class CalculatorThemeMode {
    SYSTEM,      // Follow system setting
    LIGHT,       // Force light mode
    DARK,        // Force dark mode
    AMOLED       // Force AMOLED black mode
}

/**
 * CompositionLocal for accessing extended color scheme throughout the app
 */
val LocalCalculatorColors = staticCompositionLocalOf<CalculatorColorScheme> {
    error("CalculatorColorScheme not provided")
}

/**
 * Extended color scheme with calculator-specific color roles
 * Extends Material 3 ColorScheme with calculator-specific semantic colors
 */
data class CalculatorColorScheme(
    // Material 3 base color scheme
    val material: ColorScheme,
    
    // Display area colors
    val displayBackground: Color,
    val displayTextPrimary: Color,
    val displayTextSecondary: Color,
    val displayResult: Color,
    val displayError: Color,
    
    // Button colors - Digits
    val digitButtonBackground: Color,
    val digitButtonText: Color,
    val digitButtonPressed: Color,
    
    // Button colors - Operators
    val operatorButtonBackground: Color,
    val operatorButtonText: Color,
    val operatorButtonPressed: Color,
    
    // Button colors - Functions
    val functionButtonBackground: Color,
    val functionButtonText: Color,
    val functionButtonPressed: Color,
    
    // Button colors - Controls (AC, delete, etc.)
    val controlButtonBackground: Color,
    val controlButtonText: Color,
    val controlButtonPressed: Color,
    
    // Button colors - Memory
    val memoryButtonBackground: Color,
    val memoryButtonText: Color,
    val memoryButtonPressed: Color,
    
    // Button colors - Equals (primary action)
    val equalsButtonBackground: Color,
    val equalsButtonText: Color,
    val equalsButtonPressed: Color,
    
    // Button colors - Scientific
    val scientificButtonBackground: Color,
    val scientificButtonText: Color,
    val scientificButtonPressed: Color,
    
    // State colors
    val pressedOverlay: Color,
    val disabledBackground: Color,
    val disabledText: Color,
    val focusRing: Color,
    val hoverOverlay: Color,
    val selectedBackground: Color,
    val selectedText: Color,
    
    // Utility colors
    val isDark: Boolean,
    val isAmoled: Boolean,
    val isHighContrast: Boolean
)

/**
 * Main calculator theme composable
 * 
 * @param darkTheme Whether to use dark theme (overridden by themeMode)
 * @param seedColor The seed color for dynamic theming
 * @param useDynamicColor Whether to use Android 12+ dynamic colors
 * @param isAmoled Whether to use true black backgrounds for OLED displays
 * @param highContrast Whether to enable high contrast accessibility mode
 * @param themeMode The theme mode to use (system, light, dark, amoled)
 * @param content The content to theme
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    seedColor: Color = SeedColors.Default,
    useDynamicColor: Boolean = false,
    isAmoled: Boolean = false,
    highContrast: Boolean = false,
    themeMode: CalculatorThemeMode = CalculatorThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    // Determine effective dark mode based on theme mode
    val effectiveDarkTheme = when (themeMode) {
        CalculatorThemeMode.LIGHT -> false
        CalculatorThemeMode.DARK, CalculatorThemeMode.AMOLED -> true
        CalculatorThemeMode.SYSTEM -> darkTheme
    }
    
    val effectiveAmoled = themeMode == CalculatorThemeMode.AMOLED || 
                          (isAmoled && effectiveDarkTheme)

    // Generate base color scheme using MaterialKolor
    val seededTheme = rememberDynamicMaterialThemeState(
        seedColor = seedColor,
        style = PaletteStyle.Vibrant,
        isDark = effectiveDarkTheme,
        isAmoled = effectiveAmoled,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        contrastLevel = if (highContrast) 1.0 else 0.0
    )
    
    // Get platform dynamic colors if available and requested
    val baseColorScheme = if (useDynamicColor) {
        platformDynamicColorScheme(effectiveDarkTheme) ?: seededTheme.colorScheme
    } else {
        seededTheme.colorScheme
    }

    // Apply theme modifications based on mode
    val modifiedColorScheme = when {
        highContrast -> createHighContrastColorScheme(baseColorScheme, effectiveDarkTheme)
        effectiveAmoled -> createAmoledColorScheme(baseColorScheme)
        else -> enhanceColorScheme(baseColorScheme, effectiveDarkTheme)
    }

    // Create calculator-specific extended color scheme
    val calculatorColors = createCalculatorColorScheme(
        materialScheme = modifiedColorScheme,
        isDark = effectiveDarkTheme,
        isAmoled = effectiveAmoled,
        isHighContrast = highContrast,
        seedColor = seedColor
    )

    // Refined shapes following Material 3 guidelines
    val shapes = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp)
    )

    // Provide both Material theme and extended calculator colors
    CompositionLocalProvider(
        LocalCalculatorColors provides calculatorColors,
        LocalCalculatorHighContrast provides highContrast
    ) {
        MaterialTheme(
            colorScheme = modifiedColorScheme,
            shapes = shapes,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Creates the extended calculator color scheme with specific roles for calculator UI
 */
private fun createCalculatorColorScheme(
    materialScheme: ColorScheme,
    isDark: Boolean,
    isAmoled: Boolean,
    isHighContrast: Boolean,
    seedColor: Color
): CalculatorColorScheme {
    return if (isHighContrast) {
        createHighContrastCalculatorScheme(materialScheme, isDark)
    } else if (isAmoled) {
        createAmoledCalculatorScheme(materialScheme)
    } else if (isDark) {
        createDarkCalculatorScheme(materialScheme, seedColor)
    } else {
        createLightCalculatorScheme(materialScheme, seedColor)
    }
}

/**
 * Light theme calculator color scheme
 */
private fun createLightCalculatorScheme(
    material: ColorScheme,
    seedColor: Color
): CalculatorColorScheme {
    // Determine button colors based on seed color for harmonious theming
    val isWarmSeed = seedColor.red > seedColor.blue
    val displayBg = if (isWarmSeed) WarmGray50 else CoolGray50
    
    return CalculatorColorScheme(
        material = material,
        
        // Display
        displayBackground = DisplayColors.BackgroundLight,
        displayTextPrimary = DisplayColors.TextPrimaryLight,
        displayTextSecondary = DisplayColors.TextSecondaryLight,
        displayResult = material.primary,
        displayError = ErrorColor,
        
        // Digit buttons
        digitButtonBackground = ButtonColors.DigitBackgroundLight,
        digitButtonText = ButtonColors.DigitTextLight,
        digitButtonPressed = ButtonColors.DigitPressedLight,
        
        // Operator buttons
        operatorButtonBackground = ButtonColors.OperatorBackgroundLight,
        operatorButtonText = ButtonColors.OperatorTextLight,
        operatorButtonPressed = ButtonColors.OperatorPressedLight,
        
        // Function buttons
        functionButtonBackground = ButtonColors.FunctionBackgroundLight,
        functionButtonText = ButtonColors.FunctionTextLight,
        functionButtonPressed = ButtonColors.FunctionPressedLight,
        
        // Control buttons
        controlButtonBackground = ButtonColors.ControlBackgroundLight,
        controlButtonText = ButtonColors.ControlTextLight,
        controlButtonPressed = ButtonColors.ControlPressedLight,
        
        // Memory buttons
        memoryButtonBackground = ButtonColors.MemoryBackgroundLight,
        memoryButtonText = ButtonColors.MemoryTextLight,
        memoryButtonPressed = ButtonColors.MemoryPressedLight,
        
        // Equals button - Use primary color for hero action
        equalsButtonBackground = material.primary,
        equalsButtonText = material.onPrimary,
        equalsButtonPressed = material.primary.copy(alpha = 0.85f),
        
        // Scientific buttons
        scientificButtonBackground = ButtonColors.ScientificBackgroundLight,
        scientificButtonText = ButtonColors.ScientificTextLight,
        scientificButtonPressed = ButtonColors.ScientificBackgroundLight.copy(alpha = 0.8f),
        
        // State colors
        pressedOverlay = StateColors.PressedOverlayLight,
        disabledBackground = StateColors.DisabledBackgroundLight,
        disabledText = StateColors.DisabledTextLight,
        focusRing = material.primary,
        hoverOverlay = StateColors.HoverOverlayLight,
        selectedBackground = StateColors.SelectedBackgroundLight,
        selectedText = StateColors.SelectedTextLight,
        
        // Meta
        isDark = false,
        isAmoled = false,
        isHighContrast = false
    )
}

/**
 * Dark theme calculator color scheme
 */
private fun createDarkCalculatorScheme(
    material: ColorScheme,
    seedColor: Color
): CalculatorColorScheme {
    return CalculatorColorScheme(
        material = material,
        
        // Display
        displayBackground = DisplayColors.BackgroundDark,
        displayTextPrimary = DisplayColors.TextPrimaryDark,
        displayTextSecondary = DisplayColors.TextSecondaryDark,
        displayResult = material.primary,
        displayError = ErrorColorDark,
        
        // Digit buttons
        digitButtonBackground = ButtonColors.DigitBackgroundDark,
        digitButtonText = ButtonColors.DigitTextDark,
        digitButtonPressed = ButtonColors.DigitPressedDark,
        
        // Operator buttons
        operatorButtonBackground = ButtonColors.OperatorBackgroundDark,
        operatorButtonText = ButtonColors.OperatorTextDark,
        operatorButtonPressed = ButtonColors.OperatorPressedDark,
        
        // Function buttons
        functionButtonBackground = ButtonColors.FunctionBackgroundDark,
        functionButtonText = ButtonColors.FunctionTextDark,
        functionButtonPressed = ButtonColors.FunctionPressedDark,
        
        // Control buttons
        controlButtonBackground = ButtonColors.ControlBackgroundDark,
        controlButtonText = ButtonColors.ControlTextDark,
        controlButtonPressed = ButtonColors.ControlPressedDark,
        
        // Memory buttons
        memoryButtonBackground = ButtonColors.MemoryBackgroundDark,
        memoryButtonText = ButtonColors.MemoryTextDark,
        memoryButtonPressed = ButtonColors.MemoryPressedDark,
        
        // Equals button
        equalsButtonBackground = material.primary,
        equalsButtonText = material.onPrimary,
        equalsButtonPressed = material.primary.copy(alpha = 0.85f),
        
        // Scientific buttons
        scientificButtonBackground = ButtonColors.ScientificBackgroundDark,
        scientificButtonText = ButtonColors.ScientificTextDark,
        scientificButtonPressed = ButtonColors.ScientificBackgroundDark.copy(alpha = 0.8f),
        
        // State colors
        pressedOverlay = StateColors.PressedOverlayDark,
        disabledBackground = StateColors.DisabledBackgroundDark,
        disabledText = StateColors.DisabledTextDark,
        focusRing = material.primary,
        hoverOverlay = StateColors.HoverOverlayDark,
        selectedBackground = StateColors.SelectedBackgroundDark,
        selectedText = StateColors.SelectedTextDark,
        
        // Meta
        isDark = true,
        isAmoled = false,
        isHighContrast = false
    )
}

/**
 * AMOLED-optimized calculator color scheme with true blacks
 */
private fun createAmoledCalculatorScheme(
    material: ColorScheme
): CalculatorColorScheme {
    return CalculatorColorScheme(
        material = material,
        
        // Display - True blacks
        displayBackground = DisplayColors.BackgroundAmoled,
        displayTextPrimary = Color(0xFFFFFFFF),
        displayTextSecondary = Color(0xFF98989D),
        displayResult = material.primary,
        displayError = Error400,
        
        // Digit buttons - Elevated from pure black
        digitButtonBackground = ButtonColors.DigitBackgroundAmoled,
        digitButtonText = Color(0xFFFFFFFF),
        digitButtonPressed = ButtonColors.DigitPressedAmoled,
        
        // Operator buttons
        operatorButtonBackground = ButtonColors.OperatorBackgroundAmoled,
        operatorButtonText = Color(0xFFFFFFFF),
        operatorButtonPressed = ButtonColors.OperatorPressedAmoled,
        
        // Function buttons
        functionButtonBackground = ButtonColors.FunctionBackgroundAmoled,
        functionButtonText = Color(0xFF8E8E93),
        functionButtonPressed = ButtonColors.FunctionPressedAmoled,
        
        // Control buttons
        controlButtonBackground = ButtonColors.ControlBackgroundAmoled,
        controlButtonText = Color(0xFFFF453A),
        controlButtonPressed = ButtonColors.ControlPressedAmoled,
        
        // Memory buttons
        memoryButtonBackground = ButtonColors.MemoryBackgroundAmoled,
        memoryButtonText = ButtonColors.MemoryTextDark,
        memoryButtonPressed = ButtonColors.MemoryPressedAmoled,
        
        // Equals button - Vibrant on black
        equalsButtonBackground = material.primary,
        equalsButtonText = Color(0xFF000000),
        equalsButtonPressed = material.primary.copy(alpha = 0.9f),
        
        // Scientific buttons
        scientificButtonBackground = ButtonColors.ScientificBackgroundAmoled,
        scientificButtonText = ButtonColors.ScientificTextDark,
        scientificButtonPressed = ButtonColors.ScientificBackgroundAmoled.copy(alpha = 0.8f),
        
        // State colors
        pressedOverlay = StateColors.PressedOverlayAmoled,
        disabledBackground = StateColors.DisabledBackgroundAmoled,
        disabledText = StateColors.DisabledTextAmoled,
        focusRing = Color(0xFF5CA8FF),
        hoverOverlay = Color(0x0DFFFFFF),
        selectedBackground = material.primary.copy(alpha = 0.2f),
        selectedText = material.primary,
        
        // Meta
        isDark = true,
        isAmoled = true,
        isHighContrast = false
    )
}

/**
 * High contrast calculator color scheme (WCAG AAA)
 */
private fun createHighContrastCalculatorScheme(
    material: ColorScheme,
    isDark: Boolean
): CalculatorColorScheme {
    return if (isDark) {
        CalculatorColorScheme(
            material = material,
            
            // Display
            displayBackground = Color(0xFF000000),
            displayTextPrimary = Color(0xFFFFFFFF),
            displayTextSecondary = Color(0xFFB0B0B5),
            displayResult = Color(0xFF6B8CFF),
            displayError = Color(0xFFFF6B6B),
            
            // All buttons with maximum contrast
            digitButtonBackground = Color(0xFF2A2A2A),
            digitButtonText = Color(0xFFFFFFFF),
            digitButtonPressed = Color(0xFF3A3A3A),
            
            operatorButtonBackground = Color(0xFF3A3A3A),
            operatorButtonText = Color(0xFFFFFFFF),
            operatorButtonPressed = Color(0xFF4A4A4A),
            
            functionButtonBackground = Color(0xFF1A1A1A),
            functionButtonText = Color(0xFFB0B0B5),
            functionButtonPressed = Color(0xFF2A2A2A),
            
            controlButtonBackground = Color(0xFF3D1515),
            controlButtonText = Color(0xFFFF6B6B),
            controlButtonPressed = Color(0xFF5C2A2A),
            
            memoryButtonBackground = Color(0xFF2A1F35),
            memoryButtonText = Color(0xFFD0A8FF),
            memoryButtonPressed = Color(0xFF3D2A4A),
            
            equalsButtonBackground = Color(0xFF6B8CFF),
            equalsButtonText = Color(0xFF000000),
            equalsButtonPressed = Color(0xFF8AA4FF),
            
            scientificButtonBackground = Color(0xFF2A2015),
            scientificButtonText = Color(0xFFFFB84D),
            scientificButtonPressed = Color(0xFF4A3A2A),
            
            // State colors
            pressedOverlay = Color(0x1AFFFFFF),
            disabledBackground = Color(0xFF1A1A1A),
            disabledText = Color(0xFF6B6B6B),
            focusRing = Color(0xFF6B8CFF),
            hoverOverlay = Color(0x0DFFFFFF),
            selectedBackground = Color(0xFF1A1F35),
            selectedText = Color(0xFFB8C5FF),
            
            isDark = true,
            isAmoled = false,
            isHighContrast = true
        )
    } else {
        CalculatorColorScheme(
            material = material,
            
            // Display
            displayBackground = Color(0xFFFFFFFF),
            displayTextPrimary = Color(0xFF000000),
            displayTextSecondary = Color(0xFF424242),
            displayResult = HighContrastColors.Primary,
            displayError = HighContrastColors.Error,
            
            // All buttons with maximum contrast
            digitButtonBackground = Color(0xFFF0F0F0),
            digitButtonText = Color(0xFF000000),
            digitButtonPressed = Color(0xFFD0D0D0),
            
            operatorButtonBackground = Color(0xFFE0E0E0),
            operatorButtonText = Color(0xFF000000),
            operatorButtonPressed = Color(0xFFC0C0C0),
            
            functionButtonBackground = Color(0xFFF5F5F5),
            functionButtonText = Color(0xFF424242),
            functionButtonPressed = Color(0xFFE0E0E0),
            
            controlButtonBackground = Color(0xFFFFE5E5),
            controlButtonText = Color(0xFFB00020),
            controlButtonPressed = Color(0xFFFFCCCC),
            
            memoryButtonBackground = Color(0xFFF5E6FF),
            memoryButtonText = Color(0xFF6B2C8C),
            memoryButtonPressed = Color(0xFFEBD4FF),
            
            equalsButtonBackground = HighContrastColors.Primary,
            equalsButtonText = Color(0xFFFFFFFF),
            equalsButtonPressed = Color(0xFF003380),
            
            scientificButtonBackground = Color(0xFFFFF4E6),
            scientificButtonText = Color(0xFF8B4500),
            scientificButtonPressed = Color(0xFFFFE4CC),
            
            // State colors
            pressedOverlay = Color(0x14000000),
            disabledBackground = Color(0xFFF0F0F0),
            disabledText = Color(0xFF9E9E9E),
            focusRing = HighContrastColors.Primary,
            hoverOverlay = Color(0x0A000000),
            selectedBackground = Color(0xFFE8F0FF),
            selectedText = Color(0xFF0051CC),
            
            isDark = false,
            isAmoled = false,
            isHighContrast = true
        )
    }
}

/**
 * Enhances base color scheme with calculator-specific adjustments
 */
private fun enhanceColorScheme(
    baseScheme: ColorScheme,
    isDark: Boolean
): ColorScheme {
    return baseScheme
}

/**
 * Creates an accessible high contrast color scheme (WCAG 2.1 AAA - 7:1 ratio minimum).
 * Uses refined colors that maintain high contrast while being visually comfortable.
 */
private fun createHighContrastColorScheme(baseScheme: ColorScheme, isDark: Boolean): ColorScheme {
    return if (isDark) {
        baseScheme.copy(
            // Primary colors - bright blue for visibility on dark
            primary = Color(0xFF6B8CFF),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFF1A1F35),
            onPrimaryContainer = Color(0xFFB8C5FF),

            // Secondary colors - teal accent
            secondary = Color(0xFF4CD9D9),
            onSecondary = Color(0xFF000000),
            secondaryContainer = Color(0xFF0A2E2E),
            onSecondaryContainer = Color(0xFF8AEDED),

            // Tertiary colors - soft purple
            tertiary = Color(0xFFD0A8FF),
            onTertiary = Color(0xFF000000),
            tertiaryContainer = Color(0xFF2A1F35),
            onTertiaryContainer = Color(0xFFE8D5FF),

            // Surface colors - near black with subtle warmth
            background = Color(0xFF0A0A0C),
            onBackground = Color(0xFFF0F0F5),
            surface = Color(0xFF0A0A0C),
            onSurface = Color(0xFFF0F0F5),
            surfaceVariant = Color(0xFF1A1A20),
            onSurfaceVariant = Color(0xFFD0D0D8),
            surfaceTint = Color(0xFF6B8CFF),

            // Container colors - clear hierarchy
            surfaceContainer = Color(0xFF15151A),
            surfaceContainerHigh = Color(0xFF202028),
            surfaceContainerHighest = Color(0xFF2A2A35),
            surfaceContainerLow = Color(0xFF101014),
            surfaceContainerLowest = Color(0xFF0A0A0C),

            // Error colors - accessible red
            error = Color(0xFFFF6B6B),
            onError = Color(0xFF000000),
            errorContainer = Color(0xFF3D1515),
            onErrorContainer = Color(0xFFFF9E9E),

            // Outline - visible but not harsh
            outline = Color(0xFF6B6B7B),
            outlineVariant = Color(0xFF4A4A58),

            // Inverse
            inverseSurface = Color(0xFFF0F0F5),
            inverseOnSurface = Color(0xFF0A0A0C),
            inversePrimary = Color(0xFF0A0A0C),

            // Scrim
            scrim = Color(0xFF000000),

            // Surface dim/bright
            surfaceDim = Color(0xFF050508),
            surfaceBright = Color(0xFF202028)
        )
    } else {
        // Light mode high contrast
        baseScheme.copy(
            // Primary colors - strong blue
            primary = Color(0xFF0051CC),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE8F0FF),
            onPrimaryContainer = Color(0xFF003380),

            // Secondary colors - deep teal
            secondary = Color(0xFF006B6B),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFE0F5F5),
            onSecondaryContainer = Color(0xFF004040),

            // Tertiary colors - rich purple
            tertiary = Color(0xFF6B2C8C),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFF5E8FF),
            onTertiaryContainer = Color(0xFF4A1A66),

            // Surface colors - clean white with subtle warmth
            background = Color(0xFFFDFDFD),
            onBackground = Color(0xFF1A1A1F),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1A1F),
            surfaceVariant = Color(0xFFF5F5F8),
            onSurfaceVariant = Color(0xFF4A4A55),
            surfaceTint = Color(0xFF0051CC),

            // Container colors - clear hierarchy
            surfaceContainer = Color(0xFFF5F5F8),
            surfaceContainerHigh = Color(0xFFECECF0),
            surfaceContainerHighest = Color(0xFFE0E0E8),
            surfaceContainerLow = Color(0xFFFAFAFC),
            surfaceContainerLowest = Color(0xFFFFFFFF),

            // Error colors - accessible red
            error = Color(0xFFB00020),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFE5E8),
            onErrorContainer = Color(0xFF8B0018),

            // Outline - visible boundaries
            outline = Color(0xFF6B6B7B),
            outlineVariant = Color(0xFFC5C5D0),

            // Inverse
            inverseSurface = Color(0xFF1A1A1F),
            inverseOnSurface = Color(0xFFFDFDFD),
            inversePrimary = Color(0xFFFFFFFF),

            // Scrim
            scrim = Color(0xFF000000),

            // Surface dim/bright
            surfaceDim = Color(0xFFE0E0E8),
            surfaceBright = Color(0xFFFFFFFF)
        )
    }
}

/**
 * Creates a pure AMOLED black theme for maximum battery savings on OLED displays.
 * Uses true black (#000000) for backgrounds with vibrant accent colors.
 */
private fun createAmoledColorScheme(baseScheme: ColorScheme): ColorScheme {
    return baseScheme.copy(
        // Pure black backgrounds
        background = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF000000),
        onSurface = Color(0xFFFFFFFF),

        // Elevated surfaces with minimal brightness
        surfaceContainer = Color(0xFF0A0A0A),
        surfaceContainerHigh = Color(0xFF121212),
        surfaceContainerHighest = Color(0xFF1A1A1A),
        surfaceContainerLow = Color(0xFF050505),
        surfaceContainerLowest = Color(0xFF000000),
        surfaceVariant = Color(0xFF1C1C1E),
        onSurfaceVariant = Color(0xFFB0B0B5),

        // Vibrant accents on black
        primary = baseScheme.primary.copy(
            red = (baseScheme.primary.red * 1.1f).coerceAtMost(1f),
            green = (baseScheme.primary.green * 1.1f).coerceAtMost(1f),
            blue = (baseScheme.primary.blue * 1.1f).coerceAtMost(1f)
        ),
        onPrimary = Color(0xFF000000),
        primaryContainer = baseScheme.primary.copy(alpha = 0.15f),
        onPrimaryContainer = baseScheme.primary,

        // Enhanced secondary
        secondary = baseScheme.secondary.copy(
            red = (baseScheme.secondary.red * 1.05f).coerceAtMost(1f),
            green = (baseScheme.secondary.green * 1.05f).coerceAtMost(1f),
            blue = (baseScheme.secondary.blue * 1.05f).coerceAtMost(1f)
        ),
        onSecondary = Color(0xFF000000),
        secondaryContainer = baseScheme.secondary.copy(alpha = 0.12f),
        onSecondaryContainer = baseScheme.secondary,

        // Refined tertiary
        tertiary = baseScheme.tertiary,
        onTertiary = Color(0xFF000000),
        tertiaryContainer = baseScheme.tertiary.copy(alpha = 0.12f),
        onTertiaryContainer = baseScheme.tertiary,

        // Error with better visibility on black
        error = Color(0xFFFF6B6B),
        onError = Color(0xFF000000),
        errorContainer = Color(0xFF2A1515),
        onErrorContainer = Color(0xFFFF8A8A),

        // Outlines for definition
        outline = Color(0xFF3A3A3C),
        outlineVariant = Color(0xFF2C2C2E),

        // Inverse for contrast
        inverseSurface = Color(0xFFE5E5EA),
        inverseOnSurface = Color(0xFF000000),
        inversePrimary = Color(0xFF000000),

        // Scrim and brightness
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFF000000),
        surfaceBright = Color(0xFF1C1C1E),
        surfaceTint = baseScheme.primary
    )
}

// =============================================================================
// HELPER FUNCTIONS AND EXTENSIONS
// =============================================================================

/**
 * Helper function to get the appropriate calculator colors
 */
@Composable
fun calculatorColors(): CalculatorColorScheme {
    return LocalCalculatorColors.current
}

/**
 * Extension function to get button background color by type
 */
fun CalculatorColorScheme.buttonBackground(type: org.solovyev.android.calculator.ui.ButtonType): Color {
    return when (type) {
        org.solovyev.android.calculator.ui.ButtonType.DIGIT -> digitButtonBackground
        org.solovyev.android.calculator.ui.ButtonType.OPERATION -> operatorButtonBackground
        org.solovyev.android.calculator.ui.ButtonType.OPERATION_HIGHLIGHTED -> equalsButtonBackground
        org.solovyev.android.calculator.ui.ButtonType.CONTROL -> controlButtonBackground
        org.solovyev.android.calculator.ui.ButtonType.SPECIAL -> scientificButtonBackground
        org.solovyev.android.calculator.ui.ButtonType.MEMORY -> memoryButtonBackground
    }
}

/**
 * Extension function to get button text color by type
 */
fun CalculatorColorScheme.buttonText(type: org.solovyev.android.calculator.ui.ButtonType): Color {
    return when (type) {
        org.solovyev.android.calculator.ui.ButtonType.DIGIT -> digitButtonText
        org.solovyev.android.calculator.ui.ButtonType.OPERATION -> operatorButtonText
        org.solovyev.android.calculator.ui.ButtonType.OPERATION_HIGHLIGHTED -> equalsButtonText
        org.solovyev.android.calculator.ui.ButtonType.CONTROL -> controlButtonText
        org.solovyev.android.calculator.ui.ButtonType.SPECIAL -> scientificButtonText
        org.solovyev.android.calculator.ui.ButtonType.MEMORY -> memoryButtonText
    }
}

/**
 * Extension function to get button pressed color by type
 */
fun CalculatorColorScheme.buttonPressed(type: org.solovyev.android.calculator.ui.ButtonType): Color {
    return when (type) {
        org.solovyev.android.calculator.ui.ButtonType.DIGIT -> digitButtonPressed
        org.solovyev.android.calculator.ui.ButtonType.OPERATION -> operatorButtonPressed
        org.solovyev.android.calculator.ui.ButtonType.OPERATION_HIGHLIGHTED -> equalsButtonPressed
        org.solovyev.android.calculator.ui.ButtonType.CONTROL -> controlButtonPressed
        org.solovyev.android.calculator.ui.ButtonType.SPECIAL -> scientificButtonPressed
        org.solovyev.android.calculator.ui.ButtonType.MEMORY -> memoryButtonPressed
    }
}
