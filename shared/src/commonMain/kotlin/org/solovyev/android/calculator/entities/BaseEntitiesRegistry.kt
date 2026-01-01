package org.solovyev.android.calculator.entities

import org.solovyev.android.calculator.EntitiesRegistry
import jscl.common.math.MathEntity
import jscl.common.math.MathRegistry

abstract class BaseEntitiesRegistry<T : MathEntity>(
    private val mathRegistry: MathRegistry<T>
) : EntitiesRegistry<T> {

    override fun init() {
        mathRegistry.init()
    }

    override fun getEntities(): List<T> = mathRegistry.getEntities()

    override fun getSystemEntities(): List<T> = mathRegistry.getSystemEntities()

    override fun addOrUpdate(t: T): T = mathRegistry.addOrUpdate(t)

    override fun remove(variable: T) = mathRegistry.remove(variable)

    override fun getNames(): List<String> = mathRegistry.getNames()

    override fun contains(name: String): Boolean = mathRegistry.contains(name)

    override fun get(name: String): T? = mathRegistry.get(name)

    override fun getById(id: Int): T? = mathRegistry.getById(id)
    
    // Default implementation for Shared (no description logic by default, override in Android)
    override fun getDescription(name: String): String? = null
    
    // Default empty save
    override fun save() {}
    
    // Default empty Category logic (impl in App/Android logic if category depends on imports? 
    // Wait, Category class is in shared. So BaseEntitiesRegistry can implement it?)
    // EntitiesRegistry defines getCategory.
    // If Logic is simple, implement here. If not, abstract.
    // BaseEntitiesRegistry usually knows.
    // But overriding Entities.getCategory depends on Entities.
    // Let's implement Abstract first.
}
