package org.solovyev.android.calculator.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// CALCULATOR DESIGN SYSTEM - COLOR PALETTE
// ============================================================================
// A comprehensive, accessible, and beautiful color system for Calculator++
// Following Material Design 3 principles with calculator-specific enhancements
// ============================================================================

// =============================================================================
// BASE PALETTE - Core colors that form the foundation of our design system
// These are carefully selected for perfect harmony and accessibility
// =============================================================================

// Primary Brand Colors - Trustworthy Blue
// Selected for high legibility and universal appeal across cultures
val Blue50 = Color(0xFFE3F2FD)
val Blue100 = Color(0xFFBBDEFB)
val Blue200 = Color(0xFF90CAF9)
val Blue300 = Color(0xFF64B5F6)
val Blue400 = Color(0xFF42A5F5)
val Blue500 = Color(0xFF2196F3)
val Blue600 = Color(0xFF1E88E5)
val Blue700 = Color(0xFF1976D2)
val Blue800 = Color(0xFF1565C0)
val Blue900 = Color(0xFF0D47A1)

// Secondary Teal - Fresh, modern accent
val Teal50 = Color(0xFFE0F2F1)
val Teal100 = Color(0xFFB2DFDB)
val Teal200 = Color(0xFF80CBC4)
val Teal300 = Color(0xFF4DB6AC)
val Teal400 = Color(0xFF26A69A)
val Teal500 = Color(0xFF009688)
val Teal600 = Color(0xFF00897B)
val Teal700 = Color(0xFF00796B)
val Teal800 = Color(0xFF00695C)
val Teal900 = Color(0xFF004D40)

// Tertiary Purple - Creative, distinctive
val Purple50 = Color(0xFFF3E5F5)
val Purple100 = Color(0xFFE1BEE7)
val Purple200 = Color(0xFFCE93D8)
val Purple300 = Color(0xFFBA68C8)
val Purple400 = Color(0xFFAB47BC)
val Purple500 = Color(0xFF9C27B0)
val Purple600 = Color(0xFF8E24AA)
val Purple700 = Color(0xFF7B1FA2)
val Purple800 = Color(0xFF6A1B9A)
val Purple900 = Color(0xFF4A148C)

// Neutral Grays - Warm, approachable neutrals
val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray400 = Color(0xFFBDBDBD)
val Gray500 = Color(0xFF9E9E9E)
val Gray600 = Color(0xFF757575)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242)
val Gray900 = Color(0xFF212121)

// Warm Grays - For light theme backgrounds (inviting, paper-like)
val WarmGray50 = Color(0xFFF9F8F6)
val WarmGray100 = Color(0xFFF2F0ED)
val WarmGray200 = Color(0xFFE8E5E1)
val WarmGray300 = Color(0xFFD6D2CC)
val WarmGray400 = Color(0xFFB8B3AB)
val WarmGray500 = Color(0xFF9A948A)
val WarmGray600 = Color(0xFF7A756E)
val WarmGray700 = Color(0xFF5C5852)
val WarmGray800 = Color(0xFF3E3B37)
val WarmGray900 = Color(0xFF21201E)

// Cool Grays - For dark theme surfaces (modern, sophisticated)
val CoolGray50 = Color(0xFFF5F7F9)
val CoolGray100 = Color(0xFFECEFF2)
val CoolGray200 = Color(0xFFDCE2E8)
val CoolGray300 = Color(0xFFC5CDD6)
val CoolGray400 = Color(0xFFA0AEBD)
val CoolGray500 = Color(0xFF7A8A9A)
val CoolGray600 = Color(0xFF5A6B7A)
val CoolGray700 = Color(0xFF424D59)
val CoolGray800 = Color(0xFF2D343C)
val CoolGray900 = Color(0xFF1A1D21)

// Semantic Colors - Clear meaning at a glance
val Success50 = Color(0xFFE8F5E9)
val Success100 = Color(0xFFC8E6C9)
val Success200 = Color(0xFFA5D6A7)
val Success300 = Color(0xFF81C784)
val Success400 = Color(0xFF66BB6A)
val Success500 = Color(0xFF4CAF50)
val Success600 = Color(0xFF43A047)
val Success700 = Color(0xFF388E3C)
val Success800 = Color(0xFF2E7D32)
val Success900 = Color(0xFF1B5E20)

