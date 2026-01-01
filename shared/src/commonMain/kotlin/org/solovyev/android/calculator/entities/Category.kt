package org.solovyev.android.calculator.entities

import org.jetbrains.compose.resources.StringResource
import jscl.common.math.MathEntity

interface Category<E : MathEntity> {
    fun getCategoryOrdinal(): Int
    fun getCategoryName(): String
    fun isInCategory(entity: E): Boolean

    val title: StringResource
}
