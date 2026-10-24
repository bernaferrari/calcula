package com.bernaferrari.calculator.domain.programmer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProgrammerTest {
    private fun type(vararg keys: ProgKey, from: ProgState = ProgState()) = keys.fold(from) { s, k -> Programmer.press(s, k) }

    @Test fun hexArithmeticAndBitwise() {
        val hex = ProgState(base = Base.HEX)
        assertEquals("100", Programmer.display(type(ProgKey.F, ProgKey.F, ProgKey.PLUS, ProgKey.D1, ProgKey.EQUALS, from = hex)))
        assertEquals("F", Programmer.display(type(ProgKey.F, ProgKey.F, ProgKey.AND, ProgKey.D0, ProgKey.F, ProgKey.EQUALS, from = hex)))
        assertEquals(256L, Programmer.value(type(ProgKey.D1, ProgKey.SHL, ProgKey.D8, from = hex)))
    }

    @Test fun precedenceIsCs() {
        // 1 + 2 << 3 is (1 + 2) << 3 = 24; 6 OR 1 AND 3 is 6 OR 1 = 7.
        assertEquals(24L, Programmer.value(type(ProgKey.D1, ProgKey.PLUS, ProgKey.D2, ProgKey.SHL, ProgKey.D3)))
        assertEquals(7L, Programmer.value(type(ProgKey.D6, ProgKey.OR, ProgKey.D1, ProgKey.AND, ProgKey.D3)))
        assertEquals(1L, Programmer.value(type(ProgKey.D7, ProgKey.MOD, ProgKey.D3)))
    }

    @Test fun wordSizeWrapsLikeHardware() {
        val byte = ProgState(word = WordSize.BYTE)
        assertEquals(-128L, Programmer.value(type(ProgKey.D1, ProgKey.D2, ProgKey.D7, ProgKey.PLUS, ProgKey.D1, from = byte)))
        assertEquals("FF", Programmer.format(-1, Base.HEX, WordSize.BYTE))
        assertEquals(-1L, Programmer.value(type(ProgKey.NOT, ProgKey.D0, from = byte)))
        // Digits that don't fit the word aren't taken.
        assertEquals("255", Programmer.display(type(ProgKey.D2, ProgKey.D5, ProgKey.D5, ProgKey.D5, from = byte)))
    }

    @Test fun basesConvertAndLimitDigits() {
        val dec = type(ProgKey.D2, ProgKey.D5, ProgKey.D5, ProgKey.PLUS, ProgKey.D1)
        val hex = Programmer.withBase(dec, Base.HEX)
        assertEquals("FF + 1", Programmer.display(hex))
        assertEquals("1111 1111 + 1", Programmer.display(Programmer.withBase(hex, Base.BIN)))
        // Binary takes only 0 and 1.
        assertEquals("1", Programmer.display(type(ProgKey.D1, ProgKey.D2, from = ProgState(base = Base.BIN))))
        // A negative decimal becomes its two's complement in hex, and back.
        val neg = type(ProgKey.D5, ProgKey.NEGATE, from = ProgState(word = WordSize.BYTE))
        assertEquals("−5", Programmer.display(neg))
        assertEquals("FB", Programmer.display(Programmer.withBase(neg, Base.HEX)))
        assertEquals("−5", Programmer.display(Programmer.withBase(Programmer.withBase(neg, Base.HEX), Base.DEC)))
    }

    @Test fun divisionByZeroIsAnError() {
        val s = type(ProgKey.D1, ProgKey.DIVIDE, ProgKey.D0, ProgKey.EQUALS)
        assertEquals(ProgError.DIVIDE_BY_ZERO, s.error)
        assertNull(type(ProgKey.D1, from = s).error)
    }

    @Test fun bitsFlip() {
        val five = type(ProgKey.D5)
        assertEquals(7L, Programmer.value(Programmer.flipBit(five, 1)))
        assertEquals(4L, Programmer.value(Programmer.flipBit(five, 0)))
        // The sign bit of a byte makes it negative.
        assertEquals(-128L, Programmer.value(Programmer.flipBit(ProgState(word = WordSize.BYTE), 7)))
    }

    @Test fun keepsWhatWasEvaluatedAboveTheResult() {
        val done = type(ProgKey.F, ProgKey.F, ProgKey.PLUS, ProgKey.D1, ProgKey.EQUALS, from = ProgState(base = Base.HEX))
        assertEquals("FF + 1", Programmer.display(done.expression!!, done.base))
        // It follows the base, and a second = keeps it rather than dropping it.
        assertEquals("255 + 1", Programmer.withBase(done, Base.DEC).let { Programmer.display(it.expression!!, it.base) })
        assertEquals(done, Programmer.press(done, ProgKey.EQUALS))
        // Going on from the answer starts a new calculation.
        assertNull(Programmer.press(done, ProgKey.PLUS).expression)
    }
}