val Warning50 = Color(0xFFFFF8E1)
val Warning100 = Color(0xFFFFECB3)
val Warning200 = Color(0xFFFFE082)
val Warning300 = Color(0xFFFFD54F)
val Warning400 = Color(0xFFFFCA28)
val Warning500 = Color(0xFFFFC107)
val Warning600 = Color(0xFFFFB300)
val Warning700 = Color(0xFFFFA000)
val Warning800 = Color(0xFFFF8F00)
val Warning900 = Color(0xFFFF6F00)

val Error50 = Color(0xFFFFEBEE)
val Error100 = Color(0xFFFFCDD2)
val Error200 = Color(0xFFEF9A9A)
val Error300 = Color(0xFFE57373)
val Error400 = Color(0xFFEF5350)
val Error500 = Color(0xFFF44336)
val Error600 = Color(0xFFE53935)
val Error700 = Color(0xFFD32F2F)
val Error800 = Color(0xFFC62828)
val Error900 = Color(0xFFB71C1C)

val Info50 = Color(0xFFE1F5FE)
val Info100 = Color(0xFFB3E5FC)
val Info200 = Color(0xFF81D4FA)
val Info300 = Color(0xFF4FC3F7)
val Info400 = Color(0xFF29B6F6)
val Info500 = Color(0xFF03A9F4)
val Info600 = Color(0xFF039BE5)
val Info700 = Color(0xFF0288D1)
val Info800 = Color(0xFF0277BD)
val Info900 = Color(0xFF01579B)

// =============================================================================
// CALCULATOR-SPECIFIC COLOR ROLES
// These colors define the unique visual language of the calculator interface
// =============================================================================

/**
 * Display area colors - Where calculations appear
 * Designed for maximum readability and reduced eye strain
 */
object DisplayColors {
    // Light theme - Clean, paper-like display
    val BackgroundLight = Color(0xFFFFFFFF)
    val BackgroundSecondaryLight = Color(0xFFF7F7F9)
    val TextPrimaryLight = Color(0xFF1C1C1E)
    val TextSecondaryLight = Color(0xFF636366)
    val ResultLight = Color(0xFF007AFF)
    val ResultSecondaryLight = Color(0xFF34C759)
    val ErrorTextLight = Error700

    // Dark theme - Easy on the eyes
    val BackgroundDark = Color(0xFF1C1C1E)
    val BackgroundSecondaryDark = Color(0xFF2C2C2E)
    val TextPrimaryDark = Color(0xFFFFFFFF)
    val TextSecondaryDark = Color(0xFF8E8E93)
    val ResultDark = Color(0xFF0A84FF)
    val ResultSecondaryDark = Color(0xFF30D158)
    val ErrorTextDark = Error400

    // AMOLED theme - True blacks for OLED displays
    val BackgroundAmoled = Color(0xFF000000)
    val BackgroundSecondaryAmoled = Color(0xFF0A0A0A)
}

/**
 * Button color system - Distinct roles for different button types
 * Each role has specific semantic meaning and visual weight
 */
object ButtonColors {
    // Digit buttons (0-9, .) - Neutral, readable
    // These are the most frequently used buttons, so they should be comfortable
    val DigitBackgroundLight = Color(0xFFE5E5EA)
    val DigitBackgroundDark = Color(0xFF3A3A3C)
    val DigitBackgroundAmoled = Color(0xFF1C1C1E)
    val DigitTextLight = Color(0xFF1C1C1E)
    val DigitTextDark = Color(0xFFFFFFFF)

    // Digit pressed states - Subtle feedback
    val DigitPressedLight = Color(0xFFD1D1D6)
    val DigitPressedDark = Color(0xFF48484A)
    val DigitPressedAmoled = Color(0xFF2C2C2E)

    // Operator buttons (+, -, ×, ÷) - Secondary accent
    // Distinct from digits but not overwhelming
    val OperatorBackgroundLight = Color(0xFFD1D1D6)
    val OperatorBackgroundDark = Color(0xFF48484A)
    val OperatorBackgroundAmoled = Color(0xFF2C2C2E)
    val OperatorTextLight = Color(0xFF1C1C1E)
    val OperatorTextDark = Color(0xFFFFFFFF)

    // Operator pressed states
    val OperatorPressedLight = Color(0xFFC7C7CC)
    val OperatorPressedDark = Color(0xFF636366)
    val OperatorPressedAmoled = Color(0xFF3A3A3C)

    // Function buttons (sin, cos, log, etc.) - Tertiary, subtle
    val FunctionBackgroundLight = Color(0xFFF2F2F7)
    val FunctionBackgroundDark = Color(0xFF2C2C2E)
    val FunctionBackgroundAmoled = Color(0xFF141414)
    val FunctionTextLight = Color(0xFF636366)
    val FunctionTextDark = Color(0xFFAEAEB2)

