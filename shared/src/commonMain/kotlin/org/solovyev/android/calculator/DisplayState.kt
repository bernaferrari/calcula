package org.solovyev.android.calculator

import jscl.math.Generic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.solovyev.android.calculator.jscl.JsclOperation

@Serializable
data class DisplayState(
    val text: String,
    val valid: Boolean,
    val sequence: Long,
    @Transient var operation: JsclOperation = JsclOperation.numeric,
    @Transient var result: Generic? = null
) {

    // fun getResult(): Generic? = result
    // fun getOperation(): JsclOperation = operation

    fun same(that: DisplayState): Boolean {
        return text == that.text && operation == that.operation
    }

    override fun toString(): String {
        return "DisplayState{valid=$valid, sequence=$sequence, operation=$operation}"
    }

    fun isEmpty(): Boolean {
        return valid && text.isEmpty()
    }

    companion object {

        
        fun empty(): DisplayState {
            return DisplayState("", true, Calculator.NO_SEQUENCE)
        }

        
        fun createError(
            operation: JsclOperation,
            errorMessage: String,
            sequence: Long
        ): DisplayState {
            return DisplayState(
                text = errorMessage,
                valid = false,
                sequence = sequence,
                operation = operation
            )
        }

        
        fun createValid(
            operation: JsclOperation,
            result: Generic?,
            stringResult: String,
            sequence: Long
        ): DisplayState {
            return DisplayState(
                text = stringResult,
                valid = true,
                sequence = sequence,
                operation = operation,
                result = result
            )
        }
    }
}
