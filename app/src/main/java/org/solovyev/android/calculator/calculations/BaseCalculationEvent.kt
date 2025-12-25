package org.solovyev.android.calculator.calculations

import org.solovyev.android.calculator.jscl.JsclOperation

abstract class BaseCalculationEvent(
    open val operation: JsclOperation,
    open val expression: String,
    open val sequence: Long
) {
    override fun toString(): String =
        "BaseCalculationEvent(operation=$operation, expression='$expression', sequence=$sequence)"
}