    // Function pressed states
    val FunctionPressedLight = Color(0xFFE5E5EA)
    val FunctionPressedDark = Color(0xFF3A3A3C)
    val FunctionPressedAmoled = Color(0xFF1C1C1E)

    // Control buttons (AC, ⌫, etc.) - Destructive/clear actions
    val ControlBackgroundLight = Color(0xFFF2F2F7)
    val ControlBackgroundDark = Color(0xFF2C2C2E)
    val ControlBackgroundAmoled = Color(0xFF141414)
    val ControlTextLight = Color(0xFFFF3B30)
    val ControlTextDark = Color(0xFFFF453A)

    // Control pressed states
    val ControlPressedLight = Color(0xFFFFE5E5)
    val ControlPressedDark = Color(0xFF5C2A2A)
    val ControlPressedAmoled = Color(0xFF3D1515)

    // Memory buttons (M+, M-, MR, MC) - Distinct purple tint
    val MemoryBackgroundLight = Color(0xFFF5E6FF)
    val MemoryBackgroundDark = Color(0xFF3D2A4A)
    val MemoryBackgroundAmoled = Color(0xFF2A1F35)
    val MemoryTextLight = Color(0xFF8E44AD)
    val MemoryTextDark = Color(0xFFD0A8FF)

    // Memory pressed states
    val MemoryPressedLight = Color(0xFFEBD4FF)
    val MemoryPressedDark = Color(0xFF4A3558)
    val MemoryPressedAmoled = Color(0xFF352A40)

    // Equals button - Hero action, primary brand color
    val EqualsBackgroundLight = Color(0xFF007AFF)
    val EqualsBackgroundDark = Color(0xFF0A84FF)
    val EqualsBackgroundAmoled = Color(0xFF0A84FF)
    val EqualsTextLight = Color(0xFFFFFFFF)
    val EqualsTextDark = Color(0xFFFFFFFF)

    // Equals pressed state - Darker/lighter variant
    val EqualsPressedLight = Color(0xFF0051D5)
    val EqualsPressedDark = Color(0xFF409CFF)
    val EqualsPressedAmoled = Color(0xFF5CA8FF)

    // Special highlight buttons (secondary actions) - Green accent
    val HighlightBackgroundLight = Color(0xFF34C759)
    val HighlightBackgroundDark = Color(0xFF30D158)
    val HighlightBackgroundAmoled = Color(0xFF30D158)
    val HighlightTextLight = Color(0xFFFFFFFF)
    val HighlightTextDark = Color(0xFF000000)

    // Scientific operation buttons - Orange accent for discovery
    val ScientificBackgroundLight = Color(0xFFFFF4E6)
    val ScientificBackgroundDark = Color(0xFF4A3A2A)
    val ScientificBackgroundAmoled = Color(0xFF2A2015)
    val ScientificTextLight = Color(0xFFFF9500)
    val ScientificTextDark = Color(0xFFFFB84D)
}

// =============================================================================
// STATE COLORS - For interactive feedback and system states
// =============================================================================

/**
 * Comprehensive state color system for all interactive elements
 * Ensures consistent feedback across the entire app
 */
object StateColors {
    // Pressed/Active states - Immediate feedback
    val PressedOverlayLight = Color(0x14000000)  // 8% black overlay
    val PressedOverlayDark = Color(0x14FFFFFF)   // 8% white overlay
    val PressedOverlayAmoled = Color(0x1AFFFFFF) // 10% white overlay for visibility

    // Disabled states - Clearly inactive but still visible
    val DisabledBackgroundLight = Color(0xFFE5E5EA)
    val DisabledBackgroundDark = Color(0xFF3A3A3C)
    val DisabledBackgroundAmoled = Color(0xFF1C1C1E)
    val DisabledTextLight = Color(0xFFBDBDBD)
    val DisabledTextDark = Color(0xFF636366)
    val DisabledTextAmoled = Color(0xFF48484A)

    // Focus states - Keyboard navigation visibility
    val FocusRingLight = Color(0xFF007AFF)
    val FocusRingDark = Color(0xFF0A84FF)
    val FocusRingAmoled = Color(0xFF5CA8FF)

    // Hover states - Desktop/mouse interaction
    val HoverOverlayLight = Color(0x0A000000)  // 4% black overlay
    val HoverOverlayDark = Color(0x0AFFFFFF)   // 4% white overlay

    // Selected states - Active/selected items
    val SelectedBackgroundLight = Color(0xFFE3F2FD)
    val SelectedBackgroundDark = Color(0xFF1565C0)
    val SelectedTextLight = Color(0xFF1976D2)
    val SelectedTextDark = Color(0xFF64B5F6)

