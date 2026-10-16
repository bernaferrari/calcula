package com.bernaferrari.calculator.ui.calculator

import kotlin.test.assertEquals
import kotlin.test.Test

class DisplayFormatterTest {
    private val us = DisplayFormatter(Separators.Dot)
    private val br = DisplayFormatter(Separators.Comma)

    @Test fun groupsIntegerPartOnly() {
        assertEquals("1,234,567.891", us.number("1234567.891"))
        assertEquals("1.234.567,891", br.number("1234567.891"))
        assertEquals("−1,000", us.number("−1000"))
        assertEquals("999", us.number("999"))
    }

    @Test fun scientificNotationReadsAsPowerOfTen() {
        assertEquals("1.5×10²⁰", us.number("1.5E20"))
        assertEquals("2×10⁻¹²", us.number("2E−12"))
    }

    @Test fun expressionsKeepOperatorsAndMapCursor() {
        val f = us.format("1234+5", ghostParens = 1, colors = null)
        assertEquals("1,234+5)", f.text.text)
        // Cursor after "1234" in raw text sits after "1,234" on screen.
        assertEquals(5, f.rawToDisplay[4])
        assertEquals(4, f.displayToRaw[5])
        assertEquals(6, f.displayToRaw[f.displayToRaw.lastIndex])
    }

    @Test fun functionsAndConstants() {
        assertEquals("sin(30)×π", us.format("sin(30)×π", 0, null).text.text)
        assertEquals("2e", us.format("2e", 0, null).text.text)
    }
}
