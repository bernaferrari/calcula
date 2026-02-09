package org.solovyev.android.calculator.latex

/**
 * Maps calculator input actions to LaTeX syntax.
 * Used when LaTeX output mode is enabled to generate copyable LaTeX from keyboard presses.
 */
object LatexMapper {

    /**
     * Converts a calculator action to its LaTeX equivalent.
     * @param action The calculator action (e.g., "×", "sin", "π")
     * @return The LaTeX representation
     */
    fun toLatex(action: String): String = when (action) {
        // Operators
        "×" -> " \\times "
        "÷", "/" -> " \\div "
        "−" -> " - "
        "+" -> " + "
        "^" -> "^{"
        "^2" -> "^{2}"
        "%" -> " \\% "

        // Square root
        "√" -> "\\sqrt{"

        // Trigonometric functions
        "sin" -> "\\sin("
        "cos" -> "\\cos("
        "tan" -> "\\tan("
        "asin" -> "\\arcsin("
        "acos" -> "\\arccos("
        "atan" -> "\\arctan("

        // Logarithmic functions
        "ln" -> "\\ln("
        "lg" -> "\\log_{10}("
        "log" -> "\\log("

        // Greek letters and constants
        "π" -> "\\pi"
        "e" -> "e"
        "i" -> "i"
        "∞" -> "\\infty"

        // Comparison operators
        "≤" -> " \\leq "
        "≥" -> " \\geq "
        "≠" -> " \\neq "

        // Parentheses - smart insertion
        "()" -> "()"
        "(" -> "("
        ")" -> ")"
        "(…)" -> "\\left( \\right)"

        // Factorial
        "!" -> "!"

        // Degree symbol
        "°" -> "^{\\circ}"

        // Period stays as-is for decimals
        "." -> "."
        "," -> ","

        // Numbers and hex digits stay as-is
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> action
        "A", "B", "C", "D", "E", "F" -> action
        "00", "000" -> action

        // Variables
        "x" -> "x"
        "y" -> "y"
        "t" -> "t"
        "j" -> "j"

        // Base prefixes - convert to subscript notation
        "0b:" -> "_{2}"
        "0d:" -> "_{10}"
        "0x:" -> "_{16}"

        // Default: return as-is
        else -> action
    }

    /**
     * Wraps numerator and denominator in a LaTeX fraction.
     */
    fun wrapFraction(numerator: String, denominator: String): String =
        "\\frac{$numerator}{$denominator}"

    /**
     * Checks if an action should trigger cursor positioning inside braces.
     * @return true if cursor should be positioned between braces (e.g., for ^{})
     */
    fun needsCursorInside(action: String): Boolean = when (action) {
        "^", "√" -> true
        else -> false
    }
}
