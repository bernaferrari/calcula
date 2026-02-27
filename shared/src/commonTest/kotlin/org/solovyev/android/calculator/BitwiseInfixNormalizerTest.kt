package org.solovyev.android.calculator

import kotlin.test.Test
import kotlin.test.assertEquals

class BitwiseInfixNormalizerTest {

    @Test
    fun normalizesBitwiseSymbolsToFunctions() {
        assertEquals("and(5,3)", BitwiseInfixNormalizer.normalize("5 & 3"))
        assertEquals("or(and(5,3),1)", BitwiseInfixNormalizer.normalize("5 & 3 | 1"))
        assertEquals("shl(2,3)", BitwiseInfixNormalizer.normalize("2 << 3"))
        assertEquals("shr(16,2)", BitwiseInfixNormalizer.normalize("16 >> 2"))
        assertEquals("not(8)", BitwiseInfixNormalizer.normalize("~8"))
    }

    @Test
    fun supportsXorWhenCaretIsSpaced() {
        assertEquals("xor(6,3)", BitwiseInfixNormalizer.normalize("6 ^ 3"))
    }

    @Test
    fun keepsPowerCaretWhenNotSpaced() {
        assertEquals("2^10", BitwiseInfixNormalizer.normalize("2^10"))
    }

    @Test
    fun preservesFunctionArgumentsAndNestedGroups() {
        assertEquals(
            "and(max(1,2),or(3,1))",
            BitwiseInfixNormalizer.normalize("max(1,2) & (3 | 1)")
        )
    }

    @Test
    fun leavesUnsupportedOperatorSequencesUntouched() {
        assertEquals("a&&b", BitwiseInfixNormalizer.normalize("a&&b"))
        assertEquals("a||b", BitwiseInfixNormalizer.normalize("a||b"))
    }
}
