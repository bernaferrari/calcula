package jscl.common

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

abstract class SynchronizedObject<D> {

    protected val delegate: D

    protected val mutex = ReentrantLock()

    constructor(delegate: D) {
        this.delegate = delegate
    }

    // for manually synchronization it is allows to use mutex
    fun acquireMutex(): ReentrantLock {
        return mutex
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is SynchronizedObject<*>) {
            return false
        }

        mutex.withLock {
            if (delegate != other.delegate) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        mutex.withLock {
            return delegate.hashCode()
        }
    }

    override fun toString(): String {
        mutex.withLock {
            return delegate.toString()
        }
    }
}
