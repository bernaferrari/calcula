package org.solovyev.android.calculator

import org.solovyev.android.calculator.entities.Category
import org.solovyev.common.math.MathEntity
import org.solovyev.common.math.MathRegistry

interface EntitiesRegistry<E : MathEntity> : MathRegistry<E> {
    fun getDescription(name: String): String?

    fun getCategory(entity: E): Category<*>?

    fun save()
}
