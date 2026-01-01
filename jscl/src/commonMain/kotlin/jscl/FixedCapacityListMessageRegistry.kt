package jscl

import kotlinx.atomicfu.atomic
import jscl.common.msg.Message
import jscl.common.msg.MessageRegistry

class FixedCapacityListMessageRegistry(private val capacity: Int) : MessageRegistry {

    private val messages: MutableList<Message> = ArrayList(capacity)

    private val size = atomic(0)

    override fun addMessage(message: Message) {
        if (!messages.contains(message)) {
            if (size.value <= capacity) {
                messages.add(message)
                size.incrementAndGet()
            } else {
                messages.removeAt(0)
                messages.add(message)
            }
        }
    }

    override fun getMessage(): Message {
        if (hasMessage()) {
            size.decrementAndGet()
            return messages.removeAt(0)
        } else {
            throw IllegalStateException("No messages!")
        }
    }

    override fun hasMessage(): Boolean {
        return size.value > 0
    }
}
