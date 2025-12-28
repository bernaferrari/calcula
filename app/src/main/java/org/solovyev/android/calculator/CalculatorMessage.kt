package org.solovyev.android.calculator

import org.solovyev.common.msg.AbstractMessage
import org.solovyev.common.msg.Message
import org.solovyev.common.msg.MessageType

/**
 * User: serso
 * Date: 9/20/12
 * Time: 8:06 PM
 */
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
        @JvmStatic
        fun newInfoMessage(messageCode: String, vararg parameters: Any?): Message {
            return CalculatorMessage(messageCode, MessageType.info, *parameters)
        }

        @JvmStatic
        fun newWarningMessage(messageCode: String, vararg parameters: Any?): Message {
            return CalculatorMessage(messageCode, MessageType.warning, *parameters)
        }

        @JvmStatic
        fun newErrorMessage(messageCode: String, vararg parameters: Any?): Message {
            return CalculatorMessage(messageCode, MessageType.error, *parameters)
        }
    }
}
