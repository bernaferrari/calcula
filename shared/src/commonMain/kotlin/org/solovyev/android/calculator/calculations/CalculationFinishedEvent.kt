package org.solovyev.android.calculator.calculations

import jscl.math.Generic
import org.solovyev.android.calculator.jscl.JsclOperation
import jscl.common.msg.Message

data class CalculationFinishedEvent(
    override val operation: JsclOperation,
    override val expression: String,
    override val sequence: Long,
    val result: Generic? = null,
    val stringResult: String = "",
    val messages: List<Message> = emptyList()
) : BaseCalculationEvent(operation, expression, sequence) {

    constructor(
        operation: JsclOperation,
        expression: String,
        sequence: Long
    ) : this(operation, expression, sequence, null, "", emptyList())

    override fun toString(): String =
        "CalculationFinishedEvent(super=${super.toString()}, result=$result, stringResult='$stringResult', messages=$messages)"
}
