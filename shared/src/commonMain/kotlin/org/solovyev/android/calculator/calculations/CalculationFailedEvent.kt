package org.solovyev.android.calculator.calculations

import org.solovyev.android.calculator.jscl.JsclOperation

data class CalculationFailedEvent(
    override val operation: JsclOperation,
    override val expression: String,
    override val sequence: Long,
    val exception: Exception
) : BaseCalculationEvent(operation, expression, sequence)
