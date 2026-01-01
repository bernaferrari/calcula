package org.solovyev.android.calculator

import jscl.common.msg.AbstractMessage
import jscl.common.msg.Message
import jscl.common.msg.MessageType
import kotlin.jvm.JvmStatic

class CalculatorMessage : AbstractMessage {

    constructor(
        messageCode: String,
        messageType: MessageType,
        vararg parameters: Any?
    ) : super(messageCode, messageType, *parameters)

    constructor(
        messageCode: String,
        messageType: MessageType,
        parameters: List<*>
    ) : super(messageCode, messageType, parameters)

    companion object {
        fun newInfoMessage(messageCode: String, vararg parameters: Any?): Message {
            return CalculatorMessage(messageCode, MessageType.info, *parameters)
        }

        fun newWarningMessage(messageCode: String, vararg parameters: Any?): Message {
            return CalculatorMessage(messageCode, MessageType.warning, *parameters)
        }

        fun newErrorMessage(messageCode: String, vararg parameters: Any?): Message {
            return CalculatorMessage(messageCode, MessageType.error, *parameters)
        }
    }
}
