package org.solovyev.android.calculator.ui

import jscl.NumeralBase
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgrammerUiModelTest {

    @Test
    fun hintModelMatchesExpectedSnapshotStyleText() {
        assertEquals(
            ProgrammerHintUiModel(
                primaryHint = "Only 0-1 digits",
                secondaryHint = "Bitwise order: ~, << >>, &, ^, |. Plain ^ stays power."
            ),
            programmerHintUiModel(NumeralBase.bin)
        )

        assertEquals(
            ProgrammerHintUiModel(
                primaryHint = "0-9 and A-F digits",
                secondaryHint = "Bitwise order: ~, << >>, &, ^, |. Plain ^ stays power."
            ),
            programmerHintUiModel(NumeralBase.hex)
        )
    }

    @Test
    fun statusModelMatchesExpectedSnapshotStyleText() {
        assertEquals(
            ProgrammerStatusUiModel(
                modeLabel = "32b U HEX",
                overflowLabel = "Overflow"
            ),
            programmerStatusUiModel(wordSize = 32, signed = false, base = NumeralBase.hex, overflow = true)
        )

        assertEquals(
            ProgrammerStatusUiModel(
                modeLabel = "8b S BIN",
                overflowLabel = null
            ),
            programmerStatusUiModel(wordSize = 8, signed = true, base = NumeralBase.bin, overflow = false)
        )
    }
}
