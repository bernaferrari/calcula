package jscl.text.msg

import jscl.common.msg.AbstractMessage
import jscl.common.msg.MessageType

/**
 * User: serso
 * Date: 11/26/11
 * Time: 11:20 AM
 */
open class JsclMessage(
    messageCode: String,
    messageType: MessageType,
    parameters: List<*>
) : AbstractMessage(messageCode, messageType, parameters) {

    constructor(
        messageCode: String,
        messageType: MessageType,
        vararg parameters: Any?
    ) : this(messageCode, messageType, parameters.toList())

}