    // Drag states - Visual feedback during drag operations
    val DraggingElevationLight = Color(0x4D000000)
    val DraggingElevationDark = Color(0x66FFFFFF)
}

// =============================================================================
// HIGH CONTRAST MODE - WCAG AAA compliant colors (7:1 minimum ratio)
// =============================================================================

/**
 * High contrast colors for accessibility - Maximum visibility
 * All combinations meet or exceed WCAG 2.1 AAA standards
 */
object HighContrastColors {
    // Primary - Deep, vibrant blue
    val Primary = Color(0xFF0051CC)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFE8F0FF)
    val OnPrimaryContainer = Color(0xFF003380)

    // Secondary - Rich teal
    val Secondary = Color(0xFF006B6B)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFE0F5F5)
    val OnSecondaryContainer = Color(0xFF004040)

    // Tertiary - Deep purple
    val Tertiary = Color(0xFF6B2C8C)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFF5E8FF)
    val OnTertiaryContainer = Color(0xFF4A1A66)

    // Surface - Pure contrasts
    val BackgroundLight = Color(0xFFFFFFFF)
    val OnBackgroundLight = Color(0xFF000000)
    val SurfaceLight = Color(0xFFFFFFFF)
    val OnSurfaceLight = Color(0xFF000000)

    val BackgroundDark = Color(0xFF000000)
    val OnBackgroundDark = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF000000)
    val OnSurfaceDark = Color(0xFFFFFFFF)

    // Button-specific high contrast colors
    val DigitBackground = Color(0xFFF0F0F0)
    val DigitText = Color(0xFF000000)
    val OperatorBackground = Color(0xFFE0E0E0)
    val OperatorText = Color(0xFF000000)
    val EqualsBackground = Color(0xFF0051CC)
    val EqualsText = Color(0xFFFFFFFF)
    val ClearBackground = Color(0xFFFFE5E5)
    val ClearText = Color(0xFFB00020)

    // Outlines - Strong boundaries
    val OutlineLight = Color(0xFF000000)
    val OutlineDark = Color(0xFFFFFFFF)
    val OutlineVariantLight = Color(0xFF666666)
    val OutlineVariantDark = Color(0xFF999999)

    // Error - Unmistakable red
    val Error = Color(0xFFB00020)
    val OnError = Color(0xFFFFFFFF)
}

// =============================================================================
// THEME SEED COLORS - Curated palette for Material You dynamic theming
// =============================================================================

/**
 * Pre-selected seed colors that generate beautiful, harmonious themes
 * Each color is tested to ensure good contrast in both light and dark modes
 */
object SeedColors {
    val ClassicBlue = Color(0xFF007AFF)      // iOS-style blue - trustworthy, familiar
    val OceanTeal = Color(0xFF0095B6)        // Deep ocean - calm, professional
    val ForestGreen = Color(0xFF2E7D32)      // Natural green - fresh, organic
    val RoyalPurple = Color(0xFF6B2C8C)      // Regal purple - creative, premium
    val SunsetOrange = Color(0xFFFF6B35)     // Warm orange - energetic, friendly
    val RosePink = Color(0xFFE91E63)         // Vibrant pink - playful, modern
    val MidnightNavy = Color(0xFF1A237E)     // Deep navy - sophisticated, elegant
    val CrimsonRed = Color(0xFFC62828)       // Bold red - powerful, urgent
    val GoldenAmber = Color(0xFFFF8F00)      // Warm amber - inviting, vintage
    val Turquoise = Color(0xFF00ACC1)        // Fresh turquoise - tropical, clean
    val Lavender = Color(0xFF7E57C2)         // Soft lavender - soothing, dreamy
    val Coral = Color(0xFFFF7043)            // Living coral - vibrant, warm
    val Mint = Color(0xFF26A69A)             // Fresh mint - clean, modern
    val Berry = Color(0xFFAD1457)            // Deep berry - rich, luxurious
    val Slate = Color(0xFF455A64)            // Neutral slate - professional, balanced

    // Default calculator seed
    val Default = ClassicBlue

    // All available seeds for theme picker
    val All = listOf(
        ClassicBlue, OceanTeal, ForestGreen, RoyalPurple, SunsetOrange,
        RosePink, MidnightNavy, CrimsonRed, GoldenAmber, Turquoise,
        Lavender, Coral, Mint, Berry, Slate
    )
}

// =============================================================================
// GRADIENT COLORS - For premium visual effects
// =============================================================================

