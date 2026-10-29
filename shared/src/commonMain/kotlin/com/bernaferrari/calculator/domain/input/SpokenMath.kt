package com.bernaferrari.calculator.domain.input

/**
 * Turns what Siri hears, or what someone types into Shortcuts, into the calculator's language:
 * "12 times 3 plus 4", "15% of 80", "square root of 2", "2 to the power of 10", `3*4/2`.
 */
object SpokenMath {
    private val phrases = listOf(
        "to the power of" to "^", "raised to" to "^", "divided by" to "÷", "multiplied by" to "×",
        "square root of" to "√", "cube root of" to "∛", "percent of" to "%×", "% of" to "%×",
        "squared" to "²", "cubed" to "³", "factorial" to "!",
        "times" to "×", "plus" to "+", "minus" to "−", "over" to "÷", "percent" to "%",
        "point" to ".", "pi" to "π", "open parenthesis" to "(", "close parenthesis" to ")",
    )

    fun normalize(input: String): String {
        var text = " " + input.lowercase().trim().removeSuffix("?").removeSuffix("=") + " "
        text = text.replace(Regex("^ *(what is|what's|calculate|compute) "), " ")
        for ((words, symbol) in phrases) text = text.replace(Regex("(?<![a-z])${Regex.escape(words)}(?![a-z])"), symbol)
        return text
            // 3 x 4 means times; the x of a graph never comes this way.
            .replace(Regex("(?<=[0-9)])\\s*x\\s*(?=[0-9(])"), "×")
            .replace('*', '×').replace('/', '÷').replace('-', '−')
            // A spoken thousands separator ("1,200") is not a decimal point.
            .replace(Regex("(?<=\\d),(?=\\d{3}(\\D|$))"), "")
            .replace(" ", "")
            .replace(Regex("(?<=\\d)e(?=[+−]?\\d)"), "E")
    }
}
