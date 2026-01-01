package jscl.common.msg

import jscl.common.msg.MessageLevel.Companion.ERROR_LEVEL
import jscl.common.msg.MessageLevel.Companion.INFO_LEVEL
import jscl.common.msg.MessageLevel.Companion.WARNING_LEVEL

enum class MessageType(
    private val messageLevel: Int,
    private val stringValue: String
) : MessageLevel {

    error(ERROR_LEVEL, "ERROR"),
    warning(WARNING_LEVEL, "WARNING"),
    info(INFO_LEVEL, "INFO");

    companion object {
        fun getLowestMessageType(): MessageType {
            return info
        }
    }

    fun getStringValue(): String {
        return stringValue
    }

    override fun getMessageLevel(): Int {
        return messageLevel
    }

    override fun getName(): String {
        return stringValue
    }
}
