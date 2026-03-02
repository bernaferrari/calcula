package org.solovyev.android.calculator.ui.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Consistent corner radius scale for the calculator UI.
 * Following Material Design 3 guidelines with custom calculator-specific values.
 */
object CalculatorCornerRadius {
    /** 4dp: Small elements (chips, badges, small indicators) */
    val Small = 4.dp

    /** 8dp: Medium elements (small buttons, input fields) */
    val Medium = 8.dp

    /** 12dp: Standard cards, dialogs, panels */
    val Standard = 12.dp

    /** 16dp: Large buttons, main surfaces */
    val Large = 16.dp

    /** 20dp: Extra large cards, bottom sheets */
    val ExtraLarge = 20.dp

    /** 28dp: Display area, hero elements */
    val Display = 28.dp
}

/**
 * Consistent elevation scale for shadows and depth.
 * Creates visual hierarchy through elevation.
 */
object CalculatorElevation {
    /** 0dp: Pressed buttons, flat elements */
    val Pressed = 0.dp

    /** 1dp: Subtle resting state */
    val Subtle = 1.dp

    /** 2dp: Standard buttons, resting state */
    val Standard = 2.dp

    /** 4dp: Elevated cards, selected items */
    val Elevated = 4.dp

    /** 6dp: Display card, important surfaces */
    val Display = 6.dp

    /** 8dp: Hero elements (equals button), floating actions */
    val Hero = 8.dp
}

/**
 * Standard spacing values for consistent layouts.
 */
object CalculatorSpacing {
    /** 4dp: Tight spacing */
    val XSmall = 4.dp

    /** 8dp: Compact spacing */
    val Small = 8.dp

    /** 12dp: Standard spacing */
    val Medium = 12.dp

    /** 16dp: Comfortable spacing */
    val Large = 16.dp

    /** 24dp: Generous spacing */
    val XLarge = 24.dp

    /** 32dp: Extra generous spacing */
    val XXLarge = 32.dp
}

/**
 * Standard padding values for components.
 */
object CalculatorPadding {
    /** 4dp */
    val XSmall = 4.dp

    /** 8dp */
    val Small = 8.dp

    /** 12dp */
    val Medium = 12.dp

    /** 16dp */
    val Standard = 16.dp

    /** 20dp */
    val Large = 20.dp

    /** 24dp */
    val XLarge = 24.dp
}

/**
 * Shared gesture constants so keyboard stacks behave consistently.
 */
object CalculatorGestureTokens {
    /** Auto-activation threshold ratio (80%). */
    const val AutoActivationThresholdRatio: Float = 0.8f

    /** Lower clamp for auto-activation trigger distance. */
    val AutoActivationMinDistance: Dp = 16.dp

    /** Swipe reveal distance used by Not Boring keyboard. */
    val VerticalRevealDistance: Dp = 40.dp
}

/**
 * Shared button border tokens for subtle edge definition.
 */
object CalculatorButtonBorderTokens {
    /** Border stroke width for calculator keys. */
    val Width: Dp = 1.dp

    /** Default alpha for number/digit keys. */
    const val DigitAlpha: Float = 0.24f

    /** Default alpha for operation/control/special keys. */
    const val AccentAlpha: Float = 0.18f

    /** Pressed-state alpha to improve tactile feedback. */
    const val PressedAlpha: Float = 0.36f
}

/**
 * Shared overflow-menu geometry and sizing.
 */
object CalculatorMenuTokens {
    /** Menu corner radius. */
    val ContainerCorner: Dp = 18.dp

    /** Icon container corner radius. */
    val LeadingIconCorner: Dp = 10.dp

    /** Minimum menu width. */
    val MinWidth: Dp = 236.dp

    /** Maximum menu width to avoid oversized popovers on tablets. */
    val MaxWidth: Dp = 320.dp

    /** Minimum touch target for each menu row. */
    val ItemMinHeight: Dp = 50.dp

    /** Leading icon container size. */
    val LeadingIconSize: Dp = 30.dp
}
