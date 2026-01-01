package org.solovyev.android.calculator

import jscl.common.msg.Message
import jscl.common.msg.MessageLevel

class ParseException : RuntimeException, Message {

    private val internalMessage: Message
    val expression: String
    val position: Int?

    constructor(jsclParseException: jscl.text.ParseException) {
        this.internalMessage = jsclParseException
        this.expression = jsclParseException.expression
        this.position = jsclParseException.position
    }

    constructor(
        position: Int?,
        expression: String,
        message: Message
    ) {
        this.internalMessage = message
        this.expression = expression
        this.position = position
    }

    constructor(
        expression: String,
        message: Message
    ) : this(null, expression, message)

    override fun getMessageCode(): String {
        return internalMessage.getMessageCode()
    }

    override fun getParameters(): List<Any> {
        return internalMessage.getParameters()
    }

    override fun getMessageLevel(): MessageLevel {
        return internalMessage.getMessageLevel()
    }

    // Removed getLocalizedMessage() dependency on CalculatorMessages static method for KMP.
    // Platform-specific handling should catch ParseException and localize it using ResourceProvider/Context.
    override val message: String?
        get() = internalMessage.getMessageCode()
}