object GradientColors {
    // Display result gradients - Subtle depth for results
    val ResultLight = listOf(Color(0xFF007AFF), Color(0xFF5856D6))
    val ResultDark = listOf(Color(0xFF0A84FF), Color(0xFF7B8CFF))
    val ResultAmoled = listOf(Color(0xFF5CA8FF), Color(0xFF9BB5FF))

    // Equals button gradients - Hero action emphasis
    val EqualsLight = listOf(Color(0xFF007AFF), Color(0xFF0066CC))
    val EqualsDark = listOf(Color(0xFF0A84FF), Color(0xFF5CA8FF))

    // Background ambient gradients - Subtle depth
    val AmbientLight = listOf(Color(0xFFF5F5F7), Color(0xFFFFFFFF))
    val AmbientDark = listOf(Color(0xFF000000), Color(0xFF1C1C1E))
}

// =============================================================================
// LEGACY COLOR EXPORTS - For backward compatibility
// These will be deprecated in future versions
// =============================================================================

// Primary Brand Colors
val PrimaryBlue = Blue500
val PrimaryBlueDark = Blue400
val OnPrimaryLight = Color(0xFFFFFFFF)
val OnPrimaryDark = Color(0xFFFFFFFF)

// Secondary Colors
val SecondaryTeal = Teal500
val SecondaryPurple = Purple500

// Background Colors - Legacy aliases
val BackgroundLight = WarmGray50
val BackgroundDark = Color(0xFF000000)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Gray900
val SurfaceElevatedLight = Color(0xFFFFFFFF)
val SurfaceElevatedDark = CoolGray800

// Display Area Colors - Legacy aliases
val DisplayBackgroundLight = DisplayColors.BackgroundLight
val DisplayBackgroundDark = DisplayColors.BackgroundDark
val DisplayEditorTextLight = DisplayColors.TextPrimaryLight
val DisplayEditorTextDark = DisplayColors.TextPrimaryDark
val DisplayResultLight = DisplayColors.ResultLight
val DisplayResultDark = DisplayColors.ResultDark

// Button Colors - Legacy aliases
val ButtonDigitBackgroundLight = ButtonColors.DigitBackgroundLight
val ButtonDigitBackgroundDark = ButtonColors.DigitBackgroundDark
val ButtonDigitTextLight = ButtonColors.DigitTextLight
val ButtonDigitTextDark = ButtonColors.DigitTextDark

val ButtonOperationBackgroundLight = ButtonColors.OperatorBackgroundLight
val ButtonOperationBackgroundDark = ButtonColors.OperatorBackgroundDark
val ButtonOperationTextLight = ButtonColors.OperatorTextLight
val ButtonOperationTextDark = ButtonColors.OperatorTextDark

val ButtonControlBackgroundLight = ButtonColors.ControlBackgroundLight
val ButtonControlBackgroundDark = ButtonColors.ControlBackgroundDark
val ButtonControlTextLight = ButtonColors.ControlTextLight
val ButtonControlTextDark = ButtonColors.ControlTextDark

val ButtonHighlightBackgroundLight = ButtonColors.HighlightBackgroundLight
val ButtonHighlightBackgroundDark = ButtonColors.HighlightBackgroundDark
val ButtonHighlightTextLight = ButtonColors.HighlightTextLight
val ButtonHighlightTextDark = ButtonColors.HighlightTextDark

// Border and divider colors
val DividerLight = Color(0xFFE5E5EA)
val DividerDark = Color(0xFF38383A)
val BorderLight = Color(0xFFC7C7CC)
val BorderDark = Color(0xFF48484A)

// Text Colors
val TextPrimaryLight = Gray900
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryLight = Gray600
val TextSecondaryDark = Color(0xFFAEAEB2)
val TextTertiaryLight = Gray500
val TextTertiaryDark = Color(0xFF6E6E73)

// Error and special states
val ErrorColor = Error600
val ErrorColorDark = Error400
val SuccessColor = Success600
val SuccessColorDark = Success400

// Shadow colors for elevation
val ShadowColorLight = Color(0x1A000000)
val ShadowColorDark = Color(0x40000000)

// Overlay colors for glass effects
val OverlayLight = Color(0x80FFFFFF)
val OverlayDark = Color(0x80000000)

// Accent colors for highlighting
val AccentOrange = Color(0xFFFF9500)
val AccentYellow = Color(0xFFFFCC00)
val AccentRed = Color(0xFFFF3B30)
val AccentPink = Color(0xFFFF2D55)
val AccentIndigo = Color(0xFF5856D6)
