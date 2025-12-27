package org.solovyev.common

abstract class SynchronizedObject<D> {

    protected val delegate: D

    protected val mutex: Any

    constructor(delegate: D) {
        this.delegate = delegate
        this.mutex = this
    }

    constructor(delegate: D, mutex: Any) {
        this.delegate = delegate
        this.mutex = mutex
    }

    // for manually synchronization it is allows to use mutex
    // Note: using explicit getter since mutex property already has one
    fun acquireMutex(): Any {
        return mutex
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is SynchronizedObject<*>) {
            return false
        }

        synchronized(mutex) {
            if (delegate != other.delegate) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        synchronized(mutex) {
            return delegate.hashCode()
        }
    }

    override fun toString(): String {
        synchronized(mutex) {
            return delegate.toString()
        }
    }
}
