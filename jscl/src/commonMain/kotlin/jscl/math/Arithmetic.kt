package jscl.math

interface Arithmetic<T : Arithmetic<T>> {
    fun add(that: T): T
    fun subtract(that: T): T
    fun multiply(that: T): T
    @Throws(NotDivisibleException::class)
    fun divide(that: T): T
}
