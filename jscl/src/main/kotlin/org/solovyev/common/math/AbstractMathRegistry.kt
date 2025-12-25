/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---------------------------------------------------------------------
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

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

    override fun addOrUpdate(entity: T): T {
        synchronized(this) {
            val existingEntity = if (entity.isIdDefined()) getById(entity.getId()) else get(entity.name)
            if (existingEntity == null) {
                addEntity(entity, entities)
                this.entityNames = null
                if (entity.isSystem()) {
                    systemEntities.add(entity)
                }
                return entity
            } else {
                existingEntity.copy(entity)
                this.entities.sort()
                this.entityNames = null
                this.systemEntities.sort()
                return existingEntity
            }
        }
    }

    override fun remove(entity: T) {
        synchronized(this) {
            if (!entity.isSystem()) {
                val removed = removeByName(entities, entity.name)
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
