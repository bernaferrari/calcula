package org.solovyev.common.math

import org.solovyev.common.collections.SortedList
import org.solovyev.common.text.Strings

/**
 * User: serso
 * Date: 9/29/11
 * Time: 4:57 PM
 */
abstract class AbstractMathRegistry<T : MathEntity> : MathRegistry<T> {

    companion object {
        private val MATH_ENTITY_COMPARATOR = MathEntityComparator<MathEntity>()

        @Volatile
        private var counter: Int = 0

        @Synchronized
        private fun count(): Int {
            val result = counter
            counter++
            return result
        }

        private fun <E : MathEntity> removeByName(entities: MutableList<E>, name: String): E? {
            for (i in entities.indices) {
                val entity = entities[i]
                if (entity.name == name) {
                    entities.removeAt(i)
                    return entity
                }
            }
            return null
        }

        private fun areEqual(l: Any?, r: Any?): Boolean {
            return l?.equals(r) ?: (r == null)
        }
    }

    protected val entities: SortedList<T> = SortedList.newInstance(ArrayList(30), MATH_ENTITY_COMPARATOR)

    private var entityNames: MutableList<String>? = null

    protected val systemEntities: SortedList<T> = SortedList.newInstance(ArrayList(30), MATH_ENTITY_COMPARATOR)

    @Volatile
    private var initialized: Boolean = false

    override fun init() {
        if (initialized) {
            return
        }
        synchronized(this) {
            if (initialized) {
                return
            }
            onInit()
            initialized = true
        }
    }

    protected abstract fun onInit()

    override fun getEntities(): List<T> {
        synchronized(this) {
            return entities.toList()
        }
    }

    override fun getSystemEntities(): List<T> {
        synchronized(this) {
            return systemEntities.toList()
        }
    }

    protected fun add(entity: T) {
        synchronized(this) {
            if (entity.isSystem()) {
                if (contains(entity.name, this.systemEntities)) {
                    throw IllegalArgumentException("Trying to add two system entities with same name: " + entity.name)
                }

                this.systemEntities.add(entity)
            }

            if (!contains(entity.name, this.entities)) {
                addEntity(entity, this.entities)
                this.entityNames = null
            }
        }
    }

    private fun addEntity(entity: T, list: MutableList<T>) {
        assert(Thread.holdsLock(this))

        entity.setId(count())
        list.add(entity)
    }

    override fun addOrUpdate(t: T): T {
        synchronized(this) {
            val existingEntity = if (t.isIdDefined()) getById(t.getId()) else get(t.name)
            if (existingEntity == null) {
                addEntity(t, entities)
                this.entityNames = null
                if (t.isSystem()) {
                    systemEntities.add(t)
                }
                return t
            } else {
                existingEntity.copy(t)
                this.entities.sort()
                this.entityNames = null
                this.systemEntities.sort()
                return existingEntity
            }
        }
    }

    override fun remove(variable: T) {
        synchronized(this) {
            if (!variable.isSystem()) {
                val removed = removeByName(entities, variable.name)
                if (removed != null) {
                    this.entityNames = null
                }
            }
        }
    }

    override fun getNames(): List<String> {
        synchronized(this) {
            if (entityNames != null) {
                return entityNames!!
            }
            entityNames = ArrayList(entities.size)
            for (entity in entities) {
                val name = entity.name
                if (!Strings.isEmpty(name)) {
                    entityNames!!.add(name)
                }
            }
            return entityNames!!
        }
    }

    override fun get(name: String): T? {
        synchronized(this) {
            return get(name, entities)
        }
    }

    private fun get(name: String, list: List<T>): T? {
        for (i in list.indices) {
            val entity = list[i]
            if (areEqual(entity.name, name)) {
                return entity
            }
        }
        return null
    }

    override fun getById(id: Int): T? {
        synchronized(this) {
            for (entity in entities) {
                if (areEqual(entity.getId(), id)) {
                    return entity
                }
            }
            return null
        }
    }

    override fun contains(name: String): Boolean {
        synchronized(this) {
            return contains(name, this.entities)
        }
    }

    private fun contains(name: String, entities: List<T>): Boolean {
        return get(name, entities) != null
    }

    internal class MathEntityComparator<T : MathEntity> : Comparator<T> {

        override fun compare(l: T, r: T): Int {
            val rName = r.name
            val lName = l.name
            var result = rName.length - lName.length
            if (result == 0) {
                result = lName.compareTo(rName)
            }
            return result
        }
    }
}
