package jscl

import org.solovyev.common.msg.Message
import org.solovyev.common.msg.MessageRegistry

class FixedCapacityListMessageRegistry(private val capacity: Int) : MessageRegistry {

    private val messages: MutableList<Message> = ArrayList(capacity)

    @Volatile
    private var size: Int = 0

    override fun addMessage(message: Message) {
        if (!messages.contains(message)) {
            if (size <= capacity) {
                messages.add(message)
                size++
            } else {
                messages.removeAt(0)
                messages.add(message)
            }
        }
    }

    override fun getMessage(): Message {
        if (hasMessage()) {
            size--
            return messages.removeAt(0)
        } else {
            throw IllegalStateException("No messages!")
        }
    }

    override fun hasMessage(): Boolean {
        return size > 0
    }
}
