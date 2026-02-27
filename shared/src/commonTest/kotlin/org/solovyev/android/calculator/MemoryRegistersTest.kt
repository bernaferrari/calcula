package org.solovyev.android.calculator

import kotlin.test.Test
import kotlin.test.assertEquals
import org.solovyev.android.calculator.memory.MemoryRegisters

class MemoryRegistersTest {

    @Test
    fun normalizeNameCleansAndUppercases() {
        assertEquals("ALPHA_01", MemoryRegisters.normalizeName(" alpha 01 "))
        assertEquals("A-B_C", MemoryRegisters.normalizeName("a-b c"))
    }

    @Test
    fun normalizeNameFallsBackToDefaultWhenEmpty() {
        assertEquals("A", MemoryRegisters.normalizeName("   "))
        assertEquals("A", MemoryRegisters.normalizeName("!@#$"))
    }

    @Test
    fun addUsesNumericSumWhenPossible() {
        assertEquals("8.0", MemoryRegisters.add("5", "3"))
    }

    @Test
    fun addFallsBackToIncomingValueWhenNotNumeric() {
        assertEquals("x", MemoryRegisters.add("not_number", "x"))
    }

    @Test
    fun subtractUsesNumericDifferenceWhenPossible() {
        assertEquals("2.0", MemoryRegisters.subtract("5", "3"))
    }

    @Test
    fun subtractFallsBackToIncomingValueWhenNotNumeric() {
        assertEquals("k", MemoryRegisters.subtract("?", "k"))
    }
}
