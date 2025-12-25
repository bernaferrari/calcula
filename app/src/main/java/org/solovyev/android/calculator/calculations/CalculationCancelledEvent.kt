package org.solovyev.android.calculator.calculations

import org.solovyev.android.calculator.jscl.JsclOperation

data class CalculationCancelledEvent(
    override val operation: JsclOperation,
    override val expression: String,
    override val sequence: Long
) : BaseCalculationEvent(operation, expression, sequence)
