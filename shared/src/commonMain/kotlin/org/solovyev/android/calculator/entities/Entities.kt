package org.solovyev.android.calculator.entities

import jscl.common.math.MathEntity

object Entities {
    fun <E : MathEntity, C : Category<E>> getCategory(entity: E, categories: Array<C>): Category<E>? {
        return categories.firstOrNull { it.isInCategory(entity) }
    }
}

// Extension function for more idiomatic Kotlin usage
fun <E : MathEntity, C : Category<E>> E.findCategory(categories: Array<C>): Category<E>? {
    return categories.firstOrNull { it.isInCategory(this) }
}
