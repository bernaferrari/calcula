package org.solovyev.android.calculator.converter

import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterTest {

    @Test(expected = NumberFormatException::class)
    fun testShouldNotParseInvalidDecNumber() {
        Converter.parse("1A", 10)
    }

    @Test
    fun testShouldParseValidHexNumber() {
        assertEquals(26, Converter.parse("1A", 16).toLong())
    }
}
