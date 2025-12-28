package org.solovyev.common.msg

class ListMessageRegistry : MessageRegistry {

    private val messages: MutableList<Message> = mutableListOf()

    override fun addMessage(message: Message) {
        if (!messages.contains(message)) {
            messages.add(message)
        }
    }

    override fun getMessage(): Message {
        return this.messages.removeAt(0)
    }

    override fun hasMessage(): Boolean {
        return this.messages.isNotEmpty()
    }
}
