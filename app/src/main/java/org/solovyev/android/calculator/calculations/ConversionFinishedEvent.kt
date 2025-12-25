package org.solovyev.android.calculator.calculations

import jscl.NumeralBase
import org.solovyev.android.calculator.DisplayState

data class ConversionFinishedEvent(
    val result: String,
    val numeralBase: NumeralBase,
    override val state: DisplayState
) : BaseConversionEvent(state)
