package org.solovyev.android.calculator

import kotlin.test.Test
import kotlin.test.assertEquals

class ProgrammerLiteralNormalizerTest {

    @Test
    fun normalizesCommonProgrammerPrefixesWithoutColon() {
        assertEquals("0x:FF", ProgrammerLiteralNormalizer.normalize("0xFF"))
        assertEquals("0b:1010", ProgrammerLiteralNormalizer.normalize("0b1010"))
        assertEquals("0o:77", ProgrammerLiteralNormalizer.normalize("0o77"))
        assertEquals("0d:42", ProgrammerLiteralNormalizer.normalize("0d42"))
    }

    @Test
    fun preservesAlreadyNormalizedPrefixes() {
        assertEquals("0x:FF + 1", ProgrammerLiteralNormalizer.normalize("0x:FF + 1"))
        assertEquals("0b:11", ProgrammerLiteralNormalizer.normalize("0b:11"))
    }

    @Test
    fun only_rewrites_full_tokens() {
        assertEquals("foo0xFF", ProgrammerLiteralNormalizer.normalize("foo0xFF"))
        assertEquals("0xFFbar", ProgrammerLiteralNormalizer.normalize("0xFFbar"))
        assertEquals("(0x:AB)+1", ProgrammerLiteralNormalizer.normalize("(0xAB)+1"))
    }
}
