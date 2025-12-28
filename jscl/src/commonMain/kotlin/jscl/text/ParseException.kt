package jscl.text

import org.solovyev.common.msg.Message
import org.solovyev.common.msg.MessageLevel
import org.solovyev.common.msg.MessageType

open class ParseException : Exception, Message {

    var position: Int = 0
        private set
    var expression: String = ""
        private set
    private var messageCode: String = ""
    private var parameters: List<*> = emptyList<Any>()

    internal constructor()

    constructor(position: Int, expression: String, messageCode: String, vararg parameters: Any?) {
        set(
            position,
            expression,
            messageCode,
            if (parameters.isEmpty()) emptyList() else parameters.toList()
        )
    }

    internal fun set(position: Int, expression: String, messageCode: String, parameters: List<*>) {
        this.position = position
        this.expression = expression
        this.messageCode = messageCode
        this.parameters = parameters
    }

    override fun getMessageCode(): String = messageCode

    @Suppress("UNCHECKED_CAST")
    override fun getParameters(): List<Any> = parameters as List<Any>

    override fun getMessageLevel(): MessageLevel = MessageType.error

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParseException) return false

        val exception = other

        if (position != exception.position) return false
        if (expression != exception.expression) return false
        if (messageCode != exception.messageCode) return false
        return parameters == exception.parameters
    }

    override fun hashCode(): Int {
        var result = position
        result = 31 * result + expression.hashCode()
        result = 31 * result + messageCode.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }
}
