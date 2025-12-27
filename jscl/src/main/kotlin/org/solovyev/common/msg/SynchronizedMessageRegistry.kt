package org.solovyev.common.msg

import org.solovyev.common.SynchronizedObject

internal class SynchronizedMessageRegistry : SynchronizedObject<MessageRegistry>, MessageRegistry {

    private constructor(delegate: MessageRegistry) : super(delegate)

    private constructor(delegate: MessageRegistry, mutex: Any) : super(delegate, mutex)

    companion object {
        fun wrap(delegate: MessageRegistry): MessageRegistry {
            return SynchronizedMessageRegistry(delegate)
        }

        fun wrap(delegate: MessageRegistry, mutex: Any): MessageRegistry {
            return SynchronizedMessageRegistry(delegate, mutex)
        }
    }

    override fun addMessage(message: Message) {
        synchronized(this.mutex) {
            delegate.addMessage(message)
        }
    }

    override fun hasMessage(): Boolean {
        synchronized(this.mutex) {
            return delegate.hasMessage()
        }
    }

    override fun getMessage(): Message {
        synchronized(this.mutex) {
            return delegate.getMessage()
        }
    }
}
