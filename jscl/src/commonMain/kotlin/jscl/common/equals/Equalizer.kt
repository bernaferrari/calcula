package jscl.common.equals

interface Equalizer<T> {
    fun areEqual(first: T, second: T): Boolean
}
