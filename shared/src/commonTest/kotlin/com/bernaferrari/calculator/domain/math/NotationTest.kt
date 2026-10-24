package com.bernaferrari.calculator.domain.math

import kotlin.test.Test
import kotlin.test.assertEquals

class NotationTest {
    private fun sci(text: String, decimals: Int = -1) = NumberFormatter.scientific(Decimal(text), engineering = false, decimals)
    private fun eng(text: String, decimals: Int = -1) = NumberFormatter.scientific(Decimal(text), engineering = true, decimals)

    @Test fun scientific() {
        assertEquals("1.2345678E6", sci("1234567.8"))
        assertEquals("4.7E−4", sci("0.00047"))
        assertEquals("−2.5E0", sci("-2.5"))
        assertEquals("1.23E6", sci("1234567.8", 2))
        // Rounding carries into the next power.
        assertEquals("1.00E1", sci("9.999", 2))
    }

    @Test fun engineeringKeepsExponentsInThrees() {
        assertEquals("12.345E3", eng("12345"))
        assertEquals("470E−6", eng("0.00047"))
        assertEquals("1.2345678E6", eng("1234567.8"))
        assertEquals("999E0", eng("999"))
        assertEquals("1.0E3", eng("999.96", 1))
    }
}
