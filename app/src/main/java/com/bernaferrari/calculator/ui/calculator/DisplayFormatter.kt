package com.bernaferrari.calculator.ui.calculator

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.bernaferrari.calculator.domain.model.DecimalSeparator
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Turns the internal expression (`1234.5×1E20`) into what people read (`1,234.5 × 10²⁰` with
 * their locale's separators), keeping a map between the two so taps can place the cursor.
 */
class DisplayFormatter(locale: Locale = Locale.getDefault()) {
    private val symbols = DecimalFormatSymbols.getInstance(locale)
    val decimal: Char = symbols.decimalSeparator
    private val grouping: Char = symbols.groupingSeparator.let { if (it == ' ' || it == ' ') ' ' else it }

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
                    c in "()^" -> emit(i, c.toString(), colors?.let { SpanStyle(color = it.operator) })
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

    /** Function names (`sin`, `log`…) never contain `e`, so a lone `e` is always Euler's number. */
    private fun isNameChar(c: Char) = c in 'a'..'z' && c != 'e'

    private companion object {
        val SUPERSCRIPT = mapOf(
            '0' to "⁰", '1' to "¹", '2' to "²", '3' to "³", '4' to "⁴", '5' to "⁵", '6' to "⁶", '7' to "⁷",
            '8' to "⁸", '9' to "⁹", '−' to "⁻", '-' to "⁻", '+' to "",
        )
    }
}

/** The locale whose separators to use: the system's, or one forcing `1,234.5` or `1.234,5`. */
fun DecimalSeparator.locale(system: Locale): Locale = when (this) {
    DecimalSeparator.AUTO -> system
    DecimalSeparator.DOT -> Locale.US
    DecimalSeparator.COMMA -> Locale.GERMANY
}
