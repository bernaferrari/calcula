package jscl.common.math

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import jscl.common.collections.SortedList
import jscl.common.text.Strings

/**
 * User: serso
 * Date: 9/29/11
 * Time: 4:57 PM
 */
abstract class AbstractMathRegistry<T : MathEntity> : MathRegistry<T> {

    companion object {
        private val MATH_ENTITY_COMPARATOR = MathEntityComparator<MathEntity>()
        private val counter = atomic(0)

        private fun count(): Int = counter.getAndIncrement()

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

    private val lock = ReentrantLock()

    protected val entities: SortedList<T> = SortedList.newInstance(ArrayList(30), MATH_ENTITY_COMPARATOR)

    private var entityNames: MutableList<String>? = null

    protected val systemEntities: SortedList<T> = SortedList.newInstance(ArrayList(30), MATH_ENTITY_COMPARATOR)

    private val initialized = atomic(false)

    override fun init() {
        if (initialized.value) {
            return
        }
        lock.withLock {
            if (initialized.value) {
                return
            }
            onInit()
            initialized.value = true
        }
    }

    protected abstract fun onInit()

    override fun getEntities(): List<T> {
        lock.withLock {
            return entities.toList()
        }
    }

    override fun getSystemEntities(): List<T> {
        lock.withLock {
            return systemEntities.toList()
        }
    }

    protected fun add(entity: T) {
        lock.withLock {
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
        entity.setId(count())
        list.add(entity)
    }

    override fun addOrUpdate(t: T): T {
        lock.withLock {
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
        lock.withLock {
            if (!variable.isSystem()) {
                val removed = removeByName(entities, variable.name)
                if (removed != null) {
                    this.entityNames = null
                }
            }
        }
    }

    override fun getNames(): List<String> {
        lock.withLock {
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
        lock.withLock {
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
        lock.withLock {
            for (entity in entities) {
                if (areEqual(entity.getId(), id)) {
                    return entity
                }
            }
            return null
        }
    }

    override fun contains(name: String): Boolean {
        lock.withLock {
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
