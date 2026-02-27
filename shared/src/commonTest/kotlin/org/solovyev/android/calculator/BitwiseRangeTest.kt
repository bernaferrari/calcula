package org.solovyev.android.calculator

import jscl.NumeralBase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BitwiseRangeTest {

    @Test
    fun signedOverflowUsesTwosComplementBounds() {
        assertFalse(BitwiseRange.isOverflow("127", 8, signed = true, base = NumeralBase.dec))
        assertFalse(BitwiseRange.isOverflow("-128", 8, signed = true, base = NumeralBase.dec))
        assertTrue(BitwiseRange.isOverflow("128", 8, signed = true, base = NumeralBase.dec))
        assertTrue(BitwiseRange.isOverflow("-129", 8, signed = true, base = NumeralBase.dec))
    }

    @Test
    fun unsignedOverflowRejectsNegativeAndOutOfRange() {
        assertFalse(BitwiseRange.isOverflow("255", 8, signed = false, base = NumeralBase.dec))
        assertTrue(BitwiseRange.isOverflow("256", 8, signed = false, base = NumeralBase.dec))
        assertTrue(BitwiseRange.isOverflow("-1", 8, signed = false, base = NumeralBase.dec))
    }

    @Test
    fun parsesNonDecimalOutputs() {
        assertFalse(BitwiseRange.isOverflow("FF", 8, signed = false, base = NumeralBase.hex))
        assertTrue(BitwiseRange.isOverflow("100", 8, signed = false, base = NumeralBase.hex))
        assertFalse(BitwiseRange.isOverflow("1111 1111", 8, signed = false, base = NumeralBase.bin))
        assertTrue(BitwiseRange.isOverflow("1 0000 0000", 8, signed = false, base = NumeralBase.bin))
    }

    @Test
    fun ignoresNonIntegerDisplayValues() {
        assertFalse(BitwiseRange.isOverflow("", 8, signed = false, base = NumeralBase.dec))
        assertFalse(BitwiseRange.isOverflow("3.14", 8, signed = false, base = NumeralBase.dec))
        assertFalse(BitwiseRange.isOverflow("NaN", 8, signed = false, base = NumeralBase.dec))
        assertFalse(BitwiseRange.isOverflow("∞", 8, signed = false, base = NumeralBase.dec))
    }
}
