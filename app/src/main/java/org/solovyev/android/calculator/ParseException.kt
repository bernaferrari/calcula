package org.solovyev.android.calculator

import jscl.i18n.JsclLocale
import org.solovyev.common.msg.Message
import org.solovyev.common.msg.MessageLevel

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

    override fun getLocalizedMessage(): String {
        return internalMessage.getLocalizedMessage(JsclLocale.getDefault())
    }

    override fun getLocalizedMessage(locale: JsclLocale): String {
        return internalMessage.getLocalizedMessage(locale)
    }

    override val message: String?
        get() = getLocalizedMessage()
}
