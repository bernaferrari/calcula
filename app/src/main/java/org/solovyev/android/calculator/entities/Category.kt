package org.solovyev.android.calculator.entities

import androidx.annotation.StringRes
import org.solovyev.common.math.MathEntity

interface Category<E : MathEntity> {
    fun getCategoryOrdinal(): Int
    fun getCategoryName(): String
    fun isInCategory(entity: E): Boolean

    @get:StringRes
    val title: Int
}
