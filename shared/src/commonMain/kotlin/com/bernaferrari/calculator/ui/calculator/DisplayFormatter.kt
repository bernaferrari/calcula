package com.bernaferrari.calculator.ui.calculator

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.bernaferrari.calculator.domain.math.Tokenizer
import com.bernaferrari.calculator.domain.model.DecimalSeparator
import com.bernaferrari.calculator.platform.systemSeparators

/**
 * Turns the internal expression (`1234.5×1E20`) into what people read (`1,234.5 × 10²⁰` with
 * their locale's separators), keeping a map between the two so taps can place the cursor.
 */
class DisplayFormatter(separators: Separators = systemSeparators()) {
    val decimal: Char = separators.decimal
    // Spacey locales (fr, pt-PT…) group with a thin space, which reads better in the big display.
    private val grouping: Char = separators.grouping.let { if (it == '\u00A0' || it == '\u202F') '\u2009' else it }

    class Formatted(
        val text: AnnotatedString,
        /** rawToDisplay[i] is the display offset right before raw character i. */
        val rawToDisplay: IntArray,
        val displayToRaw: IntArray,
    )

    data class Colors(val operator: Color, val function: Color, val ghost: Color)

    /** Formats a plain number, e.g. a result on the tape. */
    fun number(raw: String): String = format(raw, ghostParens = 0, colors = null).text.text

    fun format(raw: String, ghostParens: Int, colors: Colors?): Formatted {
        val rawToDisplay = IntArray(raw.length + 1)
        val displayToRaw = ArrayList<Int>(raw.length * 2)
        val text = buildAnnotatedString {
            fun emit(rawIndex: Int, s: String, style: SpanStyle? = null) {
                if (style != null) withStyle(style) { append(s) } else append(s)
                repeat(s.length) { displayToRaw += rawIndex }
            }

            var i = 0
            while (i < raw.length) {
                val c = raw[i]
                rawToDisplay[i] = length
                when {
                    c.isDigit() || c == '.' -> {
                        // Group the integer part of this number run.
                        var end = i
                        while (end < raw.length && (raw[end].isDigit() || raw[end] == '.')) end++
                        val dot = raw.indexOf('.', i).takeIf { it in i until end } ?: end
                        val groupable = raw.getOrNull(i - 1) != 'E'
                        for (j in i until end) {
                            rawToDisplay[j] = length
                            val digitsLeft = dot - j
                            if (groupable && j in (i + 1) until dot && digitsLeft % 3 == 0) emit(j, grouping.toString())
                            emit(j, if (raw[j] == '.') decimal.toString() else raw[j].toString())
                        }
                        i = end
                        continue
                    }
                    c == 'E' -> {
                        emit(i, "×10", colors?.let { SpanStyle(color = it.operator) })
                        var j = i + 1
                        while (j < raw.length && (raw[j].isDigit() || (j == i + 1 && raw[j] in "−-+"))) {
                            rawToDisplay[j] = length
                            emit(j, SUPERSCRIPT[raw[j]] ?: raw[j].toString())
                            j++
                        }
                        i = j
                        continue
                    }
                    c in "+−×÷" -> emit(i, c.toString(), colors?.let { SpanStyle(color = it.operator) })
                    // A graph's equation: spaced so both sides read on their own.
                    c == '=' -> emit(i, " = ", colors?.let { SpanStyle(color = it.operator) })
                    c in "()^" -> emit(i, c.toString(), colors?.let { SpanStyle(color = it.operator) })
                    // A graph's slider parameter standing alone (`a`, not the `a` of `abs(`).
                    c in Tokenizer.PARAMETERS && Tokenizer.functionAt(raw, i) == null -> emit(i, c.toString())
                    isNameChar(c) -> {
                        var end = i
                        while (end < raw.length && (isNameChar(raw[end]) || raw[end] in "⁻¹")) end++
                        for (j in i until end) {
                            rawToDisplay[j] = length
                            emit(j, raw[j].toString(), colors?.let { SpanStyle(color = it.function) })
                        }
                        i = end
                        continue
                    }
                    else -> emit(i, c.toString())
                }
                i++
            }
            rawToDisplay[raw.length] = length
            displayToRaw += raw.length
            if (ghostParens > 0) {
                val ghost = ")".repeat(ghostParens)
                if (colors != null) withStyle(SpanStyle(color = colors.ghost)) { append(ghost) } else append(ghost)
                repeat(ghostParens) { displayToRaw += raw.length }
            }
        }
        return Formatted(text, rawToDisplay, displayToRaw.toIntArray())
    }

    /** Function names (`sin`, `log`…) never contain `e`, `x` or `y`, so those always stand alone. */
    private fun isNameChar(c: Char) = c in 'a'..'z' && c != 'e' && c != 'x' && c != 'y'

    private companion object {
        val SUPERSCRIPT = mapOf(
            '0' to "⁰", '1' to "¹", '2' to "²", '3' to "³", '4' to "⁴", '5' to "⁵", '6' to "⁶", '7' to "⁷",
            '8' to "⁸", '9' to "⁹", '−' to "⁻", '-' to "⁻", '+' to "",
        )
    }
}

/** A locale's decimal and grouping marks. */
data class Separators(val decimal: Char, val grouping: Char) {
    companion object {
        val Dot = Separators('.', ',')
        val Comma = Separators(',', '.')
    }
}

/** The separators to use: the system's, or forced `1,234.5` or `1.234,5`. */
fun DecimalSeparator.separators(): Separators = when (this) {
    DecimalSeparator.AUTO -> systemSeparators()
    DecimalSeparator.DOT -> Separators.Dot
    DecimalSeparator.COMMA -> Separators.Comma
}
