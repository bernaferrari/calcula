package jscl.common.msg

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

internal class SynchronizedMessageRegistry private constructor(
    private val delegate: MessageRegistry,
    private val lock: ReentrantLock = ReentrantLock()
) : MessageRegistry {

    companion object {
        fun wrap(delegate: MessageRegistry): MessageRegistry {
            return SynchronizedMessageRegistry(delegate)
        }

        fun wrap(delegate: MessageRegistry, lock: ReentrantLock): MessageRegistry {
            return SynchronizedMessageRegistry(delegate, lock)
        }
    }

    override fun addMessage(message: Message) {
        lock.withLock {
            delegate.addMessage(message)
        }
    }

    override fun hasMessage(): Boolean {
        lock.withLock {
            return delegate.hasMessage()
        }
    }

    override fun getMessage(): Message {
        lock.withLock {
            return delegate.getMessage()
        }
    }
}
