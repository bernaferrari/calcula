package org.solovyev.android.calculator.calculations

import org.solovyev.android.calculator.DisplayState

data class ConversionFailedEvent(
    override val state: DisplayState
) : BaseConversionEvent(state)
