package org.solovyev.common.msg

open class AbstractMessage : Message {

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

    companion object
}
