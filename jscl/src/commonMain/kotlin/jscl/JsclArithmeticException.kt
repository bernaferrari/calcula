package jscl

import jscl.text.msg.JsclMessage
import jscl.common.msg.Message
import jscl.common.msg.MessageLevel
import jscl.common.msg.MessageType

abstract class JsclArithmeticException(
    messageCode: String,
    vararg parameters: Any
) : ArithmeticException(), Message {

    private var msg: Message = JsclMessage(messageCode, MessageType.error, *parameters)

    override fun getMessageCode(): String {
        return msg.getMessageCode()
    }

    override fun getParameters(): List<Any> {
        return msg.getParameters()
    }

    override fun getMessageLevel(): MessageLevel {
        return msg.getMessageLevel()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsclArithmeticException) return false

        return msg == other.msg
    }

    override fun hashCode(): Int {
        return msg.hashCode()
    }

    fun setMessage(message: Message) {
        this.msg = message
    }
}
