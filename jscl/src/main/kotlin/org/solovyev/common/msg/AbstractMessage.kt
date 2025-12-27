package org.solovyev.common.msg

import jscl.i18n.JsclLocale
import org.solovyev.common.text.Strings

abstract class AbstractMessage : Message {

    private val messageCode: String
    private val parameters: List<Any>
    private val messageLevel: MessageLevel

    constructor(messageCode: String, messageType: MessageLevel, vararg parameters: Any?) {
        this.messageCode = messageCode
        this.parameters = if (parameters.isEmpty()) {
            emptyList()
        } else {
            parameters.filterNotNull()
        }
        this.messageLevel = messageType
    }

    constructor(messageCode: String, messageType: MessageLevel, parameters: List<*>) {
        this.messageCode = messageCode
        this.parameters = parameters.filterNotNull().toMutableList()
        this.messageLevel = messageType
    }

    override fun getMessageCode(): String {
        return this.messageCode
    }

    override fun getParameters(): List<Any> {
        return this.parameters.toList()
    }

    override fun getMessageLevel(): MessageLevel {
        return this.messageLevel
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is AbstractMessage) {
            return false
        }

        val that = other

        if (!areEqual(parameters, that.parameters)) {
            return false
        }
        if (messageCode != that.messageCode) {
            return false
        }
        if (messageLevel != that.messageLevel) {
            return false
        }

        return true
    }

    private fun areEqual(thisList: List<Any>, thatList: List<Any>): Boolean {
        if (thisList.size != thatList.size) {
            return false
        }
        for (i in thisList.indices) {
            val thisItem = thisList[i]
            val thatItem = thatList[i]
            if (thisItem != thatItem) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        return arrayOf(messageCode, messageLevel, parameters).contentHashCode()
    }

    /**
     * Method converts message to string setting passed message parameters and translating some of them.
     *
     * @param locale language to which parameters should be translated (if possible)
     * @return message as string with properly translated and set parameters
     */
    override fun getLocalizedMessage(locale: JsclLocale): String {
        return makeMessage(locale, getMessagePattern(locale)?.replace("'", "''") ?: "", parameters, messageLevel)
    }

    override fun getLocalizedMessage(): String {
        return this.getLocalizedMessage(JsclLocale.getDefault())
    }

    protected abstract fun getMessagePattern(locale: JsclLocale): String?

    companion object {
        fun makeMessage(locale: JsclLocale, format: String?, parameters: List<*>, messageLevel: MessageLevel): String {
            if (!Strings.isEmpty(format)) {
                return Messages.prepareMessage(locale, format!!, parameters)
            }

            return messageLevel.getName() + ": message code = " + format
        }
    }
}
