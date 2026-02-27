package org.solovyev.android.calculator.ui

import jscl.NumeralBase
import org.solovyev.android.calculator.BitwiseRange

internal data class ProgrammerHintUiModel(
    val primaryHint: String,
    val secondaryHint: String
)

internal data class ProgrammerStatusUiModel(
    val modeLabel: String,
    val overflowLabel: String?
)

internal fun programmerHintUiModel(base: NumeralBase): ProgrammerHintUiModel {
    return ProgrammerHintUiModel(
        primaryHint = baseInputHint(base),
        secondaryHint = "Bitwise order: ~, << >>, &, ^, |. Plain ^ stays power."
    )
}

internal fun programmerStatusUiModel(
    wordSize: Int,
    signed: Boolean,
    base: NumeralBase,
    overflow: Boolean
): ProgrammerStatusUiModel {
    return ProgrammerStatusUiModel(
        modeLabel = BitwiseRange.modeLabel(wordSize = wordSize, signed = signed, base = base),
        overflowLabel = if (overflow) "Overflow" else null
    )
}
