package jscl.common.math

interface MathRegistry<T : MathEntity> {

    fun getEntities(): List<T>

    fun getSystemEntities(): List<T>

    fun addOrUpdate(t: T): T

    fun remove(variable: T)

    fun getNames(): List<String>

    fun contains(name: String): Boolean

    fun get(name: String): T?

    fun getById(id: Int): T?

    fun init()
}
