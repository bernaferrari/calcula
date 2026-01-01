package org.solovyev.android.calculator.operators

import org.jetbrains.compose.resources.StringResource
import jscl.math.operator.*
import org.solovyev.android.calculator.ui.*
import org.solovyev.android.calculator.entities.Category

sealed class OperatorCategory(override val title: StringResource, private val order: Int) : Category<Operator> {

    object Common : OperatorCategory(Res.string.c_fun_category_common, 0) {
        override fun isInCategory(entity: Operator): Boolean {
            return values().none { it != this && it.isInCategory(entity) }
        }
    }

    object Derivatives : OperatorCategory(Res.string.derivatives, 1) {
        override fun isInCategory(entity: Operator): Boolean {
            return entity is Derivative || entity is Integral || entity is IndefiniteIntegral
        }
    }

    object Other : OperatorCategory(Res.string.other, 2) {
        override fun isInCategory(entity: Operator): Boolean {
            return entity is Sum || entity is Product
        }
    }

    override fun getCategoryOrdinal(): Int = order

    override fun getCategoryName(): String = when (this) {
        Common -> "Common"
        Derivatives -> "Derivatives"
        Other -> "Other"
    }

    companion object {
        fun values(): Array<OperatorCategory> = arrayOf(Common, Derivatives, Other)
    }
}
